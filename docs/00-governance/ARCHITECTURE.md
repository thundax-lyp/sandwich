# Sandwich 系统架构

本文件只保留架构决策和实现红线。已经拆分到专项治理文档的规则，在本文只保留入口链接和最高层边界。

相关文档：

- 架构意图、小步提交和决策记忆见 [`ARCHITECTURE-INTENT.md`](./ARCHITECTURE-INTENT.md)
- 新增类、改类名、改目录、判断模块归属见 [`NAMING-AND-PLACEMENT-RULES.md`](./NAMING-AND-PLACEMENT-RULES.md)
- 数据库、实体、DAO、Mapper 和持久化查询见 [`DATABASE-RULES.md`](./DATABASE-RULES.md)
- 领域标识、数据库主键和业务编号边界见 [`UNIFIED-ID-DESIGN.md`](./UNIFIED-ID-DESIGN.md)
- HTTP API 注解矩阵见 [`API-ANNOTATION-MATRIX.md`](./API-ANNOTATION-MATRIX.md)
- 当前用户、会员和线程上下文透传见 [`CONTEXT-PROPAGATION-RULES.md`](./CONTEXT-PROPAGATION-RULES.md)
- 文档写作与维护见 [`DOCUMENT-RULES.md`](./DOCUMENT-RULES.md)
- 上线准备、运维和 jar 打包见 [`DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`](./DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md)

## Project Baseline

- project: Maven multi-module
- java: 8
- spring-boot: 2.0.5.RELEASE
- packaging:
  - `sandwish-common`: pom
  - `sandwish-common-core`: jar
  - `sandwish-common-web`: jar
  - `sandwish-common-test`: jar
  - `sandwish-common-cache`: jar
  - `sandwish-common-mybatis`: jar
  - `sandwish-common-security`: jar
  - `sandwish-common-swagger`: jar
  - `sandwish-common-log`: jar
  - `sandwish-common-mq`: jar
  - `sandwish-common-oss`: jar
  - `sandwish-biz`: jar
  - `sandwish-infra`: jar
  - `sandwish-admin-api`: jar
  - `sandwish-front-api`: jar
- base package: `com.github.thundax`
- persistence: MyBatis-Plus
- api docs: Swagger / Springfox
- formatter: Spotless
- rule gate: Checkstyle

## Project Identity

- 项目对外展示名固定为 `Sandwich`。
- Maven artifact、模块名、目录名和包内项目名前缀继续沿用现有 `sandwish`，不得为了拼写统一做无业务收益的大规模重命名。
- 运行 jar 的 `finalName` 固定使用入口模块名：`sandwish-admin-api`、`sandwish-front-api`。
- 部署样例、README、数据库脚本和治理文档使用 `Sandwich` 表达项目展示名，引用真实模块、路径或 artifact 时使用对应 `sandwish-*` 名称。

## Quality Tools

- `spotless` 是 formatter，只负责整理代码格式、import 和版式，不承载规约语义。
- `checkstyle` 是 rule gate，只负责检查、报错和阻断，不负责改代码，也不替代 formatter。
- 新增或调整静态规则时，先判断这是“格式整理”还是“规约约束”：前者放 `spotless`，后者放 `checkstyle`。
- 不要把同一类职责同时配到 `spotless` 和 `checkstyle`，避免重复约束、相互打架和误导后续 AI。
- Sandwich 是 Java 8 项目，质量工具版本必须优先满足 Java 8 构建运行约束；不得直接套用只支持更高 JDK 的插件或 formatter 版本。

## Architecture Shape

Sandwich 固定采用三层 API 架构。

固定主链路为：

`HTTP/API -> Controller -> Service -> DAO/Mapper -> Database`

三层职责固定为：

- Web 层：请求入口、登录态、权限、参数接收、API 响应组装。
- Service 层：业务流程、事务边界、业务校验、跨 DAO 编排。
- DAO/Mapper 层：持久化访问、SQL 映射、分页查询、数据装载。
- Domain Entity / Service 使用领域枚举和值对象表达业务状态，不直接比较状态字符串。

建模边界固定为单业务域应用形态：

- 数据归属应该通过具体业务身份表达，例如 `userId`、`departmentId`、`clientId` 或具体业务对象标识。
- 当前系统不引入租户模型；只有产品明确进入多租户 SaaS 形态时，才引入租户模型和跨层租户隔离规则。

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

- Common 聚合模块
- 管理 `sandwish-common-core`、`sandwish-common-web`、`sandwish-common-test`、`sandwish-common-cache`、`sandwish-common-mybatis`、`sandwish-common-security`、`sandwish-common-swagger`、`sandwish-common-log`、`sandwish-common-mq` 和 `sandwish-common-oss`

