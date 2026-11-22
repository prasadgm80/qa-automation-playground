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
 * Shared test lifecycle: starts the browser, logs in, exposes page objects, and quits.
 *
 * <p>Tests run in parallel, so per-test state is kept in {@link ThreadLocal} fields
 * and accessed via {@code loginPage()} / {@code homePage()}.
 */
public abstract class BaseTest {
    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    private final ThreadLocal<LoginPage> loginPage = new ThreadLocal<>();
    private final ThreadLocal<HomePage> homePage = new ThreadLocal<>();

    /** Starts the browser, builds page objects, and logs in before each test. */
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

    /** The login page for the current test thread. */
    protected LoginPage loginPage() {
        return loginPage.get();
    }

    /** The home page for the current test thread. */
    protected HomePage homePage() {
        return homePage.get();
    }

    /** Logs the outcome and quits the browser after each test. */
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
