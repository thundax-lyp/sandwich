# HOW TO REMODEL DOMAIN ENTITY

## 1. Purpose

本文档说明：当需要清理 `BaseEntity`、`DataEntity`、`AdminDataEntity`、`TreeEntity`、`AdminTreeEntity` 等旧式实体基类时，如何按 domain 语义重新设计业务 `Entity`。

目标是删除以表字段和接口序列化为中心的继承链，让 `sandwish-biz` 中的 `Entity` 表达 Service 可使用的业务对象，而不是复用持久化字段、后台审计字段或 JSON 输出形态。

## 2. Scope

当前范围：

- `sandwish-common-core` 中领域实体根 `com.github.thundax.common.domain.Entity`
- `sandwish-common-mybatis` 中旧持久化实体基类
- `sandwish-biz` 中业务 `Entity` 和业务侧 base 类
- `sandwish-admin-api`、`sandwish-front-api` 中 API `Request`、`Response`、`VO` 和 `InterfaceAssembler`
- `sandwish-infra` 中 `DO/DataObject`、审计字段和 `PersistenceAssembler`

不在范围内：

- 不重新设计数据库表结构
- 不引入 `domain/application/repository/facade` 等额外分层目录
- 不把旧基类替换成新的通用业务实体父类
- 不要求一次提交完成全部实体迁移

## 3. Bounded Context

Sandwich 固定采用三层 API 架构：

`HTTP/API -> Controller -> Service -> DAO/Mapper -> Database`

本任务中的 domain 指 `sandwish-biz` 内 Service 使用的业务 `Entity`，不是额外新增的 `domain` 分层目录。

领域实体固定表达业务身份、业务属性和业务行为。以下内容不得作为领域实体的默认语言：

- 数据库审计字段的统一父类抽象
- API JSON 序列化注解
- Controller 输出字段裁剪规则
- MyBatis-Plus 持久化对象职责

## 4. When To Use

- 准备删除 `BaseEntity`、`DataEntity` 或 `AdminDataEntity`
- 准备删除或改造 `TreeEntity`、`AdminTreeEntity`
- 发现业务 `Entity` 继承了只表达表字段的公共父类
- 发现业务 `Entity` 中存在 Jackson 注解或 API 输出控制逻辑
- 需要判断 `priority`、`remarks`、`createDate`、`updateDate`、`createUserId`、`updateUserId` 应该迁移到哪里

## 5. Do Not Use For

- 只改 `DO/DataObject` 字段映射
- 只新增一个普通业务字段
- 只调整 API `Response` 字段展示
- 只处理数据库审计拦截器
- 只做格式化或 import 清理

## 6. Pre-Checks

开始迁移前固定先完成以下检查：

1. 读取 `docs/00-governance/ARCHITECTURE.md`。
2. 读取 `docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`。
3. 涉及 `DO/DataObject`、审计字段或持久化转换时，读取 `docs/00-governance/DATABASE-RULES.md`。
4. 用 `rg` 列出旧基类继承点和 Jackson 注解引用点。
5. 判断当前改动是否可以拆成一个可编译的小步提交。

推荐盘点命令：

```bash
rg "extends (BaseEntity|DataEntity|AdminDataEntity|TreeEntity|AdminTreeEntity)" -n
rg "com\\.fasterxml\\.jackson|@Json" -n sandwish-biz/src/main/java
```

## 7. Steps

### 7.1 Fix The Target Model

先固定目标模型，再开始移动字段。

- `common.domain.Entity` 只表达领域身份
- 业务 `Entity` 只表达业务属性和业务行为
- `DO/DataObject` 只表达持久化字段
- `Request`、`Response`、`VO` 只表达 API 输入输出形态
- `InterfaceAssembler` 负责 API 模型和业务 `Entity` 之间的显式转换
- `PersistenceAssembler` 负责业务 `Entity` 和 `DO/DataObject` 之间的显式转换

不得新增 `DomainEntity`、`AuditEntity`、`AdminEntity`、`BaseBizEntity` 等替代型通用父类。

### 7.2 Move Identity To Domain Entity Root

`common.domain.Entity` 可以承载领域身份的最小能力：

- `EntityId id`
- `getEntityId`
- `setEntityId`
- 同类且 `EntityId` 相同的相等性判断

`String id` 到 `EntityId` 的转换不得继续依赖持久化包父类。需要从字符串构造实体时，由具体实体构造器显式调用 `EntityIdCodec`。

### 7.3 Classify Former DataEntity Fields

对 `DataEntity` 中字段逐个按领域语义归类。

`priority` 固定按具体对象语义迁移：

- 有排序业务含义：迁移到具体业务 `Entity`
- 没有排序业务含义：不迁移到业务 `Entity`
- API 仍需展示排序值：由对应 `Response` 承接

