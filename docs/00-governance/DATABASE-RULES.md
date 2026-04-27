# Database Rules

本文件只定义工程级数据库红线。

## Purpose

本文档固定数据库、持久化模型和查询映射的工程级规则，避免 Entity、DAO interface、DAO implementation、Mapper、Mapper XML 和 `DO/DataObject` 的职责混淆。

## Scope

当前范围：

- 数据库平台默认规则
- 表、字段、索引和关系约束
- DAO / Mapper / Mapper XML 归属
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

## Relationship Rules

- 默认不强制数据库外键
- 关联一致性由 Service 和数据库约束共同保证
- 跨表写入必须在 Service 中明确编排
- 跨模块引用只保留必要业务键或快照，不复制对端主表结构

## Persistence Layer Rules

- `sandwish-biz` 固定保留 `Entity`、Service 和 DAO interface。
- `sandwish-infra` 固定承载 `DO/DataObject`、DAO implementation、MyBatis Mapper、Mapper XML 和 `PersistenceAssembler`。
- `Entity` 表达 Service 可使用的业务数据对象，不直接作为 MyBatis XML result type。
- `DO/DataObject` 表达持久化实现对象，不暴露给 Controller 或 Service。
- DAO interface 不使用 MyBatis 扫描标记。
- MyBatis Mapper interface 使用 MyBatis 扫描标记。
- Mapper XML namespace 固定指向 `sandwish-infra` 中的 Mapper interface。
- Mapper XML result type 固定指向 `DO/DataObject`。
- `PersistenceAssembler` 只负责 `Entity <-> DO/DataObject` 转换，不调用 Service、DAO 或 Mapper。
- DAO implementation 负责调用 MyBatis Mapper 并通过 `PersistenceAssembler` 完成模型转换。
- Service 不感知 `DO/DataObject`。
- Controller 不直接依赖 DAO、Mapper、`DO/DataObject` 或 `PersistenceAssembler`。
- `DO/DataObject` 只承载数据库字段、必要关系字段和持久化查询所需的显式字段。
- `DO/DataObject` 不承载业务 `query` 对象，不定义 `Query` 内部类，不通过父类继承公共查询容器。
- Mapper 方法查询参数固定为一级显式参数或 `sandwish-infra` 内部 persistence 参数对象。
- Mapper XML 固定读取一级参数或 persistence 参数对象字段，不读取通用 `query.*`。
- `PersistenceAssembler` 不负责 `Entity.query` 与 `DO.query` 互转；业务查询条件必须在 Service / DAO implementation 中显式拆解。
- 多方言 Mapper XML 必须同步维护同一业务语义；确有 SQL 差异时，差异必须保留在对应方言 XML 中，不通过牺牲方言能力强行统一。

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
