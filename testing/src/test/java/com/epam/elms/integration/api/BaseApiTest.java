package com.epam.elms.integration.api;

import com.epam.elms.utils.JwtTestHelper;
import com.epam.elms.utils.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeSuite;

/**
 * Base class for all REST-Assured integration tests.
 *
 * <p>Reads the target URL from the system property {@code app.base.url}
 * (default: {@code http://localhost:8080}).
 *
 * <p>These tests are designed to run against a LIVE application instance
 * (i.e., the Spring Boot app must already be started). In a CI pipeline
 * the app would be started via {@code spring-boot:run} or Docker before
 * the Surefire run.
 */
public abstract class BaseApiTest {

    protected static final String BASE_URL =
            System.getProperty("app.base.url", "http://localhost:8080");

    /** Pre-built JWT tokens used across test classes */
    protected static final String EMPLOYEE_EMAIL = "testemployee@epam.com";
    protected static final String ADMIN_EMAIL    = "testadmin@epam.com";

    protected static String employeeToken;
    protected static String adminToken;

    @BeforeSuite(alwaysRun = true)
    public void initTokens() {
        employeeToken = JwtTestHelper.employeeToken(EMPLOYEE_EMAIL);
        adminToken    = JwtTestHelper.adminToken(ADMIN_EMAIL);
        RestAssuredConfig.configure(BASE_URL, -1); // port embedded in BASE_URL
    }

    protected RequestSpecification givenEmployee() {
        return RestAssuredConfig.buildAuthSpec(BASE_URL, employeeToken);
    }

    protected RequestSpecification givenAdmin() {
        return RestAssuredConfig.buildAuthSpec(BASE_URL, adminToken);
    }

    protected RequestSpecification givenNoAuth() {
        return RestAssuredConfig.buildBaseSpec(BASE_URL);
    }
}
