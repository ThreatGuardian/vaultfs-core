package utils;

/** Prints the VaultFS startup banner with a smooth gradient. */
public class Banner {

    private static final String[][] ROWS = buildRows();

    private static String[][] buildRows() {
        String b = "\u2588\u2588";
        return new String[][] {
            //  V          A          U          L        T        F          S
            {b+"  "+b, " "+b+b+" ", b+"  "+b, b+"    ", b+b+b, b+b+b, " "+b+b+" "},
            {b+"  "+b, b+"  "+b,   b+"  "+b, b+"    ", "  "+b+"  ", b+"    ", b+"    "},
            {b+"  "+b, b+b+b,     b+"  "+b, b+"    ", "  "+b+"  ", b+b+"  ", " "+b+b+" "},
            {" "+b+b+" ", b+"  "+b, b+"  "+b, b+"    ", "  "+b+"  ", b+"    ", "    "+b},
            {"  "+b+"  ", b+"  "+b, " "+b+b+" ", b+b+b, "  "+b+"  ", b+"    ", " "+b+b+" "},
        };
    }

    private static final int[][] STARTS = {
        {66, 165, 245}, {80, 145, 248}, {100, 130, 250}, {130, 110, 245}, {160, 95, 235}
    };
    private static final int[][] ENDS = {
        {190, 120, 240}, {210, 110, 225}, {225, 100, 210}, {236, 90, 195}, {240, 80, 180}
    };

    /** Prints the gradient ASCII banner to the console. */
    public static void print() {
        String version = getVersion();
        System.out.println();
        for (int row = 0; row < 5; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < ROWS[row].length; col++) {
                if (col == 5) line.append("   ");      // extra gap before F (VAULT ___ FS)
                else if (col > 0) line.append(" ");     // normal 1-space gap
                line.append(ROWS[row][col]);
            }
            System.out.println(gradientLine(line.toString(), STARTS[row], ENDS[row]));
        }
        System.out.println();
        System.out.println(gradientLine("        VaultFS CLI  v" + version,
                new int[]{140, 100, 250}, new int[]{236, 90, 195}));
        System.out.println(Colors.c(Colors.WHITE, "    Secure File System Simulator"));
        System.out.println(Colors.c(Colors.GRAY, "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
    }

    /** Reads version from version.txt (checks vaultfs.home then user.dir). */
    private static String getVersion() {
        String[] bases = {
            System.getProperty("vaultfs.home"),
            System.getProperty("user.dir")
        };
        for (String base : bases) {
            if (base == null) continue;
            java.io.File f = new java.io.File(base, "version.txt");
            if (f.exists()) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
                    String line = br.readLine();
                    if (line != null && !line.trim().isEmpty()) return line.trim();
                } catch (Exception ignored) {}
            }
        }
        return "0.0.0";
    }

    private static String gradientLine(String text, int[] s, int[] e) {
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == ' ') { sb.append(' '); continue; }
            float r = len > 1 ? (float) i / (len - 1) : 0;
            int red = (int)(s[0] + r * (e[0] - s[0]));
            int grn = (int)(s[1] + r * (e[1] - s[1]));
            int blu = (int)(s[2] + r * (e[2] - s[2]));
            sb.append(String.format("\u001B[38;2;%d;%d;%dm%c", red, grn, blu, c));
        }
        sb.append("\u001B[0m");
        return sb.toString();
    }
}
