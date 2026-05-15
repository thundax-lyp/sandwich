# SUBMISSION DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich 提交内容占位业务域的数据库表、字段映射、关系约束和持久化规则。

本文档以 `SUBMISSION-REQUIREMENTS.md` 的提交内容模型为基础。建表 SQL 见 [`../../db/schema/submission.sql`](../../db/schema/submission.sql)，初始化脚本见 [`../../db/data/submission.sql`](../../db/data/submission.sql)。

## 2. Scope

当前覆盖范围：

- `submission_submission`
- `submission_image`
- `SubmissionDO`
- `SubmissionImageDO`
- `SubmissionMapper`
- `SubmissionImageMapper`
- `SubmissionDaoImpl`
- `SubmissionImageDaoImpl`
- `SubmissionPersistenceAssembler`
- `SubmissionImagePersistenceAssembler`

当前不覆盖范围：

- 图片上传和存储对象主数据，归属 `STORAGE-DATABASE-DESIGN.md`。
- 开放接口 client 和 token，归属 `AUTH-DATABASE-DESIGN.md`。
- 数据审计表，归属 `AUDIT-DATABASE-DESIGN.md`。
- 搜索索引和全文检索表。
- 内容分类、标签、评论和互动表。
- 生产数据变更脚本。

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- 存储引擎优先使用 `InnoDB`。
- 字符集优先使用 `utf8mb4`。
- 提交内容表使用 `submission_` 业务域前缀。
- 独立数据库表主键数据库类型固定为 `bigint`，Java 类型固定为 `Long`。
- 独立数据库表主键由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- 枚举字段使用 `varchar` 存储。
- `priority` 是 `FlatSort` 排序字段，固定由 Service 管理。
- `submitted_at` 是业务提交时间字段。
- `last_status_changed_at` 是业务状态变化时间字段。
- 业务对象变更审计固定归属 Audit 模块，不在 `submission_` 表中保存通用审计字段。
- `DO/DataObject` 不暴露给 Controller 或 Service。

## 4. Naming Rules

- 提交内容主表固定为 `submission_submission`。
- 提交内容图片引用表固定为 `submission_image`。
- 主键字段固定为 `id`。
- 提交内容外键字段固定为 `submission_id`。
- 存储对象引用字段固定为 `storage_object_id`。
- 图片排序字段固定为 `sort_order`。
- 来源 client 字段固定为 `source_client_id`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `submission_submission` | `SubmissionDO` | `SubmissionMapper` | `Submission` |
| `submission_image` | `SubmissionImageDO` | `SubmissionImageMapper` | `SubmissionImage` |

## 6. Table Design

### 6.1 submission_submission

`submission_submission` 保存提交内容主体。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 提交内容主键 |
| `title` | `title` | `title` | 是 | 标题 |
| `content` | `content` | `content` | 是 | 正文 |
| `source_client_id` | `sourceClientId` | `sourceClientId` | 是 | 来源第三方 client ID |
| `status` | `status` | `status` | 是 | 提交内容状态 |
| `priority` | `priority` | `priority` | 是 | 排序值 |
| `submitted_at` | `submittedAt` | `submittedAt` | 是 | 提交发生时间 |
| `last_status_changed_at` | `lastStatusChangedAt` | `lastStatusChangedAt` | 否 | 最近状态变化时间 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `title` 固定使用 `varchar(200)`。
- `content` 固定使用 `text`。
- `source_client_id` 固定保存来源 client ID。
- `status` 通过 `SubmissionStatus.value()` 写入。
- `status` 固定使用状态值：`SUBMITTED`、`APPROVED`、`REJECTED`、`CLOSED`。
- `priority` 默认值固定为 `0`，由 Service 维护。
- `submitted_at` 固定写入提交发生时间。
- `last_status_changed_at` 仅在状态调整时写入。

索引：

- 主键：`pk_submission_submission(id)`
- 唯一索引：`uk_submission_submission_priority(priority)`
- 普通索引：`idx_submission_submission_status(status, priority)`
- 普通索引：`idx_submission_submission_client(source_client_id, priority)`
- 普通索引：`idx_submission_submission_submitted(submitted_at, id)`

### 6.2 submission_image

`submission_image` 保存提交内容图片引用关系。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 图片引用主键 |
| `submission_id` | `submissionId` | `submissionId` | 是 | 提交内容主键 |
| `storage_object_id` | `storageObjectId` | `storageObjectId` | 是 | 存储对象主键 |
| `sort_order` | `sortOrder` | `sortOrder` | 是 | 图片展示顺序 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `submission_id` 引用 `submission_submission.id`。
- `storage_object_id` 引用 `assist_storage.id`。
- `sort_order` 从 `0` 开始。
- 本表只保存图片引用关系，不复制 Storage 对象字段。

索引：

- 主键：`pk_submission_image(id)`
- 唯一索引：`uk_submission_image_order(submission_id, sort_order)`
- 唯一索引：`uk_submission_image_storage(submission_id, storage_object_id)`
- 普通索引：`idx_submission_image_storage(storage_object_id)`

## 7. Relationship Rules

- 当前提交内容表不强制数据库外键。
- `submission_image.submission_id` 通过 Service 和数据库唯一约束关联 `submission_submission.id`。
- `submission_image.storage_object_id` 通过 Service 校验关联 `assist_storage.id`。
- 当前 `Submission` 生命周期通过 `status` 表达，不设计逻辑删除字段。
- 图片引用关系随创建写入，状态调整不修改图片引用。
- 一个 `Submission` 可以关联多条 `SubmissionImage`。

## 8. Persistence Rules

- `SubmissionDO` 固定映射 `submission_submission`。
- `SubmissionImageDO` 固定映射 `submission_image`。
- `SubmissionMapper` 固定保持 `public interface SubmissionMapper extends BaseMapper<SubmissionDO> {}`。
- `SubmissionImageMapper` 固定保持 `public interface SubmissionImageMapper extends BaseMapper<SubmissionImageDO> {}`。
- `SubmissionDaoImpl` 固定负责 `Submission <-> SubmissionDO` 转换、查询条件拆解和分页。
- `SubmissionImageDaoImpl` 固定负责 `SubmissionImage <-> SubmissionImageDO` 转换和按提交内容 ID 查询图片列表。
- `PersistenceAssembler` 只负责 Entity 与 `DO/DataObject` 字段转换。
- Service 不感知 `SubmissionDO` 或 `SubmissionImageDO`。
- Controller 不直接依赖 Mapper、`DO/DataObject` 或 `PersistenceAssembler`。

## 9. Query Model Rules

- 提交内容列表固定支持按 `status`、`sourceClientId`、`submittedAt` 时间范围过滤。
- 提交内容列表和分页默认按 `priority asc, id asc` 排序。
- 提交内容详情固定按 `submission_id, sort_order` 升序装载图片列表。
- 提交内容重排固定只更新 `priority`。
- `pageNo` / `pageSize` 由 Service 校验，DAO implementation 只按已校验参数执行分页。

## 10. Open Items

无
