# COMMON SPLIT RUNBOOK

## 1. Purpose

本文档指导 `sandwish-common` 分拆为多个 `sandwish-common-*` 子模块。

目标是在不改变业务行为的前提下，先把公共持久化能力、基础工具、Web 支撑和专项技术能力分离，为后续引入 MyBatis-Plus 和消灭 Mapper XML 提供清晰承载位置。

本手册是专项迁移手册，不沉淀为长期架构红线。

## 2. Scope

当前范围：

- 新增 `sandwish-common` 聚合模块
- 将原 `sandwish-common` jar 分拆为多个 `sandwish-common-*` jar
- 优先拆出 `sandwish-common-mybatis`
- 保持现有包名 `com.github.thundax.common.*` 不变
- 保持业务行为、Mapper XML、PageHelper 行为不变
- 调整 Maven 依赖关系

不在范围内：

- 不引入 MyBatis-Plus
- 不删除 PageHelper
- 不消灭 Mapper XML
- 不重写 DAO / Mapper 方法
- 不调整 Controller / Service 业务语义
- 不照搬其他项目的全部 common 模块数量

## 3. Target Module Shape

固定目标形态：

```text
sandwish-common
├── sandwish-common-core
├── sandwish-common-mybatis
├── sandwish-common-web
├── sandwish-common-redis
├── sandwish-common-storage
└── sandwish-common-test
```

阶段性允许只创建已经需要的模块。不得为了目录完整一次性新增无承载内容的模块。

## 4. Module Responsibilities

### 4.1 `sandwish-common-core`

职责：

- 纯 Java 工具类
- 集合、编码、日期、JSON、反射、字符串、文件基础工具
- `Global`
- `PooledThreadLocal`
- 基础异常和无 Web / DB 绑定的通用能力

禁止：

- 不依赖 MyBatis
- 不依赖 PageHelper
- 不依赖 Servlet API
- 不依赖 Redis starter
- 不依赖文件存储实现

### 4.2 `sandwish-common-mybatis`

职责：

- `common/persistence/**`
- `CrudService` / `CrudServiceImpl`
- `TreeService` / `TreeServiceImpl`
- `BaseService` / `BaseServiceImpl` 中与持久化服务基类相关的能力
- MyBatis 扫描标记
- PageHelper 迁移期依赖
- 数据库方言类

禁止：

- 不承载业务 DAO implementation
- 不承载业务 Mapper XML
- 不引入 MyBatis-Plus，直到执行 MyBatis-Plus 引入手册

### 4.3 `sandwish-common-web`

职责：

- `common/web/**`
- Servlet / HTTP 请求响应工具
- Cookie 工具
- Web filter 基础支撑

禁止：

- 不承载业务 Controller
- 不承载后台或前台登录态规则
- 不承载持久化基础设施

### 4.4 `sandwish-common-redis`

职责：

- Redis client、连接、序列化和通用 Redis 技术支撑

禁止：

- 不承载业务 Redis key 规则
- 不承载业务 Redis DAO implementation

### 4.5 `sandwish-common-storage`

职责：

- FTP / local file 通用技术能力
- 通用资源抽象
- 文件名过滤器

禁止：

- 不承载 `storage` 业务表持久化
- 不承载业务文件绑定规则

### 4.6 `sandwish-common-test`

职责：

- 测试基类
- 测试工具
- 测试依赖集中承载

禁止：

- 不被 main scope 依赖

## 5. Execution Order

固定执行顺序：

1. 将当前 `sandwish-common` 改为 `pom` 聚合模块
2. 新增 `sandwish-common-core`
3. 新增 `sandwish-common-mybatis`
4. 先迁移 `common/persistence/**` 与持久化 service 基类到 `sandwish-common-mybatis`
5. 迁移被 `sandwish-common-mybatis` 依赖的最小 core 工具到 `sandwish-common-core`
6. 调整 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api` 依赖
7. 验证编译
8. 再按依赖关系拆 `web`、`redis`、`storage`、`test`

## 6. First Refactor Unit

第一组只允许完成：

- `sandwish-common` 聚合化
- `sandwish-common-core` 建立
- `sandwish-common-mybatis` 建立
- 持久化公共代码迁入 `sandwish-common-mybatis`
- 被持久化公共代码直接依赖的最小 core 工具迁入 `sandwish-common-core`
- Maven 依赖调整

本组不允许：

- 改 PageHelper 调用
- 改 Mapper XML
- 改 DAO / Mapper 方法名
- 改业务查询语义
- 引入 MyBatis-Plus

## 7. Dependency Rules

固定依赖方向：

```text
sandwish-common-mybatis -> sandwish-common-core
sandwish-common-web -> sandwish-common-core
sandwish-common-redis -> sandwish-common-core
sandwish-common-storage -> sandwish-common-core
```

禁止：

- `sandwish-common-core` 反向依赖其他 common 子模块
- common 子模块依赖 `sandwish-biz`、`sandwish-infra`、API 应用
- API 应用直接依赖过多 common 子模块，优先通过 `sandwish-infra` / `sandwish-biz` 的真实需要传递

## 8. Verification

每个分拆单元完成后固定验证：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

检查项：

- Maven reactor 能识别新增模块
- `sandwish-common-core` 不含 MyBatis / PageHelper / Servlet / Redis starter 依赖
- `sandwish-common-mybatis` 承载持久化公共依赖
- 现有 Mapper XML 路径和扫描行为不变
- Admin / Front 包编译通过

## 9. Completion Criteria

`common` 第一阶段完成必须满足：

- `sandwish-common` 已成为聚合模块
- `sandwish-common-core` 与 `sandwish-common-mybatis` 已建立
- 持久化公共代码已归属 `sandwish-common-mybatis`
- 原有业务行为未改变
- MyBatis-Plus 尚未引入
- 代码、TODO 和 commit 能解释本次只做结构拆分

## 10. Open Items

无
