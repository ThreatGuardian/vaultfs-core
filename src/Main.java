import auth.AuthManager;
import java.util.Scanner;
import utils.Banner;
import utils.Colors;

/** Entry point for the File System Simulator CLI. */
public class Main {
    /** Starts the command loop and routes input to FileSystem operations. */
    public static void main(String[] args) {
        Banner.print();

        // Auto login if not logged in
        if (!AuthManager.isLoggedIn()) {
            AuthManager.startLoginFlow();
        }

        // Show welcome
        System.out.println(Colors.c(Colors.WHITE, "Welcome back, ") + Colors.c(Colors.YELLOW, AuthManager.getUserEmail()) + Colors.c(Colors.WHITE, "!"));
        System.out.println(Colors.c(Colors.GRAY, "─────────────────────────────────────"));
        System.out.println();

        filesystem.FileSystem fs = new filesystem.FileSystem();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(Colors.c(Colors.GREEN + Colors.BOLD, fs.currentDirectory.absolutePath + "> "));
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            String[] tokens = line.trim().split("\\s+");
            String command = tokens[0];
            try {
                if ("pwd".equals(command)) {
                    fs.pwd();
                } else if ("cd".equals(command)) {
                    fs.cd(tokens[1]);
                } else if ("mkdir".equals(command)) {
                    fs.mkdir(tokens[1]);
                } else if ("rmdir".equals(command) && "-f".equals(tokens[1])) {
                    fs.rmdir(tokens[2], true);
                } else if ("rmdir".equals(command)) {
                    fs.rmdir(tokens[1], false);
                } else if ("rename".equals(command)) {
                    if (fs.currentDirectory.fileIndex.contains(tokens[1])) {
                        fs.renameFile(tokens[1], tokens[2]);
                    } else {
                        fs.renameDirectory(tokens[1], tokens[2]);
                    }
                } else if ("create".equals(command)) {
                    fs.createFile(tokens[1], Long.parseLong(tokens[2]));
                } else if ("delete".equals(command)) {
                    fs.deleteFile(tokens[1]);
                } else if ("info".equals(command)) {
                    fs.info(tokens[1]);
                } else if ("find".equals(command)) {
                    fs.find(tokens[1]);
                } else if ("search".equals(command) && "-t".equals(tokens[1])) {
                    fs.searchByType(tokens[2]);
                } else if ("search".equals(command)) {
                    fs.find(tokens[1]);
                } else if ("ls".equals(command) && "-l".equals(tokens[1])) {
                    fs.ls(true);
                } else if ("ls".equals(command)) {
                    fs.ls(false);
                } else if ("tree".equals(command) && tokens.length > 1) {
                    fs.tree(tokens[1]);
                } else if ("tree".equals(command)) {
                    fs.tree(null);
                } else if ("topk".equals(command) && tokens.length > 2) {
                    fs.topK(Integer.parseInt(tokens[1]), tokens[2]);
                } else if ("topk".equals(command)) {
                    fs.topK(Integer.parseInt(tokens[1]), null);
                } else if ("help".equals(command)) {
                    printHelp();
                } else if ("whoami".equals(command)) {
                    AuthManager.whoami();
                } else if ("logout".equals(command)) {
                    AuthManager.logout();
                    break;
                } else if ("clear".equals(command)) {
                    clearScreen();
                } else if ("exit".equals(command)) {
                    System.out.println(Colors.c(Colors.GREEN, "Goodbye!"));
                    break;
                } else {
                    System.out.println(Colors.c(Colors.RED, "[command not found] type 'help' to see all commands"));
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(Colors.c(Colors.RED, "Missing argument for '" + command + "'. Type 'help' for usage."));
            } catch (NumberFormatException e) {
                System.out.println(Colors.c(Colors.RED, "Invalid number. Usage: create <name> <bytes> or topk <k>"));
            } catch (Exception e) {
                System.out.println(Colors.c(Colors.RED, "Error: " + e.getMessage()));
            }
        }

        scanner.close();
    }

    /** Prints the complete command help table. */
    private static void printHelp() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║           File System Simulator — Commands           ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "pwd") + "                      " + Colors.c(Colors.WHITE, "Print current path") + "          ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "cd <name>") + "                " + Colors.c(Colors.WHITE, "Navigate into directory") + "     ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "cd ..") + "                    " + Colors.c(Colors.WHITE, "Go one level up") + "             ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "cd /") + "                     " + Colors.c(Colors.WHITE, "Go to root") + "                  ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "mkdir <name>") + "             " + Colors.c(Colors.WHITE, "Create directory") + "            ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "rmdir <name>") + "             " + Colors.c(Colors.WHITE, "Delete empty directory") + "      ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "rmdir -f <name>") + "          " + Colors.c(Colors.WHITE, "Force delete directory") + "      ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "rename <old> <new>") + "       " + Colors.c(Colors.WHITE, "Rename file or directory") + "    ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "create <name> <bytes>") + "    " + Colors.c(Colors.WHITE, "Create file") + "                 ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "delete <name>") + "            " + Colors.c(Colors.WHITE, "Delete file") + "                 ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "info <name>") + "              " + Colors.c(Colors.WHITE, "Show file metadata") + "          ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "find <name>") + "              " + Colors.c(Colors.WHITE, "Find file in tree") + "           ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "search -t <type>") + "         " + Colors.c(Colors.WHITE, "Search files by type") + "        ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "ls") + "                       " + Colors.c(Colors.WHITE, "List current directory") + "      ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "ls -l") + "                    " + Colors.c(Colors.WHITE, "Detailed listing") + "            ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "tree") + "                     " + Colors.c(Colors.WHITE, "Print full directory tree") + "   ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "tree <path>") + "              " + Colors.c(Colors.WHITE, "Print subtree") + "              ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "topk <k>") + "                 " + Colors.c(Colors.WHITE, "Top k largest files") + "         ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "topk <k> <path>") + "          " + Colors.c(Colors.WHITE, "Top k in specific path") + "      ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "whoami") + "                   " + Colors.c(Colors.WHITE, "Show account details") + "          ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "logout") + "                   " + Colors.c(Colors.WHITE, "Logout from AuthFS") + "           ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "clear") + "                    " + Colors.c(Colors.WHITE, "Clear terminal") + "              ║");
        System.out.println("║ " + Colors.c(Colors.YELLOW, "exit") + "                     " + Colors.c(Colors.WHITE, "Exit program") + "                ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    /** Prints 50 newlines to clear the terminal view. */
    private static void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
}
