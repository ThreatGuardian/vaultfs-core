package datastructures;

import java.util.LinkedList;
import models.FileNode;
import utils.Colors;

/**
 * Manages directory hierarchy operations over a FileNode tree.
 */
public class DirectoryTree {
    private FileNode root;

    /**
     * Creates the root directory node using the provided sandbox path.
     */
    public DirectoryTree(String sandboxPath) {
        this.root = new FileNode("/", sandboxPath, null);
    }

    /**
     * Returns the root directory node.
     */
    public FileNode getRoot() {
        return root;
    }

    /**
     * Finds a directory node by absolute path using BFS traversal.
     */
    public FileNode findNode(String absolutePath) {
        if (absolutePath == null) {
            return null;
        }

        LinkedList<FileNode> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            FileNode current = queue.poll();
            if (current.absolutePath.equals(absolutePath)) {
                return current;
            }

            for (FileNode child : current.children) {
                queue.add(child);
            }
        }

        return null;
    }

    /**
     * Creates and attaches a new child directory under the parent.
     */
    public FileNode insertDirectory(FileNode parent, String name, String absolutePath) {
        FileNode newNode = new FileNode(name, absolutePath, parent);
        parent.addChild(newNode);
        return newNode;
    }

    /**
     * Removes a directory node from its parent unless it is the root.
     */
    public boolean removeDirectory(FileNode node) {
        if (node.parent == null) {
            System.out.println("Cannot remove root");
            return false;
        }

        node.parent.removeChild(node);
        return true;
    }

    /**
     * Prints an ASCII representation of directories and files recursively.
     */
    public void printTree(FileNode node, String prefix, boolean isLast) {
        if (node == null) {
            return;
        }

        if (node.parent == null && "".equals(prefix)) {
            System.out.println(Colors.c(Colors.BLUE + Colors.BOLD, "/"));
        } else {
            System.out.println(prefix.replace("│", Colors.c(Colors.GRAY, "│"))
                    + Colors.c(Colors.GRAY, isLast ? "└── " : "├── ")
                    + Colors.c(Colors.BLUE + Colors.BOLD, node.name + "/"));
        }

        String childPrefix = prefix + (isLast ? "    " : "│   ");
        int childCount = node.children.size();

        int fileCount = 0;
        if (node.files != null) {
            fileCount = node.files.getAll().size();
        }

        for (int i = 0; i < childCount; i++) {
            boolean childIsLast = (i == childCount - 1) && (fileCount == 0);
            printTree(node.children.get(i), childPrefix, childIsLast);
        }

        if (node.files != null) {
            for (int i = 0; i < fileCount; i++) {
                boolean fileIsLast = i == fileCount - 1;
                System.out.println(childPrefix.replace("│", Colors.c(Colors.GRAY, "│"))
                        + Colors.c(Colors.GRAY, fileIsLast ? "└── " : "├── ")
                        + Colors.c(Colors.WHITE, node.files.getAll().get(i).filename));
            }
        }
    }

    /**
     * Prints the full tree starting from the root node.
     */
    public void printTreeFromRoot() {
        printTree(root, "", true);
    }
}
