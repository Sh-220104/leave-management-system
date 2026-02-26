package com.epam.elms.integration.api;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {@code /auth} endpoints.
 *
 * <h3>Covered endpoints</h3>
 * <ul>
 *   <li>POST /auth/register</li>
 *   <li>POST /auth/login</li>
 *   <li>POST /auth/logout</li>
 * </ul>
 */
public class AuthApiTest extends BaseApiTest {

    private static final String REGISTER_URL = "/auth/register";
    private static final String LOGIN_URL    = "/auth/login";
    private static final String LOGOUT_URL   = "/auth/logout";

    // ── /auth/register ────────────────────────────────────────────────────────

    @Test(description = "POST /auth/register – valid payload returns 200 with employee data",
          groups = {"auth", "smoke"})
    public void register_validPayload_returns200WithEmployeeData() {
        String body = """
            {
              "email"    : "newuser_%d@epam.com",
              "password" : "Password@123",
              "name"     : "New User",
              "role"     : "EMPLOYEE"
            }
            """.formatted(System.currentTimeMillis());

        given()
            .spec(givenNoAuth())
            .body(body)
        .when()
            .post(REGISTER_URL)
        .then()
            .statusCode(200)
            .body("email", containsString("@epam.com"))
            .body("name",  equalTo("New User"))
            .body("id",    notNullValue());
    }

    @Test(description = "POST /auth/register – duplicate email returns 4xx error",
          groups = {"auth"})
    public void register_duplicateEmail_returnsErrorStatus() {
        String email = "duplicate_%d@epam.com".formatted(System.currentTimeMillis());
        String body  = """
            {"email":"%s","password":"Password@123","name":"User","role":"EMPLOYEE"}
            """.formatted(email);

        // First registration – must succeed
        given().spec(givenNoAuth()).body(body).when().post(REGISTER_URL)
               .then().statusCode(200);

        // Second registration with same email – must fail
        given().spec(givenNoAuth()).body(body).when().post(REGISTER_URL)
               .then().statusCode(anyOf(is(400), is(409), is(422)));
    }

    // ── /auth/login ───────────────────────────────────────────────────────────

    @Test(description = "POST /auth/login – correct credentials return JWT and employee info",
          groups = {"auth", "smoke"})
    public void login_validCredentials_returnsJwtAndEmployeeInfo() {
        // Register a fresh user first
        String email = "logintest_%d@epam.com".formatted(System.currentTimeMillis());
        String registerBody = """
            {"email":"%s","password":"Password@123","name":"Login Test","role":"EMPLOYEE"}
            """.formatted(email);
        given().spec(givenNoAuth()).body(registerBody).when().post(REGISTER_URL)
               .then().statusCode(200);

        // Now login
        String loginBody = """
            {"email":"%s","password":"Password@123"}
            """.formatted(email);

        given()
            .spec(givenNoAuth())
            .body(loginBody)
        .when()
            .post(LOGIN_URL)
        .then()
            .statusCode(200)
            .body("jwt",        notNullValue())
            .body("jwt",        not(emptyString()))
            .body("email",      equalTo(email))
            .body("employeeId", greaterThan(0))
            .body("role",       notNullValue());
    }

    @Test(description = "POST /auth/login – unknown email returns error status",
          groups = {"auth"})
    public void login_unknownEmail_returnsErrorStatus() {
        String body = """
            {"email":"noexist_%d@epam.com","password":"wrong"}
            """.formatted(System.currentTimeMillis());

        given()
            .spec(givenNoAuth())
            .body(body)
        .when()
            .post(LOGIN_URL)
        .then()
            .statusCode(anyOf(is(400), is(401), is(422)));
    }

    @Test(description = "POST /auth/login – wrong password returns error status",
          groups = {"auth"})
    public void login_wrongPassword_returnsErrorStatus() {
        String email = "pwdtest_%d@epam.com".formatted(System.currentTimeMillis());
        String reg   = """
            {"email":"%s","password":"Correct@123","name":"Pwd Test","role":"EMPLOYEE"}
            """.formatted(email);
        given().spec(givenNoAuth()).body(reg).when().post(REGISTER_URL)
               .then().statusCode(200);

        String loginBody = """
            {"email":"%s","password":"WrongPassword!"}
            """.formatted(email);

        given()
            .spec(givenNoAuth())
            .body(loginBody)
        .when()
            .post(LOGIN_URL)
        .then()
            .statusCode(anyOf(is(400), is(401), is(422)));
    }

    // ── /auth/logout ──────────────────────────────────────────────────────────

    @Test(description = "POST /auth/logout – returns 200 OK (stateless, no body required)",
          groups = {"auth", "smoke"})
    public void logout_returns200() {
        given()
            .spec(givenEmployee())
        .when()
            .post(LOGOUT_URL)
        .then()
            .statusCode(200);
    }

    // ── Response-structure validation ─────────────────────────────────────────

    @Test(description = "POST /auth/login – JWT token in response is a valid 3-part structure",
          groups = {"auth"})
    public void login_responseJwt_hasValidJwtStructure() {
        String email = "structtest_%d@epam.com".formatted(System.currentTimeMillis());
        String reg   = """
            {"email":"%s","password":"Struct@123","name":"Struct User","role":"EMPLOYEE"}
            """.formatted(email);
        given().spec(givenNoAuth()).body(reg).when().post(REGISTER_URL)
               .then().statusCode(200);

        String jwt = given()
            .spec(givenNoAuth())
            .body("""
                {"email":"%s","password":"Struct@123"}
                """.formatted(email))
        .when()
            .post(LOGIN_URL)
        .then()
            .statusCode(200)
            .extract().path("jwt");

        assertJwtStructure(jwt);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void assertJwtStructure(String jwt) {
        if (jwt == null || jwt.split("\\.").length != 3) {
            throw new AssertionError("Expected a valid JWT (header.payload.signature) but got: " + jwt);
        }
    }
}
