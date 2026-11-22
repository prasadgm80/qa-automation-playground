package base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.HomePage;
import pages.LoginPage;
import utilities.ConfigReader;
import utilities.Utils;

import java.lang.reflect.Method;

/**
 * Shared test lifecycle: starts the browser, logs in, exposes ready-to-use page
 * objects, and shuts the browser down afterwards.
 *
 * <p>Tests run in parallel (see {@code testng.xml}), and with {@code parallel="methods"}
 * TestNG shares one test-class instance across threads. To keep each test isolated,
 * the per-test state (driver helper and page objects) is stored in {@link ThreadLocal}
 * fields and exposed through the {@code loginPage()} / {@code homePage()} accessors.
 */
public abstract class BaseTest {
    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    private final ThreadLocal<LoginPage> loginPage = new ThreadLocal<>();
    private final ThreadLocal<HomePage> homePage = new ThreadLocal<>();

    @BeforeMethod
    public void setUp(Method method) {
        log.info("Starting test: {}", method.getName());
        ConfigReader config = ConfigReader.load();

        DriverFactory.startDriver(
                config.getRequired("browser"),
                config.getRequired("url"),
                config.getBoolean("headless", false));

        // One Utils (driver + explicit wait) per thread, shared by every page object.
        Utils utils = new Utils(DriverFactory.getDriver());
        loginPage.set(new LoginPage(utils));
        homePage.set(new HomePage(utils));

        loginPage.get().loginToClientDashBoard(
                config.getRequired("username"),
                config.getRequired("password"));
    }

    protected LoginPage loginPage() {
        return loginPage.get();
    }

    protected HomePage homePage() {
        return homePage.get();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("Test failed: {}", result.getName(), result.getThrowable());
        } else {
            log.info("Finished test: {}", result.getName());
        }
        DriverFactory.quitDriver();
        loginPage.remove();
        homePage.remove();
    }
}
