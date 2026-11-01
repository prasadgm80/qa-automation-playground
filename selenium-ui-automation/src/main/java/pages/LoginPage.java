package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utilities.Utils;

import java.util.Objects;

public class LoginPage {
    private final WebDriver driver;
    private final Utils utils;

    @FindBy(id = "userEmail")
    private WebElement userId;

    @FindBy(id = "userPassword")
    private WebElement password;

    @FindBy(id = "login")
    private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver must not be null");
        this.utils = new Utils(driver);
        PageFactory.initElements(driver, this);
    }

    public void loginToClientDashBoard(String id, String passwordText) {
        utils.sendKeys(userId, id);
        utils.sendKeys(password, passwordText);
        utils.click(loginButton);
    }
}
