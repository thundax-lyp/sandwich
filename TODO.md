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