允许定义排序能力接口，但它只能表达业务排序能力，不得变成新的排序字段父类。

排序能力接口固定满足以下规则：

- 只能是 interface
- 归属 `sandwish-biz`
- 不继承 `Entity`
- 不声明字段
- 不依赖 Jackson、MyBatis 或 Spring Web
- 不替代具体业务对象对排序语义的命名和校验

允许进入排序能力接口的方法固定为业务排序视角：

- `getPriority`
- `setPriority`

不得把排序能力接口用于以下场景：

- 只是为了让所有业务实体都有 `priority`
- 只是为了复用 `DataEntity` 的旧字段结构
- 只是为了让 API 统一展示排序字段
- 只是为了减少 `InterfaceAssembler` 或 `PersistenceAssembler` 显式转换代码

`remarks` 固定按具体对象语义迁移：

- 有业务备注、说明或描述含义：迁移到具体业务 `Entity`
- 只是历史表字段占位：不迁移到业务 `Entity`
- API 查询需要备注条件：使用具体查询参数或实体查询对象表达

`remarks` 不默认归入审计能力。审计字段固定回答“谁在什么时候创建或修改了对象”，普通备注字段回答“对象本身有什么说明”。

当备注表达操作审计语义时，必须使用具体业务名称承接：

- 变更原因使用 `changeReason`
- 审批意见使用 `approvalComment`
- 操作备注使用 `operationRemark`
- 审计日志内容使用 `content`、`message` 或当前日志对象已有字段

不得把 `remarks` 放进 `Auditable`，也不得把 `Auditable` 扩展成通用备注承载接口。

`createDate`、`updateDate` 固定按生命周期语义迁移：

- 只是数据库审计字段：保留在 `DO/DataObject` 和 infra 审计填充链路
- API 需要返回：放在 `Response` 或 `VO`
- 具有明确业务时间含义：放在具体业务 `Entity`，并使用业务名称，例如 `registerDate`、`lastLoginDate`

### 7.4 Classify Former AdminDataEntity Fields

对 `AdminDataEntity` 中字段逐个按业务语义归类。

`createUserId`、`updateUserId` 固定按以下规则处理：

- 只是持久化审计：保留在 infra `DO/DataObject` 的 `createBy`、`updateBy`
- 参与业务展示、权限、签名或流程判断：迁移到具体业务 `Entity`
- Service 不预填纯持久化审计字段

`Signable` 固定作为业务能力接口处理：

- 需要签名的实体自己 `implements Signable`
- `getSignId` 由具体实体显式实现
- `getSignName`、`getSignBody` 由具有签名语义的实体定义
- 不通过 `AdminDataEntity` 提供默认签名实现

### 7.5 Define Auditable Only As A Domain Capability

允许定义 `Auditable`，但它只能表达业务可审计能力，不得变成新的审计字段父类。

`Auditable` 固定满足以下规则：

- 只能是 interface
- 归属 `sandwish-biz`
- 不继承 `Entity`
- 不声明字段
- 不依赖 Jackson、MyBatis、Spring Web 或当前登录态
- 不触发持久化审计填充
- 不替代 `DO/DataObject` 的 `createBy`、`updateBy`、`createDate`、`updateDate`

允许进入 `Auditable` 的方法固定为业务审计视角：

- `getCreateUserId`
- `setCreateUserId`
- `getUpdateUserId`
- `setUpdateUserId`

`createDate`、`updateDate` 不默认进入 `Auditable`。只有当某个业务对象把创建时间、修改时间作为业务生命周期的一部分使用时，才在具体业务 `Entity` 中以业务语义保留。

不得把 `Auditable` 用于以下场景：

- 只是为了让 infra 拦截器自动填充审计字段
- 只是为了让 API 统一返回审计字段
- 只是为了减少 `PersistenceAssembler` 显式转换代码
- 只是为了复用 `AdminDataEntity` 的旧字段结构

当业务对象实现 `Auditable` 时，`PersistenceAssembler` 仍必须显式完成：

- `DO.createBy` 与 `Entity.createUserId` 的转换
- `DO.updateBy` 与 `Entity.updateUserId` 的转换

### 7.6 Remove Jackson From Domain Entity

`sandwish-biz` 的业务 `Entity` 不得依赖 Jackson 注解。

必须从业务 `Entity` 删除：

- `JsonInclude`
- `JsonIgnoreProperties`
- `JsonIgnore`
- `JsonFormat`
- `JsonProperty`

迁移规则：

- JSON 字段名放在 `Request`、`Response`、`VO`
- JSON 日期格式放在 `Response`、`VO`
- 字段是否输出由 `InterfaceAssembler` 决定
- 防止循环序列化不得作为保留 `JsonIgnore` 的理由，出现该问题时应停止直接序列化业务 `Entity`

