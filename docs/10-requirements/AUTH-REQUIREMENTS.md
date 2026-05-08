# AUTH REQUIREMENTS

## 1. Purpose

本文档定义 Sandwich 后台认证、前台会员认证、OAuth2 授权和多登录方式的业务需求边界。

`Auth` 负责后台登录表单、验证码、密码认证、短信登录、第三方登录适配、登录标识解析、认证凭据状态流转、访问 token 生命周期、refresh token 生命周期、OAuth2 client、authorization、token verify、introspection、userinfo 和认证会话审计。后台认证固定区分用户主体、登录标识、认证凭据、访问 token、refresh token 和登录会话。

后台用户主体管理需求见 [`SYSTEM-REQUIREMENTS.md`](./SYSTEM-REQUIREMENTS.md)。登录标识和认证凭据归属 `Auth`，本文档定义这些资料在认证流程中的使用规则。

统一认证业务结构固定以 `Principal*` 模型描述后台用户认证和前台会员认证的共性。`Principal*` 不替代 `User`、`Member` 或入口安全上下文，只用于在 `auth` 域内统一表达认证主体、登录标识、认证凭据和 token 运行态。

## 2. Scope

当前覆盖范围：

- 后台账号密码登录。
- 后台短信登录。
- 后台企业微信登录。
- 后台 GitHub 登录。
- 后台登录表单和验证码。
- 后台访问 token 创建、校验、续期和删除。
- 后台 refresh token 创建、校验、轮换和失效。
- 后台权限会话创建、touch 和释放。
- 后台用户登录标识管理。
- 后台用户密码凭据管理。
- 后台认证会话创建、touch、登出、失效和过期。
- 后台认证会话按 token 和用户失效。
- OAuth2 client 查询和密钥校验。
- OAuth2 authorization code 授权、决策、撤销和换 token。
- OAuth2 access token 签发、状态流转、撤销和 introspection。
- OAuth2 token verify、introspection 和 userinfo。
- 后台用户创建、修改登录名、重置密码时的认证前置数据维护。

当前不覆盖范围：

- MFA 二次认证。
- 完整 OIDC discovery、JWKS 和动态客户端注册。
- Spring Security 权限整改。
- 用户、角色、菜单授权模型重做。
- 生产数据变更执行。

## 3. Bounded Context

后台认证目标模型固定为：

`User -> PrincipalIdentity -> PrincipalCredential -> AuthSession`

`User` 归属 `sys` 用户主体，承载后台用户资料、组织关系、权限等级、启停状态和审计字段。

`PrincipalIdentity` 归属 `auth` 认证模型，承载后台用户和前台会员的登录标识。一个 `PrincipalKey` 可以绑定多个 `PrincipalIdentity`。

`PrincipalCredential` 归属 `auth` 认证模型，承载后台用户和前台会员的认证凭据。一个 `PrincipalIdentity` 可以绑定多个 `PrincipalCredential`。

`AuthSession` 归属 `auth` 认证模型，承载后台登录后的会话事实。

`AccessToken` 继续承载后台访问 token。`OAuthAccessToken` 承载 OAuth2 access token 事实。`OAuthRefreshToken` 承载 refresh token 事实。`AuthSession` 固定不替代 token 的传输职责。

`PermissionSession` 继续承载权限集合缓存。`AuthSession` 固定不承载权限集合。

`OAuthClient` 归属 `auth` 认证模型，承载 OAuth2 客户端配置、密钥哈希、授权类型、scope、redirect uri 和 token TTL 策略。

`OAuthAuthorization` 归属 `auth` 认证模型，承载 OAuth2 授权请求、授权码、PKCE 参数、授权范围、决策状态和一次性消费状态。

`OAuthAccessToken` 归属 `auth` 认证模型，承载 OAuth2 access token、客户端、用户、授权范围、过期和失效状态。

`OAuthRefreshToken` 归属 `auth` 认证模型，承载 refresh token、关联访问 token、客户端、用户、过期和失效状态。

前台会员认证运行态也归属 `auth` 认证模型。`MemberLoginForm`、`MemberAuthSession`、`MemberAccessToken` 和 `MemberRefreshToken` 保留 `Member` 前缀，用于区分后台用户认证模型和前台会员认证模型。

后台和前台认证的共性结构固定为：

`PrincipalKey -> PrincipalIdentity -> PrincipalCredential -> PrincipalAccessToken / PrincipalRefreshToken`

