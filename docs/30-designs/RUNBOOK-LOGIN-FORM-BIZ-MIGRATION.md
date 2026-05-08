# LoginForm Biz Migration RUNBOOK

## 1. Purpose

本文档定义将后台 `LoginForm` 与前台 `MemberLoginForm` 认证前置运行态下沉到 `sandwish-biz` 的一次性执行顺序。

目标是让 admin-api / front-api 的认证入口只承担 HTTP、验证码图片输出、请求响应装配和入口适配；登录表单创建、刷新、验证码文本、短信/邮箱验证码、密钥对和登录表单生命周期固定由 biz 的 `PreAuthSessionService` 承载。

## 2. Scope

本 RUNBOOK 覆盖：

- 后台登录表单运行态 `LoginForm`
- 前台会员登录表单运行态 `MemberLoginForm`
- 后台验证码文本生成、保存、读取和校验
- 前台验证码文本生成、保存和校验
- 前台注册短信验证码、邮箱验证码生成和校验
- 后台 SM2 登录表单密钥生成和私钥读取
- 前台 RSA 登录表单密钥生成、私钥读取和密码解密
- admin-api / front-api 认证入口对新 biz Service 的委托
- 对应单元测试、Controller contract 测试和架构残留扫描

本 RUNBOOK 不覆盖：

- 验证码图片绘制和 HTTP 输出
- Spring Security filter、`UserAccessHolder`、`MemberSecurityContext`
- 企业微信、GitHub 等入口 provider 适配
- `PrincipalIdentity` / `PrincipalCredential` 认证模型
- `AuthSession`、`MemberAuthSession`、access token、refresh token
- Redis / JetCache DAO implementation 物理存储方式
- 数据库结构变更

## 3. Current Boundary

当前目标态基础已经存在：

- `LoginForm`、`MemberLoginForm`、`LoginFormDao`、`MemberLoginFormDao` 已在 `sandwish-biz`。
- `LoginFormDaoImpl`、`MemberLoginFormDaoImpl` 已在 `sandwish-infra`。
- `AdminAuthServiceImpl` 在 `sandwish-admin-api` 直接编排 `LoginFormDao`。
- `MemberAuthServiceImpl` 与 `MemberRegistrationServiceImpl` 在 `sandwish-front-api` 直接编排 `MemberLoginFormDao`。
- `RsaSessionUtils` 在 `sandwish-front-api` 直接读取和更新 `MemberLoginFormDao`。

目标边界固定为：

- `sandwish-biz` 承载登录表单业务 Service 和认证前置运行态规则。
- `sandwish-infra` 只实现登录表单 DAO 存储。
- `sandwish-admin-api` 保留 `CaptchaController` 图片输出、`AuthController` 请求绑定和 `AdminAuthServiceImpl` 入口委托。
- `sandwish-front-api` 保留会员 Controller、Request / Response / InterfaceAssembler 和入口安全适配。

## 4. Design Decisions

- 固定新增一个 `PreAuthSessionService`，统一承载后台用户和前台会员认证前置会话。
- 登录表单发生在 principal 认证完成之前，`Principal*` 名称固定留给认证主体、登录标识和认证凭据模型。
- 后台 `PreAuthSessionService` 使用 SM2 生成登录表单密钥，保持后台接口响应字段不变。
- 前台会员 `PreAuthSessionService` 使用 RSA 生成登录表单密钥，保持前台接口响应字段不变。
- 后台在线用户数量限制仍读取 `AccessTokenDao.count()`；该规则属于后台登录表单创建前置校验，可以由 `PreAuthSessionService` 编排。
- `PreAuthSessionService` 创建登录前置会话前必须先检查 pre-auth session 容量；容量满时直接拒绝创建登录表单。
- 登录成功后必须释放对应 `PreAuthSession`；登录失败保留当前 `PreAuthSession`，由 TTL、刷新或显式删除收口。
- 白名单验证码规则归属登录表单 Service。
- 入口模块不得直接依赖 `LoginFormDao` 或 `MemberLoginFormDao`。
- 验证码图片输出仍由 Controller 根据 Service 返回的验证码文本完成。

## 5. Execution Order

### 5.1 冻结当前行为

执行前先确认当前行为和残留位置：

```bash
rg -n "LoginFormDao|MemberLoginFormDao|RsaSessionUtils|createLoginForm|refreshLoginForm|validateCaptcha|createCaptcha" sandwish-biz sandwish-admin-api sandwish-front-api sandwish-infra -g '!target/**'
mvn -pl sandwish-biz,sandwish-admin-api,sandwish-front-api,sandwish-infra -am test
```

验收点：

- 当前测试基线可运行，或失败点与本迁移无关且记录在提交说明中。
- 入口模块直接依赖登录表单 DAO 的文件已全部列入后续步骤。

### 5.2 落地后台 `PreAuthSessionService`

