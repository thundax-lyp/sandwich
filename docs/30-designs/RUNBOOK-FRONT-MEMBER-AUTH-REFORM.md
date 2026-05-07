# RUNBOOK Front Member Auth Reform

## 1. Purpose

本文档定义前台会员认证目标模型和 API token 认证执行手册。

本 RUNBOOK 固定服务于下一步拆解 `TODO.md` 待审阅任务。文件级任务、执行状态和人工审阅队列固定进入 `TODO.md`，不在本文档记录完成历史。

## 2. Scope

最终目标：

- 前台认证不依赖 HTTP session。
- 前台认证流程与后台认证流程保持一致的模型拆分和生命周期。
- 前台会员主体、登录标识、认证凭据、登录表单、认证会话、访问 token 和刷新 token 分离建模。
- front-api 请求通过 member access token 建立 `MemberSecurityContext`。
- 登录表单、验证码、短信验证码和密钥通过 `MemberLoginForm` 承载，不通过 HTTP session 承载。

边界：

- 前台认证不支持 OAuth2 client。
- 前台认证不复用后台 `Auth` 的 `LoginForm`、`AuthSession`、access token 或 refresh token 数据结构。
- 前台认证可以对照后台 `UserIdentity`、`UserCredential`、`LoginForm`、`AuthSession`、access token 和 refresh token 的字段与规则搬运，但必须使用 member 域命名。
- 本 RUNBOOK 不定义前端页面和 admin-web 改造。
- 本 RUNBOOK 不引入多租户模型。

已确认业务口径：

- `Member` 是前台会员主体，只保留必要业务数据。
- `MemberIdentity` 固定支持 `ACCOUNT`、`MOBILE`、`EMAIL`。
- `MemberIdentity` 固定使用 `identity_type + identity_value` 全局唯一约束。
- `MOBILE` 和 `EMAIL` 登录标识权威数据固定在 `MemberIdentity`。
- `MemberGender` 固定使用 `MALE`、`FEMALE`、`PRIVATE`。
- `MemberLoginForm` 固定承载图形验证码、短信验证码、邮箱验证码和登录前置密钥。
- 会员注册是 front-api 专属能力。
- 会员注册固定要求 `name`。
- 会员注册完成后的初始 `MemberStatus` 固定为 `ACTIVE`。
- 首轮注册固定支持账号密码注册、手机号注册和邮箱注册。

## 3. Target Model

前台会员认证固定使用以下业务对象：

- `Member`
- `MemberIdentity`
- `MemberCredential`
- `MemberLoginForm`
- `MemberAuthSession`
- `MemberAccessToken`
- `MemberRefreshToken`
- `MemberSecurityContext`

### 3.1 Member

`Member` 是前台会员主体，只承载必要业务数据。

固定规则：

- `Member` 不承载登录标识。
- `Member` 不承载认证凭据。
- `Member` 不承载验证码、短信验证码、登录表单密钥或 HTTP session 数据。
- `Member` 不承载 access token 或 refresh token。
- `MemberStatus` 固定表达会员生命周期：`PENDING`、`ACTIVE`、`SUSPENDED`、`CLOSED`。
- `MemberGender` 固定表达会员性别：`MALE`、`FEMALE`、`PRIVATE`。

字段迁移规则：

- `loginName` 迁移到 `MemberIdentity.identityValue`。
- `loginPass` 迁移到 `MemberCredential.credentialValue`。
- `email` 和 `mobile` 作为登录方式时迁移到 `MemberIdentity`。
- `registerIp`、`registerDate`、`lastLoginIp`、`lastLoginDate` 和 `loginCount` 迁移到认证会话或认证统计模型。
- `address` 和 `zipcode` 属于私密数据，固定不进入会员认证模型。

### 3.2 MemberIdentity

`MemberIdentity` 是前台会员登录标识。

固定字段：

- `id`
- `memberId`
- `identityType`
- `identityValue`
- `status`

固定规则：

- `identityType` 固定支持 `ACCOUNT`、`MOBILE`、`EMAIL`。
- `identity_type + identity_value` 全局唯一。
- `MOBILE` 和 `EMAIL` 的登录权威数据固定在 `MemberIdentity`，不从 `Member.email` 或 `Member.mobile` 读取认证依据。
- `MemberIdentityStatus` 对照 `UserIdentityStatus`，固定支持 `ENABLED`、`DISABLED`。

### 3.3 MemberCredential

`MemberCredential` 是前台会员认证凭据。

固定字段：

- `id`
- `memberId`
- `identityId`
- `credentialType`
- `credentialValue`
- `status`
- `needChangePassword`
- `failedCount`
- `failedLimit`
- `lockedUntil`
- `expiresAt`
- `lastVerifiedAt`

