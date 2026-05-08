# RUNBOOK Principal Auth Session Reform

## 1. Purpose

本文档定义认证会话从 `AuthSession` / `MemberAuthSession` 迁移到 `PrincipalAuthSession`，并引入 `PrincipalLoginEvent` 审计事实的执行手册。

本 RUNBOOK 固定服务于一次性重构任务。执行完成后，本 RUNBOOK 必须被删除、拆分或收敛到稳定需求文档和数据库设计文档。

目标：

- `PrincipalAuthSession` 统一 admin、member、OAuth2 token exchange 的运行态认证会话。
- `PrincipalAuthSession` 只进入 Redis，不落数据库。
- `PrincipalAuthSession` 不持有 token。
- `PrincipalAuthSession` 承载 session 相关附加内容，替代 `PermissionSession`。
- `PrincipalAccessToken` / `PrincipalRefreshToken` 持有 `PrincipalAuthSessionId`，通过 token 找 session。
- `PrincipalLoginEvent` 落数据库，记录登录、登出、刷新和 OAuth 授权相关审计事实。
- 旧 `AuthSession` / `MemberAuthSession` 数据库链路被删除。
- 旧 `PermissionSession` 运行态会话链路被删除。

## 2. Scope

范围内：

- auth 领域对象、值对象、枚举、DAO 契约。
- infra Redis DAO、DB DAO、DO、Mapper、Assembler。
- admin-api 登录、刷新、登出、OAuth2 授权码换 token、OAuth2 refresh token。
- front-api member 登录、刷新、登出。
- token DAO 中 `sessionId` 的业务类型升级。
- `PermissionSession` 合并进 `PrincipalAuthSession.values`。
- auth schema 中认证会话表删除和登录事件表新增。
- 相关 testcase 更新。

范围外：

- `OAuthClient` 配置治理。
- `OAuthAuthorization` 作为授权码事实的完整领域重命名。
- 登录事件查询 API。
- 登录失败原因枚举标准化。
- 历史生产数据迁移脚本。

## 3. Target Data Structures

### 3.1 PrincipalAuthSession

位置：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalAuthSession.java
```

定义：

```java
public class PrincipalAuthSession {
    private PrincipalAuthSessionId id;
    private PrincipalKey principalKey;
    private String clientId;
    private Map<String, PrincipalAuthSessionValue> values;
    private Date issuedAt;
    private Date lastAccessTime;
    private Date expireAt;

    public boolean isExpired(Date now);
    public int remainingSeconds(Date now);
}
```

字段规则：

- `id` 是 Redis 会话主键，使用雪花 ID 的 hex string 表达。
- `principalKey` 固定表达认证主体。
- `clientId` 固定表达 token 所属 client，admin/member 使用默认 clientId。
- `values` 固定存放 session 相关附加内容。
- `issuedAt` 固定为会话创建时间。
- `lastAccessTime` 固定为最近访问时间。
- `expireAt` 固定为会话过期时间。
- `PrincipalAuthSession` 不提供 `touch` 方法。
- `lastAccessTime` 只能由 service 编排后通过 DAO touch 能力更新。
- `values` 永远不为 null。
- `values` 的 key 使用业务常量，不使用自由散落字符串。
- `values` 只保存当前 session 运行态附加内容，不保存审计事实。
- `values` 不承担 token 反查职责。

`PrincipalAuthSessionValue`：

```java
public static class PrincipalAuthSessionValue {
    private Object value;
    private Date expiredAt;
}
```

`values` 第一批固定 key：

```text
PERMISSIONS
```

`PERMISSIONS` value 类型：

```java
Set<String>
```

`PERMISSIONS` 规则：

- admin 权限集合存入 `PrincipalAuthSession.values["PERMISSIONS"]`。
- member 当前阶段不写入 `PERMISSIONS`。
- `expiredAt` 允许为空；为空时跟随 session TTL。
- 权限重新加载由 `AdminAuthService` / `PermissionService` 编排，不由 `PrincipalAuthSession` 自己加载。

禁止字段：

- `token`
- `refreshToken`
- `identityId`
- `identityType`
- `loginType`
- `status`
- `logoutAt`
- `invalidateReason`

有效性规则：

- token 是否可用由 `PrincipalAccessToken` / `PrincipalRefreshToken` 自己判断。
- session 是否可用只判断 Redis 中存在并且 `expireAt` 未过期。
- logout、invalidate、kick out 固定删除 session，并 revoke 相关 token。

### 3.2 PrincipalAuthSessionId

位置：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/valueobject/PrincipalAuthSessionId.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/codec/PrincipalAuthSessionIdCodec.java
```

