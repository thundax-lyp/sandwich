# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前主线顺序

1. 文档治理骨架
   - 补齐架构、文档、命名、数据库和部署治理文档
2. 三层架构规则收敛
   - 固定 `Controller -> Service -> DAO/Mapper` 边界
3. 业务需求与数据库文档补齐
   - 按现有模块和业务域逐步建立 `10-requirements/` 与 `20-database/`
4. 质量与交付闭环
   - 按任务补测试、文档同步和小步提交

## P0 - Controller / Service 模型职责隔离

## P0 - 持久化表达改造

- [ ] `docs/30-designs`：持久化迁移手册收尾
  - 范围对象：`INFRA-SPLIT-RUNBOOK.md`、`TODO.md`、治理文档
  - 处理动作：持久化迁移完成后删除临时手册；删除或收窄已完成 TODO；将稳定规则沉淀到治理文档
  - 验收点：持久化临时手册不再保留，完成历史只存在于 commit / PR 中

- [ ] `storage-business-persistence`：收敛存储业务绑定持久化表达
  - 范围对象：`StorageDao` 业务绑定方法、`StorageDaoImpl`、`StorageMapper`、`mapper/mapping/mysql/StorageMapper.xml`、`mapper/mapping/dameng/StorageMapper.xml`、`StorageBusinessDO`、`StoragePersistenceAssembler`、`StorageBusiness`
  - 当前依赖：`StorageBusinessDO extends DataEntity<StorageBusinessDO>`；`StorageBusinessDO.Query` 承载 `businessId`、`businessType`、`businessParams`、`publicFlag` 查询条件；`StoragePersistenceAssembler` 负责 `StorageBusiness.Query <-> StorageBusinessDO.Query` 转换；MySQL 将业务绑定字段直接更新在 `assist_storage`，达梦使用 `assist_storage_business` 关系表
  - 处理动作：拉平 `StorageBusinessDO` 父类字段；移除业务绑定 DO query 转换；显式保留 `findBusiness`、`insertBusiness`、`deleteBusiness`、`deleteBusinessByBusiness` 方法；分别保持 MySQL 更新主表和达梦维护关系表的现有 SQL 语义
  - 验收点：存储业务绑定链路不再依赖 `StorageBusinessDO.Query` 或 `StorageBusinessDO extends DataEntity`；Admin 包编译通过

- [ ] `assist-signature-persistence`：收敛签名持久化表达
  - 范围对象：`SignatureService`、`SignatureServiceImpl`、`SignatureDao`、`SignatureDaoImpl`、`SignatureMapper`、`mapper/mapping/mysql/SignatureMapper.xml`、`mapper/mapping/dameng/SignatureMapper.xml`、`mapper/mapping/kingbase/SignatureMapper.xml`、`SignatureDO`、`SignaturePersistenceAssembler`
  - 当前依赖：`SignatureService extends CrudService<Signature>`；`SignatureServiceImpl extends CrudServiceImpl<SignatureDao, Signature>`；`SignatureDao extends CrudDao<Signature>`；`SignatureMapper extends CrudDao<SignatureDO>`；`SignatureDO extends DataEntity<SignatureDO>`；`SignatureDO.Query` 承载 `businessType`、`businessId`、`businessIdList`、`isVerifySign` 查询条件；`SignaturePersistenceAssembler` 负责 `Signature.Query <-> SignatureDO.Query` 转换
  - 处理动作：显式化签名 Mapper 方法；拉平 `SignatureDO` 父类字段；将 `DO.query` 替换为显式查询字段；保留 `get` 使用 `businessType + businessId` 查询、`getMany` 使用 `business_id` 集合查询、`findList`、`insert`、`update`、`delete`、`insertOrUpdate` 能力；分别保持 MySQL `ON DUPLICATE KEY UPDATE` 和达梦 / Kingbase `MERGE INTO` 语义
  - 验收点：签名链路不再依赖通用查询契约、`SignatureDO.Query` 或 `SignatureDO extends DataEntity`；Admin 包编译通过

- [ ] `assist-async-task-persistence`：收敛异步任务 Redis 持久化表达
  - 范围对象：`AsyncTaskService`、`AsyncTaskServiceImpl`、`AsyncTaskDao`、`AsyncTaskDaoImpl`、`AsyncTaskDO`、`AsyncTaskPersistenceAssembler`
  - 当前依赖：`AsyncTaskDao` 为自定义 Redis DAO，不存在 Mapper / Mapper XML；`AsyncTaskDO extends AdminDataEntity<AsyncTaskDO>`；`AsyncTaskPersistenceAssembler` 复制 `AdminDataEntity` 父类字段；Redis key 前缀为 `Constants.CACHE_PREFIX + "assist.asyncTask."`，保存时使用 `expiredSeconds`
  - 处理动作：拉平 `AsyncTaskDO` 父类字段；保持 `get`、`save`、`delete` Redis 语义和过期时间语义；移除对 `AdminDataEntity` 父类字段的持久化对象依赖；不新增 Mapper 或数据库表
  - 验收点：异步任务 Redis 链路不再依赖 `AsyncTaskDO extends AdminDataEntity`；Admin 包编译通过

