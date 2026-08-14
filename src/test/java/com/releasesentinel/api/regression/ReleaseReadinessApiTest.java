package com.releasesentinel.api.regression;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReleaseReadinessApiTest extends ApiRegressionTestBase {

    @Test
    void releaseQualitySummaryIsReadyWhenAllSignalsAreClean() {
        String projectId = createProject(uniqueProjectKey());
        String environmentId = createEnvironment(projectId);
        String releaseId = createRelease(projectId, uniqueVersion("1.0.0"));
        String testRunId = createTestRun(releaseId, environmentId);

        String loginTestId = createTestCase(projectId, "Login API smoke " + UUID.randomUUID(), "HIGH");
        String checkoutTestId = createTestCase(projectId, "Checkout API smoke " + UUID.randomUUID(), "CRITICAL");

        addExecution(testRunId, loginTestId, "PASSED");
        addExecution(testRunId, checkoutTestId, "PASSED");

        get("/api/releases/" + releaseId + "/quality-summary")
                .then()
                .statusCode(200)
                .body("releaseId", equalTo(releaseId))
                .body("status", equalTo("READY"))
                .body("totalTests", equalTo(2))
                .body("passed", equalTo(2))
                .body("failed", equalTo(0))
                .body("passRate", equalTo(100.0F))
                .body("riskReasons.size()", equalTo(0));
    }

    @Test
    void failedTestExecutionMakesReleaseAtRisk() {
        String projectId = createProject(uniqueProjectKey());
        String environmentId = createEnvironment(projectId);
        String releaseId = createRelease(projectId, uniqueVersion("1.1.0"));
        String testRunId = createTestRun(releaseId, environmentId);

        String passedTestId = createTestCase(projectId, "Account API accepts valid update " + UUID.randomUUID(), "HIGH");
        String failedTestId = createTestCase(projectId, "Payment API declines expired card " + UUID.randomUUID(), "CRITICAL");

        addExecution(testRunId, passedTestId, "PASSED");
        addExecution(testRunId, failedTestId, "FAILED");

        get("/api/releases/" + releaseId + "/quality-summary")
                .then()
                .statusCode(200)
                .body("status", equalTo("AT_RISK"))
                .body("totalTests", equalTo(2))
                .body("failed", equalTo(1))
                .body("passRate", equalTo(50.0F))
                .body("riskReasons", hasItem("Pass rate is below 90%"))
                .body("riskReasons", hasItem("Release has failed test executions"));
    }

    @Test
    void criticalOpenDefectBlocksRelease() {
        String projectId = createProject(uniqueProjectKey());
        String environmentId = createEnvironment(projectId);
        String releaseId = createRelease(projectId, uniqueVersion("1.2.0"));
        String testRunId = createTestRun(releaseId, environmentId);
        String testCaseId = createTestCase(projectId, "Checkout confirmation API " + UUID.randomUUID(), "CRITICAL");
        String executionId = addExecution(testRunId, testCaseId, "FAILED");

        post("/api/releases/" + releaseId + "/defects", Map.of(
                        "title", "Checkout confirmation returns 500",
                        "description", "Critical checkout regression found by automated API test",
                        "severity", "CRITICAL",
                        "priority", "URGENT",
                        "linkedTestExecutionId", executionId))
                .then()
                .statusCode(201)
                .body("blockingRelease", equalTo(true))
                .body("status", equalTo("OPEN"));

        get("/api/releases/" + releaseId + "/quality-summary")
                .then()
                .statusCode(200)
                .body("status", equalTo("BLOCKED"))
                .body("openCriticalDefects", equalTo(1))
                .body("blockingDefects", equalTo(1))
                .body("riskReasons", hasItem("Release has open critical defects"))
                .body("recommendation", containsString("Do not release"));
    }

    @Test
    void invalidProjectPayloadReturnsValidationErrorContract() {
        post("/api/projects", Map.of(
                        "key", "bad-key",
                        "name", "Invalid Project"))
                .then()
                .statusCode(400)
                .body("message", equalTo("Request validation failed"))
                .body("fieldErrors.key", containsString("must start with a letter"));
    }

    @Test
    void missingReleaseQualitySummaryReturnsNotFoundContract() {
        String missingReleaseId = UUID.randomUUID().toString();

        get("/api/releases/" + missingReleaseId + "/quality-summary")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("message", equalTo("Release not found: " + missingReleaseId));
    }

    @Test
    void duplicateExecutionInSameTestRunReturnsConflict() {
        String projectId = createProject(uniqueProjectKey());
        String environmentId = createEnvironment(projectId);
        String releaseId = createRelease(projectId, uniqueVersion("1.3.0"));
        String testRunId = createTestRun(releaseId, environmentId);
        String testCaseId = createTestCase(projectId, "Duplicate execution guard " + UUID.randomUUID(), "HIGH");

        addExecution(testRunId, testCaseId, "PASSED");

        post("/api/test-runs/" + testRunId + "/executions", Map.of(
                        "testCaseId", testCaseId,
                        "result", "FAILED",
                        "notes", "Duplicate result should be rejected"))
                .then()
                .statusCode(409)
                .body("message", equalTo("Test case already executed in this test run"));
    }
}