`PrincipalKey` 由 `principalType` 和 `principalId` 组成。`principalType=USER` 表示后台用户主体，`principalType=MEMBER` 表示前台会员主体。`principalId` 必须指向对应主体主键。`PrincipalIdentity` 统一表达账号、手机号、邮箱等登录标识。`PrincipalCredential` 统一表达密码等认证凭据。`PrincipalAccessToken` 和 `PrincipalRefreshToken` 统一表达面向主体的访问 token 与刷新 token 运行态。

固定约束：

- `Principal*` 是 auth 域内部统一结构描述，不作为公开 HTTP Request / Response。
- `PrincipalKey.principalType + principalId` 必须唯一定位业务主体；后台固定为 `USER + User.id`，前台固定为 `MEMBER + Member.id`。
- 后台上下文仍由 `UserAccessHolder` 建立和读取，前台上下文仍由 `MemberSecurityContext` 建立和读取。
- API 入口模块只做 HTTP、安全框架、第三方 provider、权限会话和响应装配适配；可复用的认证业务流程优先收敛到 `sandwish-biz` 的 `AuthService`。
- `AuthService` 进入 `sandwish-biz` 后不得直接依赖 Servlet、Spring Security `Authentication`、API Request / Response、`PermissionService` 或入口模块 provider。

## 4. Module Mapping

