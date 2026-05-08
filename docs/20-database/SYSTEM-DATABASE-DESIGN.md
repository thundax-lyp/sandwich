# SYSTEM DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich 后台系统管理域的数据库表、字段映射、关系约束和持久化规则。

本文档以 `SYSTEM-REQUIREMENTS.md` 的后台系统模型为基础，固定 `sys` 拥有的用户、角色、菜单、部门、字典和日志的目标持久化设计。登录标识、认证凭据、认证会话、访问 token、OAuth2 client、authorization、access token 和 refresh token 的数据库设计见 `AUTH-DATABASE-DESIGN.md`。

建表 SQL 见 [`../../db/schema/system.sql`](../../db/schema/system.sql)，初始化脚本见 [`../../db/data/system.sql`](../../db/data/system.sql)。

## 2. Scope

当前覆盖范围：

- `sys_user`
- `sys_role`
- `sys_menu`
- `sys_department`
- `sys_dict`
- `sys_log`
- `sys_user_role`
- `sys_role_menu`
- 对应 `DO/DataObject`
- 对应 MyBatis Mapper
- 对应 DAO implementation
- 对应 `PersistenceAssembler`

当前不覆盖范围：

- `auth_session`
- `auth_oauth_client`
- `auth_oauth_authorization`
- `auth_principal_identity`
- `auth_principal_credential`
- 前台会员表
- 生产数据变更脚本

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- 主体表主键数据库类型固定为 `bigint`，Java 类型固定为 `Long`。
- 主体表主键由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- 关系表使用来源主键作为联合关系字段，不单独生成关系 ID。
- `sys_user.email` 和 `sys_user.mobile` 使用持久化加密 typeHandler。
- `sys_user.ranks` 和 `sys_menu.ranks` 映射领域 `AccessRank rank`。
- `super_flag` 和 `admin_flag` 是数据库布尔标记字段，由持久化装配器转换为领域权限枚举。
- `sys_user.enable_flag` 和 `sys_role.enable_flag` 固定存储状态枚举名：`ENABLED` / `DISABLED`，默认值固定为 `ENABLED`。
- `sys_menu.display_flag` 固定存储 `MenuVisibility` 枚举名：`VISIBLE` / `HIDDEN`，默认值固定为 `VISIBLE`。
- `sys_menu.lft` / `sys_menu.rgt` 和 `sys_department.lft` / `sys_department.rgt` 是 nested-set 持久化索引。
- `Entity` 不暴露 `lft` / `rgt`。
- `create_date` / `create_by` / `update_date` / `update_by` 是通用审计字段，由 infra 统一填充。
- `del_flag` 是逻辑删除字段，`Entity` 与 `DO/DataObject` 不声明 `delFlag`。
- DAO get/list/page 查询固定追加 `del_flag = '0'` 条件。
- `DO/DataObject` 不暴露给 Controller 或 Service。

## 4. Naming Rules

- 后台用户表固定为 `sys_user`。
- 角色表固定为 `sys_role`。
- 菜单表固定为 `sys_menu`。
- 部门表固定为 `sys_department`。
- 字典表固定为 `sys_dict`。
- 日志表固定为 `sys_log`。
- 用户角色关系表固定为 `sys_user_role`。
- 角色菜单关系表固定为 `sys_role_menu`。
- 用户访问等级和菜单访问等级数据库列固定为 `ranks`。
- 菜单和部门树索引列固定为 `lft` 和 `rgt`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `sys_user` | `UserDO` | `UserMapper` | `User` |
| `sys_role` | `RoleDO` | `RoleMapper` | `Role` |
| `sys_menu` | `MenuDO` | `MenuMapper` | `Menu` |
| `sys_department` | `DepartmentDO` | `DepartmentMapper` | `Department` |
| `sys_dict` | `DictDO` | `DictMapper` | `Dict` |
| `sys_log` | `LogDO` | `LogMapper` | `Log` |
| `sys_user_role` | `UserRoleDO` | `UserRoleMapper` | relationship |
| `sys_role_menu` | `MenuRoleDO` | `MenuRoleMapper` | relationship |

## 6. Table Design

### 6.1 sys_user

