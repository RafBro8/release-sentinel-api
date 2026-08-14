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
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "defects")
public class Defect {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_test_execution_id")
    private TestExecution linkedTestExecution;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 1200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DefectSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DefectPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DefectStatus status;

    @Column(name = "blocking_release", nullable = false)
    private boolean blockingRelease;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Defect() {
    }

    public Defect(
            Release release,
            TestExecution linkedTestExecution,
            String title,
            String description,
            DefectSeverity severity,
            DefectPriority priority,
            Boolean blockingRelease) {
        this.id = UUID.randomUUID();
        this.release = release;
        this.project = release.getProject();
        this.linkedTestExecution = linkedTestExecution;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.priority = priority;
        this.status = DefectStatus.OPEN;
        this.blockingRelease = blockingRelease != null ? blockingRelease : severity == DefectSeverity.CRITICAL;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void updateStatus(DefectStatus nextStatus) {
        if (status == DefectStatus.CLOSED) {
            throw new IllegalStateException("Closed defects cannot be reopened");
        }
        if (nextStatus == DefectStatus.CLOSED && status != DefectStatus.RESOLVED) {
            throw new IllegalStateException("Defect must be resolved before it can be closed");
        }

        this.status = nextStatus;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public Release getRelease() {
        return release;
    }

    public TestExecution getLinkedTestExecution() {
        return linkedTestExecution;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public DefectSeverity getSeverity() {
        return severity;
    }

    public DefectPriority getPriority() {
        return priority;
    }

    public DefectStatus getStatus() {
        return status;
    }

    public boolean isBlockingRelease() {
        return blockingRelease;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
