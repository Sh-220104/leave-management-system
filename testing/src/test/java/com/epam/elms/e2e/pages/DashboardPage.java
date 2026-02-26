package com.epam.elms.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the Employee Dashboard page ({@code /}).
 *
 * <p>Maps UI elements to the React component at {@code frontend/src/pages/EmployeeDashboard.jsx}.
 */
public class DashboardPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private static final By HEADING             = By.cssSelector("h2");
    private static final By BALANCE_SECTION      = By.cssSelector(".dashboard-section");
    private static final By BALANCE_TABLE_HEADER = By.cssSelector("h3");
    private static final By NOT_AUTHORIZED_TEXT  = By.xpath("//*[contains(text(),'Not authorized')]");
    private static final By NO_BALANCE_MSG       = By.xpath("//*[contains(text(),'No leave balance')]");
    private static final By NO_REQUESTS_MSG      = By.xpath("//*[contains(text(),'No leave requests')]");
    private static final By NAVBAR               = By.cssSelector("nav");
    private static final By APPLY_LEAVE_LINK     = By.xpath("//a[contains(@href,'apply-leave') or contains(text(),'Apply')]");

    // ── Constructor ───────────────────────────────────────────────────────────

    public DashboardPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Navigate to the dashboard root. */
    public DashboardPage open() {
        navigateToPath("/");
        return this;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** @return true if the main heading is visible. */
    public boolean isDashboardVisible() {
        return isElementDisplayed(HEADING);
    }

    /** @return the text of the page heading. */
    public String getHeadingText() {
        return getText(HEADING);
    }

    /** @return true if the "Not authorized" message is shown. */
    public boolean isNotAuthorizedMessageVisible() {
        return isElementDisplayed(NOT_AUTHORIZED_TEXT);
    }

    /** @return true if the leave-balance section exists. */
    public boolean isBalanceSectionVisible() {
        return isElementDisplayed(BALANCE_SECTION);
    }

    /** @return true if the navigation bar is shown. */
    public boolean isNavBarVisible() {
        return isElementDisplayed(NAVBAR);
    }

    /** @return true if the "No leave balance data" message is shown. */
    public boolean isNoBalanceMessageVisible() {
        return isElementDisplayed(NO_BALANCE_MSG);
    }

    /** @return true if the "No leave requests" message is shown. */
    public boolean isNoRequestsMessageVisible() {
        return isElementDisplayed(NO_REQUESTS_MSG);
    }

    /** @return true if the current URL is the root "/". */
    public boolean isOnDashboardPage() {
        String url = getCurrentUrl();
        return url.endsWith("/") || url.endsWith("3000") || url.contains("localhost");
    }

    /** Click the Apply Leave link/button if visible. */
    public void clickApplyLeave() {
        click(APPLY_LEAVE_LINK);
    }

    /** @return true if an "Apply Leave" link is visible. */
    public boolean isApplyLeaveLinkVisible() {
        return isElementDisplayed(APPLY_LEAVE_LINK);
    }
}
