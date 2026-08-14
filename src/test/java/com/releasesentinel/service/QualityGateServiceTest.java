package com.releasesentinel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.releasesentinel.api.dto.ReleaseQualitySummaryResponse;
import com.releasesentinel.domain.Defect;
import com.releasesentinel.domain.DefectPriority;
import com.releasesentinel.domain.DefectSeverity;
import com.releasesentinel.domain.DefectStatus;
import com.releasesentinel.domain.Environment;
import com.releasesentinel.domain.EnvironmentType;
import com.releasesentinel.domain.Project;
import com.releasesentinel.domain.QualityStatus;
import com.releasesentinel.domain.Release;
import com.releasesentinel.domain.ReleaseStatus;
import com.releasesentinel.domain.TestCase;
import com.releasesentinel.domain.TestExecution;
import com.releasesentinel.domain.TestPriority;
import com.releasesentinel.domain.TestResult;
import com.releasesentinel.domain.TestRun;
import com.releasesentinel.domain.TestType;
import com.releasesentinel.repository.DefectRepository;
import com.releasesentinel.repository.ReleaseRepository;
import com.releasesentinel.repository.TestExecutionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QualityGateServiceTest {

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private TestExecutionRepository testExecutionRepository;

    @Mock
    private DefectRepository defectRepository;

    @InjectMocks
    private QualityGateService qualityGateService;

    @Test
    void returnsAtRiskWhenNoTestsExist() {
        UUID releaseId = UUID.randomUUID();
        Release release = release();
        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(testExecutionRepository.findByTestRunReleaseIdOrderByExecutedAtAsc(releaseId)).thenReturn(List.of());
        when(defectRepository.findByReleaseIdOrderByCreatedAtDesc(releaseId)).thenReturn(List.of());

        ReleaseQualitySummaryResponse response = qualityGateService.getReleaseQualitySummary(releaseId);

        assertThat(response.status()).isEqualTo(QualityStatus.AT_RISK);
        assertThat(response.totalTests()).isZero();
        assertThat(response.riskReasons()).contains("No test executions have been recorded for this release");
    }

    @Test
    void returnsReadyWhenAllQualitySignalsAreClean() {
        UUID releaseId = UUID.randomUUID();
        Release release = release();
        TestRun testRun = testRun(release);
        TestCase testCase = testCase(release.getProject());
        List<TestExecution> executions = List.of(
                new TestExecution(testRun, testCase, TestResult.PASSED, null),
                new TestExecution(testRun, testCase, TestResult.PASSED, null),
                new TestExecution(testRun, testCase, TestResult.PASSED, null));

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(testExecutionRepository.findByTestRunReleaseIdOrderByExecutedAtAsc(releaseId)).thenReturn(executions);
        when(defectRepository.findByReleaseIdOrderByCreatedAtDesc(releaseId)).thenReturn(List.of());

        ReleaseQualitySummaryResponse response = qualityGateService.getReleaseQualitySummary(releaseId);

        assertThat(response.status()).isEqualTo(QualityStatus.READY);
        assertThat(response.passRate()).isEqualTo(100.0);
        assertThat(response.riskReasons()).isEmpty();
    }

    @Test
    void returnsAtRiskWhenPassRateIsBelowThreshold() {
        UUID releaseId = UUID.randomUUID();
        Release release = release();
        TestRun testRun = testRun(release);
        TestCase testCase = testCase(release.getProject());
        List<TestExecution> executions = List.of(
                new TestExecution(testRun, testCase, TestResult.PASSED, null),
                new TestExecution(testRun, testCase, TestResult.PASSED, null),
                new TestExecution(testRun, testCase, TestResult.FAILED, null));

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(testExecutionRepository.findByTestRunReleaseIdOrderByExecutedAtAsc(releaseId)).thenReturn(executions);
        when(defectRepository.findByReleaseIdOrderByCreatedAtDesc(releaseId)).thenReturn(List.of());

        ReleaseQualitySummaryResponse response = qualityGateService.getReleaseQualitySummary(releaseId);

        assertThat(response.status()).isEqualTo(QualityStatus.AT_RISK);
        assertThat(response.failed()).isEqualTo(1);
        assertThat(response.passRate()).isEqualTo(66.7);
        assertThat(response.riskReasons()).contains("Pass rate is below 90%", "Release has failed test executions");
    }

    @Test
    void returnsBlockedWhenBlockedTestExecutionExists() {
        UUID releaseId = UUID.randomUUID();
        Release release = release();
        TestRun testRun = testRun(release);
        TestCase testCase = testCase(release.getProject());

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(testExecutionRepository.findByTestRunReleaseIdOrderByExecutedAtAsc(releaseId))
                .thenReturn(List.of(new TestExecution(testRun, testCase, TestResult.BLOCKED, null)));
        when(defectRepository.findByReleaseIdOrderByCreatedAtDesc(releaseId)).thenReturn(List.of());

        ReleaseQualitySummaryResponse response = qualityGateService.getReleaseQualitySummary(releaseId);

        assertThat(response.status()).isEqualTo(QualityStatus.BLOCKED);
        assertThat(response.blocked()).isEqualTo(1);
        assertThat(response.riskReasons()).contains("Release has blocked test executions");
    }

    @Test
    void returnsBlockedWhenOpenCriticalDefectExists() {
        UUID releaseId = UUID.randomUUID();
        Release release = release();
        Defect defect = new Defect(
                release,
                null,
                "Checkout unavailable",
                null,
                DefectSeverity.CRITICAL,
                DefectPriority.URGENT,
                null);

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(testExecutionRepository.findByTestRunReleaseIdOrderByExecutedAtAsc(releaseId)).thenReturn(passingExecutions(release));
        when(defectRepository.findByReleaseIdOrderByCreatedAtDesc(releaseId)).thenReturn(List.of(defect));

        ReleaseQualitySummaryResponse response = qualityGateService.getReleaseQualitySummary(releaseId);

        assertThat(response.status()).isEqualTo(QualityStatus.BLOCKED);
        assertThat(response.openCriticalDefects()).isEqualTo(1);
        assertThat(response.blockingDefects()).isEqualTo(1);
    }

    @Test
    void ignoresResolvedAndClosedDefectsForBlockingLogic() {
        UUID releaseId = UUID.randomUUID();
        Release release = release();
        Defect resolved = new Defect(
                release,
                null,
                "Resolved checkout defect",
                null,
                DefectSeverity.CRITICAL,
                DefectPriority.URGENT,
                null);
        resolved.updateStatus(DefectStatus.RESOLVED);
        Defect closed = new Defect(
                release,
                null,
                "Closed invoice defect",
                null,
                DefectSeverity.CRITICAL,
                DefectPriority.URGENT,
                null);
        closed.updateStatus(DefectStatus.RESOLVED);
        closed.updateStatus(DefectStatus.CLOSED);

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(testExecutionRepository.findByTestRunReleaseIdOrderByExecutedAtAsc(releaseId)).thenReturn(passingExecutions(release));
        when(defectRepository.findByReleaseIdOrderByCreatedAtDesc(releaseId)).thenReturn(List.of(resolved, closed));

        ReleaseQualitySummaryResponse response = qualityGateService.getReleaseQualitySummary(releaseId);

        assertThat(response.status()).isEqualTo(QualityStatus.READY);
        assertThat(response.openDefects()).isZero();
        assertThat(response.blockingDefects()).isZero();
    }

    @Test
    void throwsNotFoundWhenReleaseDoesNotExist() {
        UUID releaseId = UUID.randomUUID();
        when(releaseRepository.findById(releaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qualityGateService.getReleaseQualitySummary(releaseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Release not found: " + releaseId);
    }

    private Release release() {
        Project project = new Project("SHOP", "Shopping Platform", null);
        return new Release(project, "1.0.0", ReleaseStatus.IN_TEST, null);
    }

    private TestRun testRun(Release release) {
        Environment environment = new Environment(release.getProject(), "staging", EnvironmentType.STAGING, null);
        return new TestRun(release, environment, "Regression run");
    }

    private TestCase testCase(Project project) {
        return new TestCase(project, "Checkout API returns confirmation", null, TestPriority.CRITICAL, TestType.API);
    }

    private List<TestExecution> passingExecutions(Release release) {
        TestRun testRun = testRun(release);
        TestCase testCase = testCase(release.getProject());
        return List.of(
                new TestExecution(testRun, testCase, TestResult.PASSED, null),
                new TestExecution(testRun, testCase, TestResult.PASSED, null));
    }
}
