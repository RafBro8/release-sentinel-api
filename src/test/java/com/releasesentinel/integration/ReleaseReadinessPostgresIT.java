package com.releasesentinel.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReleaseReadinessPostgresIT extends PostgresIntegrationTestBase {

    @Test
    void validatesReleaseReadinessWorkflowAgainstPostgres() {
        String projectId = createProject(uniqueProjectKey());
        String environmentId = createEnvironment(projectId);
        String releaseId = createRelease(projectId, uniqueVersion("2.0.0"));

        String testCaseId = post("/api/projects/" + projectId + "/test-cases", Map.of(
                        "title", "Checkout API confirms order " + UUID.randomUUID(),
                        "description", "PostgreSQL-backed release readiness workflow",
                        "priority", "CRITICAL",
                        "type", "API"))
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String testRunId = post("/api/releases/" + releaseId + "/test-runs", Map.of(
                        "environmentId", environmentId,
                        "name", "PostgreSQL regression run " + UUID.randomUUID()))
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String executionId = post("/api/test-runs/" + testRunId + "/executions", Map.of(
                        "testCaseId", testCaseId,
                        "result", "FAILED",
                        "notes", "Failure persisted through PostgreSQL Testcontainers"))
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        post("/api/releases/" + releaseId + "/defects", Map.of(
                        "title", "Checkout confirmation fails in staging",
                        "description", "Critical defect linked to a failed API execution",
                        "severity", "CRITICAL",
                        "priority", "URGENT",
                        "linkedTestExecutionId", executionId))
                .then()
                .statusCode(201)
                .body("blockingRelease", equalTo(true));

        get("/api/releases/" + releaseId + "/quality-summary")
                .then()
                .statusCode(200)
                .body("status", equalTo("BLOCKED"))
                .body("totalTests", equalTo(1))
                .body("failed", equalTo(1))
                .body("openCriticalDefects", equalTo(1))
                .body("blockingDefects", equalTo(1))
                .body("riskReasons", hasItem("Release has open critical defects"));
    }
}
