# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `auth-oauth-access-token`：新增 OAuth access token 持久化端口
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth
    sandwish-infra/src/main/java/com/github/thundax/modules/auth
    sandwish-infra/src/test/java/com/github/thundax/modules/auth
  - 处理动作：新增 OAuthAccessToken Entity/DAO/DO/Mapper/Assembler/DAO implementation 和基础测试
  - 验收点：OAuth access token 可按 tokenId/tokenHash 查询并写回状态
  - 重要度：10/10

- [ ] `auth-oauth-token-grant`：正规化 OAuth2 token grant 流程
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/test/java/com/github/thundax/modules/auth
  - 处理动作：扩展 token 请求字段，补 clientSecret、grantType、redirectUri、PKCE 和 refresh token grant 校验
  - 验收点：authorization_code 和 refresh_token grant 均走标准 token 入口并生成 OAuth token 状态
  - 重要度：10/10

- [ ] `auth-oauth-token-revoke-introspection`：补 OAuth token revoke 和 introspection 状态
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/test/java/com/github/thundax/modules/auth
  - 处理动作：revoke access/refresh token 状态，introspection 按 OAuth access token 状态返回 active
  - 验收点：revoke 后 token introspection 返回 active=false
  - 重要度：9/10

- [ ] `auth-oauth-token-cleanup`：清理 OAuth token 正规化现场
  - 范围文件：TODO.md
    docs/30-designs/AUTH-OAUTH-TOKEN-RUNBOOK.md
  - 处理动作：删除已完成 TODO 和失效 RUNBOOK
  - 验收点：TODO 为空或只保留未完成项，仓库无失效 OAuth token RUNBOOK
  - 重要度：10/10

## 待审阅任务项

## 待讨论项
