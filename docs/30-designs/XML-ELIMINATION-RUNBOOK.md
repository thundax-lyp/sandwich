# XML ELIMINATION RUNBOOK

## 1. Purpose

本文档指导逐条持久化链路消灭 Mapper XML。

目标是以达梦 XML 的业务行为为标准，将 Mapper XML 中的 SQL 迁移为 MyBatis-Plus、Java Mapper 注解或明确的 Java 持久化实现，最终删除 `mapping/mysql`、`mapping/dameng`、`mapping/kingbase` XML。

本手册只在 `common` 分拆和 MyBatis-Plus 引入完成后执行。

## 2. Scope

当前范围：

- 按业务对象逐条迁移 Mapper XML
- 达梦 XML 作为唯一行为标准
- 删除对应链路的 MySQL / Kingbase / 达梦 XML
- 用 MyBatis-Plus `BaseMapper` 承接通用单表 CRUD
- 用 Java 持久化代码承接达梦特有语义
- 同步 DAO implementation、Mapper interface、DO、`PersistenceAssembler`

不在范围内：

- 不维护 MySQL / Kingbase 业务等价
- 不保留已迁移链路的 XML 兼容入口
- 不引入新业务分层
- 不让 Controller 直接感知持久化实现
- 不一次性全仓删除 XML

## 3. Fixed Standard

固定标准：

- 达梦业务逻辑是唯一标准
- `mapping/dameng/*.xml` 是迁移前的事实来源
- MySQL / Kingbase XML 不作为迁移校准来源
- 方言差异不再作为长期兼容目标

迁移时如果发现 MySQL / Kingbase 与达梦不同：

- 不按 MySQL / Kingbase 调整目标行为
- 不新增兼容分支
- 按达梦语义实现
- 在当前链路提交中删除对应历史 XML

## 4. Replacement Strategy

优先级固定为：

1. MyBatis-Plus `BaseMapper` 处理标准单表 CRUD
2. MyBatis-Plus `Wrapper` 处理简单条件查询
3. Mapper 注解 SQL 处理少量稳定 SQL
4. Java 持久化实现处理批量、upsert、树结构、关系表维护等复杂行为

禁止：

- 为了消灭 XML 写难以阅读的大段注解 SQL
- 为了使用 Wrapper 改变达梦 SQL 语义
- 用字符串拼接承接动态 SQL
- 把数据库差异重新藏到 Controller / Service

## 5. Refactor Unit

一次只迁移一个业务对象或一组强相关持久化链路。

每个迁移单元必须包含：

- 目标业务对象
- 达梦 XML 行为盘点
- 可由 `BaseMapper` 承接的方法
- 需要 Java 实现的方法
- 需要删除的 XML 文件
- 需要调整的 DAO / Mapper / DO / Assembler
- 编译验证命令

## 6. Candidate Order

固定优先顺序：

1. 已完成显式 DAO / DO query 收敛的链路
2. 单表 CRUD 链路
3. 简单条件查询链路
4. 批量写入或状态更新链路
5. 关系表维护链路
6. 树结构链路

不得先迁移树结构、复杂 join、批量 upsert 或跨表关系维护链路。

## 7. Execution Steps

每条链路固定执行：

1. 读取对应 `mapping/dameng/*.xml`
2. 盘点 SQL 方法、参数、返回映射和副作用
3. 标记 MySQL / Kingbase XML 为历史实现，不作为目标标准
4. 让 Mapper interface 按需继承 `BaseMapper<DO>`
5. 将标准 CRUD 改为 MyBatis-Plus 方法
6. 将达梦专有或复杂 SQL 迁移为 Java 持久化实现或简短注解 SQL
7. 调整 DAO implementation，保持 DAO interface 对 Service 的语义稳定
8. 调整 `PersistenceAssembler`，保持 `Entity <-> DO` 职责清晰
9. 删除该链路的 `mysql`、`dameng`、`kingbase` XML
10. 删除失效的 XML 配置或 mapper 方法
11. 编译验证
12. 同步 TODO，删除或收窄已完成链路

## 8. PageHelper Removal

PageHelper 移除固定在所有分页链路迁移到 MyBatis-Plus 后执行。

删除条件：

- `rg "PageHelper" sandwish-*` 无 main 代码调用
- `pagehelper-spring-boot-starter` 不再被任何模块需要
- MyBatis-Plus 分页固定使用 `DbType.DM`
- Admin / Front 包编译通过

## 9. Verification

每条链路固定验证：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

检查项：

- 当前链路不再加载 Mapper XML
- 当前链路 Mapper interface 不再声明已删除 XML 方法
- DAO interface 对 Service 的业务语义未漂移
- 达梦 XML 原有业务行为已被 Java / MyBatis-Plus 承接
- MySQL / Kingbase 对应 XML 已同步删除
- TODO 已删除或收窄当前已完成项

## 10. Completion Criteria

全局 XML 消灭完成必须满足：

- `sandwish-infra` 中不存在业务 Mapper XML
- `mapping/mysql`、`mapping/dameng`、`mapping/kingbase` 目录已删除
- 所有保留 Mapper 方法都有 Java 实现来源
- PageHelper 已删除
- 达梦是唯一持久化行为标准
- 数据库治理文档已同步长期规则

## 11. Open Items

无
