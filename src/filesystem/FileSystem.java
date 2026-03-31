package filesystem;

import auth.AuthManager;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import sync.FirestoreSync;
import utils.Colors;

/**
 * Core engine that connects data structures and sandbox disk operations.
 */
public class FileSystem {
    private datastructures.DirectoryTree tree;
    private datastructures.FileHeap globalHeap;
    public models.FileNode currentDirectory;
    private utils.JsonExporter exporter;
    private String sandboxPath;

    /** Initializes sandbox paths, data structures, and JSON exporter. */
    public FileSystem() {
        this.sandboxPath = System.getProperty("user.dir") + File.separator + "sandbox";
        new File(sandboxPath).mkdirs();
        this.tree = new datastructures.DirectoryTree(sandboxPath);
        this.globalHeap = new datastructures.FileHeap();
        this.currentDirectory = this.tree.getRoot();
        this.exporter = new utils.JsonExporter(System.getProperty("user.dir") + File.separator + "state.json");
    }

    /** Returns the current directory path for prompt display. */
    public String getCurrentPath() {
        return currentDirectory.absolutePath;
    }

    /** Prints the absolute path of the current directory. */
    public void pwd() {
        System.out.println(Colors.c(Colors.CYAN, currentDirectory.absolutePath));
        exportState();
    }

    /** Changes the current directory using root, parent, single-name, or path traversal rules. */
    public void cd(String path) {
        if (path == null || path.isEmpty()) {
            System.out.println(Colors.c(Colors.RED, "Directory not found: " + path));
            exportState();
            return;
        }

        if ("/".equals(path)) {
            currentDirectory = tree.getRoot();
            exportState();
            return;
        }

        if ("..".equals(path)) {
            if (currentDirectory.parent != null) {
                currentDirectory = currentDirectory.parent;
            } else {
                System.out.println(Colors.c(Colors.RED, "Already at root"));
            }
            exportState();
            return;
        }

        if (path.contains(File.separator) || path.contains("/")) {
            String normalized = path.replace("\\", "/");
            String[] parts = normalized.split("/");
            models.FileNode walker = normalized.startsWith("/") ? tree.getRoot() : currentDirectory;

            boolean failed = false;
            for (String part : parts) {
                if (part.isEmpty() || ".".equals(part)) {
                    continue;
                }
                if ("..".equals(part)) {
                    if (walker.parent != null) {
                        walker = walker.parent;
                    }
                    continue;
                }

                models.FileNode next = walker.getChild(part);
                if (next == null) {
                    System.out.println(Colors.c(Colors.RED, "Directory not found: " + part));
                    failed = true;
                    break;
                }
                walker = next;
            }

            if (!failed) {
                currentDirectory = walker;
            }
            exportState();
            return;
        }

        models.FileNode next = currentDirectory.getChild(path);
        if (next != null) {
            currentDirectory = next;
        } else {
            System.out.println(Colors.c(Colors.RED, "Directory not found: " + path));
        }
        exportState();
    }

    /** Creates a new child directory in memory and in the sandbox on disk. */
    public void mkdir(String name) {
        if (currentDirectory.getChild(name) != null) {
            System.out.println(Colors.c(Colors.RED, "Directory already exists: " + name));
            exportState();
            return;
        }

        String absolutePath = currentDirectory.absolutePath + File.separator + name;
        boolean created = new File(absolutePath).mkdir();
        if (!created) {
            System.out.println(Colors.c(Colors.RED, "Failed to create directory on disk: " + name));
            exportState();
            return;
        }

        tree.insertDirectory(currentDirectory, name, absolutePath);
        System.out.println("Directory '" + Colors.c(Colors.BLUE, name) + "' "
                + Colors.c(Colors.GREEN, "created successfully") + ".");
        exportState();
    }

