package filesystem;

import java.io.File;
import java.util.List;
import models.FileMetadata;
import models.FileNode;

/**
 * Encapsulates search and result-formatting operations used by FileSystem.
 */
public class SearchService {
    /** Collects formatted results for files whose metadata type matches the input type. */
    public void collectByType(FileNode node, String type, List<String> results) {
        for (FileMetadata metadata : node.files.getAll()) {
            if (metadata.type.equals(type)) {
                results.add(node.absolutePath + File.separator + metadata.filename + " — " + metadata.formattedSize());
            }
        }

        for (FileNode child : node.children) {
            collectByType(child, type, results);
        }
    }

    /** Formats bytes as B, KB, or MB text for top-k output. */
    public String formatSize(long sizeBytes) {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }
        if (sizeBytes < 1_048_576) {
            return String.format("%.1f KB", sizeBytes / 1024.0);
        }
        return String.format("%.1f MB", sizeBytes / 1_048_576.0);
    }
}
