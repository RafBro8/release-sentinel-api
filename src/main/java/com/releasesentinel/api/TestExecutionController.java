package com.releasesentinel.api;

import com.releasesentinel.api.dto.CreateTestExecutionRequest;
import com.releasesentinel.api.dto.TestExecutionResponse;
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
public class TestExecutionController {

    private final TestExecutionService testExecutionService;

    public TestExecutionController(TestExecutionService testExecutionService) {
        this.testExecutionService = testExecutionService;
    }

    @PostMapping("/api/test-runs/{testRunId}/executions")
    @ResponseStatus(HttpStatus.CREATED)
    public TestExecutionResponse addExecution(
            @PathVariable UUID testRunId,
            @Valid @RequestBody CreateTestExecutionRequest request) {
        return testExecutionService.addExecution(testRunId, request);
    }

    @GetMapping("/api/test-runs/{testRunId}/executions")
    public List<TestExecutionResponse> getTestRunExecutions(@PathVariable UUID testRunId) {
        return testExecutionService.getTestRunExecutions(testRunId);
    }
}