    /** Removes a child directory with optional forced recursive deletion. */
    public void rmdir(String name, boolean force) {
        models.FileNode node = currentDirectory.getChild(name);
        if (node == null) {
            System.out.println(Colors.c(Colors.RED, "Directory not found: " + name));
            exportState();
            return;
        }

        boolean hasChildren = node.children != null && !node.children.isEmpty();
        boolean hasFiles = node.files != null && node.files.size() > 0;

        if (!force && (hasChildren || hasFiles)) {
            System.out.println(Colors.c(Colors.RED, "Directory not empty. Use rmdir -f " + name));
            exportState();
            return;
        }

        if (force) {
            deleteDiskRecursive(new File(node.absolutePath));
        } else {
            new File(node.absolutePath).delete();
        }

        tree.removeDirectory(node);
        System.out.println("Directory '" + Colors.c(Colors.BLUE, name) + "' "
            + Colors.c(Colors.GREEN, "removed successfully") + ".");
        exportState();
    }

    /** Recursively deletes a file or directory from disk. */
    private void deleteDiskRecursive(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDiskRecursive(child);
                }
            }
        }
        f.delete();
    }

    /** Renames a child directory in memory and on disk. */
    public void renameDirectory(String oldName, String newName) {
        models.FileNode node = currentDirectory.getChild(oldName);
        if (node == null) {
            System.out.println(Colors.c(Colors.RED, "Directory not found: " + oldName));
            exportState();
            return;
        }

        String oldPath = node.absolutePath;
        String newPath = currentDirectory.absolutePath + File.separator + newName;
        boolean renamed = new File(oldPath).renameTo(new File(newPath));
        if (!renamed) {
            System.out.println(Colors.c(Colors.RED, "Failed to rename directory: " + oldName));
            exportState();
            return;
        }

        node.name = newName;
        node.absolutePath = newPath;
        System.out.println("Directory renamed successfully.");
        exportState();
    }

    /** Creates a file entry in list/map/heap and creates the file on disk. */
    public void createFile(String filename, long sizeBytes) {
        if (currentDirectory.fileIndex.contains(filename)) {
            System.out.println(Colors.c(Colors.RED, "File already exists: " + filename));
            exportState();
            return;
        }

        String filePath = currentDirectory.absolutePath + File.separator + filename;
        try {
            boolean created = new File(filePath).createNewFile();
            if (!created) {
                System.out.println(Colors.c(Colors.RED, "Failed to create file on disk: " + filename));
                exportState();
                return;
            }
        } catch (IOException e) {
            System.out.println(Colors.c(Colors.RED, "Failed to create file: " + e.getMessage()));
            exportState();
            return;
        }

        models.FileMetadata m = new models.FileMetadata(filename, sizeBytes);
        currentDirectory.files.add(m);
        currentDirectory.fileIndex.put(filename, m);
        globalHeap.insert(filename, filePath, sizeBytes);
        System.out.println("File '" + Colors.c(Colors.WHITE, filename) + "' "
            + Colors.c(Colors.GREEN, "created successfully")
            + " with size " + Colors.c(Colors.CYAN, m.formattedSize()));
        exportState();
    }

    /** Deletes a file from list/map/heap and removes it from disk. */
    public void deleteFile(String filename) {
        if (!currentDirectory.fileIndex.contains(filename)) {
            System.out.println(Colors.c(Colors.RED, "File not found: " + filename));
            exportState();
            return;
        }

        String filePath = currentDirectory.absolutePath + File.separator + filename;
        new File(filePath).delete();
        currentDirectory.files.remove(filename);
        currentDirectory.fileIndex.remove(filename);
        globalHeap.remove(filePath);
        System.out.println("File '" + Colors.c(Colors.WHITE, filename) + "' "
            + Colors.c(Colors.GREEN, "deleted successfully") + ".");
        exportState();
    }

    /** Renames a file and synchronizes linked list, hash map, heap, and disk. */
    public void renameFile(String oldName, String newName) {
        models.FileMetadata oldMetadata = currentDirectory.fileIndex.get(oldName);
        if (oldMetadata == null) {
            System.out.println(Colors.c(Colors.RED, "File not found: " + oldName));
            exportState();
            return;
        }

        String oldPath = currentDirectory.absolutePath + File.separator + oldName;
        String newPath = currentDirectory.absolutePath + File.separator + newName;
        boolean renamed = new File(oldPath).renameTo(new File(newPath));
        if (!renamed) {
            System.out.println(Colors.c(Colors.RED, "Failed to rename file: " + oldName));
            exportState();
            return;
        }

        currentDirectory.files.remove(oldName);
        currentDirectory.fileIndex.remove(oldName);

        models.FileMetadata newMetadata = new models.FileMetadata(newName, oldMetadata.sizeBytes);
        currentDirectory.files.add(newMetadata);
        currentDirectory.fileIndex.put(newName, newMetadata);

        globalHeap.remove(oldPath);
        globalHeap.insert(newName, newPath, newMetadata.sizeBytes);

        System.out.println("File renamed successfully.");
        exportState();
    }

    /** Lists subdirectories first and then files in simple or detailed mode. */
    public void ls(boolean detailed) {
        boolean hasDirectories = currentDirectory.children != null && !currentDirectory.children.isEmpty();
        ArrayList<models.FileMetadata> files = currentDirectory.files.getAll();
        boolean hasFiles = !files.isEmpty();

        if (!hasDirectories && !hasFiles) {
            System.out.println(Colors.c(Colors.RED, "(empty directory)"));
            exportState();
            return;
        }

        if (hasDirectories) {
            for (models.FileNode child : currentDirectory.children) {
                System.out.println(Colors.c(Colors.BLUE + Colors.BOLD, child.name + "/"));
            }
        }

        if (hasFiles) {
            if (detailed) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                System.out.println(Colors.c(Colors.GRAY, "NAME                         SIZE       TYPE     MODIFIED"));
                for (models.FileMetadata m : files) {
                    String row = String.format("%-28s %-10s %-8s %s",
                            m.filename,
                            Colors.c(Colors.CYAN, m.formattedSize()),
                            m.type,
                            m.modifiedAt.format(formatter));
                    System.out.println(row);
                }
            } else {
                for (models.FileMetadata m : files) {
                    System.out.println(Colors.c(Colors.WHITE, m.filename));
                }
            }
        }
        exportState();
    }

    /** Prints complete metadata information for a file in the current directory. */
    public void info(String filename) {
        models.FileMetadata m = currentDirectory.fileIndex.get(filename);
        if (m == null) {
            System.out.println(Colors.c(Colors.RED, "File not found"));
            exportState();
            return;
        }

        System.out.println(Colors.c(Colors.GRAY, "Filename: ") + Colors.c(Colors.WHITE, m.filename));
        System.out.println(Colors.c(Colors.GRAY, "Size: ") + Colors.c(Colors.WHITE, m.sizeBytes + " bytes"));
        System.out.println(Colors.c(Colors.GRAY, "Type: ") + Colors.c(Colors.WHITE, m.type));
        System.out.println(Colors.c(Colors.GRAY, "CreatedAt: ") + Colors.c(Colors.WHITE, String.valueOf(m.createdAt)));
        System.out.println(Colors.c(Colors.GRAY, "ModifiedAt: ") + Colors.c(Colors.WHITE, String.valueOf(m.modifiedAt)));
        exportState();
    }

    /** Finds all matching filenames in the tree and prints absolute file paths. */
    public void find(String filename) {
        List<String> results = new ArrayList<>();
        findHelper(tree.getRoot(), filename, results);

        if (results.isEmpty()) {
            System.out.println(Colors.c(Colors.RED, "No file named '" + filename + "' found"));
        } else {
            for (String path : results) {
                System.out.println(Colors.c(Colors.CYAN, path));
            }
        }
        exportState();
    }

    /** Performs DFS and prints files whose type matches the requested extension. */
    public void searchByType(String type) {
        List<String> results = new ArrayList<>();
        collectByType(tree.getRoot(), type, results);

        if (results.isEmpty()) {
            System.out.println(Colors.c(Colors.RED, "No files of type '." + type + "' found"));
        } else {
            for (String line : results) {
                System.out.println(line);
            }
        }
        exportState();
    }

    /** Prints the directory tree from root or from a resolved absolute path node. */
    public void tree(String path) {
        if (path == null || path.isEmpty()) {
            this.tree.printTreeFromRoot();
            exportState();
            return;
        }

        String absolute = path;
        if (!absolute.startsWith(sandboxPath)) {
            if (absolute.startsWith("/")) {
                absolute = sandboxPath + absolute;
            } else {
                absolute = sandboxPath + File.separator + absolute;
            }
        }
        absolute = absolute.replace("/", File.separator).replace("\\", File.separator);

        models.FileNode node = this.tree.findNode(absolute);
        if (node == null) {
            System.out.println(Colors.c(Colors.RED, "Directory not found: " + path));
        } else {
            this.tree.printTree(node, "", true);
        }
        exportState();
    }

    /** Prints ranked top-k largest files globally or within a subtree path scope. */
    public void topK(int k, String path) {
        ArrayList<datastructures.FileHeap.HeapEntry> results;

        if (path == null) {
            results = globalHeap.topK(k);
        } else {
            String absolute = path;
            if (!absolute.startsWith(sandboxPath)) {
                if (absolute.startsWith("/")) {
                    absolute = sandboxPath + absolute;
                } else {
                    absolute = sandboxPath + File.separator + absolute;
                }
            }
            absolute = absolute.replace("/", File.separator).replace("\\", File.separator);
            models.FileNode node = tree.findNode(absolute);
            if (node == null) {
                System.out.println(Colors.c(Colors.RED, "Directory not found: " + path));
                exportState();
                return;
            }

            datastructures.FileHeap tempHeap = new datastructures.FileHeap();
            collectFilesIntoHeap(node, tempHeap);
            results = tempHeap.topK(k);
        }

        if (results.isEmpty()) {
            System.out.println(Colors.c(Colors.RED, "No files found"));
            exportState();
            return;
        }

        for (int i = 0; i < results.size(); i++) {
            datastructures.FileHeap.HeapEntry entry = results.get(i);
            String rank = Colors.c(Colors.YELLOW, (i + 1) + ".");
            String file = Colors.c(Colors.WHITE, entry.filename);
            String size = Colors.c(Colors.CYAN, formatSize(entry.sizeBytes));
            System.out.println(rank + " " + file + " — " + size + " — " + entry.absolutePath);
        }
        exportState();
    }

    /** Collects all files under a subtree and inserts them into a temporary heap. */
    private void collectFilesIntoHeap(models.FileNode node, datastructures.FileHeap tempHeap) {
        for (models.FileMetadata m : node.files.getAll()) {
            String absoluteFilePath = node.absolutePath + File.separator + m.filename;
            tempHeap.insert(m.filename, absoluteFilePath, m.sizeBytes);
        }

        for (models.FileNode child : node.children) {
            collectFilesIntoHeap(child, tempHeap);
        }
    }

    /** Finds matching filenames recursively and appends full absolute file paths to results. */
    private void findHelper(models.FileNode node, String filename, List<String> results) {
        if (node.files.contains(filename)) {
            results.add(node.absolutePath + File.separator + filename);
        }

        for (models.FileNode child : node.children) {
            findHelper(child, filename, results);
        }
    }

    /** Collects formatted results for files whose metadata type matches the input type. */
    private void collectByType(models.FileNode node, String type, List<String> results) {
        for (models.FileMetadata m : node.files.getAll()) {
            if (m.type.equals(type)) {
                results.add(node.absolutePath + File.separator + m.filename + " — " + m.formattedSize());
            }
        }

        for (models.FileNode child : node.children) {
            collectByType(child, type, results);
        }
    }

    /** Formats bytes as B, KB, or MB text for top-k output. */
    private String formatSize(long sizeBytes) {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }
        if (sizeBytes < 1_048_576) {
            return String.format("%.1f KB", sizeBytes / 1024.0);
        }
        return String.format("%.1f MB", sizeBytes / 1_048_576.0);
    }

    /** Exports the current root, active directory, and global heap state to state.json. */
    public void exportState() {
        exporter.export(tree.getRoot(), currentDirectory, globalHeap);
        if (AuthManager.isLoggedIn()) {
            try {
                String stateContent = new String(
                        java.nio.file.Files.readAllBytes(
                                java.nio.file.Paths.get(
                                        System.getProperty("user.dir") + java.io.File.separator + "state.json"
                                )
                        )
                );
                FirestoreSync.push(
                        AuthManager.getUserEmail(),
                        AuthManager.getDeviceId(),
                        stateContent
                );
            } catch (Exception e) {
                // silent fail — never crash CLI on sync error
            }
        }
    }
}
