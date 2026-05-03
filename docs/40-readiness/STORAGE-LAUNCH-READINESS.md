# STORAGE LAUNCH READINESS

## 1. Purpose

本文档只保留 Sandwich `Storage` 上线前的验收门槛。

本文档不记录已经完成的历史整改过程，不重复需求和数据库设计内容。

## 2. Scope

覆盖范围：

- `sandwish-biz/src/main/java/com/github/thundax/modules/storage`
- `sandwish-infra/src/main/java/com/github/thundax/modules/storage`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/storage`
- `sandwish-biz/src/test/java/com/github/thundax/modules/storage`
- `sandwish-infra/src/test/java/com/github/thundax/modules/storage`

## 3. Release Gates

### 3.1 Contract Gate

上线前必须确认：

- 后台和前台存储访问路径与当前 Servlet / Converter 实现一致
- 文档中的接口路径、字段名、状态值与当前代码一致
- `StorageResponse`、`StorageQuery`、`StorageStatus`、`StorageVisibility`、`StorageOwnerType` 的公开语义一致

### 3.2 Data Gate

上线前必须确认：

- 存储相关数据库表与 `StorageDO`、`StorageBusinessDO` 字段一致
- `StoragePersistenceAssembler` 中 `Entity <-> DO` 字段转换完整
- 枚举字段写入值与数据库 `varchar` 存储值一致
- 共享主键或业务绑定关系符合 `DATABASE-RULES.md`

### 3.3 Lifecycle Gate

上线前必须确认：

- 普通上传能生成 `Storage`
- 上传后能建立业务绑定关系
- 私有资源只能由 owner 访问
- 删除后对象进入已删除语义，且不能再作为可用对象继续使用
- 缓存失效能覆盖新增、更新、删除和业务绑定变化

### 3.4 Access Gate

上线前必须确认：

- 后台存储文件访问可用
- 前台存储文件访问可用
- 文件不存在时返回明确的 404 或等价错误
- MIME type 与文件内容输出行为一致
- 上传允许的内容类型与 `StorageUtils` 当前白名单一致

### 3.5 Query Gate

上线前必须确认：

- 详情查询可用
- 分页查询可用
- MIME type 列表查询可用
- `status`、`visibility`、`ownerType`、`ownerId` 等筛选字段与 Service / DAO 查询实现一致

### 3.6 Test Gate

上线前至少应覆盖：

- 普通上传主流程
- 查询主流程
- 分页筛选主流程
- 删除流程
- `StoragePersistenceAssembler` 字段转换
- 私有资源 owner 访问边界
- 缓存失效边界

## 4. Current Rule

- 若存在阻断上线的问题，应直接补到本文件，不要散落到其他文档
- 若某项问题已经修复，应从本文件移除，不保留历史描述
- 本文件始终只保留“当前仍需要确认的事项”

## 5. Open Items

无
