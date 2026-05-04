# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `auth-oauth-authorization`：搬运 OAuth authorization 和授权码运行态
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth
    sandwish-infra/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/test/java/com/github/thundax/modules/auth
  - 处理动作：新增 OAuthAuthorization 模型、Redis/DAO 端口、authorize/decision/revoke/token 主流程
  - 验收点：授权请求可生成授权视图，用户决策后可换取 token 或被撤销
  - 重要度：10/10

- [ ] `auth-refresh-token`：搬运 refresh token 和 token refresh 能力
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth
    sandwish-infra/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/test/java/com/github/thundax/modules/auth
  - 处理动作：新增 OAuthRefreshToken/RefreshTokenSession 模型和 refresh token 刷新访问 token 流程
  - 验收点：有效 refresh token 可刷新访问 token，失效 refresh token 被拒绝
  - 重要度：10/10

- [ ] `auth-session-command`：搬运 session invalidate user/tenant/token 能力
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth
    sandwish-infra/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/test/java/com/github/thundax/modules/auth
  - 处理动作：扩展 AuthSession 和运行态 DAO，支持按用户、租户和 token 失效会话
  - 验收点：失效动作同步清理运行态、释放权限会话并写回审计状态
  - 重要度：9/10

- [ ] `auth-cleanup`：清理 OAuth/Auth 迁移现场
  - 范围文件：TODO.md
    docs/30-designs/AUTH-OAUTH-MIGRATION-RUNBOOK.md
    sandwish-biz/src/main/java/com/github/thundax/modules/auth
    sandwish-infra/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth
  - 处理动作：删除已完成 TODO、清理失效 RUNBOOK、空目录和迁移临时口径
  - 验收点：TODO 只保留未完成项，代码和文档无失效 OAuth/Auth 迁移说明
  - 重要度：10/10

## 待审阅任务项

## 待讨论项
