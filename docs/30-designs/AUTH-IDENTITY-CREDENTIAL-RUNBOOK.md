# Auth Identity Credential Runbook

## 1. Purpose

本文档定义后台用户认证模型从 `User.loginName/loginPass` 混合形态迁移到 `User + UserIdentity + UserCredential + AuthSession` 的执行路径。

迁移目标：

- 固定区分用户主体、登录标识、认证凭据和登录会话。
- 支持同一用户绑定多个登录标识。
- 将密码、失败次数、锁定、过期和强制改密固定收敛到 `UserCredential`。
- 将登录后的会话事实固定收敛到 `AuthSession`。
- 保持 Sandwich 三层 API 架构，不引入 `domain`、`application`、`interfaces`、`repository` 目录语义。

## 2. Scope

当前范围：

- 后台用户登录认证链路。
- `sys_user` 用户主体拆分。
- 登录标识、认证凭据和认证会话的新业务对象。
- `sandwish-biz` DAO / Service 契约。
- `sandwish-infra` 持久化实现。
- 后台登录 Controller 适配与测试补齐。

不在范围内：

- 前台会员登录体系迁移。
- 第三方登录接入实现。
- Spring Security 权限整改。
- 用户、角色、菜单授权模型重做。
- 数据迁移脚本的生产执行。

## 3. Bounded Context

目标模型固定为：

`User -> UserIdentity -> UserCredential -> AuthSession`

对象边界：

- `User` 是后台用户主体，只承载用户资料、组织关系、权限等级、状态和审计字段。
- `UserIdentity` 是登录标识，承载 `userId`、`identityType`、`identityValue` 和 `status`。
- `UserCredential` 是认证凭据，承载 `userId`、`identityId`、`credentialType`、`credentialValue`、失败次数、锁定、过期和强制改密状态。
- `AuthSession` 是登录会话事实，承载 token/sessionId、`userId`、`identityId`、登录方式、签发时间、最后访问时间、过期时间、登出时间和失效原因。

固定约束：

- 禁用 `UserIdentity` 只禁止该登录方式，不等于禁用 `User`。
- 锁定 `UserCredential` 只锁定该认证因子，不等于锁定 `User`。
- 密码哈希固定保存到 `UserCredential.credentialValue`。
- 登录失败次数固定保存到 `UserCredential.failedCount`。
- `AuthSession` 不承载权限集合；权限集合仍由权限会话链路负责。
- 迁移期间保留兼容读取，最终删除 `User.loginPass` 直接认证语义。

## 4. Module Mapping

### `sandwish-biz`

职责：

- 定义 `UserIdentity`、`UserCredential`、`AuthSession` 业务实体。
- 定义对应状态和类型枚举。
- 定义 `UserIdentityDao`、`UserCredentialDao`、`AuthSessionDao`。
- 定义身份、凭据和会话 Service 编排。
- 在登录流程中承接业务校验、状态流转和跨 DAO 编排。

边界：

- 不放 MyBatis Mapper。
- 不放 Mapper XML。
- 不访问 Redis 或数据库客户端。
- 不承载 Controller 请求模型。

### `sandwish-infra`

职责：

- 定义 `UserIdentityDO`、`UserCredentialDO`、`AuthSessionDO`。
- 定义 Mapper、DAO implementation 和 PersistenceAssembler。
- 承接数据库字段映射、唯一约束查询和分页查询。
- 保留迁移期从旧字段到新表的装载兼容。

边界：

- 不承载登录业务流程。
- 不暴露 DO 给 Controller 或 Service 调用方。

### `sandwish-admin-api`

职责：

- 登录入口按 `identityType + identityValue + credentialType` 调用认证 Service。
- Controller 只做请求绑定、验证码校验、密钥解密和响应组装。
- 登录成功后创建 token、权限会话和 `AuthSession`。
- 登出时释放 token、权限会话并更新 `AuthSession`。

边界：

- Controller 不直接查询 `UserIdentity` 或 `UserCredential` DAO。
- Controller 不直接更新凭据失败次数。

