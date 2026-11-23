package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Loads framework configuration once; environment variables override file values. */
public final class ConfigReader {
    private static final String CONFIG_RESOURCE = "config/config.properties";
    private static final String EXAMPLE_RESOURCE = "config/config.properties.example";

    private static volatile ConfigReader instance;

    private final Properties properties;

    private ConfigReader(Properties properties) {
        this.properties = properties;
    }

    /** Returns the shared configuration, loading it from the classpath only once. */
    public static ConfigReader load() {
        ConfigReader local = instance;
        if (local == null) {
            synchronized (ConfigReader.class) {
                local = instance;
                if (local == null) {
                    local = readFromClasspath();
                    instance = local;
                }
            }
        }
        return local;
    }

    private static ConfigReader readFromClasspath() {
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

    /** Returns the value for {@code key} (environment variable takes priority), or {@code null}. */
    public String get(String key) {
        String value = System.getenv(environmentVariableFor(key));
        if (value == null || value.isBlank()) {
            value = properties.getProperty(key);
        }
        return value == null ? null : value.trim();
    }

    /** Returns the value for {@code key}, or throws if it is missing. */
    public String getRequired(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing configuration '" + key + "'; set " + environmentVariableFor(key)
                            + " or configure it in config/config.properties");
        }
        return value;
    }

    /** Returns the integer value for {@code key}, or {@code defaultValue} if unset. */
    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Configuration '" + key + "' must be a whole number, but was '" + value + "'", e);
        }
    }

    /** Maps a property key to its environment-variable override name. */
    private static String environmentVariableFor(String key) {
        return switch (key) {
            case "base.uri" -> "BASE_URI";
            case "api.username" -> "API_USERNAME";
            case "api.password" -> "API_PASSWORD";
            default -> key.toUpperCase().replace('.', '_').replace('-', '_');
        };
    }
}
