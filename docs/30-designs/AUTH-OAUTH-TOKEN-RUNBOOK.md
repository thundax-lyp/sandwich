# AUTH OAUTH TOKEN RUNBOOK

## 1. Purpose

本文档定义 Sandwich Auth OAuth token 正规化的执行手册。

目标是让 OAuth2 授权码换 token、refresh token、revoke 和 introspection 从“复用后台访问 token 的轻量接口”收敛为具备 OAuth token 状态事实的业务闭环。

## 2. Scope

本轮覆盖：

- `OAuthAccessToken` Entity / DAO / DO / Mapper / PersistenceAssembler。
- OAuth2 token 请求的 `grantType`、`clientSecret`、`redirectUri`、`codeVerifier`、`refreshToken` 字段。
- authorization code grant 校验 client、clientSecret、redirectUri、authorizationCode、PKCE。
- refresh token grant 校验 client、clientSecret、refreshToken 状态。
- revoke access token / refresh token。
- introspection 基于 OAuth access token 状态返回 `active`。
- 对应 Service 和 persistence testcase。

本轮不覆盖：

- OIDC discovery、JWKS 和动态客户端注册。
- 第三方 provider callback 真实接入。
- Auth facade / remote contract。
- 登录安全 challenge 重做。
- 生产 DDL 执行。

## 3. Execution Order

1. 新增 OAuth access token 领域对象和持久化端口。
2. 扩展 OAuth2 token 请求和响应语义。
3. 将 authorization code grant 改为标准 token 请求入口。
4. 将 refresh token grant 改为标准 token 请求入口。
5. 将 revoke 改为按 access / refresh token hash 状态流转。
6. 将 introspection 优先读取 OAuth access token 状态。
7. 补测试并运行 `sandwish-admin-api -am test`。
8. 清理 TODO 和本 RUNBOOK。

## 4. Acceptance

- OAuth access token 有独立状态事实。
- authorization code grant 必须校验 clientSecret、redirectUri 和 PKCE。
- refresh token grant 必须校验 clientSecret 和 refresh token 状态。
- revoke 后 introspection 返回 `active=false`。
- `TODO.md` 只保留未完成任务。