边界：

- 不直接承载 Java 源码。
- 不被业务模块作为 jar 依赖。

### `sandwish-common-core`

职责：

- 通用工具类
- 通用分页数据模型
- 通用编码、加密、集合、日期、文件工具
- i18n 支撑
- 存储、线程等通用技术能力

边界：

- 通用分页模型只承载分页数据，不读取 HTTP、Cookie、Session，也不输出 HTML。
- 不承载具体业务流程。
- 不依赖 `sandwish-common-mybatis`、`sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。
- 新增通用能力前，必须确认不是某个业务模块的专用逻辑。

### `sandwish-common-web`

职责：

- 通用 Web 响应模型
- 通用请求列表辅助
- 入口无关的 Web 支撑能力

边界：

- 可以依赖 `sandwish-common-core`。
- 不承载 Controller、Filter、Interceptor 或具体入口配置。
- 不承载业务请求 / 响应对象。
- Helper 只返回数据，不抛入口层业务异常。
- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

### `sandwish-common-test`

职责：

- 通用测试支撑
- 架构测试 helper
- 测试资源基线

边界：

- 只作为测试依赖使用。
- 不承载生产运行逻辑。
- 不承载业务测试用例本身。
- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

### `sandwish-common-cache`

职责：

- 通用 JetCache 基线配置
- 缓存基础设施自动配置
- 极薄通用 cache operation

边界：

- 可以依赖 `sandwish-common-core`。
- 不承载业务 cache support。
- 不承载具体业务 key、TTL、版本号、回源和失效策略。
- 不暴露 Redis API、`StringRedisTemplate`、`RedisTemplate` 或通用 Redis 客户端替代封装。
- 不依赖 `sandwish-common-mybatis`、`sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

### `sandwish-common-swagger`

职责：

- 通用 Swagger / Springfox 自动配置
- 通用 Swagger 文档属性模型
- Swagger UI 静态资源映射
- Swagger API 分组字母序排序规则

边界：

- 适配 Spring Boot 2.0.x 与 Springfox 2.x。
- 不承载业务 Controller、Request 或 Response。
- 不承载具体业务 API 注解。
- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

### `sandwish-common-log`

职责：

- 通用系统日志注解
- 通用系统日志切面
- 系统日志事件模型
- 日志投递适配

边界：

- 可以依赖 `sandwish-common-core` 和 `sandwish-common-mq`。
- 不承载具体业务日志落库实现。
- 不承载业务日志查询、展示或管理流程。
- 不访问业务 DAO、数据库或 Redis。
- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

### `sandwish-common-mq`

职责：

- 通用消息模型
- 消息发送契约
- 消息配置
- 无消息中间件环境的 no-op sender

边界：

- 可以依赖 `sandwish-common-core`。
- 不承载具体业务 topic、tag、key 和消费语义。
- 不承载业务事件建模。
- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

### `sandwish-common-oss`

职责：

- 通用对象存储契约
- OSS 自动配置
- 本地文件对象存储客户端
- S3 对象存储客户端

边界：

- 可以依赖 `sandwish-common-core`。
- 不承载 `Storage` 业务对象、对象引用关系或业务生命周期。
- 不承载业务 bucket、object key 生成规则和访问权限规则。
- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

### `sandwish-common-mybatis`

职责：

- 通用持久化基础设施
- MyBatis-Plus 基础配置
- 数据库方言类

边界：

- 可以依赖 `sandwish-common-core`。
- MyBatis-Plus 分页插件数据库类型必须从 `spring.datasource.url` 或 `spring.datasource.driver-class-name` 推断；当前只支持 MySQL 和 DM，其他类型必须启动失败，禁止另设重复数据库类型配置。
- 不承载通用 CRUD / Tree Service 公共契约、CRUD 基类或 MyBatis 扫描标记。
- 不承载通用分页数据模型。
- 不承载业务 DAO implementation、业务 Mapper XML 或业务 SQL。
- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

### `sandwish-common-security`

职责：

- 通用安全框架接入
- Spring Security 基础契约
- 通用权限匹配规则
- 不依赖业务数据的认证上下文模型

边界：

- 可以依赖 `sandwish-common-core`。
- 不承载具体登录流程。
- 不承载用户、角色、菜单装载逻辑。
- 不访问 Redis、数据库或业务 DAO。
- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

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
- `PersistenceAssembler`
- 数据库类型转换和分页查询实现
- 业务 Redis 持久化实现和业务 Redis DAO implementation

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
- 后台专用工具、Request 和 Response

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
- 前台安全、Spring Security、过滤器、拦截器等入口适配
- 前台静态 API 支撑资源
- 前台专用工具、Request 和 Response

