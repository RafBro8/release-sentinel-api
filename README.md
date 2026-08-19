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
- Dockerized local and production-style runtime
- JUnit 5 unit and integration testing
- Rest Assured API regression testing
- Testcontainers database testing
- Postman/Newman API validation
- GitHub Actions CI quality gates
- Professional test strategy documentation

## Current Scope

The current stage establishes a production-ready backend foundation with automated quality validation:

- Spring Boot application skeleton
- Maven build configuration
- Root API info endpoint for deployed portfolio visitors
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
- PostgreSQL Testcontainers integration tests for database-backed API validation
- Postman collection and Newman workflow for manual and CLI API validation
- GitHub Actions CI pipeline running the full Maven verification suite
- Dockerfile and production Spring profile for deployable runtime packaging
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
| 10 | Production deployment prep | Docker image, production config, and deploy docs |
| 11 | Render deployment | Public API deployment with managed PostgreSQL |
| 12 | Portfolio polish | Diagrams, examples, and final showcase docs |

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

Run PostgreSQL-backed integration tests:

```bash
mvn verify
```

Run the Postman/Newman workflow:

```bash
npx newman run postman/release-sentinel-api.postman_collection.json \
  -e postman/release-sentinel-local.postman_environment.json
```

The Postman collection can also be imported manually from `postman/release-sentinel-api.postman_collection.json`
with the local environment file at `postman/release-sentinel-local.postman_environment.json`.

Useful URLs:

- API info: `http://localhost:8080/`
- Status: `http://localhost:8080/api/status`
- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Docker Runtime

Build the production-style Docker image:

```bash
docker build -t release-sentinel-api .
```

Run the API image against the local PostgreSQL container:

```bash
docker run --rm \
  --name release-sentinel-api \
  --network release-sentinel-api_default \
  -p 8080:10000 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/release_sentinel \
  -e SPRING_DATASOURCE_USERNAME=release_sentinel \
  -e SPRING_DATASOURCE_PASSWORD=release_sentinel \
  release-sentinel-api
```

Then verify:

```bash
curl http://localhost:8080/
curl http://localhost:8080/api/status
curl http://localhost:8080/actuator/health
```

## Production Configuration

The `prod` Spring profile is designed for container hosting.

Runtime environment variables:

| Variable | Purpose | Example |
| --- | --- | --- |
| `PORT` | HTTP port exposed by the hosting platform | `10000` |
| `SPRING_DATASOURCE_URL` | JDBC connection string for PostgreSQL | `jdbc:postgresql://host:5432/release_sentinel` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username | `release_sentinel` |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password | Render secret value |
| `JAVA_OPTS` | Optional JVM tuning | `-XX:MaxRAMPercentage=75` |

Render Docker deployment checklist:

- Create a PostgreSQL database in Render.
- Create a Web Service from the GitHub repository.
- Select Docker as the runtime so Render builds from the repository `Dockerfile`.
- Set `SPRING_PROFILES_ACTIVE=prod`.
- Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` from the Render PostgreSQL connection details.
- Use `/actuator/health` as the health check path.

Render web services should bind to `0.0.0.0` and use the `PORT` environment variable. The production profile handles that through `server.address` and `server.port`.

## Continuous Integration

GitHub Actions runs the project quality gate on every push and pull request to `master`.

The CI workflow executes:

```bash
mvn --batch-mode --no-transfer-progress verify
```

That command runs the unit tests, Spring MVC/controller tests, Rest Assured API regression tests, and PostgreSQL Testcontainers integration tests.
If the workflow fails, Maven Surefire and Failsafe reports are uploaded as GitHub Actions artifacts for debugging.

## Project Status

Stage 11 deployment is live. The API is deployed to Render and includes a root info endpoint for public portfolio visitors.
