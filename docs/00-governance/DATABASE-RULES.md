# Database Rules

本文件只定义工程级数据库红线。

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
- DAO interface 不使用 MyBatis 扫描标记。
- MyBatis Mapper interface 使用 MyBatis 扫描标记。
- Mapper XML namespace 固定指向 `sandwish-infra` 中的 Mapper interface。
- Mapper XML result type 固定指向 `DO/DataObject`。
- `PersistenceAssembler` 只负责 `Entity <-> DO/DataObject` 转换，不调用 Service、DAO 或 Mapper。
- Service 不感知 `DO/DataObject`。
- Controller 不直接依赖 DAO、Mapper、`DO/DataObject` 或 `PersistenceAssembler`。

## Index And Uniqueness

- 主键必须唯一
- 稳定业务键必须建立唯一约束或明确说明不能唯一的原因
- 联合唯一约束必须直接表达业务不变量
- 高频查询条件必须有显式索引
- 分页、过滤、排序优先在数据库完成

## Document Requirements

- 业务域数据库设计文档必须遵守本文件
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
