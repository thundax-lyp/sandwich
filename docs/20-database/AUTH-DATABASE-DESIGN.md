# AUTH DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich 后台认证、前台会员认证、用户登录标识、用户认证凭据、登录事件和 OAuth2 授权模型的数据库表、字段映射、关系约束和持久化规则。

本文档以 `AUTH-REQUIREMENTS.md` 的后台认证和前台会员认证模型为基础，固定 `auth` 拥有的 `PrincipalIdentity`、`PrincipalCredential`、`PrincipalLoginEvent` 和 OAuth2 模型的目标持久化设计。建表 SQL 见 [`../../db/schema/auth.sql`](../../db/schema/auth.sql)，初始化脚本见 [`../../db/data/auth.sql`](../../db/data/auth.sql)。

后台系统管理域的完整 sys 表设计见 [`SYSTEM-DATABASE-DESIGN.md`](./SYSTEM-DATABASE-DESIGN.md)。登录标识和认证凭据固定由本文档的 Principal 表承载。

## 2. Scope

当前覆盖范围：

- `auth_principal_identity`
- `auth_principal_credential`
- `auth_principal_login_event`
- `auth_oauth_client`
- `auth_oauth_authorization`
- `PrincipalIdentityDO`
- `PrincipalCredentialDO`
- `PrincipalLoginEventDO`
- `OAuthClientDO`
- `OAuthAuthorizationDO`
- `PrincipalIdentityMapper`
- `PrincipalCredentialMapper`
- `PrincipalLoginEventMapper`
- `OAuthClientMapper`
- `OAuthAuthorizationMapper`
- `PrincipalIdentityDaoImpl`
- `PrincipalCredentialDaoImpl`
- `PrincipalLoginEventDaoImpl`
- `OAuthClientDaoImpl`
- `OAuthAuthorizationDaoImpl`
- `PrincipalIdentityPersistenceAssembler`
- `PrincipalCredentialPersistenceAssembler`
- `PrincipalLoginEventPersistenceAssembler`
- `OAuthClientPersistenceAssembler`
- `OAuthAuthorizationPersistenceAssembler`

当前不覆盖范围：

- `PreAuthSession` 只使用 Redis / JetCache 运行态，不建立数据库表。
- `PrincipalAccessToken` 和 `PrincipalRefreshToken` 只使用 Redis / JetCache 运行态，不建立数据库表。
- `PrincipalAuthSession` 只使用 Redis / JetCache 运行态，不建立数据库表。
- MFA 凭据表。
- 生产数据变更脚本。

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- 存储引擎优先使用 `InnoDB`。
- 字符集优先使用 `utf8mb4`。
- 独立数据库表主键数据库类型固定为 `bigint`，Java 类型固定为 `Long`。
- 独立数据库表主键由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `identity_id` 固定引用 `auth_principal_identity.id`。
- `principal_type + principal_id` 固定表达统一认证主体业务坐标。
- `auth_principal_identity.identity_id` 不作为外部字段存在，统一登录标识主键仍使用 `id`。
- `auth_principal_credential.identity_id` 固定引用 `auth_principal_identity.id`。
- 枚举字段使用 `varchar` 存储。
- 集合字段优先使用 JSON 字符串表达，由持久化装配器负责转换。
- 敏感字段不得明文落库。
- `credential_value` 固定保存密码哈希，不保存密码明文。
- `client_secret_hash` 固定保存客户端密钥哈希，不保存客户端密钥明文。
- `token_hash` 固定保存 OAuth token 的 SHA-256 Base64Url 哈希，不保存 token 明文。
- 禁用、锁定、登出和失效通过状态字段表达。
- `DO/DataObject` 不暴露给 Controller 或 Service。

## 4. Naming Rules

