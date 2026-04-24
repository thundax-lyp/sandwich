# DAO QUERY CONTRACT RUNBOOK

## 1. Purpose

本文档是项目级迁移操作手册，只指导 `Service -> DAO -> Mapper -> Mapper XML` 查询契约重构。

目标是彻底废弃当前基于 `CrudDao.findList(T entity)` 和 `DO.query` 的通用查询模式，改为显式 DAO 查询方法与显式查询参数。

本手册作为老工程治理与迁移的长期参考文档保留在项目中。

## 2. Scope

当前范围：

- 直接替换 DAO 通用查询契约
- 从 `Service` 开始改造 DAO 查询调用
- 调整 DAO interface、DAO implementation、MyBatis Mapper、Mapper XML
- 删除 `DO.query`
- 删除 `PersistenceAssembler` 中的 query 转换
- 展开 `DO` 继承层级，移除 `DO extends ...`
- 为 `DO` 统一补齐 `Getter`、`Setter`、`NoArgsConstructor`、`AllArgsConstructor`

不在范围内：

- 不调整 `Controller`
- 不调整 API 层 `Request` / `Response`
- 不一次性重构写操作契约
- 不引入额外业务分层
- 不保留兼容过渡入口
- 不在本阶段处理 `Entity.query` 的最终去留

## 3. Fixed Boundary

本手册只处理以下调用段：

`Controller -> Service(Command/Query) -> DAO(explicit method) -> Mapper -> Database`

固定模型归属：

- `Controller`：保持现状，不在本手册中调整
- `Service`：继续接收业务语义明确的 `Command` / `Query`
- DAO：只暴露持久化语义明确的方法和参数
- `DO`：只承载数据库字段与必要关系字段
- Mapper XML：只消费一级方法参数或 persistence 参数对象

禁止：

- 继续保留 `CrudDao.findList(T entity)` 作为标准查询入口
- 继续使用 `DO.query`
- 继续让 Mapper XML 读取 `query.xxx`
- 继续让 `PersistenceAssembler` 维护 `Entity.query <-> DO.query`
- 继续让 `DO` 继承任何父类承载公共字段或树结构字段

## 4. Target Contract

### 4.1 Service Contract

- `Service` 固定接收业务语义参数
- 写操作使用 `Command`
- 读操作使用业务语义明确的 `Query`
- `Service` 负责业务校验、权限过滤、默认值补齐和跨 DAO 编排
- `Service` 调用 DAO 时，显式拆解业务查询条件

### 4.2 DAO Contract

- DAO 查询固定使用显式方法，不再复用通用 `findList`
- DAO 查询参数固定使用平铺参数或 persistence 专属参数对象
- DAO 不复用 `Service` 的 `Command` / `Query`
- DAO 查询方法名固定表达动作与条件
- DAO 通用契约不得继续假设 `DO` 继承公共基类

### 4.3 Mapper Contract

- Mapper interface 方法名与 DAO interface 保持一致
- Mapper interface 参数与 DAO interface 保持一致
- Mapper XML 只读取一级参数或 persistence 参数对象字段
- Mapper / XML 不依赖任何 `DO` 父类字段约定

## 5. Method Naming Rules

动作前缀只允许：

- `get`
- `list`
- `page`
- `count`
- `exists`
- `insert`
- `batchInsert`
- `upsert`
- `update`
- `batchUpdate`
- `delete`
- `batchDelete`

查询方法命名固定为：

`动作 + By + 条件`

固定约束：

- `getBy...` 只返回单条
- `listBy...` 只返回列表
- `pageBy...` 只返回分页结果
- `countBy...` 只返回数量
- `existsBy...` 只返回存在性判断
- DAO interface、DAO implementation、Mapper 三层方法名保持一致

禁止：

- `find*`
- `query*`
- `search*`
- `select*`
- 无条件语义的裸 `get` / `list` / `page` / `count`

## 6. Query Parameter Rules

- 查询条件少且关系直接时，固定使用平铺参数
- 查询条件达到明显成组关系时，允许使用 `sandwish-infra` 内部 persistence 参数对象
- persistence 参数对象只服务 DAO / Mapper，不向 `Service` 暴露
- 不允许以 `DO` 充当查询参数对象
- 不允许继续使用 `DO.Query` 内部类

## 7. DO Rules

- `DO` 固定不再保留 `Query` 内部类
- `DO` 固定不再提供 `getQuery()` / `setQuery()`
- `DO` 固定不继承任何类
- 公共字段不通过继承复用，按表结构在各 `DO` 中显式展开
- 树结构字段不通过继承复用，按表结构在各 `DO` 中显式展开
- `DO` 统一补齐 `@Getter`
- `DO` 统一补齐 `@Setter`
- `DO` 统一补齐 `@NoArgsConstructor`
- `DO` 统一补齐 `@AllArgsConstructor`
- 不默认增加 `@Data`
- 带行为的访问器方法不得机械替换

## 8. Persistence Assembler Rules

- `PersistenceAssembler` 只负责 `Entity <-> DO`
- 删除所有 query 转换方法
- 不新增 `Command/Query <-> persistence 参数对象` 转换职责
- `Service Query -> DAO 参数` 的拆解固定发生在 `Service`

## 9. Execution Strategy

本次改造固定走彻底替换，不保留兼容层。

执行顺序固定为：

1. 盘点 DAO interface 中当前依赖通用查询契约的方法
2. 为每条查询链路定义显式 DAO 查询方法名
3. 明确每条查询链路的参数形态：平铺或 persistence 参数对象
4. 从 `Service` 调整 DAO 调用
5. 同步调整 DAO implementation
6. 同步调整 Mapper interface
7. 同步调整 Mapper XML
8. 删除对应 `DO.query`
9. 删除对应 `PersistenceAssembler` query 转换代码
10. 展开 `DO` 继承字段并移除 `DO extends ...`
11. 替换废弃的 `CrudDao` / `TreeDao` 基类字段假设
12. 删除废弃的 DAO 通用查询入口

## 10. Refactor Unit

一次只改一个业务对象或一组强相关查询链路。

每个改造单元必须满足：

- 影响范围可解释
- `Service` 调整可闭环
- DAO / Mapper / XML 同步完成
- 不需要同步改 `Controller`
- 能在一个或少量 commit 中清楚说明
- `DO` 去基类化能够在当前单元内闭环

## 11. Verification

每个改造单元完成后固定检查：

- `Service` 已不再向 DAO 传递依赖 `entity.query` 的对象
- DAO 已不再暴露通用查询方法
- Mapper XML 已不再出现 `query.xxx`
- `DO` 已删除 `Query` 内部类与 `getQuery()` / `setQuery()`
- `DO` 已不再继承任何父类
- `PersistenceAssembler` 已删除 query 转换
- 三层方法名一致
- 影响模块编译通过

## 12. Completion Criteria

单个查询链路完成必须满足：

- `Controller` 无需同步修改
- `Service` 保持业务语义参数
- DAO 查询契约已改为显式方法
- 查询参数已平铺或收敛为 persistence 参数对象
- `DO.query` 已从该链路移除
- `DO extends ...` 已从该链路移除
- Mapper XML 已不依赖 `query.xxx`
- `DO` 已完成本手册规定的 `lombok` 装配与字段展开
- 代码、测试、文档和 commit 能共同解释该次改造

## 13. Open Items

无