固定规则：

- `credentialType` 初始固定支持 `PASSWORD`。
- `identity_id + credential_type` 全局唯一。
- `MemberCredentialStatus` 对照 `UserCredentialStatus`，固定支持 `ACTIVE`、`LOCKED`、`EXPIRED`、`DISABLED`。
- 密码和其他敏感凭据不得保存明文。
- 登录失败次数、锁定、过期和最近验证时间固定在 `MemberCredential` 维度维护。

### 3.4 MemberLoginForm

`MemberLoginForm` 是前台登录前置表单临时状态。

固定字段：

- `loginToken`
- `refreshTokenList`
- `captcha`
- `mobile`
- `mobileValidateCode`
- `email`
- `emailValidateCode`
- `expiredSeconds`
- `checkCode`
- `publicKey`
- `privateKey`

固定规则：

- `MemberLoginForm` 使用 Redis / JetCache 运行态存储。
- `MemberLoginForm` 不建数据库表。
- `MemberLoginForm` 不是认证会话。
- 登录前必须先创建 `MemberLoginForm`。
- 登录、图形验证码、短信验证码和密钥读取固定通过 `loginToken` 关联。
- 登录成功或登录表单失效后必须清理 `MemberLoginForm`。
- 前台缓存 key 必须使用 member/front 专用前缀，不得使用后台 auth 或 admin session 前缀。

### 3.5 MemberAuthSession

`MemberAuthSession` 是前台会员认证会话事实。

固定规则：

- 本 RUNBOOK 中的 `session` 和 `sessionId` 固定指前台认证会话，不指 HTTP session。
- 登录成功后必须创建 `MemberAuthSession`。
- `MemberAuthSession` 固定包含数据库审计态和 Redis / JetCache 运行态。
- 数据库审计态保存登录事实、最终最近访问时间、登出、失效和过期状态。
- Redis / JetCache 运行态服务每次 API 请求的 token 校验和认证会话 touch。
- `MemberAuthSessionStatus` 对照后台 `AuthSessionStatus`。

### 3.6 MemberAccessToken

`MemberAccessToken` 是前台 API 请求访问 token。

固定规则：

- 登录成功后必须创建 `MemberAccessToken`。
- front-api 请求必须通过 access token 建立 `MemberSecurityContext`。
- access token 必须能定位 `MemberAuthSession` 和 `Member`。
- access token 过期、登出或撤销后不得继续访问受保护前台接口。

### 3.7 MemberRefreshToken

`MemberRefreshToken` 是前台刷新 token。

固定规则：

- 登录成功后必须创建 `MemberRefreshToken`。
- refresh token 必须关联 access token、认证会话和会员。
- refresh token 固定支持过期、使用和撤销状态。
- 刷新 access token 时必须校验 refresh token 状态。

## 4. Database Plan

前台会员认证数据库表固定使用 `member_` 前缀。

目标表：

- `member_member`
- `member_identity`
- `member_credential`
- `member_auth_session`
- `member_access_token`
- `member_refresh_token`

不建表对象：

- `MemberLoginForm`

固定索引：

- `member_identity` 建立 `identity_type + identity_value` 联合唯一索引。
- `member_credential` 建立 `identity_id + credential_type` 联合唯一索引。
- token 表必须建立 token 值唯一索引。
- session 和 token 表必须建立 `member_id`、`session_id`、`status` 相关查询索引。

## 5. Front API Plan

前台认证入口固定改成 API 风格。

目标入口：

- 创建登录表单。
- 刷新登录表单。
- 获取或刷新图形验证码。
- 发送注册短信验证码。
- 发送注册邮箱验证码。
- 账号密码注册。
- 手机号注册。
- 邮箱注册。
- 账号密码登录。
- 短信登录。
- refresh token 刷新 access token。
- token 登出。
- 当前会员登录状态。

固定规则：

