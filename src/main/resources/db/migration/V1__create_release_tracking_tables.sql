CREATE TABLE projects (
    id UUID PRIMARY KEY,
    project_key VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE environments (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    name VARCHAR(80) NOT NULL,
    type VARCHAR(30) NOT NULL,
    base_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_environments_project_name UNIQUE (project_id, name)
);

CREATE TABLE releases (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    version VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    target_date DATE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_releases_project_version UNIQUE (project_id, version)
);
