# Unified ID Design

本文档定义 Sandwich 统一 ID 的工程边界和默认写法。

## 1. Scope

当前范围：

- 统一 `EntityId` 等领域标识建模
- 保留 Java 类型安全
- 固定 API、Service、DAO、PersistenceAssembler 和 `DO/DataObject` 边界转换
- 区分数据库主键、业务编号和领域标识

不在范围内：

- 不引入新的发号中心
- 不覆盖业务单号设计
- 不替代数据库主键规则，主键规则见 [`DATABASE-RULES.md`](./DATABASE-RULES.md)

## 2. Core Distinction

以下三类标识不得混用：

- `Database Primary Key`：数据库主键，例如主表 `id`
- `Business No`：业务编号，例如订单号、支付流水号、外部业务号
- `Domain Identifier`：Java 里的强类型领域标识，例如 `EntityId`

固定判断：

- 数据库主键用于持久化唯一定位
- 业务编号用于业务语义识别、外部集成或人工查询
- 领域标识用于 Service、DAO interface 和 Entity 之间传递稳定对象身份

## 3. Module Boundary

- 统一 ID 公共能力固定放在 `sandwish-common-core`
- 当前基础类型位于 `com.github.thundax.common.id`
- `sandwish-biz` 的 Entity、Service 和 DAO interface 优先使用 `EntityId`
- `sandwish-admin-api` 和 `sandwish-front-api` 的 Request / Response 使用 `Long` 承载数据库主键
- `sandwish-infra` 的 `DO/DataObject` 使用 `Long` 承载数据库主键
- API 边界和持久化边界转换固定通过 `EntityIdCodec`

## 4. Core Model

当前固定模型：

- `Identifier<T>`：标识接口
- `BaseId<T>`：抽象基类
- `BaseStringId`：文本型 ID 基类
- `BaseLongId`：数值型 ID 基类
- `EntityId`：当前通用领域实体标识
- `EntityIdCodec`：`EntityId <-> Long` 和 `EntityId <-> String` 边界转换器
- `EntityIdTypeHandler`：MyBatis-Plus 中 `EntityId <-> BIGINT` 的类型转换器
- `SnowflakeIdGenerator`：雪花 ID 生成器

## 5. Hard Rules

- 领域标识采用“单值包装 + 强类型”模型
- 当前业务实体主键领域标识固定使用 `EntityId`；不为 `User`、`Role`、`Storage` 等当前对象预先新增 `UserId`、`RoleId`、`StorageId`。
- 只有当某个对象的 ID 存在独立值规则、独立来源或跨上下文防混用收益时，才新增更细粒度强类型 ID。
- `BaseId<T>` 必须不可变
- 具体 ID 构造器固定非公开，统一使用 `of(...)`
- 判等必须基于“具体类型 + 底层值”
- 不允许每个具体 ID 重写 `equals/hashCode`
- Jackson 对外序列化为单一基础值，不序列化成额外包裹结构
- 新增具体 ID 类型时，优先复用 `BaseStringId` 或 `BaseLongId`
- 边界转换逻辑固定放在 `Codec`，不散落到 Controller、Service、DAO implementation 中

## 6. Value Type Rules

- 文本型 ID 底层值不得为空白
- 一个具体 ID 类型只能固定一种底层值类型
- `EntityId` 固定使用 `Long`
- 新增具体 ID 类型前，必须明确它是数据库主键领域标识、业务编号，还是外部系统标识
- 业务编号不得为了复用 `EntityId` 而伪装成数据库主键
- `AuditObjectRef.objectId` 固定使用字符串表达被审计对象标识
- 写入 Audit 时，数据库主键类型的 `EntityId` 固定转换为 `String.valueOf(id.value())`

默认写法：

```java
EntityId id = EntityId.of(100001L);
EntityId nullableId = EntityIdCodec.toDomain(request.getId());
Long value = EntityIdCodec.toValue(entity.getId());
String auditObjectId = EntityIdCodec.toStringValue(entity.getId());
```

## 7. Layer Rules

### 7.1 Controller / InterfaceAssembler

- Request / Response 使用 `Long` 承载数据库主键
- 外部系统 ID、业务编号和 Audit 对象坐标可以继续使用 `String`
- HTTP `Long` 到 `EntityId` 的转换固定放在 Controller 或 `InterfaceAssembler`
- `InterfaceAssembler` 不新增通用 `toEntityId(...)` 公开方法；避免把 ID 转换包装成新的全局抽象层

### 7.2 Service / DAO Interface / Entity

- Entity 的主键字段优先使用 `EntityId`
- Service 公共方法按 ID 读取、删除或批量操作时优先使用 `EntityId`
- DAO interface 按 ID 读取、删除或批量操作时优先使用 `EntityId`
- Service 不直接操作 `DO/DataObject.id`

### 7.3 PersistenceAssembler / DO

- `DO/DataObject` 按数据库字段使用 `Long id`
- `EntityId <-> Long` 的持久化转换固定放在 `PersistenceAssembler`
- MyBatis-Plus 直接读写 `EntityId` 字段时，固定使用 `EntityIdTypeHandler`
- DAO implementation 不直接把 `Long id` 回填到 Entity；通过 `PersistenceAssembler` 完成模型转换

## 8. Persistence Defaults

- 独立数据库表的数据库主键固定为 `bigint`
- 独立数据库表的 `DO/DataObject.id` 固定为 `Long`
- 独立数据库表的 `DO/DataObject.id` 固定使用 `@TableId(type = IdType.INPUT)`
- DAO implementation 固定通过 `SnowflakeIdGenerator` 生成新主键
- Service 和 Entity 不负责为数据库主表生成 `id`
- DAO `insert` 返回持久化后的主键，Service 负责把返回主键回填到 Entity
- 共享主键表、外部业务键主键表和关系表按 `DATABASE-RULES.md` 显式说明主键来源

## 9. Migration Rules

- 全项目数据库主键迁移固定按 `RUNBOOK-GLOBAL-SNOWFLAKE-ID-MIGRATION.md` 执行
- 数据库、代码、文档必须先统一“这是主键、业务编号还是领域标识”
- 新增 ID 类型应先从一个明确业务对象开始，不做大范围抽象预留

## 10. Open Items

无
