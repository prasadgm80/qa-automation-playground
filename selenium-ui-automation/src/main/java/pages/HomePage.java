package pages;

import org.openqa.selenium.By;
import utilities.Utils;

/** Home page showing the product catalogue. */
public class HomePage extends BasePage {

    public HomePage(Utils utils) {
        super(utils);
    }

    /** Builds the "Add To Cart" button locator for the named product. */
    public By addToCart(String productName) {
        return By.xpath("//b[text()='" + productName
                + "']/parent::h5/following-sibling::button[text()=' Add To Cart']");
    }

    /** Clicks "Add To Cart" for the named product. */
    public void addProductToCart(String productName) {
        utils.click(addToCart(productName));
    }
}
