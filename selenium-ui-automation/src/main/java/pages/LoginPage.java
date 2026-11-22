package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utilities.Utils;

/** Login page for the client dashboard. */
public class LoginPage extends BasePage {

    @FindBy(id = "userEmail")
    private WebElement userId;

    @FindBy(id = "userPassword")
    private WebElement password;

    @FindBy(id = "login")
    private WebElement loginButton;

    public LoginPage(Utils utils) {
        super(utils);
        PageFactory.initElements(utils.getDriver(), this);
    }

    /** Logs in with the given email and password. */
    public void loginToClientDashBoard(String id, String passwordText) {
        utils.sendKeys(userId, id);
        utils.sendKeys(password, passwordText);
        utils.click(loginButton);
    }
}
