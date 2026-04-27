# MYBATIS PLUS INTRODUCTION RUNBOOK

## 1. Purpose

本文档指导在 `sandwish-common-mybatis` 中引入 MyBatis-Plus。

目标是以达梦数据库行为为标准，将 MyBatis-Plus 作为持久化基础能力引入项目，同时保持现有 Mapper XML 链路可运行，为后续消灭 XML 做准备。

本手册只处理依赖、配置、扫描和分页基础设施，不处理具体业务 Mapper 改写。

## 2. Scope

当前范围：

- 引入 MyBatis-Plus Boot2 starter
- 将 MyBatis-Plus 依赖归属 `sandwish-common-mybatis`
- 达梦作为唯一目标数据库标准
- 配置 MyBatis-Plus 分页插件使用 `DbType.DM`
- 保持现有 Mapper XML 可运行
- 保持现有 DAO / Mapper 方法不变

不在范围内：

- 不消灭 Mapper XML
- 不迁移具体 DAO 到 `BaseMapper`
- 不批量替换 PageHelper 调用
- 不再维护 MySQL / Kingbase 方言等价目标
- 不调整业务查询语义

## 3. Version Decision

固定版本基线：

- Spring Boot：`2.0.5.RELEASE`
- Java：`1.8`
- MyBatis-Plus：`3.5.5`
- MyBatis-Plus starter：`com.baomidou:mybatis-plus-boot-starter`

禁止：

- 不使用 `mybatis-plus-spring-boot3-starter`
- 不直接追随最新 MyBatis-Plus 版本
- 不同时显式引入 `mybatis-spring-boot-starter`、`mybatis`、`mybatis-spring` 与 MyBatis-Plus starter 形成版本冲突

## 4. Database Standard

固定口径：

- 达梦是唯一业务行为标准
- `mapping/dameng/*.xml` 是迁移到 Java / MyBatis-Plus 前的事实标准
- MySQL / Kingbase XML 差异不作为兼容目标维护
- MySQL / Kingbase 差异视为历史实现或编码偏差

影响：

- MyBatis-Plus 分页插件固定使用 `DbType.DM`
- 新增持久化行为固定按达梦语义实现
- 后续迁移 XML 时，以达梦 XML 校准行为

## 5. Dependency Rules

MyBatis-Plus 依赖固定放在 `sandwish-common-mybatis`。

迁移期允许 `PageHelper` 继续存在，但固定限制：

- PageHelper 只服务尚未迁移的旧分页链路
- MyBatis-Plus 分页只服务明确迁移后的链路
- 同一查询链路不得同时使用 PageHelper 和 MyBatis-Plus 分页

完成 MyBatis-Plus 分页迁移后，删除 PageHelper。

## 6. Configuration Rules

固定新增 MyBatis-Plus 配置：

- 配置类归属 `sandwish-common-mybatis`
- 使用 `MybatisPlusInterceptor`
- 添加 `PaginationInnerInterceptor`
- `PaginationInnerInterceptor` 固定使用 `DbType.DM`

禁止：

- 不按 profile 切换 MySQL / Kingbase 分页方言
- 不为历史数据库差异新增配置开关
- 不在 API 应用模块中重复声明 MyBatis-Plus 基础插件

## 7. Mapper Rules

引入阶段固定保持：

- 原 `@MapperScan` 扫描规则不变
- 原 `@MyBatisDao` 标记不变
- 原 Mapper XML 路径不变
- 原 DAO implementation 调用 Mapper 方式不变

允许：

- 后续单个链路迁移时，Mapper interface 继承 MyBatis-Plus `BaseMapper`

禁止：

- 引入阶段批量让所有 Mapper 继承 `BaseMapper`
- 引入阶段删除 XML
- 引入阶段删除 DAO implementation / `PersistenceAssembler`

## 8. Execution Steps

执行顺序固定为：

1. 确认 `sandwish-common-mybatis` 已完成第一阶段分拆
2. 在 root `pom.xml` 管理 `mybatis-plus.version`
3. 在 `sandwish-common-mybatis` 引入 `mybatis-plus-boot-starter`
4. 移除或替换同模块内重复 MyBatis starter 依赖
5. 新增 MyBatis-Plus 基础配置，分页方言固定 `DbType.DM`
6. 检查 API 应用和 `sandwish-infra` 依赖传递关系
7. 保持 XML 和旧 Mapper 行为不变
8. 编译验证

## 9. Verification

固定验证：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

检查项：

- 不存在 MyBatis starter 与 MyBatis-Plus starter 重复竞争自动配置
- Mapper 扫描正常
- 现有 Mapper XML 能被加载
- `DbType.DM` 分页插件装配成功
- PageHelper 旧链路未被本步骤改写

## 10. Completion Criteria

MyBatis-Plus 引入完成必须满足：

- `sandwish-common-mybatis` 统一承载 MyBatis-Plus 依赖
- MyBatis-Plus 使用 Boot2 starter
- 达梦分页插件配置明确
- 旧 XML 链路继续保留
- 未发生具体业务 Mapper 改写
- 后续 XML 消灭可按链路推进

## 11. Open Items

无
