# TREE PERSISTENCE BOUNDARY RUNBOOK

## 1. Purpose

本文档指导将树结构的嵌套集索引从业务实体中剥离，并迁入 infra 持久化实现。

目标是保留 `parentId` 表达业务父子关系，同时将 `lft` / `rgt` 固定为持久化层维护的 nested-set index，避免持久化优化字段继续泄漏到 domain / Service 公共契约中。

本手册必须在 `common` 分拆和 MyBatis-Plus 引入之前执行。

## 2. Scope

当前范围：

- `Office` / 机构树
- `Menu` / 菜单树
- `TreeEntity`
- `AdminTreeEntity`
- `TreeService`
- `TreeServiceImpl`
- `TreeDao`
- `OfficeDao` / `MenuDao`
- `OfficeDO` / `MenuDO`
- `OfficeMapper` / `MenuMapper`
- 达梦 `OfficeMapper.xml` / `MenuMapper.xml`

不在范围内：

- 不改变树业务能力
- 不删除 `parentId`
- 不引入 MyBatis-Plus
- 不删除 Mapper XML
- 不改变 API 入参和出参
- 不重新设计为闭包表、路径枚举或递归查询

## 3. Fixed Boundary

固定口径：

- `parentId` 是业务树关系字段，允许存在于 Entity、Request、Response、Service 参数中
- `lft` / `rgt` 是 nested-set persistence index，只允许存在于 DO、Mapper、infra 树持久化实现中
- Service 表达业务动作，不计算、不读取、不写入 `lft` / `rgt`
- infra 负责新增、移动、删除时维护 `lft` / `rgt`
- 达梦现有树 SQL 语义是迁移前事实标准

禁止：

- Entity 暴露 `getLft()` / `getRgt()`
- Service 依赖 `treeSpan()`、`hasChild()` 或 `isChildOf()` 计算嵌套集区间
- DAO interface 要求业务实体作为 `@Param("node")` 暴露嵌套集字段
- Controller / API Response 暴露 `lft` / `rgt`

## 4. Target Responsibilities

### 4.1 Entity

Entity 固定表达业务树节点：

- `id`
- `parentId`
- `name`
- 业务字段

Entity 不表达：

- `lft`
- `rgt`
- nested-set span
- nested-set child 判断

### 4.2 Service

Service 固定表达业务动作：

- 新增树节点
- 更新树节点业务字段
- 删除树节点
- 移动树节点
- 查询直接子节点
- 查询树展示数据

Service 不负责：

- 计算插入区间
- 空出树区间
- 移动树区间
- 压缩删除后的树区间
- 读取最大 `rgt`

### 4.3 Infra

Infra 固定负责：

- 读取树持久化节点 `id`、`parentId`、`lft`、`rgt`
- 分配新增节点区间
- 移动子树区间
- 删除后压缩区间
- 更新直接父节点
- 保持 `parentId` 与 `lft` / `rgt` 一致

## 5. Execution Order

固定执行顺序：

1. 新增 infra 树持久化支撑对象或服务，承接现有 `TreeServiceImpl` 的 nested-set 算法
2. 先迁移 `Office` 树链路
3. 再迁移 `Menu` 树链路
4. 从 `Office` / `Menu` Entity 基类移除 `lft` / `rgt`
5. 收窄 `TreeEntity`，只保留业务树关系或删除该抽象
6. 收窄 `TreeDao`，移除嵌套集字段对业务实体的约束
7. 收窄 `TreeServiceImpl`，移除 nested-set 算法
8. 编译验证

## 6. First Refactor Unit

第一组只处理 `Office`。

允许：

- 保留现有 Mapper XML
- 保留达梦现有 SQL
- 新增 infra 内部树节点参数对象
- 新增 infra 内部 nested-set 维护组件
- 调整 `OfficeDaoImpl` 和 `OfficeMapper`

禁止：

- 同步改 `Menu`
- 同步引入 MyBatis-Plus
- 同步拆 common
- 同步删除 XML
- 同步改变 API 字段

## 7. Office Migration Rules

`Office` 迁移后必须满足：

- `Office` / `BaseOffice` 不再持有 `lft` / `rgt`
- `OfficeDO` 继续持有 `parentId`、`lft`、`rgt`
- `OfficeDao` 对 Service 暴露业务语义方法
- `OfficeDaoImpl` 调用 infra 树持久化组件维护 `lft` / `rgt`
- `OfficeMapper` 继续映射达梦 XML 中的树维护 SQL
- API 查询和保存仍使用 `parentId`

## 8. Menu Migration Rules

`Menu` 迁移后必须满足：

- `Menu` / `BaseMenu` 不再持有 `lft` / `rgt`
- `Menu.compareTo` 不再依赖 `lft`
- 菜单树展示排序改为明确业务排序或 infra 返回顺序
- `MenuDO` 继续持有 `parentId`、`lft`、`rgt`
- `MenuDao` 不再继承需要实体暴露嵌套集字段的公共 `TreeDao`
- 角色菜单关系删除语义不变

## 9. Verification

每个迁移单元完成后固定验证：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

检查项：

- `rg "getLft|getRgt|setLft|setRgt" sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity` 无业务实体命中
- `OfficeDO` / `MenuDO` 仍保留 `lft` / `rgt`
- Service 层不再调用 nested-set 区间方法
- API 入参出参未新增 `lft` / `rgt`
- 现有 Mapper XML 仍可加载

## 10. Completion Criteria

本迁移完成必须满足：

- `lft` / `rgt` 已从业务实体边界移除
- nested-set 算法归属 infra
- `parentId` 继续作为业务树关系字段
- `TreeServiceImpl` 不再承载持久化区间算法
- `TreeDao` 不再要求业务实体暴露 `lft` / `rgt`
- 后续 common 分拆和 MyBatis-Plus 引入不会固化旧边界

## 11. Open Items

无
