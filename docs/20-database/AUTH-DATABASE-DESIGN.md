# AUTH DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich 后台认证、OAuth2 授权和 OAuth token 模型的数据库表、字段映射、关系约束和持久化规则。

本文档以 `AUTH-REQUIREMENTS.md` 的后台认证模型为基础，固定 `UserIdentity`、`UserCredential` 和 `AuthSession` 的目标持久化设计。当前仓库未提供独立建表 SQL，真实数据库 DDL 必须在上线前与本文档完成核对。

## 2. Scope

当前覆盖范围：

- `auth_user_identity`
- `auth_user_credential`
- `auth_session`
- `auth_oauth_client`
- `auth_oauth_authorization`
- `auth_oauth_access_token`
- `auth_oauth_refresh_token`
- `UserIdentityDO`
- `UserCredentialDO`
- `AuthSessionDO`
- `OAuthClientDO`
- `OAuthAuthorizationDO`
- `OAuthAccessTokenDO`
- `OAuthRefreshTokenDO`
- `UserIdentityMapper`
- `UserCredentialMapper`
- `AuthSessionMapper`
- `OAuthClientMapper`
- `OAuthAuthorizationMapper`
- `OAuthAccessTokenMapper`
- `OAuthRefreshTokenMapper`
- `UserIdentityDaoImpl`
- `UserCredentialDaoImpl`
- `AuthSessionDaoImpl`
- `OAuthClientDaoImpl`
- `OAuthAuthorizationDaoImpl`
- `OAuthAccessTokenDaoImpl`
- `OAuthRefreshTokenDaoImpl`
- `UserIdentityPersistenceAssembler`
- `UserCredentialPersistenceAssembler`
- `AuthSessionPersistenceAssembler`
- `OAuthClientPersistenceAssembler`
- `OAuthAuthorizationPersistenceAssembler`
- `OAuthAccessTokenPersistenceAssembler`
- `OAuthRefreshTokenPersistenceAssembler`

当前不覆盖范围：

