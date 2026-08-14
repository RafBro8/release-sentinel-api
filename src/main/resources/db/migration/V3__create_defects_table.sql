CREATE TABLE defects (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    release_id UUID NOT NULL REFERENCES releases(id),
    linked_test_execution_id UUID REFERENCES test_executions(id),
    title VARCHAR(180) NOT NULL,
    description VARCHAR(1200),
    severity VARCHAR(30) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    blocking_release BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_defects_project_id ON defects(project_id);
CREATE INDEX idx_defects_release_id ON defects(release_id);
CREATE INDEX idx_defects_status ON defects(status);
CREATE INDEX idx_defects_severity ON defects(severity);
