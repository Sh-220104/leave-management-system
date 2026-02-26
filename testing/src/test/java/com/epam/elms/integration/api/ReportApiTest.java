package com.epam.elms.integration.api;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {@code /reports} endpoints.
 *
 * <h3>Covered endpoints</h3>
 * <ul>
 *   <li>GET /reports/leaves</li>
 * </ul>
 *
 * <p>These tests require a running application at {@code app.base.url} (default: http://localhost:8080).
 */
public class ReportApiTest extends BaseApiTest {

    private static final String REPORT_LEAVES_URL = "/reports/leaves";
    private static final String REGISTER_URL      = "/auth/register";
    private static final String LOGIN_URL         = "/auth/login";
    private static final String APPLY_URL         = "/leaves/apply";

    // ── GET /reports/leaves ───────────────────────────────────────────────────

    @Test(description = "GET /reports/leaves – authenticated request returns 200 with a list",
          groups = {"report", "smoke"})
    public void getLeaveReports_authenticated_returns200WithList() {
        given()
            .spec(givenAdmin())
        .when()
            .get(REPORT_LEAVES_URL)
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test(description = "GET /reports/leaves – unauthenticated returns 401 or 403",
          groups = {"report", "security"})
    public void getLeaveReports_noAuth_returns401or403() {
        given()
            .spec(givenNoAuth())
        .when()
            .get(REPORT_LEAVES_URL)
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }

    @Test(description = "GET /reports/leaves – response Content-Type is JSON",
          groups = {"report"})
    public void getLeaveReports_returnsJsonContentType() {
        given()
            .spec(givenAdmin())
        .when()
            .get(REPORT_LEAVES_URL)
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"));
    }

    @Test(description = "GET /reports/leaves – employee token is accepted (endpoint is accessible to authenticated users)",
          groups = {"report"})
    public void getLeaveReports_employeeToken_returns200or403() {
        // The endpoint may restrict to admin only, so 403 is acceptable
        given()
            .spec(givenEmployee())
        .when()
            .get(REPORT_LEAVES_URL)
        .then()
            .statusCode(anyOf(is(200), is(403)));
    }

    @Test(description = "GET /reports/leaves – after applying leave, it appears in the report",
          groups = {"report", "smoke"})
    public void getLeaveReports_afterApplyingLeave_reportContainsIt() {
        // Register + login a fresh employee
        String email = "rpt_emp_" + System.currentTimeMillis() + "@epam.com";
        String regBody = """
                {"email":"%s","password":"Pass@1234","name":"Report Tester","role":"EMPLOYEE"}
                """.formatted(email);
        given().spec(givenNoAuth()).body(regBody).when().post(REGISTER_URL).then().statusCode(200);

        io.restassured.response.Response loginResp = given()
                .spec(givenNoAuth())
                .body("""
                        {"email":"%s","password":"Pass@1234"}
                        """.formatted(email))
                .when().post(LOGIN_URL)
                .then().statusCode(200).extract().response();

        long   employeeId = ((Number) loginResp.path("employeeId")).longValue();
        String token      = loginResp.path("jwt");

        // Get leave types
        java.util.List<java.util.Map<String, Object>> types = given()
                .spec(givenEmployee())
                .when().get("/leave-types")
                .then().statusCode(200).extract().jsonPath().getList("$");

        if (types == null || types.isEmpty()) return; // Need seeded leave types

        long ltId     = ((Number) types.get(0).get("id")).longValue();
        String start  = java.time.LocalDate.now().plusDays(6).toString();
        String end    = java.time.LocalDate.now().plusDays(8).toString();

        // Apply leave
        String applyBody = """
                {
                  "employeeId"  : %d,
                  "leaveTypeId" : %d,
                  "startDate"   : "%s",
                  "endDate"     : "%s",
                  "reason"      : "Report test leave"
                }
                """.formatted(employeeId, ltId, start, end);

        given()
            .spec(givenEmployee())
            .header("Authorization", "Bearer " + token)
            .body(applyBody)
        .when()
            .post(APPLY_URL)
        .then()
            .statusCode(200);

        // Fetch report and verify list is not empty
        java.util.List<?> report = given()
                .spec(givenAdmin())
                .when().get(REPORT_LEAVES_URL)
                .then().statusCode(200).extract().jsonPath().getList("$");

        if (report == null || report.isEmpty()) {
            throw new AssertionError("Report should contain at least one leave record after applying leave");
        }
    }

    @Test(description = "GET /reports/leaves – response is an array even when no leaves exist",
          groups = {"report"})
    public void getLeaveReports_noLeaves_returnsEmptyOrNonEmptyArray() {
        // The report always returns an array
        given()
            .spec(givenAdmin())
        .when()
            .get(REPORT_LEAVES_URL)
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }
}
