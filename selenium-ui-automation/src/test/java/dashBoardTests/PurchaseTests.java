package dashBoardTests;

import base.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utilities.ExcelReader;

import java.lang.reflect.Method;

public class PurchaseTests extends BaseTest {
    private static final String TEST_DATA_RESOURCE = "testData/testData_ClientDashBoard.xlsx";

    @DataProvider(name = "excelData")
    public Object[][] excelData(Method testMethod) {
        return ExcelReader.readTestData(
                TEST_DATA_RESOURCE,
                testMethod.getDeclaringClass().getSimpleName(),
                testMethod.getName());
    }

    @Test(dataProvider = "excelData")
    public void test1(String productName) {
        homePage().addProductToCart(productName);
    }
}