范围文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PreAuthSessionService.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PreAuthSessionServiceImpl.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/auth/service/impl/PreAuthSessionServiceImplTest.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AdminAuthServiceImpl.java`
- `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/security/AuthPermissionLifecycleTest.java`

处理动作：

- 将 `AdminAuthServiceImpl` 中后台登录表单创建、刷新、删除、验证码、短信验证码和私钥读取逻辑搬入 biz `PreAuthSessionServiceImpl`。
- `AdminAuthServiceImpl` 保留原公开方法，内部委托 `PreAuthSessionService`。
- 测试覆盖创建、刷新、验证码校验、短信验证码校验和私钥读取。

验收点：

- `AdminAuthServiceImpl` 不直接依赖 `LoginFormDao`。
- 后台 Controller contract 测试不需要改变 HTTP 签名。
- `mvn -pl sandwish-biz,sandwish-admin-api,sandwish-infra -am test` 通过。

### 5.3 扩展前台会员认证前置会话能力

范围文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PreAuthSessionService.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PreAuthSessionServiceImpl.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/auth/service/impl/PreAuthSessionServiceImplTest.java`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberAuthServiceImpl.java`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberRegistrationServiceImpl.java`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/utils/RsaSessionUtils.java`

处理动作：

- 将 `MemberAuthServiceImpl` 中会员登录表单创建、刷新、验证码生成和验证码校验逻辑搬入 biz `PreAuthSessionServiceImpl`。
- 将 `MemberRegistrationServiceImpl` 中验证码、短信验证码、邮箱验证码和登录表单删除逻辑改为委托 `PreAuthSessionService`。
- 将 `RsaSessionUtils` 的密钥更新、私钥读取和密码解密能力搬入 `PreAuthSessionService` 后删除该工具类。
- `MemberAuthServiceImpl` 保留前台登录、token 和 session 编排，只委托登录表单前置规则。

验收点：

- `sandwish-front-api` 不直接依赖 `MemberLoginFormDao`。
- `RsaSessionUtils` 删除或不再被生产代码引用。
- 前台登录和注册入口公开 HTTP 签名不变。
- `mvn -pl sandwish-biz,sandwish-front-api,sandwish-infra -am test` 通过。

### 5.4 清理入口模块残留依赖

范围文件：

- `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/AdminAuthService.java`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/MemberAuthService.java`
- `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/testsupport/InMemoryLoginFormDaoImpl.java`
- `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/controller/AuthControllerContractTest.java`
- `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/controller/CaptchaControllerContractTest.java`
- `sandwish-front-api/src/test/java`

处理动作：

- 调整测试支撑，使入口测试依赖新 biz Service 或保留在测试范围内的 in-memory DAO。
- 只在确有必要时收窄 `AdminAuthService` / `MemberAuthService` 的登录表单方法；本轮优先保持入口接口稳定。
- 清理不再需要的 import、mock 和测试 helper。

验收点：

- 生产代码扫描中 `LoginFormDao` 只出现在 `sandwish-biz` 契约和 `sandwish-infra` 实现。
- 生产代码扫描中 `MemberLoginFormDao` 只出现在 `sandwish-biz` 契约和 `sandwish-infra` 实现。
- 测试 helper 不影响生产依赖边界。

### 5.5 文档同步

范围文件：

- `docs/10-requirements/AUTH-REQUIREMENTS.md`
- `docs/20-database/AUTH-DATABASE-DESIGN.md`

处理动作：

- 将 Auth 需求文档中的登录表单 Service 归属更新为 biz auth Service。
- 将 `PreAuthSession` 容量控制、容量满拒绝登录前置会话、登录成功释放的目标规则写入需求文档。
- 数据库设计文档继续明确 `LoginForm` / `MemberLoginForm` 是 Redis / JetCache 运行态，不建立数据库表。
- 文档只写目标态，不写迁移历史。

验收点：

- 文档不保留“从入口模块迁移到 biz”的历史描述。
- 文档明确 Controller 只做入口适配和验证码图片输出。
- 文档明确 `PreAuthSession` 与登录后 `AuthSession` / token 的容量控制边界。

### 5.6 收口验证

执行命令：

```bash
rg -n "LoginFormDao|MemberLoginFormDao|RsaSessionUtils" sandwish-admin-api/src/main/java sandwish-front-api/src/main/java -g '!target/**'
rg -n "createLoginForm|refreshLoginForm|validateCaptcha|createCaptcha" sandwish-biz sandwish-admin-api sandwish-front-api -g '!target/**'
mvn test
```

验收点：

- 入口模块生产代码不直接依赖登录表单 DAO。
- 登录表单核心业务方法在 biz Service 中可测试。
- 全量 Maven 测试通过。
- 已完成 TODO 删除或收窄。
- 本 RUNBOOK 和迁移现场在最终提交中清理。

## 6. Commit Plan

- `Feat(auth): 下沉后台登录表单服务`
- `Feat(auth): 下沉会员登录表单服务`
- `Refactor(auth): 清理入口登录表单DAO依赖`
- `Docs(auth): 同步登录表单目标文档`
- `Docs(auth): 收口登录表单迁移任务`

提交名称可以按实际拆分调整，但每个提交必须只表达一个明确能力变化。

## 7. Open Items

无
