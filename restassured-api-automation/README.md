# REST Assured API Automation (EventHub)

BDD API automation for the [EventHub API](https://api.eventhub.rahulshettyacademy.com/api/docs/)
using **REST Assured + Cucumber** on the **JUnit 5 Platform**.

## Layout
```
src/main/java/
  config/        ConfigReader           # loads config, env vars override file values
  clients/       RequestFactory         # one reusable, pre-configured request spec
                 AuthService, EventService
  auth/          TokenManager           # logs in once, caches the JWT
  models/        request/response POJOs
  utils/         DateUtils              # computes relative future dates (e.g. eventDate)
                 RestAssuredLogFilter   # logs every request/response via SLF4J
  context/       ScenarioContext        # per-scenario state shared between steps
src/test/
  java/runners/        RunCucumberTest  # JUnit 5 Platform suite
  java/stepdefinitions AuthSteps, EventSteps, Hooks
  resources/features/  *.feature
  resources/junit-platform.properties   # glue, report plugins, parallel settings
```

## Configuration
Copy the template and fill in credentials of an account already registered on EventHub:
```
cp src/main/resources/config/config.properties.example src/main/resources/config/config.properties
```
| Property              | Env override   | Description                          |
|----------------------|----------------|--------------------------------------|
| `base.uri`           | `BASE_URI`     | API base URL                         |
| `api.username`       | `API_USERNAME` | Login email                          |
| `api.password`       | `API_PASSWORD` | Login password                       |
| `http.timeout.seconds` | `HTTP_TIMEOUT_SECONDS` | Response timeout            |

`config.properties` is git-ignored; environment variables take priority (used in CI).

## Run
```
# from the repository root
mvn -pl restassured-api-automation -am verify
```
- Tests run in parallel (scenario level) — scales to hundreds of tests.
- Cucumber HTML dashboard: `target/cucumber-html-reports/overview-features.html`
  (generated automatically — no extra command needed).
- Logs: `target/logs/api-automation.log`.

## Test data
- Credentials (login) come from `config.properties` / env vars — see Configuration above.
- Scenario data (event title, price, venue, etc.) lives in the `Examples:` table of
  `events.feature`. Add a new row there for a new test case — no code changes needed.
- `eventDate` is never hard-coded (it would eventually become a past date). Instead the
  Examples table supplies a `daysFromNow` offset, and `utils/DateUtils.futureDate(int)`
  turns it into a valid ISO date-time at run time.

## Add a new test
1. Add a scenario (or an `Examples:` row) to a `.feature` file under `src/test/resources/features`.
2. Implement any new steps in `stepdefinitions` (inject `ScenarioContext` via the constructor).
3. Add a service method in `clients` if a new endpoint is involved.