- 会员注册是 front-api 专属能力，不照搬后台创建用户入口。
- 会员注册请求必须填写 `name`。
- 会员注册必须创建 `Member` 主体。
- 会员注册创建的 `Member.status` 固定为 `ACTIVE`。
- 会员注册必须创建至少一个 `MemberIdentity`。
- 账号密码注册必须创建 `MemberCredential(PASSWORD)`。
- 账号密码注册必须携带 `loginToken`。
- 账号密码注册必须校验 `MemberLoginForm` 中的图形验证码。
- 账号密码注册密码必须加密传输。
- 账号密码注册服务端必须通过 `MemberLoginForm` 中的私钥解密密码。
- 账号密码注册写入 `MemberCredential.credentialValue` 前必须按前台密码策略加密凭据值。
- 手机号注册必须携带 `loginToken`。
- 手机号注册必须校验 `MemberLoginForm` 中的短信验证码。
- 发送注册短信验证码前必须提交 `loginToken`、`mobile` 和图形验证码。
- 发送注册短信验证码前必须校验 `MemberLoginForm` 中的图形验证码。
- 注册短信验证码必须保存到 `MemberLoginForm.mobileValidateCode`，并绑定 `MemberLoginForm.mobile`。
- 手机号注册成功后必须创建 `MemberIdentity(MOBILE)`。
- 邮箱注册必须携带 `loginToken`。
- 邮箱注册必须校验 `MemberLoginForm` 中的邮箱验证码。
- 发送注册邮箱验证码前必须提交 `loginToken`、`email` 和图形验证码。
- 发送注册邮箱验证码前必须校验 `MemberLoginForm` 中的图形验证码。
- 注册邮箱验证码必须保存到 `MemberLoginForm.emailValidateCode`，并绑定 `MemberLoginForm.email`。
- 邮箱注册成功后必须创建 `MemberIdentity(EMAIL)`。
- 登录请求必须携带 `loginToken`。
- 账号密码登录通过 `MemberIdentity(ACCOUNT)` 和 `MemberCredential(PASSWORD)` 认证。
- 手机号短信登录通过 `MemberIdentity(MOBILE)` 和 `MemberLoginForm.mobileValidateCode` 认证。
- 邮箱注册和 `MemberIdentity(EMAIL)` 进入本 RUNBOOK；邮箱验证码登录不进入本 RUNBOOK。
- `MemberStatus.ACTIVE` 是允许登录的会员主体状态。
- 认证错误响应不得泄露凭据值、密码哈希、验证码或 token 明文以外的敏感状态。

## 6. Execution Order

本 RUNBOOK 分 8 个阶段完成。每个阶段必须进入 `TODO.md` 拆成文件级任务，经人工审核后执行。

### 6.1 固化 Member 主体最小模型

目标：

- 收敛 `Member` 主体字段。
- 将登录标识、凭据和登录行为从 `Member` 剥离。

执行内容：

- 保留 `MemberStatus` 和 `MemberGender`。
- 完成 `loginName`、`loginPass`、`email`、`mobile`、登录行为字段和非主体资料字段的目标模型迁移。
- 同步 `MemberDO`、`MemberPersistenceAssembler`、`MemberDaoImpl`、测试、`MEMBER-REQUIREMENTS.md`、`MEMBER-DATABASE-DESIGN.md` 和 `db/schema/member.sql`。

验证：

```bash
mvn -pl sandwish-biz,sandwish-infra -am -Dtest=MemberServiceImplTest,MemberPersistenceAssemblerTest -DfailIfNoTests=false -DfailIfNoSpecifiedTests=false test -DskipITs
```

### 6.2 新增 MemberIdentity 和 MemberCredential

目标：

- 建立前台会员登录标识和凭据模型。

执行内容：

- 新增 `MemberIdentity`、`MemberCredential` 和对应枚举。
- 新增 DAO interface、DO、Mapper、DAO implementation 和 PersistenceAssembler。
- 新增 `member_identity` 和 `member_credential` schema。
- 同步初始化字典和文档。
- 将现有登录名和密码逻辑迁移到 identity/credential。

验证：

```bash
mvn -pl sandwish-biz,sandwish-infra -am test -DskipITs
```

### 6.3 新增 MemberLoginForm

目标：

- 建立前台登录前置临时状态。

执行内容：

- 新增 `MemberLoginForm`。
- 新增 `MemberLoginFormDao`。
- 新增 Redis / JetCache DAO implementation。
- 迁移图形验证码、短信验证码、邮箱验证码和密钥存储到 `MemberLoginForm`。
- 登录前置验证码和密钥不依赖 HTTP session。

验证：

```bash
mvn -pl sandwish-biz,sandwish-infra,sandwish-front-api -am test -DskipITs
rg "HttpSession|sessionId|getSessionCache|setSessionCache|RsaSessionUtils" sandwish-front-api/src/main/java/com/github/thundax/modules/member
```

### 6.4 实现会员注册

目标：

- 建立 front-api 会员注册能力。

执行内容：

