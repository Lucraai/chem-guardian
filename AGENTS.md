# Repository Guidelines

## Project Structure & Module Organization
This repository is split into two services:

- `backend-java/`: Spring Boot backend, JPA entities, controllers, services, and Flyway migrations under `src/main/resources/db/migration/`.
- `ai-service-python/`: FastAPI-based AI service with document parsing, embeddings, and advisor logic under `app/`.
- `docs/`: API contracts, architecture notes, and seed examples.
- `scripts/`: local import and demo helpers.
- `docker-compose.yml`: local stack for Postgres, Qdrant, Redis, MinIO, Java, and Python services.

Static web pages for the Java service live in `backend-java/src/main/resources/static/`.

## Build, Test, and Development Commands

- `docker compose up --build`: start the full local stack.
- `cd backend-java && mvn spring-boot:run`: run the Java API locally.
- `cd backend-java && mvn test`: run Java tests.
- `cd ai-service-python && uvicorn app.main:app --reload --host 0.0.0.0 --port 8000`: run the Python service locally.
- `cd ai-service-python && pytest`: run Python tests if present.
- `python scripts/seed_demo.py`: load sample standards and run the demo flow.

## Coding Style & Naming Conventions
Use the native conventions of each stack:

- Java: 4-space indentation, `PascalCase` for classes, `camelCase` for methods and fields, package names under `com.cnchem.guardian`.
- Python: 4-space indentation, `snake_case` for functions/modules, `PascalCase` for classes, keep FastAPI routes and schemas in `app/`.
- Keep filenames and endpoint names descriptive and aligned with the existing domain language, such as `StandardLibraryService` or `advisor_service.py`.

No formatter or linter is enforced in the repo yet, so keep changes consistent with nearby code.

## Testing Guidelines
There is no committed test suite yet, so add tests close to the code you change:

- Java tests: `backend-java/src/test/java/...`
- Python tests: `ai-service-python/tests/...`

Prefer focused unit tests for services and request validation. If you add integration coverage, document any required local services or fixtures in `docs/`.

## Commit & Pull Request Guidelines
The git history is currently too small to define a strict commit convention. Use short, imperative commit subjects, for example: `backend: add standard preview validation`.

Pull requests should include:

- a concise summary of the change and affected service(s)
- any schema, API, or seed-data impact
- screenshots or sample requests for UI/API changes when relevant
- validation steps run locally, such as `mvn test` or `pytest`

## Configuration Tips
Keep environment-specific values out of source control. Use local overrides for service URLs and infrastructure endpoints such as `AI_SERVICE_URL`, `QDRANT_URL`, and database credentials.
