package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Properties;

public class BasePage {

    public WebDriver driver;
        public Properties prop;
    public static ThreadLocal<WebDriver> tldriver = new ThreadLocal<WebDriver>();

    public WebDriver initializeDriver(String url, String browserName) {

        if (browserName.equals("chrome")) {
            WebDriverManager.chromedriver().setup();
            //driver = new ChromeDriver();
            //ChromeOptions co = new ChromeOptions();
            //co.addArguments("--headless");
            tldriver.set(new ChromeDriver());
        } else if (browserName.equals("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            //driver = new FirefoxDriver();
            tldriver.set(new FirefoxDriver());
        } else {
            System.out.println("Browser" + browserName
                    + "is not defined in properties file, please give the correct browser name");
        }
        getDriver().manage().deleteAllCookies();
        getDriver().manage().window().fullscreen();
        getDriver().get(url);
        return getDriver();
    }

    public static synchronized WebDriver getDriver() {
        return tldriver.get();
    }

    public Properties initializeProperties() {
        prop = new Properties();
        Path configPath = Path.of("src", "main", "java", "config", "config.properties");

        try (FileInputStream input = new FileInputStream(configPath.toFile())) {
            prop.load(input);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load properties from " + configPath, e);
        }

        return prop;
    }
}