- 前台会员登录表。
- MFA 凭据表。
- 认证审计日志表。
- 生产数据迁移脚本。

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- 存储引擎优先使用 `InnoDB`。
- 字符集优先使用 `utf8mb4`。
- `UserIdentityDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- `UserCredentialDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- `AuthSessionDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- `OAuthClientDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- `OAuthAuthorizationDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- `OAuthAccessTokenDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- `OAuthRefreshTokenDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- `user_id` 固定引用 `sys_user.id`。
- `identity_id` 固定引用 `auth_user_identity.id`。
- 枚举字段使用 `varchar` 存储。
- 集合字段优先使用 JSON 字符串表达，由持久化装配器负责转换。
- 敏感字段不得明文落库。
- `credential_value` 固定保存密码哈希，不保存密码明文。
- `client_secret_hash` 固定保存客户端密钥哈希，不保存客户端密钥明文。
- `token_hash` 固定保存 OAuth token 的 SHA-256 Base64Url 哈希，不保存 token 明文。
- 新增表不声明 `del_flag`，禁用、锁定、登出和失效通过状态字段表达。
- `DO/DataObject` 不暴露给 Controller 或 Service。

## 4. Naming Rules

- 登录标识表固定为 `auth_user_identity`。
- 认证凭据表固定为 `auth_user_credential`。
- 认证会话表固定为 `auth_session`。
- OAuth 客户端表固定为 `auth_oauth_client`。
- OAuth 授权表固定为 `auth_oauth_authorization`。
- OAuth access token 表固定为 `auth_oauth_access_token`。
- OAuth refresh token 表固定为 `auth_oauth_refresh_token`。
- 主键字段固定为 `id`。
- 后台用户主键字段固定为 `user_id`。
- 登录标识主键字段固定为 `identity_id`。
- 登录标识类型字段固定为 `identity_type`。
- 登录标识值字段固定为 `identity_value`。
- 凭据类型字段固定为 `credential_type`。
- 凭据值字段固定为 `credential_value`。
- 会话标识字段固定为 `session_id`。
- 访问 token 字段固定为 `token`。
- 状态字段固定为 `status`。
- 审计字段固定为 `create_date`、`create_by`、`update_date`、`update_by`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `auth_user_identity` | `UserIdentityDO` | `UserIdentityMapper` | `UserIdentity` |
| `auth_user_credential` | `UserCredentialDO` | `UserCredentialMapper` | `UserCredential` |
| `auth_session` | `AuthSessionDO` | `AuthSessionMapper` | `AuthSession` |
| `auth_oauth_client` | `OAuthClientDO` | `OAuthClientMapper` | `OAuthClient` |
| `auth_oauth_authorization` | `OAuthAuthorizationDO` | `OAuthAuthorizationMapper` | `OAuthAuthorization` |
| `auth_oauth_access_token` | `OAuthAccessTokenDO` | `OAuthAccessTokenMapper` | `OAuthAccessToken` |
| `auth_oauth_refresh_token` | `OAuthRefreshTokenDO` | `OAuthRefreshTokenMapper` | `OAuthRefreshToken` |

## 6. Table Design

### 6.1 auth_user_identity

`auth_user_identity` 保存后台用户登录标识。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 登录标识主键 |
| `user_id` | `userId` | `userId` | 是 | 后台用户 ID |
| `identity_type` | `identityType` | `identityType` | 是 | 登录标识类型 |
| `identity_value` | `identityValue` | `identityValue` | 是 | 登录标识值 |
| `status` | `status` | `status` | 是 | 登录标识状态 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 MyBatis-Plus `IdType.ASSIGN_UUID` 生成。
- `user_id` 来源是 `sys_user.id`。
- `identity_type` 固定写入 `ACCOUNT`、`MOBILE` 或 `EMAIL`。
- `status` 固定写入 `ENABLED` 或 `DISABLED`。
- `identity_value` 必须保存规范化后的登录标识值。
- `ACCOUNT` 类型 `identity_value` 迁移期来源是 `sys_user.login_name`。
- `MOBILE` 类型 `identity_value` 迁移期来源是 `sys_user.mobile` 或 `sys_user_encrypt.mobile`。
- `EMAIL` 类型 `identity_value` 迁移期来源是 `sys_user.email` 或 `sys_user_encrypt.email`。

索引：

- 主键：`pk_auth_user_identity(id)`
- 联合唯一索引：`uk_auth_user_identity_type_value(identity_type, identity_value)`
- 普通索引：`idx_auth_user_identity_user(user_id, status)`
- 普通索引：`idx_auth_user_identity_user_type(user_id, identity_type)`

### 6.2 auth_user_credential

`auth_user_credential` 保存后台用户认证凭据。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 认证凭据主键 |
| `user_id` | `userId` | `userId` | 是 | 后台用户 ID |
| `identity_id` | `identityId` | `identityId` | 是 | 登录标识 ID |
| `credential_type` | `credentialType` | `credentialType` | 是 | 凭据类型 |
| `credential_value` | `credentialValue` | `credentialValue` | 是 | 凭据值 |
| `status` | `status` | `status` | 是 | 凭据状态 |
| `need_change_password` | `needChangePassword` | `needChangePassword` | 是 | 是否需要强制改密 |
| `failed_count` | `failedCount` | `failedCount` | 是 | 连续失败次数 |
| `failed_limit` | `failedLimit` | `failedLimit` | 是 | 最大允许连续失败次数 |
| `locked_until` | `lockedUntil` | `lockedUntil` | 否 | 锁定截止时间 |
| `expires_at` | `expiresAt` | `expiresAt` | 否 | 过期时间 |
| `last_verified_at` | `lastVerifiedAt` | `lastVerifiedAt` | 否 | 最近验证时间 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 MyBatis-Plus `IdType.ASSIGN_UUID` 生成。
- `user_id` 来源是 `sys_user.id`。
- `identity_id` 来源是 `auth_user_identity.id`。
- `credential_type` 固定写入 `PASSWORD`。
- `credential_value` 固定保存密码哈希。
- `credential_value` 不保存密码明文。
- `status` 固定写入 `ACTIVE`、`LOCKED`、`EXPIRED` 或 `DISABLED`。
- `need_change_password` 固定使用 `tinyint(1)` 或项目既有等价写法。
- `failed_count` 默认值固定为 `0`。
- `failed_limit` 默认值固定来自后台登录配置。
- `locked_until` 为空时，非锁定状态不受时间锁限制。
- `expires_at` 为空时，凭据不过期。
- `PASSWORD` 类型凭据迁移期来源是 `sys_user.login_pass` 或 `sys_user_encrypt.login_pass`。

索引：

- 主键：`pk_auth_user_credential(id)`
- 联合唯一索引：`uk_auth_user_credential_identity_type(identity_id, credential_type)`
- 普通索引：`idx_auth_user_credential_user(user_id, status)`
- 普通索引：`idx_auth_user_credential_identity_status(identity_id, status)`
- 普通索引：`idx_auth_user_credential_locked(locked_until)`

### 6.3 auth_session

`auth_session` 保存后台认证会话审计事实。活跃会话运行态固定保存在 Redis，不通过本表承接逐请求 touch。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 认证会话主键 |
| `session_id` | `sessionId` | `sessionId` | 是 | 认证会话标识 |
| `token` | `token` | `token` | 是 | 访问 token |
| `user_id` | `userId` | `userId` | 是 | 后台用户 ID |
| `identity_id` | `identityId` | `identityId` | 是 | 登录标识 ID |
| `identity_type` | `identityType` | `identityType` | 是 | 登录标识类型 |
| `login_type` | `loginType` | `loginType` | 是 | 登录方式 |
| `status` | `status` | `status` | 是 | 会话状态 |
| `issued_at` | `issuedAt` | `issuedAt` | 是 | 签发时间 |
| `last_access_time` | `lastAccessTime` | `lastAccessTime` | 是 | 最近访问时间 |
| `expire_at` | `expireAt` | `expireAt` | 是 | 过期时间 |
| `logout_at` | `logoutAt` | `logoutAt` | 否 | 登出时间 |
| `invalidate_reason` | `invalidateReason` | `invalidateReason` | 否 | 失效原因 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 MyBatis-Plus `IdType.ASSIGN_UUID` 生成。
- `session_id` 由 Service 生成，作为认证会话业务标识。
- `token` 来源是 `AccessToken.token`。
- `user_id` 来源是 `sys_user.id`。
- `identity_id` 来源是 `auth_user_identity.id`。
- `identity_type` 固定写入登录时使用的标识类型。
- `login_type` 固定写入 `PASSWORD`。
- `status` 固定写入 `ACTIVE`、`LOGGED_OUT`、`INVALIDATED` 或 `EXPIRED`。
- `issued_at` 和 `last_access_time` 创建时固定相同。
- `last_access_time` 不随每次有效请求直接更新，登出、失效或过期收口时从 Redis 运行态回写最终最近访问时间。
- `expire_at` 来源是 token 或认证会话有效期策略。
- `logout_at` 只在主动登出时写入。
- `invalidate_reason` 只在安全策略失效时写入。

索引：

- 主键：`pk_auth_session(id)`
- 唯一索引：`uk_auth_session_session_id(session_id)`
- 唯一索引：`uk_auth_session_token(token)`
- 普通索引：`idx_auth_session_user_status(user_id, status)`
- 普通索引：`idx_auth_session_identity(identity_id, identity_type)`
- 普通索引：`idx_auth_session_expire(status, expire_at)`

### 6.4 auth_oauth_client

`auth_oauth_client` 保存 OAuth2 客户端配置。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 客户端主键 |
| `client_id` | `clientId` | `clientId` | 是 | 客户端标识 |
| `client_secret_hash` | `clientSecretHash` | `clientSecretHash` | 是 | 客户端密钥哈希 |
| `client_name` | `clientName` | `clientName` | 是 | 客户端名称 |
| `client_type` | `clientType` | `clientType` | 是 | 客户端类型 |
| `grant_types` | `grantTypes` | `grantTypes` | 是 | 授权类型集合 |
| `scopes` | `scopes` | `scopes` | 是 | scope 集合 |
| `redirect_uris` | `redirectUris` | `redirectUris` | 是 | 回调地址集合 |
| `access_token_ttl_seconds` | `accessTokenTtlSeconds` | `accessTokenTtlSeconds` | 是 | access token TTL |
| `refresh_token_ttl_seconds` | `refreshTokenTtlSeconds` | `refreshTokenTtlSeconds` | 是 | refresh token TTL |
| `enabled` | `enabled` | `enabled` | 是 | 启用标记 |
| `contact` | `contact` | `contact` | 否 | 联系方式 |
| `remark` | `remark` | `remark` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |

索引：

- 主键：`pk_auth_oauth_client(id)`
- 唯一索引：`uk_auth_oauth_client_client_id(client_id)`

### 6.5 auth_oauth_authorization

`auth_oauth_authorization` 保存 OAuth2 授权请求和授权码事实。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 授权主键 |
| `authorization_code` | `authorizationCode` | `authorizationCode` | 是 | 授权码 |
| `client_id` | `clientId` | `clientId` | 是 | 客户端标识 |
| `user_id` | `userId` | `userId` | 是 | 用户标识 |
| `redirect_uri` | `redirectUri` | `redirectUri` | 是 | 回调地址 |
| `scopes` | `scopes` | `scopes` | 是 | 授权范围集合 |
| `state` | `state` | `state` | 否 | OAuth2 state |
| `code_challenge` | `codeChallenge` | `codeChallenge` | 否 | PKCE challenge |
| `code_challenge_method` | `codeChallengeMethod` | `codeChallengeMethod` | 否 | PKCE challenge method |
| `issued_at` | `issuedAt` | `issuedAt` | 是 | 签发时间 |
| `expire_at` | `expireAt` | `expireAt` | 是 | 过期时间 |
| `used` | `used` | `used` | 是 | 是否已消费 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |

索引：

- 主键：`pk_auth_oauth_authorization(id)`
- 唯一索引：`uk_auth_oauth_authorization_code(authorization_code)`
- 普通索引：`idx_auth_oauth_authorization_client_user(client_id, user_id, expire_at)`

### 6.6 auth_oauth_access_token

`auth_oauth_access_token` 保存 OAuth2 access token 事实。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | access token 主键 |
| `token_id` | `tokenId` | `tokenId` | 是 | token 标识 |
| `token_hash` | `tokenHash` | `tokenHash` | 是 | token 哈希 |
| `client_id` | `clientId` | `clientId` | 是 | 客户端标识 |
| `user_id` | `userId` | `userId` | 是 | 用户标识 |
| `scopes` | `scopes` | `scopes` | 是 | 授权范围集合 |
| `issued_at` | `issuedAt` | `issuedAt` | 是 | 签发时间 |
| `expire_at` | `expireAt` | `expireAt` | 是 | 过期时间 |
| `status` | `status` | `status` | 是 | token 状态 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |

索引：

- 主键：`pk_auth_oauth_access_token(id)`
- 唯一索引：`uk_auth_oauth_access_token_id(token_id)`
- 唯一索引：`uk_auth_oauth_access_token_hash(token_hash)`
- 普通索引：`idx_auth_oauth_access_token_client_user(client_id, user_id, status)`

### 6.7 auth_oauth_refresh_token

`auth_oauth_refresh_token` 保存 refresh token 事实。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | refresh token 主键 |
| `token_id` | `tokenId` | `tokenId` | 是 | token 标识 |
| `token_hash` | `tokenHash` | `tokenHash` | 是 | token 哈希 |
| `access_token_id` | `accessTokenId` | `accessTokenId` | 是 | 关联 access token 标识 |
| `client_id` | `clientId` | `clientId` | 是 | 客户端标识 |
| `user_id` | `userId` | `userId` | 是 | 用户标识 |
| `issued_at` | `issuedAt` | `issuedAt` | 是 | 签发时间 |
| `expire_at` | `expireAt` | `expireAt` | 是 | 过期时间 |
| `status` | `status` | `status` | 是 | token 状态 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |

索引：

- 主键：`pk_auth_oauth_refresh_token(id)`
- 唯一索引：`uk_auth_oauth_refresh_token_id(token_id)`
- 唯一索引：`uk_auth_oauth_refresh_token_hash(token_hash)`
- 普通索引：`idx_auth_oauth_refresh_token_client_user(client_id, user_id, status)`

## 7. Relationship Rules

- `auth_user_identity.user_id` 引用 `sys_user.id`。
- `auth_user_credential.user_id` 引用 `sys_user.id`。
- `auth_user_credential.identity_id` 引用 `auth_user_identity.id`。
- `auth_session.user_id` 引用 `sys_user.id`。
- `auth_session.identity_id` 引用 `auth_user_identity.id`。
- `auth_session.token` 引用访问 token 存储中的 token 值。
- `auth_oauth_authorization.client_id` 引用 `auth_oauth_client.client_id`。
- `auth_oauth_authorization.user_id` 引用 `sys_user.id`。
- `auth_oauth_access_token.client_id` 引用 `auth_oauth_client.client_id`。
- `auth_oauth_access_token.user_id` 引用 `sys_user.id`。
- `auth_oauth_refresh_token.client_id` 引用 `auth_oauth_client.client_id`。
- `auth_oauth_refresh_token.user_id` 引用 `sys_user.id`。
- 当前项目不强制数据库外键。
- 用户创建时，Service 必须先保存 `sys_user`，再保存 `auth_user_identity` 和 `auth_user_credential`。
- 修改登录名时，Service 必须更新 `ACCOUNT` 类型 `auth_user_identity`。
- 重置密码时，Service 必须更新 `PASSWORD` 类型 `auth_user_credential`。
- 删除 token 或登出时，Service 必须更新对应 `auth_session` 状态。

## 8. Persistence Rules

- `UserIdentityMapper` 固定继承 `BaseMapper<UserIdentityDO>`。
- `UserCredentialMapper` 固定继承 `BaseMapper<UserCredentialDO>`。
- `AuthSessionMapper` 固定继承 `BaseMapper<AuthSessionDO>`。
- `OAuthClientMapper` 固定继承 `BaseMapper<OAuthClientDO>`。
- `OAuthAuthorizationMapper` 固定继承 `BaseMapper<OAuthAuthorizationDO>`。
- `OAuthAccessTokenMapper` 固定继承 `BaseMapper<OAuthAccessTokenDO>`。
- `OAuthRefreshTokenMapper` 固定继承 `BaseMapper<OAuthRefreshTokenDO>`。
- Mapper interface 不新增注解 SQL、Mapper XML 或 SQL Provider。
- `UserIdentityDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `UserCredentialDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `AuthSessionDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `OAuthClientDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `OAuthAuthorizationDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `OAuthAccessTokenDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `OAuthRefreshTokenDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `UserIdentityPersistenceAssembler` 只负责 `UserIdentity <-> UserIdentityDO` 转换。
- `UserCredentialPersistenceAssembler` 只负责 `UserCredential <-> UserCredentialDO` 转换。
- `AuthSessionPersistenceAssembler` 只负责 `AuthSession <-> AuthSessionDO` 转换。
- `OAuthClientPersistenceAssembler` 只负责 `OAuthClient <-> OAuthClientDO` 转换。
- `OAuthAuthorizationPersistenceAssembler` 只负责 `OAuthAuthorization <-> OAuthAuthorizationDO` 转换。
- `OAuthAccessTokenPersistenceAssembler` 只负责 `OAuthAccessToken <-> OAuthAccessTokenDO` 转换。
- `OAuthRefreshTokenPersistenceAssembler` 只负责 `OAuthRefreshToken <-> OAuthRefreshTokenDO` 转换。
- `PersistenceAssembler` 不调用 Service、DAO 或 Mapper。
- DAO insert 后必须返回持久化主键。
- Service 负责把 DAO insert 返回主键回填到业务 Entity。
- Service 不直接操作 `DO/DataObject.id`。
- Controller 不直接依赖 DAO、Mapper、`DO/DataObject` 或 `PersistenceAssembler`。

