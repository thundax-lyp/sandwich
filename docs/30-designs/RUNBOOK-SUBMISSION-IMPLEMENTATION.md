# RUNBOOK SUBMISSION IMPLEMENTATION

## 1. Purpose

本文档定义 `Submission` 占位业务域从文档设计落到代码实现的一次性执行顺序。

目标是在不引入额外业务复杂度的前提下，按 `biz -> infra -> admin-api` 顺序实现提交内容主体、图片引用、后台查询和状态调整能力，为后续 `sandwish-open-api` 接入第三方提交入口保留稳定业务基础。

## 2. Scope

本 RUNBOOK 覆盖：

- `sandwish-biz` 中 `Submission` 领域对象、值对象、枚举、DAO 契约、Service 契约和 Service implementation。
- `sandwish-infra` 中 `SubmissionDO`、`SubmissionImageDO`、Mapper、DAO implementation 和持久化装配器。
- `sandwish-admin-api` 中后台查询、详情、状态调整 Controller、request、response、assembler 和错误转换。
- `Submission` 审计快照、审计对象加载和 `@AuditLog` 写操作接入。
- 对应单元测试、契约测试和架构测试补充。

本 RUNBOOK 不覆盖：

- 新建 `sandwish-open-api` 模块。
- 第三方 API KEY / SECRET 认证流程。
- open-api 提交接口。
- admin-web 页面。
- 图片上传能力改造。
- 数据库迁移工具接入。

## 3. Inputs

执行前固定读取：

1. [`docs/AGENT.md`](../AGENT.md)
2. [`docs/00-governance/ARCHITECTURE.md`](../00-governance/ARCHITECTURE.md)
3. [`docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`](../00-governance/NAMING-AND-PLACEMENT-RULES.md)
4. [`docs/00-governance/DATABASE-RULES.md`](../00-governance/DATABASE-RULES.md)
5. [`docs/10-requirements/SUBMISSION-REQUIREMENTS.md`](../10-requirements/SUBMISSION-REQUIREMENTS.md)
6. [`docs/20-database/SUBMISSION-DATABASE-DESIGN.md`](../20-database/SUBMISSION-DATABASE-DESIGN.md)

涉及审计接入时固定再读取：

1. [`docs/10-requirements/AUDIT-REQUIREMENTS.md`](../10-requirements/AUDIT-REQUIREMENTS.md)
2. [`docs/20-database/AUDIT-DATABASE-DESIGN.md`](../20-database/AUDIT-DATABASE-DESIGN.md)

涉及后台 API 错误码时固定再读取：

1. [`docs/00-governance/API-ANNOTATION-MATRIX.md`](../00-governance/API-ANNOTATION-MATRIX.md)
2. [`docs/30-designs/ADMIN-API-ERROR-CODE-DESIGN.md`](./ADMIN-API-ERROR-CODE-DESIGN.md)

## 4. Execution Order

### 4.1 Biz Domain