定义：

```java
public class PrincipalAuthSessionId extends BaseStringId {
    public static PrincipalAuthSessionId of(String value);
    public static PrincipalAuthSessionId ofNullable(String value);
}
```

Codec 规则：

```java
PrincipalAuthSessionIdCodec.nextId(SnowflakeIdGenerator idGenerator)
PrincipalAuthSessionIdCodec.toValue(PrincipalAuthSessionId id)
PrincipalAuthSessionIdCodec.toDomain(String value)
```

`nextId` 固定生成雪花 ID，并转换为 hex string。

### 3.3 PrincipalAuthSessionDao

位置：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/PrincipalAuthSessionDao.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PrincipalAuthSessionDaoImpl.java
```

契约：

```java
public interface PrincipalAuthSessionDao {
    PrincipalAuthSession getById(PrincipalAuthSessionId id);

    void insert(PrincipalAuthSession session, int expireSeconds);

    void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds);

    void deleteById(PrincipalAuthSessionId id);
}
```

Redis 形态：

```text
SANDWISH_PRINCIPAL_AUTH_SESSION_SESSION_{sessionId}
```

Value：

```java
private static class PrincipalAuthSessionCacheDTO implements CacheDTO {
    private String id;
    private String principalType;
    private Long principalId;
    private String clientId;
    private Map<String, PrincipalAuthSessionValueCacheDTO> values;
    private Date issuedAt;
    private Date lastAccessTime;
    private Date expireAt;
}

private static class PrincipalAuthSessionValueCacheDTO implements CacheDTO {
    private Object value;
    private Date expiredAt;
}
```

TTL：

```text
expireSeconds = session.expireAt - now
```

约束：

- 不建立 session -> token 反查索引。
- 不保存 token hash。
- 不保存 status。
- 不保存审计字段。
- `touch` 是 service 可调用的运行态写能力，不是 `PrincipalAuthSession` 的业务方法。
- `values` 随 session 整体写入 Redis。
- `values` 内单项是否过期由 service 读取时判断。

### 3.4 PrincipalAccessToken

位置：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalAccessToken.java
```

目标定义：

```java
public class PrincipalAccessToken {
    private PrincipalAccessTokenId id;
    private PrincipalAccessTokenCode tokenCode;
    private String clientId;
    private PrincipalAuthSessionId sessionId;
    private PrincipalKey principalKey;
    private Set<String> scopes;
    private Date issuedAt;
    private Date expireAt;
    private PrincipalTokenStatus status;
}
```

变化：

- `sessionId: String` 改为 `PrincipalAuthSessionId`。
- cache DTO 仍可存 `String sessionId`，assembler 内转换。
- 通过 access token 找 session。
- session 不反向保存 access token。

### 3.5 PrincipalRefreshToken

位置：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalRefreshToken.java
```

目标定义：

```java
public class PrincipalRefreshToken {
    private PrincipalRefreshTokenId id;
    private PrincipalRefreshTokenCode tokenCode;
    private PrincipalAccessTokenId accessTokenId;
    private String clientId;
    private PrincipalAuthSessionId sessionId;
    private PrincipalKey principalKey;
    private Date issuedAt;
    private Date expireAt;
    private PrincipalTokenStatus status;
}
```

变化：

- `sessionId: String` 改为 `PrincipalAuthSessionId`。
- refresh token 轮换时沿用旧 `sessionId`。
- refresh token 失效时只修改 token 状态，不修改 session。

### 3.6 OAuthAuthorization

位置：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/OAuthAuthorization.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/OAuthAuthorizationDO.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/OAuthAuthorizationPersistenceAssembler.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/OAuthAuthorizationDaoImpl.java
```

目标定义：

