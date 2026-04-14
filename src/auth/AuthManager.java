package auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

import utils.Colors;

/** Manages VaultFS login state, device identity, and account display.
 *  Authentication is delegated to a remote auth server; the CLI polls for results. */
public class AuthManager {

    private static final String TOKEN_DIR = System.getProperty("user.home") + "/.authfs";
    private static final String TOKEN_FILE = TOKEN_DIR + "/token";
    private static final String EMAIL_FILE = TOKEN_DIR + "/email";
    private static final String NAME_FILE = TOKEN_DIR + "/name";
    private static final String CONFIG_FILE = TOKEN_DIR + "/config";

    /** Returns whether a non-empty token file exists. */
    public static boolean isLoggedIn() {
        File token = new File(TOKEN_FILE);
        return token.exists() && token.length() > 0;
    }

    /** Returns the saved user email or Unknown when not available. */
    public static String getUserEmail() {
        String email = readFile(EMAIL_FILE);
        if (email == null || email.isEmpty()) {
            return "Unknown";
        }
        return email;
    }

    /** Returns the saved user name or Unknown when not available. */
    public static String getUserName() {
        String name = readFile(NAME_FILE);
        if (name == null || name.isEmpty()) {
            return "Unknown";
        }
        return name;
    }

    /** Returns the device ID from config or generates and persists a new UUID. */
    public static String getDeviceId() {
        String deviceId = readFile(CONFIG_FILE);
        if (deviceId != null && !deviceId.isEmpty()) {
            return deviceId;
        }
        new File(TOKEN_DIR).mkdirs();
        String generated = UUID.randomUUID().toString();
        writeFile(CONFIG_FILE, generated);
        return generated;
    }

    /** Starts the remote auth-server login flow with browser + polling. */
    public static void startLoginFlow() {
        try {
            // Step 1 — Get a session ID from auth server
            String sessionId = getNewSession();
            if (sessionId == null) {
                System.out.println(Colors.c(Colors.YELLOW, "\u26A0\uFE0F  Auth server unreachable. Continuing as Guest."));
                persistLogin(UUID.randomUUID().toString(), "guest@vaultfs.local", "Guest");
                return;
            }

            // Step 2 — Build login URL
            String loginUrl = AuthConfig.AUTH_SERVER_URL + "/auth/login?sessionId=" + sessionId;

            // Step 3 — Print login box
            System.out.println();
            System.out.println(Colors.c(Colors.CYAN, "  \u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557"));
            System.out.println(Colors.c(Colors.CYAN, "  \u2551              VaultFS \u2014 Login                         \u2551"));
            System.out.println(Colors.c(Colors.CYAN, "  \u2551                                                      \u2551"));
            System.out.println(Colors.c(Colors.CYAN, "  \u2551  Opening browser for login...                        \u2551"));
            System.out.println(Colors.c(Colors.CYAN, "  \u2551                                                      \u2551"));
            System.out.println(Colors.c(Colors.CYAN, "  \u2551  If browser doesn't open, visit:                     \u2551"));
            System.out.println(Colors.c(Colors.CYAN, "  \u2551  ") + Colors.c(Colors.GREEN, loginUrl));
            System.out.println(Colors.c(Colors.CYAN, "  \u2551                                                      \u2551"));
            System.out.println(Colors.c(Colors.CYAN, "  \u2551  Press ENTER to skip and continue as Guest           \u2551"));
            System.out.println(Colors.c(Colors.CYAN, "  \u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D"));
            System.out.println();

            // Step 4 — Open browser
            openBrowser(loginUrl);

            // Step 5 — Poll auth server for result
            pollForLogin(sessionId);

        } catch (Exception e) {
            System.out.println(Colors.c(Colors.YELLOW, "\u26A0\uFE0F  Login failed: " + e.getMessage()));
            persistLogin(UUID.randomUUID().toString(), "guest@vaultfs.local", "Guest");
        }
    }

    /** Requests a new auth session from the remote server. */
    private static String getNewSession() {
        try {
            URL url = URI.create(AuthConfig.AUTH_SERVER_URL + "/auth/session/new").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            String response = readStream(conn.getInputStream());
            return extractJsonValue(response, "sessionId");
        } catch (Exception e) {
            return null;
        }
    }

