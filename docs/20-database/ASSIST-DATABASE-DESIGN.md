# ASSIST DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich `Assist` 辅助能力域的数据库边界和运行态持久化规则。

本文档以 `ASSIST-REQUIREMENTS.md` 的异步任务模型为基础。当前 `Assist` 不建立数据库表，不提供 `db/schema/assist.sql` 或 `db/data/assist.sql`。

## 2. Scope

当前覆盖范围：

- `AsyncTask` 的非数据库持久化边界。
- `AsyncTaskDaoImpl` 的 JetCache 运行态保存规则。
- 异步任务 key 索引缓存。
- 异步任务排序缓存边界。

当前不覆盖范围：

- `assist_storage`
- `assist_storage_business`
- `assist_storage_multipart_upload`
- `assist_storage_multipart_upload_part`
- 异步任务历史表。
- 异步任务 Mapper。
- 异步任务 `DO/DataObject`。
- 生产数据变更脚本。

说明：

- `assist_storage*` 表归属 `Storage` 业务域，数据库设计见 `STORAGE-DATABASE-DESIGN.md`。

## 3. Database Rules

- `Assist` 当前不建立数据库表。
- `Assist` 当前不定义 MyBatis Mapper。
- `Assist` 当前不定义 `DO/DataObject`。
- `AsyncTask` 运行态由 JetCache 保存。
- `AsyncTask` ID 由 `AsyncTaskDaoImpl` 通过 `SnowflakeIdGenerator` 生成。
- `AsyncTask` 缓存对象使用 DAO implementation 内部 `AsyncTaskCacheDTO`。
- `AsyncTaskCacheDTO` 不属于 `DO/DataObject`，不纳入数据库表注解、主键和 Mapper 规则。

## 4. Naming Rules

- 异步任务缓存 section 固定为 `Constants.CACHE_PREFIX + "assist.asyncTask."`。
- 异步任务 key 索引缓存 section 固定为 `Constants.CACHE_PREFIX + "assist.asyncTask.keys."`。
- 异步任务 key 索引固定使用 `keys`。
- 后续如新增 `Assist` 数据库表，表名前缀必须使用 `assist_`，并同步新增数据库设计文档、SQL 脚本和架构测试白名单。

## 5. Table Mapping

当前无数据库表映射。

| Runtime Object | Storage | Mapper | Entity |
| --- | --- | --- | --- |
| `AsyncTaskCacheDTO` | JetCache | 无 | `AsyncTask` |

## 6. Table Design

当前无数据库表。

### 6.1 AsyncTask 缓存对象

`AsyncTaskCacheDTO` 保存异步任务运行态字段。

| Cache Field | Entity Field | Required | Description |
| --- | --- | --- | --- |
| `id` | `id` | 是 | 异步任务 ID |
| `title` | `title` | 否 | 任务标题 |
| `status` | `status` | 否 | 任务状态 |
| `message` | `message` | 否 | 状态消息 |
| `data` | `data` | 否 | 任务结果数据 |
| `isPrivate` | `isPrivate` | 否 | 是否私有任务 |
| `expiredSeconds` | `expiredSeconds` | 否 | 任务缓存过期秒数 |
| `priority` | `priority` | 否 | 缓存集合排序值 |
| `remarks` | `remarks` | 否 | 备注 |

字段规则：

- `status` 固定保存 `AsyncTaskStatus.value()`。
- `expiredSeconds` 缺失时由 `AsyncTask.DEFAULT_EXPIRED_SECONDS` 兜底。
- `priority` 缺失时按 `0` 处理。

## 7. Relationship Rules

- 当前 `Assist` 不建立数据库外键或关系表。
- 异步任务与当前用户的归属关系只在业务对象中表达，不通过数据库关系维护。

## 8. Persistence Rules

- `AsyncTaskDaoImpl` 固定负责 `AsyncTask <-> AsyncTaskCacheDTO` 转换。
- `AsyncTaskDaoImpl` 固定负责写入任务缓存和维护 key 索引缓存。
- `AsyncTaskDaoImpl` 删除任务时必须同步移除 key 索引。
- `AsyncTaskDaoImpl` 列表读取时必须清理已经过期或不存在的 key。
- Service 不感知 `AsyncTaskCacheDTO`。
- Controller 不直接依赖 JetCache 或 `AsyncTaskCacheDTO`。

## 9. Query Model Rules

- 异步任务按 key 索引读取当前缓存集合。
- 列表排序按 `priority` 和 `id` 排序。
- `sortDirection = DESC` 时反转排序结果。
- 任务过期会导致排序域变化，排序接口必须以当前缓存集合为准。

## 10. Open Items

无
