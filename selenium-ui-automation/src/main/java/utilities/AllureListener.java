package utilities;

import base.DriverFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Adds rich, automatic diagnostics to the Allure report.
 *
 * <p>On every failure it attaches, when available:
 * <ul>
 *     <li>a full-page screenshot,</li>
 *     <li>the page HTML source, and</li>
 *     <li>the browser console log.</li>
 * </ul>
 * Each attachment is best-effort: if one cannot be captured it is skipped and the
 * others are still attached.
 */
public class AllureListener extends AllureTestNg {
    private static final Logger log = LoggerFactory.getLogger(AllureListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);
        log.info("[Allure] starting: {}", result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);

        WebDriver driver = DriverFactory.getDriverOrNull();
        if (driver == null) {
            return;
        }

        attachScreenshot(driver, result.getName());
        attachPageSource(driver);
        attachBrowserLogs(driver);
    }

    private void attachScreenshot(WebDriver driver, String testName) {
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return;
        }
        try {
            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(
                    "Failure screenshot - " + testName,
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png");
        } catch (WebDriverException e) {
            log.warn("Could not capture screenshot: {}", e.getMessage());
            Allure.addAttachment("Screenshot capture error", "text/plain",
                    new ByteArrayInputStream(
                            ("Unable to capture screenshot: " + e.getMessage())
                                    .getBytes(StandardCharsets.UTF_8)),
                    ".txt");
        }
    }

    private void attachPageSource(WebDriver driver) {
        try {
            String source = driver.getPageSource();
            if (source != null) {
                Allure.addAttachment("Page source", "text/html",
                        new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)), ".html");
            }
        } catch (WebDriverException e) {
            log.warn("Could not capture page source: {}", e.getMessage());
        }
    }

    private void attachBrowserLogs(WebDriver driver) {
        try {
            LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
            String logs = entries.getAll().stream()
                    .map(LogEntry::toString)
                    .collect(Collectors.joining(System.lineSeparator()));
            if (!logs.isBlank()) {
                Allure.addAttachment("Browser console log", "text/plain",
                        new ByteArrayInputStream(logs.getBytes(StandardCharsets.UTF_8)), ".txt");
            }
        } catch (RuntimeException e) {
            // Not all browsers expose console logs; ignore quietly.
            log.debug("Browser logs unavailable: {}", e.getMessage());
        }
    }
}
