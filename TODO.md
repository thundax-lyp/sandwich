# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Audit 3.3 biz-core`：新增 Audit 领域模型和 Service 契约
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/valueobject/AuditObjectRef.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/valueobject/AuditSnapshot.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/valueobject/AuditField.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/valueobject/AuditChangedField.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/enums/AuditAction.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/enums/AuditOperatorType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/AuditMeta.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/AuditLog.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/dao/AuditMetaDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/dao/AuditLogDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/service/AuditService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/service/command/CreateAuditLogCommand.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/service/query/AuditLogQuery.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/service/query/AuditMetaQuery.java`
  - 处理动作：新增 Audit 领域对象、值对象、枚举、DAO 契约、Service 契约和 Service 输入模型。
  - 验收点：Audit biz 契约可编译；执行 `mvn -pl sandwish-biz -am compile`。
  - 重要度：10/10

- [ ] `Audit 3.3 infra-core`：新增 Audit 持久化实现
  - 范围文件：
    - `sandwish-infra/src/main/java/com/github/thundax/modules/audit/persistence/dataobject/AuditMetaDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/audit/persistence/dataobject/AuditLogDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/audit/persistence/mapper/AuditMetaMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/audit/persistence/mapper/AuditLogMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/audit/persistence/assembler/AuditMetaPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/audit/persistence/assembler/AuditLogPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/audit/persistence/dao/AuditMetaDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/audit/persistence/dao/AuditLogDaoImpl.java`
  - 处理动作：实现 `audit_meta` / `audit_log` 的 DO、Mapper、assembler 和 DAO implementation。
  - 验收点：Audit infra 持久化实现可编译；执行 `mvn -pl sandwish-infra -am compile`。
  - 重要度：10/10

- [ ] `Audit 3.4 runtime`：新增 Audit 注解运行时
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/annotation/AuditLog.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/AuditLogAspect.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/AuditExpressionEvaluator.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/AuditObjectLoader.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/AuditObjectLoaderRegistry.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/AuditSnapshotAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/AuditSnapshotAssemblerRegistry.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/AuditDiffService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/AuditOperatorResolver.java`
  - 处理动作：实现注解、表达式解析、loader/assembler registry、diff 和操作者解析。
  - 验收点：Audit 运行时可编译；执行 `mvn -pl sandwish-biz -am compile`。
  - 重要度：10/10

- [ ] `Audit 3.5 sys-access`：接入系统管理目标对象审计
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/UserAuditObjectLoader.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/UserAuditSnapshotAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/RoleAuditObjectLoader.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/RoleAuditSnapshotAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/MenuAuditObjectLoader.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/MenuAuditSnapshotAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/DepartmentAuditObjectLoader.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/DepartmentAuditSnapshotAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/DictAuditObjectLoader.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/sys/DictAuditSnapshotAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DepartmentServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DictServiceImpl.java`
  - 处理动作：为系统管理目标对象补 loader、snapshot assembler，并在单对象写方法上声明 Audit 注解。
  - 验收点：系统管理审计接入代码可编译；执行 `mvn -pl sandwish-biz -am compile`。
  - 重要度：10/10

- [ ] `Audit 3.5 assist-member-access`：接入 AsyncTask 和 Member 审计
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/assist/AsyncTaskAuditObjectLoader.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/assist/AsyncTaskAuditSnapshotAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/member/MemberAuditObjectLoader.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/member/MemberAuditSnapshotAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/AsyncTaskServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberServiceImpl.java`
  - 处理动作：为 AsyncTask 和 Member 补 loader、snapshot assembler，并在单对象写方法上声明 Audit 注解。
  - 验收点：两个目标对象审计接入代码可编译；执行 `mvn -pl sandwish-biz -am compile`。
  - 重要度：9/10

- [ ] `Audit 3.6 admin-api`：新增后台审计查询 API
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/audit/controller/AuditController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/audit/controller/request/AuditLogPageRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/audit/controller/request/AuditObjectHistoryRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/audit/controller/request/AuditMetaRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/audit/controller/response/AuditLogResponse.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/audit/controller/response/AuditMetaResponse.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/audit/controller/response/AuditFieldResponse.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/audit/assembler/AuditInterfaceAssembler.java`
  - 处理动作：新增后台对象审计历史、审计日志分页和审计元数据读取接口。
  - 验收点：后台审计查询 API 可编译；执行 `mvn -pl sandwish-admin-api -am compile`。
  - 重要度：9/10

- [ ] `Audit 3.7 architecture-tests`：补齐架构约束和审计回归测试源码
  - 范围文件：
    - `sandwish-biz/src/test/java/com/github/thundax/modules/audit/service/impl/AuditServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/audit/runtime/AuditDiffServiceTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/audit/runtime/AuditLogAspectTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/architecture/AuditArchitectureTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/architecture/DataObjectAnnotationArchitectureTest.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/architecture/AuditApiArchitectureTest.java`
  - 处理动作：新增审计流程和架构约束测试源码，更新 DO 架构测试以禁止业务表通用审计字段回流。
  - 验收点：测试源码可编译；执行 `mvn -pl sandwish-biz -am compile`、`mvn -pl sandwish-infra -am compile` 和 `mvn -pl sandwish-admin-api -am compile`，不单独跑 test。
  - 重要度：10/10

- [ ] `Audit 3.8 final-cleanup`：最终完整验证并清理 RUNBOOK 现场
  - 范围文件：
    - `TODO.md`
    - `docs/30-designs/RUNBOOK-AUDIT-SERVICE-REFORM.md`
  - 处理动作：先执行完整 `mvn clean install`，再按完成情况删除或收窄 TODO 项，删除本 RUNBOOK，并执行残留扫描。
  - 验收点：`mvn clean install` 成功；残留扫描只命中 Audit 自身、`sys_log` 或 Open Items 明确字段；`TODO.md` 不保留已完成项；`RUNBOOK-AUDIT-SERVICE-REFORM.md` 已删除。
  - 重要度：10/10

## 待讨论项
