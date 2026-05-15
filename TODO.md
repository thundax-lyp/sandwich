# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项


## 待审阅任务项

- [ ] `open-api-architecture-tests`：补齐 Open API 模块架构和契约测试
  - 范围文件：
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/ApiSurfaceArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/ExceptionLayeringArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/InterfaceAssemblerArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/RequestAnnotationArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/ResponseAnnotationArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/ServletRegistrationArchitectureTest.java`
  - 处理动作：让新入口模块被 API surface、异常分层、Request/Response、Assembler 和 Servlet 注册门禁覆盖。
  - 验收点：`mvn -pl sandwish-open-api -am test` 包含架构测试且全部通过。
  - 重要度：9/10

- [ ] `open-api-runbook-cleanup`：完成 Open API RUNBOOK 现场清理
  - 范围文件：
    - `TODO.md`
    - `docs/30-designs/RUNBOOK-OPEN-API-MODULE.md`
    - `docs/10-requirements/OPEN-API-REQUIREMENTS.md`
    - `docs/30-designs/OPEN-API-AUTH-DESIGN.md`
    - `docs/30-designs/OPEN-API-ERROR-CODE-DESIGN.md`
    - `docs/20-database/OPEN-API-DATABASE-DESIGN.md`
  - 处理动作：执行完成后删除 RUNBOOK、删除或收窄已完成 TODO，并确认文档口径与代码一致。
  - 验收点：`TODO.md` 不保留已完成任务，RUNBOOK 被清理，`git status --short` 无无关修改。
  - 重要度：8/10

## 待讨论项