## 9. Query Model Rules

`UserIdentityDao` 固定支持以下查询：

- 按 `id` 查询。
- 按 `identityType + identityValue` 查询。
- 按 `userId + identityType` 查询。
- 按 `userId + status` 查询。

`UserCredentialDao` 固定支持以下查询：

- 按 `id` 查询。
- 按 `identityId + credentialType` 查询。
- 按 `userId + credentialType` 查询。
- 按 `userId + status` 查询。
- 写回失败次数、锁定状态和最近验证时间。

`AuthSessionDao` 固定支持以下查询：

- 按 `id` 查询。
- 按 `sessionId` 查询。
- 按 `token` 查询。
- 按 `userId + status` 查询。
- 写回最近访问时间。
- 写回登出状态。
- 写回失效状态。
- 写回过期状态。
- 按 `userId + status` 批量失效。

`OAuthClientDao` 固定支持以下查询：

- 按 `id` 查询。
- 按 `clientId` 查询。
- 按 `clientId + enabled` 查询。

`OAuthAuthorizationDao` 固定支持以下查询：

- 按 `authorizationCode` 查询。
- 写回授权码已使用状态。
- 删除或撤销授权请求。

`OAuthAccessTokenDao` 固定支持以下查询：

- 按 `id` 查询。
- 按 `tokenId` 查询。
- 按 `tokenHash` 查询。
- 写回 `REVOKED` 和 `EXPIRED` 状态。

