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

- [ ] `assist`：拆分辅助域持久化表达改造任务
  - 范围对象：签名、异步任务相关持久化链路
  - 处理动作：先横向盘点 `assist` 域每条持久化链路，按对象或清晰子链路拆出后续持久化表达改造 TODO，标明各自涉及的 `Service`、Dao interface、DaoImpl、Mapper、Mapper XML、`DO` 和 `PersistenceAssembler`，以及是否依赖通用查询契约、`DO.query`、`DO extends ...`
  - 验收点：辅助域形成可逐条执行的持久化表达改造任务清单；本项不直接改造任何单条持久化链路代码

- [ ] `member`：拆分会员域持久化表达改造任务
  - 范围对象：会员查询、会员资料相关持久化链路
  - 处理动作：先横向盘点 `member` 域每条持久化链路，按对象或清晰子链路拆出后续持久化表达改造 TODO，标明各自涉及的 `Service`、Dao interface、DaoImpl、Mapper、Mapper XML、`DO` 和 `PersistenceAssembler`，以及是否依赖通用查询契约、`DO.query`、`DO extends ...`
  - 验收点：会员域形成可逐条执行的持久化表达改造任务清单；本项不直接改造任何单条持久化链路代码

- [ ] `common-persistence`：拆分公共持久化契约改造任务
  - 范围对象：`CrudDao`、`TreeDao` 及其对通用查询入口、父类字段和树结构父类的依赖
  - 处理动作：先横向盘点公共持久化契约中的通用查询入口、基类字段假设和树结构假设，按清晰子链路拆出后续改造 TODO，标明各自涉及的公共接口、受影响业务域和替换顺序
  - 验收点：公共持久化契约形成可逐条执行的改造任务清单；本项不直接修改任何公共接口或基类代码

- [ ] `common-assembler`：拆分公共 PersistenceAssembler 收敛任务
  - 范围对象：各业务域 `PersistenceAssembler` 中的 `Entity.query <-> DO.query` 转换和 `DO` 继承依赖
  - 处理动作：先横向盘点各业务域 `PersistenceAssembler` 当前承担的 query 转换和父类字段装配职责，按对象或清晰子链路拆出后续收敛 TODO，标明各自涉及的 `Entity`、`DO` 和装配规则调整点
  - 验收点：公共 `PersistenceAssembler` 收敛形成可逐条执行的任务清单；本项不直接修改任何装配代码

- [ ] `docs/00-governance`：拆分持久化表达改造治理同步任务
  - 范围对象：`ARCHITECTURE.md`、`DATABASE-RULES.md`、`NAMING-AND-PLACEMENT-RULES.md`、`TODO.md`
  - 处理动作：先盘点治理文档中与 DAO 查询契约、`DO` 定义、`PersistenceAssembler` 职责相关的现有规则，拆出后续治理同步 TODO，标明需要调整的规则点和对应迁移前置条件
  - 验收点：治理同步形成可逐条执行的文档任务清单；本项不直接修改治理文档内容