- [ ] `member-persistence`：收敛会员持久化表达
  - 范围对象：`MemberService`、`MemberServiceImpl`、`MemberDao`、`MemberDaoImpl`、`MemberMapper`、`mapper/mapping/mysql/MemberMapper.xml`、`mapper/mapping/dameng/MemberMapper.xml`、`mapper/mapping/kingbase/MemberMapper.xml`、`MemberDO`、`MemberPersistenceAssembler`
  - 当前依赖：`MemberService extends CrudService<Member>`；`MemberServiceImpl extends CrudServiceImpl<MemberDao, Member>`；`MemberDao extends CrudDao<Member>`；`MemberMapper extends CrudDao<MemberDO>`；`MemberDO extends AdminDataEntity<MemberDO>`；`MemberDO.Query` 承载 `enableFlag`、`email`、`name`、`remarks`、`beginRegisterDate`、`endRegisterDate`、`beginLoginDate`、`endLoginDate`、`ywtbId`、`zjhm`、`mobile` 查询条件；`MemberPersistenceAssembler` 负责 `Member.Query <-> MemberDO.Query` 转换
  - 处理动作：显式化会员 Mapper 方法；拉平 `MemberDO` 父类字段；将 `DO.query` 替换为显式查询字段；保留 `get`、`getMany`、`findList`、`insert`、`update`、`delete`、`updatePriority`、`findByLoginName`、`findByEmail`、`updateLoginInfo`、`updateInfo`、`updateLoginPass`、`updateEnableFlag`、`getByZjhm`、`getByYwtbId` 能力；保持 MySQL / 达梦 / Kingbase 三方言 SQL 语义一致
  - 验收点：会员链路不再依赖通用查询契约、`MemberDO.Query` 或 `MemberDO extends AdminDataEntity`；Front 包编译通过

- [ ] `common-crud-dao-contract`：收敛通用 Crud DAO 契约
  - 范围对象：`sandwish-common/.../CrudDao.java`、仍继承 `CrudDao` 的业务 DAO / Mapper、相关 `CrudServiceImpl` 调用点
  - 当前依赖：`CrudDao<T>` 固定暴露 `get`、`getMany`、`findList`、`insert`、`update`、`updatePriority`、`updateStatus`、`updateDelFlag`、`delete`；剩余继承点包括 `SignatureDao`、`MemberDao`、`StorageDao`、`UserDao`、`RoleDao`、`UserEncryptDao`、`DictDao`、`LogDao`、`UploadFileDao`、`OfficeDao` 以及 `SignatureMapper`、`MemberMapper`、`StorageMapper`
  - 处理动作：在各业务链路完成显式 Mapper / DAO 方法后，盘点仍需保留的通用读写方法；将无业务语义或只由旧 XML 模板支撑的方法从公共契约中拆出或下沉到具体 DAO；同步 `CrudServiceImpl` 依赖
  - 验收点：公共 Crud 契约只保留跨业务真实共享的方法；所有剩余继承点有明确业务理由；Admin / Front 包编译通过

- [ ] `common-tree-dao-contract`：收敛通用 Tree DAO 契约
  - 范围对象：`sandwish-common/.../TreeDao.java`、`MenuDao`、菜单树 Mapper XML、已显式化的 `OfficeDao` 对照实现
  - 当前依赖：`TreeDao<T>` 继承 `CrudDao<T>`，并通过 `@Param("node")` 传递树节点对象，统一要求 `getTreeNode`、`updateLftRgt`、`updateParent`、`getMaxPosition`、`moveTreeRgts`、`moveTreeLfts`、`moveTreeNodes`；当前仅 `MenuDao` 仍继承 `TreeDao`
  - 处理动作：参考 `OfficeDao` 显式树方法改造经验，评估 `MenuDao` 是否下沉显式树方法；移除公共树契约对 `CrudDao`、父类字段和嵌套集字段命名的隐式假设；保持菜单树移动、删除、插入语义
  - 验收点：树 DAO 契约不再强制所有树对象共享同一 Mapper 参数结构；菜单与机构树链路编译通过

- [ ] `common-entity-query-contract`：收敛公共实体 query 契约
  - 范围对象：`BaseEntity`、`DataEntity`、`TreeEntity`、`AdminDataEntity`、`AdminTreeEntity`、剩余 `Entity.Query` / `DO.Query` 使用点
  - 当前依赖：`DataEntity` 持有通用 `Object query`，并提供 `createQueryObject`、`setQueryProp`、`getQueryProp` 反射入口；`AdminDataEntity` / `AdminTreeEntity` 叠加创建人、更新人和签名能力；剩余 DO 继承集中在 `SignatureDO`、`AsyncTaskDO`、`StorageDO`、`StorageBusinessDO`、`MemberDO`
  - 处理动作：等待各 `DO.Query` 消除后，评估 `DataEntity.query`、反射 query setter/getter 和 DO 侧父类继承是否还能保留；将业务实体仍需的查询对象职责与持久化 DO 职责分离；避免一次性删除影响 Controller / Service 查询绑定
  - 验收点：公共实体基类不再作为持久化查询条件容器；剩余 query 使用点有明确业务层理由；Admin / Front 包编译通过

