package com.releasesentinel.api;

import com.releasesentinel.api.dto.CreateTestCaseRequest;
import com.releasesentinel.api.dto.TestCaseResponse;
import com.releasesentinel.service.TestExecutionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestCaseController {

    private final TestExecutionService testExecutionService;

    public TestCaseController(TestExecutionService testExecutionService) {
        this.testExecutionService = testExecutionService;
    }

    @PostMapping("/api/projects/{projectId}/test-cases")
    @ResponseStatus(HttpStatus.CREATED)
    public TestCaseResponse createTestCase(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTestCaseRequest request) {
        return testExecutionService.createTestCase(projectId, request);
    }

    @GetMapping("/api/projects/{projectId}/test-cases")
    public List<TestCaseResponse> getProjectTestCases(@PathVariable UUID projectId) {
        return testExecutionService.getProjectTestCases(projectId);
    }

    @GetMapping("/api/test-cases/{testCaseId}")
    public TestCaseResponse getTestCase(@PathVariable UUID testCaseId) {
        return testExecutionService.getTestCase(testCaseId);
    }
}
