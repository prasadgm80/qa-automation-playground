package utilities;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Thin, reusable wrapper around Selenium.
 *
 * <p>Every action here:
 * <ul>
 *     <li>waits for the element to be ready (explicit wait),</li>
 *     <li>retries automatically if it fails (handles flaky UIs), and</li>
 *     <li>logs what happened, including each failed attempt.</li>
 * </ul>
 *
 * <p>The wait timeout and the number of retries are read once from
 * {@code config.properties} ({@code explicit.wait.seconds} and
 * {@code action.retry.count}), so behaviour is tunable without touching code.
 */
public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;
    private final int maxAttempts;

    public Utils(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver must not be null");
        ConfigReader config = ConfigReader.load();
        int waitSeconds = config.getInt("explicit.wait.seconds", 10);
        this.maxAttempts = Math.max(1, config.getInt("action.retry.count", 3));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
        this.actions = new Actions(driver);
    }

    /** The driver this helper wraps (used by page objects for PageFactory). */
    public WebDriver getDriver() {
        return driver;
    }

    // ---------------------------------------------------------------------
    // Click / type
    // ---------------------------------------------------------------------

    public void click(By locator) {
        retry("click " + locator, () ->
                wait.until(ExpectedConditions.elementToBeClickable(locator)).click());
    }

    public void click(WebElement element) {
        retry("click element", () ->
                wait.until(ExpectedConditions.elementToBeClickable(element)).click());
    }

    public void sendKeys(By locator, String text) {
        retry("sendKeys to " + locator, () ->
                wait.until(ExpectedConditions.elementToBeClickable(locator)).sendKeys(text));
    }

    public void sendKeys(WebElement element, String text) {
        retry("sendKeys to element", () ->
                wait.until(ExpectedConditions.elementToBeClickable(element)).sendKeys(text));
    }

    /** Clears the field first, then types the given text. */
    public void type(By locator, String text) {
        retry("type into " + locator, () -> {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.clear();
            element.sendKeys(text);
        });
    }

    public void clear(By locator) {
        retry("clear " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).clear());
    }

    // ---------------------------------------------------------------------
    // Read state
    // ---------------------------------------------------------------------

    public String getText(By locator) {
        return retry("getText from " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText());
    }

    public String getAttribute(By locator, String attribute) {
        return retry("getAttribute '" + attribute + "' from " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getAttribute(attribute));
    }

    /** Returns {@code true} if the element becomes visible within the wait timeout. */
    public boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (RuntimeException e) {
            log.debug("Element not displayed: {}", locator);
            return false;
        }
    }

    public boolean isEnabled(By locator) {
        return retry("isEnabled " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isEnabled());
    }

    public boolean isSelected(By locator) {
        return retry("isSelected " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isSelected());
    }

    // ---------------------------------------------------------------------
    // Waits
    // ---------------------------------------------------------------------

    public WebElement waitForVisible(By locator) {
        return retry("waitForVisible " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)));
    }

    public WebElement waitForClickable(By locator) {
        return retry("waitForClickable " + locator, () ->
                wait.until(ExpectedConditions.elementToBeClickable(locator)));
    }

    public boolean waitForInvisible(By locator) {
        return retry("waitForInvisible " + locator, () ->
                wait.until(ExpectedConditions.invisibilityOfElementLocated(locator)));
    }

    public boolean waitForText(By locator, String text) {
        return retry("waitForText '" + text + "' in " + locator, () ->
                wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text)));
    }

    // ---------------------------------------------------------------------
    // Dropdowns
    // ---------------------------------------------------------------------

    public void selectByVisibleText(By locator, String text) {
        retry("selectByVisibleText '" + text + "' in " + locator, () ->
                new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(locator)))
                        .selectByVisibleText(text));
    }

    public void selectByValue(By locator, String value) {
        retry("selectByValue '" + value + "' in " + locator, () ->
                new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(locator)))
                        .selectByValue(value));
    }

    public void selectByIndex(By locator, int index) {
        retry("selectByIndex " + index + " in " + locator, () ->
                new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(locator)))
                        .selectByIndex(index));
    }

    // ---------------------------------------------------------------------
    // Mouse / keyboard / JavaScript
    // ---------------------------------------------------------------------

    public void hover(By locator) {
        retry("hover over " + locator, () ->
                actions.moveToElement(wait.until(ExpectedConditions.visibilityOfElementLocated(locator)))
                        .perform());
    }

    public void doubleClick(By locator) {
        retry("doubleClick " + locator, () ->
                actions.doubleClick(wait.until(ExpectedConditions.elementToBeClickable(locator))).perform());
    }

    public void rightClick(By locator) {
        retry("rightClick " + locator, () ->
                actions.contextClick(wait.until(ExpectedConditions.elementToBeClickable(locator))).perform());
    }

    public void jsClick(By locator) {
        retry("jsClick " + locator, () -> {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        });
    }

    public void scrollIntoView(By locator) {
        retry("scrollIntoView " + locator, () -> {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", element);
        });
    }

    // ---------------------------------------------------------------------
    // Element lookups
    // ---------------------------------------------------------------------

    public WebElement find(By locator) {
        return retry("find " + locator, () ->
                wait.until(ExpectedConditions.presenceOfElementLocated(locator)));
    }

    public List<WebElement> findAll(By locator) {
        return retry("findAll " + locator, () ->
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator)));
    }

    // ---------------------------------------------------------------------
    // Alerts, frames, navigation
    // ---------------------------------------------------------------------

    public void acceptAlert() {
        retry("acceptAlert", () -> wait.until(ExpectedConditions.alertIsPresent()).accept());
    }

    public void dismissAlert() {
        retry("dismissAlert", () -> wait.until(ExpectedConditions.alertIsPresent()).dismiss());
    }

    public String getAlertText() {
        return retry("getAlertText", () -> {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            return alert.getText();
        });
    }

    public void switchToFrame(By locator) {
        retry("switchToFrame " + locator, () ->
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator)));
    }

    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ---------------------------------------------------------------------
    // Retry engine
    // ---------------------------------------------------------------------

    /**
     * Runs an action and retries it up to {@code action.retry.count} times.
     * Every failed attempt is logged; if all attempts fail the last error is thrown.
     */
    private <T> T retry(String description, Supplier<T> action) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                lastError = e;
                log.warn("Attempt {}/{} failed for [{}]: {}",
                        attempt, maxAttempts, description, e.getMessage());
            }
        }
        log.error("All {} attempt(s) failed for [{}]", maxAttempts, description);
        throw lastError;
    }

    /** Convenience overload for actions that do not return a value. */
    private void retry(String description, Runnable action) {
        retry(description, () -> {
            action.run();
            return null;
        });
    }
}
