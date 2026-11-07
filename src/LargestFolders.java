import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

public class LargestFolders {

    private record FolderSize(Path path, long bytes) {}

    public static void main(String[] args) throws IOException {
        Path root = Paths.get(
            System.getProperty("user.home"),
                "Library", "Containers"
        );

        if (!Files.isDirectory(root)) {
            System.err.println("Directory not found: " + root);
            return;
        }

        List<FolderSize> folders = new ArrayList<>();

        try (Stream<Path> entries = Files.list(root)) {
            entries.filter(Files::isDirectory).forEach(folder -> {
                try {
                    folders.add(new FolderSize(
                        folder, calculateSize(folder)
                    ));
                } catch (IOException e) {
                    System.err.println(
                        "Could not fully scan " + folder + ": "
                        + e.getMessage()
                    );
                }
            });
        }

        folders.sort(
            Comparator.comparingLong(FolderSize::bytes).reversed()
        );

        System.out.printf("%-12s %s%n", "SIZE", "FOLDER");
        System.out.println("-----------------------------------------------");

        for (FolderSize folder : folders) {
            System.out.printf(
                "%-12s %s%n",
                formatSize(folder.bytes()),
                folder.path().getFileName()
            );
        }
    }

    private static long calculateSize(Path folder) throws IOException {
        final long[] total = {0};

        Files.walkFileTree(folder, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(
                    Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    total[0] += attrs.size();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(
                    Path file, IOException exc) {
                System.err.println("Skipping: " + file);
                return FileVisitResult.CONTINUE;
            }
        });

        return total[0];
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";

        String[] units = {"KB", "MB", "GB", "TB"};
        double size = bytes;
        int unit = -1;

        do {
            size /= 1024;
            unit++;
        } while (size >= 1024 && unit < units.length - 1);

        return String.format(Locale.ROOT, "%.2f %s", size, units[unit]);
    }
}