边界：

- 可以依赖 `sandwish-biz`。
- 面向前台访问能力，不承载后台管理语义。
- 前台登录态、权限和会话适配固定在前台入口模块处理。
- 复用业务能力必须通过 `sandwish-biz`，不得复制后台业务实现。

## Dependency Direction

固定依赖方向为：

`sandwish-admin-api -> sandwish-infra -> sandwish-biz -> sandwish-common-mybatis -> sandwish-common-core`

`sandwish-front-api -> sandwish-infra -> sandwish-biz -> sandwish-common-mybatis -> sandwish-common-core`

通用 Web 支撑链路允许入口模块依赖：

`sandwish-admin-api -> sandwish-common-web -> sandwish-common-core`

`sandwish-front-api -> sandwish-common-web -> sandwish-common-core`

Spring Security 接入链路允许入口模块依赖：

`sandwish-admin-api -> sandwish-common-security -> sandwish-common-core`

`sandwish-front-api -> sandwish-common-security -> sandwish-common-core`

Swagger 文档链路允许入口模块依赖：

`sandwish-admin-api -> sandwish-common-swagger`

`sandwish-front-api -> sandwish-common-swagger`

OSS 存储链路允许 infra 和入口装配依赖：

`sandwish-infra -> sandwish-common-oss`

日志与消息链路允许入口模块依赖：

`sandwish-admin-api -> sandwish-common-log -> sandwish-common-mq -> sandwish-common-core`

测试支撑链路允许测试代码依赖：

`*-test -> sandwish-common-test`

禁止依赖方向：

- `sandwish-common` 及其子模块不得依赖任何业务或入口模块。
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
- 固定接收 API `Request` 或基础请求参数，固定输出 API `Response` 或统一 API 响应包装。
- 不直接暴露业务 `Entity` 作为公开 HTTP 响应模型。
- 不把 API `Request` / `Response` 下沉到 Service。
- 分页入口固定接收 `PageRequest` 或基础分页参数，分页出口固定输出 `PageResponse` 或统一响应包装。

### Service

- 负责业务流程、事务、状态流转、业务校验和跨 DAO 编排。
- 可以依赖 DAO / Mapper。
- 可以依赖 `sandwish-common` 的通用工具和基础服务。
- 对外提供稳定业务方法，避免让 Controller 感知过多持久化细节。
- 跨模块业务复用优先放在 `sandwish-biz` 的 Service。
- 方法入参固定使用 `*DTO`、`*Query`、业务 `Entity` 或 Java-Type。
- 方法返回结果固定使用 `*DTO`、业务 `Entity` 或 Java-Type。
- Java-Type 包含 primitive / boxed primitive、`String`、`BigDecimal`、`Date`、`Enum`、数组、`java.*` 集合容器和项目统一标识值类型。
- 分页业务数据固定使用 `PageDTO<T>`，`T` 只能是 `*DTO`、业务 `Entity` 或 Java 标准类型。
- Service 接口应该显式声明当前业务需要暴露的方法。
- Service 公开方法不得仅由测试代码调用；测试不得成为公开方法存在的唯一理由。
- 暂未接入生产调用但确属稳定业务入口的方法，必须声明 `@LayerPublicApi(reason = "...")` 并说明非测试原因。
- 不新增空 `BaseService`、空 marker Service、通用 `BaseServiceImpl` 或泛型 CRUD / Tree Service 公共契约。
- 不直接依赖 API `Request` / `Response`。
- 不直接依赖 `DO` / `DataObject`。
- 不直接暴露 MyBatis-Plus `Page`、`IPage`、`Wrapper` 或其他持久化实现类型。
- 不负责 API 响应字段裁剪、HTTP 状态语义或入口展示模型组装。

### DAO / Mapper

