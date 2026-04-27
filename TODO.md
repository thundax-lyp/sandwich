# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前主线顺序

1. 文档治理骨架
   - 补齐架构、文档、命名、数据库和部署治理文档
2. 三层架构规则收敛
   - 固定 `Controller -> Service -> DAO/Mapper` 边界
3. 业务需求与数据库文档补齐
   - 按现有模块和业务域逐步建立 `10-requirements/` 与 `20-database/`
4. 质量与交付闭环
   - 按任务补测试、文档同步和小步提交

## P0 - Controller / Service 模型职责隔离

## P0 - 持久化表达改造

- [ ] `docs/30-designs`：持久化迁移手册收尾
  - 范围对象：`INFRA-SPLIT-RUNBOOK.md`、`TODO.md`、治理文档
  - 处理动作：持久化迁移完成后删除临时手册；删除或收窄已完成 TODO；将稳定规则沉淀到治理文档
  - 验收点：持久化临时手册不再保留，完成历史只存在于 commit / PR 中

- [ ] `docs-architecture-persistence-boundary`：同步架构文档中的持久化边界规则
  - 范围对象：`docs/00-governance/ARCHITECTURE.md`
  - 当前依赖：架构文档已固定 `DAO implementation`、Mapper、Mapper XML、`DO/DataObject`、`PersistenceAssembler` 归属 `sandwish-infra`，但尚未明确 `DO` 不承载业务 query、`PersistenceAssembler` 不回填查询对象、Mapper 方法优先显式表达业务语义
  - 处理动作：在持久化表达改造完成后，将稳定规则补入 `DAO / Mapper` 与 `Entity / VO / DTO` 小节；明确 Redis DAO 属于 infra 持久化实现但不要求新增 Mapper / XML
  - 验收点：架构文档能指导后续新链路按三层边界落位，不再需要临时迁移手册解释持久化边界

- [ ] `docs-database-query-model-rules`：同步数据库规则中的查询模型和 DO 规则
  - 范围对象：`docs/00-governance/DATABASE-RULES.md`
  - 当前依赖：数据库规则已定义 `Entity`、`DO/DataObject`、Mapper XML 和 `PersistenceAssembler` 归属；`Document Requirements` 已要求业务域数据库文档包含 `Query Model Rules`，但尚未固定 DO 查询字段、Mapper 参数和方言 XML 差异处理规则
  - 处理动作：在各业务域 DO query 消除后，补充 `DO/DataObject` 使用显式字段承载持久化查询条件、Mapper XML 不依赖通用 `query.*` 容器、方言 SQL 差异必须保留并说明的规则
  - 验收点：数据库治理文档能约束新增或修改 Mapper XML 时的查询条件表达和多方言同步要求

- [ ] `docs-naming-persistence-placement`：同步命名目录规则中的持久化对象细则
  - 范围对象：`docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`
  - 当前依赖：命名目录规则已固定 `DO/DataObject`、Mapper、DAO implementation、`PersistenceAssembler` 目录和命名；尚未明确 `DO` 与业务 `Entity` 查询模型的命名边界、显式查询字段命名、公共 DAO 契约收敛后的放置口径
  - 处理动作：在公共契约和装配器收敛后，补充 DO 显式字段、查询字段命名、DAO / Mapper 显式方法命名和 `PersistenceAssembler` 单向 query 复制规则；同步删除过时的开放项或收窄为具体命名决策
  - 验收点：命名目录规则能作为新增持久化类、Mapper 方法和装配器方法的直接命名依据

## P0 - Common / MyBatis-Plus / XML 演进

- [ ] `tree-persistence-boundary-stage-1`：将树嵌套集索引迁入 infra
  - 参考手册：`docs/30-designs/TREE-PERSISTENCE-BOUNDARY-RUNBOOK.md`
  - 范围对象：`Office` / `Menu` 树链路、`TreeEntity`、`AdminTreeEntity`、`TreeServiceImpl`、`TreeDao`、`OfficeDO`、`MenuDO`、`OfficeMapper`、`MenuMapper`
  - 处理动作：先迁移 `Office`，再迁移 `Menu`；`parentId` 保留在业务实体中；`lft` / `rgt` 只保留在 DO、Mapper 和 infra 树持久化实现中；Service 不再计算 nested-set 区间
  - 验收点：Admin / Front 包编译通过；业务实体不再暴露 `lft` / `rgt`；现有 XML 尚未删除；MyBatis-Plus 尚未引入

- [ ] `common-split-stage-1`：执行 common 第一阶段分拆
  - 参考手册：`docs/30-designs/COMMON-SPLIT-RUNBOOK.md`
  - 范围对象：root `pom.xml`、`sandwish-common`、新增 `sandwish-common-core`、新增 `sandwish-common-mybatis`、依赖 `sandwish-common` 的业务模块
  - 处理动作：将 `sandwish-common` 改为聚合模块；建立 `sandwish-common-core` 与 `sandwish-common-mybatis`；先迁移持久化公共代码和最小 core 依赖；保持 PageHelper、Mapper XML 和业务行为不变
  - 验收点：`mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package` 通过；MyBatis-Plus 尚未引入；本任务完成后删除或收窄该 TODO

- [ ] `mybatis-plus-introduction`：在 common-mybatis 中引入 MyBatis-Plus
  - 参考手册：`docs/30-designs/MYBATIS-PLUS-INTRODUCTION-RUNBOOK.md`
  - 范围对象：root `pom.xml`、`sandwish-common-mybatis`、MyBatis / PageHelper 依赖声明、MyBatis-Plus 基础配置
  - 处理动作：引入 `com.baomidou:mybatis-plus-boot-starter:3.5.5`；达梦分页插件固定 `DbType.DM`；保持现有 XML 和 Mapper 扫描行为不变；避免 MyBatis starter 重复竞争自动配置
  - 验收点：Admin / Front 包编译通过；现有 XML 链路可继续运行；未迁移任何具体业务 Mapper

- [ ] `xml-elimination-first-chain`：选择首条达梦标准链路消灭 XML
  - 参考手册：`docs/30-designs/XML-ELIMINATION-RUNBOOK.md`
  - 范围对象：一条已完成 DAO 显式化和 DO query 收敛的简单持久化链路、对应 Mapper interface、DAO implementation、DO、`PersistenceAssembler`、`mapping/mysql`、`mapping/dameng`、`mapping/kingbase` XML
  - 处理动作：以达梦 XML 为唯一标准迁移到 MyBatis-Plus / Java 持久化实现；同步删除该链路三组 XML；MySQL / Kingbase 差异不再作为兼容目标
  - 验收点：当前链路不再加载 XML；Admin / Front 相关包编译通过；TODO 删除或收窄该链路剩余工作
