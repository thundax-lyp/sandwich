# 命名与目录规则

## Purpose

本文件只回答三个问题：

1. 该建什么类型的类
2. 该放哪一层、哪个目录
3. 该叫什么名字

本文件采用二维结构：按 `Path/Layer/Naming` 组织规则，并按 `Hard Rules（门禁）` 与 `Review Rules（AI/人工审阅）` 分区。
新增规则必须先完成分类归位：先判定 `Hard/Review`，再归入 `Path/Layer/Naming`，禁止新增未分类规则。
`Hard Rules` 必须存在 ArchUnit 或 Checkstyle 门禁；无法稳定门禁的语义判断固定放入 `Review Rules`。

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
- 接口传输对象：`DTO`
- Service 查询对象：`XxxQuery`
- Service 写入口对象：`XxxCommand`
- Service 分页输入对象：`PageQuery`
- Service 分页返回对象：`PageResult`
- API 模型装配器：`InterfaceAssembler`
- 通用技术能力：`Utils` / `Helper`
- 静态资源：放在所属 API 模块静态资源目录

## Hard Rules（门禁规则，必须稳定）

### Path

- `PATH_INFRA_PERSISTENCE_OWNERSHIP`：DAO implementation、MyBatis Mapper、`DO/DataObject`、`PersistenceAssembler` 固定归属 `sandwish-infra/src/main/java/com/github/thundax/modules/{module}/persistence/`，DAO implementation 放 `persistence/dao`，MyBatis Mapper 放 `persistence/mapper`，`DO/DataObject` 放 `persistence/dataobject`，`PersistenceAssembler` 放 `persistence/assembler`。
- `PATH_DATA_OBJECT_INFRA_ONLY`：生产代码中 `DO/DataObject` 只能在 `sandwish-infra` 定义和引用，其他模块不得定义、导入、作为字段、参数、返回值或泛型使用。
- `PATH_BIZ_MODULE_PACKAGE`：业务域代码固定按 `sandwish-biz/src/main/java/com/github/thundax/modules/{module}/` 组织，领域对象放 `entity`，领域枚举放 `entity/enums`，领域值对象放 `entity/valueobject`，DAO interface 放 `dao`，Service interface 放 `service`，Service implementation 放 `service/impl`，Service 查询对象放 `service/query`，Service 写入口对象放 `service/command`。
- `PATH_API_MODULE_PACKAGE`：API 入口代码固定按 `{api-module}/src/main/java/com/github/thundax/modules/{module}/` 组织，Controller 放 `controller`，API 请求对象放 `controller/request`，API 响应对象放 `controller/response`，`InterfaceAssembler` 放 `assembler`。
- `PATH_FRONT_AUTH_MODULE_PACKAGE`：`sandwish-front-api` 中会员登录、注册、token、认证过滤器和认证上下文固定归属 `com.github.thundax.modules.auth`；`com.github.thundax.modules.member` 在前台入口只用于非认证类会员业务接口。
- `PATH_INTERFACE_ASSEMBLER_API_OWNERSHIP`：`InterfaceAssembler` 固定归属对应 API 入口模块，不进入 `sandwish-biz` 或 `sandwish-infra`
- `PATH_REQUEST_RESPONSE_API_OWNERSHIP`：API `Request` / `Response` 固定归属对应 API 入口模块，并下沉到对应业务模块的 `controller/request` 与 `controller/response` 包；不进入 `sandwish-biz`、`sandwish-infra` 或 `sandwish-common`
- `PATH_SERVICE_QUERY_BIZ_OWNERSHIP`：Service 查询对象固定归属 `sandwish-biz/src/main/java/com/github/thundax/modules/{module}/service/query/`，不进入 API、Entity、DAO、infra 或 common 包。
- `PATH_SERVICE_COMMAND_BIZ_OWNERSHIP`：Service 写入口对象固定归属 `sandwish-biz/src/main/java/com/github/thundax/modules/{module}/service/command/`，不进入 API、Entity、DAO、infra 或 common 包。
- `PATH_COMMON_PAGE_MODEL`：`PageQuery` 和 `PageResult` 固定归属 `sandwish-common-core` 的 `com.github.thundax.common.page` 包。
- `PATH_ENTRY_SERVICE_API_ONLY`：入口注册编排 Service 固定归属 API 入口模块；前台会员注册编排命名为 `MemberRegistrationService` / `MemberRegistrationServiceImpl`；后台权限会话适配 Service 固定归属 `sandwish-admin-api` 的 `auth.service`；可复用认证业务 Service 可以归属 `sandwish-biz` 的 `auth.service`，但不得依赖 API Request / Response、Servlet、安全框架上下文或入口专用 provider；`sandwish-biz` 不得声明 `*RegistrationService`、`PermissionService` 或对应 `*ServiceImpl`。

