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
        name = "environments",
        uniqueConstraints = @UniqueConstraint(name = "uk_environments_project_name", columnNames = {"project_id", "name"}))
public class Environment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EnvironmentType type;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Environment() {
    }

    public Environment(Project project, String name, EnvironmentType type, String baseUrl) {
        this.id = UUID.randomUUID();
        this.project = project;
        this.name = name;
        this.type = type;
        this.baseUrl = baseUrl;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getName() {
        return name;
    }

    public EnvironmentType getType() {
        return type;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
