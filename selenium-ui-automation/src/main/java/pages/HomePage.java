package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utilities.Utils;

public class HomePage extends BasePage {
    private final Utils utils;

    @FindBy(id = "userEmail")
    public WebElement userId;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
        PageFactory.initElements(driver, this);
    }

    public By addToCart(String productName)
    {
        return By.xpath("//b[text()='"+productName+"']/parent::h5/following-sibling::button[text()=' Add To Cart']");
    }

    public void addProductToCart(String productName) {
        utils.click(addToCart(productName));
    }
}
