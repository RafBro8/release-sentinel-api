package com.releasesentinel.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class PostgresIntegrationTestBase {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("release_sentinel_it")
            .withUsername("release_sentinel")
            .withPassword("release_sentinel");

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

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
        return "PG" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    protected String uniqueVersion(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected String createProject(String key) {
        return post("/api/projects", Map.of(
                        "key", key,
                        "name", key + " Platform",
                        "description", "PostgreSQL integration test project"))
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
}
