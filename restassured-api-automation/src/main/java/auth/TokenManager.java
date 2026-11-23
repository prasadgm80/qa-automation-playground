package auth;

import clients.AuthService;
import config.ConfigReader;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Logs in once with the configured credentials and caches the JWT for all scenarios. */
public final class TokenManager {

    private static final Logger LOG = LoggerFactory.getLogger(TokenManager.class);
    private static final ConfigReader CONFIG = ConfigReader.load();

    private static volatile String cachedToken;

    private TokenManager() {
    }

    /** Returns a valid bearer token, logging in only the first time it is requested. */
    public static String getBearerToken() {
        String local = cachedToken;
        if (local == null) {
            synchronized (TokenManager.class) {
                local = cachedToken;
                if (local == null) {
                    local = login();
                    cachedToken = local;
                }
            }
        }
        return local;
    }

    private static String login() {
        String email = CONFIG.getRequired("api.username");
        LOG.info("Authenticating as {}", email);

        Response response = AuthService.login(email, CONFIG.getRequired("api.password"));
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Login failed (" + response.statusCode() + "): " + response.asString());
        }

        String token = response.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Login succeeded but no token was returned: " + response.asString());
        }
        return token;
    }
}
