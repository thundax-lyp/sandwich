# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `member-login-form`：新增前台会员登录表单运行态
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/MemberLoginForm.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberLoginFormDao.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dao/MemberLoginFormDaoImpl.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/member/persistence/dao/MemberLoginFormDaoImplTest.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/utils/RsaSessionUtils.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/service/SessionCacheSupport.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/service/impl/SessionCacheSupportImpl.java`
    - `docs/10-requirements/MEMBER-REQUIREMENTS.md`
    - `docs/20-database/MEMBER-DATABASE-DESIGN.md`
  - 处理动作：建立 `MemberLoginForm` Redis / JetCache 运行态，承载 `loginToken`、验证码、短信验证码、邮箱验证码和登录前置密钥。
  - 验收点：登录前置验证码和密钥均通过 `loginToken` 关联，不依赖 HTTP session。
  - 重要度：10/10

- [ ] `member-register-api`：实现前台会员注册入口
  - 范围文件：
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/RegisterController.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/request/MemberAccountRegisterRequest.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/request/MemberMobileRegisterRequest.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/request/MemberEmailRegisterRequest.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/request/MemberRegisterSmsCodeRequest.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/request/MemberRegisterEmailCodeRequest.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/response/MemberRegisterResponse.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/assembler/MemberRegisterInterfaceAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberRegistrationService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberRegistrationServiceImpl.java`
    - `sandwish-front-api/src/test/java/com/github/thundax/modules/member/controller/RegisterControllerContractTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/member/service/impl/MemberRegistrationServiceImplTest.java`
  - 处理动作：实现账号密码注册、手机号注册、邮箱注册和注册验证码发送入口。
  - 验收点：注册请求固定要求 `name`，注册后 `Member.status = ACTIVE`，账号密码注册校验图形验证码并解密传输密码，手机号和邮箱注册发送验证码前必须校验图形验证码。
  - 重要度：10/10

- [ ] `member-token-model`：新增会员认证会话与 token 模型
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/MemberAuthSession.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/MemberAccessToken.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/MemberRefreshToken.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/enums/MemberAuthSessionStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/enums/MemberAccessTokenStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/enums/MemberRefreshTokenStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberAuthSessionDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberAuthSessionRuntimeDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberAccessTokenDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberRefreshTokenDao.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/member/entity/MemberCredentialTest.java`
    - `db/data/system.sql`
    - `docs/10-requirements/MEMBER-REQUIREMENTS.md`
  - 处理动作：建立会员登录后的认证会话、access token 和 refresh token 业务模型。
  - 验收点：登录成功可创建认证会话、访问 token 和刷新 token，状态枚举对齐后台认证生命周期。
  - 重要度：10/10

- [ ] `member-token-infra`：新增会员认证会话与 token 持久化
  - 范围文件：
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dataobject/MemberAuthSessionDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dataobject/MemberAccessTokenDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dataobject/MemberRefreshTokenDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/mapper/MemberAuthSessionMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/mapper/MemberAccessTokenMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/mapper/MemberRefreshTokenMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberAuthSessionPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberAccessTokenPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberRefreshTokenPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dao/MemberAuthSessionDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dao/MemberAuthSessionRuntimeDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dao/MemberAccessTokenDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dao/MemberRefreshTokenDaoImpl.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/member/persistence/assembler/MemberAuthSessionPersistenceAssemblerTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/member/persistence/assembler/MemberAccessTokenPersistenceAssemblerTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/member/persistence/assembler/MemberRefreshTokenPersistenceAssemblerTest.java`
    - `db/schema/member.sql`
    - `docs/20-database/MEMBER-DATABASE-DESIGN.md`
  - 处理动作：新增 `member_auth_session`、`member_access_token` 和 `member_refresh_token` 的持久化与运行态实现。
  - 验收点：token 值唯一，认证会话和 token 可按 `memberId`、`sessionId`、`status` 查询和更新。
  - 重要度：10/10

- [ ] `member-auth-api`：实现前台 token 登录、刷新、登出和登录状态
  - 范围文件：
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/LoginController.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/request/MemberAccountLoginRequest.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/request/MemberSmsLoginRequest.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/request/MemberRefreshTokenRequest.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/response/MemberTokenResponse.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/controller/response/MemberLoginStatusResponse.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/assembler/MemberLoginInterfaceAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberAuthService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberAuthServiceImpl.java`
    - `sandwish-front-api/src/test/java/com/github/thundax/modules/member/controller/LoginControllerContractTest.java`
    - `sandwish-front-api/src/test/java/com/github/thundax/modules/member/assembler/MemberLoginInterfaceAssemblerTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/member/service/impl/MemberAuthServiceImplTest.java`
  - 处理动作：实现账号密码登录、短信登录、refresh token、token 登出和当前会员登录状态接口。
  - 验收点：登录成功返回 access token、refresh token 和过期信息，登出按 token 撤销，响应模型不返回敏感凭据。
  - 重要度：10/10

- [ ] `member-security-context`：实现前台 access token 请求上下文
  - 范围文件：
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/security/MemberSecurityContext.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/security/MemberSpringPrincipal.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/security/FrontSpringSecurityConfiguration.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/security/MemberAccessTokenAuthenticationFilter.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/security/MemberSpringAuthenticationFilter.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/security/MemberSpringAuthenticationProvider.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/service/MemberAccessSupport.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/service/impl/MemberAccessSupportImpl.java`
    - `sandwish-front-api/src/test/java/com/github/thundax/modules/member/security/MemberSecurityContextTest.java`
    - `docs/00-governance/CONTEXT-PROPAGATION-RULES.md`
  - 处理动作：通过 member access token 建立 `MemberSecurityContext`，移除前台请求中的 HTTP session cache 语义。
  - 验收点：受保护前台接口必须通过 access token 建立当前会员上下文，`MemberSecurityContext` 只承载当前请求会员身份。
  - 重要度：10/10

- [ ] `member-auth-docs-closeout`：同步文档并清理 RUNBOOK 现场
  - 范围文件：
    - `docs/10-requirements/MEMBER-REQUIREMENTS.md`
    - `docs/20-database/MEMBER-DATABASE-DESIGN.md`
    - `docs/00-governance/CONTEXT-PROPAGATION-RULES.md`
    - `docs/30-designs/RUNBOOK-FRONT-MEMBER-AUTH-REFORM.md`
    - `TODO.md`
  - 处理动作：将已实现的前台会员认证规则收敛到正式需求、数据库设计和上下文治理文档，并删除临时 RUNBOOK。
  - 验收点：正式文档覆盖目标模型和 API token 上下文规则，`TODO.md` 删除或收窄已完成项，`RUNBOOK-FRONT-MEMBER-AUTH-REFORM.md` 被清理。
  - 重要度：10/10

## 待讨论项
