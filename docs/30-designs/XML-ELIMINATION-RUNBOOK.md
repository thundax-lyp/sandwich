# XML ELIMINATION RUNBOOK

## 1. Purpose

本文档指导 Sandwich 将原有基于 Mapper XML、PageHelper 和旧 CRUD 基类的持久化链路，迁移为基于 `com.baomidou.mybatisplus` 的 Java 持久化实现。

最终目标：

- 消灭业务 Mapper XML。
- 消灭 PageHelper。
- 消灭业务 `Dao` / `DaoImpl` / `ServiceImpl` 对 `sandwish-common-mybatis` 旧 CRUD 基类的依赖。
- 保持 `Service -> Dao interface -> DaoImpl -> Mapper -> Database` 三层边界。

本文档是阶段性迁移手册，迁移完成后从 `docs/30-designs/` 删除。

## 2. Scope

当前范围：

- `sandwish-infra` 中业务 Mapper XML 的迁移。
- `sandwish-biz` 中业务 `Dao` interface 的显式化。
- `sandwish-infra` 中 `DaoImpl`、`Mapper`、`DO`、`PersistenceAssembler` 的迁移。
- `sandwish-biz` 中依赖旧 `CrudServiceImpl` 的 `ServiceImpl` 迁移。
- PageHelper 调用、依赖和公共支撑类移除。
- `TODO.md` 按 entity 平行拆解迁移任务。

不在范围内：

- 不改变 Controller/API 行为。
- 不引入新业务分层。
- 不让 Controller 直接感知持久化实现。
- 不让 `sandwish-biz` 直接依赖 MyBatis-Plus Mapper 或 Wrapper。
- 不维护 MySQL / Kingbase XML 的长期业务等价。

## 3. Final State

迁移完成后固定满足：

- `sandwish-infra` 中不存在业务 Mapper XML。
- `mapping/mysql`、`mapping/dameng`、`mapping/kingbase` 目录不存在。
- main 代码中不存在 PageHelper 调用。
- 项目不再需要 `pagehelper-spring-boot-starter`。
- 业务 `Dao` interface 不继承旧 `CrudDao`。
- 业务 `ServiceImpl` 不继承旧 `CrudServiceImpl`。
- `DaoImpl` 不依赖旧 CRUD 基类完成通用持久化。
- `Mapper` 继承 `BaseMapper<DO>`，只在 `sandwish-infra` 内使用。
- `QueryWrapper`、`LambdaQueryWrapper` 和 MyBatis-Plus `Page` 不泄漏到 `sandwish-biz`。
- DAO 分页返回使用 MyBatis-Plus `Page<Entity>`，由 Service 层装配当前对外分页模型。
- `sandwish-common-mybatis` 只保留 MyBatis-Plus 基础配置、扫描标记、数据库方言和必要公共基础设施。
- Mapper interface 使用标准 `@Mapper`，保持最小定义，不承载业务方法。

## 4. Behavior Source

业务逻辑固定以达梦 XML 为准：

- `mapper.mapping.dameng.*.xml` 是迁移前唯一事实来源。
- MySQL / Kingbase XML 只用于识别历史方言差异。
- MySQL / Kingbase XML 不作为迁移目标语义。
- 如果 MySQL / Kingbase 与达梦行为不同，按达梦行为实现。
- 差异不在迁移过程中调和。
- 迁移后测试断言按达梦 XML 原行为编写。

## 5. Task Shape

手册按垂直域编写，任务按平行 entity 执行。

垂直域指迁移一个 entity 时必须同时检查和调整的完整链路：

`domain Entity -> Dao interface -> ServiceImpl -> DO -> PersistenceAssembler -> DaoImpl -> Mapper -> dameng XML -> tests -> cleanup`

平行 entity 指 `TODO.md` 中每个执行任务只迁移一个 entity 或一组不可拆分的强相关 entity。

固定规则：

- 一个 entity 一个 TODO。
- 一个 entity 一个闭环提交。
- 不用“迁移 sys 模块”这类模块级大任务替代 entity 任务。
- 不跨 entity 顺手重构。
- 公共清理任务放在所有 entity 迁移完成后执行。

## 6. DAO Signature Rules

业务 `Dao` interface 固定使用显式业务方法，不复刻旧 CRUD 基类形态。

查询类方法参数固定展开：

- `findById(String id)`
- `findList(String name, String status)`
- `Page<Entity> findPage(String name, String status, int pageNo, int pageSize)`
- `countByStatus(String status)`
- `existsByLoginName(String loginName)`

禁止查询类方法使用 `domain.Entity` 承载条件：

- 禁止 `findList(Entity entity)`。
- 禁止 `findPage(Page<Entity> page, Entity entity)`。
- 禁止 `count(Entity entity)`。
- 禁止用实体对象传递只服务查询的临时条件。
- 禁止 DAO 分页方法接收或返回 `com.github.thundax.common.persistence.Page`。
- DAO 分页方法只接收 `pageNo`、`pageSize` 基础参数；参数有效性在 `ServiceImpl` 里校验。