- [ ] `assembler-query-conversion`：清理持久化装配中的 DO query 双向转换
  - 范围对象：`SignaturePersistenceAssembler`、`StoragePersistenceAssembler`、`MemberPersistenceAssembler`、相关 `SignatureDO`、`StorageDO`、`StorageBusinessDO`、`MemberDO`
  - 当前依赖：签名、存储、会员仍存在 `toDataObjectQuery` / `toEntityQuery` 或 `toBusinessDataObjectQuery` / `toBusinessEntityQuery`，并在 `toDataObject` / `toEntity` 中执行 `Entity.Query <-> DO.Query` 双向转换
  - 处理动作：随 `assist-signature-persistence`、`storage-file-persistence`、`storage-business-persistence`、`member-persistence` 逐项删除 DO query 转换；查询条件从业务实体 query 单向复制到 DO 显式查询字段；实体回转不再重建业务 query
  - 验收点：所有 `PersistenceAssembler` 不再调用 `dataObject.setQuery(...)` 或 `dataObject.getQuery()`；剩余业务实体 query 只服务 Controller / Service 查询输入

- [ ] `assembler-common-field-copy`：收口持久化装配中的公共字段复制
  - 范围对象：全部 `*PersistenceAssembler`、已拉平的 `*DO` 公共字段、仍待拉平的 `SignatureDO`、`AsyncTaskDO`、`StorageDO`、`StorageBusinessDO`、`MemberDO`
  - 当前依赖：各装配器重复复制 `id`、`isNewRecord`、`priority`、`remarks`、`createDate`、`updateDate`、`delFlag`、`createUserId`、`updateUserId`；`sys` 域已多处改为 `copyQuery(entity.getQuery(), dataObject)` 单向复制，辅助 / 存储 / 会员仍有 DO query 双向转换
  - 处理动作：在所有 DO 拉平后，评估是否保留局部显式复制或抽取最小公共复制方法；避免新增跨层重型基类；保持字段复制在 `PersistenceAssembler` 内可读、可追踪
  - 验收点：公共字段复制规则一致；无新的 DO 父类依赖或反射式装配；Admin / Front 包编译通过

- [ ] `docs-architecture-persistence-boundary`：同步架构文档中的持久化边界规则
  - 范围对象：`docs/00-governance/ARCHITECTURE.md`
  - 当前依赖：架构文档已固定 `DAO implementation`、Mapper、Mapper XML、`DO/DataObject`、`PersistenceAssembler` 归属 `sandwish-infra`，但尚未明确 `DO` 不承载业务 query、`PersistenceAssembler` 不回填查询对象、Mapper 方法优先显式表达业务语义
  - 处理动作：在持久化表达改造完成后，将稳定规则补入 `DAO / Mapper` 与 `Entity / VO / DTO` 小节；明确 Redis DAO 属于 infra 持久化实现但不要求新增 Mapper / XML
  - 验收点：架构文档能指导后续新链路按三层边界落位，不再需要临时迁移手册解释持久化边界

- [ ] `docs-database-query-model-rules`：同步数据库规则中的查询模型和 DO 规则
  - 范围对象：`docs/00-governance/DATABASE-RULES.md`
  - 当前依赖：数据库规则已定义 `Entity`、`DO/DataObject`、Mapper XML 和 `PersistenceAssembler` 归属；`Document Requirements` 已要求业务域数据库文档包含 `Query Model Rules`，但尚未固定 DO 查询字段、Mapper 参数和方言 XML 差异处理规则
  - 处理动作：在各业务域 DO query 消除后，补充 `DO/DataObject` 使用显式字段承载持久化查询条件、Mapper XML 不依赖通用 `query.*` 容器、方言 SQL 差异必须保留并说明的规则
  - 验收点：数据库治理文档能约束新增或修改 Mapper XML 时的查询条件表达和多方言同步要求

- [ ] `docs-naming-persistence-placement`：同步命名目录规则中的持久化对象细则
  - 范围对象：`docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`
  - 当前依赖：命名目录规则已固定 `DO/DataObject`、Mapper、DAO implementation、`PersistenceAssembler` 目录和命名；尚未明确 `DO` 与业务 `Entity` 查询模型的命名边界、显式查询字段命名、公共 DAO 契约收敛后的放置口径
  - 处理动作：在公共契约和装配器收敛后，补充 DO 显式字段、查询字段命名、DAO / Mapper 显式方法命名和 `PersistenceAssembler` 单向 query 复制规则；同步删除过时的开放项或收窄为具体命名决策
  - 验收点：命名目录规则能作为新增持久化类、Mapper 方法和装配器方法的直接命名依据
