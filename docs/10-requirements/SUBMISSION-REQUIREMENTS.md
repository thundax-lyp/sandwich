# SUBMISSION REQUIREMENTS

## 1. Purpose

本文档定义 Sandwich 占位业务域 `Submission` 的业务需求边界。

`Submission` 用于承载第三方或系统入口提交的简单内容表单，固定包含标题、正文和图片列表，为后续 `sandwish-open-api`、第三方 client 身份、scope 权限、业务写入和审计链路提供最小真实业务对象。

## 2. Scope

当前覆盖范围：

- 提交内容主体 `Submission`
- 提交内容图片 `SubmissionImage`
- 提交内容图片上传入口
- 标题、正文、图片列表和来源 client
- 提交内容生命周期状态
- 提交内容平铺排序
- 提交内容创建和查询
- 提交内容状态调整

当前不覆盖范围：

- 富文本编辑器。
- 评论、点赞、收藏、转发等互动能力。
- 内容分类、标签和搜索索引。
- 图片处理、裁剪、水印和审核。
- 面向终端用户的公开内容展示。

## 3. Bounded Context

`Submission` 是 Sandwich 的占位业务域，不承载系统管理、会员认证、存储对象或审计日志自身职责。

`Submission` 固定表达“一次外部或内部提交的内容表单”。图片列表只保存对 `Storage` 对象的业务引用，不复制存储对象元数据。

`Submission` 是业务对象，成功写入后必须进入 Audit 数据审计链路。接口访问、失败请求、认证事件和第三方调用日志不进入 `Submission`，分别归属 `sys_log`、认证日志或开放接口运行日志。

## 4. Module Mapping

- `sandwish-biz/src/main/java/com/github/thundax/modules/submission`
  - 定义 `Submission`、`SubmissionImage`、状态枚举、DAO 契约和业务 Service。
