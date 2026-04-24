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

## P0 - 文档治理

- [ ] `docs/00-governance`：补齐治理文档基础集
  - 范围对象：`DOCUMENT-RULES.md`、`NAMING-AND-PLACEMENT-RULES.md`、`DATABASE-RULES.md`、`DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`
  - 处理动作：按 Sandwich 三层 API 架构补齐治理文档，保留文档结构、任务路由与执行闭环
  - 验收点：AI 能按 `docs/AGENT.md` 找到对应治理入口，不需要默认全量加载 `docs`

- [ ] `sandwish-infra`：讨论持久化实现层抽取方案
  - 任务目标：评估新增 `sandwish-infra` 模块，将持久化实现从 `sandwish-biz` 中抽取出来
  - 当前判断：方向可行，但应定位为三层架构中的持久化实现模块，不作为额外业务分层；`biz` 保留 Service、轻量 Entity 和 DAO 接口，`infra` 承载 `DO`、Mapper、`PersistenceAssembler` 和 DAO 实现
  - 需要确认：是否采用 `Controller -> Service(biz) -> DAO interface(biz) -> DAO/Mapper impl(infra) -> DB` 的依赖方向；是否先选择 `storage` 或 `dict` 做试点；是否允许逐步移除 `BaseEntity`
