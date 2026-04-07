package auth;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/** Reads OAuth client credentials from oauth.properties in the project root. */
public class OAuthConfig {
    private static final Properties props = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream(
                System.getProperty("user.dir") + "/oauth.properties")) {
            props.load(fis);
        } catch (IOException e) {
            // Config not found — OAuth will be unavailable
        }
    }

    public static String googleClientId() {
        return props.getProperty("google.client.id", "").trim();
    }

    public static String googleClientSecret() {
        return props.getProperty("google.client.secret", "").trim();
    }

    public static String githubClientId() {
        return props.getProperty("github.client.id", "").trim();
    }

    public static String githubClientSecret() {
        return props.getProperty("github.client.secret", "").trim();
    }

    public static boolean isGoogleConfigured() {
        return !googleClientId().isEmpty() && !googleClientSecret().isEmpty();
    }

    public static boolean isGitHubConfigured() {
        return !githubClientId().isEmpty() && !githubClientSecret().isEmpty();
    }
}
