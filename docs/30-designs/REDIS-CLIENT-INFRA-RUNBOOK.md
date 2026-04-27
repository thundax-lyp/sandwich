# RedisClient Infra Runbook

> Superseded note: `docs/30-designs/COMMON-CACHE-JETCACHE-RUNBOOK.md` is now the active runbook for replacing `RedisClient` with JetCache through `sandwish-common-cache`. This document remains as historical inventory and boundary context.

## 1. Goal

`RedisClient` is an old common utility wrapper. The target state is to delete `com.github.thundax.common.utils.redis.RedisClient`.

Redis access is an infra implementation detail. Service and Controller code must not directly depend on Redis, JetCache, `StringRedisTemplate`, or a generic cache client.

## 2. Current Problem

`RedisClient` mixes several responsibilities:

- raw Redis value operations
- JSON serialization and deserialization
- typed object cache helpers
- key scanning and pattern deletion
- hash operations
- TTL and existence helpers
- business cache semantics through caller-owned keys

Because it lives in `sandwish-common-core`, every layer can inject it. Current callers include API controllers, front-api session utilities, security services, infra DAO implementations, and infra `CacheSupport` classes.

## 3. Current Call Site Inventory

### 3.1 Common Definition

- `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/utils/redis/RedisClient.java`

### 3.2 Admin API Direct Callers

These callers must move behind business/application services before `RedisClient` can be deleted:

- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/KeypairApiController.java`
  - stores SM2 private key by login token
  - current operations: `set`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserApiController.java`
  - reads SM2 private key during password decrypt
  - current operations: `get`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/PersonalApiController.java`
  - reads SM2 private key during password decrypt
  - current operations: `get`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/security/service/impl/SubjectServiceImpl.java`
  - caches subject permissions and subject version
  - current operations: `get`, `set`, `expire`, `delete`, `deleteByPattern`
- `sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/DefaultEncryptConfiguration.java`
  - stale constructor injection surface for `UserEncryptService`

### 3.3 Front API Direct Callers

- `sandwish-front-api/src/main/java/com/github/thundax/modules/member/service/impl/SessionCacheServiceImpl.java`
  - session attribute cache
  - current operations: typed `get`, `set`, `delete`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/utils/DictUtils.java`
  - legacy dict map cache
  - current operations: `get`, `set`

### 3.4 Biz Direct Caller

- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/servlet/SmsValidateCodeServlet.java`
  - SMS validation rate-limit key
  - current operations: `exists`, `set`
  - also uses `SpringContextHolder.getBean(RedisClient.class)`, which is a hard boundary leak

### 3.5 Infra DAO Callers

These are already in infra, but should stop depending on common `RedisClient` before deletion:

- `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dao/AsyncTaskDaoImpl.java`
  - current operations: typed `get`, `set`, `delete`
- `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/AccessTokenDaoImpl.java`
  - current operations: `keys`, `get`, typed `get`, `set`, `expire`, `delete`
- `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/LoginFormDaoImpl.java`
  - current operations: typed `get`, `get`, `set`, `exists`, `delete`, `setHash`
- `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/LoginLockDaoImpl.java`
  - current operations: typed `get`, `set`, `increment`, `delete`, `exists`, `getExpire`

### 3.6 Infra CacheSupport Callers

These should be migrated after the higher-layer leaks are gone. They are the best place to introduce JetCache-backed implementation details:

- `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/cache/StorageCacheSupport.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/cache/DictCacheSupport.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/cache/MenuCacheSupport.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/cache/OfficeCacheSupport.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/cache/RoleCacheSupport.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/cache/UserCacheSupport.java`

## 4. Target Boundaries

### 4.1 Forbidden Direction

The following packages must not import `RedisClient`:

- `sandwish-admin-api`
- `sandwish-front-api`
- `sandwish-biz`
- service implementations
- controllers
- servlet/filter/interceptor entry points

### 4.2 Allowed Interim Direction

During migration, `sandwish-infra` may still use Redis directly, but not through `sandwish-common-core` `RedisClient` once the infra adapter exists.

### 4.3 Final Direction

- Redis-specific operations live under `sandwish-infra`.
- Business-facing code depends on business interfaces and service semantics.
- Object cache internals live in `<BusinessObject>CacheSupport`.
- Temporary auth state lives behind auth DAO or auth-specific store interfaces.
- Session state lives behind session-specific service contracts.
- `RedisClient` is deleted from `sandwish-common-core`.

### 4.4 Service And DAO Addition Rules

RedisClient migration may add Service, DAO or Store contracts, but the added type must match the responsibility being moved.

Fixed rules:

