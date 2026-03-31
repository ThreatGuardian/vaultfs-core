package utils;

/** Prints the AuthFS startup banner. */
public class Banner {
    /** Prints the exact ASCII banner lines to the console. */
    public static void print() {
        System.out.println(Colors.c(Colors.CYAN + Colors.BOLD, "    _         _   _       _____ ____"));
        System.out.println(Colors.c(Colors.CYAN + Colors.BOLD, "   / \\  _   _| |_| |__  |  ___/ ___|"));
        System.out.println(Colors.c(Colors.CYAN + Colors.BOLD, "  / _ \\| | | | __| '_ \\ | |_  \\___ " + "\\"));
        System.out.println(Colors.c(Colors.CYAN + Colors.BOLD, " / ___ \\ |_| | |_| | | ||  _|  ___) |"));
        System.out.println(Colors.c(Colors.CYAN + Colors.BOLD, "/_/   \\_\\__,_|\\__|_| |_||_|   |____/"));
        System.out.println("");
        System.out.println(Colors.c(Colors.BOLD, "          AuthFS CLI  v1.0.0"));
        System.out.println(Colors.c(Colors.WHITE, "     Secure File System Simulator"));
        System.out.println(Colors.c(Colors.GRAY, "─────────────────────────────────────"));
    }
}
