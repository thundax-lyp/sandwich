# Database Rules

本文件只定义工程级数据库红线。

## Purpose

本文档固定数据库、持久化模型和查询映射的工程级规则，避免 Entity、DAO interface、DAO implementation、Mapper 和 `DO/DataObject` 的职责混淆。

## Scope

当前范围：

- 数据库平台默认规则
- 表、字段、索引和关系约束
- DAO / Mapper 归属
- Entity 与 `DO/DataObject` 的边界
- `PersistenceAssembler` 职责
- 数据库设计文档结构

不在范围内：

- 不定义业务接口模型，API 模型归属见 [`NAMING-AND-PLACEMENT-RULES.md`](./NAMING-AND-PLACEMENT-RULES.md)
- 不定义部署流量入口，部署规则见 [`DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`](./DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md)
- 不替代具体业务域数据库设计文档

## Platform

- 数据库以当前项目实际配置为准
- 存储引擎优先使用 `InnoDB`
- 字符集优先使用 `utf8mb4`
- 主键字段默认命名为 `id`
- 布尔字段优先使用 `tinyint(1)` 或项目既有等价写法
- 金额字段优先使用 `decimal`
- 枚举字段优先使用 `varchar`

## Naming

- 表名、字段名、索引名必须与现有数据库风格保持一致
- 关系表后缀必须显式表达语义
- 逻辑删除字段、租户字段、审计字段沿用既有项目命名
- 新增命名规则前必须先盘点现有 schema

## Table Types

- 主数据表：后台维护的主数据和配置表
- 运行时业务表：保留业务主状态和必要领域时间字段
- 关系表：只保留关系本身的最小字段，关系唯一性用联合唯一约束表达
- 台账表：只追加，不回写历史
- 审计日志表：只追加，不保存敏感明文

## Field Rules

- 是否增加时间字段，取决于对象是否有独立生命周期
- Java 8 项目默认沿用现有日期时间类型
- 不为追随外部规则强制迁移为 `Instant`
- 密码、令牌、密钥、验证码等敏感信息不得明文落库
- `DO/DataObject` 审计字段按数据库列语义命名为 `createBy` / `updateBy`；业务 `Entity` 可以继续使用 `createUserId` / `updateUserId` 表达业务含义，由 `PersistenceAssembler` 显式转换。
- `createDate` / `createBy` / `updateDate` / `updateBy` 是持久化审计字段，由 infra 在 insert / update 时统一填充；`createBy` / `updateBy` 只透传当前请求的 `currentUserId`，Service 不预填审计字段。
- `del_flag` 是数据库逻辑删除审计字段，默认值为 `0`；`Entity` 与 `DO/DataObject` 不声明 `delFlag`，DAO insert 时负责写入 `del_flag = '0'`，DAO list/page 查询固定追加 `del_flag = '0'` 条件。

## Primary Key Rules

- 独立数据库表的 `DO/DataObject` 主键字段固定命名为 `id`，Java 类型固定为 `String`。
- 独立数据库表的 `DO/DataObject.id` 固定使用 `@TableId(type = IdType.ASSIGN_UUID)`，主键由 MyBatis-Plus 持久化层生成。
- DAO `insert` 方法返回持久化后的主键；Service 在 `insert` 后负责把返回主键回填到业务 `Entity`，再继续编排关系表、签名、缓存或响应数据。
- Service 和 Entity 不负责为数据库主表生成 `id`。
- 共享主键表、外部业务键主键表、关系表和非数据库 DO 不适用自动主键生成规则；这类表必须显式说明主键来源，并按来源使用 `IdType.INPUT` 或不声明 `@TableId`。
- 共享主键表的 DO 字段名必须表达主键来源，例如用户扩展表使用 `userId`，存储业务绑定表使用 `storageId`；不得为了复用数据库列名在 DO 中继续声明泛化 `id` 字段。
- 共享主键表不得用 `ASSIGN_UUID` 生成新主键，避免破坏与主对象的主键一致性。

## Relationship Rules

- 默认不强制数据库外键
- 关联一致性由 Service 和数据库约束共同保证
- 跨表写入必须在 Service 中明确编排
- 跨模块引用只保留必要业务键或快照，不复制对端主表结构

## Persistence Layer Rules

