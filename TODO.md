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

## P0 - Common Test / ArchUnit 模型注解规约

- [ ] `archunit-do-annotation-baseline`：盘点 DO 注解规约适用范围
  - 依赖前置：完成 `common-test-baseline-module`
  - 范围对象：`sandwish-infra` 下 `persistence.dataobject` 包、现有 `DO/DataObject`、Redis 持久化对象和数据库表映射对象
  - 处理动作：确认哪些对象必须补齐 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor`、`@TableName`，并记录 Redis-only DO 是否需要改名或迁移出 DO 命名体系
  - 验收点：ArchUnit 规则的扫描范围和例外边界明确，不在实现阶段临时扩大或缩小口径
- [ ] `archunit-do-annotation-rule`：引入 ArchUnit 并新增 DO 注解门禁
  - 依赖前置：完成 `archunit-do-annotation-baseline`
  - 范围对象：根 `pom.xml`、承载架构测试的模块 `pom.xml`、ArchUnit 测试类
  - 处理动作：新增 ArchUnit 测试依赖；将 `NAME_DATA_OBJECT_REQUIRED_ANNOTATIONS` 固化为自动化规则；只允许围绕该规约增加最小测试支撑
  - 验收点：缺少任一必需注解的 `DO/DataObject` 会被测试拦截
- [ ] `do-required-annotation-migration`：迁移现有 DO 到必需注解形态
  - 依赖前置：完成 `archunit-do-annotation-rule`
  - 范围对象：现有 `DO/DataObject` 类及其必要 import
  - 处理动作：补齐 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor`、`@TableName`；不顺手删除手写 getter/setter，除非单独拆出重构任务
  - 验收点：ArchUnit 规则通过；迁移不改变 DAO、Mapper、XML 和 Service 行为

## P0 - Cache / Infra 边界演进

- [ ] `cache-infra-boundary-split-todo`：拆分缓存迁移执行子任务
  - 参考手册：`docs/30-designs/CACHE-INFRA-BOUNDARY-RUNBOOK.md`
  - 范围对象：`TODO.md`、`CrudServiceImpl` 缓存职责、直接继承 `CrudServiceImpl` 的缓存链路、`RedisClient` 调用点
  - 处理动作：只盘点和拆分 TODO；按手册生成可审阅的子任务，第一项必须是现状盘点；不得直接引入 JetCache；不得直接改代码
  - 验收点：缓存迁移不再是笼统大任务；TODO 中每个后续执行项都能独立验收、独立提交，并明确是否允许引入 JetCache 或删除 `CrudServiceImpl` 方法
