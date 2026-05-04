# Auth Identity Credential Runbook

## 1. Purpose

本文档记录后台认证模型迁移完成后的当前状态和人工检查入口。

稳定需求口径以 [`../10-requirements/AUTH-REQUIREMENTS.md`](../10-requirements/AUTH-REQUIREMENTS.md) 为准。数据库设计口径以 [`../20-database/AUTH-DATABASE-DESIGN.md`](../20-database/AUTH-DATABASE-DESIGN.md) 为准。本文档不再承载已完成 TODO、提交历史或分步迁移任务。

## 2. Scope

当前范围：

- 后台账号密码认证链路。
- `User + UserIdentity + UserCredential + AuthSession` 认证模型。
- 认证会话 Redis 运行态和数据库审计态。
- 迁移兼容字段的当前保留边界。

不在范围内：

- 前台会员登录体系。
- 第三方登录接入。
- Spring Security 权限模型重做。
- 生产数据迁移脚本执行。

## 3. Current Model

目标模型固定为：

`User -> UserIdentity -> UserCredential -> AuthSession`

对象边界：

- `User` 是后台用户主体，只承载用户资料、组织关系、权限等级、状态和审计字段。
- `UserIdentity` 是登录标识，承载 `userId`、`identityType`、`identityValue` 和 `status`。
- `UserCredential` 是认证凭据，承载 `userId`、`identityId`、`credentialType`、`credentialValue`、失败次数、锁定、过期和强制改密状态。
- `AuthSession` 是登录会话事实，承载 token/sessionId、`userId`、`identityId`、登录方式、签发时间、最后访问时间、过期时间、登出时间和失效原因。
- `AuthSession` 固定分为 Redis 运行态和数据库审计态。

## 4. Fixed Rules

- 后台认证固定先解析 `UserIdentity`，再校验 `UserCredential`。
- 禁用 `UserIdentity` 只禁止该登录方式，不等于禁用 `User`。
- 锁定 `UserCredential` 只锁定该认证因子，不等于锁定 `User`。
- 密码哈希固定保存到 `UserCredential.credentialValue`。
- 登录失败次数固定保存到 `UserCredential.failedCount`。
- 旧账号维度 `LoginLockDao` 已下线，后台认证主锁定语义固定收敛到 `UserCredential`。
- `AuthSession` 不承载权限集合，权限集合仍由 `PermissionSession` 负责。
- 正常请求只 touch Redis 运行态 `AuthSession`，不逐请求更新数据库 `auth_session.last_access_time`。
- 登出、失效和过期收口时，数据库 `AuthSession` 固定写入最终状态和最后访问时间。

## 5. Compatibility Boundaries

- `User.loginName` 仅作为迁移期兼容字段，目标登录标识固定由 `UserIdentity.identityValue` 承载。
- `User.loginPass` 仅作为迁移期兼容字段，目标密码凭据固定由 `UserCredential.credentialValue` 承载。
- `UserEncrypt.loginPass` 仅作为旧用户加密表兼容镜像，不承载认证判定。
- 老用户首次登录允许从旧字段初始化 `UserIdentity` 和 `UserCredential`。
- 登录链路不得直接使用 `User.loginPass` 做密码认证。

## 6. Verification

常用检查：

```bash
mvn -q -pl sandwish-biz -am test
mvn -q -pl sandwish-infra -am test
mvn -q -pl sandwish-admin-api -am test
```

最小人工检查：

- 后台账号密码登录成功。
- 后台账号密码登录失败累计到凭据维度。
- 凭据锁定后不能继续登录。
- 登录成功后存在 `AccessToken`、`PermissionSession`、Redis 运行态 `AuthSession` 和数据库审计态 `AuthSession`。
- 有效请求只刷新 Redis 运行态 `AuthSession`。
- 登出后 token 不可继续访问，Redis 运行态删除，数据库审计态标记为 `LOGGED_OUT`。
- 用户修改登录名后旧登录名不可登录，新登录名可登录。

## 7. Open Items

无
