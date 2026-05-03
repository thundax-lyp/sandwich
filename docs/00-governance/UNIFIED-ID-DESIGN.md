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
- 不要求全库字段类型重写
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
- `sandwish-admin-api` 和 `sandwish-front-api` 的 Request / Response 可以按 HTTP 协议需要保留 `String`
- `sandwish-infra` 的 `DO/DataObject` 可以按数据库字段需要保留 `String`
- API 边界和持久化边界转换固定通过 `EntityIdCodec`

## 4. Core Model

当前固定模型：

- `Identifier<T>`：标识接口
- `BaseId<T>`：抽象基类
- `BaseStringId`：文本型 ID 基类
- `BaseLongId`：数值型 ID 基类
- `EntityId`：当前通用领域实体标识
- `EntityIdCodec`：`EntityId <-> String` 边界转换器

## 5. Hard Rules

- 领域标识采用“单值包装 + 强类型”模型
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
- `EntityId` 固定使用 `String`
- 新增具体 ID 类型前，必须明确它是数据库主键领域标识、业务编号，还是外部系统标识
- 业务编号不得为了复用 `EntityId` 而伪装成数据库主键

默认写法：

```java
EntityId id = EntityId.of("user-1");
EntityId nullableId = EntityIdCodec.toDomain(request.getId());
String value = EntityIdCodec.toValue(entity.getId());
```

## 7. Layer Rules

### 7.1 Controller / InterfaceAssembler

- Request / Response 可以使用 `String` 承载外部 ID
- HTTP 字符串到 `EntityId` 的转换固定放在 Controller 或 `InterfaceAssembler`
- `InterfaceAssembler` 不新增通用 `toEntityId(...)` 公开方法；避免把 ID 转换包装成新的全局抽象层

### 7.2 Service / DAO Interface / Entity

- Entity 的主键字段优先使用 `EntityId`
- Service 公共方法按 ID 读取、删除或批量操作时优先使用 `EntityId`
- DAO interface 按 ID 读取、删除或批量操作时优先使用 `EntityId`
- Service 不直接操作 `DO/DataObject.id`

### 7.3 PersistenceAssembler / DO

- `DO/DataObject` 按数据库字段使用 `String id`
- `EntityId <-> String` 的持久化转换固定放在 `PersistenceAssembler`
- DAO implementation 不直接把 `String id` 回填到 Entity；通过 `PersistenceAssembler` 完成模型转换

## 8. Persistence Defaults

- 独立数据库表的 `DO/DataObject.id` 固定为 `String`
- 独立数据库表的 `DO/DataObject.id` 固定使用 `@TableId(type = IdType.ASSIGN_UUID)`
- Service 和 Entity 不负责为数据库主表生成 `id`
- DAO `insert` 返回持久化后的主键，Service 负责把返回主键回填到 Entity
- 共享主键表、外部业务键主键表和关系表按 `DATABASE-RULES.md` 显式说明主键来源

## 9. Migration Rules

- 不为了统一 ID 做全库字段类型重写
- 只在语义明显不匹配时调整数据库结构
- 数据库、代码、文档必须先统一“这是主键、业务编号还是领域标识”
- 新增 ID 类型应先从一个明确业务对象开始，不做大范围抽象预留

## 10. Open Items

- 是否在 `EntityId` 之外新增 `UserId`、`RoleId`、`StorageId` 等更细粒度强类型 ID。
- 是否为 MyBatis-Plus 增加强类型 ID TypeHandler。