`sys_user` 保存后台用户主体资料。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 用户主键 |
| `department_id` | `departmentId` | `departmentId` | 否 | 所属部门 ID |
| `email` | `email` | `email` | 否 | 加密邮箱 |
| `mobile` | `mobile` | `mobile` | 否 | 加密手机号 |
| `tel` | `tel` | `tel` | 否 | 联系电话 |
| `name` | `name` | `name` | 是 | 用户名称 |
| `ranks` | `ranks` | `rank` | 是 | 访问等级 |
| `super_flag` | `superFlag` | `privilege` | 是 | 超级管理员标记 |
| `admin_flag` | `adminFlag` | `privilege` | 是 | 管理员标记 |
| `enable_flag` | `enableFlag` | `status` | 是 | 启用状态 |
| `priority` | `priority` | `priority` | 是 | 排序值 |
| `remarks` | `remarks` | `remarks` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `email` 和 `mobile` 使用 `DefaultEncryptTypeHandler`。
- `ranks` 通过 `AccessRankCodec` 与 `AccessRank` 转换。
- `super_flag` 和 `admin_flag` 共同转换为 `UserPrivilege`。
- `enable_flag` 转换为 `UserStatus`。
- 注册、登录行为数据不落在 `sys_user`。

索引：

- 主键：`pk_sys_user(id)`
- 普通索引：`idx_sys_user_department(department_id)`
- 普通索引：`idx_sys_user_status(enable_flag, priority, create_date)`

### 6.2 sys_role

`sys_role` 保存后台角色。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 角色主键 |
| `name` | `name` | `name` | 是 | 角色名称 |
| `admin_flag` | `adminFlag` | `privilege` | 是 | 管理员角色标记 |
| `enable_flag` | `enableFlag` | `status` | 是 | 启用状态 |
| `priority` | `priority` | `priority` | 是 | 排序值 |
| `remarks` | `remarks` | `remarks` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。

索引：

- 主键：`pk_sys_role(id)`
- 普通索引：`idx_sys_role_status(enable_flag, priority, create_date)`

### 6.3 sys_menu

`sys_menu` 保存后台菜单和权限资源。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 菜单主键 |
| `parent_id` | `parentId` | `parentId` | 否 | 父菜单 ID |
| `lft` | `lft` | - | 是 | nested-set 左索引 |
| `rgt` | `rgt` | - | 是 | nested-set 右索引 |
| `name` | `name` | `name` | 是 | 菜单名称 |
| `perms` | `perms` | `perms` | 否 | 权限编码 |
| `ranks` | `ranks` | `rank` | 是 | 访问等级 |
| `display_flag` | `displayFlag` | `visibility` | 是 | 显示状态，取值固定为 `VISIBLE` / `HIDDEN` |
| `display_params` | `displayParams` | `displayParams` | 否 | 显示参数 |
| `url` | `url` | `url` | 否 | 访问路径 |
| `target` | `target` | `target` | 否 | 打开目标 |
| `priority` | `priority` | `priority` | 是 | 排序值 |
| `remarks` | `remarks` | `remarks` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `parent_id` 复用 `sys_menu.id`。

索引：

- 主键：`pk_sys_menu(id)`
- 普通索引：`idx_sys_menu_parent(parent_id, priority)`
- 普通索引：`idx_sys_menu_nested(lft, rgt)`
- 普通索引：`idx_sys_menu_display(display_flag, ranks)`

### 6.4 sys_department

`sys_department` 保存后台部门树。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 部门主键 |
| `parent_id` | `parentId` | `parentId` | 否 | 父部门 ID |
| `lft` | `lft` | - | 是 | nested-set 左索引 |
| `rgt` | `rgt` | - | 是 | nested-set 右索引 |
| `name` | `name` | `name` | 是 | 部门名称 |
| `short_name` | `shortName` | `shortName` | 否 | 部门简称 |
| `priority` | `priority` | `priority` | 是 | 排序值 |
| `remarks` | `remarks` | `remarks` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `parent_id` 复用 `sys_department.id`。

索引：

- 主键：`pk_sys_department(id)`
- 普通索引：`idx_sys_department_parent(parent_id, priority)`
- 普通索引：`idx_sys_department_nested(lft, rgt)`

### 6.5 sys_dict

`sys_dict` 保存系统字典项。

系统枚举类字典固定作为 Java enum 的展示镜像，承载前端筛选、下拉、标签和说明展示所需的 `label`、`priority` 和 `remarks`。
业务合法值校验仍以代码中的 Java enum 为准，`sys_dict` 不作为业务状态和值域的权威来源。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 字典项主键 |
| `type` | `type` | `type` | 是 | 字典类型 |
| `label` | `label` | `label` | 是 | 显示标签 |
| `value` | `value` | `value` | 是 | 字典值 |
| `priority` | `priority` | `priority` | 是 | 排序值 |
| `remarks` | `remarks` | `remarks` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `type` / `value` 是字典业务键，不作为数据库主键。

索引：

- 主键：`pk_sys_dict(id)`
- 普通索引：`idx_sys_dict_type(type, priority, create_date)`

### 6.6 sys_log

