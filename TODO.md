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

- [ ] `docs/00-governance`：固化 Controller / Service 模型边界规则
  - 范围对象：`ARCHITECTURE.md`、`NAMING-AND-PLACEMENT-RULES.md`
  - 处理动作：固定 `Controller=Request/Response`、`Service=Entity`；固定 `InterfaceAssembler` 的放置、命名和禁止事项
  - 验收点：后续 API 入口迁移不需要重新讨论 `Request/Response`、`Entity` 和 `InterfaceAssembler` 的归属

- [ ] `TODO.md`：按 API 入口拆分 Controller / Service 模型隔离任务
  - 范围对象：后台与前台 Controller/API 入口
  - 处理动作：按 `docs/30-designs/MODEL-SEPARATION-RUNBOOK.md` 的 TODO 模板，为每个入口列出当前 API 模型泄漏点、待新增或收窄的 `Request/Response`、待新增 `InterfaceAssembler`、Service 方法签名是否需要调整
  - 验收点：每个入口都有可独立验收的模型隔离 TODO；TODO 不包含 Mapper/XML/DAO implementation 迁移

- [ ] `sys-dict`：拆分 Controller / Service 模型职责隔离试点任务
  - 范围对象：`Dict` 相关 Controller/API、Service、Entity、API 查询与响应模型
  - 处理动作：先盘点 `dict` 入口的 API 模型泄漏点，拆出后续试点 TODO，标明需要新增或收窄的 `Request/Response`、`InterfaceAssembler` 和 Service 方法签名调整点；不迁移 Mapper/XML/DAO implementation
  - 验收点：`sys-dict` 形成可执行的 Controller / Service 模型隔离试点清单；本项不直接改造任何入口代码

- [ ] `docs/30-designs`：模型隔离迁移收尾
  - 范围对象：`INFRA-SPLIT-RUNBOOK.md`、`MODEL-SEPARATION-RUNBOOK.md`、`TODO.md`、治理文档
  - 处理动作：两套迁移完成后删除临时手册；删除或收窄已完成 TODO；将稳定规则沉淀到治理文档
  - 验收点：临时手册不再保留，完成历史只存在于 commit / PR 中

## P0 - 持久化表达改造

- [ ] `sys-user-encrypt`：改造用户密文字段持久化表达
  - 范围对象：`UserEncryptService`、`DefaultUserEncryptServiceImpl`、`DatabaseUserEncryptServiceImpl`、`UserEncryptDao`、`UserEncryptDaoImpl`、`UserEncryptMapper`、`mysql/dameng/kingbase/UserEncryptMapper.xml`、`UserEncryptDO`、`UserEncryptPersistenceAssembler`
  - 当前依赖：`UserEncryptService extends CrudService<UserEncrypt>`；两个 Service implementation 均继承 `CrudServiceImpl<UserEncryptDao, UserEncrypt>`；`UserEncryptDao extends CrudDao<UserEncrypt>`；`UserEncryptMapper extends CrudDao<UserEncryptDO>`；`UserEncrypt extends BaseUserEncrypt extends AdminDataEntity<UserEncrypt>`；`UserEncryptDO extends AdminDataEntity<UserEncryptDO>`；当前无独立 `Query`
  - 处理动作：收敛用户密文字段查询、列表查询和密码更新的持久化表达；拆除或替换本链路对 `CrudDao` 通用契约和 `DO extends AdminDataEntity` 的依赖
  - 验收点：默认加密实现与数据库加密实现仍保持现有业务语义；DAO implementation 完成 Entity/DO 转换；密文字段不向 Controller 或 Service 暴露 `DO`

- [ ] `sys-role`：改造角色持久化表达
  - 范围对象：`RoleService`、`RoleServiceImpl`、`RoleDao`、`RoleDaoImpl`、`RoleMapper`、`mysql/dameng/kingbase/RoleMapper.xml`、`RoleDO`、`RolePersistenceAssembler`
  - 当前依赖：`RoleService extends CrudService<Role>`；`RoleServiceImpl extends CrudServiceImpl<RoleDao, Role>`；`RoleDao extends CrudDao<Role>`；`RoleMapper extends CrudDao<RoleDO>`；`Role extends BaseRole extends AdminDataEntity<Role>`；`RoleDO extends AdminDataEntity<RoleDO>`；存在 `Role.Query <-> RoleDO.Query`
  - 处理动作：收敛角色基础查询、启停、角色菜单、角色用户关系读取和写入的持久化表达；明确 `findRoleMenu`、`findRoleUser` 返回值在 DAO implementation 中的 Entity 装配边界；拆除或替换通用查询契约、`DO.query` 和 `DO extends AdminDataEntity` 依赖
  - 验收点：角色与菜单、用户关系 SQL 仍由 Mapper XML 承担；Service 只使用 Entity 或稳定业务参数；关系装配不泄漏 `DO`

- [ ] `sys-menu`：改造菜单持久化表达
  - 范围对象：`MenuService`、`MenuServiceImpl`、`MenuDao`、`MenuDaoImpl`、`MenuMapper`、`mysql/dameng/kingbase/MenuMapper.xml`、`MenuDO`、`MenuPersistenceAssembler`
  - 当前依赖：`MenuService extends TreeService<Menu>`；`MenuServiceImpl extends TreeServiceImpl<MenuDao, Menu>`；`MenuDao extends TreeDao<Menu>`；`MenuMapper extends TreeDao<MenuDO>`；`Menu extends BaseMenu extends AdminTreeEntity<Menu>`；`MenuDO extends AdminTreeEntity<MenuDO>`；存在 `Menu.Query <-> MenuDO.Query`
  - 处理动作：收敛菜单树查询、树节点移动、显示状态更新和菜单角色关系删除的持久化表达；拆除或替换本链路对 `TreeDao`、树结构父类字段、`DO.query` 和 `DO extends AdminTreeEntity` 的依赖
  - 验收点：树结构写入仍由 Service 编排；Mapper XML 只接收 `MenuDO`；菜单角色关系写入边界清晰