`OAuthRefreshTokenDao` 固定支持以下查询：

- 按 `tokenId` 查询。
- 按 `tokenHash` 查询。
- 按 `clientId + userId + status` 查询。
- 写回 `USED`、`REVOKED` 和 `EXPIRED` 状态。

分页规则：

- 后台会话列表分页查询按 `issued_at` 降序。
- 后台身份和凭据管理分页查询按 `create_date` 降序。
- 分页参数有效性由 Service 校验。
- DAO implementation 只按已校验参数执行持久化分页。

## 10. Migration Rules

- `auth_user_identity(ACCOUNT)` 初始化来源是 `sys_user.login_name`。
- `auth_user_identity(MOBILE)` 初始化来源是 `sys_user.mobile` 或 `sys_user_encrypt.mobile`。
- `auth_user_identity(EMAIL)` 初始化来源是 `sys_user.email` 或 `sys_user_encrypt.email`。
- `auth_user_credential(PASSWORD)` 初始化来源是 `sys_user.login_pass` 或 `sys_user_encrypt.login_pass`。
- 迁移期必须保证一个可登录后台用户至少拥有一个 `ACCOUNT` 类型 `UserIdentity`。
- 迁移期必须保证一个可登录后台用户至少拥有一个 `PASSWORD` 类型 `UserCredential`。
- 登录链路切换完成后，不得继续直接读取 `sys_user.login_pass` 执行密码认证。
- 旧账号维度锁定状态迁移完成后，不得继续作为后台认证主锁定语义。

## 11. Open Items

无