第三方接口参数对象如果需要 Jackson 注解，必须放在明确的 adapter、client、plugin DTO 或 API 模型语义下，不得混入业务 `Entity` 语义。

### 7.7 Replace Generic Copy Helpers

删除 `DataEntity` 后，入口层不得继续使用基于旧父类的通用字段拷贝。

固定处理方式：

- `InterfaceAssembler` 显式转换每个 API 字段
- `Response` 需要 `createDate`、`updateDate` 时，由 assembler 从查询结果或业务对象显式赋值
- `Request` 到 `Entity` 的转换只设置业务允许由入口输入的字段
- Controller 不直接返回业务 `Entity`

### 7.8 Migrate Tree Semantics

树结构不得依赖 `TreeEntity` 或 `AdminTreeEntity` 承载通用持久化语义。

固定处理方式：

- `parentId` 是业务关系字段，可以存在于具体业务 `Entity`
- `toTreeData` 这类 API 输出形态迁移到 `InterfaceAssembler` 或 `Response`
- `lft`、`rgt` 只保留在 infra 持久化实现中
- `Menu`、`Office` 等树对象按各自业务语义保留必要字段和行为

### 7.9 Delete Old Bases In Small Steps

删除旧基类固定按可编译小步执行：

1. 先让 `common.domain.Entity` 承接身份最小能力。
2. 再迁移一个业务对象族的字段和 Jackson 注解。
3. 再更新对应 `InterfaceAssembler`、`Response` 和 `PersistenceAssembler`。
4. 再删除该对象族对旧基类的继承。
5. 最后删除没有引用的旧基类文件。

每一步完成后必须保持项目处于可编译状态。

## 8. Files To Touch

通常需要修改：

- `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/domain/Entity.java`
- `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/*.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/*/entity/**/*.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/*/{request,response,vo,assembler}/**/*.java`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/*/{request,response,vo,assembler}/**/*.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/*/persistence/{dataobject,assembler}/**/*.java`
- `sandwish-biz/src/main/java/com/github/thundax/common/**/Auditable.java`
- `sandwish-biz/src/main/java/com/github/thundax/common/**/Prioritized.java`
- 对应架构测试

通常不应修改：

- Controller 业务流程，除非它直接返回了业务 `Entity`
- Service 事务边界，除非旧字段迁移暴露出错误职责
- 数据库 schema，除非另有明确数据库任务
- Maven 模块结构

## 9. Common Mistakes

- 删除 `DataEntity` 后新增另一个等价的通用业务父类
- 把 `Auditable` 做成带字段的抽象类
- 把排序能力接口做成带字段的抽象类
- 把 `createDate`、`updateDate` 机械搬进所有业务 `Entity`
- 为了保留 JSON 输出效果，在 domain 中继续使用 Jackson 注解
- 让 `Response` 继承业务 `Entity`
- 让 `InterfaceAssembler` 调 Service 或 DAO 补字段
- 把 `DO/DataObject` 暴露给 Service 或 Controller
- 一次性迁移所有实体，导致编译错误和行为变化混在一起

## 10. Verification

每个小步提交前固定检查：

1. `sandwish-biz` 的业务 `Entity` 是否仍引用 `com.fasterxml.jackson.annotation`。
2. 当前迁移对象是否仍继承旧基类。
3. API 是否仍通过 `Request`、`Response` 或 `VO` 表达 JSON 形态。
4. `PersistenceAssembler` 是否仍显式转换 `Entity <-> DO/DataObject`。
5. Service 是否仍不感知 `DO/DataObject`。
6. Controller 是否仍不直接返回业务 `Entity`。

推荐测试范围：

```bash
mvn -q -pl sandwish-common/sandwish-common-core,sandwish-common/sandwish-common-mybatis,sandwish-biz -am test
mvn -q -pl sandwish-admin-api -am test
```

只修改某个业务对象族时，可以先运行对应模块或对应测试类，但收口前必须说明未运行的测试范围。

## 11. Commit Guidance

本任务必须拆成小步提交。每个 commit 固定只完成一个可解释判断。

推荐提交边界：

- `Refactor(common): 收敛领域实体身份能力`
- `Refactor(sys): 下沉系统实体领域字段`
- `Refactor(admin): 移除实体JSON序列化依赖`
- `Refactor(infra): 显式转换实体审计字段`
- `Test(architecture): 禁止领域实体依赖Jackson`

不得把以下内容混在同一个 commit：

- 多个无关业务域的字段语义迁移
- 删除旧基类和大面积 API 响应重塑
- 纯架构测试和未完成的业务迁移

## 12. Open Items

无
