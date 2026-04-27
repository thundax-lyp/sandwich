# Sandwich Architecture Intent

本文档解释架构治理背后的意图。规则正文以 [`ARCHITECTURE.md`](./ARCHITECTURE.md) 为准。

## 1. Purpose

Sandwich 的目标是在现有 Java 8、Spring Boot 2、jar 应用和三层 API 架构基础上建立可持续治理方式。

本文档只解释以下意图：

- 为什么保持三层架构
- 为什么先修模型边界再替换持久化工具
- 为什么业务语义和持久化机制要分离
- 为什么控制抽象新增
- 为什么收敛 common 工具层级
- 为什么区分 formatter 和 rule gate
- 为什么采用小步提交
- 为什么 `commit message` 是工程记忆

## 2. Architecture Direction

Sandwich 固定以现有三层架构为主线：

`HTTP/API -> Controller -> Service -> DAO/Mapper -> Database`

治理目标是让这条链路更清晰，并保持文档、代码、测试和提交记录之间的闭环。

## 3. Persistence Evolution Intent

Sandwich 的持久化演进不是为了追逐某个工具或单纯减少文件数量，而是为了让业务语义留在业务层，让持久化复杂性收敛到 infra。

在引入 MyBatis-Plus、拆分 common 或删除 Mapper XML 之前，必须先判断现有模型边界是否干净。工具替换不能固化旧边界泄漏；如果 domain / Service 已经承载了持久化索引、分页方言或 SQL 维护算法，应先收敛边界，再替换工具。

业务字段和持久化机制固定分开理解：

- 业务关系字段表达业务事实，可以进入 Entity、Service 参数和 API 模型。
- 持久化索引、分页方言、批量 SQL、upsert、树区间维护等机制服务于数据库访问，不应进入 domain 或 Controller。

复杂持久化机制固定归属 infra。Service 负责业务动作、事务边界和编排；infra 负责把这些动作转换为数据库可执行的查询、更新和索引维护。

消灭 Mapper XML 不等于消灭复杂性。复杂 SQL 不能因为去 XML 而被挤入 Controller、Service、大段注解 SQL 或脆弱 Wrapper。迁移后的复杂性必须仍然有明确的持久化实现归属，并保持三层边界可读、可验证。

## 4. Common Simplification Intent

收敛 `common` 的目标不是单纯追求更少文件或更短代码，而是减少不必要的代码层级关系，让 AI 和开发者更容易直接理解真实调用链。

`common` 中只应保留项目确实需要的公共语义。薄封装、无调用工具方法、只转发第三方库的方法和空壳继承类，会让阅读者多跳一层才能到达真正行为，也会让 AI 在修改时误判边界、误以为存在项目专属规则。因此，能在调用点清楚表达的薄方法优先展开；没有项目语义增量的包装类优先删除或收窄。

保留 `common` 能力时，应满足至少一个条件：

- 承载 Sandwich 自己的稳定语义，而不只是代理第三方 API。
- 被多个模块复用，并且统一入口能降低真实重复复杂度。
- 保护三层架构边界，例如 Web、线程、存储、加密、i18n 等基础设施契约。

删除或展开 `common` 能力时，不能把复杂逻辑散落到 Controller 或业务流程里。收敛层级的判断标准是“让真实行为更直接可见”，不是把共享复杂性复制到各处。

## 5. Quality Tool Intent

质量工具的目标是降低协作摩擦，而不是把格式化、规约和架构判断混成一个黑盒。

`spotless` 固定作为 formatter。它可以整理 import、空白、换行和代码版式，让机械格式问题不占用审阅注意力。

`checkstyle` 固定作为 rule gate。它只表达必须阻断的代码规约，例如命名、非法 import、直接书写全限定类名等，不负责改代码。

这两个工具的职责必须分开：格式问题进入 `spotless`，规约问题进入 `checkstyle`。同一类规则不应在两边重复配置，否则后续 AI 和开发者会难以判断到底应该“自动修复”还是“人工改设计”。

Sandwich 当前固定 Java 8 和 Spring Boot 2.0.5.RELEASE。质量工具可以参考其他项目的用法，但版本必须服从本项目运行约束；如果上游项目使用更高 JDK 的插件或 formatter，应在 Sandwich 中降级到 Java 8 可运行版本。代码必须主动适配质量规则，不通过放松规则、长期 suppression 或长期 baseline 回避违规。

## 6. Small Step Commit Intent

小步提交不是单纯的 Git 使用习惯，而是本项目控制复杂度的一部分。

每次提交只收敛一个明确判断，是为了把试错半径压到最小，让回退、定位、复盘都落在可控范围内，而不是把多个判断混在一次大改中。

`commit message` 的意图不是重复“改了文件”，而是给这次判断建立可检索的工程记忆：

- 在时间上保留决策顺序，便于之后回看“先做了什么、为什么回退、规则何时收紧”。
- 在空间上标记改动落点，便于快速定位这是哪个模块、哪一层、哪一类问题。
- 在治理上把代码、测试、文档、`TODO.md` 清理绑定为同一条可解释的变更链路。

因此，AI 和开发者都不应把不相关修改堆叠到同一个提交里，也不应使用失去语义的 `commit message`。提交历史在本项目中承担的是“可回放的决策轨迹”，而不只是代码快照存档。

## 7. Commit Message Shape

本项目固定提交格式：

`Type(domain): 中文说明`

其中：

- `Type` 表达改动类型，例如 `Feat`、`Fix`、`Docs`、`Test`、`Refactor`、`Chore`
- `domain` 表达业务域、模块或治理域，例如 `admin`、`front`、`biz`、`common`、`storage`、`sys`、`governance`
- 中文说明表达具体能力变化，不写空泛的“调整”“优化”“修改”

## 8. Open Items

无