- `sandwish-infra/src/main/java/com/github/thundax/modules/submission`
  - 实现 `Submission` 持久化对象、Mapper、DAO implementation 和持久化转换。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/submission`
  - 提供后台查询、详情、状态调整、排序和提交图片上传入口适配。
- `sandwish-open-api/src/main/java/com/github/thundax/modules/submission`
  - 提供第三方提交入口适配。
- `sandwish-front-api`
  - 当前不提供 `Submission` 入口。

## 5. Core Business Objects

### 5.1 Submission

`Submission` 是提交内容主体。

核心字段：

- `id`：提交内容 ID。
- `title`：标题。
- `content`：正文。
- `sourceClientId`：来源第三方 client ID。
- `status`：提交内容状态。
- `priority`：排序值。
- `submittedAt`：提交发生时间。
- `lastStatusChangedAt`：最近状态变化时间。
- `images`：提交内容图片列表。

固定状态：

- `SUBMITTED`：已提交。
- `APPROVED`：已通过。
- `REJECTED`：已拒绝。
- `CLOSED`：已关闭。

固定约束：

- `title` 必填。
- `content` 必填。
- `sourceClientId` 必填，后台手工创建时固定使用入口定义的系统 client 标识。
- `priority` 只用于后台列表平铺排序，不承载提交时间、状态或来源语义。
- `submittedAt` 表达业务提交时间，不作为通用审计字段。
- `lastStatusChangedAt` 只表达提交状态变化时间，不替代 Audit 变更记录。
- `Submission` 不保存图片 URL、文件名、大小和 MIME 类型。

### 5.2 SubmissionImage

`SubmissionImage` 是提交内容和存储对象的图片引用关系。

核心字段：

- `id`：图片引用 ID。
- `submissionId`：提交内容 ID。
- `storageObjectId`：存储对象 ID。
- `sortOrder`：图片展示顺序。

固定约束：

- 一个 `Submission` 可以关联多张图片。
- 图片顺序由 `sortOrder` 表达，从 `0` 开始。
- `storageObjectId` 固定引用 `Storage` 对象主键。
- 同一个 `Submission` 下 `sortOrder` 必须唯一。
- 同一个 `Submission` 下同一个 `storageObjectId` 不得重复引用。

## 6. Global Constraints

### 6.1 Command Boundary

提交内容写操作固定使用 Command 表达。

创建提交内容 Command 固定包含：

- `title`
- `content`
- `imageObjectIds`
- `sourceClientId`

状态调整 Command 固定包含：

- `id`
- `status`

排序 Command 固定包含：

- `orderedIds`
- `sortDirection`

Command 固定不包含：

- 当前用户 ID。
- 当前第三方 client 名称。
- 请求 IP。
- 审计快照。
- 存储对象元数据。
- `priority` 数值。

### 6.2 Image Boundary

`Submission` 提供业务专用图片上传入口，权限固定使用 `submission:submission:edit`。

上传实现固定复用 `StorageUploadRequestHelper` 和 Storage Service，上传后的存储对象 `ownerType` 固定为 `SUBMISSION`。Submission Controller 不直接访问 `StoredObjectStore`、DAO 或底层对象存储实现。

开放接口和后台入口必须先通过 Storage 能力得到图片对象 ID，再提交 `imageObjectIds`。Service 固定校验图片对象 ID 列表，并写入 `SubmissionImage`。

### 6.3 Audit Boundary

`Submission` 写操作固定生成数据审计。

审计对象坐标固定为：

- `objectType=Submission`
- `objectId=Submission.id`

创建提交内容使用 `AuditAction.CREATE`。状态调整使用 `AuditAction.UPDATE` 或后续更具体状态动作。

### 6.4 Sort Boundary

`Submission` 固定作为 `FlatSort` 可排序实体，使用 `priority` 控制后台列表展示顺序。

固定约束：

- 外部入口和后台入口不得提交 `priority` 数值。
- 后台重排只接收 `orderedIds` 和 `sortDirection`。
- `Submission` 排序域固定为全局平铺排序集合。
- 创建提交内容时，Service 负责生成新的 `priority`。
- 提交内容列表默认按 `priority` 升序查询。

## 7. Functional Requirements

### 7.1 Create Submission

系统必须支持创建提交内容。

创建成功后：

- 写入 `Submission`。
- 写入 `SubmissionImage` 列表。
- `status` 固定为 `SUBMITTED`。
- `priority` 固定由 Service 生成。
- `submittedAt` 固定为提交发生时间。
- 记录 `CREATE` 审计日志。

### 7.2 Query Submission

后台必须支持按提交内容状态、来源 client、提交时间范围分页查询提交内容。

提交内容分页默认按 `priority` 升序查询。

### 7.3 Get Submission Detail

后台必须支持按提交内容 ID 查询详情，详情包含图片对象 ID 列表。

### 7.4 Change Submission Status

后台必须支持调整提交内容状态。

状态调整成功后：

- 更新 `status`。
- 更新 `lastStatusChangedAt`。
- 记录数据审计。

### 7.5 Sort Submission

后台必须支持提交内容平铺重排。

排序成功后：

- 只更新 `priority`。
- 刷新查询后顺序与 `orderedIds` 一致。
- 不修改提交内容状态、标题、正文、图片引用和提交时间。

### 7.6 Upload Submission Image

后台必须支持在提交内容模块上传图片。

上传成功后：

- 返回 Storage 对象 ID、原始文件名、contentType 和内容访问 URL。
- Storage 对象 `ownerType` 固定为 `SUBMISSION`。
- Storage 对象 ID 可作为后续创建提交内容时的 `imageObjectIds`。

## 8. Key Flows

### 8.1 Open API Submit Flow

固定流程：

1. 第三方 client 完成认证。
2. 第三方上传图片并取得 storage object ID。
3. 第三方提交 `title`、`content` 和 `imageObjectIds`。
4. `sandwish-open-api` 组装创建提交内容 Command。
5. `SubmissionService` 创建提交内容和图片引用。
6. Audit 记录 `Submission` 创建事实。
7. 接口返回提交内容 ID。

### 8.2 Admin Query Flow

固定流程：

1. 后台用户提交查询条件。
2. `sandwish-admin-api` 组装查询对象。
3. `SubmissionService` 查询提交内容分页。
4. 入口 assembler 组装后台响应。

### 8.3 Admin Sort Flow

固定流程：

1. 后台用户提交 `orderedIds` 和 `sortDirection`。
2. `sandwish-admin-api` 组装排序 Command。
3. `SubmissionService` 校验排序域完整性。
4. `SubmissionService` 在事务内交换写回 `priority`。
5. 接口返回排序成功。

## 9. Non-Functional Requirements

- 标题、正文和图片列表必须进行长度和数量校验。
- 图片对象 ID 列表必须保持顺序。
- 创建提交内容和创建图片引用必须在同一事务内完成。
- 提交内容查询必须支持分页。
- 提交内容排序必须符合 `FlatSort` 规则。
- 提交内容写操作必须可审计。

## 10. Open Items

无
