package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.Locale;
import java.util.Objects;

public final class DriverFactory {
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
        WebDriver driver;
        switch (browserName.trim().toLowerCase(Locale.ROOT)) {
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                if (headless) {
                    options.addArguments("--headless=new", "--window-size=1920,1080");
                }
                driver = new ChromeDriver(options);
            }
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
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
            driver.get(url);
            return driver;
        } catch (RuntimeException e) {
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
                driver.quit();
            }
        } finally {
            DRIVER.remove();
        }
    }
}
