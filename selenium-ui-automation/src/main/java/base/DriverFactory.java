package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;

public final class DriverFactory {
    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static WebDriver startDriver(String browserName, String url, boolean headless) {
        Objects.requireNonNull(browserName, "browserName must not be null");
        Objects.requireNonNull(url, "url must not be null");
        if (browserName.isBlank()) {
            throw new IllegalArgumentException("browserName must not be blank");
        }
        if (url.isBlank()) {
            throw new IllegalArgumentException("url must not be blank");
        }

        quitDriver();
        // Selenium Manager (built into Selenium 4.6+) automatically downloads and
        // configures the matching browser driver, so no external driver manager is needed.
        WebDriver driver;
        String normalized = browserName.trim().toLowerCase(Locale.ROOT);
        log.info("Starting '{}' browser (headless={})", normalized, headless);
        switch (normalized) {
            case "chrome" -> {
                ChromeOptions options = new ChromeOptions();
                if (headless) {
                    options.addArguments("--headless=new", "--window-size=1920,1080");
                }
                driver = new ChromeDriver(options);
            }
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) {
                    options.addArguments("-headless");
                }
                driver = new FirefoxDriver(options);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported browser '" + browserName + "'. Supported browsers: chrome, firefox");
        }

        DRIVER.set(driver);
        try {
            driver.manage().deleteAllCookies();
            if (!headless) {
                driver.manage().window().maximize();
            }
            log.info("Navigating to {}", url);
            driver.get(url);
            return driver;
        } catch (RuntimeException e) {
            log.error("Failed to initialise the browser session", e);
            quitDriver();
            throw e;
        }
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No WebDriver is active on this thread");
        }
        return driver;
    }

    public static WebDriver getDriverOrNull() {
        return DRIVER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        try {
            if (driver != null) {
                log.info("Quitting browser session");
                driver.quit();
            }
        } finally {
            DRIVER.remove();
        }
    }
}
