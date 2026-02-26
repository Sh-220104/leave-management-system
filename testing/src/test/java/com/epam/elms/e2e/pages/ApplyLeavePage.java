package com.epam.elms.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * Page Object for the Apply Leave Form page ({@code /apply-leave}).
 *
 * <p>Maps UI elements to the React component at
 * {@code frontend/src/pages/ApplyLeaveForm.jsx}.
 */
public class ApplyLeavePage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private static final By HEADING           = By.cssSelector("h2,h1");
    private static final By LEAVE_TYPE_SELECT = By.cssSelector("select");
    private static final By START_DATE_INPUT  = By.cssSelector("input[type='date'][name*='start'], input[type='date']:first-of-type");
    private static final By END_DATE_INPUT    = By.cssSelector("input[type='date'][name*='end'], input[type='date']:last-of-type");
    private static final By REASON_INPUT      = By.cssSelector("textarea, input[type='text'][name*='reason']");
    private static final By SUBMIT_BUTTON     = By.cssSelector("button[type='submit']");
    private static final By SUCCESS_MESSAGE   = By.cssSelector(".success-msg, .alert-success, [class*='success']");
    private static final By ERROR_MESSAGE     = By.cssSelector(".error-msg, .alert-danger, [class*='error']");

    // ── Constructor ───────────────────────────────────────────────────────────

    public ApplyLeavePage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Navigate directly to the apply-leave form. */
    public ApplyLeavePage open() {
        navigateToPath("/apply-leave");
        return this;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Select a leave type by visible text. */
    public ApplyLeavePage selectLeaveType(String visibleText) {
        Select select = new Select(waitForClickable(LEAVE_TYPE_SELECT));
        select.selectByVisibleText(visibleText);
        return this;
    }

    /** Select a leave type by index (0-based). */
    public ApplyLeavePage selectLeaveTypeByIndex(int index) {
        Select select = new Select(waitForClickable(LEAVE_TYPE_SELECT));
        select.selectByIndex(index);
        return this;
    }

    /** Enter start date in YYYY-MM-DD format. */
    public ApplyLeavePage enterStartDate(String date) {
        clearAndType(START_DATE_INPUT, date);
        return this;
    }

    /** Enter end date in YYYY-MM-DD format. */
    public ApplyLeavePage enterEndDate(String date) {
        clearAndType(END_DATE_INPUT, date);
        return this;
    }

    /** Enter the reason / notes. */
    public ApplyLeavePage enterReason(String reason) {
        clearAndType(REASON_INPUT, reason);
        return this;
    }

    /** Click the submit / apply button. */
    public ApplyLeavePage clickSubmit() {
        click(SUBMIT_BUTTON);
        return this;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** @return true if the apply-leave form heading is visible. */
    public boolean isFormVisible() {
        return isElementDisplayed(HEADING) && isElementDisplayed(SUBMIT_BUTTON);
    }

    /** @return true if a success message is displayed. */
    public boolean isSuccessMessageVisible() {
        return isElementDisplayed(SUCCESS_MESSAGE);
    }

    /** @return true if an error message is displayed. */
    public boolean isErrorMessageVisible() {
        return isElementDisplayed(ERROR_MESSAGE);
    }

    /** @return the heading text. */
    public String getHeadingText() {
        return getText(HEADING);
    }

    /** @return the error message text (or empty string). */
    public String getErrorMessageText() {
        return isErrorMessageVisible() ? getText(ERROR_MESSAGE) : "";
    }

    /** @return true if the current URL contains "apply-leave". */
    public boolean isOnApplyLeavePage() {
        return getCurrentUrl().contains("apply-leave");
    }
}
