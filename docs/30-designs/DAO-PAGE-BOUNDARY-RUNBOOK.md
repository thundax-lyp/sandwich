# DAO 分页边界单项迁移 Runbook

## Purpose

本文档定义单个 `DAO` 分页方法的迁移手册。

每次只处理一个 `DAO`，对应一个 `TODO.md` item。批量范围只放在 `TODO.md` 中排队，不放进本手册。

## Input

执行一个 TODO item 前，先确认该项给出的精确文件：

- `DAO interface`
- `DAO implementation`
- 对应 `Service implementation`
- 对应测试文件

## Boundary

固定边界：

- `DAO interface` 不返回 `com.github.thundax.common.persistence.Page`。
- `DAO implementation` 不构造业务分页模型。
- `DAO` 分页返回类型使用 `com.baomidou.mybatisplus.extension.plugins.pagination.Page<Entity>`。
- `Service implementation` 负责把 DAO 分页结果组装为业务分页模型。
- `Controller` 和 `Service interface` 对外分页返回行为不变。

## Steps

### 1. 读取单项范围

只读取当前 TODO item 的范围文件，确认：

- 当前分页方法签名。
- 当前 `Service` 如何归一化 `pageNo/pageSize`。
- 当前测试如何 fake DAO 返回值。

### 2. 修改 DAO interface

将当前 DAO 的分页方法返回类型改为 MyBatis-Plus `Page<Entity>`。

要求：

- 删除 `com.github.thundax.common.persistence.Page` import。
- 增加 `com.baomidou.mybatisplus.extension.plugins.pagination.Page` import。
- 不改变分页查询参数语义。

### 3. 修改 DAO implementation

让 implementation 直接返回 MyBatis-Plus 分页结果。

要求：

- Mapper 查询仍在 infra 内完成。
- DO 到 Entity 的 records 映射仍在 infra 内完成。
- `total/current/size` 由 MyBatis-Plus Page 承载。
- 不创建 `com.github.thundax.common.persistence.Page`。

### 4. 修改 Service implementation

Service 接收 DAO 返回的 MyBatis-Plus Page 后，组装业务分页模型。

要求：

- 保留现有 `pageNo/pageSize` 归一化逻辑。
- 保留 Service interface 返回类型。
- 保留 Controller 可见行为。
- 不让 Controller 依赖 MyBatis-Plus Page。

### 5. 更新测试

更新当前 DAO 对应 Service 测试。

要求覆盖：

- DAO 收到归一化后的 `pageNo/pageSize`。
- Service 返回业务分页模型。
- `count/list/pageNo/pageSize` 结果保持不变。

### 6. 单项验证

执行当前模块相关测试。若该 DAO 没有现成测试，补齐或在 TODO 中收窄剩余测试任务。

固定搜索检查：

```bash
rg "com.github.thundax.common.persistence.Page" 当前DAO接口 当前DAO实现
```

期望该搜索无结果。

### 7. 单项收口

收口前固定处理：

- 删除当前 TODO item，或收窄为未完成部分。
- 删除失效 import。
- 删除本项产生的空目录。
- 确认未修改非本项范围文件。
- 提交当前 DAO 的迁移 commit。

## Completion Criteria

单个 DAO item 完成时必须同时满足：

- DAO interface 不返回业务分页模型。
- DAO implementation 不构造业务分页模型。
- Service 仍返回业务分页模型。
- 对外 API 行为不变。
- 测试或明确的剩余测试 TODO 已收口。
- 工作区没有本项遗留现场。
