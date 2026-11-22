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

/** Selenium wrapper: every action waits for the element, retries on failure, and logs. */
public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;
    private final int maxAttempts;

    /** Reads wait timeout and retry count from config (once) and builds the explicit wait. */
    public Utils(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver must not be null");
        ConfigReader config = ConfigReader.load();
        int waitSeconds = config.getInt("explicit.wait.seconds", 10);
        this.maxAttempts = Math.max(1, config.getInt("action.retry.count", 3));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
        this.actions = new Actions(driver);
    }

    /** Returns the wrapped driver (used by page objects for PageFactory). */
    public WebDriver getDriver() {
        return driver;
    }

    // ---------------------------------------------------------------------
    // Click / type
    // ---------------------------------------------------------------------

    /** Clicks the element located by {@code locator}. */
    public void click(By locator) {
        retry("click " + locator, () ->
                wait.until(ExpectedConditions.elementToBeClickable(locator)).click());
    }

    /** Clicks the given element. */
    public void click(WebElement element) {
        retry("click element", () ->
                wait.until(ExpectedConditions.elementToBeClickable(element)).click());
    }

    /** Types {@code text} into the element located by {@code locator}. */
    public void sendKeys(By locator, String text) {
        retry("sendKeys to " + locator, () ->
                wait.until(ExpectedConditions.elementToBeClickable(locator)).sendKeys(text));
    }

    /** Types {@code text} into the given element. */
    public void sendKeys(WebElement element, String text) {
        retry("sendKeys to element", () ->
                wait.until(ExpectedConditions.elementToBeClickable(element)).sendKeys(text));
    }

    /** Clears the field, then types {@code text}. */
    public void type(By locator, String text) {
        retry("type into " + locator, () -> {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.clear();
            element.sendKeys(text);
        });
    }

    /** Clears the field located by {@code locator}. */
    public void clear(By locator) {
        retry("clear " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).clear());
    }

    // ---------------------------------------------------------------------
    // Read state
    // ---------------------------------------------------------------------

    /** Returns the visible text of the element. */
    public String getText(By locator) {
        return retry("getText from " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText());
    }

    /** Returns the value of the given attribute on the element. */
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

    /** Returns {@code true} if the element is enabled. */
    public boolean isEnabled(By locator) {
        return retry("isEnabled " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isEnabled());
    }

    /** Returns {@code true} if the element is selected (checkbox/radio/option). */
    public boolean isSelected(By locator) {
        return retry("isSelected " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isSelected());
    }

    // ---------------------------------------------------------------------
    // Waits
    // ---------------------------------------------------------------------

    /** Waits until the element is visible and returns it. */
    public WebElement waitForVisible(By locator) {
        return retry("waitForVisible " + locator, () ->
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator)));
    }

    /** Waits until the element is clickable and returns it. */
    public WebElement waitForClickable(By locator) {
        return retry("waitForClickable " + locator, () ->
                wait.until(ExpectedConditions.elementToBeClickable(locator)));
    }

    /** Waits until the element is no longer visible. */
    public boolean waitForInvisible(By locator) {
        return retry("waitForInvisible " + locator, () ->
                wait.until(ExpectedConditions.invisibilityOfElementLocated(locator)));
    }

    /** Waits until {@code text} appears in the element. */
    public boolean waitForText(By locator, String text) {
        return retry("waitForText '" + text + "' in " + locator, () ->
                wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text)));
    }

    // ---------------------------------------------------------------------
    // Dropdowns
    // ---------------------------------------------------------------------

    /** Selects a dropdown option by its visible text. */
    public void selectByVisibleText(By locator, String text) {
        retry("selectByVisibleText '" + text + "' in " + locator, () ->
                new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(locator)))
                        .selectByVisibleText(text));
    }

    /** Selects a dropdown option by its value attribute. */
    public void selectByValue(By locator, String value) {
        retry("selectByValue '" + value + "' in " + locator, () ->
                new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(locator)))
                        .selectByValue(value));
    }

    /** Selects a dropdown option by its index. */
    public void selectByIndex(By locator, int index) {
        retry("selectByIndex " + index + " in " + locator, () ->
                new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(locator)))
                        .selectByIndex(index));
    }

    // ---------------------------------------------------------------------
    // Mouse / keyboard / JavaScript
    // ---------------------------------------------------------------------

    /** Moves the mouse over the element. */
    public void hover(By locator) {
        retry("hover over " + locator, () ->
                actions.moveToElement(wait.until(ExpectedConditions.visibilityOfElementLocated(locator)))
                        .perform());
    }

    /** Double-clicks the element. */
    public void doubleClick(By locator) {
        retry("doubleClick " + locator, () ->
                actions.doubleClick(wait.until(ExpectedConditions.elementToBeClickable(locator))).perform());
    }

    /** Right-clicks (context-clicks) the element. */
    public void rightClick(By locator) {
        retry("rightClick " + locator, () ->
                actions.contextClick(wait.until(ExpectedConditions.elementToBeClickable(locator))).perform());
    }

    /** Clicks the element via JavaScript (useful when a normal click is intercepted). */
    public void jsClick(By locator) {
        retry("jsClick " + locator, () -> {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        });
    }

    /** Scrolls the element into the center of the viewport. */
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

    /** Waits for the element to be present and returns it. */
    public WebElement find(By locator) {
        return retry("find " + locator, () ->
                wait.until(ExpectedConditions.presenceOfElementLocated(locator)));
    }

    /** Waits for all matching elements to be present and returns them. */
    public List<WebElement> findAll(By locator) {
        return retry("findAll " + locator, () ->
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator)));
    }

    // ---------------------------------------------------------------------
    // Alerts, frames, navigation
    // ---------------------------------------------------------------------

    /** Accepts (clicks OK on) the current alert. */
    public void acceptAlert() {
        retry("acceptAlert", () -> wait.until(ExpectedConditions.alertIsPresent()).accept());
    }

    /** Dismisses (clicks Cancel on) the current alert. */
    public void dismissAlert() {
        retry("dismissAlert", () -> wait.until(ExpectedConditions.alertIsPresent()).dismiss());
    }

    /** Returns the text of the current alert. */
    public String getAlertText() {
        return retry("getAlertText", () -> {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            return alert.getText();
        });
    }

    /** Switches into the frame located by {@code locator}. */
    public void switchToFrame(By locator) {
        retry("switchToFrame " + locator, () ->
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator)));
    }

    /** Switches back to the main document from any frame. */
    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    /** Returns the current page title. */
    public String getTitle() {
        return driver.getTitle();
    }

    /** Returns the current page URL. */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ---------------------------------------------------------------------
    // Retry engine
    // ---------------------------------------------------------------------

    /** Runs the action, retrying up to {@code maxAttempts}; logs each failure and rethrows the last. */
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

    /** Retry overload for actions that return no value. */
    private void retry(String description, Runnable action) {
        retry(description, () -> {
            action.run();
            return null;
        });
    }
}