- 统一认证主体登录标识表固定为 `auth_principal_identity`。
- 统一认证主体凭据表固定为 `auth_principal_credential`。
- 统一认证登录事件表固定为 `auth_principal_login_event`。
- OAuth 客户端表固定为 `auth_oauth_client`。
- OAuth 授权表固定为 `auth_oauth_authorization`。
- 主键字段固定为 `id`。
- 统一认证主体类型字段固定为 `principal_type`。
- 统一认证主体 ID 字段固定为 `principal_id`。
- 登录标识主键字段固定为 `identity_id`。
- 登录标识类型字段固定为 `identity_type`。
- 登录标识值字段固定为 `identity_value`。
- 凭据类型字段固定为 `credential_type`。
- 凭据值字段固定为 `credential_value`。
- 状态字段固定为 `status`。
- 配置类表使用 `create_date`、`create_by`、`update_date`、`update_by`；会话事实和授权码事实不使用通用审计字段。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `auth_principal_identity` | `PrincipalIdentityDO` | `PrincipalIdentityMapper` | `PrincipalIdentity` |
| `auth_principal_credential` | `PrincipalCredentialDO` | `PrincipalCredentialMapper` | `PrincipalCredential` |
| `auth_principal_login_event` | `PrincipalLoginEventDO` | `PrincipalLoginEventMapper` | `PrincipalLoginEvent` |
| `auth_oauth_client` | `OAuthClientDO` | `OAuthClientMapper` | `OAuthClient` |
| `auth_oauth_authorization` | `OAuthAuthorizationDO` | `OAuthAuthorizationMapper` | `OAuthAuthorization` |

## 6. Table Design

### 6.1 auth_principal_identity

`auth_principal_identity` 保存统一认证主体登录标识。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 登录标识主键 |
| `principal_type` | `principalType` | `principalKey.principalType` | 是 | 主体类型 |
| `principal_id` | `principalId` | `principalKey.principalId` | 是 | 主体 ID |
| `identity_type` | `identityType` | `type` | 是 | 登录标识类型 |
| `identity_value` | `identityValue` | `identityValue` | 是 | 登录标识值 |
| `status` | `status` | `status` | 是 | 登录标识状态 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- 后台用户固定写入 `principal_type=USER`、`principal_id=sys_user.id`。
- 前台会员固定写入 `principal_type=MEMBER`、`principal_id=member_member.id`。
- `identity_type` 固定写入 `USER_ACCOUNT`、`USER_MOBILE`、`USER_EMAIL`、`USER_WECOM`、`USER_GITHUB`、`MEMBER_ACCOUNT`、`MEMBER_MOBILE` 或 `MEMBER_EMAIL`。
- `status` 固定写入 `ENABLED` 或 `DISABLED`。
- `identity_value` 必须保存规范化后的登录标识值。
- 本表不保存通用审计字段。

索引：

- 主键：`pk_auth_principal_identity(id)`
- 联合唯一索引：`uk_auth_principal_identity_type_value(identity_type, identity_value)`
- 普通索引：`idx_auth_principal_identity_principal(principal_type, principal_id, status)`
- 普通索引：`idx_auth_principal_identity_principal_type(principal_type, principal_id, identity_type)`

### 6.2 auth_principal_credential

`auth_principal_credential` 保存统一认证主体认证凭据。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 认证凭据主键 |
| `principal_type` | `principalType` | `principalKey.principalType` | 是 | 主体类型 |
| `principal_id` | `principalId` | `principalKey.principalId` | 是 | 主体 ID |
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

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `principal_type + principal_id` 必须与关联 `auth_principal_identity` 的主体坐标一致。
- `identity_id` 来源是 `auth_principal_identity.id`。
- `credential_type` 固定写入 `USER_PASSWORD` 或 `MEMBER_PASSWORD`。
- `credential_value` 固定保存密码哈希，不保存密码明文。
- `status` 固定写入 `ACTIVE`、`LOCKED`、`EXPIRED` 或 `DISABLED`。
- `need_change_password` 固定使用 `tinyint(1)` 或项目既有等价写法。
- 本表不保存通用审计字段。

索引：

- 主键：`pk_auth_principal_credential(id)`
- 联合唯一索引：`uk_auth_principal_credential_identity_type(identity_id, credential_type)`
- 普通索引：`idx_auth_principal_credential_principal(principal_type, principal_id, status)`
- 普通索引：`idx_auth_principal_credential_identity_status(identity_id, status)`
- 普通索引：`idx_auth_principal_credential_locked(locked_until)`

### 6.3 auth_principal_login_event

