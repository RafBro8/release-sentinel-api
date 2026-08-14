package com.releasesentinel.service;

import com.releasesentinel.api.dto.CreateTestCaseRequest;
import com.releasesentinel.api.dto.CreateTestExecutionRequest;
import com.releasesentinel.api.dto.CreateTestRunRequest;
import com.releasesentinel.api.dto.TestCaseResponse;
import com.releasesentinel.api.dto.TestExecutionResponse;
import com.releasesentinel.api.dto.TestRunResponse;
import com.releasesentinel.domain.Environment;
import com.releasesentinel.domain.Project;
import com.releasesentinel.domain.Release;
import com.releasesentinel.domain.TestCase;
import com.releasesentinel.domain.TestExecution;
import com.releasesentinel.domain.TestRun;
import com.releasesentinel.repository.EnvironmentRepository;
import com.releasesentinel.repository.ReleaseRepository;
import com.releasesentinel.repository.TestCaseRepository;
import com.releasesentinel.repository.TestExecutionRepository;
import com.releasesentinel.repository.TestRunRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TestExecutionService {

    private final ReleaseTrackingService releaseTrackingService;
    private final ReleaseRepository releaseRepository;
    private final EnvironmentRepository environmentRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestRunRepository testRunRepository;
    private final TestExecutionRepository testExecutionRepository;

    public TestExecutionService(
            ReleaseTrackingService releaseTrackingService,
            ReleaseRepository releaseRepository,
            EnvironmentRepository environmentRepository,
            TestCaseRepository testCaseRepository,
            TestRunRepository testRunRepository,
            TestExecutionRepository testExecutionRepository) {
        this.releaseTrackingService = releaseTrackingService;
        this.releaseRepository = releaseRepository;
        this.environmentRepository = environmentRepository;
        this.testCaseRepository = testCaseRepository;
        this.testRunRepository = testRunRepository;
        this.testExecutionRepository = testExecutionRepository;
    }

    @Transactional
    public TestCaseResponse createTestCase(UUID projectId, CreateTestCaseRequest request) {
        Project project = releaseTrackingService.findProject(projectId);

        if (testCaseRepository.existsByProjectIdAndTitleIgnoreCase(projectId, request.title())) {
            throw new DuplicateResourceException("Test case title already exists for project: " + request.title());
        }

        TestCase testCase = new TestCase(
                project,
                request.title(),
                request.description(),
                request.priority(),
                request.type());

        return TestCaseResponse.from(testCaseRepository.save(testCase));
    }

    public List<TestCaseResponse> getProjectTestCases(UUID projectId) {
        releaseTrackingService.findProject(projectId);
        return testCaseRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(TestCaseResponse::from)
                .toList();
    }

    public TestCaseResponse getTestCase(UUID testCaseId) {
        return TestCaseResponse.from(findTestCase(testCaseId));
    }

    @Transactional
    public TestRunResponse createTestRun(UUID releaseId, CreateTestRunRequest request) {
        Release release = findRelease(releaseId);
        Environment environment = environmentRepository.findById(request.environmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Environment not found: " + request.environmentId()));

        UUID releaseProjectId = release.getProject().getId();
        UUID environmentProjectId = environment.getProject().getId();
        if (!releaseProjectId.equals(environmentProjectId)) {
            throw new DuplicateResourceException("Environment does not belong to the release project");
        }

        TestRun testRun = new TestRun(release, environment, request.name());
        return TestRunResponse.from(testRunRepository.save(testRun));
    }

    public List<TestRunResponse> getReleaseTestRuns(UUID releaseId) {
        findRelease(releaseId);
        return testRunRepository.findByReleaseIdOrderByCreatedAtDesc(releaseId).stream()
                .map(TestRunResponse::from)
                .toList();
    }

    public TestRunResponse getTestRun(UUID testRunId) {
        return TestRunResponse.from(findTestRun(testRunId));
    }

    @Transactional
    public TestExecutionResponse addExecution(UUID testRunId, CreateTestExecutionRequest request) {
        TestRun testRun = findTestRun(testRunId);
        TestCase testCase = findTestCase(request.testCaseId());

        UUID runProjectId = testRun.getRelease().getProject().getId();
        UUID testCaseProjectId = testCase.getProject().getId();
        if (!runProjectId.equals(testCaseProjectId)) {
            throw new DuplicateResourceException("Test case does not belong to the test run project");
        }

        if (testExecutionRepository.existsByTestRunIdAndTestCaseId(testRunId, request.testCaseId())) {
            throw new DuplicateResourceException("Test case already executed in this test run");
        }

        TestExecution execution = new TestExecution(testRun, testCase, request.result(), request.notes());
        return TestExecutionResponse.from(testExecutionRepository.save(execution));
    }

    public List<TestExecutionResponse> getTestRunExecutions(UUID testRunId) {
        findTestRun(testRunId);
        return testExecutionRepository.findByTestRunIdOrderByExecutedAtAsc(testRunId).stream()
                .map(TestExecutionResponse::from)
                .toList();
    }

    private Release findRelease(UUID releaseId) {
        return releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found: " + releaseId));
    }

    private TestCase findTestCase(UUID testCaseId) {
        return testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found: " + testCaseId));
    }

    private TestRun findTestRun(UUID testRunId) {
        return testRunRepository.findById(testRunId)
                .orElseThrow(() -> new ResourceNotFoundException("Test run not found: " + testRunId));
    }
}
