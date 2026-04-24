# Sandwich 系统架构

本文件只保留架构决策和实现红线。本文档是讨论稿，后续规则收敛后再拆分到命名、数据库、部署等专项治理文档。

相关文档：

- 架构意图、小步提交和决策记忆见 [`ARCHITECTURE-INTENT.md`](./ARCHITECTURE-INTENT.md)
- 新增类、改类名、改目录、判断模块归属见 [`NAMING-AND-PLACEMENT-RULES.md`](./NAMING-AND-PLACEMENT-RULES.md)
- 数据库、实体、DAO、Mapper 和持久化查询见 [`DATABASE-RULES.md`](./DATABASE-RULES.md)
- 文档写作与维护见 [`DOCUMENT-RULES.md`](./DOCUMENT-RULES.md)
- 上线准备、运维和 jar 打包见 [`DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`](./DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md)

## Project Baseline

- project: Maven multi-module
- java: 8
- spring-boot: 2.0.5.RELEASE
- packaging:
  - `sandwish-common`: jar
  - `sandwish-biz`: jar
  - `sandwish-infra`: jar
  - `sandwish-admin-api`: jar
  - `sandwish-front-api`: jar
- base package: `com.github.thundax`
- persistence: MyBatis + PageHelper
- api docs: Swagger / Springfox

## Architecture Shape

Sandwich 固定采用三层 API 架构。

固定主链路为：

`HTTP/API -> Controller -> Service -> DAO/Mapper -> Database`

三层职责固定为：

- Web 层：请求入口、登录态、权限、参数接收、API 响应组装。
- Service 层：业务流程、事务边界、业务校验、跨 DAO 编排。
- DAO/Mapper 层：持久化访问、SQL 映射、分页查询、数据装载。

以下目录语义不作为 Sandwich 默认业务架构：

- `interfaces`
- `application`
- `domain`
- `facade`
- `repository`

`sandwish-infra` 固定只作为持久化实现模块，不引入额外业务分层语义。

## Module Boundaries

### `sandwish-common`

职责：

- 通用工具类
- 基础 Web 支撑
- 通用编码、加密、集合、日期、文件工具
- i18n 支撑
- 通用 Service 基类
- 通用持久化基础设施
- Redis client、连接、序列化和通用技术支撑
- 存储、线程等通用技术能力

边界：

- 不承载具体业务流程。
- 不依赖 `sandwish-biz`、`sandwish-admin-api`、`sandwish-front-api`。
- 新增通用能力前，必须确认不是某个业务模块的专用逻辑。

### `sandwish-biz`

职责：

- 共享业务实体
- DAO interface
- Service
- 业务工具
- 后台与前台复用的业务能力
- 系统、认证、会员、存储、辅助能力等业务模块

边界：

- 可以依赖 `sandwish-common`。
- 不依赖 `sandwish-infra`、`sandwish-admin-api` 和 `sandwish-front-api`。
- 不放 Controller 入口适配。
- 不放 `DO` / `DataObject`。
- 不放 MyBatis Mapper implementation。
- 不放 Mapper XML。
- 业务规则优先收敛到 Service，不下沉到 Controller。

### `sandwish-infra`

职责：

- 持久化实现
- Redis persistence implementation
- `DO` / `DataObject`
- DAO implementation
- MyBatis Mapper
- Mapper XML
- `PersistenceAssembler`
- 数据库类型转换和分页查询实现
- 业务 Redis 持久化实现、业务 Redis DAO implementation、Redis Mapper 和 Redis Mapper XML

边界：

- 可以依赖 `sandwish-biz` 和 `sandwish-common`。
- 不依赖 `sandwish-admin-api` 和 `sandwish-front-api`。
- 不承载业务流程。
- 不暴露 HTTP 模型。
- 不让 Controller 直接调用。
- 持久化包路径固定保留 `persistence` 包段，用于显式区分业务侧 DAO interface 与 infra 侧持久化实现。

### `sandwish-admin-api`

职责：

- 后台 API 应用入口
- 后台 Controller
- 后台配置
- 后台安全、日志、Swagger、任务等入口适配
- 后台静态 API 支撑资源
- 后台专用工具与 VO

边界：

- 可以依赖 `sandwish-biz`。
- 面向管理端能力，不承载前台用户访问语义。
- Controller 不直接编写复杂业务流程；复杂流程进入 Service。
- Controller 只输出 API 响应，不返回服务端页面视图。

### `sandwish-front-api`

职责：

- 前台 API 应用入口
- 前台 Controller
- 前台配置
- 前台安全、Shiro、过滤器、拦截器等入口适配
- 前台静态 API 支撑资源
- 前台专用工具与 VO

边界：

- 可以依赖 `sandwish-biz`。
- 面向前台访问能力，不承载后台管理语义。
- 前台登录态、权限和会话适配固定在前台入口模块处理。
- 复用业务能力必须通过 `sandwish-biz`，不得复制后台业务实现。

## Dependency Direction

固定依赖方向为：

`sandwish-admin-api -> sandwish-infra -> sandwish-biz -> sandwish-common`

`sandwish-front-api -> sandwish-infra -> sandwish-biz -> sandwish-common`

禁止依赖方向：

