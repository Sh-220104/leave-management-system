package com.epam.elms.utils;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Centralised RestAssured configuration shared by all API integration tests.
 */
public final class RestAssuredConfig {

    private RestAssuredConfig() {}

    /**
     * Builds a base {@link RequestSpecification} pointing at the given host/port.
     *
     * @param baseUrl full base URL, e.g. "http://localhost:8080"
     * @return configured RequestSpecification
     */
    public static RequestSpecification buildBaseSpec(String baseUrl) {
        return new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Builds an authenticated spec with the provided JWT bearer token.
     */
    public static RequestSpecification buildAuthSpec(String baseUrl, String jwtToken) {
        return new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + jwtToken)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /** Configure global RestAssured defaults. */
    public static void configure(String host, int port) {
        RestAssured.baseURI = host;
        RestAssured.port    = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
