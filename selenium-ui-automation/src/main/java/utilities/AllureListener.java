package utilities;

import base.DriverFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class AllureListener extends AllureTestNg {
    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);

        WebDriver driver = DriverFactory.getDriverOrNull();
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return;
        }

        try {
            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(
                    "Failure screenshot - " + result.getName(),
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png");
        } catch (WebDriverException e) {
            byte[] message = ("Unable to capture screenshot: " + e.getMessage())
                    .getBytes(StandardCharsets.UTF_8);
            Allure.addAttachment(
                    "Screenshot capture error",
                    "text/plain",
                    new ByteArrayInputStream(message),
                    ".txt");
        }
    }
}
