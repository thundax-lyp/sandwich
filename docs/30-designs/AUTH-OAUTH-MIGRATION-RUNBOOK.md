# AUTH OAUTH MIGRATION RUNBOOK

## 1. Purpose

本文档定义从 `../bacon` 搬运 OAuth client、authorization、refresh token、session invalidate、token verify、OAuth2 introspection/userinfo 和多登录方式到 Sandwich 的执行手册。

本次迁移只搬业务能力和端口意图，不搬 `../bacon` 的 `api/application/domain/interfaces/repository` 目录形态。Sandwich 固定保持三层 API 架构：

`HTTP/API -> Controller -> Service -> DAO/Mapper -> Database`

## 2. Target Shape

Auth 扩展后的核心对象固定为：

- `OAuthClient`：OAuth 客户端配置和密钥状态。
- `OAuthAuthorization`：授权请求、授权码和授权决策状态。
- `OAuthRefreshToken`：刷新 token 事实和刷新会话。
- `AuthSession`：用户认证会话，支持按 user/tenant 失效。
- `AccessToken`：访问 token 传输和校验入口。

认证入口固定支持：

- 密码登录。
- 短信登录。
- 企业微信登录。
- GitHub 登录。
- token refresh。
- token verify。
- OAuth2 introspection。
- OAuth2 userinfo。

## 3. Module Mapping

- `sandwish-biz/src/main/java/com/github/thundax/modules/auth`
  - 增加 OAuth client、authorization、refresh token、token verify 和多登录方式的 Entity、DAO、Service 契约。
- `sandwish-infra/src/main/java/com/github/thundax/modules/auth`
  - 增加对应 DO、Mapper、DAO implementation、PersistenceAssembler 和 Redis 运行态实现。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth`
  - 增加 OAuth2、token、登录方式和 session 管理 Controller / Request / Response / Assembler。
- `docs/10-requirements/AUTH-REQUIREMENTS.md`
  - 同步 Auth 范围从后台密码登录扩展到 OAuth2 与多登录方式。
- `docs/20-database/AUTH-DATABASE-DESIGN.md`
  - 同步新增表、字段、索引和 Redis 运行态规则。

## 4. Execution Order

1. 文档先行：扩展 Auth 需求和数据库设计，删除 OAuth2、多登录方式不覆盖的旧口径。
2. 基础对象：落 `OAuthClient`、`OAuthAuthorization`、`OAuthRefreshToken` 及枚举。
3. 持久化端口：落 DAO、DO、Mapper、PersistenceAssembler。
4. 会话失效：扩展 `AuthSession` 和 `AuthSessionRuntimeDao`，支持按 user/tenant/token 失效。
5. token 能力：落 token verify、refresh token、introspection、userinfo。
6. 登录方式：落短信、企业微信、GitHub 登录端口，先形成可替换 provider 边界。
7. API 入口：落 Controller Request/Response/Assembler。
8. 测试收口：每个能力至少有 Service 或 DAO 层测试钉住主流程。
9. 清理现场：删除完成 TODO，清理失效 RUNBOOK、空目录和无用迁移口径。

## 5. Acceptance

- Auth 需求文档不再声明 OAuth2、多登录方式不覆盖。
- 数据库设计文档覆盖新增 OAuth 表和 Redis 运行态。
- Controller 只处理 HTTP 入口、请求绑定和响应组装。
- Service 承接认证、授权、token、session 失效和跨 DAO 编排。
- DAO/Mapper 只承接持久化访问。
- OAuth2 和多登录方式不引入 Bacon 的分层目录。
- 相关 Maven 测试可运行通过。
- `TODO.md` 只保留未完成任务。
