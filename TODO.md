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

- [ ] `assist-async-task-api`：隔离异步任务 API 模型
  - 范围入口：`AsyncTaskApiController`
  - 当前 API 模型泄漏点：入口直接使用 `AsyncTaskVo`；Controller 内存在 `AsyncTask` 到 API 模型转换
  - 需要新增或收窄的 `Request`：异步任务详情请求
  - 需要新增或收窄的 `Response`：异步任务详情响应
  - 需要新增的 `InterfaceAssembler`：`AsyncTaskInterfaceAssembler`
  - Service 方法签名是否需要调整：当前不调整；Service 继续使用 `AsyncTask`
  - 验收命令：`mvn -pl sandwish-admin-api -am -DskipTests package`

- [ ] `assist-storage-entry`：明确存储入口模型隔离边界
  - 范围入口：`StorageController`
  - 当前 API 模型泄漏点：入口混合服务端视图返回、`HttpServletRequest` 查询读取、`StorageVo` 上传测试响应和 `Storage` 查询对象组装
  - 需要新增或收窄的 `Request`：上传测试、列表查询、删除请求；服务端页面入口继续保留时，必须单独标明不是核心 API 模型隔离目标
  - 需要新增或收窄的 `Response`：上传响应、树数据响应、预览/删除结果响应
  - 需要新增的 `InterfaceAssembler`：`StorageInterfaceAssembler`
  - Service 方法签名是否需要调整：当前不调整；页面入口继续直接使用 `HttpServletRequest` 组装查询时，先拆出 API 与页面/支撑入口边界
  - 验收命令：`mvn -pl sandwish-admin-api -am -DskipTests package`

- [ ] `front-member-entry`：明确前台会员登录入口模型隔离边界
  - 范围入口：`LoginController`、`LogoutController`
  - 当前 API 模型泄漏点：入口主要返回字符串视图/状态并直接使用 `HttpServletRequest`、`HttpServletResponse`、`Model`；当前未通过 Service 暴露业务 `Entity`
  - 需要新增或收窄的 `Request`：登录检查、退出登录请求；页面入口继续保留时，先明确是否迁移为 API Request
  - 需要新增或收窄的 `Response`：登录状态响应、退出结果响应；页面入口继续保留时，先明确响应模型边界
  - 需要新增的 `InterfaceAssembler`：改造为 API 响应模型时新增 `MemberLoginInterfaceAssembler`
  - Service 方法签名是否需要调整：当前无明确 Service 调用，本项不调整 Service
  - 验收命令：`mvn -pl sandwish-front-api -am -DskipTests package`

- [ ] `legacy-support-controllers`：明确非业务 API Controller 的处理口径
  - 范围入口：`TagController`、已注释的 `UploadController`
  - 当前 API 模型泄漏点：`TagController` 返回服务端视图；`UploadController` 当前整体注释，包含旧上传和文件输出逻辑
  - 需要新增或收窄的 `Request`：无；先判断保留、迁移到静态支撑、删除或另行拆任务
  - 需要新增或收窄的 `Response`：无；转为 API 入口时再新增响应模型
  - 需要新增的 `InterfaceAssembler`：无；后续确认转为业务 API 时再新增
  - Service 方法签名是否需要调整：当前不调整
  - 验收命令：`mvn -pl sandwish-admin-api -am -DskipTests package`

- [ ] `docs/30-designs`：模型隔离迁移收尾
  - 范围对象：`INFRA-SPLIT-RUNBOOK.md`、`MODEL-SEPARATION-RUNBOOK.md`、`TODO.md`、治理文档
  - 处理动作：两套迁移完成后删除临时手册；删除或收窄已完成 TODO；将稳定规则沉淀到治理文档
  - 验收点：临时手册不再保留，完成历史只存在于 commit / PR 中

## P0 - 持久化表达改造

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