```java
public class OAuthAuthorization {
    private EntityId id;
    private String authorizationCode;
    private String clientId;
    private PrincipalKey principalKey;
    private String redirectUri;
    private Set<String> scopes;
    private String state;
    private String codeChallenge;
    private String codeChallengeMethod;
    private Date issuedAt;
    private Date expireAt;
    private boolean used;
}
```

变化：

- `userId: EntityId` 改为 `principalKey: PrincipalKey`。
- 当前 admin OAuth2 授权写入 `PrincipalKey.of(PrincipalType.USER, userId)`。
- OAuthAuthorization 继续落 DB，表达授权码事实。
- OAuthAuthorization 不表达运行态 session。

### 3.7 PrincipalLoginEvent

位置：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalLoginEvent.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/valueobject/PrincipalLoginEventId.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/codec/PrincipalLoginEventIdCodec.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/PrincipalLoginEventDao.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/PrincipalLoginEventDO.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/PrincipalLoginEventMapper.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/PrincipalLoginEventPersistenceAssembler.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PrincipalLoginEventDaoImpl.java
```

定义：

```java
public class PrincipalLoginEvent {
    private PrincipalLoginEventId id;
    private PrincipalKey principalKey;
    private String clientId;
    private PrincipalLoginEventType eventType;
    private PrincipalAuthenticationMethod authenticationMethod;
    private PrincipalIdentityType identityType;
    private Date occurredAt;
    private String ip;
    private String userAgent;
    private String reason;
}
```

字段规则：

- `principalKey` 登录失败时允许为空。
- `clientId` 固定必填。
- `eventType` 固定必填。
- `authenticationMethod` 固定必填。
- `identityType` 允许为空。
- `occurredAt` 固定必填。
- `ip` / `userAgent` 由 Controller 直接从 HTTP request 读取并传入。
- `reason` 使用固定字符串集合，不记录异常堆栈。

`reason` 固定值：

```text
NONE
INVALID_CREDENTIAL
ACCOUNT_DISABLED
PRINCIPAL_NOT_FOUND
IDENTITY_NOT_FOUND
CAPTCHA_INVALID
PRE_AUTH_SESSION_INVALID
TOKEN_INVALID
TOKEN_EXPIRED
REFRESH_TOKEN_USED
USER_LOGOUT
RELOGIN
PASSWORD_RESET
KICKED_OUT
OAUTH_DENIED
OAUTH_CLIENT_INVALID
OAUTH_CODE_INVALID
OAUTH_CODE_EXPIRED
SYSTEM_INVALIDATE
```

`reason` 使用规则：

- 成功事件固定使用 `NONE`。
- `LOGIN_FAILED` 固定使用失败原因。
- `LOGOUT` 固定使用 `USER_LOGOUT`、`RELOGIN`、`PASSWORD_RESET`、`KICKED_OUT` 或 `SYSTEM_INVALIDATE`。
- `TOKEN_REFRESH` 成功固定使用 `NONE`。
- OAuth 授权拒绝固定使用 `OAUTH_DENIED`。
- OAuth client 校验失败固定使用 `OAUTH_CLIENT_INVALID`。
- OAuth code 校验失败固定使用 `OAUTH_CODE_INVALID` 或 `OAUTH_CODE_EXPIRED`。
- 不能匹配到明确原因时使用 `SYSTEM_INVALIDATE`。

### 3.8 PrincipalLoginEventType

位置：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalLoginEventType.java
```

枚举：

```java
LOGIN_SUCCESS
LOGIN_FAILED
LOGOUT
TOKEN_REFRESH
OAUTH_AUTHORIZED
```

### 3.9 PrincipalAuthenticationMethod