    /** Polls the auth server for login completion, with ENTER-to-skip support. */
    private static void pollForLogin(String sessionId) {
        final boolean[] skipLogin = {false};
        Thread inputThread = new Thread(() -> {
            try {
                System.in.read();
                skipLogin[0] = true;
            } catch (Exception ignored) {}
        });
        inputThread.setDaemon(true);
        inputThread.start();

        System.out.println(Colors.c(Colors.GRAY, "\uD83D\uDD10 Waiting for login... (press ENTER to skip)"));
        System.out.println();

        int waited = 0;
        while (waited < AuthConfig.LOGIN_TIMEOUT_SECONDS && !skipLogin[0]) {
            try {
                Thread.sleep(AuthConfig.POLL_INTERVAL_MS);
                waited += 2;

                URL url = URI.create(AuthConfig.AUTH_SERVER_URL + "/auth/poll?sessionId=" + sessionId).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                String response = readStream(conn.getInputStream());

                if (response.contains("\"status\":\"done\"")) {
                    String name = extractJsonValue(response, "name");
                    String email = extractJsonValue(response, "email");
                    String provider = extractJsonValue(response, "provider");
                    System.out.println(Colors.c(Colors.GREEN, "\u2713") + " Logged in as "
                        + Colors.c(Colors.YELLOW, name) + " via " + provider);
                    persistLogin(UUID.randomUUID().toString(), email, name);
                    return;
                }

                if (response.contains("\"status\":\"expired\"")) {
                    break;
                }

                if (waited % 10 == 0) {
                    System.out.println(Colors.c(Colors.GRAY, "   Still waiting... ("
                        + (AuthConfig.LOGIN_TIMEOUT_SECONDS - waited) + "s remaining, press ENTER to skip)"));
                }

            } catch (Exception ignored) {}
        }

        System.out.println(Colors.c(Colors.YELLOW, "\u26A0\uFE0F  Login skipped. Continuing as Guest."));
        persistLogin(UUID.randomUUID().toString(), "guest@vaultfs.local", "Guest");
    }

    /** Opens a URL in the default browser using platform-specific commands. */
    private static void openBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.contains("win")) {
                rt.exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            } else if (os.contains("mac")) {
                rt.exec(new String[]{"open", url});
            } else {
                String[] browsers = {"xdg-open", "firefox", "google-chrome", "chromium-browser"};
                boolean opened = false;
                for (String browser : browsers) {
                    try {
                        rt.exec(new String[]{browser, url});
                        opened = true;
                        break;
                    } catch (Exception ignored) {}
                }
                if (!opened) {
                    System.out.println("Please open this URL manually: " + url);
                }
            }
        } catch (Exception e) {
            System.out.println("Could not open browser automatically.");
            System.out.println("Please open this URL manually: " + url);
        }
    }

    /** Clears local auth files and logs the user out. */
    public static void logout() {
        new File(TOKEN_FILE).delete();
        new File(EMAIL_FILE).delete();
        new File(NAME_FILE).delete();
        System.out.println(Colors.c(Colors.GREEN, "\u2713") + " Logged out successfully");
    }

    /** Prints formatted account details when logged in. */
    public static void whoami() {
        if (!isLoggedIn()) {
            System.out.println(Colors.c(Colors.RED, "Not logged in."));
            return;
        }
        System.out.println(Colors.c(Colors.GRAY, "==== Account Details ===="));
        System.out.println("Email    : " + Colors.c(Colors.YELLOW, getUserEmail()));
        System.out.println("Device ID: " + Colors.c(Colors.CYAN, getDeviceId()));
        System.out.println("Status   : " + Colors.c(Colors.GREEN, "\u25CF Online"));
        System.out.println(Colors.c(Colors.GRAY, "========================="));
    }

    /** Writes file content to the given path and reports failures. */
    private static void writeFile(String path, String content) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content == null ? "" : content);
        } catch (IOException e) {
            System.out.println(Colors.c(Colors.RED, "Failed to write file: " + path));
        }
    }

    /** Reads and trims file content or returns null on failure. */
    private static String readFile(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString().trim();
        } catch (IOException e) {
            return null;
        }
    }

    /** Reads all bytes from an InputStream and returns as a String. */
    private static String readStream(InputStream is) throws IOException {
        return new String(is.readAllBytes());
    }

    /** Extracts a simple JSON string value by key. */
    private static String extractJsonValue(String json, String key) {
        try {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search) + search.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "unknown";
        }
    }

    /** Persists login credentials to local auth files. */
    private static void persistLogin(String token, String email, String name) {
        new File(TOKEN_DIR).mkdirs();
        writeFile(TOKEN_FILE, token);
        writeFile(EMAIL_FILE, email);
        writeFile(NAME_FILE, name);
    }
}
