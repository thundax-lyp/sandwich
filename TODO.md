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