- 新增会员注册 Request / Response。
- 注册请求固定要求 `name`。
- 注册流程创建 `Member`。
- 注册流程按注册方式创建 `MemberIdentity`。
- 账号密码注册流程创建 `MemberCredential(PASSWORD)`。
- 账号密码注册流程校验 `loginToken` 和图形验证码。
- 账号密码注册流程使用 `MemberLoginForm.privateKey` 解密传输密码。
- 账号密码注册流程使用前台密码策略加密凭据值后写入 `MemberCredential.credentialValue`。
- 新增发送注册短信验证码接口。
- 发送注册短信验证码接口固定接收 `loginToken`、`mobile` 和图形验证码。
- 发送注册短信验证码接口必须先校验图形验证码，再写入 `MemberLoginForm.mobile` 和 `MemberLoginForm.mobileValidateCode`。
- 手机号注册流程校验 `loginToken`、`mobile` 和短信验证码。
- 手机号注册流程创建 `MemberIdentity(MOBILE)`。
- 新增发送注册邮箱验证码接口。
- 发送注册邮箱验证码接口固定接收 `loginToken`、`email` 和图形验证码。
- 发送注册邮箱验证码接口必须先校验图形验证码，再写入 `MemberLoginForm.email` 和 `MemberLoginForm.emailValidateCode`。
- 邮箱注册流程校验 `loginToken`、`email` 和邮箱验证码。
- 邮箱注册流程创建 `MemberIdentity(EMAIL)`。
- 注册流程不得把密码保存到 `Member`。
- 注册流程不得把登录标识保存到 `Member`。
- 注册失败响应不得泄露凭据值或已存在标识的敏感细节。

验证：

```bash
mvn -pl sandwish-biz,sandwish-infra,sandwish-front-api -am test -DskipITs
```

### 6.5 新增 MemberAuthSession、MemberAccessToken 和 MemberRefreshToken

目标：

- 建立前台登录后的 token API 认证态。

执行内容：

- 新增认证会话、access token 和 refresh token 领域对象与枚举。
- 新增 DAO interface、DO、Mapper、DAO implementation 和 runtime DAO。
- 新增 schema 和 data 字典。
- 建立 token 生成、刷新、撤销和认证会话 touch 规则。

验证：

```bash
mvn -pl sandwish-biz,sandwish-infra -am test -DskipITs
```

### 6.6 实现 front-api 认证入口

目标：

- 建立 front-api 登录、刷新、登出和登录状态 API token 入口。

执行内容：

- 实现登录表单、验证码、短信验证码、账号密码登录、短信登录、refresh token 和登出接口。
- 登录成功返回 access token、refresh token 和必要过期信息。
- 登出按 token 撤销，不再 invalidate HTTP session。
- 响应模型不得返回敏感凭据。

验证：

```bash
mvn -pl sandwish-front-api -am test -DskipITs
```

### 6.7 实现 MemberSecurityContext 和请求过滤器

目标：

- front-api 请求通过 access token 建立前台会员上下文。

执行内容：

- 新增 access token 认证过滤器。
- `MemberSecurityContext` 只承载当前请求会员身份，不承载 session cache。
- 受保护前台接口必须通过 token 认证进入。
- 请求过滤器不使用 Spring Security form-login/session 形态。

验证：

```bash
mvn -pl sandwish-front-api -am test -DskipITs
rg "formLogin|HttpSession|invalidate|getSessionCache|setSessionCache" sandwish-front-api/src/main/java
```

### 6.8 收口文档、TODO 和 RUNBOOK

目标：

- 将临时 RUNBOOK 收口为正式需求、数据库设计和 TODO 关闭状态。

执行内容：

- 同步 `MEMBER-REQUIREMENTS.md`。
- 同步 `MEMBER-DATABASE-DESIGN.md`。
- 同步 `CONTEXT-PROPAGATION-RULES.md` 中前台上下文规则。
- 删除或收窄已完成 `TODO.md` 项。
- 清理本 RUNBOOK。

验证：

```bash
mvn -pl sandwish-biz,sandwish-infra,sandwish-front-api -am test -DskipITs
rg "RUNBOOK-FRONT-MEMBER-AUTH-REFORM|HttpSession|session cache|form login" docs sandwish-front-api/src/main/java
git status --short
```

## 7. Verification

阶段性最小验证命令：

```bash
mvn -pl sandwish-biz -am test -DskipITs
mvn -pl sandwish-infra -am test -DskipITs
mvn -pl sandwish-front-api -am test -DskipITs
```

全链路验证命令：

```bash
mvn -pl sandwish-biz,sandwish-infra,sandwish-front-api -am test -DskipITs
```

残留扫描：

```bash
rg "HttpSession|formLogin|getSessionCache|setSessionCache|SessionCacheSupport|RsaSessionUtils" sandwish-front-api/src/main/java
rg "loginName|loginPass|enable_flag" sandwish-biz/src/main/java/com/github/thundax/modules/member sandwish-infra/src/main/java/com/github/thundax/modules/member db/schema/member.sql docs/20-database/MEMBER-DATABASE-DESIGN.md
```

## 8. Open Items

无
