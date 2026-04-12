package utils;

/**
 * Lightweight logger with optional debug output.
 */
public final class Logger {
    private static boolean debugEnabled = Boolean.parseBoolean(System.getenv().getOrDefault("VAULTFS_DEBUG", "false"));

    private Logger() {
    }

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public static void info(String message) {
        System.out.println(message);
    }

    public static void warn(String message) {
        System.out.println(Colors.c(Colors.YELLOW, message));
    }

    public static void error(String message) {
        System.out.println(Colors.c(Colors.RED, message));
    }

    public static void debug(String message) {
        if (debugEnabled) {
            System.out.println(Colors.c(Colors.GRAY, "[debug] " + message));
        }
    }
}
