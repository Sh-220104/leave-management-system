package com.epam.elms.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Abstract base class for all Page Object classes.
 *
 * <p>Provides shared utility methods (wait helpers, navigation, JS execution)
 * so that concrete pages don't duplicate boilerplate code.
 */
public abstract class BasePage {

    protected static final Logger log = LoggerFactory.getLogger(BasePage.class);

    /** Default explicit-wait timeout in seconds. */
    protected static final int WAIT_TIMEOUT_SEC = 10;

    protected final WebDriver    driver;
    protected final WebDriverWait wait;
    protected final String        baseUrl;

    protected BasePage(WebDriver driver, String baseUrl) {
        this.driver  = driver;
        this.baseUrl  = baseUrl;
        this.wait    = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SEC));
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Navigate to an absolute URL. */
    public void navigateTo(String url) {
        log.debug("Navigating to: {}", url);
        driver.get(url);
    }

    /** Navigate to a path relative to baseUrl (e.g. "/login"). */
    public void navigateToPath(String path) {
        navigateTo(baseUrl + path);
    }

    /** Return the current page URL. */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /** Return the current page title. */
    public String getPageTitle() {
        return driver.getTitle();
    }

    // ── Wait helpers ──────────────────────────────────────────────────────────

    /** Wait until element identified by {@code locator} is visible. */
    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Wait until element identified by {@code locator} is clickable. */
    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Wait until the URL contains the given fragment. */
    protected void waitForUrlContaining(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    /** Wait until the URL does NOT contain the given fragment. */
    protected void waitForUrlNotContaining(String fragment) {
        wait.until(driver -> !driver.getCurrentUrl().contains(fragment));
    }

    /** Wait until element identified by {@code locator} contains specific text. */
    protected WebElement waitForTextInElement(By locator, String text) {
        return wait.until(driver -> {
            WebElement el = driver.findElement(locator);
            return (el.getText().contains(text)) ? el : null;
        });
    }

    /** Check whether an element is currently displayed on the page. */
    protected boolean isElementDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    // ── Form helpers ──────────────────────────────────────────────────────────

    /** Clear a field and type text into it. */
    protected void clearAndType(By locator, String text) {
        WebElement el = waitForClickable(locator);
        el.clear();
        el.sendKeys(text);
    }

    /** Click an element, waiting for it to be clickable first. */
    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    /** Return the trimmed text of an element. */
    protected String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }
}
