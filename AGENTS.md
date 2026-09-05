# Repository Guidelines

## Project Structure & Module Organization

Java code lives under `src/main/java/com/zincoid/nullbot`. Keep QQ commands in `bot/`, business logic and persistence in `core/`, REST endpoints in `web/`, and STOMP handling in `websocket/`. Configuration, MyBatis XML, SQL, and static assets are in `src/main/resources`. Tests mirror the main package beneath `src/test/java`; generated media in `src/test/file` should not be committed. Deployment files are at the root; helpers are under `src/tool`.

## Build, Test, and Development Commands

- `mvn install:install-file -Dfile=src/tool/resvg-jni-0.1.4.jar -DgroupId=io.github.apeuriox -DartifactId=resvg-jni -Dversion=0.1.4 -Dpackaging=jar -DgeneratePom=true` installs the bundled native renderer dependency once.
- `mvn clean test` compiles the project and runs the JUnit suite.
- `mvn clean package -DskipTests` creates the executable JAR in `target/`.
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev` runs locally with development configuration.
- `docker compose up -d --build` builds and starts the configured service stack.

Use JDK 21 and Maven 3.8+. Copy `application-prod.yml.template` to an ignored profile file such as `application-dev.yml`, then supply MySQL, Shiro/NapCat, JWT, AI, storage, and Chrome settings before running.

## Coding Style & Naming Conventions

Follow the existing Java style: four-space indentation, same-line braces, one public top-level type per file, and packages rooted at `com.zincoid.nullbot`. Use `PascalCase` for types, `camelCase` for members, and suffixes such as `Cmd`, `Controller`, `Service`, `ServiceImpl`, `Mapper`, `DTO`, and `VO`. Keep commands grouped by feature. Lombok and MapStruct are available. No formatter or linter is enforced; use IntelliJ defaults and organize imports.

## Testing Guidelines

Tests use JUnit 5 through `spring-boot-starter-test`. Name classes `*Tests` and test methods for the behavior being exercised. Use focused unit tests where possible; reserve `@SpringBootTest` for integration coverage. Existing integration tests activate `dev` and may require MySQL, external APIs, Chrome, or writable storage, so document prerequisites and avoid interactive or machine-specific paths. Run `mvn test` before submitting. No coverage threshold is configured.

## Commit & Pull Request Guidelines

Recent history follows scoped Conventional Commits, for example `feat(image): optimize image saving` or `fix(ai): handle empty responses`. Use `feat`, `fix`, `refactor`, `chore`, or `docs` with a concise scope and imperative summary. Pull requests should explain the change and validation performed, link relevant issues, identify configuration or schema changes, and include screenshots or sample bot output for visible behavior. Never commit secrets, profile-specific YAML, logs, generated media, or `target/` artifacts.