- 负责数据库访问和 SQL 映射。
- 查询、分页、过滤、排序优先下推到持久化层。
- DAO / Mapper 公开方法不得仅由测试代码调用；测试不得通过新增持久化公开方法绕过业务层。
- DAO / Mapper 的 `@LayerPublicApi` 使用必须更谨慎，reason 必须说明真实持久化契约或框架调用来源。
- 不承载业务流程。
- 不处理 Web 会话、权限适配和页面语义。
- SQL 变化必须同步检查实体、DAO implementation、Mapper、Service 调用和数据库文档。
- DAO interface 固定归属 `sandwish-biz`。
- DAO implementation、MyBatis Mapper 和 `DO` / `DataObject` 固定归属 `sandwish-infra`。
- `PersistenceAssembler` 固定归属 `sandwish-infra`，只负责 `Entity <-> DO/DataObject` 转换。
- DAO / Mapper 方法优先使用显式业务语义命名，不以通用 `findList(T entity)` 或无条件语义方法承载新增查询。
- Mapper interface 保持最小 `BaseMapper<DO>` 定义，业务查询逻辑固定在 DAO implementation 中。
- DAO interface 方法入参固定使用业务 `Entity` 或 Java 标准类型。
- DAO interface 方法返回值固定使用业务 `Entity`、Java 标准类型或 MyBatis-Plus `Page<Entity>`。
- DAO 分页入参固定使用 `int pageNo, int pageSize`，分页返回固定使用 MyBatis-Plus `Page<Entity>`。
- DAO interface 不接收或返回 `*DTO`、API `Request` / `Response`、`DO` / `DataObject` 或 common `PageDTO`。
- Redis DAO 属于 infra 持久化实现；Redis 持久化不要求新增 MyBatis Mapper。
- 树结构的 `lft` / `rgt` 属于 nested-set 持久化索引，只允许存在于 `DO/DataObject`、Mapper 和 infra DAO implementation 中。
- 当测试为生产 DAO implementation 提供 InMemory implementation 时，InMemory implementation 固定放在 `src/test/java` 并标记 `@Profile("test")`；对应生产 DAO implementation 必须标记 `@Profile("!test")`，防止测试上下文误加载生产实现。

### Entity / DTO

- Entity 优先表达持久化对象或业务数据对象。
- DTO 用于 Service 边界的数据传输。
- 不在 DTO 中写复杂业务流程。
- 不强制引入值对象、聚合根等非当前架构必需概念。
- `*Query` 固定作为 Service 入参读取条件模型。
- Service `add` 方法必须返回新建主实体的 `EntityId`，不得依赖入参回填副作用表达创建结果。
- `*Query` 类级注解必须且只能包含 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor`。
- `DO` / `DataObject` 不承载业务 `query` 对象，不定义 `Query` 内部类，不作为 Service 查询模型传递。
- `PersistenceAssembler` 不回填查询对象；查询条件从 Service 到 DAO / Mapper 时必须显式拆解或转换为 infra 内部 persistence 参数对象。
- 树业务 `Entity` 只表达 `parentId` 等业务关系字段，不暴露 `lft` / `rgt` 或 nested-set 区间计算方法。

### Request / Response / InterfaceAssembler

- `Request` 固定表达 API 入参，归属对应 API 入口模块。
- `Response` 固定表达 API 出参，归属对应 API 入口模块。
- `InterfaceAssembler` 固定归属对应 API 入口模块，命名以 `InterfaceAssembler` 结尾。
- `InterfaceAssembler` 只负责 API `Request` / `Response` 与 Service `Entity` / 稳定业务参数 / 业务结果之间的转换。
- `InterfaceAssembler` 不调用 Service、DAO 或 Mapper。
- `InterfaceAssembler` 不处理事务、权限、数据库查询或核心业务规则。
- `InterfaceAssembler` 不转换 `DO` / `DataObject`。
- Service 与 DAO/Mapper 不依赖 `InterfaceAssembler`。

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
- Service 如需当前用户、部门等上下文，应通过稳定上下文对象或现有 Holder 获取，避免散落读取 request。
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
- 先保证当前应用可运行，再做结构治理。
- 文档、代码、测试和提交记录必须保持同一套项目口径。

## Governance Coverage

- 后台、前台、业务模块的详细目录、命名和层次规则固定由 [`NAMING-AND-PLACEMENT-RULES.md`](./NAMING-AND-PLACEMENT-RULES.md) 承载，本文不重复展开。
- 当前已建立需求文档和数据库设计文档的业务域固定为 `system`、`auth`、`storage`、`member`。
- 修改 `system`、`auth`、`storage`、`member` 的需求、数据库、SQL、持久化对象或接口时，必须同步检查对应 `10-requirements/` 与 `20-database/` 文档。
- 修改尚未建立需求和数据库设计文档的业务域时，若变更会扩大领域模型、表结构、接口契约或部署边界，必须先补对应需求和数据库设计文档。
- 轻量架构测试已经作为治理门禁存在于各模块 `src/test/java/com/github/thundax/architecture/`；新增可机械校验的架构红线时，必须补充或扩展对应架构测试。

## Open Items

无
