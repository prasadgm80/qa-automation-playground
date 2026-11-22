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
        return By.xpath("//b[text()=" + xpathLiteral(productName)
                + "]/parent::h5/following-sibling::button[text()=' Add To Cart']");
    }

    /** Clicks "Add To Cart" for the named product. */
    public void addProductToCart(String productName) {
        utils.click(addToCart(productName));
    }

    /** Returns an XPath string literal, handling quotes safely via {@code concat()}. */
    private static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts = value.split("'", -1);
        StringBuilder literal = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                literal.append(", \"'\", ");
            }
            literal.append("'").append(parts[i]).append("'");
        }
        return literal.append(")").toString();
    }
}