### Layer

- `LAYER_CONTROLLER_TO_SERVICE`：Controller 可以调用 Service，不直接访问 DAO / Mapper
- `LAYER_CONTROLLER_REQUEST_RESPONSE`：Controller 固定接收 `Request` 并输出 `Response` / API 响应包装；入口模型放在同业务模块的 `controller/request` 与 `controller/response` 包，不下沉到 Service
- `LAYER_SERVICE_BOUNDARY_TYPES`：Service 方法入参固定为 `*Query`、`*Query + PageQuery` 或 `*Command` 三种形态；方法参数最多 2 个；不得接收或返回 API `Request` / `Response`、`DO/DataObject`、MyBatis-Plus `Page/IPage/Wrapper` 或其他持久化实现类型。
- `LAYER_SERVICE_PAGE_RESULT`：Service 分页返回结果固定使用 `PageResult<T>`，`T` 只能是 `*DTO`、业务 `Entity` 或 Java 标准类型；`PageResult` 不作为 Service 方法入参。
- `LAYER_SERVICE_WRITE_COMMAND`：Service 写入口固定接收一个 `*Command`，不接收业务 `Entity`、散落业务字段或 API `Request`。
- `LAYER_SERVICE_NO_EMPTY_BASE`：不得新增空 `BaseService`、空 marker Service 或通用 `BaseServiceImpl`；Service 共性能力必须有明确方法契约或具体业务价值。
- `LAYER_DAO_BOUNDARY_TYPES`：DAO interface 方法入参固定使用业务 `Entity` 或 Java 标准类型；返回值固定使用业务 `Entity`、Java 标准类型或 MyBatis-Plus `Page<Entity>`；不得接收或返回 `*DTO`、API `Request` / `Response`、`DO/DataObject`、common `PageQuery` 或 common `PageResult`。
- `LAYER_SERVICE_QUERY_MODEL`：Service 读取条件使用 `XxxQuery` 表达时，`XxxQuery` 固定作为 Service 输入模型，只承载读取过滤条件，不承载 HTTP、Session、权限适配、分页状态、持久化实现类型或 request 字符串解析逻辑。
- `LAYER_SERVICE_QUERY_NO_SETTER_LOGIC`：`XxxQuery` 源码不得声明手写 `setXxx` 方法；JDK8 下使用 class 承载字段定义，request 到 query 的枚举解析、日期归一化和字段装配固定放在对应 `InterfaceAssembler`。
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
- `NAME_API_SERVICE_ENTRY_ONLY`：`sandwish-admin-api` 和 `sandwish-front-api` 中只有入口认证适配、注册编排和后台权限会话适配类型允许使用 `Service` / `ServiceImpl` 后缀，且名称必须以 `AuthService`、`RegistrationService`、`PermissionService` 或对应 `ServiceImpl` 结尾；其他业务 Service 固定归属 `sandwish-biz`。
- `NAME_DAO`：DAO interface 命名固定以 `Dao` 结尾
- `NAME_DAO_IMPL`：DAO implementation 命名固定以 `DaoImpl` 结尾
- `NAME_MAPPER`：Mapper 命名以 `Mapper` 结尾
- `NAME_DATA_OBJECT`：持久化对象命名以 `DO` 或 `DataObject` 结尾
- `NAME_DATA_OBJECT_REQUIRED_ANNOTATIONS`：`DO/DataObject` 固定使用 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor` 和 `@TableName`；这些注解属于持久化实现对象的允许注解
- `NAME_DATA_OBJECT_QUERY_FIELD`：`DO/DataObject` 中用于持久化查询的字段必须显式命名，不使用通用 `query`
- `NAME_DAO_METHOD_SHAPE`：DAO interface 方法名固定使用持久化端口口径：按 ID 读取应该使用 `getById`，按 ID 批量读取应该使用 `listByIds`，按业务唯一键读取应该使用 `getByXxx`，列表查询应该使用 `list(...)`，分页查询应该使用 `page(..., pageNo, pageSize)`，计数应该使用 `count(...)`，按 ID 删除应该使用 `deleteById`，批量动作应该使用 `batchXxx`
- `NAME_PERSISTENCE_ASSEMBLER`：持久化装配器命名以 `PersistenceAssembler` 结尾
- `NAME_INTERFACE_ASSEMBLER`：API 模型装配器命名以 `InterfaceAssembler` 结尾
- `NAME_HELPER_NO_ARCH_SUFFIX`：通用工具类或 Helper 不得使用 `Mapper`、`Converter`、`Assembler`、`DAO`、`Service`、`Controller`、`Repository`、`Facade`、`Gateway`、`Adapter`、`Client`、`Handler`、`Processor`、`Manager`、`Factory` 等架构角色后缀；这些后缀只能用于对应分层或明确架构职责的类型。
- `NAME_HELPER_BOUNDARY_REQUIRED`：Helper 命名必须绑定明确对象、容器或入口场景，例如 `RequestListHelper`、`TreeNodeListHelper`、`PageResponseHelper`；不得新增 `ListHelper`、`ObjectHelper`、`DataHelper`、`CommonHelper`、`BaseHelper`、`GenericHelper` 等无边界通用 Helper。
- `NAME_REQUEST_RESPONSE`：API 请求和响应对象命名以 `Request`、`Response` 结尾，分别放在对应 API 模块的 `modules/{module}/controller/request` 与 `modules/{module}/controller/response` 包
- `NAME_REQUEST_REQUIRED_ANNOTATIONS`：API `Request` 类级注解有且仅有 `@Getter`、`@Setter`、`@ApiModel`、`@JsonInclude(JsonInclude.Include.NON_NULL)` 和 `@JsonIgnoreProperties(ignoreUnknown = true)`
- `NAME_RESPONSE_REQUIRED_ANNOTATIONS`：API `Response` 类级注解有且仅有 `@Getter`、`@Builder`、`@ApiModel`、`@JsonInclude(JsonInclude.Include.NON_NULL)` 和 `@JsonIgnoreProperties(ignoreUnknown = true)`
- `NAME_DECLARATION_ORDER`：类成员固定按 Checkstyle `DeclarationOrder` 排列；类级静态常量和静态字段放在实例字段前，同类成员按可见性顺序排列。
- `NAME_DTO`：Service 边界传输对象必须以 `DTO` 结尾。
- `NAME_SERVICE_QUERY`：Service 查询对象命名固定为 `{业务对象名}Query`，例如 `UserQuery`、`StorageQuery`；不得使用 API `Request`、`Param`、`Condition` 或泛化 `Query` 类替代。
- `NAME_SERVICE_COMMAND`：Service 写入口对象命名固定为 `{业务动作}{业务对象}Command` 或 `{业务动作}Command`，例如 `CreateDictCommand`、`RenameRoleCommand`、`BindRoleMenusCommand`；不得使用 API `Request`、`Param`、`DTO` 或业务 `Entity` 替代。
- `NAME_SERVICE_METHOD_BUSINESS_ACTION`：Service 写方法固定使用业务动作名，不使用 `update*`、`save*`、`insert*`、`batch*` 等泛化或过时命名；条件清理动作允许使用 `deleteByXxx(*Query)` 窄口径。

## Review Rules（AI/人工审阅，暂不强门禁）

### Path

- 同一业务对象的 Controller、Service、DAO interface、Entity 和 API 支撑资源应按固定层归属放置，并保持业务模块路径一致
- 后台接口入口和后台 API 支撑静态资源归属 `sandwish-admin-api`
- 前台接口入口和前台 API 支撑静态资源归属 `sandwish-front-api`
- 前后台共享业务规则归属 `sandwish-biz`
- 同一业务模块的 Controller 保持在 `controller` 包；不要再按 action 或资源名拆成 `controller/{action}` 子包
- 后台专用入口不放到 `sandwish-front-api`
- 前台专用入口不放到 `sandwish-admin-api`
- 前后台复用业务不复制到两个 API 入口模块
- 除入口专用认证适配、注册编排和后台权限会话适配 Service 外，其他 Service interface 和 Service implementation 应归属 `sandwish-biz`；可复用认证业务 Service 优先归属 `sandwish-biz`，后台权限会话适配命名为 `PermissionService`，前台会员注册编排命名为 `MemberRegistrationService`
- 无业务语义的通用能力才进入 `sandwish-common`
- `persistence` 包段固定保留，用于区分业务侧 DAO interface 与 infra 侧持久化实现

### Layer

- Controller 优先完成参数接收、基础校验和响应组装
- 事务边界默认放在 Service
- Service 优先表达业务动作，避免让 Controller 感知过多持久化细节
- 业务 Entity 不作为公开 HTTP 响应模型直接暴露
- `InterfaceAssembler` 按对应 API 入口模块的现有包结构放置，优先使用 `assembler` 包
- DAO / Mapper 查询、分页、过滤、排序优先下推到数据库
- DTO 不写复杂业务流程
- `PersistenceAssembler` 只做 `Entity <-> DO/DataObject` 字段转换，查询条件从 Service DTO 到持久化参数的拆解不回填到 `DO`
- `CacheDTO` 默认只保存当前缓存对象自己的字段和关联对象 ID，例如 `departmentId`、`roleIds`、`parentId`；不默认嵌套 `DepartmentCacheDTO`、`RoleCacheDTO` 或其他关联对象快照。
- `CacheDTO` 需要嵌套关联对象时，必须先确认读取收益、陈旧数据接受范围和缓存失效规则，不把普通对象图直接塞进远端缓存载体。

### Naming & Placement

- 命名应表达职责与层次，避免泛化命名
- Entity 命名表达业务对象，不使用无意义泛化名称
- DTO 命名表达使用场景或业务对象
- 业务枚举常量使用 `UPPER_SNAKE_CASE` 业务语义名，简单枚举统一实现 `value() -> name()` 和大小写不敏感的 `from(String value)`；解析失败统一抛 `BizException`，不返回 `null`，不静默 fallback 到默认值。
- 新增目录前先确认现有目录无法承载
- 包名与现有模块风格冲突时，优先保持当前模块内部一致
- `DO/DataObject` 显式查询字段按对应业务字段命名，必要时使用 `query` 前缀区分非表字段
- DAO interface 命名应保持“DAO 端口”而非 Service 流程语义；优先用 `getById/getByXxx/list/listByIds/page/count/deleteById/batchXxx` 表达持久化访问形状
- DAO interface 的 `list/page/count` 条件参数顺序必须一致，`pageNo/pageSize` 固定放在分页方法参数末尾
- DAO interface 参数名优先使用 `status`、`visibility`、`privilege`、`ownerType` 等业务名
- Service 方法优先表达业务动作，不为贴合 CRUD 而弱化业务语义；例如 `verifySign/removeBusiness/changeStatus` 优先保留业务动词

## Open Items

无
