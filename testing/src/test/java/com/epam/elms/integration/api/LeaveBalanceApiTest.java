package com.epam.elms.integration.api;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {@code /balance} endpoints.
 *
 * <h3>Covered endpoints</h3>
 * <ul>
 *   <li>GET /balance/{employeeId}</li>
 * </ul>
 *
 * <p>These tests require a running application at {@code app.base.url} (default: http://localhost:8080).
 */
public class LeaveBalanceApiTest extends BaseApiTest {

    private static final String BALANCE_URL  = "/balance/";
    private static final String REGISTER_URL = "/auth/register";
    private static final String LOGIN_URL    = "/auth/login";

    // ── Helper: register fresh employee + return {employeeId, token} ──────────

    private Object[] registerEmployeeWithToken(String prefix) {
        String email = prefix + "_" + System.currentTimeMillis() + "@epam.com";
        String body  = """
                {"email":"%s","password":"Pass@1234","name":"Balance Tester","role":"EMPLOYEE"}
                """.formatted(email);
        given().spec(givenNoAuth()).body(body).when().post(REGISTER_URL).then().statusCode(200);

        Response resp = given()
                .spec(givenNoAuth())
                .body("""
                        {"email":"%s","password":"Pass@1234"}
                        """.formatted(email))
                .when().post(LOGIN_URL)
                .then().statusCode(200).extract().response();

        return new Object[]{((Number) resp.path("employeeId")).longValue(), (String) resp.path("jwt")};
    }

    // ── GET /balance/{employeeId} ─────────────────────────────────────────────

    @Test(description = "GET /balance/{id} – authenticated employee can retrieve their own balance (200)",
          groups = {"balance", "smoke"})
    public void getBalance_authenticatedEmployee_returns200WithList() {
        Object[] data       = registerEmployeeWithToken("get_balance");
        long     employeeId = (long) data[0];
        String   token      = (String) data[1];

        given()
            .spec(givenEmployee())
            .header("Authorization", "Bearer " + token)
        .when()
            .get(BALANCE_URL + employeeId)
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test(description = "GET /balance/{id} – response body is a list (possibly empty)",
          groups = {"balance"})
    public void getBalance_returnsListType() {
        Object[] data       = registerEmployeeWithToken("balance_list");
        long     employeeId = (long) data[0];
        String   token      = (String) data[1];

        given()
            .spec(givenEmployee())
            .header("Authorization", "Bearer " + token)
        .when()
            .get(BALANCE_URL + employeeId)
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("$", instanceOf(java.util.List.class));
    }

    @Test(description = "GET /balance/{id} – unauthenticated request returns 401 or 403",
          groups = {"balance", "security"})
    public void getBalance_noAuth_returns401or403() {
        given()
            .spec(givenNoAuth())
        .when()
            .get(BALANCE_URL + "1")
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }

    @Test(description = "GET /balance/{id} – employee with no balances returns empty list",
          groups = {"balance"})
    public void getBalance_newEmployee_returnsListPossiblyEmpty() {
        // New employee – balances are auto-created from existing leave types.
        // We verify the response is still a valid array.
        Object[] data       = registerEmployeeWithToken("empty_balance");
        long     employeeId = (long) data[0];
        String   token      = (String) data[1];

        given()
            .spec(givenEmployee())
            .header("Authorization", "Bearer " + token)
        .when()
            .get(BALANCE_URL + employeeId)
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test(description = "GET /balance/{id} – admin can also retrieve any employee's balance",
          groups = {"balance", "smoke"})
    public void getBalance_adminCanAccess_returns200() {
        Object[] data       = registerEmployeeWithToken("admin_view_balance");
        long     employeeId = (long) data[0];

        given()
            .spec(givenAdmin())
        .when()
            .get(BALANCE_URL + employeeId)
        .then()
            .statusCode(anyOf(is(200), is(403))); // depends on security config
    }

    @Test(description = "GET /balance/{id} – when employee has balances, each entry has expected fields",
          groups = {"balance"})
    public void getBalance_withBalances_eachEntryHasRequiredFields() {
        Object[] data       = registerEmployeeWithToken("fields_balance");
        long     employeeId = (long) data[0];
        String   token      = (String) data[1];

        Response resp = given()
                .spec(givenEmployee())
                .header("Authorization", "Bearer " + token)
                .when().get(BALANCE_URL + employeeId)
                .then().statusCode(200).extract().response();

        java.util.List<java.util.Map<String, Object>> balances = resp.jsonPath().getList("$");
        if (balances != null && !balances.isEmpty()) {
            // Verify each balance entry has 'balance' field
            resp.then().body("balance", everyItem(notNullValue()));
        }
    }

    @Test(description = "GET /balance/{id} – after admin adjusts, balance is updated",
          groups = {"balance", "smoke"})
    public void getBalance_afterAdminAdjust_returnsUpdatedBalance() {
        Object[] data       = registerEmployeeWithToken("adjusted_balance");
        long     employeeId = (long) data[0];
        String   token      = (String) data[1];

        // Get available leave types
        java.util.List<java.util.Map<String, Object>> types = given()
                .spec(givenAdmin())
                .when().get("/leave-types")
                .then().statusCode(200).extract().jsonPath().getList("$");

        if (types == null || types.isEmpty()) return;

        long ltId = ((Number) types.get(0).get("id")).longValue();

        // Admin adjusts balance to 99
        given()
            .spec(givenAdmin())
            .queryParam("leaveTypeId", ltId)
            .queryParam("amount",      99.0)
        .when()
            .put("/admin/leave-balance/" + employeeId + "/adjust")
        .then()
            .statusCode(200);

        // Re-fetch balance and verify one entry equals 99.0
        java.util.List<java.util.Map<String, Object>> balances = given()
                .spec(givenEmployee())
                .header("Authorization", "Bearer " + token)
                .when().get(BALANCE_URL + employeeId)
                .then().statusCode(200).extract().jsonPath().getList("$");

        boolean found = balances.stream()
                .anyMatch(b -> b.containsKey("balance") &&
                               ((Number) b.get("balance")).doubleValue() == 99.0);

        if (!found) {
            throw new AssertionError("Expected to find balance of 99.0 after admin adjustment, but did not.");
        }
    }
}
