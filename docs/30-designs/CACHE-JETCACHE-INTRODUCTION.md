# Cache JetCache Introduction

## 1. Context

Sandwich currently runs on Spring Boot `2.0.5.RELEASE` and Java 8.

All active CrudService cache chains have been moved behind infra-local `<BusinessObject>CacheSupport` classes. The next cache framework step must keep that boundary:

- Service, Controller, Entity, Request and Response classes must not depend on JetCache.
- Existing Redis key names, TTL values and invalidation timing remain owned by the corresponding infra `CacheSupport`.
- This step must not migrate another business chain or introduce a cross-business cache helper.

## 2. Compatibility Decision

JetCache is introduced with the `2.5.x` line.

Reasoning:

- JetCache compatibility documentation lists `2.5` as tested with Spring Boot `1.1.9.RELEASE` through `2.0.5.RELEASE`.
- The same compatibility table warns that later `2.6` and `2.7` Redis integrations have Jedis/Spring Data Redis version constraints that are not a clean fit for this project baseline.
- The project therefore manages `jetcache.version` as `2.5.16`, the latest patch line used for this baseline-compatible family.

## 3. Dependency Scope

The dependency is limited to:

- root dependency management: `com.alicp.jetcache:jetcache-core`
- `sandwish-infra`: direct dependency on `jetcache-core`

No API application module depends on JetCache directly.

No `jetcache-starter-*` dependency is introduced in this step. Starter activation would require application-level YAML and bootstrapping decisions, which would exceed the current infra-only task.

## 4. Runtime Configuration

No `application*.yml` JetCache configuration is added in this step.

Current `CacheSupport` classes continue to use the project-owned `RedisClient` implementation. JetCache is available to infra code for the next implementation step, but it is not yet the runtime backing store for existing caches.

## 5. Boundary Rules

Allowed:

- JetCache imports inside `sandwish-infra`
- JetCache-backed implementation details inside module-owned `persistence.cache` packages
- Future replacement of a single `<BusinessObject>CacheSupport` internals after its key and invalidation semantics are preserved

Forbidden:

- JetCache imports in Service, Controller, Entity, Request or Response classes
- JetCache annotations on business service interfaces or implementations
- Shared `CacheManager`, `CacheTemplate`, `CacheRepository`, `CacheFacade` or cross-business helper classes
- Application YAML changes before a concrete infra runtime migration requires them

## 6. Verification

This task closes when:

- `TODO.md` deletes `cache-jetcache-introduction`
- `jetcache-core` is managed at the root and depended on only by `sandwish-infra`
- JetCache type references do not appear outside infra
- `mvn -pl sandwish-admin-api,sandwish-front-api -am test` passes