位置：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalAuthenticationMethod.java
```

枚举：

```java
PASSWORD
SMS_CODE
EMAIL_CODE
GITHUB
WECOM
OAUTH_CODE
REFRESH_TOKEN
```

## 4. Target Database Structures

### 4.1 新增 auth_principal_login_event

位置：

```text
db/schema/auth.sql
docs/20-database/AUTH-DATABASE-DESIGN.md
```

DDL：

```sql
CREATE TABLE IF NOT EXISTS `auth_principal_login_event` (
    `id` bigint NOT NULL,
    `principal_type` varchar(32) DEFAULT NULL,
    `principal_id` bigint DEFAULT NULL,
    `client_id` varchar(64) NOT NULL,
    `event_type` varchar(32) NOT NULL,
    `authentication_method` varchar(32) NOT NULL,
    `identity_type` varchar(32) DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    `ip` varchar(64) DEFAULT NULL,
    `user_agent` varchar(512) DEFAULT NULL,
    `reason` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_auth_principal_login_event_principal_time` (`principal_type`, `principal_id`, `occurred_at`),
    KEY `idx_auth_principal_login_event_client_time` (`client_id`, `occurred_at`),
    KEY `idx_auth_principal_login_event_type_time` (`event_type`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一认证登录事件表';
```

### 4.2 升级 auth_oauth_authorization

位置：

```text
db/schema/auth.sql
docs/20-database/AUTH-DATABASE-DESIGN.md
```

目标 DDL：

```sql
CREATE TABLE IF NOT EXISTS `auth_oauth_authorization` (
    `id` bigint NOT NULL,
    `authorization_code` varchar(128) NOT NULL,
    `client_id` varchar(64) NOT NULL,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` bigint NOT NULL,
    `redirect_uri` varchar(512) NOT NULL,
    `scopes` text NOT NULL,
    `state` varchar(255) DEFAULT NULL,
    `code_challenge` varchar(128) DEFAULT NULL,
    `code_challenge_method` varchar(16) DEFAULT NULL,
    `issued_at` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `used` tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_oauth_authorization_code` (`authorization_code`),
    KEY `idx_auth_oauth_authorization_client_principal` (`client_id`, `principal_type`, `principal_id`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2授权码表';
```

变化：

- 删除 `user_id`。
- 新增 `principal_type`。
- 新增 `principal_id`。
- 索引从 `client_id + user_id + expire_at` 改成 `client_id + principal_type + principal_id + expire_at`。

### 4.3 删除旧会话表

最终删除：

```sql
DROP TABLE `auth_session`;
DROP TABLE `member_auth_session`;
```

对应删除位置：

```text
db/schema/auth.sql
docs/20-database/AUTH-DATABASE-DESIGN.md
```

## 5. Target Flows

### 5.1 Admin Password Login

目标链路：

```text
AuthController.login
-> AdminAuthService.authenticatePassword
-> AdminAuthService.createAccessToken
-> PrincipalAuthSessionDao.insert
-> PrincipalAccessTokenDao.insert
-> PrincipalRefreshTokenDao.insert
-> PrincipalLoginEventDao.insert(LOGIN_SUCCESS, PASSWORD)
```

规则：

- `PrincipalAuthSession` 使用 `ADMIN_CLIENT_ID`。
- access token 和 refresh token 持有同一个 `PrincipalAuthSessionId`。
- admin 权限集合写入 `PrincipalAuthSession.values["PERMISSIONS"]`。
- 不再创建 `PermissionSession`。

### 5.2 Admin SMS / WECOM / GitHub Login

目标链路：

```text
authenticateIdentity / provider
-> create PrincipalAuthSession
-> create PrincipalAccessToken
-> create PrincipalRefreshToken
-> insert PrincipalLoginEvent(LOGIN_SUCCESS, method)
```

`PrincipalAuthenticationMethod` 映射：

```text
USER_MOBILE -> SMS_CODE
USER_WECOM -> WECOM
USER_GITHUB -> GITHUB
```

### 5.3 Member Account Login

目标链路：

```text
LoginController.accountLogin
-> MemberAuthService.loginAccount
-> PrincipalAuthSessionDao.insert
-> PrincipalAccessTokenDao.insert
-> PrincipalRefreshTokenDao.insert
-> PrincipalLoginEventDao.insert(LOGIN_SUCCESS, PASSWORD)
```

规则：

- `PrincipalAuthSession` 使用 `MEMBER_CLIENT_ID`。
- 不创建 `MemberAuthSession`。

### 5.4 Member SMS Login

目标链路：

```text
LoginController.smsLogin
-> MemberAuthService.loginSms
-> PrincipalAuthSessionDao.insert
-> PrincipalAccessTokenDao.insert
-> PrincipalRefreshTokenDao.insert
-> PrincipalLoginEventDao.insert(LOGIN_SUCCESS, SMS_CODE)
```

### 5.5 Token Refresh

目标链路：

```text
refreshToken
-> PrincipalRefreshTokenDao.getByToken
-> oldRefreshToken.canRefresh
-> PrincipalAuthSessionDao.getById(oldRefreshToken.sessionId)
-> oldRefreshToken.markUsed
-> create next access token with same sessionId
-> create next refresh token with same sessionId
-> PrincipalLoginEventDao.insert(TOKEN_REFRESH, REFRESH_TOKEN)
```

规则：

- refresh 不创建新 session。
- refresh 沿用旧 session。
- session 不记录 refresh 次数。
- 旧 refresh token 标记为 `USED`。

### 5.6 Logout

目标链路：

```text
accessToken
-> PrincipalAccessTokenDao.getByToken
-> accessToken.revoke
-> PrincipalAuthSessionDao.deleteById(accessToken.sessionId)
-> active refresh tokens revoke
-> PrincipalLoginEventDao.insert(LOGOUT, reason)
```

规则：

- logout 删除 Redis session。
- logout revoke access token。
- logout revoke 同 principal、client、session 关联的 refresh token。
- 如果 token 已失效，业务接口按现有语义返回错误。

### 5.7 Kick Out / Password Reset / Re-login

目标链路：

```text
principalKey + clientId
-> list active access tokens
-> for each token: revoke + delete session
-> list active refresh tokens
-> for each token: revoke
-> PrincipalLoginEventDao.insert(LOGOUT, reason)
```

规则：

- 不通过 session 查 token。
- 固定通过 token DAO 的 principal/client/status 索引查 token。

### 5.8 OAuth2 Authorization Code Exchange

目标链路：

```text
authorizationCode
-> OAuthAuthorizationDao.getByAuthorizationCode
-> authorization.canConsume
-> authorization.markUsed
-> PrincipalAuthSessionDao.insert(clientId = OAuthClient.clientId)
-> PrincipalAccessTokenDao.insert
-> PrincipalRefreshTokenDao.insert
-> PrincipalLoginEventDao.insert(OAUTH_AUTHORIZED, OAUTH_CODE)
```

规则：

- `OAuthAuthorization` 继续落 DB，表达授权码事实。
- `OAuthAuthorization.principalKey` 作为 token 的 `principalKey` 来源。
- OAuth token exchange 创建 `PrincipalAuthSession`。
- OAuth refresh token 沿用旧 session。

### 5.9 Token Introspection / Userinfo

目标链路：

```text
token
-> PrincipalAccessTokenDao.getByToken
-> accessToken.canAccess
-> PrincipalAuthSessionDao.getById(accessToken.sessionId)
-> session exists && !session.isExpired(now)
-> user/member enabled
```

规则：

- introspection 必须同时判断 token 和 session。
- session 不存在时 token 视为 inactive。

## 6. File-Level Execution Order

### 6.1 Phase 1: 新增 PrincipalAuthSession 基础结构

新增：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalAuthSession.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/valueobject/PrincipalAuthSessionId.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/codec/PrincipalAuthSessionIdCodec.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/PrincipalAuthSessionDao.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PrincipalAuthSessionDaoImpl.java
```

测试：

```text
sandwish-infra/src/test/java/com/github/thundax/modules/auth/persistence/dao/PrincipalAuthSessionDaoImplTest.java
```

验证：

```bash
mvn -pl sandwish-biz -am test
mvn -pl sandwish-infra -am -Dtest=PrincipalAuthSessionDaoImplTest -DfailIfNoTests=false test
```

### 6.2 Phase 2: token.sessionId 类型升级

修改：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalAccessToken.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalRefreshToken.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PrincipalAccessTokenDaoImpl.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PrincipalRefreshTokenDaoImpl.java
```

验证：

```bash
mvn -pl sandwish-biz,sandwish-infra -am test
```

### 6.3 Phase 3: admin-api 接入 PrincipalAuthSession

修改：

```text
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AdminAuthServiceImpl.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/result/AuthTokenQueryResult.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/assembler/AuthInterfaceAssembler.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/security/filter/AccessTokenAuthenticationFilter.java
```

删除 admin 对旧 session DB 的写依赖：

```text
AuthSessionDao
AuthSessionRuntimeDao
AuthSession
```

本阶段只断开 admin 使用，不删除旧类。

验证：

```bash
mvn -pl sandwish-admin-api -am -DskipTests compile
mvn -pl sandwish-admin-api -am -Dtest=AuthPermissionLifecycleTest -DfailIfNoTests=false test
```

### 6.4 Phase 4: front-api 接入 PrincipalAuthSession

修改：

```text
sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberAuthServiceImpl.java
sandwish-front-api/src/main/java/com/github/thundax/modules/auth/security/MemberAccessTokenAuthenticationFilter.java
```

删除 member 对旧 session DB 的写依赖：

```text
MemberAuthSessionDao
MemberAuthSessionRuntimeDao
MemberAuthSession
```

本阶段只断开 front 使用，不删除旧类。

验证：

```bash
mvn -pl sandwish-front-api -am -DskipTests compile
```

### 6.5 Phase 5: OAuth2 接入 PrincipalAuthSession

修改：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/OAuthAuthorization.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/OAuthAuthorizationDO.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/OAuthAuthorizationPersistenceAssembler.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/OAuthAuthorizationDaoImpl.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AdminAuthServiceImpl.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java
db/schema/auth.sql
docs/20-database/AUTH-DATABASE-DESIGN.md
```

涉及方法：

```text
decideOAuth2
exchangeAuthorizationCode
refreshOAuth2Token
queryOAuthAccessToken
exchangeOAuth2Token
revokeOAuth2Token
```

验证：

```bash
mvn -pl sandwish-admin-api -am -DskipTests compile
mvn -pl sandwish-admin-api -am -Dtest=AuthControllerContractTest -DfailIfNoTests=false test
```

### 6.6 Phase 6: 新增 PrincipalLoginEvent

新增：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalLoginEvent.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/valueobject/PrincipalLoginEventId.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/codec/PrincipalLoginEventIdCodec.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalLoginEventType.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalAuthenticationMethod.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/PrincipalLoginEventDao.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/PrincipalLoginEventDO.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/PrincipalLoginEventMapper.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/PrincipalLoginEventPersistenceAssembler.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PrincipalLoginEventDaoImpl.java
```

修改：

```text
db/schema/auth.sql
docs/20-database/AUTH-DATABASE-DESIGN.md
```

Controller 规则：

- `AuthController` 和 `LoginController` 直接从 `HttpServletRequest` 读取 `ip` 和 `userAgent`。
- `ip` 读取优先级固定为 `X-Forwarded-For` 首个值、`X-Real-IP`、`request.getRemoteAddr()`。
- `userAgent` 固定读取 `User-Agent` header。
- Service 接收 `ip` 和 `userAgent`，不直接依赖 Servlet API。

验证：

```bash
mvn -pl sandwish-biz,sandwish-infra -am test
```

### 6.7 Phase 7: 登录事件接入业务链路

修改：

```text
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AdminAuthServiceImpl.java
sandwish-front-api/src/main/java/com/github/thundax/modules/auth/controller/LoginController.java
sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberAuthServiceImpl.java
```

接入事件：

```text
LOGIN_SUCCESS
LOGOUT
TOKEN_REFRESH
OAUTH_AUTHORIZED
```

失败事件：

```text
LOGIN_FAILED
```

执行策略：

- 第一小步只接成功、登出、刷新、OAuth 授权。
- 第二小步接登录失败分支。
- 登录失败分支必须保持当前异常语义不变。

### 6.8 Phase 8: 删除旧 AuthSession / MemberAuthSession 链路

删除：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AuthSession.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/MemberAuthSession.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/AuthSessionStatus.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/MemberAuthSessionStatus.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/AuthSessionDao.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/AuthSessionRuntimeDao.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/MemberAuthSessionDao.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/MemberAuthSessionRuntimeDao.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/AuthSessionPersistenceAssembler.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/MemberAuthSessionPersistenceAssembler.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/AuthSessionDO.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/MemberAuthSessionDO.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/AuthSessionMapper.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/MemberAuthSessionMapper.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionDaoImpl.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionRuntimeDaoImpl.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/MemberAuthSessionDaoImpl.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/MemberAuthSessionRuntimeDaoImpl.java
sandwish-infra/src/test/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionRuntimeDaoImplTest.java
```

修改：

```text
db/schema/auth.sql
docs/20-database/AUTH-DATABASE-DESIGN.md
```

扫描：

```bash
rg "AuthSession|MemberAuthSession|auth_session|member_auth_session|AuthSessionStatus|MemberAuthSessionStatus" sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api db docs
```

### 6.9 Phase 9: PermissionSession 合并进 PrincipalAuthSession

目标：

- `PermissionSession` 的权限集合进入 `PrincipalAuthSession.values["PERMISSIONS"]`。
- `PermissionSession` 不再表达 session 存活。
- admin 权限读取从 `PrincipalAuthSession` 获取。
- 旧 `PermissionSession`、`PermissionDao` 和 `PermissionDaoImpl` 删除。

删除：

```text
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PermissionSession.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/PermissionDao.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/PermissionService.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/PermissionServiceImpl.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PermissionDaoImpl.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/security/filter/AccessTokenAuthenticationFilter.java
```

修改：

```text
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/PermissionService.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/PermissionServiceImpl.java
sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/security/filter/AccessTokenAuthenticationFilter.java
sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalAuthSession.java
sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PrincipalAuthSessionDaoImpl.java
```

规则：

- `PermissionService` 可以保留为权限加载和刷新能力。
- `PermissionService` 不再创建独立 session。
- `AccessTokenAuthenticationFilter` 通过 access token 的 `sessionId` 加载 `PrincipalAuthSession`。
- `AccessTokenAuthenticationFilter` 从 `values["PERMISSIONS"]` 读取权限集合。
- 权限缺失或权限项 TTL 过期时，由 `PermissionService` 重新加载权限并写回 `PrincipalAuthSession`。
- `PrincipalAuthSession` 固定是认证会话存活来源。

## 7. Verification

固定验证命令：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests compile
mvn -pl sandwish-admin-api -am -Dtest=AuthPermissionLifecycleTest -DfailIfNoTests=false test
mvn -pl sandwish-admin-api -am -Dtest=AuthControllerContractTest -DfailIfNoTests=false test
mvn -pl sandwish-front-api -am -DskipTests compile
mvn -pl sandwish-infra -am test
git diff --check
```

固定残留扫描：

```bash
rg "AuthSession|MemberAuthSession|auth_session|member_auth_session|AuthSessionStatus|MemberAuthSessionStatus" sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api db docs
rg "PermissionSession|PermissionDao|PermissionDaoImpl|PERMISSION_SESSION" sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api db docs
rg "String sessionId|private String sessionId|getSessionId\\(|setSessionId\\(" sandwish-biz/src/main/java/com/github/thundax/modules/auth sandwish-infra/src/main/java/com/github/thundax/modules/auth sandwish-admin-api/src/main/java/com/github/thundax/modules/auth sandwish-front-api/src/main/java/com/github/thundax/modules/auth
rg "PrincipalLoginEvent|auth_principal_login_event" sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api db docs
rg "OAuthAuthorization.*userId|getUserId\\(|setUserId\\(|`user_id` bigint NOT NULL" sandwish-biz/src/main/java/com/github/thundax/modules/auth sandwish-infra/src/main/java/com/github/thundax/modules/auth sandwish-admin-api/src/main/java/com/github/thundax/modules/auth db/schema/auth.sql
```

## 8. Commit Boundaries

每个阶段可以独立提交。每个提交必须保持影响模块可编译。

固定提交拆分：

1. `Feat(auth): 增加主体认证会话运行态`
2. `Refactor(auth): 令牌绑定主体认证会话`
3. `Refactor(auth): 后台认证接入主体认证会话`
4. `Refactor(auth): 前台会员认证接入主体认证会话`
5. `Refactor(auth): OAuth令牌交换接入主体认证会话`
6. `Feat(auth): 增加主体登录事件审计`
7. `Refactor(auth): 认证链路写入主体登录事件`
8. `Refactor(auth): 删除旧认证会话链路`
9. `Refactor(auth): 合并权限运行态会话`

## 9. Open Items

无
