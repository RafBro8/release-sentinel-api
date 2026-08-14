package com.releasesentinel.api;

import com.releasesentinel.api.dto.CreateTestRunRequest;
import com.releasesentinel.api.dto.TestRunResponse;
import com.releasesentinel.service.TestExecutionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRunController {

    private final TestExecutionService testExecutionService;

    public TestRunController(TestExecutionService testExecutionService) {
        this.testExecutionService = testExecutionService;
    }

    @PostMapping("/api/releases/{releaseId}/test-runs")
    @ResponseStatus(HttpStatus.CREATED)
    public TestRunResponse createTestRun(
            @PathVariable UUID releaseId,
            @Valid @RequestBody CreateTestRunRequest request) {
        return testExecutionService.createTestRun(releaseId, request);
    }

    @GetMapping("/api/releases/{releaseId}/test-runs")
    public List<TestRunResponse> getReleaseTestRuns(@PathVariable UUID releaseId) {
        return testExecutionService.getReleaseTestRuns(releaseId);
    }

    @GetMapping("/api/test-runs/{testRunId}")
    public TestRunResponse getTestRun(@PathVariable UUID testRunId) {
        return testExecutionService.getTestRun(testRunId);
    }
}
