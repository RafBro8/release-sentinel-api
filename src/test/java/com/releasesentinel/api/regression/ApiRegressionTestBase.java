package com.releasesentinel.api.regression;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class ApiRegressionTestBase {

    @LocalServerPort
    private int port;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    protected Response post(String path, Object body) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);
    }

    protected Response get(String path) {
        return RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get(path);
    }

    protected String uniqueProjectKey() {
        return "RS" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    protected String uniqueVersion(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected String createProject(String key) {
        return post("/api/projects", Map.of(
                        "key", key,
                        "name", key + " Platform",
                        "description", "API regression test project"))
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    protected String createEnvironment(String projectId) {
        return post("/api/projects/" + projectId + "/environments", Map.of(
                        "name", "staging-" + UUID.randomUUID().toString().substring(0, 8),
                        "type", "STAGING",
                        "baseUrl", "https://staging.example.com"))
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    protected String createRelease(String projectId, String version) {
        return post("/api/projects/" + projectId + "/releases", Map.of(
                        "version", version,
                        "status", "IN_TEST"))
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    protected String createTestCase(String projectId, String title, String priority) {
        return post("/api/projects/" + projectId + "/test-cases", Map.of(
                        "title", title,
                        "description", "Created by Rest Assured regression test",
                        "priority", priority,
                        "type", "API"))
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    protected String createTestRun(String releaseId, String environmentId) {
        return post("/api/releases/" + releaseId + "/test-runs", Map.of(
                        "environmentId", environmentId,
                        "name", "API regression run " + UUID.randomUUID()))
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    protected String addExecution(String testRunId, String testCaseId, String result) {
        return post("/api/test-runs/" + testRunId + "/executions", Map.of(
                        "testCaseId", testCaseId,
                        "result", result,
                        "notes", "Recorded by Rest Assured"))
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
