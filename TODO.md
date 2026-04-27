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

## P0 - Common Cache / RedisClient JetCache 迁移

执行原则：

- 按列表顺序执行，除非某一步编译失败要求拆分。
- JetCache 只能出现在 `sandwish-common-cache`、`sandwish-infra` 的实现细节，不能外泄到 Controller、Service、Entity、Request、Response。
- 迁移期允许 `RedisClient` 暂存；新增或改造代码不得新增 `RedisClient` 调用点。

- [ ] `common-cache-migration-cleanup`：收尾清理 common-cache 迁移现场
  - 依赖前置：common-core 已删除 `RedisClient`
  - 范围对象：过期 RedisClient runbook、缓存边界文档、TODO 已完成项、Maven 依赖、应用配置、无效 package 和 `.gitkeep`
  - 处理动作：删除或收窄旧 RedisClient/infra adapter 文档；移除不再需要的 `spring-boot-starter-data-redis` 依赖；删除完成的 TODO 项或改写为下一阶段明确任务；清理空目录占位文件；保留 `COMMON-CACHE-JETCACHE-RUNBOOK.md` 作为最终运行说明
  - 允许引入 JetCache：否
  - 允许删除 `RedisClient`：已完成后执行
  - 验收点：`rg -n "RedisClient|common.utils.redis" docs TODO.md sandwish-common sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api -g '*.java' -g '*.md' -g 'pom.xml'` 仅保留历史说明或无输出；`git status --short` 干净
