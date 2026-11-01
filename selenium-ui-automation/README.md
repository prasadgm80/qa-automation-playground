# selenium-ui-automation

A simple Maven project for Selenium UI automation using Java, TestNG, Apache POI, and Chrome.

## Requirements

- Java 17 or later
- Maven 3.8 or later
- Google Chrome

Selenium Manager, included with Selenium, resolves the matching ChromeDriver automatically.

## Local credentials

Runtime configuration is loaded from the classpath at `src/main/resources/config/config.properties`. That local file is intentionally ignored by Git; use `config/config.properties.example` as a template. Environment variables override properties: `BROWSER`, `BASE_URL`, `HEADLESS`, `TEST_USERNAME`, and `TEST_PASSWORD`.

## Excel-driven test data

`PurchaseTests` reads data from the classpath resource `testData/testData_ClientDashBoard.xlsx` using a TestNG data provider. The worksheet name must match the test class name (`PurchaseTests`). The first row is a header row; each later row starts with the test method name in column A, followed by that test's arguments in columns B onward. The provider matches the current test method automatically and supports multiple data rows for a test. Blank cells between values are passed as empty strings, preserving the argument positions.

## Run the tests

From the repository root, run:

```bash
mvn -pl selenium-ui-automation test
```

Allure TestNG results are written to `selenium-ui-automation/target/allure-results`.
Generate and open the report locally with:

```bash
mvn -pl selenium-ui-automation allure:serve
```

To generate report files without starting a server, run:

```bash
mvn -pl selenium-ui-automation allure:report
```

## Continuous integration

The GitHub Actions workflow builds the Maven reactor on pushes and pull requests. It runs the browser tests when the repository has `TEST_USERNAME` and `TEST_PASSWORD` secrets configured, using headless Chrome, and uploads Allure results as a workflow artifact.
