package dashBoardTests;

import base.DriverFactory;
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
import utilities.ConfigReader;

@Listeners(AllureListener.class)
public class PurchaseTests {
    LoginPage loginPage;
    HomePage homePage;
    private static final String TEST_DATA_RESOURCE = "testData/testData_ClientDashBoard.xlsx";

    @DataProvider(name = "excelData")
    public Object[][] excelData(Method testMethod) {
        return ExcelReader.readTestData(
                TEST_DATA_RESOURCE,
                testMethod.getDeclaringClass().getSimpleName(),
                testMethod.getName());
    }

    @BeforeMethod
    public void setUp() {
        ConfigReader config = ConfigReader.load();
        String userName = config.getRequired("username");
        String password = config.getRequired("password");

        DriverFactory.startDriver(
                config.getRequired("browser"),
                config.getRequired("url"),
                config.getBoolean("headless", false));
        loginPage = new LoginPage(DriverFactory.getDriver());
        homePage = new HomePage(DriverFactory.getDriver());

        loginPage.loginToClientDashBoard(userName, password);
    }

    @Test(dataProvider = "excelData")
    public void test1(String productName) {
        homePage.addProductToCart(productName);
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        DriverFactory.quitDriver();
    }
}
