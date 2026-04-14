package auth;

/** Centralized configuration for VaultFS remote authentication. */
public class AuthConfig {
    public static final String AUTH_SERVER_URL = "https://vaultfs-auth-server.onrender.com";
    public static final int LOCAL_PORT = 9000;
    public static final int POLL_INTERVAL_MS = 2000;
    public static final int LOGIN_TIMEOUT_SECONDS = 120;
}