`auth_principal_login_event` 保存统一认证登录事件审计事实。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 登录事件主键 |
| `principal_type` | `principalType` | `principalKey.principalType` | 否 | 主体类型 |
| `principal_id` | `principalId` | `principalKey.principalId` | 否 | 主体 ID |
| `client_id` | `clientId` | `clientId` | 是 | 客户端标识 |
| `event_type` | `eventType` | `eventType` | 是 | 登录事件类型 |
| `authentication_method` | `authenticationMethod` | `authenticationMethod` | 是 | 认证方式 |
| `identity_type` | `identityType` | `identityType` | 否 | 登录标识类型 |
| `occurred_at` | `occurredAt` | `occurredAt` | 是 | 发生时间 |
| `ip` | `ip` | `ip` | 否 | 请求 IP |
| `user_agent` | `userAgent` | `userAgent` | 否 | 请求 User-Agent |
| `reason` | `reason` | `reason` | 否 | 固定原因值 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成十六进制字符串。
- `principal_type + principal_id` 登录失败且无法识别主体时允许为空。
- `client_id` 来源是认证入口对应客户端。
- `event_type` 固定写入 `LOGIN_SUCCESS`、`LOGIN_FAILED`、`LOGOUT`、`TOKEN_REFRESH` 或 `OAUTH_AUTHORIZED`。
- `authentication_method` 固定写入 `PASSWORD`、`SMS_CODE`、`EMAIL_CODE`、`GITHUB`、`WECOM`、`OAUTH_CODE` 或 `REFRESH_TOKEN`。
- `reason` 使用 `PrincipalLoginEvent` 固定原因常量，不记录异常堆栈。

索引：

- 主键：`pk_auth_principal_login_event(id)`
- 普通索引：`idx_auth_principal_login_event_principal_time(principal_type, principal_id, occurred_at)`
- 普通索引：`idx_auth_principal_login_event_client_time(client_id, occurred_at)`
- 普通索引：`idx_auth_principal_login_event_type_time(event_type, occurred_at)`

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
| `status` | `status` | `status` | 是 | 客户端状态 |
| `contact` | `contact` | `contact` | 否 | 联系方式 |
| `remark` | `remark` | `remark` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `client_id` 由 Service 生成，作为 OAuth 客户端业务标识。
- `client_secret_hash` 固定保存客户端密钥哈希。

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
| `principal_type` | `principalType` | `principalKey.principalType` | 是 | 主体类型 |
| `principal_id` | `principalId` | `principalKey.principalId` | 是 | 主体 ID |
| `redirect_uri` | `redirectUri` | `redirectUri` | 是 | 回调地址 |
| `scopes` | `scopes` | `scopes` | 是 | 授权范围集合 |
| `state` | `state` | `state` | 否 | OAuth2 state |
| `code_challenge` | `codeChallenge` | `codeChallenge` | 否 | PKCE challenge |
| `code_challenge_method` | `codeChallengeMethod` | `codeChallengeMethod` | 否 | PKCE challenge method |
| `issued_at` | `issuedAt` | `issuedAt` | 是 | 签发时间 |
| `expire_at` | `expireAt` | `expireAt` | 是 | 过期时间 |
| `used` | `used` | `used` | 是 | 是否已消费 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `authorization_code` 由 Service 生成，作为 OAuth 授权码业务标识。
- `client_id` 来源是 `auth_oauth_client.client_id`。
- `principal_type + principal_id` 表达统一认证主体业务坐标。

索引：

- 主键：`pk_auth_oauth_authorization(id)`
- 唯一索引：`uk_auth_oauth_authorization_code(authorization_code)`
- 普通索引：`idx_auth_oauth_authorization_client_principal(client_id, principal_type, principal_id, expire_at)`

## 7. Relationship Rules

- `auth_principal_identity.principal_type + principal_id` 表达统一认证主体业务坐标。
- `auth_principal_credential.principal_type + principal_id` 表达统一认证主体业务坐标。
- `auth_principal_credential.identity_id` 引用 `auth_principal_identity.id`。
- `auth_oauth_authorization.client_id` 引用 `auth_oauth_client.client_id`。
- `auth_oauth_authorization.principal_type + principal_id` 表达统一认证主体业务坐标。
- 当前项目不强制数据库外键。
- 用户创建时，Service 必须先保存 `sys_user`，再保存 `auth_principal_identity` 和 `auth_principal_credential`。
- 修改登录名时，Service 必须更新 `USER_ACCOUNT` 类型 `auth_principal_identity`。
- 重置密码时，Service 必须更新 `USER_PASSWORD` 类型 `auth_principal_credential`。
- 登录成功、登录失败、登出、刷新 token 和 OAuth 授权时，Service 必须写入 `auth_principal_login_event`。

## 8. Persistence Rules