`sys_log` 保存后台系统日志。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 日志主键 |
| `user_id` | `userId` | `userId` | 否 | 操作用户 ID |
| `type` | `type` | `type` | 否 | 日志类型 |
| `log_date` | `logDate` | `logDate` | 是 | 日志发生时间 |
| `title` | `title` | `title` | 否 | 日志标题 |
| `remote_addr` | `remoteAddr` | `remoteAddr` | 否 | 远端地址 |
| `user_agent` | `userAgent` | `userAgent` | 否 | 客户端信息 |
| `method` | `method` | `method` | 否 | 请求方法 |
| `request_uri` | `requestUri` | `requestUri` | 否 | 请求路径 |
| `request_params` | `requestParams` | `requestParams` | 否 | 请求参数摘要 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `user_id` 复用 `sys_user.id`。

索引：

- 主键：`pk_sys_log(id)`
- 普通索引：`idx_sys_log_date(log_date)`
- 普通索引：`idx_sys_log_user(user_id, log_date)`
- 普通索引：`idx_sys_log_type(type, log_date)`

### 6.7 sys_user_role

`sys_user_role` 保存用户和角色关系。

| Column | DO Field | Required | Description |
| --- | --- | --- | --- |
| `user_id` | `userId` | 是 | 用户 ID |
| `role_id` | `roleId` | 是 | 角色 ID |

字段规则：

- `user_id` 复用 `sys_user.id`。
- `role_id` 复用 `sys_role.id`。
- 关系表使用联合主键，不单独生成 `id`。

索引：

- 联合唯一索引：`uk_sys_user_role(user_id, role_id)`
- 普通索引：`idx_sys_user_role_role(role_id)`

### 6.8 sys_role_menu

`sys_role_menu` 保存角色和菜单关系。

| Column | DO Field | Required | Description |
| --- | --- | --- | --- |
| `role_id` | `roleId` | 是 | 角色 ID |
| `menu_id` | `menuId` | 是 | 菜单 ID |

字段规则：

- `role_id` 复用 `sys_role.id`。
- `menu_id` 复用 `sys_menu.id`。
- 关系表使用联合主键，不单独生成 `id`。

索引：

- 联合唯一索引：`uk_sys_role_menu(role_id, menu_id)`
- 普通索引：`idx_sys_role_menu_menu(menu_id)`

## 7. Relationship Rules

- `sys_user.department_id` 引用 `sys_department.id`。
- `sys_user_role.user_id` 引用 `sys_user.id`。
- `sys_user_role.role_id` 引用 `sys_role.id`。
- `sys_role_menu.role_id` 引用 `sys_role.id`。
- `sys_role_menu.menu_id` 引用 `sys_menu.id`。
- `sys_menu.parent_id` 引用 `sys_menu.id`。
- `sys_department.parent_id` 引用 `sys_department.id`。
- 当前项目不强制数据库外键。
- 用户角色关系由 Service 重写。
- 角色菜单关系由 Service 重写。
- 菜单和部门树移动由 infra DAO implementation 维护 `parent_id`、`lft` 和 `rgt`。

## 8. Persistence Rules

- Mapper interface 固定继承 `BaseMapper<DO>`。
- Mapper interface 不新增注解 SQL、Mapper XML 或 SQL Provider。
- DAO implementation 固定通过 MyBatis-Plus wrapper 构造查询、更新和删除。
- `PersistenceAssembler` 只负责 `Entity <-> DO/DataObject` 转换。
- `PersistenceAssembler` 不调用 Service、DAO 或 Mapper。
- `UserDaoImpl` 负责维护 `sys_user_role`。
- `RoleDaoImpl` 负责维护 `sys_user_role` 和 `sys_role_menu`。
- `MenuDaoImpl` 负责维护菜单树索引和清理 `sys_role_menu`。
- `DepartmentDaoImpl` 负责维护部门树索引。
- `DictDaoImpl` 负责字典修订号来源数据。
- `LogDaoImpl` 负责日志批量插入和批量清理。
- 用户、角色、菜单、部门和字典缓存由对应 cache support 维护。

## 9. Query Model Rules

用户查询支持：

- `departmentId`
- `loginName`
- `name`
- `status`
- `privilege`

角色查询支持：

- `status`

菜单查询支持：

- `parentId`
- `visibility`
- `maxRank`

部门查询支持：

- `parentId`
- `name`
- `remarks`

字典查询支持：

- `type`
- `label`
- `remarks`

日志查询支持：

- `type`
- `remoteAddr`
- `title`
- `requestUri`
- `userLoginName`
- `userName`
- `beginDate`
- `endDate`

排序规则：

- 用户默认按 `priority`、`create_date` 升序。
- 角色默认按 `priority`、`create_date` 升序。
- 菜单默认按 `lft` 升序。
- 部门默认按 `lft` 升序。
- 字典默认按 `type`、`priority`、`create_date` 升序。
- 日志默认按 `log_date` 降序。

## 10. Open Items

无