## 5. Migration Steps

### 5.1 固化需求和数据库口径

执行项：

- 新增后台认证需求文档。
- 新增后台用户认证数据库设计文档。
- 明确字段、唯一约束、索引、状态枚举和迁移期兼容规则。

验收点：

- 文档不引用外部项目作为正式规则来源。
- 新表与旧字段的迁移关系明确。
- 每个业务对象都有固定归属模块和表名。

### 5.2 建立业务对象和 DAO 契约

执行项：

- 在 `sandwish-biz` 新增 `UserIdentity`、`UserCredential`、`AuthSession`。
- 新增身份类型、凭据类型、凭据状态、会话状态等枚举。
- 新增 DAO 契约，方法只表达业务查询和状态写回。

验收点：

- 业务实体不依赖 MyBatis。
- DAO 契约不暴露 DO。
- 状态流转方法集中在实体或 Service 中。

### 5.3 建立持久化实现

执行项：

- 在 `sandwish-infra` 新增 DO、Mapper、DAO implementation 和 assembler。
- 为 `identityType + identityValue`、`identityId + credentialType`、`token/sessionId` 提供唯一查询。
- 增加对应持久化测试。

验收点：

- DO 与业务实体转换覆盖核心字段。
- DAO implementation 不承载登录业务流程。
- 唯一查询与状态更新有测试覆盖。

### 5.4 改造用户保存链路

执行项：

- 用户创建和修改时同步维护默认 `ACCOUNT` 身份。
- 密码创建和重置时写入 `UserCredential`。
- 保留旧字段读取兼容，直到登录链路完全切换。

验收点：

- 新增用户后存在可登录的 `UserIdentity` 和 `UserCredential`。
- 修改登录名只影响对应 `UserIdentity`。
- 密码修改只影响对应 `UserCredential`。

### 5.5 改造后台登录链路

执行项：

- 登录时先解析 `UserIdentity`。
- 再按 `identityId + PASSWORD` 读取 `UserCredential`。
- 校验 `User`、`UserIdentity` 和 `UserCredential` 状态。
- 成功后清零失败次数并记录最近验证时间。
- 失败后递增失败次数并按规则锁定凭据。

验收点：

- 登录行为与现有接口响应兼容。
- 错误密码只更新凭据失败状态。
- 禁用用户、禁用身份、锁定凭据返回可区分错误。

### 5.6 建立认证会话

执行项：

- 登录成功后创建 `AuthSession`。
- 请求活跃时 touch 会话最后访问时间。
- 登出时标记 `LOGGED_OUT`。
- token 失效、安全策略失效时标记 `INVALIDATED`。

验收点：

- token 和 `AuthSession` 能稳定关联。
- 登出后 token、权限会话和认证会话状态一致。
- 会话状态不替代权限集合。

### 5.7 收口旧字段语义

执行项：

- 已删除登录流程中对 `User.loginPass` 的直接认证依赖；老用户首次登录仅允许用该字段初始化 `UserCredential`。
- 已删除 `LoginLockDao` 的账号维度锁定接口和实现，失败次数、锁定和解锁状态由 `UserCredential` 承载。
- `UserEncrypt.loginPass` 仅保留为旧用户加密表兼容镜像，不承载认证判定。
- 测试和文档随对应实现提交同步更新。

验收点：

- 登录认证固定通过 `UserIdentity + UserCredential`。
- 旧字段只保留迁移期兼容或被明确删除。
- TODO 中对应项按完成范围删除或收窄。

## 6. Verification

常用检查：

```bash
mvn -q -pl sandwish-biz,sandwish-infra -am test
mvn -q -pl sandwish-admin-api -am test
```

最小人工检查：

- 后台账号密码登录成功。
- 后台账号密码登录失败累计次数。
- 凭据锁定后不能继续登录。
- 登出后 token 不可继续访问。
- 用户修改登录名后旧登录名不可登录，新登录名可登录。

## 7. Open Items

无
