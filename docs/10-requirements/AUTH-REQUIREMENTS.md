# AUTH REQUIREMENTS

## 1. Purpose

本文档定义 Sandwich 后台认证模型的业务需求边界。

`Auth` 负责后台登录表单、验证码、密码认证、登录标识解析、认证凭据状态流转、访问 token 生命周期和认证会话审计。后台认证固定区分用户主体、登录标识、认证凭据和登录会话。

## 2. Scope

当前覆盖范围：

- 后台账号密码登录。
- 后台登录表单和验证码。
- 后台访问 token 创建、校验、续期和删除。
- 后台权限会话创建、touch 和释放。
- 后台用户登录标识管理。
- 后台用户密码凭据管理。
- 后台认证会话创建、touch、登出、失效和过期。
- 后台用户创建、修改登录名、重置密码时的认证前置数据维护。

当前不覆盖范围：

- 前台会员登录体系。
- 第三方登录接入。
- MFA 二次认证。
- 短信验证码登录。
- OAuth2 / OIDC 协议接入。
- Spring Security 权限整改。
- 用户、角色、菜单授权模型重做。
- 生产数据迁移执行。

## 3. Bounded Context

后台认证目标模型固定为：

`User -> UserIdentity -> UserCredential -> AuthSession`

`User` 归属 `sys` 用户主体，承载后台用户资料、组织关系、权限等级、启停状态和审计字段。

`UserIdentity` 归属 `auth` 认证模型，承载后台登录标识。一个 `User` 可以绑定多个 `UserIdentity`。

`UserCredential` 归属 `auth` 认证模型，承载后台认证凭据。一个 `User` 可以绑定多个 `UserCredential`。

`AuthSession` 归属 `auth` 认证模型，承载后台登录后的会话事实。

`AccessToken` 继续承载请求访问 token。`AuthSession` 固定不替代 `AccessToken` 的传输职责。

`PermissionSession` 继续承载权限集合缓存。`AuthSession` 固定不承载权限集合。

## 4. Module Mapping