- Add a business DAO or Store interface when the responsibility is state persistence, lookup, TTL, lock, token, session attribute, async task state, or temporary key storage.
- Add a Service only when the caller needs business orchestration, validation, permission adaptation, key generation flow, or cross-DAO coordination.
- Do not add a Service that only forwards `get`, `set`, `delete`, `exists`, `expire`, or `keys` to Redis.
- Do not add a generic `RedisService`, `CacheService`, `RedisStore`, `CacheStore`, `RedisRepository`, or `CacheRepository`.
- DAO or Store interfaces live in `sandwish-biz` when business-facing code needs them.
- Implementations live in `sandwish-infra`.
- Infra-only replacement adapters must stay narrowly scoped and must not become a new common client.

Applied examples:

- SM2 private key storage needs a business-facing Store and may need a Service only if key generation/decryption flow is moved out of Controller.
- Subject remote cache needs a Store below `SubjectServiceImpl`; `SubjectServiceImpl` remains the business/security orchestration point.
- Login token, login form, login lock, async task and SMS send marker are state persistence concerns and should be modeled as DAO/Store responsibilities.
- `<BusinessObject>CacheSupport` does not need a new Service or DAO just to replace its backing Redis implementation.

## 5. Migration Order

### Step 1: Keypair Private Key Store

Create a business semantic interface for SM2 private key storage. Fixed shape:

- interface in `sandwish-biz`: `KeypairPrivateKeyStore`
- implementation in `sandwish-infra`: Redis-backed private key store

If key generation and private-key storage remain coupled in the controller, add a small business Service to own that flow. The Store still owns TTL persistence.

Move `KeypairApiController`, `UserApiController`, and `PersonalApiController` from `RedisClient` to that semantic interface.

Do not expose Redis key names or TTL rules to controllers.

### Step 2: Subject Cache Store

Move `SubjectServiceImpl` remote subject/version storage behind an auth/security cache store.

Keep local in-memory maps inside `SubjectServiceImpl` if needed, but remote persistence operations should not be direct Redis calls from admin API service code.

### Step 3: Front Session And Legacy Dict Utilities

Move front session cache storage behind an implementation detail outside direct `RedisClient` injection.

For `DictUtils`, prefer replacing the legacy map cache with the existing `DictDao`/`DictCacheSupport` path or deleting the utility if unused. Do not create a new generic cache helper just for it.

### Step 4: Biz Servlet SMS State

Move SMS validation rate-limit state out of `SmsValidateCodeServlet`.

Fixed shape:

- business DAO or Store interface in `sandwish-biz`
- infra implementation stores rate-limit state and TTL
- servlet calls semantic method such as `canSend` / `markSent`

### Step 5: Infra DAO Redis Adapter

Replace infra DAO direct `RedisClient` usage with an infra-local adapter or typed store implementation.

Existing business DAO interfaces should remain the semantic boundary for `AccessTokenDao`, `LoginFormDao`, `LoginLockDao`, and `AsyncTaskDao`. Do not add an extra Service for these if the flow is still only persistence.

Do not move `RedisClient` into infra under the same generic name. If an adapter is needed, keep it package-private or narrowly named by responsibility.

### Step 6: CacheSupport Backing Migration

Migrate `CacheSupport` internals away from common `RedisClient`.

Allowed options:

- use JetCache inside each `<BusinessObject>CacheSupport`
- use an infra-local, non-public Redis operation adapter only inside `persistence.cache`

Do not introduce a cross-business `CacheManager`, `CacheTemplate`, `CacheRepository`, or `CacheFacade`.

### Step 7: Delete Common RedisClient

After no imports remain:

- delete `RedisClient.java`
- remove dead dependencies or configuration only if no other code still needs them
- update docs that still describe `RedisClient` as a common-core responsibility

## 6. Verification Commands

Use these checks while closing each task:

```bash
rg -n "RedisClient" sandwish-admin-api sandwish-front-api sandwish-biz -g '*.java'
rg -n "com.github.thundax.common.utils.redis.RedisClient" sandwish-common sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api -g '*.java'
mvn -pl sandwish-admin-api,sandwish-front-api -am test
```

Final deletion additionally requires:

```bash
rg -n "RedisClient|common.utils.redis" sandwish-common sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api -g '*.java'
```

## 7. Non-Goals

- Do not change Spring Boot version.
- Do not make controllers depend on JetCache.
- Do not introduce a generic RedisClient clone. The active migration now introduces `sandwish-common-cache` only for JetCache baseline configuration and thin cache infrastructure.
- Do not rewrite Shiro session/cache internals unless required by a specific RedisClient deletion step.
- Do not change existing Redis key names during the first migration pass.