- `sandwish-biz` 固定保留 `Entity`、Service 和 DAO interface。
- `sandwish-infra` 固定承载 `DO/DataObject`、DAO implementation、MyBatis-Plus Mapper 和 `PersistenceAssembler`。
- `Entity` 表达 Service 可使用的业务数据对象，不直接作为 MyBatis-Plus Mapper 持久化对象。
- `DO/DataObject` 表达持久化实现对象，不暴露给 Controller 或 Service。
- DAO interface 不使用 MyBatis 扫描标记。
- MyBatis Mapper interface 使用标准 `@Mapper` 扫描标记。
- Mapper interface 保持最小定义，固定形态为 `public interface XxxMapper extends BaseMapper<XxxDO> {}`。
- 业务查询、排序、关系装载和分页逻辑固定在 DAO implementation 中通过 MyBatis-Plus API 或当前 entity 专用持久化 helper 完成。
- 禁止新增 Mapper XML、注解 SQL 或 SQL Provider 作为业务持久化实现入口。
- `PersistenceAssembler` 只负责 `Entity <-> DO/DataObject` 转换，不调用 Service、DAO 或 Mapper。
- DAO implementation 负责调用 MyBatis Mapper 并通过 `PersistenceAssembler` 完成模型转换。
- 数据库表审计字段填充固定使用 infra 统一持久化拦截能力，不在各个 DAO implementation 的业务写法中散落设置。
- Service 不感知 `DO/DataObject`。
- Controller 不直接依赖 DAO、Mapper、`DO/DataObject` 或 `PersistenceAssembler`。
- `DO/DataObject` 只承载数据库字段、必要关系字段和持久化查询所需的显式字段。
- `DO/DataObject` 字段名默认依赖 MyBatis-Plus camelCase 到 snake_case 自动映射，禁止使用 `@TableField` 做列名映射，禁止 `@TableField(exist = false)` 非表字段；仅允许加密等持久化类型处理场景使用 `@TableField(typeHandler = ...)`。
- `DO/DataObject` 不承载业务 `query` 对象，不定义 `Query` 内部类，不通过父类继承公共查询容器。
- Mapper 方法查询参数固定为一级显式参数或 `sandwish-infra` 内部 persistence 参数对象。
- `PersistenceAssembler` 不负责 `Entity.query` 与 `DO.query` 互转；业务查询条件必须在 Service / DAO implementation 中显式拆解。
- 树结构中 `parentId` 是业务关系字段，可以存在于 `Entity`、`DO/DataObject` 和 API 模型中。
- 树结构中 `lft` / `rgt` 是 nested-set 持久化索引，只能存在于 `DO/DataObject`、Mapper 和 infra DAO implementation 中。
- 需要按树子孙范围过滤时，Service / Controller 固定传递业务字段，区间读取和 SQL join 固定在 infra 持久化实现中完成。
- MyBatis-Plus 链路固定以达梦行为为标准，分页插件固定使用 `DbType.DM`。
- PageHelper 已下线，禁止新增依赖、调用或兼容支撑。
- 旧 `CrudDao` / `CrudServiceImpl` 基类已下线，禁止业务 DAO / Service 继承回流。
- DAO 分页方法直接返回 `com.baomidou.mybatisplus.extension.plugins.pagination.Page<Entity>`，分页参数使用 `int pageNo, int pageSize`。
- `pageNo` / `pageSize` 有效性由 Service 校验，DAO implementation 只负责按已校验参数执行持久化分页。

## Index And Uniqueness

- 主键必须唯一
- 稳定业务键必须建立唯一约束或明确说明不能唯一的原因
- 联合唯一约束必须直接表达业务不变量
- 高频查询条件必须有显式索引
- 分页、过滤、排序优先在数据库完成

## Document Requirements

- 业务域数据库设计文档必须遵守本文件
- 数据库设计文档固定放在 `docs/20-database/`
- 每份数据库设计文档至少包含：
  - `Purpose`
  - `Scope`
  - `Database Rules`
  - `Naming Rules`
  - `Table Mapping`
  - `Table Design`
  - `Relationship Rules`
  - `Persistence Rules`
  - `Query Model Rules`
  - `Open Items`
- `Open Items` 为空时必须写 `无`
- 文档中的表名、字段名、索引名必须与后续 SQL、Entity、Mapper 一致

## Open Items

- 需要盘点当前项目真实 schema 后固定表名前缀、审计字段和逻辑删除字段。
