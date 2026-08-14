# Release Sentinel API

Release Sentinel is a Spring Boot API that helps engineering teams evaluate release readiness before shipping software.
It combines test execution results, defect severity, environment coverage, and configurable quality gates into a clear release recommendation.

## Why This Project Exists

Small engineering teams often make release decisions from scattered signals: spreadsheet test cases, defect tickets, Slack updates, CI logs, and manual QA notes.
Release Sentinel provides a backend service for turning those signals into structured API data that can answer one practical question:

> Is this release ready to ship?

## Portfolio Focus

This project is built to demonstrate backend development and SDET-quality engineering skills:

- Java 21 and Spring Boot API development
- RESTful endpoint design
- PostgreSQL-backed persistence
- Dockerized local development
- JUnit 5 unit and integration testing
- Rest Assured API regression testing
- Testcontainers database testing
- Postman/Newman API validation
- CI/CD quality gates
- Professional test strategy documentation

## Current Scope

The current stage establishes the API foundation and the first release tracking APIs:

- Spring Boot application skeleton
- Maven build configuration
- Actuator health endpoint
- PostgreSQL Docker Compose setup
- OpenAPI/Swagger dependency
- Flyway database migrations
- Project, environment, and release APIs
- Test case, test run, and test execution APIs
- Defect tracking APIs with release-blocking rules
- Release quality gate summary with READY, AT_RISK, and BLOCKED recommendations
- Global API error response contract
- JUnit smoke, controller, service, workflow-rule, defect lifecycle, and quality gate tests
- Rest Assured API regression tests for end-to-end release readiness workflows
- Initial project documentation

## Planned Stages

| Stage | Focus | Outcome |
| --- | --- | --- |
| 1 | Project foundation | Runnable Spring Boot API skeleton |
| 2 | Projects, environments, releases | Core release tracking model |
| 3 | Test cases and test runs | Test execution data model |
| 4 | Defects | Severity, status, and release-blocking rules |
| 5 | Quality gate engine | READY, AT_RISK, or BLOCKED release recommendation |
| 6 | Rest Assured suite | Automated API regression tests |
| 7 | Testcontainers | Real PostgreSQL integration tests |
| 8 | Postman/Newman | Manual and CLI API validation |
| 9 | CI/CD | Automated build and quality pipeline |
| 10 | Portfolio polish | Diagrams, examples, and final showcase docs |

## Local Development

Prerequisites:

- Java 21
- Maven 3.9+
- Docker

Start PostgreSQL:

```bash
docker compose up -d
```

Run the API:

```bash
mvn spring-boot:run
```

Run tests:

```bash
mvn test
```

Useful URLs:

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Project Status

Stage 6 is in progress and should be reviewed before the sixth commit.
