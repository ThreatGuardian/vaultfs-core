package utils;

/** Provides ANSI colors with runtime-safe terminal support checks. */
public class Colors {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String GRAY = "\u001B[90m";
    public static final String BOLD = "\u001B[1m";

    /** Returns true when ANSI colors are likely supported by the terminal. */
    public static boolean isSupported() {
        String osName = System.getProperty("os.name").toLowerCase();
        return System.getenv("WT_SESSION") != null
                || System.getenv("TERM") != null
                || System.getenv("COLORTERM") != null
                || osName.contains("mac")
                || osName.contains("linux");
    }

    /** Wraps text in a color code when supported, otherwise returns plain text. */
    public static String c(String colorCode, String text) {
        if (isSupported()) {
            return colorCode + text + RESET;
        }
        return text;
    }
}
