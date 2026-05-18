# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项

- [ ] `integration-docker`：同步 Docker 运行配置
  - 范围文件：deploy/docker-compose.yml
  - 范围文件：deploy/images/admin-api.Dockerfile
  - 范围文件：deploy/images/front-api.Dockerfile
  - 范围文件：deploy/images/open-api.Dockerfile
  - 处理动作：代码和 profile 变更完成后同步 API Docker 运行配置
  - 验收点：Docker 运行配置能加载最新 jar、profile 和集成测试相关运行参数
  - 重要度：9/10

## 待审阅任务项

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
