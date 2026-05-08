# RUNBOOK Auth Principal Migration

## 1. Purpose

本文档定义 Auth 身份与凭据模型迁移到 `PrincipalIdentity` / `PrincipalCredential` 的执行手册。

迁移目标：

- Auth 统一使用 `PrincipalIdentity` 表达登录身份。
- Auth 统一使用 `PrincipalCredential` 表达认证凭据。
- `UserIdentity` / `UserCredential` / `MemberIdentity` / `MemberCredential` 从生产代码中删除。
- `AdminAuthService` / `MemberAuthService` 只承担入口认证编排、会话和响应组装，不继续承载 Principal 细节。

## 2. Scope

包含范围：

- 后台用户身份与密码维护代码迁移。
- 会员注册、登录、改密和重置密码代码迁移。
- admin-api 密码、短信、wecom 和 github 登录代码迁移。
- front-api 密码和短信登录代码迁移。
- 认证会话中的 `identityId` / `identityType` 语义迁移。
- 旧 Identity / Credential Service、DAO、Mapper、Entity、Enum 和测试引用拆除。
- Auth 相关需求、架构或专项文档同步。

不包含范围：

- 数据准备。
- 数据库收尾。
- 线上数据备份、旧表 drop 和运维操作。

## 3. Execution Order

本 RUNBOOK 建立在已完成旧模型边界盘点和 Principal 目标模型确认的基础上。执行阶段从写路径迁移开始。

已完成前置认知：

- `PrincipalIdentity` / `PrincipalCredential` 是 Auth 身份与凭据的唯一目标模型。
- `PrincipalType` 已经表达 admin-api / front-api 的主体边界，不引入 `PrincipalModule`。
- `PrincipalKey` 固定表达主体坐标，`principalType` 区分 `USER` / `MEMBER`，`principalId` 指向对应业务主体主键。
- 旧 `UserIdentity` / `UserCredential` / `MemberIdentity` / `MemberCredential` 是待迁移、待删除模型，不再作为新能力扩展点。
- `AdminAuthService` / `MemberAuthService` 现有职责过重，Principal 身份与凭据细节迁回 biz，并通过 Principal 相关 Service 分拆。
- 数据准备和数据库收尾不属于本 RUNBOOK 执行范围。

可验证点：

```bash
rg "UserIdentity|UserCredential|MemberIdentity|MemberCredential" sandwish-biz sandwish-admin-api sandwish-front-api
```

每个执行阶段进入执行前，必须在 `TODO.md` 拆成可审阅任务；任务经人工审阅后再执行。

### 3.1 迁移写路径

目标：

- 所有产生或维护登录身份、认证凭据的业务写路径改写 Principal。

执行内容：

- 后台用户新增和修改登录名写 `PrincipalIdentityType.USER_ACCOUNT`。
- 后台用户设置、修改和重置密码写 `PrincipalCredentialType.USER_PASSWORD`。
- 会员账号注册写 `PrincipalIdentityType.MEMBER_ACCOUNT`。
- 会员手机注册写 `PrincipalIdentityType.MEMBER_MOBILE`。
- 会员邮箱注册写 `PrincipalIdentityType.MEMBER_EMAIL`。
- 会员设置、修改和重置密码写 `PrincipalCredentialType.MEMBER_PASSWORD`。

提交边界：

- 后台用户维护迁移形成独立提交。
- 会员身份与凭据维护迁移形成独立提交。

### 3.2 迁移读路径

目标：

- 所有认证读取和展示读取改读 Principal。

执行内容：

- `AdminAuthService` 密码登录使用 `PrincipalAuthService.authenticatePassword`。
- `AdminAuthService` 短信、wecom 和 github 登录使用 `PrincipalAuthService.authenticateIdentity`。
- `MemberAuthService` 密码登录使用 `PrincipalAuthService.authenticatePassword`。
- `MemberAuthService` 短信登录使用 `PrincipalAuthService.authenticateIdentity`。
- 后台用户、当前用户和会员展示所需账号、手机、邮箱读取改读 `PrincipalIdentityService`。

提交边界：

- admin-api 认证读取迁移形成独立提交。
- front-api 认证读取迁移形成独立提交。

### 3.3 迁移会话 identity 语义

目标：

- 认证会话中的 identity 语义统一指向 Principal。

执行内容：

- `AuthSession.identityId` 指向 `PrincipalIdentity.id`。
- `AuthSession.identityType` 使用 `PrincipalIdentityType`。
- `MemberAuthSession.identityId` 指向 `PrincipalIdentity.id`。
- `MemberAuthSession.identityType` 使用 `PrincipalIdentityType`。
- 会话持久化 assembler 在边界兼容旧字符串值。

兼容规则：

- admin 旧 session 字符串 `ACCOUNT` / `MOBILE` 在读取边界映射为 `USER_ACCOUNT` / `USER_MOBILE`。
- member 旧 session 字符串 `ACCOUNT` / `MOBILE` 在读取边界映射为 `MEMBER_ACCOUNT` / `MEMBER_MOBILE`。
- 新写入固定使用 `PrincipalIdentityType.value()`。

提交边界：

- 会话 identity 语义迁移形成独立提交。

### 3.4 拆除旧模型

目标：

- 生产代码、测试代码和文档中不再保留旧 Identity / Credential 模型依赖。

执行内容：

- 删除旧 Service：
  - `UserIdentityService`
  - `UserCredentialService`
  - `MemberIdentityService`
  - `MemberCredentialService`
- 删除旧 DAO、Mapper 和 XML：
  - `UserIdentityDao`
  - `UserCredentialDao`
  - `MemberIdentityDao`
  - `MemberCredentialDao`
- 删除旧 Entity 和 Enum：
  - `UserIdentity`
  - `UserCredential`
  - `MemberIdentity`
  - `MemberCredential`
  - 对应 `*Type` / `*Status`
- 删除旧测试桩和测试数据引用。

提交边界：

- 旧模型拆除形成独立提交。

### 3.5 同步文档并清理现场

目标：

- 文档口径与 Principal Auth 模型一致。
- RUNBOOK 和 TODO 现场在任务完成后被清理。

执行内容：

- 更新 Auth 需求文档中的身份与凭据模型描述。
- 更新架构或治理文档中的 Auth Service 边界描述。
- 删除旧 `*AuthService` 规则残留。
- 确认 `TODO.md` 只保留未完成任务。
- 迁移完成后删除本 RUNBOOK。

提交边界：

- 文档同步形成独立提交。
- RUNBOOK 清理与最终现场收口形成独立提交或并入最后一个文档提交。

## 4. Verification

行为验证：

- admin 密码登录可用。
- admin 短信登录可用。
- admin wecom 登录可用。
- admin github 登录可用。
- member 密码登录可用。
- member 短信登录可用。
- admin 用户新增、修改登录名、修改密码和重置密码可用。
- member 注册、修改密码和重置密码可用。
- 新认证会话中的 `identityId` 指向 `PrincipalIdentity.id`。
- 新认证会话中的 `identityType` 使用 `PrincipalIdentityType`。

引用验证：

```bash
rg "UserIdentity|UserCredential|MemberIdentity|MemberCredential" sandwish-biz sandwish-admin-api sandwish-front-api docs
```

测试验证：

```bash
mvn -pl sandwish-biz -am test
mvn -pl sandwish-admin-api -am test
mvn -pl sandwish-front-api -am test
```

## 5. Open Items

无
