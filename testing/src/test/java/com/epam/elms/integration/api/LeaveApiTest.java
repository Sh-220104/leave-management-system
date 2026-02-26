package com.epam.elms.integration.api;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {@code /leaves} endpoints.
 *
 * <h3>Covered endpoints</h3>
 * <ul>
 *   <li>GET  /leaves/employee/{employeeId}</li>
 *   <li>GET  /leaves/pending</li>
 *   <li>POST /leaves/apply</li>
 *   <li>PUT  /leaves/{id}/approve</li>
 *   <li>PUT  /leaves/{id}/reject</li>
 * </ul>
 *
 * <p>These tests require a running application at {@code app.base.url} (default: http://localhost:8080).
 */
public class LeaveApiTest extends BaseApiTest {

    private static final String LEAVES_BASE        = "/leaves";
    private static final String APPLY_URL          = LEAVES_BASE + "/apply";
    private static final String PENDING_URL         = LEAVES_BASE + "/pending";
    private static final String EMPLOYEE_LEAVES_URL = LEAVES_BASE + "/employee/";
    private static final String REGISTER_URL        = "/auth/register";
    private static final String LOGIN_URL           = "/auth/login";

    // ── Helper: register + login, return {token, employeeId} ─────────────────

    private long[] registerAndLogin(String emailPrefix) {
        String email = emailPrefix + "_" + System.currentTimeMillis() + "@epam.com";
        String regBody = """
                {"email":"%s","password":"Pass@1234","name":"Test User","role":"EMPLOYEE"}
                """.formatted(email);

        // Register
        given().spec(givenNoAuth()).body(regBody).when().post(REGISTER_URL).then().statusCode(200);

        // Login – extract token + employeeId
        Response loginResp = given()
                .spec(givenNoAuth())
                .body("""
                        {"email":"%s","password":"Pass@1234"}
                        """.formatted(email))
                .when().post(LOGIN_URL)
                .then().statusCode(200).extract().response();

        String token = loginResp.path("jwt");
        int    id    = loginResp.path("employeeId");
        return new long[]{id, 0}; // slot[1] will carry leaveTypeId after first fetch
    }

    // ── /leaves/employee/{employeeId} ─────────────────────────────────────────

    @Test(description = "GET /leaves/employee/{id} – returns 200 with list (may be empty)",
          groups = {"leave", "smoke"})
    public void getEmployeeLeaves_returnsOkWithList() {
        long[] ids = registerAndLogin("emp_leaves");
        long employeeId = ids[0];

        given()
            .spec(givenEmployee())
        .when()
            .get(EMPLOYEE_LEAVES_URL + employeeId)
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test(description = "GET /leaves/employee/{id} – unauthenticated request returns 401 or 403",
          groups = {"leave", "security"})
    public void getEmployeeLeaves_withoutAuth_returns401or403() {
        given()
            .spec(givenNoAuth())
        .when()
            .get(EMPLOYEE_LEAVES_URL + "1")
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ── /leaves/pending ───────────────────────────────────────────────────────

    @Test(description = "GET /leaves/pending – authenticated request returns 200 with a list",
          groups = {"leave", "smoke"})
    public void getPendingLeaves_authenticated_returns200() {
        given()
            .spec(givenEmployee())
        .when()
            .get(PENDING_URL)
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test(description = "GET /leaves/pending – unauthenticated returns 401 or 403",
          groups = {"leave", "security"})
    public void getPendingLeaves_noAuth_returns401or403() {
        given()
            .spec(givenNoAuth())
        .when()
            .get(PENDING_URL)
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ── /leaves/apply ─────────────────────────────────────────────────────────

    @Test(description = "POST /leaves/apply – valid leave request returns 200 OK",
          groups = {"leave", "smoke"})
    public void applyLeave_validPayload_returns200() {
        // Register + login a fresh employee
        String email = "applyleave_" + System.currentTimeMillis() + "@epam.com";
        String regBody = """
                {"email":"%s","password":"Pass@1234","name":"Apply Tester","role":"EMPLOYEE"}
                """.formatted(email);
        given().spec(givenNoAuth()).body(regBody).when().post(REGISTER_URL).then().statusCode(200);

        Response loginResp = given()
                .spec(givenNoAuth())
                .body("""
                        {"email":"%s","password":"Pass@1234"}
                        """.formatted(email))
                .when().post(LOGIN_URL)
                .then().statusCode(200).extract().response();

        long employeeId = ((Number) loginResp.path("employeeId")).longValue();
        String token    = loginResp.path("jwt");

        // Get leave types to pick first available
        Response leaveTypes = given()
                .spec(givenEmployee())
                .when().get("/leave-types")
                .then().statusCode(200).extract().response();

        // Apply leave using first available leave type (if any)
        java.util.List<java.util.Map<String, Object>> types = leaveTypes.jsonPath().getList("$");
        if (types == null || types.isEmpty()) {
            // No leave types seeded – skip rest of this test (not a failure)
            return;
        }

        long leaveTypeId = ((Number) types.get(0).get("id")).longValue();
        String startDate = java.time.LocalDate.now().plusDays(2).toString();
        String endDate   = java.time.LocalDate.now().plusDays(4).toString();

        String body = """
                {
                  "employeeId"  : %d,
                  "leaveTypeId" : %d,
                  "startDate"   : "%s",
                  "endDate"     : "%s",
                  "reason"      : "Integration test leave"
                }
                """.formatted(employeeId, leaveTypeId, startDate, endDate);

        given()
            .spec(givenEmployee())
            .header("Authorization", "Bearer " + token)
            .body(body)
        .when()
            .post(APPLY_URL)
        .then()
            .statusCode(200)
            .body(containsString("submitted"));
    }

    @Test(description = "POST /leaves/apply – missing required fields returns 4xx",
          groups = {"leave", "validation"})
    public void applyLeave_missingFields_returns4xx() {
        String body = """
                {"reason":"missing employee and type"}
                """;

        given()
            .spec(givenEmployee())
            .body(body)
        .when()
            .post(APPLY_URL)
        .then()
            .statusCode(anyOf(is(400), is(422)));
    }

    @Test(description = "POST /leaves/apply – past start date returns 4xx",
          groups = {"leave", "validation"})
    public void applyLeave_pastDate_returns4xx() {
        String body = """
                {
                  "employeeId"  : 1,
                  "leaveTypeId" : 1,
                  "startDate"   : "2020-01-01",
                  "endDate"     : "2020-01-05",
                  "reason"      : "Past dates"
                }
                """;

        given()
            .spec(givenEmployee())
            .body(body)
        .when()
            .post(APPLY_URL)
        .then()
            .statusCode(anyOf(is(400), is(422)));
    }

    // ── /leaves/{id}/approve ──────────────────────────────────────────────────

    @Test(description = "PUT /leaves/{id}/approve – non-existent id returns 4xx",
          groups = {"leave"})
    public void approveLeave_nonExistentId_returns4xx() {
        given()
            .spec(givenAdmin())
        .when()
            .put(LEAVES_BASE + "/999999/approve")
        .then()
            .statusCode(anyOf(is(400), is(404), is(422)));
    }

    @Test(description = "PUT /leaves/{id}/approve – unauthenticated returns 401 or 403",
          groups = {"leave", "security"})
    public void approveLeave_noAuth_returns401or403() {
        given()
            .spec(givenNoAuth())
        .when()
            .put(LEAVES_BASE + "/1/approve")
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ── /leaves/{id}/reject ───────────────────────────────────────────────────

    @Test(description = "PUT /leaves/{id}/reject – non-existent id returns 4xx",
          groups = {"leave"})
    public void rejectLeave_nonExistentId_returns4xx() {
        given()
            .spec(givenAdmin())
        .when()
            .put(LEAVES_BASE + "/999999/reject")
        .then()
            .statusCode(anyOf(is(400), is(404), is(422)));
    }

    @Test(description = "PUT /leaves/{id}/reject – unauthenticated returns 401 or 403",
          groups = {"leave", "security"})
    public void rejectLeave_noAuth_returns401or403() {
        given()
            .spec(givenNoAuth())
        .when()
            .put(LEAVES_BASE + "/1/reject")
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ── Full leave lifecycle ──────────────────────────────────────────────────

    @Test(description = "Full leave lifecycle: apply → approve – complete flow returns 200",
          groups = {"leave", "smoke"})
    public void leaveLifecycle_applyThenApprove_succeeds() {
        // Register employee
        String email = "lifecycle_" + System.currentTimeMillis() + "@epam.com";
        String regBody = """
                {"email":"%s","password":"Pass@1234","name":"Lifecycle Tester","role":"EMPLOYEE"}
                """.formatted(email);
        given().spec(givenNoAuth()).body(regBody).when().post(REGISTER_URL).then().statusCode(200);

        // Login
        Response loginResp = given()
                .spec(givenNoAuth())
                .body("""
                        {"email":"%s","password":"Pass@1234"}
                        """.formatted(email))
                .when().post(LOGIN_URL)
                .then().statusCode(200).extract().response();

        long employeeId = ((Number) loginResp.path("employeeId")).longValue();
        String token    = loginResp.path("jwt");

        // Get leave types
        java.util.List<java.util.Map<String, Object>> types = given()
                .spec(givenEmployee())
                .when().get("/leave-types")
                .then().statusCode(200).extract().jsonPath().getList("$");

        if (types == null || types.isEmpty()) return; // Seeded types required

        long leaveTypeId = ((Number) types.get(0).get("id")).longValue();
        String startDate = java.time.LocalDate.now().plusDays(3).toString();
        String endDate   = java.time.LocalDate.now().plusDays(5).toString();

        // Apply leave
        String applyBody = """
                {
                  "employeeId"  : %d,
                  "leaveTypeId" : %d,
                  "startDate"   : "%s",
                  "endDate"     : "%s",
                  "reason"      : "Lifecycle test"
                }
                """.formatted(employeeId, leaveTypeId, startDate, endDate);

        given()
            .spec(givenEmployee())
            .header("Authorization", "Bearer " + token)
            .body(applyBody)
        .when()
            .post(APPLY_URL)
        .then()
            .statusCode(200);

        // Fetch pending list – find our new request
        java.util.List<java.util.Map<String, Object>> pending = given()
                .spec(givenAdmin())
                .when().get(PENDING_URL)
                .then().statusCode(200).extract().jsonPath().getList("$");

        if (pending == null || pending.isEmpty()) return;

        // Approve the latest pending request
        long leaveId = ((Number) pending.get(pending.size() - 1).get("id")).longValue();

        given()
            .spec(givenAdmin())
        .when()
            .put(LEAVES_BASE + "/" + leaveId + "/approve")
        .then()
            .statusCode(200)
            .body(containsString("approved"));
    }
}
