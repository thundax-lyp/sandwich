# Sandwich Architecture Intent

本文档解释架构治理背后的意图。规则正文以 [`ARCHITECTURE.md`](./ARCHITECTURE.md) 为准。

## 1. Purpose

Sandwich 的目标是在现有 Java 8、Spring Boot 2、jar 应用和三层 API 架构基础上建立可持续治理方式。

本文档只解释以下意图：

- 为什么保持三层架构
- 为什么控制抽象新增
- 为什么采用小步提交
- 为什么 `commit message` 是工程记忆

## 2. Architecture Direction

Sandwich 固定以现有三层架构为主线：

`HTTP/API -> Controller -> Service -> DAO/Mapper -> Database`

治理目标是让这条链路更清晰，并保持文档、代码、测试和提交记录之间的闭环。

## 3. Small Step Commit Intent

小步提交不是单纯的 Git 使用习惯，而是本项目控制复杂度的一部分。

每次提交只收敛一个明确判断，是为了把试错半径压到最小，让回退、定位、复盘都落在可控范围内，而不是把多个判断混在一次大改中。

`commit message` 的意图不是重复“改了文件”，而是给这次判断建立可检索的工程记忆：

- 在时间上保留决策顺序，便于之后回看“先做了什么、为什么回退、规则何时收紧”。
- 在空间上标记改动落点，便于快速定位这是哪个模块、哪一层、哪一类问题。
- 在治理上把代码、测试、文档、`TODO.md` 清理绑定为同一条可解释的变更链路。

因此，AI 和开发者都不应把不相关修改堆叠到同一个提交里，也不应使用失去语义的 `commit message`。提交历史在本项目中承担的是“可回放的决策轨迹”，而不只是代码快照存档。

## 4. Commit Message Shape

本项目固定提交格式：

`Type(domain): 中文说明`

其中：

- `Type` 表达改动类型，例如 `Feat`、`Fix`、`Docs`、`Test`、`Refactor`、`Chore`
- `domain` 表达业务域、模块或治理域，例如 `admin`、`front`、`biz`、`common`、`storage`、`sys`、`governance`
- 中文说明表达具体能力变化，不写空泛的“调整”“优化”“修改”

## 5. Open Items

无
