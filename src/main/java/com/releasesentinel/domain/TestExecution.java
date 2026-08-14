package com.releasesentinel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "test_executions",
        uniqueConstraints = @UniqueConstraint(name = "uk_test_executions_run_case", columnNames = {"test_run_id", "test_case_id"}))
public class TestExecution {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TestResult result;

    @Column(length = 1000)
    private String notes;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    protected TestExecution() {
    }

    public TestExecution(TestRun testRun, TestCase testCase, TestResult result, String notes) {
        this.id = UUID.randomUUID();
        this.testRun = testRun;
        this.testCase = testCase;
        this.result = result;
        this.notes = notes;
        this.executedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public TestRun getTestRun() {
        return testRun;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public TestResult getResult() {
        return result;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
