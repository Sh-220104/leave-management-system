package com.epam.elms.integration.api;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {@code /admin} endpoints.
 *
 * <h3>Covered endpoints</h3>
 * <ul>
 *   <li>PUT  /admin/leave-balance/{employeeId}/adjust?leaveTypeId=&amount=</li>
 *   <li>POST /admin/role/{employeeId}?role=</li>
 *   <li>POST /admin/leave-type?type=&description=</li>
 * </ul>
 *
 * <p>These tests require a running application at {@code app.base.url} (default: http://localhost:8080).
 */
public class AdminApiTest extends BaseApiTest {

    private static final String ADMIN_BASE      = "/admin";
    private static final String REGISTER_URL    = "/auth/register";
    private static final String LOGIN_URL       = "/auth/login";
    private static final String LEAVE_TYPES_URL = "/leave-types";

    // ── Helper: register a fresh employee and return their ID ─────────────────

    private long registerEmployee(String emailPrefix) {
        String email = emailPrefix + "_" + System.currentTimeMillis() + "@epam.com";
        String body  = """
                {"email":"%s","password":"Pass@1234","name":"Employee User","role":"EMPLOYEE"}
                """.formatted(email);
        given().spec(givenNoAuth()).body(body).when().post(REGISTER_URL).then().statusCode(200);

        Response loginResp = given()
                .spec(givenNoAuth())
                .body("""
                        {"email":"%s","password":"Pass@1234"}
                        """.formatted(email))
                .when().post(LOGIN_URL)
                .then().statusCode(200).extract().response();

        return ((Number) loginResp.path("employeeId")).longValue();
    }

    // ── POST /admin/leave-type ────────────────────────────────────────────────

    @Test(description = "POST /admin/leave-type – valid params create a new leave type (200)",
          groups = {"admin", "smoke"})
    public void createLeaveType_validParams_returns200() {
        String type        = "StudyLeave_" + System.currentTimeMillis();
        String description = "Study and exam leave";

        given()
            .spec(givenAdmin())
            .queryParam("type",        type)
            .queryParam("description", description)
        .when()
            .post(ADMIN_BASE + "/leave-type")
        .then()
            .statusCode(200);
    }

    @Test(description = "POST /admin/leave-type – missing params returns 4xx",
          groups = {"admin", "validation"})
    public void createLeaveType_missingParams_returns4xx() {
        given()
            .spec(givenAdmin())
            // no query params intentionally
        .when()
            .post(ADMIN_BASE + "/leave-type")
        .then()
            .statusCode(anyOf(is(400), is(422)));
    }

    @Test(description = "POST /admin/leave-type – unauthenticated returns 401 or 403",
          groups = {"admin", "security"})
    public void createLeaveType_noAuth_returns401or403() {
        given()
            .spec(givenNoAuth())
            .queryParam("type",        "TestType")
            .queryParam("description", "Desc")
        .when()
            .post(ADMIN_BASE + "/leave-type")
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ── POST /admin/role/{employeeId} ─────────────────────────────────────────

    @Test(description = "POST /admin/role/{id} – valid role assignment returns 200",
          groups = {"admin", "smoke"})
    public void setRole_validEmployee_returns200() {
        long empId = registerEmployee("setrole");

        given()
            .spec(givenAdmin())
            .queryParam("role", "MANAGER")
        .when()
            .post(ADMIN_BASE + "/role/" + empId)
        .then()
            .statusCode(200);
    }

    @Test(description = "POST /admin/role/{id} – non-existent employee returns 4xx",
          groups = {"admin"})
    public void setRole_nonExistentEmployee_returns4xx() {
        given()
            .spec(givenAdmin())
            .queryParam("role", "MANAGER")
        .when()
            .post(ADMIN_BASE + "/role/999999")
        .then()
            .statusCode(anyOf(is(400), is(404), is(422)));
    }

    @Test(description = "POST /admin/role/{id} – unauthenticated returns 401 or 403",
          groups = {"admin", "security"})
    public void setRole_noAuth_returns401or403() {
        given()
            .spec(givenNoAuth())
            .queryParam("role", "MANAGER")
        .when()
            .post(ADMIN_BASE + "/role/1")
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ── PUT /admin/leave-balance/{employeeId}/adjust ──────────────────────────

    @Test(description = "PUT /admin/leave-balance/{id}/adjust – valid adjustment returns 200",
          groups = {"admin", "smoke"})
    public void adjustLeaveBalance_existingBalance_returns200() {
        // Create a leave type first so we have a valid leaveTypeId
        String ltType = "AdjustLeave_" + System.currentTimeMillis();
        given()
            .spec(givenAdmin())
            .queryParam("type",        ltType)
            .queryParam("description", "Adjust test")
        .when()
            .post(ADMIN_BASE + "/leave-type")
        .then()
            .statusCode(200);

        // Register employee (this auto-creates balances for all leave types)
        long empId = registerEmployee("adjust_bal");

        // Get all leave types and pick one
        java.util.List<java.util.Map<String, Object>> types = given()
                .spec(givenAdmin())
                .when().get(LEAVE_TYPES_URL)
                .then().statusCode(200).extract().jsonPath().getList("$");

        if (types == null || types.isEmpty()) return; // Skip if no leave types

        long leaveTypeId = ((Number) types.get(0).get("id")).longValue();

        given()
            .spec(givenAdmin())
            .queryParam("leaveTypeId", leaveTypeId)
            .queryParam("amount",      25.0)
        .when()
            .put(ADMIN_BASE + "/leave-balance/" + empId + "/adjust")
        .then()
            .statusCode(200);
    }

    @Test(description = "PUT /admin/leave-balance/{id}/adjust – non-existent employee/type returns 4xx",
          groups = {"admin"})
    public void adjustLeaveBalance_notFound_returns4xx() {
        given()
            .spec(givenAdmin())
            .queryParam("leaveTypeId", 999999L)
            .queryParam("amount",      10.0)
        .when()
            .put(ADMIN_BASE + "/leave-balance/999999/adjust")
        .then()
            .statusCode(anyOf(is(400), is(404), is(422)));
    }

    @Test(description = "PUT /admin/leave-balance/{id}/adjust – unauthenticated returns 401 or 403",
          groups = {"admin", "security"})
    public void adjustLeaveBalance_noAuth_returns401or403() {
        given()
            .spec(givenNoAuth())
            .queryParam("leaveTypeId", 1L)
            .queryParam("amount",      10.0)
        .when()
            .put(ADMIN_BASE + "/leave-balance/1/adjust")
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }

    // ── Combined admin flow ───────────────────────────────────────────────────

    @Test(description = "Admin flow: create leave type → register employee → adjust balance",
          groups = {"admin", "smoke"})
    public void adminFlow_createTypeAndAdjustBalance_succeeds() {
        // Step 1: Create leave type
        String ltType = "FlowLeave_" + System.currentTimeMillis();
        given()
            .spec(givenAdmin())
            .queryParam("type",        ltType)
            .queryParam("description", "Admin flow leave type")
        .when()
            .post(ADMIN_BASE + "/leave-type")
        .then()
            .statusCode(200);

        // Step 2: Register an employee (balances auto-created)
        long empId = registerEmployee("flow_admin");

        // Step 3: Fetch leave types and pick last (our just-created one may be last)
        java.util.List<java.util.Map<String, Object>> types = given()
                .spec(givenAdmin())
                .when().get(LEAVE_TYPES_URL)
                .then().statusCode(200).extract().jsonPath().getList("$");

        if (types == null || types.isEmpty()) return;

        long ltId = ((Number) types.get(types.size() - 1).get("id")).longValue();

        // Step 4: Adjust balance
        given()
            .spec(givenAdmin())
            .queryParam("leaveTypeId", ltId)
            .queryParam("amount",      30.0)
        .when()
            .put(ADMIN_BASE + "/leave-balance/" + empId + "/adjust")
        .then()
            .statusCode(200);
    }
}