只有写入类方法使用 `domain.Entity`：

- `insert(Entity entity)`
- `update(Entity entity)`

删除类方法固定使用明确参数：

- `deleteById(String id)`
- `deleteByIds(List<String> ids)`
- `deleteByOwnerId(String ownerId)`

当查询参数过多时，先保持展开参数。只有参数已经破坏可读性并经任务讨论确认后，才能新增专用 query object。query object 不得复用 `domain.Entity`。

## 7. Replacement Strategy

替代优先级固定为：

1. MyBatis-Plus `BaseMapper` 处理标准单表 CRUD。
2. MyBatis-Plus `LambdaQueryWrapper` 处理单表动态条件查询。
3. MyBatis-Plus `Page` 处理分页。
4. `DaoImpl` Java 持久化实现处理批量、树结构、关系表维护和跨查询组装。
5. 当前 entity 专用持久化 helper 承接 `DaoImpl` 中不可读的复杂实现。

禁止：

- 新增 Mapper XML。
- 在 Mapper interface 中新增业务方法。
- 在 Mapper interface 中使用注解 SQL。
- 在 Mapper interface 中使用 SQL Provider。
- 用字符串拼接承接动态 SQL。
- 为了使用 Wrapper 改变达梦 SQL 语义。
- 把数据库差异藏到 Controller 或 Service。
- 把复杂业务规则下沉到 Mapper 或 SQL Provider。
- 让 `sandwish-biz` 依赖 `BaseMapper`、`QueryWrapper`、`LambdaQueryWrapper` 或 MyBatis-Plus `Page`。
- DAO 以外的 `sandwish-biz` 代码直接操作 MyBatis-Plus 分页实现。

## 8. Mapper Minimal Definition Rules

Mapper interface 固定保持最小定义。

标准形态：

```java
@Mapper
public interface PostMapper extends BaseMapper<PostDO> {}
```

固定规则：

- Mapper interface 只继承 `BaseMapper<DO>`。
- Mapper interface 使用 `org.apache.ibatis.annotations.Mapper`。
- Mapper interface 不使用 `MyBatisDao`。
- Mapper interface 不声明业务查询方法。
- Mapper interface 不声明分页方法。
- Mapper interface 不声明批量业务方法。
- Mapper interface 不使用 MyBatis 注解 SQL。
- Mapper interface 不使用 SQL Provider。
- 查询条件组装固定放在 `DaoImpl`。
- 分页组装固定放在 `DaoImpl`。
- 批量、树结构、关系表维护和跨查询组装固定放在 `DaoImpl` 或 `DaoImpl` 私有方法。
- 达梦 XML 中的业务语义固定由 `DaoImpl` 承接。

如果单个 `DaoImpl` 方法无法在可读范围内承接复杂 SQL，先拆分 `DaoImpl` 私有方法或当前 entity 专用持久化 helper。不得退回 Mapper 自定义方法、注解 SQL、SQL Provider 或 XML。

## 9. One Entity Migration Procedure

每个 entity 固定按以下步骤执行：

1. 读取对应 `mapper/mapping/dameng/*.xml`。
2. 盘点 XML 中的 statement、参数、返回映射、排序、状态过滤、分页和副作用。
3. 标记 MySQL / Kingbase XML 为历史实现，不作为目标标准。
4. 盘点 `domain Entity`、`Dao interface`、`ServiceImpl`、`DO`、`PersistenceAssembler`、`DaoImpl`、`Mapper`。
5. 将 `Dao` interface 从旧 CRUD 基类迁出。
6. 将查询类 `Dao` 方法参数展开。
7. 只保留 `insert(Entity entity)`、`update(Entity entity)` 使用实体参数。
8. 调整 `ServiceImpl`，移除旧 `CrudServiceImpl` 继承，显式组合 `Dao`。
9. 让 `Mapper` 保持 `@Mapper public interface XxxMapper extends BaseMapper<XxxDO> {}` 最小定义。
10. 用 `BaseMapper` 承接标准 CRUD。
11. 用 `LambdaQueryWrapper` 承接单表条件查询。
12. `ServiceImpl` 校验 `pageNo`、`pageSize`，DAO 使用 MyBatis-Plus `Page` 承接分页查询并返回 `Page<Entity>`。
13. 在 `DaoImpl` 中用 Java 持久化实现承接复杂达梦 SQL。
14. 保持 `PersistenceAssembler` 负责 `Entity <-> DO` 转换。
15. 删除当前 entity 对应的 MySQL / Kingbase / 达梦 XML。
16. 删除空的 mapping 目录和失效 Mapper 方法。
17. 补齐或更新当前 entity 的最小测试。
18. 编译验证。
19. 删除或收窄 `TODO.md` 中当前 entity 任务。
20. 提交当前 entity 闭环。

