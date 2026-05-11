# ASSIST REQUIREMENTS

## 1. Purpose

本文档定义 Sandwich `Assist` 辅助能力域的业务需求边界。

`Assist` 对应代码中的 `com.github.thundax.modules.assist`，负责后台可轮询的异步任务运行态。当前 `Assist` 只沉淀异步任务，不承载存储对象；存储对象虽然使用 `assist_` 数据库表前缀，但业务域归属 `Storage`。

## 2. Scope

当前覆盖范围：

- 异步任务创建。
- 异步任务状态变更。
- 异步任务删除。
- 异步任务读取。
- 异步任务排序。
- 异步任务运行态缓存。

当前不覆盖范围：

- 持久化异步任务历史。
- 异步任务数据库表。
- 队列调度引擎。
- 任务执行器注册中心。
- 任务执行日志明细。
- 存储对象、对象引用和分片上传。
- 签名插件协议对象。

## 3. Bounded Context

`Assist` 是后台辅助运行态能力，用于表达短生命周期异步任务的查询和展示状态。

异步任务的权威状态保存在 Redis / JetCache 运行态缓存中，不进入业务数据库。缓存过期后任务状态不可恢复，调用方必须按短生命周期轮询模型处理。

## 4. Module Mapping

- `sandwish-biz/src/main/java/com/github/thundax/modules/assist`
  - 定义 `AsyncTask`、`AsyncTaskStatus`、`AsyncTaskId`、DAO interface、Service 和 Command。
- `sandwish-infra/src/main/java/com/github/thundax/modules/assist`
  - 实现 `AsyncTaskDaoImpl`，通过 JetCache 保存异步任务运行态和任务 key 索引。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist`
  - 提供后台异步任务读取和排序 Controller、Request、Response 和 InterfaceAssembler。

## 5. Core Business Objects

### 5.1 AsyncTask

`AsyncTask` 是异步任务运行态对象。

核心字段：

- `id`：异步任务 ID，使用 `AsyncTaskId`。
- `title`：任务标题。
- `status`：任务状态，使用 `AsyncTaskStatus`。
- `message`：任务状态消息。
- `data`：任务结果数据。
- `isPrivate`：是否私有任务。
- `expiredSeconds`：缓存过期秒数，默认值为 `1800`。
- `priority`：排序值。
- `remarks`：备注。

固定约束：

- `AsyncTask` 不落数据库表。
- `AsyncTask` 由 `AsyncTaskDaoImpl` 保存到 JetCache。
- `AsyncTask.expiredSeconds` 控制单个任务缓存生命周期。
- `AsyncTask.priority` 只用于当前缓存索引内任务排序，不具备数据库唯一约束。

### 5.2 AsyncTaskStatus

`AsyncTaskStatus` 固定表达异步任务状态。

固定值：

- `IDLE`
- `ACTIVE`
- `SUSPENDED`
- `SUCCESS`
- `ERROR`

## 6. Global Constraints

- Controller 只做入口适配、权限、参数接收和响应组装。
- Service 负责任务创建、变更、删除和排序流程。
- DAO interface 只暴露任务运行态访问契约。
- `AsyncTaskDaoImpl` 使用 JetCache，不使用 MyBatis Mapper。
- `Assist` 不定义 `DO/DataObject`、`Mapper` 或数据库 SQL。
- 异步任务缓存 key 前缀固定由 `Constants.CACHE_PREFIX + "assist.asyncTask."` 派生。
- 异步任务 key 索引只用于读取当前未过期任务集合。
- 任务缓存对象使用 DAO implementation 内部 `CacheDTO`，不暴露给 Service 或 Controller。

## 7. Functional Requirements

### 7.1 异步任务读取

- 支持按 `AsyncTaskId` 读取任务。
- 任务不存在时返回空响应。
- 私有任务读取必须做当前用户归属校验。

### 7.2 异步任务创建

- 创建任务时未提供 ID，DAO implementation 必须生成 `AsyncTaskId`。
- 创建任务时未提供排序值，Service 必须使用当前最大 `priority + 10`。
- 创建任务后返回任务 ID。

### 7.3 异步任务变更

- 支持变更任务状态、消息、结果数据、可见性、过期时间、排序值和备注。
- 任务状态合法值以 `AsyncTaskStatus` 为准。

### 7.4 异步任务删除

- 支持按 `AsyncTaskId` 删除任务缓存。
- 删除任务时必须同步清理 key 索引。

### 7.5 异步任务排序

- 后台排序接口固定接收 `orderedIds` 和 `sortDirection`。
- 排序只覆盖当前缓存索引内仍可读取的任务集合。
- 任务过期导致排序域不完整时，Service 必须返回排序域不一致错误。

## 8. Key Flows

### 8.1 创建任务

1. 调用方构造 `AsyncTaskCommand`。
2. Service 补齐默认排序值。
3. DAO implementation 生成缺失的任务 ID。
4. DAO implementation 写入任务缓存并记录 key 索引。
5. Service 返回 `AsyncTaskId`。

### 8.2 读取任务

1. API 接收任务 ID。
2. Controller 调用 Service 读取任务。
3. 私有任务执行当前用户归属校验。
4. Controller 返回 `AsyncTaskResponse`。

### 8.3 排序任务

1. API 接收完整 `orderedIds` 和 `sortDirection`。
2. Service 读取当前缓存任务集合。
3. Service 校验空值、重复 ID 和排序域完整性。
4. Service 通过 DAO 更新任务 `priority`。
5. API 返回成功。

## 9. Non-Functional Requirements

- 异步任务读取应优先命中 Redis / JetCache，不访问数据库。
- 异步任务缓存过期后允许自然消失。
- 异步任务排序只保证当前缓存集合内的一致性。

## 10. Open Items

无
