package com.epam.elms.e2e.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates {@link WebDriver} instances for different browsers.
 *
 * <p>Browser binaries are managed automatically via WebDriverManager.
 * Pass {@code -Dheadless=true} (or the TestNG suite param) to run in headless mode.
 */
public final class WebDriverFactory {

    private static final Logger log = LoggerFactory.getLogger(WebDriverFactory.class);

    private WebDriverFactory() {}

    /**
     * Create a WebDriver for the requested browser.
     *
     * @param browser  "chrome" or "firefox" (case-insensitive)
     * @param headless true to run without a visible browser window
     * @return configured WebDriver instance
     */
    public static WebDriver create(String browser, boolean headless) {
        log.info("Creating WebDriver: browser={}, headless={}", browser, headless);

        return switch (browser.toLowerCase()) {
            case "firefox" -> createFirefox(headless);
            default        -> createChrome(headless);   // defaults to Chrome
        };
    }

    // ── Chrome ────────────────────────────────────────────────────────────────

    private static WebDriver createChrome(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }

        options.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--window-size=1920,1080",
            "--remote-allow-origins=*"
        );

        return new ChromeDriver(options);
    }

    // ── Firefox ───────────────────────────────────────────────────────────────

    private static WebDriver createFirefox(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("-headless");
        }

        return new FirefoxDriver(options);
    }
}
