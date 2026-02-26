package com.epam.elms.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the Login page ({@code /login}).
 *
 * <p>Maps UI elements to the React component at {@code frontend/src/pages/LoginPage.jsx}.
 */
public class LoginPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private static final By EMAIL_INPUT    = By.cssSelector("input[type='email']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password']");
    private static final By SUBMIT_BUTTON  = By.cssSelector("button[type='submit']");
    private static final By ERROR_MESSAGE  = By.cssSelector(".error-msg");
    private static final By PAGE_HEADING   = By.cssSelector("h2");

    // ── Constructor ───────────────────────────────────────────────────────────

    public LoginPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Navigate directly to the login page. */
    public LoginPage open() {
        navigateToPath("/login");
        return this;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Type into the email input field. */
    public LoginPage enterEmail(String email) {
        clearAndType(EMAIL_INPUT, email);
        return this;
    }

    /** Type into the password input field. */
    public LoginPage enterPassword(String password) {
        clearAndType(PASSWORD_INPUT, password);
        return this;
    }

    /** Click the login / submit button. */
    public LoginPage clickLogin() {
        click(SUBMIT_BUTTON);
        return this;
    }

    /**
     * Convenience method: fill form and submit.
     *
     * @param email    the email to enter
     * @param password the password to enter
     * @return this LoginPage instance (for chaining)
     */
    public LoginPage loginWith(String email, String password) {
        return enterEmail(email).enterPassword(password).clickLogin();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** @return true if the page heading "Login" is visible. */
    public boolean isLoginFormVisible() {
        return isElementDisplayed(PAGE_HEADING) &&
               isElementDisplayed(EMAIL_INPUT)  &&
               isElementDisplayed(SUBMIT_BUTTON);
    }

    /** @return the error message text, or empty string if no error is displayed. */
    public String getErrorMessage() {
        if (isElementDisplayed(ERROR_MESSAGE)) {
            return getText(ERROR_MESSAGE);
        }
        return "";
    }

    /** @return true if an error message element is displayed. */
    public boolean hasErrorMessage() {
        return isElementDisplayed(ERROR_MESSAGE);
    }

    /** @return the page heading text (expected: "Login"). */
    public String getHeadingText() {
        return getText(PAGE_HEADING);
    }

    /** @return true if the current URL contains "/login". */
    public boolean isOnLoginPage() {
        return getCurrentUrl().contains("/login");
    }

    /**
     * Wait until the URL changes away from the login page (indicating successful redirect).
     */
    public void waitForSuccessfulLogin() {
        waitForUrlNotContaining("/login");
    }
}
