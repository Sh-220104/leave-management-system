package com.epam.elms.e2e.tests;

import com.epam.elms.e2e.driver.WebDriverFactory;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.time.Duration;

/**
 * Base class for all Selenium E2E tests.
 *
 * <p>Reads {@code browser}, {@code headless} and {@code baseUrl} from:
 * <ol>
 *   <li>TestNG suite XML parameters</li>
 *   <li>System properties ({@code -Dbrowser=chrome}, etc.)</li>
 *   <li>Hard-coded defaults (chrome / headless / http://localhost:3000)</li>
 * </ol>
 *
 * <p>A fresh {@link WebDriver} is created before each test method and
 * quit after each test method so tests are fully isolated.
 */
public abstract class BaseE2ETest {

    protected static final Logger log = LoggerFactory.getLogger(BaseE2ETest.class);

    /** Default implicit-wait timeout in seconds. */
    private static final int IMPLICIT_WAIT_SEC = 5;

    protected WebDriver driver;
    protected String    baseUrl;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Set up a new WebDriver before each test method.
     *
     * <p>Parameters injected by the TestNG suite XML, falling back to system properties.
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "headless", "baseUrl"})
    public void setUpDriver(
            @Optional("chrome")                 String browser,
            @Optional("true")                   String headlessStr,
            @Optional("http://localhost:3000")  String baseUrl,
            ITestContext context) {

        // Allow system-property overrides
        String resolvedBrowser  = System.getProperty("browser",  browser);
        boolean headless        = Boolean.parseBoolean(System.getProperty("headless", headlessStr));
        this.baseUrl            = System.getProperty("app.ui.base.url", baseUrl);

        log.info("[E2E] Starting test: browser={}, headless={}, baseUrl={}",
                 resolvedBrowser, headless, this.baseUrl);

        driver = WebDriverFactory.create(resolvedBrowser, headless);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SEC));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    /** Tear down (quit) the WebDriver after each test method. */
    @AfterMethod(alwaysRun = true)
    public void tearDownDriver() {
        if (driver != null) {
            log.info("[E2E] Quitting WebDriver after test");
            driver.quit();
            driver = null;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Navigate to a full URL. */
    protected void navigateTo(String url) {
        driver.get(url);
    }

    /** Navigate to a path relative to baseUrl. */
    protected void navigateToPath(String path) {
        driver.get(baseUrl + path);
    }

    /** Return the current URL. */
    protected String currentUrl() {
        return driver.getCurrentUrl();
    }
}
