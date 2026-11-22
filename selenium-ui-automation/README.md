# selenium-ui-automation

A simple Maven project for Selenium UI automation using Java, TestNG, Apache POI, and Chrome.

## Requirements

- Java 17 or later
- Maven 3.8 or later
- Google Chrome

Selenium Manager, included with Selenium, resolves the matching ChromeDriver automatically.

## Local credentials

Runtime configuration is loaded from the classpath at `src/main/resources/config/config.properties`. That local file is intentionally ignored by Git; use `config/config.properties.example` as a template. Environment variables override properties: `BROWSER`, `BASE_URL`, `HEADLESS`, `TEST_USERNAME`, and `TEST_PASSWORD`.

Additional tuning knobs (optional, with sensible defaults):

- `explicit.wait.seconds` (default `10`) – how long each action waits for an element before failing.
- `action.retry.count` (default `3`) – how many times a single Selenium action (click, sendKeys, …) is retried before failing. Set to `1` to disable retries.

The configuration file is read once and cached, so all tests share the same values.

## Excel-driven test data

`PurchaseTests` reads data from the classpath resource `testData/testData_ClientDashBoard.xlsx` using a TestNG data provider. The worksheet name must match the test class name (`PurchaseTests`). The first row is a header row; each later row starts with the test method name in column A, followed by that test's arguments in columns B onward. The provider matches the current test method automatically and supports multiple data rows for a test. Blank cells are skipped, so a column used by one test but left empty for another does not shift the remaining values — each test receives exactly the non-empty arguments on its row.

## Run the tests

From the repository root, run:

```bash
mvn -pl selenium-ui-automation test
```

Tests run **in parallel** according to `src/test/resources/testng.xml`
(`parallel="methods"`). Each test gets its own browser session via a `ThreadLocal`
WebDriver, so tests stay isolated. To scale to hundreds of tests, raise
`thread-count` (and `data-provider-thread-count`) in `testng.xml` to match the
machine's capacity.

Drivers are resolved automatically by **Selenium Manager** (built into Selenium),
so no separate driver download or manager is required.

## Reports

Allure TestNG results are written to `selenium-ui-automation/target/allure-results`.
An HTML report is **generated automatically** at the end of every `mvn test` run to:

```
selenium-ui-automation/target/site/allure-maven-plugin/index.html
```

On failures the report automatically includes a screenshot, the page HTML source,
and the browser console log when available. A plain-text run log is also written to
`selenium-ui-automation/target/logs/automation.log`.

To open an interactive report in a browser instead, run:

```bash
mvn -pl selenium-ui-automation allure:serve
```

## Continuous integration

The GitHub Actions workflow builds the Maven reactor on pushes and pull requests. It runs the browser tests across a **Chrome and Firefox matrix** when the repository has `TEST_USERNAME` and `TEST_PASSWORD` secrets configured, using headless browsers, and uploads each browser's Allure results as a separate workflow artifact (`allure-results-chrome`, `allure-results-firefox`).