范围文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/submission/entity/**`
- `sandwish-biz/src/main/java/com/github/thundax/modules/submission/dao/**`
- `sandwish-biz/src/main/java/com/github/thundax/modules/submission/service/**`
- `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/submission/**`
- `sandwish-biz/src/test/java/com/github/thundax/modules/submission/**`

执行动作：

1. 新增 `Submission`、`SubmissionImage` 领域对象。
2. 新增 `SubmissionStatus` 枚举，固定状态为 `SUBMITTED`、`APPROVED`、`REJECTED`、`CLOSED`。
3. 新增 `SubmissionId`、`SubmissionImageId` 和对应 codec。
4. 新增 `SubmissionDao`、`SubmissionImageDao` 契约。
5. 新增创建、状态调整、详情、分页查询所需 Command / Query / Result 模型。
6. 新增 `SubmissionService` 和 `SubmissionServiceImpl`。
7. 创建提交内容时校验 `title`、`content`、`sourceClientId` 和 `imageObjectIds`。
8. 创建提交内容时生成 `SUBMITTED` 状态、`submittedAt` 和顺序图片引用。
9. 状态调整时更新 `status` 和 `lastStatusChangedAt`。
10. 在写操作上声明 `@AuditLog(type = "Submission", ...)`。
11. 新增 `Submission` 审计 object loader 和 snapshot assembler。
12. 补充 Service 单元测试和审计快照测试。

验收点：

- Service 不依赖 `DO/DataObject`、Mapper 或 Controller request / response。
- Command 不包含当前用户、请求 IP、审计快照或存储对象元数据。
- 图片列表顺序从 `0` 开始。
- 创建提交内容和图片引用由 Service 事务统一编排。
- `Submission` 写操作具备审计注解和快照装配能力。

### 4.2 Infra Persistence

范围文件：

- `sandwish-infra/src/main/java/com/github/thundax/modules/submission/persistence/dataobject/**`
- `sandwish-infra/src/main/java/com/github/thundax/modules/submission/persistence/mapper/**`
- `sandwish-infra/src/main/java/com/github/thundax/modules/submission/persistence/assembler/**`
- `sandwish-infra/src/main/java/com/github/thundax/modules/submission/persistence/dao/**`
- `sandwish-infra/src/test/java/com/github/thundax/modules/submission/**`

执行动作：

1. 新增 `SubmissionDO` 映射 `submission_submission`。
2. 新增 `SubmissionImageDO` 映射 `submission_image`。
3. 新增 `SubmissionMapper extends BaseMapper<SubmissionDO>`。
4. 新增 `SubmissionImageMapper extends BaseMapper<SubmissionImageDO>`。
5. 新增 `SubmissionPersistenceAssembler` 和 `SubmissionImagePersistenceAssembler`。
6. 新增 `SubmissionDaoImpl`，负责插入、更新状态、详情、分页查询。
7. 新增 `SubmissionImageDaoImpl`，负责批量插入、按提交内容 ID 查询图片列表。
8. DAO implementation 使用 `SnowflakeIdGenerator` 生成独立表主键。
9. 分页查询固定按 `submitted_at desc, id desc` 排序。
10. 详情装载图片固定按 `submission_id, sort_order` 升序。
11. 补充 DAO / persistence assembler 测试。

验收点：

- Mapper interface 保持最小 `BaseMapper` 形态。
- `DO/DataObject` 不使用 `@TableField` 做普通列名映射。
- DAO implementation 不暴露 `DO/DataObject`。
- 查询、排序、分页逻辑在 infra DAO implementation 中完成。
- SQL 字段、文档字段和 DO 字段一致。

### 4.3 Admin API

范围文件：

- `sandwish-admin-api/src/main/java/com/github/thundax/modules/submission/controller/**`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/submission/assembler/**`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/submission/service/**`
- `sandwish-admin-api/src/main/resources/**`
- `sandwish-admin-api/src/test/java/com/github/thundax/modules/submission/**`
- `docs/30-designs/ADMIN-API-ERROR-CODE-DESIGN.md`

执行动作：

1. 新增后台提交内容分页查询 request / response。
2. 新增后台提交内容详情 request / response。
3. 新增后台提交内容状态调整 request / response。
4. 新增 `SubmissionInterfaceAssembler` 完成 API 模型与 biz 模型转换。
5. 新增 `SubmissionController`，入口路径固定在后台 API submission 模块下。
6. 按后台 API 既有方式补充参数校验、Swagger 注解和统一响应。
7. 若新增后台错误码，更新 `ADMIN-API-ERROR-CODE-DESIGN.md` 和入口异常转换。
8. 补充 Controller contract test。

验收点：

- Controller 不直接依赖 DAO、Mapper、`DO/DataObject` 或 persistence assembler。
- Request / Response 不暴露 `DO/DataObject`。
- ID 字段按入口既有 ID codec 规则转换。
- 后台查询接口只提供后台治理视图，不暴露 open-api 专用契约。
- 状态调整接口调用 `SubmissionService` 写入口并触发审计链路。

## 5. Commit Plan

固定拆分为以下提交：

1. `Feat(submission): 增加提交内容业务服务`
   - 收敛 `sandwish-biz` 领域、Service、审计运行时和测试。
2. `Feat(submission): 增加提交内容持久化实现`
   - 收敛 `sandwish-infra` DO、Mapper、DAO implementation、assembler 和测试。
3. `Feat(admin): 增加提交内容后台接口`
   - 收敛 `sandwish-admin-api` Controller、API 模型、assembler、错误码和契约测试。
4. `Docs(submission): 清理提交内容实现手册`
   - 完成后删除本 RUNBOOK，或在存在剩余执行范围时收窄本 RUNBOOK。

## 6. Verification

每个阶段完成后固定执行最小验证：

### 6.1 Biz Verification

```bash
mvn -pl sandwish-biz -am -Dtest='*Submission*Test,*Audit*Test' test
```

### 6.2 Infra Verification

```bash
mvn -pl sandwish-infra -am -Dtest='*Submission*Test' test
```

### 6.3 Admin API Verification

```bash
mvn -pl sandwish-admin-api -am -Dtest='*Submission*Test' test
```

### 6.4 Final Verification

```bash
mvn -q spotless:check
mvn -q checkstyle:check
mvn -q verify
```

如果完整 `mvn -q verify` 受环境或耗时限制无法执行，必须记录已执行的模块级验证和未执行原因。

## 7. Closure Checklist

收口前固定检查：

1. `SUBMISSION-REQUIREMENTS.md` 与实际行为一致。
2. `SUBMISSION-DATABASE-DESIGN.md` 与 DO、Mapper、DAO implementation 一致。
3. `db/schema/submission.sql` 与 DO 字段一致。
4. Audit object type 固定为 `Submission`。
5. 后台接口错误码文档与代码一致。
6. `TODO.md` 不保留已完成任务。
7. 本 RUNBOOK 已删除、或收窄为仍未完成范围。
8. 工作区不存在无关修改。
9. 每个提交都符合 `Type(domain): 中文说明`。

## 8. Open Items

无