- `sandwish-biz/src/main/java/com/github/thundax/modules/auth`
  - 定义 `UserIdentity`、`UserCredential`、`AuthSession`、枚举、DAO 契约和认证 Service 编排。
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys`
  - 定义后台 `User` 主体、用户保存流程和用户资料维护。
- `sandwish-infra/src/main/java/com/github/thundax/modules/auth`
  - 实现认证模型 DAO，维护 DO、Mapper 和持久化转换。
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys`
  - 实现后台用户主体 DAO，维护用户资料持久化。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth`
  - 提供后台登录、刷新、验证码、登出和 token 认证入口适配。

## 5. Core Business Objects

### 5.1 User

`User` 是后台用户主体。

核心字段：

- `id`：后台用户 ID，使用 `EntityId`。
- `officeId`：所属机构 ID。
- `name`：用户名称。
- `email`：联系邮箱。
- `mobile`：联系手机号。
- `tel`：联系电话。
- `ranks`：用户等级。
- `privilege`：用户权限等级。
- `status`：用户状态。
- `registerDate`：注册时间。
- `registerIp`：注册 IP。
- `lastLoginDate`：最近登录时间。
- `lastLoginIp`：最近登录 IP。
- `loginCount`：登录次数。
- `createDate`：创建时间。
- `updateDate`：更新时间。

固定约束：

- `User` 不等于登录标识。
- `User` 不等于认证凭据。
- `User.status` 禁用时，该用户全部后台登录方式不可用。
- `User.loginName` 仅作为迁移期兼容字段，最终认证语义固定迁移到 `UserIdentity.identityValue`。
- `User.loginPass` 仅作为迁移期兼容字段，最终密码认证语义固定迁移到 `UserCredential.credentialValue`。

### 5.2 UserIdentity

`UserIdentity` 是后台登录标识。

核心字段：

- `id`：登录标识 ID。
- `userId`：关联后台用户 ID。
- `identityType`：登录标识类型。
- `identityValue`：登录标识值。
- `status`：登录标识状态。
- `createDate`：创建时间。
- `updateDate`：更新时间。

固定标识类型：

- `ACCOUNT`：后台账号。
- `MOBILE`：后台手机号。
- `EMAIL`：后台邮箱。

固定状态：

- `ENABLED`：可用于登录。
- `DISABLED`：不可用于登录。

固定约束：

- `identityType + identityValue` 必须唯一定位一个 `UserIdentity`。
- 禁用 `UserIdentity` 只影响该登录方式，不等于禁用 `User`。
- 修改后台账号固定更新 `ACCOUNT` 类型 `UserIdentity`。
- `UserIdentity` 不保存密码哈希。

### 5.3 UserCredential

`UserCredential` 是后台认证凭据。

核心字段：

- `id`：认证凭据 ID。
- `userId`：关联后台用户 ID。
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
- `createDate`：创建时间。
- `updateDate`：更新时间。

固定凭据类型：

- `PASSWORD`：后台密码凭据。

固定状态：

- `ACTIVE`：可用于认证。
- `LOCKED`：已锁定。
- `EXPIRED`：已过期。
- `DISABLED`：已禁用。

固定约束：

- 密码哈希固定保存到 `UserCredential.credentialValue`。
- 登录失败次数固定保存到 `UserCredential.failedCount`。
- 凭据锁定固定发生在 `UserCredential` 维度，不发生在 `User` 维度。
- 密码过期固定使用 `UserCredential.expiresAt` 表达。
- 首次登录或重置密码后的强制改密固定使用 `UserCredential.needChangePassword` 表达。
- `identityId + credentialType` 必须唯一定位一个认证凭据。

### 5.4 AuthSession

`AuthSession` 是后台认证会话事实。

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

- 每次后台登录成功必须创建新的 `AuthSession`。
- 请求 token 有效且刷新访问态时，必须 touch 对应 `AuthSession.lastAccessTime`。
- 主动登出固定将 `AuthSession.status` 更新为 `LOGGED_OUT`。
- 安全策略失效固定将 `AuthSession.status` 更新为 `INVALIDATED`。
- 自然过期固定将 `AuthSession.status` 更新为 `EXPIRED`。
- `AuthSession` 不保存权限集合。

### 5.5 LoginForm

`LoginForm` 是后台登录表单临时状态。

固定约束：

- 登录表单固定用于承载 `loginToken`、验证码、短信验证码、SM2 公私钥和短期校验码。
- 登录表单不是认证会话。
- 登录表单过期不等于访问 token 过期。

### 5.6 AccessToken

`AccessToken` 是后台请求访问 token。

固定约束：

- 登录成功后必须创建 `AccessToken`。
- `AccessToken` 必须能定位后台 `User`。
- token 删除时必须释放权限会话并更新认证会话状态。

### 5.7 PermissionSession

`PermissionSession` 是后台权限集合缓存。

固定约束：

- 登录成功后必须创建 `PermissionSession`。
- 有效请求必须 touch `PermissionSession`。
- 登出或 token 删除时必须释放 `PermissionSession`。
- `PermissionSession` 不替代 `AuthSession` 的审计职责。

## 6. Global Constraints

- 后台认证固定以 `UserIdentity + UserCredential` 完成登录校验。
- 后台账号密码登录固定先解析 `UserIdentity`，再校验 `UserCredential`。
- Controller 不直接访问 DAO / Mapper。
- Controller 不直接写回凭据失败次数。
- Service 固定承接认证流程、状态校验、失败次数写回、锁定和会话创建。
- DAO 固定承接持久化访问，不承载认证业务流程。
- `User.loginName` 和 `User.loginPass` 仅允许作为迁移期兼容来源。
- 新增用户时必须创建默认 `ACCOUNT` 类型 `UserIdentity`。
- 设置或重置密码时必须创建或更新 `PASSWORD` 类型 `UserCredential`。
- 旧账号维度锁定语义迁移完成后必须收敛到凭据维度锁定。
- 后台认证迁移不得改变前台会员登录语义。

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

- 后台账号密码登录固定使用 `ACCOUNT` 类型解析登录名。
- `UserIdentity` 不存在时必须按用户名密码错误处理。
- `UserIdentity.status = DISABLED` 时必须拒绝登录。
- `UserIdentity.userId` 对应 `User` 不存在时必须拒绝登录。
- `User.status` 非启用状态时必须拒绝登录。

### 7.4 密码凭据校验

- 解析身份后必须按 `identityId + PASSWORD` 读取 `UserCredential`。
- `UserCredential` 不存在时必须按用户名密码错误处理。
- `UserCredential.status = DISABLED` 时必须拒绝登录。
- `UserCredential.status = LOCKED` 时必须拒绝登录。
- `UserCredential.status = EXPIRED` 时必须拒绝登录。
- `UserCredential.lockedUntil` 未到期时必须拒绝登录。
- 密码验证必须使用现有 `PasswordService`。
- 密码验证成功后必须清零 `failedCount`。
- 密码验证成功后必须清空锁定状态。
- 密码验证成功后必须记录 `lastVerifiedAt`。
- 密码验证失败后必须递增 `failedCount`。
- 密码验证失败达到 `failedLimit` 时必须锁定该 `UserCredential`。

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
- 有效请求刷新访问态时必须 touch `AuthSession`。
- 登出时必须将当前 `AuthSession` 标记为 `LOGGED_OUT`。
- token 安全失效时必须将当前 `AuthSession` 标记为 `INVALIDATED`。
- 会话自然过期时必须将当前 `AuthSession` 标记为 `EXPIRED`。

### 7.8 用户保存联动

- 新增后台用户时必须创建 `ACCOUNT` 类型 `UserIdentity`。
- 新增后台用户并设置初始密码时必须创建 `PASSWORD` 类型 `UserCredential`。
- 修改后台登录名时必须更新 `ACCOUNT` 类型 `UserIdentity`。
- 重置后台用户密码时必须更新 `PASSWORD` 类型 `UserCredential`。
- 禁用后台用户时不删除 `UserIdentity` 和 `UserCredential`。
- 禁用某个登录标识时不禁用 `User`。
- 禁用某个认证凭据时不禁用 `User`。

### 7.9 迁移兼容

- 迁移期间允许从 `User.loginName` 初始化 `ACCOUNT` 类型 `UserIdentity`。
- 迁移期间允许从 `User.loginPass` 或 `UserEncrypt.loginPass` 初始化 `PASSWORD` 类型 `UserCredential`。
- 登录链路切换完成后不得继续直接使用 `User.loginPass` 做密码认证。
- 旧账号维度登录锁定迁移完成后不得继续作为后台认证主锁定语义。

## 8. Key Flows

### 8.1 后台账号密码登录流程

1. `AuthController.login` 接收登录请求。
2. `AuthController` 校验 `loginToken` 和验证码。
3. `AuthController` 使用登录表单私钥解密密码。
4. `AuthController` 调用认证 Service 执行登录校验。
5. 认证 Service 按 `ACCOUNT + loginName` 读取 `UserIdentity`。
6. 认证 Service 读取并校验 `User` 状态。
7. 认证 Service 按 `identityId + PASSWORD` 读取 `UserCredential`。
8. 认证 Service 校验凭据状态、锁定和过期。
9. 认证 Service 使用 `PasswordService` 校验密码。
10. 密码错误时写回凭据失败次数。
11. 密码正确时清零凭据失败状态。
12. 登录成功后创建 `AccessToken`。
13. 登录成功后创建 `PermissionSession`。
14. 登录成功后创建 `AuthSession`。
15. `AuthController` 返回 token 响应。

### 8.2 后台请求认证流程

1. 后台 token filter 读取请求 token。
2. token filter 读取 `AccessToken`。
3. token filter 校验 token check code。
4. token filter touch `AccessToken`。
5. token filter touch `PermissionSession`。
6. token filter touch `AuthSession`。
7. token filter 恢复当前用户上下文。

### 8.3 后台登出流程

1. `AuthController.logout` 接收登出请求。
2. Controller 按 token 定位 `AccessToken`。
3. Service 删除 `AccessToken`。
4. Service 释放 `PermissionSession`。
5. Service 将当前 `AuthSession` 标记为 `LOGGED_OUT`。

### 8.4 后台用户创建流程

1. 用户 Service 保存 `User` 主体。
2. 用户 Service 创建默认 `ACCOUNT` 类型 `UserIdentity`。
3. 用户 Service 创建默认 `PASSWORD` 类型 `UserCredential`。
4. 用户 Service 保存用户角色关系。

### 8.5 后台密码重置流程

1. 用户 Service 校验目标 `User` 存在。
2. 用户 Service 定位默认 `ACCOUNT` 类型 `UserIdentity`。
3. 用户 Service 更新或创建 `PASSWORD` 类型 `UserCredential`。
4. 用户 Service 将失败次数和锁定状态清零。
5. 用户 Service 按策略设置 `needChangePassword`。

## 9. Non-Functional Requirements

- 后台认证状态变化必须可测试。
- 登录失败、凭据锁定、会话登出和会话失效必须可审计。
- 登录链路不得直接访问 Mapper。
- 凭据值不得写入日志。
- 密码明文不得持久化。
- 认证错误响应不得泄露密码哈希或凭据值。
- 迁移过程必须保持现有后台账号密码登录接口兼容。
- 前后台认证链路不得复制业务规则。

## 10. Open Items

无
