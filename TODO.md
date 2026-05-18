# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项

- [ ] `front-it`：补齐前台认证与注册集成测试
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontAuthSessionIT.java
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontRegisterAccountIT.java
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontRegisterMobileIT.java
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontRegisterEmailIT.java
  - 处理动作：覆盖预认证会话、账号登录、短信登录替身链路、token refresh、login status、check-login、logout、账号/手机号/邮箱注册接口
  - 验收点：LoginController 和会员注册链路具备成功链路；验证码、凭据、token、重复账号/手机号/邮箱失败链路具备断言
  - 重要度：10/10

## 待审阅任务项

- [ ] `open-api-it`：补齐开放接口签名与提交集成测试
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/auth/OpenApiSignatureIT.java
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/submission/OpenSubmissionQueryIT.java
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/submission/OpenSubmissionMutationIT.java
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/submission/OpenSubmissionUploadIT.java
  - 处理动作：覆盖签名成功、签名失败、nonce 重放、IP 白名单失败、开放 submission 查询/创建/状态变更/图片上传接口
  - 验收点：OpenAPI 认证过滤链路和开放提交业务接口通过真实 HTTP 入口完成成功和失败断言
  - 重要度：10/10

- [ ] `integration-docs`：补齐接口覆盖清单和最终运行说明
  - 范围文件：deploy/integration/API-COVERAGE.md
  - 范围文件：deploy/integration/README.md
  - 处理动作：新增 Controller 接口到 `*IT.java` 的覆盖映射清单，并同步最终命令、数据装载、覆盖清单和排错说明
  - 验收点：admin、front、open 全部业务接口均能在清单中定位到集成测试文件；README 与实际 compose、SQL、profile 和 Maven 命令一致
  - 重要度：10/10

- [ ] `integration-docker`：同步 Docker 运行配置
  - 范围文件：deploy/docker-compose.yml
  - 范围文件：deploy/images/admin-api.Dockerfile
  - 范围文件：deploy/images/front-api.Dockerfile
  - 范围文件：deploy/images/open-api.Dockerfile
  - 处理动作：代码和 profile 变更完成后同步 API Docker 运行配置
  - 验收点：Docker 运行配置能加载最新 jar、profile 和集成测试相关运行参数
  - 重要度：9/10

- [ ] `integration-image-files`：同步 API 镜像 tar 文件
  - 范围文件：deploy/image-files/manifest.txt
  - 范围文件：deploy/image-files/sandwish-admin-api-dev.tar
  - 范围文件：deploy/image-files/sandwish-front-api-dev.tar
  - 范围文件：deploy/image-files/sandwish-open-api-dev.tar
  - 范围文件：deploy/image-files/sandwish-nginx-dev.tar
  - 处理动作：代码和 Docker 配置变更完成后重建并同步 API 相关镜像 tar
  - 验收点：`deploy/image-files/*.tar` 与最新代码、Dockerfile 和 manifest 一致
  - 重要度：9/10

- [ ] `integration-cleanup`：清理集成测试 RUNBOOK
  - 范围文件：docs/30-designs/RUNBOOK-INTEGRATION-TEST.md
  - 范围文件：TODO.md
  - 处理动作：集成测试体系完成后删除 RUNBOOK 并删除或收窄对应 TODO
  - 验收点：无残留 RUNBOOK 引用，`TODO.md` 不保留已完成任务
  - 重要度：7/10

## 待讨论项
