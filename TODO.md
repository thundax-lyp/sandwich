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

- [ ] `storage-file-persistence`：收敛存储文件持久化表达
  - 范围对象：`StorageService`、`StorageServiceImpl`、`StorageDao`、`StorageDaoImpl`、`StorageMapper`、`mapper/mapping/mysql/StorageMapper.xml`、`mapper/mapping/dameng/StorageMapper.xml`、`StorageDO`、`StoragePersistenceAssembler`
  - 当前依赖：`StorageService extends CrudService<Storage>`；`StorageServiceImpl extends CrudServiceImpl<StorageDao, Storage>`；`StorageDao extends CrudDao<Storage>`；`StorageMapper extends CrudDao<StorageDO>`；`StorageDO extends DataEntity<StorageDO>`；`StorageDO.Query` 承载 `mimeType`、`businessId`、`businessType`、`ownerId`、`ownerType`、`enableFlag`、`publicFlag`、`name`、`remarks` 查询条件；`StoragePersistenceAssembler` 负责 `Storage.Query <-> StorageDO.Query` 转换
  - 处理动作：显式化文件主表 Mapper 方法；拉平 `StorageDO` 父类字段；将 `DO.query` 替换为显式查询字段；保留 `get`、`getMany`、`findList`、`insert`、`update`、`delete`、`findMimeTypeList`、`findBusinessTypeList`、`updateEnableFlag`、`updatePublicFlag` 能力；核对 MySQL 与达梦在业务字段查询、`publicFlag`、`business_type` 来源表上的现有差异，未确认前不改变方言语义
  - 验收点：存储文件链路不再依赖通用查询契约、`StorageDO.Query` 或 `StorageDO extends DataEntity`；Admin 包编译通过

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

- [ ] `common-assembler`：拆分公共 PersistenceAssembler 收敛任务
  - 范围对象：各业务域 `PersistenceAssembler` 中的 `Entity.query <-> DO.query` 转换和 `DO` 继承依赖
  - 处理动作：先横向盘点各业务域 `PersistenceAssembler` 当前承担的 query 转换和父类字段装配职责，按对象或清晰子链路拆出后续收敛 TODO，标明各自涉及的 `Entity`、`DO` 和装配规则调整点
  - 验收点：公共 `PersistenceAssembler` 收敛形成可逐条执行的任务清单；本项不直接修改任何装配代码

- [ ] `docs/00-governance`：拆分持久化表达改造治理同步任务
  - 范围对象：`ARCHITECTURE.md`、`DATABASE-RULES.md`、`NAMING-AND-PLACEMENT-RULES.md`、`TODO.md`
  - 处理动作：先盘点治理文档中与 DAO 查询契约、`DO` 定义、`PersistenceAssembler` 职责相关的现有规则，拆出后续治理同步 TODO，标明需要调整的规则点和对应迁移前置条件
  - 验收点：治理同步形成可逐条执行的文档任务清单；本项不直接修改治理文档内容
