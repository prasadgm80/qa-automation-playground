package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
    private static final String CONFIG_RESOURCE = "config/config.properties";
    private static final String EXAMPLE_RESOURCE = "config/config.properties.example";

    private final Properties properties;

    private ConfigReader(Properties properties) {
        this.properties = properties;
    }

    public static ConfigReader load() {
        Properties properties = new Properties();
        String resource = CONFIG_RESOURCE;
        InputStream availableInput = ConfigReader.class.getClassLoader().getResourceAsStream(resource);
        if (availableInput == null) {
            resource = EXAMPLE_RESOURCE;
            availableInput = ConfigReader.class.getClassLoader().getResourceAsStream(resource);
        }
        if (availableInput == null) {
            throw new IllegalStateException(
                    "Configuration resources not found: " + CONFIG_RESOURCE + " or " + EXAMPLE_RESOURCE);
        }

        InputStream input = availableInput;
        try (input) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read configuration resource: " + resource, e);
        }
        return new ConfigReader(properties);
    }

    public String get(String key) {
        String environmentVariable = environmentVariableFor(key);
        String value = System.getenv(environmentVariable);
        if (value == null || value.isBlank()) {
            value = properties.getProperty(key);
        }
        return value == null ? null : value.trim();
    }

    public String getRequired(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing configuration '" + key + "'; set " + environmentVariableFor(key)
                            + " or configure it in config/config.properties");
        }
        return value;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException(
                    "Configuration '" + key + "' must be 'true' or 'false', but was '" + value + "'");
        }
        return Boolean.parseBoolean(value);
    }

    private static String environmentVariableFor(String key) {
        return switch (key) {
            case "username" -> "TEST_USERNAME";
            case "password" -> "TEST_PASSWORD";
            case "url" -> "BASE_URL";
            case "browser" -> "BROWSER";
            case "headless" -> "HEADLESS";
            default -> key.toUpperCase().replace('.', '_').replace('-', '_');
        };
    }
}
