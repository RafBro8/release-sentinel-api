package com.releasesentinel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.releasesentinel.api.dto.CreateTestCaseRequest;
import com.releasesentinel.api.dto.CreateTestExecutionRequest;
import com.releasesentinel.api.dto.CreateTestRunRequest;
import com.releasesentinel.api.dto.TestCaseResponse;
import com.releasesentinel.api.dto.TestExecutionResponse;
import com.releasesentinel.api.dto.TestRunResponse;
import com.releasesentinel.domain.Environment;
import com.releasesentinel.domain.EnvironmentType;
import com.releasesentinel.domain.Project;
import com.releasesentinel.domain.Release;
import com.releasesentinel.domain.ReleaseStatus;
import com.releasesentinel.domain.TestCase;
import com.releasesentinel.domain.TestExecution;
import com.releasesentinel.domain.TestPriority;
import com.releasesentinel.domain.TestResult;
import com.releasesentinel.domain.TestRun;
import com.releasesentinel.domain.TestRunStatus;
import com.releasesentinel.domain.TestType;
import com.releasesentinel.repository.EnvironmentRepository;
import com.releasesentinel.repository.ReleaseRepository;
import com.releasesentinel.repository.TestCaseRepository;
import com.releasesentinel.repository.TestExecutionRepository;
import com.releasesentinel.repository.TestRunRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestExecutionServiceTest {

    @Mock
    private ReleaseTrackingService releaseTrackingService;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private TestRunRepository testRunRepository;

    @Mock
    private TestExecutionRepository testExecutionRepository;

    @InjectMocks
    private TestExecutionService testExecutionService;

    @Test
    void createTestCaseSavesActiveTestCaseForProject() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        CreateTestCaseRequest request = new CreateTestCaseRequest(
                "Checkout API returns confirmation",
                "Validate successful checkout response",
                TestPriority.CRITICAL,
                TestType.API);

        when(releaseTrackingService.findProject(projectId)).thenReturn(project);
        when(testCaseRepository.existsByProjectIdAndTitleIgnoreCase(projectId, request.title())).thenReturn(false);
        when(testCaseRepository.save(any(TestCase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TestCaseResponse response = testExecutionService.createTestCase(projectId, request);

        assertThat(response.title()).isEqualTo("Checkout API returns confirmation");
        assertThat(response.priority()).isEqualTo(TestPriority.CRITICAL);
        assertThat(response.active()).isTrue();
        verify(testCaseRepository).save(any(TestCase.class));
    }

    @Test
    void createTestCaseRejectsDuplicateTitleWithinProject() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        CreateTestCaseRequest request = new CreateTestCaseRequest(
                "Checkout API returns confirmation",
                null,
                TestPriority.CRITICAL,
                TestType.API);

        when(releaseTrackingService.findProject(projectId)).thenReturn(project);
        when(testCaseRepository.existsByProjectIdAndTitleIgnoreCase(projectId, request.title())).thenReturn(true);

        assertThatThrownBy(() -> testExecutionService.createTestCase(projectId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Test case title already exists for project: Checkout API returns confirmation");
    }

    @Test
    void createTestRunRejectsEnvironmentFromDifferentProject() {
        UUID releaseId = UUID.randomUUID();
        UUID environmentId = UUID.randomUUID();
        Project releaseProject = new Project("SHOP", "Shopping Platform", null);
        Project environmentProject = new Project("BILLING", "Billing Platform", null);
        Release release = new Release(releaseProject, "1.0.0", ReleaseStatus.IN_TEST, null);
        Environment environment = new Environment(environmentProject, "staging", EnvironmentType.STAGING, null);
        CreateTestRunRequest request = new CreateTestRunRequest(environmentId, "Regression run");

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(environmentRepository.findById(environmentId)).thenReturn(Optional.of(environment));

        assertThatThrownBy(() -> testExecutionService.createTestRun(releaseId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Environment does not belong to the release project");
    }

    @Test
    void createTestRunDefaultsToInProgress() {
        UUID releaseId = UUID.randomUUID();
        UUID environmentId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        Release release = new Release(project, "1.0.0", ReleaseStatus.IN_TEST, null);
        Environment environment = new Environment(project, "staging", EnvironmentType.STAGING, null);
        CreateTestRunRequest request = new CreateTestRunRequest(environmentId, "Regression run");

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(environmentRepository.findById(environmentId)).thenReturn(Optional.of(environment));
        when(testRunRepository.save(any(TestRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TestRunResponse response = testExecutionService.createTestRun(releaseId, request);

        assertThat(response.name()).isEqualTo("Regression run");
        assertThat(response.status()).isEqualTo(TestRunStatus.IN_PROGRESS);
    }

    @Test
    void addExecutionRejectsDuplicateTestCaseInSameRun() {
        UUID testRunId = UUID.randomUUID();
        UUID testCaseId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        Release release = new Release(project, "1.0.0", ReleaseStatus.IN_TEST, null);
        Environment environment = new Environment(project, "staging", EnvironmentType.STAGING, null);
        TestRun testRun = new TestRun(release, environment, "Regression run");
        TestCase testCase = new TestCase(project, "Checkout API returns confirmation", null, TestPriority.CRITICAL, TestType.API);
        CreateTestExecutionRequest request = new CreateTestExecutionRequest(testCaseId, TestResult.PASSED, null);

        when(testRunRepository.findById(testRunId)).thenReturn(Optional.of(testRun));
        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));
        when(testExecutionRepository.existsByTestRunIdAndTestCaseId(testRunId, testCaseId)).thenReturn(true);

        assertThatThrownBy(() -> testExecutionService.addExecution(testRunId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Test case already executed in this test run");
    }

    @Test
    void addExecutionRecordsResultForSameProjectTestCase() {
        UUID testRunId = UUID.randomUUID();
        UUID testCaseId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        Release release = new Release(project, "1.0.0", ReleaseStatus.IN_TEST, null);
        Environment environment = new Environment(project, "staging", EnvironmentType.STAGING, null);
        TestRun testRun = new TestRun(release, environment, "Regression run");
        TestCase testCase = new TestCase(project, "Checkout API returns confirmation", null, TestPriority.CRITICAL, TestType.API);
        CreateTestExecutionRequest request = new CreateTestExecutionRequest(testCaseId, TestResult.PASSED, "All assertions passed");

        when(testRunRepository.findById(testRunId)).thenReturn(Optional.of(testRun));
        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));
        when(testExecutionRepository.existsByTestRunIdAndTestCaseId(testRunId, testCaseId)).thenReturn(false);
        when(testExecutionRepository.save(any(TestExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TestExecutionResponse response = testExecutionService.addExecution(testRunId, request);

        assertThat(response.result()).isEqualTo(TestResult.PASSED);
        assertThat(response.notes()).isEqualTo("All assertions passed");
    }
}