## 10. Page Migration Rules

PageHelper 固定消灭，不作为迁移后的兼容层保留。

迁移规则：

- 禁止新增 `PageHelper.startPage`。
- 禁止新增 `PageHelper.count`。
- 禁止新增 `PageHelper.clearPage`。
- DAO 方法签名使用 `Page<Entity> findPage(..., int pageNo, int pageSize)`。
- DAO 内部直接 `new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize)`。
- 不新增 `PageFactory.create()` 这类分页工厂。
- DAO 不接收、不返回 `com.github.thundax.common.persistence.Page`，该类型未来会删除。
- DAO 不返回 `Page<DO>`；`DaoImpl` 内部将 `Page<DO>` 转成 `Page<Entity>`。
- Service 层负责校验 `pageNo`、`pageSize` 的有效性，并把 `Page<Entity>` 装配为当前对外分页模型。
- 同一查询链路不得混用 PageHelper 和 MyBatis-Plus 分页。

全局删除 PageHelper 的条件：

- `rg "PageHelper" sandwish-*` 无 main 代码调用。
- 所有分页 XML 已迁移。
- MyBatis-Plus 分页固定使用 `DbType.DM`。
- Admin / Front 包编译通过。

## 11. Verification

每个 entity 固定验证：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

最小行为检查：

- 主键查询行为不变。
- 列表查询条件与达梦 XML 一致。
- 排序与达梦 XML 一致。
- 状态过滤与达梦 XML 一致。
- 分页页码、页大小和总数行为不变。
- 空条件行为不变。
- 主键不存在行为不变。
- insert / update 字段映射不变。

结构检查：

```bash
rg "PageHelper" sandwish-*
find sandwish-infra -path '*mapper/mapping*' -type f
rg "extends CrudDao|extends CrudServiceImpl|BaseMapper|QueryWrapper|LambdaQueryWrapper|Page<" sandwish-biz
rg "@Select|@Update|@Insert|@Delete|Provider" sandwish-infra/src/main/java/com/github/thundax/modules/*/persistence/mapper
```

允许 MyBatis-Plus `Page<Entity>` 出现在 `sandwish-biz` 的 DAO 分页签名和对应 Service 装配代码中。允许 `BaseMapper`、`QueryWrapper`、`LambdaQueryWrapper`、MyBatis-Plus `Page<DO>` 出现在 `sandwish-infra` 和 `sandwish-common-mybatis` 的基础设施中。

## 12. TODO Decomposition Rules

`TODO.md` 固定按 entity 平行拆解。

每个 entity TODO 必须包含：

- 以达梦 XML 为行为基准。
- 迁出旧 CRUD 基类。
- 展开查询 DAO 参数。
- 只允许 insert / update 使用 `domain.Entity`。
- Mapper 保持最小 `BaseMapper<DO>` 定义。
- Mapper 使用标准 `@Mapper`。
- 迁移 XML 到 `DaoImpl` 中的 MyBatis-Plus / Java 持久化实现。
- 消除当前 entity 的 PageHelper 依赖。
- 删除当前 entity 对应 XML。
- 补齐最小测试和编译验证。

公共收口 TODO 固定放在所有 entity TODO 之后。

## 13. Candidate Entity Queue

第一批迁移有达梦 XML 的 entity：

1. `Signature`
2. `Member`
3. `Storage` / `StorageBusiness`
4. `Log`
5. `Menu`
6. `Office`
7. `Role`
8. `UploadFile`
9. `UserEncrypt`
10. `User`

第二批迁移无达梦 XML 但可能仍受旧 CRUD / PageHelper 影响的持久化对象：

1. `Dict`
2. `AsyncTask`
3. `AccessToken`
4. `LoginForm`
5. `LoginLock`
6. `Permission`
7. `KeypairPrivateKey`
8. `SmsValidateCode`

公共收口：

1. 删除 PageHelper 依赖和公共支撑类。
2. 删除旧 CRUD 基类或收窄为无业务调用的基础设施。
3. 删除 Mapper XML 配置和空 mapping 目录。
4. 增加禁止新增 XML、PageHelper、旧 CRUD 基类依赖的架构测试或静态检查。
5. 同步长期数据库治理规则。

## 14. Completion Criteria

全局迁移完成必须满足：

- `sandwish-infra` 中不存在业务 Mapper XML。
- main 代码中不存在 PageHelper。
- main 代码中不存在旧 CRUD 基类业务调用。
- 业务 Mapper interface 均保持最小 `BaseMapper<DO>` 定义。
- `TODO.md` 中无 MyBatis XML 迁移剩余任务。
- Admin / Front 包编译通过。
- 数据库治理文档已同步长期规则。

## 15. Open Items

无
