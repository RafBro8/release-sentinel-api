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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "releases",
        uniqueConstraints = @UniqueConstraint(name = "uk_releases_project_version", columnNames = {"project_id", "version"}))
public class Release {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 40)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReleaseStatus status;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Release() {
    }

    public Release(Project project, String version, ReleaseStatus status, LocalDate targetDate) {
        this.id = UUID.randomUUID();
        this.project = project;
        this.version = version;
        this.status = status;
        this.targetDate = targetDate;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getVersion() {
        return version;
    }

    public ReleaseStatus getStatus() {
        return status;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
