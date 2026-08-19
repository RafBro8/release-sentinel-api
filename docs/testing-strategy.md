# Testing Strategy

Release Sentinel is intentionally test-heavy because the project is meant to demonstrate backend quality engineering and SDET judgment.
The goal is not only to prove that endpoints work, but to show how different test layers protect different kinds of behavior.

## Quality Goals

- Validate release-readiness business rules.
- Protect REST API contracts and validation responses.
- Exercise realistic API workflows end to end.
- Verify database behavior against PostgreSQL, not only H2.
- Keep the project easy to run locally and in CI.
- Provide a Postman/Newman workflow that can be used for demos and manual smoke testing.

## Test Layers

| Layer | Location | Tools | What It Protects |
| --- | --- | --- | --- |
| Smoke test | `src/test/java/com/releasesentinel/ReleaseSentinelApiApplicationTests.java` | Spring Boot Test | Application context startup |
| Controller tests | `src/test/java/com/releasesentinel/api` | `@WebMvcTest`, MockMvc | HTTP status codes, validation, JSON response contracts |
| Service tests | `src/test/java/com/releasesentinel/service` | JUnit 5, Mockito | Business rules, status transitions, quality recommendations |
| API regression tests | `src/test/java/com/releasesentinel/api/regression` | Rest Assured, random port Spring Boot | End-to-end release readiness scenarios |
| PostgreSQL integration tests | `src/test/java/com/releasesentinel/integration` | Testcontainers, PostgreSQL, Rest Assured | Flyway migrations, JPA mappings, repository behavior, database-backed API flow |
| Postman/Newman workflow | `postman/` | Postman, Newman | Shareable manual and CLI API validation |
| CI gate | `.github/workflows/ci.yml` | GitHub Actions, Maven | Repeatable verification on push and pull request |

## Business-Critical Scenarios

The automated tests cover the project behaviors that matter most for a release readiness API:

- A clean release with passing tests can be recommended as `READY`.
- A failed execution moves release quality to `AT_RISK`.
- An open critical defect blocks the release.
- Duplicate executions inside the same test run return a conflict.
- Invalid project keys return a structured validation error.
- Defect status transitions follow the allowed lifecycle.
- Flyway migrations create the expected PostgreSQL schema before the app starts.

## Commands

Run the standard test suite:

```bash
mvn test
```

Run the full quality gate:

```bash
mvn verify
```

Run the local Postman/Newman workflow:

```bash
npx newman run postman/release-sentinel-api.postman_collection.json \
  -e postman/release-sentinel-local.postman_environment.json
```

Run the same workflow against the deployed API:

```bash
npx newman run postman/release-sentinel-api.postman_collection.json \
  -e postman/release-sentinel-local.postman_environment.json \
  --env-var baseUrl=https://release-sentinel-api.onrender.com
```

## CI Behavior

The GitHub Actions workflow runs:

```bash
mvn --batch-mode --no-transfer-progress verify
```

This means every push and pull request runs:

- Unit and service tests through Surefire.
- Controller tests through Surefire.
- Rest Assured API regression tests through Surefire.
- Testcontainers-backed PostgreSQL integration tests through Failsafe.

If CI fails, Surefire and Failsafe reports are uploaded as workflow artifacts.

## Why Testcontainers Matters Here

H2 is useful for fast tests, but it does not behave exactly like PostgreSQL.
This project uses Testcontainers to verify the persistence layer and Flyway migrations against a real PostgreSQL database.

That matters because release-readiness systems depend on trustworthy stored state: releases, test runs, executions, defects, and quality summaries all need reliable database behavior.

## Manual Testing With Postman

The Postman collection models a realistic workflow:

1. Check API status.
2. Create a project.
3. Create an environment.
4. Create a release.
5. Create a test case.
6. Create a test run.
7. Add a failed execution.
8. Create a linked critical defect.
9. Request the quality summary.
10. Verify invalid project validation.

This gives reviewers a quick way to see the API behave like a product workflow rather than isolated endpoints.

## Tradeoffs

- Authentication is intentionally out of scope for the current milestone so the quality and testing story remains the focus.
- The production Render deployment is public, while the automated Maven regression suite targets local/random-port application instances.
- The Postman collection is focused on one representative workflow instead of exhaustively covering every endpoint.

## Future Test Enhancements

- Add a production smoke-test workflow against the Render URL.
- Add contract tests for OpenAPI compatibility.
- Add mutation testing for quality gate rules.
- Add performance smoke checks around quality summary calculation.
- Add GitHub Actions scheduled health checks for the deployed API.
