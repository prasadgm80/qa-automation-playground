package dashBoardTests;

import base.BasePage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import utilities.ExcelReader;
import utilities.AllureListener;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Listeners(AllureListener.class)
public class PurchaseTests {
    WebDriver driver;
    Properties prop;
    BasePage basePage;
    LoginPage loginPage;
    HomePage homePage;

    private static final Path TEST_DATA_FILE = resolveTestDataFile();

    private static Path resolveTestDataFile() {
        Path repositoryRelative = Path.of("selenium-ui-automation", "src", "main", "java",
                "testData", "testData_ClientDashBoard.xlsx");
        if (Files.isRegularFile(repositoryRelative)) {
            return repositoryRelative;
        }
        return Path.of("src", "main", "java", "testData", "testData_ClientDashBoard.xlsx");
    }

    @DataProvider(name = "excelData")
    public Object[][] excelData(Method testMethod) {
        return ExcelReader.readTestData(
                TEST_DATA_FILE,
                testMethod.getDeclaringClass().getSimpleName(),
                testMethod.getName());
    }

    @BeforeMethod
    public void setUp() {
        basePage = new BasePage();
        prop = basePage.initializeProperties();
        String userName = prop.getProperty("username");
        String password = prop.getProperty("password");
        String url = prop.getProperty("url");
        String browserName = prop.getProperty("browser");

        driver = basePage.initializeDriver(url, browserName);
        loginPage = new LoginPage(driver);
        homePage = new HomePage(driver);

        loginPage.loginToClientDashBoard(userName, password);
    }

    @Test(dataProvider = "excelData")
    public void test1(String productName) {
        homePage.addProductToCart(productName);
    }

    @AfterMethod
    public void teardown() {
        driver.quit();
    }
}
