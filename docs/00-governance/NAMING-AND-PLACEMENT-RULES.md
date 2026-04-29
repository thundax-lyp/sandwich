# 命名与目录规则

## Purpose

本文件只回答三个问题：

1. 该建什么类型的类
2. 该放哪一层、哪个目录
3. 该叫什么名字

本文件采用二维结构：按 `Path/Layer/Naming` 组织规则，并按 `Hard Rules（可门禁）` 与 `Review Rules（AI/人工审阅）` 分区。
新增规则必须先完成分类归位：先判定 `Hard/Review`，再归入 `Path/Layer/Naming`，禁止新增未分类规则。

## Scope

当前范围：

- Maven 模块归属
- Java 类、接口、实现、持久化对象、装配器命名
- Controller / Service / DAO / Mapper 层归属
- API 模型、业务对象和持久化对象的放置边界
- 静态 API 支撑资源放置边界

不在范围内：

- 不定义字段级数据库命名规则，字段和索引规则见 [`DATABASE-RULES.md`](./DATABASE-RULES.md)
- 不定义部署入口和流量边界，部署规则见 [`DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`](./DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md)
- 不替代业务需求文档

## Fast Choice

- HTTP / 页面入口：`Controller`
- 业务流程入口：`Service`
- 持久化访问：`DAO` / `Mapper`
- 持久化对象：`Entity`
- 持久化实现对象：`DO` / `DataObject`
- API 请求对象：`Request`
- API 响应对象：`Response`
- 页面展示对象：`VO`
- 接口传输对象：`DTO`
- API 模型装配器：`InterfaceAssembler`
- 通用技术能力：`Utils` / `Helper`
- 静态资源：放在所属 API 模块静态资源目录

## Hard Rules（可门禁，必须稳定）

### Path

- `PATH_ADMIN_API_OWNERSHIP`：后台接口入口和后台 API 支撑静态资源固定归属 `sandwish-admin-api`
- `PATH_FRONT_API_OWNERSHIP`：前台接口入口和前台 API 支撑静态资源固定归属 `sandwish-front-api`
- `PATH_SHARED_BUSINESS_OWNERSHIP`：前后台共享业务规则固定归属 `sandwish-biz`
- `PATH_INFRA_PERSISTENCE_OWNERSHIP`：DAO implementation、MyBatis Mapper、`DO/DataObject`、`PersistenceAssembler` 固定归属 `sandwish-infra`
- `PATH_DATA_OBJECT_INFRA_ONLY`：生产代码中 `DO/DataObject` 只能在 `sandwish-infra` 定义和引用，其他模块不得定义、导入、作为字段、参数、返回值或泛型使用。
- `PATH_COMMON_NO_BUSINESS`：无业务语义的通用能力才允许进入 `sandwish-common`
- `PATH_INTERFACE_ASSEMBLER_API_OWNERSHIP`：`InterfaceAssembler` 固定归属对应 API 入口模块，不进入 `sandwish-biz` 或 `sandwish-infra`
- `PATH_REQUEST_RESPONSE_API_OWNERSHIP`：API `Request` / `Response` 固定归属对应 API 入口模块，不进入 `sandwish-biz`、`sandwish-infra` 或 `sandwish-common`

### Layer

- `LAYER_CONTROLLER_TO_SERVICE`：Controller 可以调用 Service，不直接访问 DAO / Mapper
- `LAYER_SERVICE_TRANSACTION`：事务边界默认放在 Service
- `LAYER_CONTROLLER_REQUEST_RESPONSE`：Controller 固定接收 `Request` 并输出 `Response` / API 响应包装，不把入口模型下沉到 Service
- `LAYER_SERVICE_ENTITY_MODEL`：Service 固定使用 Entity 或稳定业务参数，不直接依赖 API `Request` / `Response`
- `LAYER_ENTITY_NO_API_RESPONSE`：业务 Entity 不作为公开 HTTP 响应模型直接暴露
- `LAYER_INTERFACE_ASSEMBLER_PURE_CONVERSION`：`InterfaceAssembler` 只负责 API 模型与 Service `Entity` / 稳定业务参数 / 业务结果之间的转换，不调用 Service、DAO 或 Mapper，不处理事务、权限、数据库查询或核心业务规则
- `LAYER_INTERFACE_ASSEMBLER_NO_DO`：`InterfaceAssembler` 不转换 `DO` / `DataObject`
- `LAYER_DAO_NO_WEB`：DAO / Mapper 不感知 HTTP、Session 和权限适配
- `LAYER_DATA_OBJECT_CREATION_ASSEMBLER_ONLY`：生产代码中 `new DO/DataObject` 只能出现在 `PersistenceAssembler` 中，DAO implementation 不直接构造 `DO/DataObject`。
- `LAYER_NO_SERVER_PAGE`：不得新增服务端页面模板、页面装饰器或标签库作为业务入口
- `LAYER_NO_EXTRA_ARCH_DEFAULT`：不得默认新增 `interfaces / application / domain / facade / repository` 等额外分层目录
- `LAYER_INFRA_NO_BUSINESS_FLOW`：`sandwish-infra` 不承载业务流程，不暴露 HTTP 模型，不让 Controller 直接调用

