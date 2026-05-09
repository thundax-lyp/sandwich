# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `service-method-assist/async-task`：规约化 AsyncTaskService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/AsyncTaskService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/AsyncTaskServiceImpl.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/assist/service/impl/AsyncTaskServiceImplTest.java
  - 处理动作：新增 AsyncTask Query、Command 和 AsyncTaskServiceImplTest，规约化异步任务创建、状态推进、查询和条件清理入口
  - 验收点：AsyncTaskService 符合 Query / PageQuery / PageResult / Command 三态规则；`mvn -pl sandwish-biz -am test` 通过
  - 重要度：7/10

- [ ] `service-method-admin/async-task`：同步 AsyncTaskController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/AsyncTaskController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/AsyncTaskIdRequest.java
  - 处理动作：将 AsyncTask API Request 显式组装为 Query 或 Command，并适配 AsyncTaskService 新契约
  - 验收点：AsyncTaskController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：7/10

- [ ] `service-method-final/guards`：收口架构约束和残留扫描
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceNamingArchitectureTest.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceMethodParameterArchitectureTest.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceMethodModelArchitectureTest.java
  - 范围文件：TODO.md
  - 范围文件：docs/30-designs/RUNBOOK-SERVICE-METHOD-REFORM.md
  - 处理动作：清理临时放行清单、删除已完成 TODO、删除 RUNBOOK，并执行最终残留扫描
  - 验收点：`mvn install` 通过；`git status --short` 干净；不残留已完成 TODO 或临时执行说明
  - 重要度：10/10

## 待讨论项
