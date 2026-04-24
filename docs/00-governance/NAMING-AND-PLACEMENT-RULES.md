# 命名与目录规则

本文件只回答三个问题：

1. 该建什么类型的类
2. 该放哪一层、哪个目录
3. 该叫什么名字

本文件采用二维结构：按 `Path/Layer/Naming` 组织规则，并按 `Hard Rules（可门禁）` 与 `Review Rules（AI/人工审阅）` 分区。
新增规则必须先完成分类归位：先判定 `Hard/Review`，再归入 `Path/Layer/Naming`，禁止新增未分类规则。

## Fast Choice

- HTTP / 页面入口：`Controller`
- 业务流程入口：`Service`
- 持久化访问：`DAO` / `Mapper`
- 持久化对象：`Entity`
- 持久化实现对象：`DO` / `DataObject`
- 页面展示对象：`VO`
- 接口传输对象：`DTO`
- 通用技术能力：`Utils` / `Helper`
- 静态资源：放在所属 API 模块静态资源目录

## Hard Rules（可门禁，必须稳定）

### Path

- `PATH_ADMIN_API_OWNERSHIP`：后台接口入口和后台 API 支撑静态资源固定归属 `sandwish-admin-api`
- `PATH_FRONT_API_OWNERSHIP`：前台接口入口和前台 API 支撑静态资源固定归属 `sandwish-front-api`
- `PATH_SHARED_BUSINESS_OWNERSHIP`：前后台共享业务规则固定归属 `sandwish-biz`
- `PATH_INFRA_PERSISTENCE_OWNERSHIP`：DAO implementation、MyBatis Mapper、Mapper XML、`DO/DataObject`、`PersistenceAssembler` 固定归属 `sandwish-infra`
- `PATH_COMMON_NO_BUSINESS`：无业务语义的通用能力才允许进入 `sandwish-common`

### Layer

- `LAYER_CONTROLLER_TO_SERVICE`：Controller 可以调用 Service，不直接访问 DAO / Mapper
- `LAYER_SERVICE_TRANSACTION`：事务边界默认放在 Service
- `LAYER_DAO_NO_WEB`：DAO / Mapper 不感知 HTTP、Session 和权限适配
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
- `NAME_PERSISTENCE_ASSEMBLER`：持久化装配器命名以 `PersistenceAssembler` 结尾
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
- DAO / Mapper 查询、分页、过滤、排序优先下推到数据库
- VO / DTO 不写复杂业务流程

### Naming & Placement

- 命名应表达职责与层次，避免泛化命名
- 新增目录前先确认现有目录无法承载
- 历史包名与现有模块风格冲突时，优先保持当前模块内部一致

## Open Items

- 是否统一 `Dao` / `DAO` 后缀风格。
- 是否进一步按业务域固定包结构。