- `PrincipalIdentityMapper` 固定继承 `BaseMapper<PrincipalIdentityDO>`。
- `PrincipalCredentialMapper` 固定继承 `BaseMapper<PrincipalCredentialDO>`。
- `PrincipalLoginEventMapper` 固定继承 `BaseMapper<PrincipalLoginEventDO>`。
- `OAuthClientMapper` 固定继承 `BaseMapper<OAuthClientDO>`。
- `OAuthAuthorizationMapper` 固定继承 `BaseMapper<OAuthAuthorizationDO>`。
- Mapper interface 不新增注解 SQL、Mapper XML 或 SQL Provider。
- `PrincipalIdentityDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `PrincipalCredentialDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `PrincipalLoginEventDaoImpl` 固定通过 MyBatis-Plus 插入登录事件事实。
- `OAuthClientDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `OAuthAuthorizationDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询和更新。
- `PrincipalAccessTokenDaoImpl` 固定通过 Redis / JetCache 保存 access token 运行态和 token hash 索引。
- `PrincipalRefreshTokenDaoImpl` 固定通过 Redis / JetCache 保存 refresh token 运行态和 token hash 索引。
- `PrincipalIdentityPersistenceAssembler` 只负责 `PrincipalIdentity <-> PrincipalIdentityDO` 转换。
- `PrincipalCredentialPersistenceAssembler` 只负责 `PrincipalCredential <-> PrincipalCredentialDO` 转换。
- `PrincipalLoginEventPersistenceAssembler` 只负责 `PrincipalLoginEvent <-> PrincipalLoginEventDO` 转换。
- `OAuthClientPersistenceAssembler` 只负责 `OAuthClient <-> OAuthClientDO` 转换。
- `OAuthAuthorizationPersistenceAssembler` 只负责 `OAuthAuthorization <-> OAuthAuthorizationDO` 转换。
- `PersistenceAssembler` 不调用 Service、DAO 或 Mapper。
- DAO insert 后必须返回持久化主键。
- Service 负责把 DAO insert 返回主键回填到业务 Entity。
- Service 不直接操作 `DO/DataObject.id`。
- Controller 不直接依赖 DAO、Mapper、`DO/DataObject` 或 `PersistenceAssembler`。

## 9. Query Model Rules

`PrincipalIdentityDao` 固定支持以下查询：

- 按 `id` 查询。
- 按 `identityType + identityValue` 查询。
- 按 `principalKey + identityType` 查询。
- 按 `principalKey + status` 查询。

`PrincipalCredentialDao` 固定支持以下查询：

- 按 `id` 查询。
- 按 `identityId + credentialType` 查询。
- 按 `principalKey + credentialType` 查询。
- 按 `principalKey + status` 查询。
- 写回失败次数、锁定状态和最近验证时间。

`OAuthClientDao` 固定支持以下查询：

- 按 `id` 查询。
- 按 `clientId` 查询。
- 按 `clientId + status` 查询。

`OAuthAuthorizationDao` 固定支持以下查询：

- 按 `authorizationCode` 查询。
- 写回授权码已使用状态。
- 删除或撤销授权请求。

`PrincipalAccessTokenDao` 固定支持以下查询：

- 按 `id` 查询。
- 按 `tokenId` 查询。
- 按 `tokenHash` 查询。
- 写回 `REVOKED` 和 `EXPIRED` 状态。

`PrincipalRefreshTokenDao` 固定支持以下查询：

- 按 `tokenId` 查询。
- 按 `tokenHash` 查询。
- 按 `principalKey + clientId + status` 查询。
- 写回 `USED`、`REVOKED` 和 `EXPIRED` 状态。

分页规则：

- 后台身份和凭据管理分页查询按 `create_date` 降序。
- 分页参数有效性由 Service 校验。
- DAO implementation 只按已校验参数执行持久化分页。

## 10. Initialization Rules

- `auth_principal_identity(USER_ACCOUNT)` 初始化来源是后台用户保存请求中的 `loginName`。
- `auth_principal_identity(USER_MOBILE)` 初始化来源是 `sys_user.mobile` 或显式移动端登录标识写入。
- `auth_principal_identity(USER_EMAIL)` 初始化来源是 `sys_user.email` 或显式邮箱登录标识写入。
- `auth_principal_credential(USER_PASSWORD)` 初始化来源是后台用户保存请求中的加密后密码。
- 必须保证一个可登录后台用户至少拥有一个 `USER_ACCOUNT` 类型 `PrincipalIdentity`。
- 必须保证一个可登录后台用户至少拥有一个 `USER_PASSWORD` 类型 `PrincipalCredential`。
- 密码认证必须读取 `auth_principal_credential.credential_value`。
- 后台认证主锁定语义必须落在 `PrincipalCredential` 维度。

## 11. Open Items

无