- `sandwish-common` 不得依赖任何业务或入口模块。
- `sandwish-biz` 不得依赖 `sandwish-infra`、`sandwish-admin-api` 或 `sandwish-front-api`。
- `sandwish-infra` 不得依赖 `sandwish-admin-api` 或 `sandwish-front-api`。
- `sandwish-admin-api` 与 `sandwish-front-api` 不得互相依赖。
- 后台与前台不得通过复制 Service 实现来共享业务能力。

## Layer Rules

### Controller

- 负责 HTTP 请求入口、参数接收、基础校验、登录态和权限适配。
- 负责返回 API 响应。
- 可以调用 Service。
- 不直接访问 DAO / Mapper。
- 不直接拼接复杂 SQL。
- 不承载核心业务规则。

### Service

- 负责业务流程、事务、状态流转、业务校验和跨 DAO 编排。
- 可以依赖 DAO / Mapper。
- 可以依赖 `sandwish-common` 的通用工具和基础服务。
- 对外提供稳定业务方法，避免让 Controller 感知过多持久化细节。
- 跨模块业务复用优先放在 `sandwish-biz` 的 Service。

### DAO / Mapper

- 负责数据库访问和 SQL 映射。
- 查询、分页、过滤、排序优先下推到持久化层。
- 不承载业务流程。
- 不处理 Web 会话、权限适配和页面语义。
- SQL 变化必须同步检查实体、Mapper XML、Service 调用和数据库文档。
- DAO interface 固定归属 `sandwish-biz`。
- DAO implementation、MyBatis Mapper、Mapper XML 和 `DO` / `DataObject` 固定归属 `sandwish-infra`。
- `PersistenceAssembler` 固定归属 `sandwish-infra`，只负责 `Entity <-> DO/DataObject` 转换。

### Entity / VO / DTO

- Entity 优先表达持久化对象或业务数据对象。
- VO / DTO 用于入口响应、页面展示或跨层传输。
- 不在 VO / DTO 中写复杂业务流程。
- 不强制引入值对象、聚合根等非当前架构必需概念。

### Static

- 静态资源仅用于 API 文档、上传访问或其他运行支撑。
- 不新增服务端页面模板、页面装饰器或标签库作为业务入口。
- 前端页面和交互由独立前端项目承载。
- 服务端 Service 必须保留关键业务校验。

## Web Application Rules

- `sandwish-admin-api` 和 `sandwish-front-api` 是两个独立 jar API 应用。
- 两个 API 应用可以复用 `sandwish-infra`、`sandwish-biz` 和 `sandwish-common`。
- 不新增 `src/main/webapp`、`WEB-INF`、服务端页面模板、标签库或页面装饰器配置。
- 配置文件固定放在 `src/main/resources/config`。
- 静态资源固定放在 `src/main/resources/static` 或现有静态资源目录。
- API 文档能力保留 Swagger / Springfox。

## Business Module Rules

- 业务模块当前按 `com.github.thundax.modules.*` 组织。
- 同一业务对象的 Controller、Service、DAO interface、Entity 和 API 支撑资源应按固定层归属放置，并保持业务模块路径一致。
- 后台专用入口放在 `sandwish-admin-api`。
- 前台专用入口放在 `sandwish-front-api`。
- 前后台共享业务规则放在 `sandwish-biz`。
- 前后台共享持久化实现放在 `sandwish-infra`。
- 通用但无业务语义的工具放在 `sandwish-common`。

## Transaction And Consistency

- 事务边界默认放在 Service。
- Controller 不声明复杂事务。
- DAO / Mapper 不负责跨表业务一致性。
- 跨 DAO 写入必须由 Service 明确编排。
- 失败回滚、重复提交、防重、状态前置条件等规则必须在 Service 中可见。

## Security And Session Boundary

- Web 登录态、权限、过滤器、拦截器属于 API 入口模块。
- 后台和前台安全语义分开维护。
- Service 不直接依赖 Servlet 视图语义。
- Service 如需当前用户、租户、机构等上下文，应通过稳定上下文对象或现有 Holder 获取，避免散落读取 request。
- 密码、令牌、密钥、验证码等敏感信息不得写入日志或页面隐藏字段。

## Configuration Rules

- 应用配置固定放在各模块 `src/main/resources/config`。
- 不提交环境私有密钥、账号、密码和生产连接串。
- 可变环境配置通过 profile、部署参数或外部配置覆盖。
- 公共默认配置应保持对本地开发友好。

## Implementation Default

- 先保证三层边界正确，再写代码。
- 先复用现有模块和包结构，再新增目录。
- 先把共享业务规则放到 `sandwish-biz`，再考虑入口模块专用适配。
- 先保证数据库是真相源，再考虑缓存和本地临时状态。
- 先保留旧项目可运行性，再做结构治理。
- 文档、代码、测试和提交记录必须保持同一套项目口径。

## Open Items

- 是否统一项目展示名为 `sandwich`，还是继续沿用 Maven artifact `sandwish`。
- 是否保留 jar finalName 中的 `interaction-admin-api`、`hudong` 等历史命名。
- 是否补充后台、前台、业务模块的详细目录规范。
- 是否为现有业务域建立 `10-requirements/` 和 `20-database/` 文档。
- 是否引入轻量架构测试或 Maven 检查来守住模块依赖方向。