- [ ] `sys-office`：改造机构持久化表达
  - 范围对象：`OfficeService`、`OfficeServiceImpl`、`OfficeDao`、`OfficeDaoImpl`、`OfficeMapper`、`mysql/dameng/kingbase/OfficeMapper.xml`、`OfficeDO`、`OfficePersistenceAssembler`
  - 当前依赖：`OfficeService extends TreeService<Office>`；`OfficeServiceImpl extends TreeServiceImpl<OfficeDao, Office>`；`OfficeDao extends TreeDao<Office>`；`OfficeMapper extends TreeDao<OfficeDO>`；`Office extends BaseOffice extends AdminTreeEntity<Office>`；`OfficeDO extends AdminTreeEntity<OfficeDO>`；存在 `Office.Query <-> OfficeDO.Query`
  - 处理动作：收敛机构树查询、树节点移动和最大排序查询的持久化表达；拆除或替换本链路对 `TreeDao`、树结构父类字段、`DO.query` 和 `DO extends AdminTreeEntity` 的依赖
  - 验收点：机构树 Service 语义不变；DAO implementation 完成 Entity/DO 转换；Mapper XML 不暴露 Entity

- [ ] `sys-log`：改造日志持久化表达
  - 范围对象：`LogService`、`LogServiceImpl`、`LogDao`、`LogDaoImpl`、`LogMapper`、`mysql/dameng/kingbase/LogMapper.xml`、`LogDO`、`LogPersistenceAssembler`
  - 当前依赖：`LogService extends CrudService<Log>`；`LogServiceImpl extends CrudServiceImpl<LogDao, Log>`；`LogDao extends CrudDao<Log>`；`LogMapper extends CrudDao<LogDO>`；`Log extends BaseLog extends AdminDataEntity<Log>`；`LogDO extends AdminDataEntity<LogDO>`；存在 `Log.Query <-> LogDO.Query`
  - 处理动作：收敛日志查询、批量插入和批量删除的持久化表达；拆除或替换本链路对通用查询契约、`DO.query` 和 `DO extends AdminDataEntity` 的依赖
  - 验收点：日志批量写入和删除 SQL 仍由 Mapper XML 承担；Service 不直接感知持久化实现对象

- [ ] `sys-dict-persistence`：改造字典持久化表达
  - 范围对象：`DictService`、`DictServiceImpl`、`DictDao`、`DictDaoImpl`、`DictMapper`、`mysql/dameng/kingbase/DictMapper.xml`、`DictDO`、`DictPersistenceAssembler`
  - 当前依赖：`DictService extends CrudService<Dict>`；`DictServiceImpl extends CrudServiceImpl<DictDao, Dict>`；`DictDao extends CrudDao<Dict>`；`DictMapper extends CrudDao<DictDO>`；`Dict extends BaseDict extends AdminDataEntity<Dict>`；`DictDO extends AdminDataEntity<DictDO>`；存在 `Dict.Query <-> DictDO.Query`
  - 处理动作：收敛字典查询、字典类型列表查询和排序/状态/删除标记更新的持久化表达；拆除或替换本链路对通用查询契约、`DO.query` 和 `DO extends AdminDataEntity` 的依赖
  - 验收点：`findTypeList` 仍由持久化层完成；DAO implementation 完成 Entity/DO 转换；字典链路不暴露 `DO`

- [ ] `sys-upload-file`：改造上传文件持久化表达
  - 范围对象：`UploadFileService`、`UploadFileServiceImpl`、`UploadFileDao`、`UploadFileDaoImpl`、`UploadFileMapper`、`mysql/dameng/UploadFileMapper.xml`、`UploadFileDO`、`UploadFilePersistenceAssembler`
  - 当前依赖：`UploadFileService extends CrudService<UploadFile>`；`UploadFileServiceImpl extends CrudServiceImpl<UploadFileDao, UploadFile>`；`UploadFileDao extends CrudDao<UploadFile>`；`UploadFileMapper extends CrudDao<UploadFileDO>`；`UploadFile extends BaseUploadFile extends AdminDataEntity<UploadFile>`；`UploadFileDO extends AdminDataEntity<UploadFileDO>`；当前无独立 `Query`；当前未发现 `kingbase/UploadFileMapper.xml`
  - 处理动作：收敛上传文件基础查询、文件内容查询和按文件 ID 批量查询的持久化表达；拆除或替换本链路对 `CrudDao` 通用契约和 `DO extends AdminDataEntity` 的依赖；确认是否需要补齐或明确不支持 Kingbase 上传文件 Mapper XML
  - 验收点：文件内容查询仍在 Mapper XML 层完成；Service 不感知 `DO`；Kingbase 方言缺口有明确处理结果

- [ ] `storage`：拆分存储域持久化表达改造任务
  - 范围对象：存储文件、存储业务绑定相关持久化链路
  - 处理动作：先横向盘点 `storage` 域每条持久化链路，按对象或清晰子链路拆出后续持久化表达改造 TODO，标明各自涉及的 `Service`、Dao interface、DaoImpl、Mapper、Mapper XML、`DO` 和 `PersistenceAssembler`，以及是否依赖通用查询契约、`DO.query`、`DO extends ...`
  - 验收点：存储域形成可逐条执行的持久化表达改造任务清单；本项不直接改造任何单条持久化链路代码

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
