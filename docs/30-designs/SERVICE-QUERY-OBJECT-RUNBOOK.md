# Service Query Object Migration Runbook

## 1. Purpose

本文档定义 Sandwich 服务层查询参数对象迁移的阶段性执行边界。

目标是把 `service.list/page(Entity entity)` 中用于查询的 `Entity.Query` 临时条件，迁移为显式 `XxxQuery` 服务层查询对象。迁移后 Controller 负责把 API `Request` 转成业务查询对象，Service 接收稳定业务参数，Entity 不再承载只服务查询的临时状态。

本文档是阶段性 RUNBOOK，迁移完成并完成现场清理后从 `docs/30-designs/` 删除。

## 2. Scope

当前范围：

- `sandwish-biz` 中 `list/page` 读取类 Service 方法的查询参数显式化。
- `sandwish-admin-api` / `sandwish-front-api` 中 Controller 到 Service 的查询参数装配。
- `Dict`、`Menu`、`User`、`Role`、`Office`、`Log`、`Storage`、`Member`、`Signature` 相关读取链路。
- `Entity.Query`、`getQuery()`、`setQuery()` 在对应 domain 内的逐步删除。
- `TODO.md` 按 domain 拆解迁移任务。

不在范围内：

- 不拆 `XxxService` 为 `XxxCommandService` / `XxxQueryService`。
- 不把 API `Request` / `Response` 下沉到 `sandwish-biz`。
- 不改变 DAO 命名规则、Mapper 规则或持久化实现方式。
- 不迁移写入命令对象；`Command` 后续单独讨论。
- 不为了迁移 Query 顺手重构无关接口。
- 不在第一轮改造 `CrudService<T>`、`TreeService<T>` 等公共基类。

## 3. Final State

迁移完成后固定满足：

- 查询对象命名为 `XxxQuery`。
- 查询对象归属 `sandwish-biz/src/main/java/com/github/thundax/modules/{domain}/service/query/`。
- `XxxQuery` 只表达 Service 读取条件，不包含 HTTP、Session、权限适配或分页状态。
- API `Request` 仍归属对应 API 模块。
- Controller 负责 `Request -> XxxQuery` 的装配。
- Service interface 的 `list/page` 接收 `XxxQuery` 或无查询参数，不接收只为查询创建的空 Entity。
- ServiceImpl 从 `XxxQuery` 读取条件，并保持原 DAO 调用语义。
- DAO interface 可以在本轮继续使用当前展开参数形态。
- Entity 不再保留当前 domain 已废弃的 `Query` 内部类、`getQuery()`、`setQuery()`。
- `rg "setQuery|getQuery|new .*\\.Query" sandwish-admin-api/src/main/java sandwish-front-api/src/main/java sandwish-biz/src/main/java` 不再命中已完成 domain。
- 本 RUNBOOK 和已完成 TODO 在最终清理提交中删除。

## 4. Boundary Rules

- Controller 是 HTTP 入口，接收 `Request`，输出 `Response` / API 响应包装。
- Service 是业务流程和事务边界，接收 Entity、稳定业务参数或 `XxxQuery`。
- `XxxQuery` 是业务侧服务输入模型，不是 API 模型。
- `XxxQuery` 不进入 `sandwish-common`，不进入 `sandwish-infra`。
- `XxxQuery` 不继承 Entity，不持有 Entity 作为查询条件容器。
- DAO 不感知 API `Request`，不感知 Controller 装配细节。
- 分页仍使用现有 `com.github.thundax.common.persistence.Page` 对外模型；本轮只替换查询条件对象。

## 5. Task Shape

任务按 domain 垂直闭环执行。

一个 domain 闭环包含：

`API Request -> Controller -> XxxQuery -> Service interface -> ServiceImpl -> DAO parameters -> Entity.Query cleanup -> tests -> TODO cleanup -> commit`

固定规则：

