package com.releasesentinel.service;

import com.releasesentinel.api.dto.ReleaseQualitySummaryResponse;
import com.releasesentinel.domain.Defect;
import com.releasesentinel.domain.DefectSeverity;
import com.releasesentinel.domain.DefectStatus;
import com.releasesentinel.domain.QualityStatus;
import com.releasesentinel.domain.Release;
import com.releasesentinel.domain.TestExecution;
import com.releasesentinel.domain.TestResult;
import com.releasesentinel.repository.DefectRepository;
import com.releasesentinel.repository.ReleaseRepository;
import com.releasesentinel.repository.TestExecutionRepository;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QualityGateService {

    private static final double REQUIRED_PASS_RATE = 90.0;
    private static final EnumSet<DefectStatus> OPEN_DEFECT_STATUSES = EnumSet.of(
            DefectStatus.OPEN,
            DefectStatus.IN_PROGRESS,
            DefectStatus.DEFERRED);

    private final ReleaseRepository releaseRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final DefectRepository defectRepository;

    public QualityGateService(
            ReleaseRepository releaseRepository,
            TestExecutionRepository testExecutionRepository,
            DefectRepository defectRepository) {
        this.releaseRepository = releaseRepository;
        this.testExecutionRepository = testExecutionRepository;
        this.defectRepository = defectRepository;
    }

    public ReleaseQualitySummaryResponse getReleaseQualitySummary(UUID releaseId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found: " + releaseId));

        List<TestExecution> executions = testExecutionRepository.findByTestRunReleaseIdOrderByExecutedAtAsc(releaseId);
        List<Defect> defects = defectRepository.findByReleaseIdOrderByCreatedAtDesc(releaseId);

        int totalTests = executions.size();
        int passed = countResults(executions, TestResult.PASSED);
        int failed = countResults(executions, TestResult.FAILED);
        int skipped = countResults(executions, TestResult.SKIPPED);
        int blocked = countResults(executions, TestResult.BLOCKED);
        double passRate = calculatePassRate(passed, totalTests);

        List<Defect> openDefects = defects.stream()
                .filter(this::isOpenDefect)
                .toList();
        int openCriticalDefects = (int) openDefects.stream()
                .filter(defect -> defect.getSeverity() == DefectSeverity.CRITICAL)
                .count();
        int openHighDefects = (int) openDefects.stream()
                .filter(defect -> defect.getSeverity() == DefectSeverity.HIGH)
                .count();
        int blockingDefects = (int) openDefects.stream()
                .filter(Defect::isBlockingRelease)
                .count();

        List<String> riskReasons = buildRiskReasons(
                totalTests,
                failed,
                blocked,
                passRate,
                openCriticalDefects,
                openHighDefects,
                blockingDefects);
        QualityStatus status = determineStatus(
                totalTests,
                failed,
                blocked,
                passRate,
                openCriticalDefects,
                openHighDefects,
                blockingDefects);

        return new ReleaseQualitySummaryResponse(
                release.getId(),
                release.getProject().getId(),
                release.getVersion(),
                status,
                totalTests,
                passed,
                failed,
                skipped,
                blocked,
                passRate,
                openDefects.size(),
                openCriticalDefects,
                openHighDefects,
                blockingDefects,
                buildRecommendation(status),
                riskReasons);
    }

    private int countResults(List<TestExecution> executions, TestResult result) {
        return (int) executions.stream()
                .filter(execution -> execution.getResult() == result)
                .count();
    }

    private double calculatePassRate(int passed, int totalTests) {
        if (totalTests == 0) {
            return 0.0;
        }

        return Math.round(((double) passed / totalTests) * 1000.0) / 10.0;
    }

    private boolean isOpenDefect(Defect defect) {
        return OPEN_DEFECT_STATUSES.contains(defect.getStatus());
    }

    private List<String> buildRiskReasons(
            int totalTests,
            int failed,
            int blocked,
            double passRate,
            int openCriticalDefects,
            int openHighDefects,
            int blockingDefects) {
        List<String> reasons = new ArrayList<>();

        if (totalTests == 0) {
            reasons.add("No test executions have been recorded for this release");
        }
        if (passRate < REQUIRED_PASS_RATE && totalTests > 0) {
            reasons.add("Pass rate is below 90%");
        }
        if (failed > 0) {
            reasons.add("Release has failed test executions");
        }
        if (blocked > 0) {
            reasons.add("Release has blocked test executions");
        }
        if (blockingDefects > 0) {
            reasons.add("Release has open blocking defects");
        }
        if (openCriticalDefects > 0) {
            reasons.add("Release has open critical defects");
        }
        if (openHighDefects > 0) {
            reasons.add("Release has open high severity defects");
        }

        return reasons;
    }

    private QualityStatus determineStatus(
            int totalTests,
            int failed,
            int blocked,
            double passRate,
            int openCriticalDefects,
            int openHighDefects,
            int blockingDefects) {
        if (blocked > 0 || blockingDefects > 0 || openCriticalDefects > 0) {
            return QualityStatus.BLOCKED;
        }

        if (totalTests == 0 || failed > 0 || passRate < REQUIRED_PASS_RATE || openHighDefects > 0) {
            return QualityStatus.AT_RISK;
        }

        return QualityStatus.READY;
    }

    private String buildRecommendation(QualityStatus status) {
        return switch (status) {
            case READY -> "Release is ready to ship based on current test and defect signals.";
            case AT_RISK -> "Review quality risks before release and confirm the team accepts the remaining risk.";
            case BLOCKED -> "Do not release until blocking quality risks are resolved.";
        };
    }
}
