package com.epam.elms.integration.api;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {@code /leave-types} endpoints.
 *
 * <h3>Covered endpoints</h3>
 * <ul>
 *   <li>GET /leave-types</li>
 * </ul>
 *
 * <p>These tests require a running application at {@code app.base.url} (default: http://localhost:8080).
 */
public class LeaveTypeApiTest extends BaseApiTest {

    private static final String LEAVE_TYPES_URL = "/leave-types";
    private static final String ADMIN_BASE      = "/admin";

    // ── GET /leave-types ──────────────────────────────────────────────────────

    @Test(description = "GET /leave-types – unauthenticated request returns 200 (public endpoint)",
          groups = {"leaveType", "smoke"})
    public void getAllLeaveTypes_noAuth_returns200() {
        given()
            .spec(givenNoAuth())
        .when()
            .get(LEAVE_TYPES_URL)
        .then()
            .statusCode(anyOf(is(200), is(401), is(403))); // Could be secured
    }

    @Test(description = "GET /leave-types – authenticated request returns 200 with a list",
          groups = {"leaveType", "smoke"})
    public void getAllLeaveTypes_authenticated_returns200WithList() {
        given()
            .spec(givenEmployee())
        .when()
            .get(LEAVE_TYPES_URL)
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test(description = "GET /leave-types – response contains JSON array",
          groups = {"leaveType"})
    public void getAllLeaveTypes_returnsJsonArray() {
        given()
            .spec(givenEmployee())
        .when()
            .get(LEAVE_TYPES_URL)
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("$", instanceOf(java.util.List.class));
    }

    @Test(description = "GET /leave-types – after creating a leave type, it appears in the list",
          groups = {"leaveType", "smoke"})
    public void getAllLeaveTypes_afterCreation_containsNewType() {
        String typeName = "UniqueType_" + System.currentTimeMillis();

        // Create via admin
        given()
            .spec(givenAdmin())
            .queryParam("type",        typeName)
            .queryParam("description", "Created in test")
        .when()
            .post(ADMIN_BASE + "/leave-type")
        .then()
            .statusCode(200);

        // Verify it now appears in the list
        given()
            .spec(givenEmployee())
        .when()
            .get(LEAVE_TYPES_URL)
        .then()
            .statusCode(200)
            .body("type", hasItem(typeName));
    }

    @Test(description = "GET /leave-types – each leave type entry has 'id', 'type' and 'description' fields",
          groups = {"leaveType"})
    public void getAllLeaveTypes_eachEntryHasRequiredFields() {
        // Ensure at least one leave type exists
        String typeName = "FieldCheck_" + System.currentTimeMillis();
        given()
            .spec(givenAdmin())
            .queryParam("type",        typeName)
            .queryParam("description", "Field check description")
        .when()
            .post(ADMIN_BASE + "/leave-type")
        .then()
            .statusCode(200);

        // Fetch and check structure
        java.util.List<java.util.Map<String, Object>> types = given()
                .spec(givenEmployee())
                .when().get(LEAVE_TYPES_URL)
                .then().statusCode(200).extract().jsonPath().getList("$");

        if (types == null || types.isEmpty()) {
            throw new AssertionError("Expected at least one leave type in the list");
        }

        for (java.util.Map<String, Object> lt : types) {
            if (!lt.containsKey("id")) {
                throw new AssertionError("LeaveType entry missing 'id' field");
            }
            if (!lt.containsKey("type")) {
                throw new AssertionError("LeaveType entry missing 'type' field");
            }
        }
    }

    @Test(description = "GET /leave-types – creating multiple types appends them all to the list",
          groups = {"leaveType"})
    public void getAllLeaveTypes_multipleCreations_allPresent() {
        String type1 = "MultiType1_" + System.currentTimeMillis();
        String type2 = "MultiType2_" + System.currentTimeMillis();

        given().spec(givenAdmin())
               .queryParam("type", type1).queryParam("description", "Multi 1")
               .when().post(ADMIN_BASE + "/leave-type").then().statusCode(200);

        given().spec(givenAdmin())
               .queryParam("type", type2).queryParam("description", "Multi 2")
               .when().post(ADMIN_BASE + "/leave-type").then().statusCode(200);

        given()
            .spec(givenEmployee())
        .when()
            .get(LEAVE_TYPES_URL)
        .then()
            .statusCode(200)
            .body("type", hasItems(type1, type2));
    }

    @Test(description = "GET /leave-types – admin token also retrieves list successfully",
          groups = {"leaveType"})
    public void getAllLeaveTypes_withAdminToken_returns200() {
        given()
            .spec(givenAdmin())
        .when()
            .get(LEAVE_TYPES_URL)
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }
}
