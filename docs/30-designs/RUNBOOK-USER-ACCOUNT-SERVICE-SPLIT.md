# User Account Service Split Runbook

## 1. Purpose

本文档定义 `UserService` 拆分为用户主体、登录身份和认证凭据协作边界的执行顺序。

目标是降低 `UserService` 职责重量，让用户主体、账号身份和密码凭据分别具备清晰 Service 契约，同时保持当前三层架构和事务边界稳定。

## 2. Scope

当前范围：

- `UserService`
- `UserIdentityService`
- `UserCredentialService`
- 依赖用户账号身份或密码凭据的 Auth / Sys Service 调用点
- 对应 Service 单元测试和 Controller / Auth 合约测试

不在范围：

- 不调整数据库表结构
- 不新增认证架构层
- 不把事务编排下放到 Controller
- 不重命名 `UserIdentity` / `UserCredential` 实体
- 不引入 OAuth、短信、企业微信或 GitHub 登录的新流程

## 3. Target Boundaries

### 3.1 UserService

`UserService` 固定承载后台用户主体和用户角色关系：

- `getById`
- `listAll`
- `list`
- `page`
- `add`
- `update`
- `batchDeleteById`
- `updateStatus`
- `batchUpdateStatus`
- `listUserRoles`

`UserService.add` / `UserService.update` 暂时保留用户创建和更新的事务编排权。身份和凭据写入可以通过拆分后的 Service 协作完成，但 Controller 不直接拼接用户、身份和凭据的跨 Service 写事务。

### 3.2 UserIdentityService

`UserIdentityService` 固定承载后台用户账号身份：

- 按账号登录名读取用户
- 按用户主键读取账号登录名
- 新增或更新账号身份

账号登录名属于 `UserIdentity`，不属于 `User` 主体字段。只要 `User` 实体不承载 `loginName`，读取登录名就不能用 `UserService.getById` 替代。

### 3.3 UserCredentialService

`UserCredentialService` 固定承载后台用户认证凭据：

- 按用户主键读取密码凭据
- 更新密码凭据
- 新增用户时创建密码凭据

密码加密、密码强度校验和旧密码校验仍由当前业务调用点按既有职责处理；本拆分只移动凭据持久化协作边界。

## 4. Execution Order

### Step 1: Extract UserIdentityService

新增 `UserIdentityService` 和 `UserIdentityServiceImpl`，迁移账号登录名相关读取能力。

完成后：

- `UserService` 不再公开账号身份读取方法。
- Auth / Sys 调用点通过 `UserIdentityService` 获取账号登录名或按登录名取用户。
- 现有用户创建 / 更新流程行为不变。

### Step 2: Extract UserCredentialService

新增 `UserCredentialService` 和 `UserCredentialServiceImpl`，迁移密码凭据读取和更新能力。

完成后：

- `UserService` 不再公开密码凭据方法。
- 当前用户改密流程通过 `UserCredentialService` 读取和更新密码凭据。
- Auth 密码认证流程继续保持既有行为。

### Step 3: Rewire UserService Write Orchestration

调整 `UserServiceImpl.add` / `UserServiceImpl.update` 内部协作方式，让账号身份和密码凭据写入通过拆分后的 Service 完成。

完成后：

- `UserService` 仍是用户保存事务入口。
- Controller 不新增跨 Service 写事务编排。
- 用户、角色、身份、凭据和签名写入顺序保持可测试。

### Step 4: Tighten Architecture Tests And Documentation

补充架构测试和文档口径，固定拆分后的职责边界。

完成后：

- `UserService` 不再出现身份和凭据专用公开方法。
- `UserIdentityService` / `UserCredentialService` 方法命名符合 Service 命名规约。
- `SYSTEM-REQUIREMENTS.md` 说明用户主体、身份和凭据的 Service 边界。

## 5. Verification

每个步骤完成后至少运行：

```bash
mvn -q -pl sandwish-biz,sandwish-admin-api -am test
```

涉及 infra 持久化协作时追加：

```bash
mvn -q -pl sandwish-infra -am test
```

收口前固定检查：

- `UserService` 是否只承载用户主体和角色关系。
- `UserIdentityService` 是否只承载账号身份。
- `UserCredentialService` 是否只承载认证凭据。
- Controller 是否没有新增跨 Service 写事务编排。
- TODO 项是否随对应代码、测试或文档改动同步删除、拆分或收窄。

## 6. Rollback Boundary

每个 Step 独立提交。若某一步出现行为回退，只回滚该 Step 对应提交，不回滚已经完成且测试通过的前置拆分。

## 7. Open Items

无
