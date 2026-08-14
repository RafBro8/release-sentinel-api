CREATE TABLE test_cases (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    title VARCHAR(160) NOT NULL,
    description VARCHAR(1000),
    priority VARCHAR(30) NOT NULL,
    type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_test_cases_project_title UNIQUE (project_id, title)
);

CREATE TABLE test_runs (
    id UUID PRIMARY KEY,
    release_id UUID NOT NULL REFERENCES releases(id),
    environment_id UUID NOT NULL REFERENCES environments(id),
    name VARCHAR(160) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE test_executions (
    id UUID PRIMARY KEY,
    test_run_id UUID NOT NULL REFERENCES test_runs(id),
    test_case_id UUID NOT NULL REFERENCES test_cases(id),
    result VARCHAR(30) NOT NULL,
    notes VARCHAR(1000),
    executed_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_test_executions_run_case UNIQUE (test_run_id, test_case_id)
);
