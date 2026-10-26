package pages;

import base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utilities.Utils;

public class LoginPage extends BasePage {
    private final Utils utils;

    @FindBy(id = "userEmail")
    public WebElement userId;

    @FindBy(id = "userPassword")
    public WebElement password;

    @FindBy(id = "login")
    public WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
        PageFactory.initElements(driver, this);
    }

    public void loginToClientDashBoard(String id, String passwordText) {
        utils.sendKeys(userId, id);
        utils.sendKeys(password, passwordText);
        utils.click(loginButton);
    }
}