- 一个 TODO 只迁移一个 domain 或一个不可拆的强相关对象。
- 一个 domain 一个闭环提交。
- 不用“迁移 sys 模块”替代 `Dict/Menu/User/Role/Office/Log` 这种对象级任务。
- 不跨 domain 顺手删除 `Entity.Query`。
- 公共收尾任务放在所有 domain 迁移完成后执行。

## 6. Query Object Rules

`XxxQuery` 固定形态：

- 类名使用业务对象名加 `Query`。
- 包名使用 `com.github.thundax.modules.{domain}.service.query`。
- 字段只来自当前读取条件。
- 字段名使用业务语义，不使用 `query`、`params`、`condition` 这类泛化容器名。
- 使用普通 Java Bean getter/setter，保持 Java 8 兼容。
- 不添加校验注解，入口校验留在 API `Request`。
- 不包含 `pageNo`、`pageSize`；分页仍由 `Page<T>` 参数承载。

禁止：

- 禁止 `XxxQuery extends Xxx`。
- 禁止把 `Request` 直接作为 Service 参数。
- 禁止把 `XxxQuery` 放入 `entity` 包。
- 禁止为 Query 迁移新增 `QueryService` 层。
- 禁止把 DAO 展开参数回退为 `Entity`。

## 7. Domain Execution Matrix

### Dict

迁移 `Dict.Query` 到 `DictQuery`，保持 `type`、`label`、`remarks` 等过滤语义。Controller 中 `DictQueryRequest` / `DictPageRequest` 只装配 `DictQuery`，Service 不再接收查询用 `Dict`。

### Menu

迁移 `Menu.Query` 到 `MenuQuery`，保持 `parentId`、`displayFlag`、`maxRank` 等读取语义。树结构写入和移动逻辑不纳入本轮。

### User

迁移 `User.Query` 到 `UserQuery`，保持用户分页和列表过滤语义。角色列表中使用的 `Role.Query` 不在 User 任务里顺手迁移，只在调用点改为当前 Role 查询对象已经提供的稳定入口；若 Role 尚未迁移，User 任务等待 Role 任务完成或只保留最小兼容。

### Role

迁移 `Role.Query` 到 `RoleQuery`，保持 `enableFlag` 等过滤语义。角色菜单、角色用户分配属于写入/关系动作，不在 Query 任务中扩展。

### Office

迁移 `Office.Query` 到 `OfficeQuery`，保持 `parentId`、`name`、`remarks` 等过滤语义。组织树移动不纳入本轮。

### Log

迁移 `Log.Query` 到 `LogQuery`，保持日志分页、列表和 `SysLogUtils` 查询条件语义。需要同步移除工具类中直接构造 `Log.Query` 的调用。

### Storage

迁移 `Storage.Query` 到 `StorageQuery`，保持存储分页、树数据和文件过滤语义。`StorageBusiness.Query` 不跟随本任务删除，除非当前任务确认无 main 代码引用。

### Member

迁移 `Member.Query` 到 `MemberQuery`，保持会员列表、分页、计数查询语义。前台登录态和认证适配不纳入本轮。

### Signature

迁移 `SignatureService.page(String businessType, Page<Signature> page)` 到 `SignatureQuery`，只承接读取条件。签名校验、删除、verifySign 等动作不纳入本轮。

## 8. Verification

每个 domain 固定验证：

```bash
rg "setQuery|getQuery|new .*\\.Query" sandwish-admin-api/src/main/java sandwish-front-api/src/main/java sandwish-biz/src/main/java
mvn -q -pl sandwish-biz -am test
mvn -q -pl sandwish-admin-api -am -DskipTests package
```

如果当前 domain 影响前台入口，补充：

```bash
mvn -q -pl sandwish-front-api -am -DskipTests package
```

验证失败时只修复当前 domain 相关问题，不扩大迁移范围。

## 9. Cleanup Rule

全部 domain 迁移完成后执行最终清理：

- 删除本 RUNBOOK。
- 删除或收窄 `TODO.md` 中已经完成的 Query 迁移任务。
- 确认 main 代码中不再使用已迁移 domain 的 `Entity.Query`。
- 确认没有空的 `service/query` 临时类或未使用 import。
- 单独提交清理现场。