- `sandwish-biz/src/main/java/com/github/thundax/modules/auth`
  - 定义 `AuthSession`、OAuth2 模型、token 模型、`Principal*` 统一认证结构、后台登录表单运行态、前台会员认证运行态模型、认证枚举、DAO 契约和可复用认证业务 Service。
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys`
  - 定义后台 `User` 主体、用户保存流程和用户资料维护。
- `sandwish-infra/src/main/java/com/github/thundax/modules/auth`
  - 实现后台认证运行态、前台会员认证运行态和 OAuth2 模型 DAO，维护 DO、Mapper 和持久化转换。
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys`
  - 实现后台用户主体、登录标识和认证凭据 DAO，维护用户资料持久化。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth`
  - 提供后台登录、刷新、验证码、登出、session command、OAuth2、token 认证入口适配、后台权限会话适配 Service 和入口专用第三方登录 provider。
- `sandwish-front-api/src/main/java/com/github/thundax/modules/auth`
  - 提供前台会员登录、注册、验证码、刷新、登出、Spring Security 过滤器、会员上下文和 token 认证入口适配。

## 5. Core Business Objects

### 5.1 User

`User` 是后台用户主体。

核心字段：

- `id`：后台用户 ID，使用 `EntityId`。
- `departmentId`：所属部门 ID。
- `name`：用户名称。
- `email`：联系邮箱。
- `mobile`：联系手机号。
- `tel`：联系电话。
- `rank`：用户访问等级，使用 `AccessRank`；持久化到 `sys_user.ranks`。
- `privilege`：用户权限等级。
- `status`：用户状态。
- `createDate`：创建时间。
- `updateDate`：更新时间。

固定约束：

- `User` 不等于登录标识。
- `User` 不等于认证凭据。
- `User` 不承载注册 IP、最近登录时间、最近登录 IP、登录次数等认证行为数据；认证行为通过认证会话与系统日志落地。
- `User.status` 禁用时，该用户全部后台登录方式不可用。
- `User.loginName` 可作为后台用户创建和账户身份初始化输入，认证语义固定由 `PrincipalIdentity.identityValue` 承载。
- `User.loginPass` 可作为后台用户创建和密码凭据初始化输入，认证语义固定由 `PrincipalCredential.credentialValue` 承载。

### 5.2 PrincipalIdentity

`PrincipalIdentity` 是统一认证主体登录标识。

核心字段：

- `id`：登录标识 ID。
- `principalKey`：认证主体坐标，由 `principalType` 和 `principalId` 组成。
- `identityType`：登录标识类型。
- `identityValue`：登录标识值。
- `status`：登录标识状态。

固定标识类型：

- `USER_ACCOUNT`：后台账号。
- `USER_MOBILE`：后台手机号。
- `USER_EMAIL`：后台邮箱。
- `USER_WECOM`：后台企业微信身份。
- `USER_GITHUB`：后台 GitHub 身份。
- `MEMBER_ACCOUNT`：前台会员账号。
- `MEMBER_MOBILE`：前台会员手机号。
- `MEMBER_EMAIL`：前台会员邮箱。

固定状态：

- `ENABLED`：可用于登录。
- `DISABLED`：不可用于登录。

固定约束：

- `identityType + identityValue` 必须唯一定位一个 `PrincipalIdentity`。
- 禁用 `PrincipalIdentity` 只影响该登录方式，不等于禁用 `User`。
- 修改后台账号固定更新 `USER_ACCOUNT` 类型 `PrincipalIdentity`。
- `PrincipalIdentity` 不保存密码哈希。
- `PrincipalIdentity` 不承载通用审计字段。

### 5.3 PrincipalCredential

`PrincipalCredential` 是统一认证主体认证凭据。

核心字段：

- `id`：认证凭据 ID。
- `identityId`：关联登录标识 ID。
- `credentialType`：凭据类型。
- `credentialValue`：凭据值。
- `status`：凭据状态。
- `needChangePassword`：是否需要强制改密。
- `failedCount`：连续失败次数。
- `failedLimit`：最大允许连续失败次数。
- `lockedUntil`：锁定截止时间。
- `expiresAt`：凭据过期时间。
- `lastVerifiedAt`：最近验证时间。

固定凭据类型：

- `USER_PASSWORD`：后台密码凭据。
- `MEMBER_PASSWORD`：前台会员密码凭据。

固定状态：

- `ACTIVE`：可用于认证。
- `LOCKED`：已锁定。
- `EXPIRED`：已过期。
- `DISABLED`：已禁用。

固定约束：

- 密码哈希固定保存到 `PrincipalCredential.credentialValue`。
- 登录失败次数固定保存到 `PrincipalCredential.failedCount`。
- 凭据锁定固定发生在 `PrincipalCredential` 维度，不发生在 `User` 维度。
- 密码过期固定使用 `PrincipalCredential.expiresAt` 表达。
- 首次登录或重置密码后的强制改密固定使用 `PrincipalCredential.needChangePassword` 表达。
- `identityId + credentialType` 必须唯一定位一个认证凭据。
- `PrincipalCredential` 不承载通用审计字段，只保存凭据认证策略所需状态。

### 5.4 AuthSession

`AuthSession` 是后台认证会话事实，分为 Redis 运行态和数据库审计态。

核心字段：

- `id`：认证会话 ID。
- `sessionId`：认证会话标识。
- `token`：访问 token。
- `userId`：后台用户 ID。
- `identityId`：登录标识 ID。
- `identityType`：登录标识类型。
- `loginType`：登录方式。
- `status`：会话状态。
- `issuedAt`：签发时间。
- `lastAccessTime`：最近访问时间。
- `expireAt`：过期时间。
- `logoutAt`：登出时间。
- `invalidateReason`：失效原因。

固定登录方式：

- `PASSWORD`：账号密码登录。

固定状态：

- `ACTIVE`：活跃。
- `LOGGED_OUT`：已登出。
- `INVALIDATED`：已失效。
- `EXPIRED`：已过期。

固定约束：

- 每次后台登录成功必须创建新的 `AuthSession` 数据库审计记录。
- 每次后台登录成功必须写入对应 `AuthSession` Redis 运行态快照。
- Redis 运行态固定承载活跃会话快照、最近访问时间和 TTL。
- 数据库审计态固定承载登录事实、最终最近访问时间、登出、失效和过期状态。
- 请求 token 有效且刷新访问态时，必须 touch 对应 Redis 运行态 `AuthSession.lastAccessTime`。
- 正常请求不得逐次更新数据库 `auth_session.last_access_time`。
- 主动登出固定将 `AuthSession.status` 更新为 `LOGGED_OUT`。
- 安全策略失效固定将 `AuthSession.status` 更新为 `INVALIDATED`。
- 自然过期固定将 `AuthSession.status` 更新为 `EXPIRED`。
- `AuthSession` 不保存权限集合。

### 5.5 Front Member Auth Runtime

前台会员认证运行态对象归属 auth 域：

- `MemberLoginForm`：前台登录前置临时状态，承载验证码、短信验证码、邮箱验证码和密码传输密钥。
- `MemberAuthSession`：前台会员认证会话事实。
- `MemberAccessToken`：前台会员 API 请求访问 token。
- `MemberRefreshToken`：前台会员刷新 token。

固定约束：

- `MemberLoginForm` 使用 Redis / JetCache 运行态存储，不建立数据库表，不依赖 HTTP session。
- 前台 API 登录成功后必须创建 `MemberAuthSession`、`MemberAccessToken` 和 `MemberRefreshToken`。
- 前台会员认证运行态使用 `member_` 物理表名前缀，但 Java 模型、DAO 契约和持久化实现归属 `modules.auth`。
- `MemberAccessToken` 和 `MemberRefreshToken` 固定只保存 token hash，不保存 token 明文。

### 5.6 LoginForm

`LoginForm` 是后台登录表单临时状态。

固定约束：

- 登录表单固定用于承载 `loginToken`、验证码、短信验证码、SM2 公私钥和短期校验码。
- 登录表单不是认证会话。
- 登录表单过期不等于访问 token 过期。
- 后台密码类接口需要加密传输时，固定复用 `LoginForm` 的 SM2 密钥，不再提供独立 keypair API。

### 5.7 AccessToken

`AccessToken` 是后台请求访问 token。

固定约束：

- 登录成功后必须创建 `AccessToken`。
- `AccessToken` 必须能定位后台 `User`。
- token 删除时必须释放权限会话并更新认证会话状态。

### 5.8 PermissionSession

`PermissionSession` 是后台权限集合缓存。

固定约束：

- 登录成功后必须创建 `PermissionSession`。
- 有效请求必须 touch `PermissionSession`。
- 登出或 token 删除时必须释放 `PermissionSession`。
- `PermissionSession` 不替代 `AuthSession` 的审计职责。
- `PermissionSession` 运行态模型和 DAO 归属 `biz.modules.auth`，后台权限会话适配 Service 归属 `sandwish-admin-api` 的 `auth.service`。

### 5.9 OAuthClient

`OAuthClient` 是 OAuth2 客户端配置。

核心字段：

- `id`：客户端主键。
- `clientId`：客户端标识。
- `clientSecretHash`：客户端密钥哈希。
- `clientName`：客户端名称。
- `clientType`：客户端类型。
- `grantTypes`：允许的授权类型集合。
- `scopes`：允许的授权范围集合。
- `redirectUris`：允许的回调地址集合。
- `accessTokenTtlSeconds`：访问 token 有效期。
- `refreshTokenTtlSeconds`：refresh token 有效期。
- `status`：客户端状态。
- `contact`：联系人。
- `remark`：备注。

固定约束：

- `clientId` 必须唯一定位一个 `OAuthClient`。
- `clientSecretHash` 只保存哈希，不保存明文。
- 禁用客户端不得发起授权、换 token 或刷新 token。
- 请求的 `grantType`、`scope` 和 `redirectUri` 必须在客户端配置范围内。

### 5.10 OAuthAuthorization

`OAuthAuthorization` 是 OAuth2 授权请求和授权码事实。

核心字段：

- `id`：授权记录主键。
- `authorizationCode`：授权码。
- `clientId`：客户端标识。
- `userId`：授权用户。
- `redirectUri`：回调地址。
- `scopes`：授权范围集合。
- `state`：OAuth2 state。
- `codeChallenge`：PKCE challenge。
- `codeChallengeMethod`：PKCE challenge method。
- `issuedAt`：签发时间。
- `expireAt`：过期时间。
- `used`：是否已消费。

固定约束：

- 授权码只能消费一次。
- 授权码过期后不得换 token。
- `redirectUri` 必须来自对应 `OAuthClient.redirectUris`。
- 授权范围必须是 `OAuthClient.scopes` 的子集。
- `S256` PKCE challenge 必须使用 code verifier 的 SHA-256 Base64Url 摘要。

### 5.11 OAuthAccessToken

`OAuthAccessToken` 是 OAuth2 access token 事实。

核心字段：

- `id`：access token 主键。
- `tokenId`：token 标识。
- `tokenHash`：token 哈希。
- `clientId`：客户端标识。
- `userId`：用户标识。
- `scopes`：授权范围集合。
- `issuedAt`：签发时间。
- `expireAt`：过期时间。
- `status`：token 状态。

固定状态：

- `ACTIVE`：可用。
- `REVOKED`：已撤销。
- `EXPIRED`：已过期。

固定约束：

- OAuth2 access token 只保存哈希，不保存明文。
- OAuth2 access token 哈希必须使用 SHA-256 Base64Url 摘要。
- introspection 必须同时校验 token 状态、过期时间和用户启用状态。
- revoke access token 后 introspection 必须返回 `active=false`。

### 5.12 OAuthRefreshToken

`OAuthRefreshToken` 是 refresh token 事实。

核心字段：

- `id`：refresh token 主键。
- `tokenId`：token 标识。
- `tokenHash`：token 哈希。
- `accessTokenId`：关联访问 token 标识。
- `clientId`：客户端标识。
- `userId`：用户标识。
- `issuedAt`：签发时间。
- `expireAt`：过期时间。
- `status`：token 状态。

固定状态：

- `ACTIVE`：可用。
- `USED`：已轮换使用。
- `REVOKED`：已撤销。
- `EXPIRED`：已过期。

固定约束：

- refresh token 只保存哈希，不保存明文。
- refresh token refresh 成功后必须轮换或标记原 token 已使用。
- refresh token 失效必须同步阻断后续访问 token 刷新。

## 6. Global Constraints

- 后台认证固定以 `PrincipalIdentity + PrincipalCredential` 完成登录校验。
- 后台账号密码登录固定先解析 `PrincipalIdentity`，再校验 `PrincipalCredential`。
- 短信、企业微信和 GitHub 登录固定通过独立 provider 解析外部身份，再映射到 `PrincipalIdentity`。
- OAuth2 客户端密钥校验固定通过 Service 完成。
- OAuth2 authorization code 和 refresh token 必须一次性消费或状态流转，避免重放。
- token verify、introspection 和 userinfo 固定只返回可公开的 token/session/user 信息。
- Controller 不直接访问 DAO / Mapper。
- Controller 不直接写回凭据失败次数。
- Service 固定承接认证流程、状态校验、失败次数写回、锁定和会话创建。
- DAO 固定承接持久化访问，不承载认证业务流程。
- `User.loginName` 和 `User.loginPass` 仅允许作为用户创建、资料维护和认证模型初始化来源。
- 新增用户时必须创建默认 `USER_ACCOUNT` 类型 `PrincipalIdentity`。
- 设置或重置密码时必须创建或更新 `USER_PASSWORD` 类型 `PrincipalCredential`。
- 后台用户锁定语义应该收敛到凭据维度锁定。
- 后台认证模型不得改变前台会员登录语义。

## 7. Functional Requirements

### 7.1 登录表单

- 后台登录前必须创建 `LoginForm`。
- 登录表单必须生成 `loginToken`。
- 登录表单必须生成验证码。
- 登录表单必须生成 SM2 密钥对。
- 登录表单数量超过限制时必须拒绝创建。
- 在线用户数量超过限制时必须拒绝创建。
- 刷新登录表单时必须校验 refresh token。

### 7.2 验证码

- 后台账号密码登录必须校验验证码。
- 验证码不存在时必须返回明确错误。
- 登录表单无效或过期时必须返回 token 错误。
- 白名单验证码启用时必须按配置放行。

### 7.3 身份解析

- 后台账号密码登录固定使用 `USER_ACCOUNT` 类型解析登录名。
- `PrincipalIdentity` 不存在时必须按用户名密码错误处理。
- `PrincipalIdentity.status = DISABLED` 时必须拒绝登录。
- `PrincipalIdentity.principalKey` 对应 `User` 不存在时必须拒绝登录。
- `User.status` 非启用状态时必须拒绝登录。

### 7.4 密码凭据校验

- 解析身份后必须按 `identityId + USER_PASSWORD` 读取 `PrincipalCredential`。
- `PrincipalCredential` 不存在时必须按用户名密码错误处理。
- `PrincipalCredential.status = DISABLED` 时必须拒绝登录。
- `PrincipalCredential.status = LOCKED` 时必须拒绝登录。
- `PrincipalCredential.status = EXPIRED` 时必须拒绝登录。
- `PrincipalCredential.lockedUntil` 未到期时必须拒绝登录。
- 密码验证必须使用 `PasswordHelper`。
- 密码验证成功后必须清零 `failedCount`。
- 密码验证成功后必须清空锁定状态。
- 密码验证成功后必须记录 `lastVerifiedAt`。
- 密码验证失败后必须递增 `failedCount`。
- 密码验证失败达到 `failedLimit` 时必须锁定该 `PrincipalCredential`。

### 7.5 访问 token

- 密码认证成功后必须创建 `AccessToken`。
- `AccessToken` 必须记录 token、`userId` 和校验码。
- token 校验失败时必须拒绝访问。
- 有效 token 请求必须刷新访问态。
- 删除 token 时必须释放权限会话。

### 7.6 权限会话

- 登录成功后必须创建 `PermissionSession`。
- `PermissionSession` 必须绑定 token 和 `userId`。
- 有效请求必须 touch 权限会话。
- 登出时必须释放权限会话。
- 权限会话的权限来源和匹配规则不在本文档重定义。

### 7.7 认证会话

- 登录成功后必须创建 `AuthSession`。
- `AuthSession` 必须绑定 token、`userId`、`identityId`、`identityType` 和 `loginType`。
- 有效请求刷新访问态时必须 touch Redis 运行态 `AuthSession`。
- 登出时必须用 Redis 运行态最后访问时间收口数据库 `AuthSession`，并标记为 `LOGGED_OUT`。
- token 安全失效时必须用 Redis 运行态最后访问时间收口数据库 `AuthSession`，并标记为 `INVALIDATED`。
- 按 token 失效会话时必须释放对应 `PermissionSession`。
- 按用户失效会话时必须失效该用户全部活跃 `AuthSession`。
- 会话自然过期时必须将数据库 `AuthSession` 标记为 `EXPIRED`。
- Redis 运行态过期不替代数据库最终状态收口。

### 7.8 OAuth2 client

- OAuth2 授权和换 token 前必须校验 `OAuthClient` 存在。
- 禁用客户端必须拒绝授权和换 token。
- 客户端密钥必须通过固定哈希 Helper 校验。
- 请求的授权类型、scope 和 redirect uri 必须落在客户端配置范围内。

### 7.9 OAuth2 authorization

- authorize 请求必须生成授权视图或授权请求记录。
- 用户同意授权后必须生成授权码。
- 用户拒绝授权后必须返回拒绝结果。
- 授权码换 token 成功后必须标记已使用。
- 授权码撤销后不得继续换 token。

### 7.10 refresh token

- OAuth2 token 响应需要按客户端策略生成 refresh token。
- refresh token 必须能定位 client、user 和 access token。
- refresh token 只保存哈希，不保存明文。
- refresh token 哈希必须使用 SHA-256 Base64Url 摘要。
- refresh token 过期、撤销或已使用时必须拒绝刷新。
- refresh token 刷新成功后必须生成新的 access token。

### 7.11 token verify / introspection / userinfo

- token verify 必须返回 token 是否有效。
- introspection 必须返回 OAuth2 `active`、`sub`、`client_id`、`scope`、`exp` 和 `token_type` 语义。
- userinfo 必须根据有效 token 返回当前用户公开信息，并提供 `sub`、`username`、`preferred_username` 和 `name`。
- 无效 token 不得抛出复杂业务异常，应返回明确非活跃结果。

### 7.12 多登录方式

- 短信登录必须校验手机号和短信验证码。
- 企业微信登录必须通过 provider 校验外部身份。
- GitHub 登录必须通过 provider 校验外部身份。
- 外部身份映射不到后台用户时必须拒绝登录。
- 多登录方式登录成功后必须复用统一 `AccessToken`、`PermissionSession` 和 `AuthSession` 创建流程。

### 7.13 用户保存联动

- 新增后台用户时必须创建 `USER_ACCOUNT` 类型 `PrincipalIdentity`。
- 新增后台用户并设置初始密码时必须创建 `USER_PASSWORD` 类型 `PrincipalCredential`。
- 修改后台登录名时必须更新 `USER_ACCOUNT` 类型 `PrincipalIdentity`。
- 重置后台用户密码时必须更新 `USER_PASSWORD` 类型 `PrincipalCredential`。
- 禁用后台用户时不删除 `PrincipalIdentity` 和 `PrincipalCredential`。
- 禁用某个登录标识时不禁用 `User`。
- 禁用某个认证凭据时不禁用 `User`。

### 7.14 认证模型初始化

- 新增后台用户时应该从 `User.loginName` 初始化 `USER_ACCOUNT` 类型 `PrincipalIdentity`。
- 新增或重置后台用户密码时应该从加密后的密码初始化 `USER_PASSWORD` 类型 `PrincipalCredential`。
- 密码认证应该读取 `PrincipalCredential.credentialValue`。
- 后台认证主锁定语义应该落在凭据维度。

## 8. Key Flows

### 8.1 后台账号密码登录流程

1. `AuthController.login` 接收登录请求。
2. `AuthController` 校验 `loginToken` 和验证码。
3. `AuthController` 使用登录表单私钥解密密码。
4. `AuthController` 调用认证 Service 执行登录校验。
5. 认证 Service 按 `USER_ACCOUNT + loginName` 读取 `PrincipalIdentity`。
6. 认证 Service 读取并校验 `User` 状态。
7. 认证 Service 按 `identityId + USER_PASSWORD` 读取 `PrincipalCredential`。
8. 认证 Service 校验凭据状态、锁定和过期。
9. 认证 Service 使用 `PasswordHelper` 校验密码。
10. 密码错误时写回凭据失败次数。
11. 密码正确时清零凭据失败状态。
12. 登录成功后创建 `AccessToken`。
13. 登录成功后创建 `PermissionSession`。
14. 登录成功后创建数据库审计态 `AuthSession`。
15. 登录成功后写入 Redis 运行态 `AuthSession`。
16. `AuthController` 返回 token 响应。

### 8.2 后台请求认证流程

1. 后台 token filter 读取请求 token。
2. token filter 读取 `AccessToken`。
3. token filter 校验 token check code。
4. token filter touch `AccessToken`。
5. token filter touch `PermissionSession`。
6. token filter touch Redis 运行态 `AuthSession`。
7. token filter 恢复当前用户上下文。

### 8.3 后台登出流程

1. `AuthController.logout` 接收登出请求。
2. Controller 按 token 定位 `AccessToken`。
3. Service 删除 `AccessToken`。
4. Service 释放 `PermissionSession`。
5. Service 读取 Redis 运行态 `AuthSession` 的最后访问时间。
6. Service 删除 Redis 运行态 `AuthSession`。
7. Service 将数据库 `AuthSession` 标记为 `LOGGED_OUT`。

### 8.4 后台用户创建流程

1. 用户 Service 保存 `User` 主体。
2. 用户 Service 创建默认 `USER_ACCOUNT` 类型 `PrincipalIdentity`。
3. 用户 Service 创建默认 `USER_PASSWORD` 类型 `PrincipalCredential`。
4. 用户 Service 保存用户角色关系。

### 8.5 后台密码重置流程

1. 用户 Service 校验目标 `User` 存在。
2. 用户 Service 定位默认 `USER_ACCOUNT` 类型 `PrincipalIdentity`。
3. 用户 Service 更新或创建 `USER_PASSWORD` 类型 `PrincipalCredential`。
4. 用户 Service 将失败次数和锁定状态清零。
5. 用户 Service 按策略设置 `needChangePassword`。

### 8.6 OAuth2 authorization code 流程

1. `OAuth2Controller.authorize` 接收授权请求。
2. Controller 调用认证 Service 校验 client、redirect uri、scope 和当前会话。
3. Service 创建授权请求视图。
4. `OAuth2Controller.decision` 接收用户授权决策。
5. 用户同意时 Service 创建授权码。
6. 用户拒绝时 Service 返回拒绝结果。
7. `OAuth2Controller.token` 使用授权码换取 access token 和 refresh token。
8. Service 标记授权码已使用。

### 8.7 refresh token 流程

1. `OAuth2Controller.token` 接收 refresh token 请求。
2. Controller 调用认证 Service 校验 client 和 refresh token。
3. Service 判断 refresh token 状态和过期时间。
4. Service 标记原 refresh token 已使用或失效。
5. Service 创建新的 access token。
6. Service 按策略创建新的 refresh token。

### 8.8 token introspection / userinfo 流程

1. OAuth2 token 查询入口接收 token。
2. Service 校验 token 是否存在、有效且未过期。
3. introspection 返回 `active` 和 token 元数据。
4. userinfo 返回当前用户公开信息。

### 8.9 多登录方式流程

1. Controller 接收短信、企业微信或 GitHub 登录请求。
2. Service 调用对应 provider 校验外部身份。
3. Service 将外部身份映射到 `PrincipalIdentity`。
4. Service 校验 `User` 状态。
5. 登录成功后复用统一 token 和 session 创建流程。

## 9. Non-Functional Requirements

- 后台认证状态变化必须可测试。
- 登录失败、凭据锁定、会话登出和会话失效必须可审计。
- 登录链路不得直接访问 Mapper。
- 凭据值不得写入日志。
- 密码明文不得持久化。
- 认证错误响应不得泄露密码哈希或凭据值。
- 后台账号密码登录接口必须保持稳定。
- 前后台认证链路不得复制业务规则。

## 10. Open Items

无
