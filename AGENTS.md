# Repository Guidelines

## Project Structure & Module Organization

This is a Maven multi-module Java 8 Spring Boot project. The root `pom.xml` defines shared dependency versions and modules:

- `interaction-framework`: common framework utilities under `src/main/java/com/wdit/common`.
- `interaction-base`: shared business/domain support under `src/main/java/com/wdit/modules`.
- `interaction-admin-api`: admin WAR application with Java code, config, JSP views, tags, and vendor JARs.
- `interaction-front`: front-facing WAR application with Java code, config, JSP views, and static assets.

Use `src/main/java` for Java classes, `src/main/resources/config` for Spring configuration, `src/main/resources/static` for CSS/JS/images, and `src/main/webapp/WEB-INF/views` for JSP pages.

## Build, Test, and Development Commands

- `mvn clean install`: builds all modules, runs tests, and installs artifacts locally.
- `mvn test`: runs the full Maven test phase.
- `mvn -pl interaction-admin-api -am package`: builds the admin WAR and required dependencies.
- `mvn -pl interaction-front -am package`: builds the front WAR and required dependencies.
- `mvn -pl interaction-framework test`: runs tests for one module.

The WAR modules rely on system-scoped vendor JARs in `src/main/webapp/WEB-INF/lib`; keep those paths intact when packaging.

## Coding Style & Naming Conventions

Follow the existing Java style: 4-space indentation, package names under `com.wdit`, PascalCase classes, camelCase fields/methods, and uppercase constants. Keep controllers, services, utilities, JSPs, and static assets in the same module as the feature they serve. Prefer existing helper classes in `interaction-framework` and `interaction-base` before adding new utilities.

## Testing Guidelines

Place tests under each module’s `src/test/java`, mirroring the production package. Name unit tests `*Test` and integration-style tests `*IT` when added. Use `mvn test` before submitting shared framework/base changes, and run the relevant module package command before WAR-facing changes.

## Commit & Pull Request Guidelines

The current history is minimal (`Initial commit`), so use clear imperative commit messages such as `Add archive upload validation` or `Fix front login redirect`. For pull requests, include a short summary, affected modules, test/build commands run, linked issues when applicable, and screenshots for JSP or static UI changes.

## Security & Configuration Tips

Do not commit credentials or environment-specific secrets in `application*.yml`. Keep database drivers and third-party binary updates deliberate, documented, and limited to the module that needs them.