### Naming & Placement

- `NAME_CONTROLLER`：Controller 命名以 `Controller` 结尾
- `NAME_SERVICE`：Service 命名以 `Service` 结尾
- `NAME_SERVICE_IMPL`：Service 实现命名以 `ServiceImpl` 结尾
- `NAME_DAO`：DAO 命名以 `Dao` 或 `DAO` 结尾，按现有包风格保持一致
- `NAME_DAO_IMPL`：DAO 实现命名以 `DaoImpl` 或 `DAOImpl` 结尾，按现有包风格保持一致
- `NAME_MAPPER`：Mapper 命名以 `Mapper` 结尾
- `NAME_ENTITY`：Entity 命名表达业务对象，不使用无意义泛化名称
- `NAME_DATA_OBJECT`：持久化对象命名以 `DO` 或 `DataObject` 结尾
- `NAME_DATA_OBJECT_REQUIRED_ANNOTATIONS`：`DO/DataObject` 固定使用 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor` 和 `@TableName`；这些注解属于持久化实现对象的允许注解
- `NAME_DATA_OBJECT_QUERY_FIELD`：`DO/DataObject` 中用于持久化查询的字段必须显式命名，不使用通用 `query`
- `NAME_DAO_MAPPER_METHOD`：DAO / Mapper 查询方法名必须表达动作和条件，不新增无条件语义的通用查询方法
- `NAME_PERSISTENCE_ASSEMBLER`：持久化装配器命名以 `PersistenceAssembler` 结尾
- `NAME_INTERFACE_ASSEMBLER`：API 模型装配器命名以 `InterfaceAssembler` 结尾
- `NAME_REQUEST_RESPONSE`：API 请求和响应对象命名以 `Request`、`Response` 结尾
- `NAME_REQUEST_REQUIRED_ANNOTATIONS`：API `Request` 类级注解有且仅有 `@Getter`、`@Setter`、`@ApiModel`、`@JsonInclude(JsonInclude.Include.NON_NULL)` 和 `@JsonIgnoreProperties(ignoreUnknown = true)`
- `NAME_RESPONSE_REQUIRED_ANNOTATIONS`：API `Response` 类级注解有且仅有 `@Getter`、`@Setter`、`@ApiModel`、`@JsonInclude(JsonInclude.Include.NON_NULL)` 和 `@JsonIgnoreProperties(ignoreUnknown = true)`
- `NAME_VO_DTO`：VO / DTO 命名必须表达使用场景或业务对象

## Review Rules（AI/人工审阅，暂不强门禁）

### Path

- 同一业务对象的 Controller、Service、DAO interface、Entity 和 API 支撑资源应按固定层归属放置，并保持业务模块路径一致
- 后台专用入口不放到 `sandwish-front-api`
- 前台专用入口不放到 `sandwish-admin-api`
- 前后台复用业务不复制到两个 API 入口模块
- `sandwish-infra` 下按 `com.github.thundax.modules.{module}.persistence.{dataobject,assembler,mapper,dao}` 组织持久化实现
- `persistence` 包段固定保留，用于区分业务侧 DAO interface 与 infra 侧持久化实现

### Layer

- Controller 优先完成参数接收、基础校验和响应组装
- Service 优先表达业务动作，避免让 Controller 感知过多持久化细节
- `InterfaceAssembler` 按对应 API 入口模块的现有包结构放置，优先使用 `assembler` 包
- DAO / Mapper 查询、分页、过滤、排序优先下推到数据库
- VO / DTO 不写复杂业务流程
- `PersistenceAssembler` 只做 `Entity <-> DO/DataObject` 字段转换，查询条件从业务 `Entity.Query` 到持久化参数的拆解不回填到 `DO`

### Naming & Placement

- 命名应表达职责与层次，避免泛化命名
- 新增目录前先确认现有目录无法承载
- 历史包名与现有模块风格冲突时，优先保持当前模块内部一致
- `DO/DataObject` 显式查询字段按对应业务字段命名，必要时使用 `query` 前缀区分非表字段
- DAO / Mapper 方法使用 `getBy...`、`listBy...`、`pageBy...`、`countBy...`、`existsBy...` 表达查询语义

## Open Items

- 是否统一 `Dao` / `DAO` 后缀风格。
- 是否进一步按业务域固定包结构。
