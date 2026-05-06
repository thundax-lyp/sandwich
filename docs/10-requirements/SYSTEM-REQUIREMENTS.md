# SYSTEM REQUIREMENTS

## 1. Purpose

本文档定义 Sandwich 后台系统管理域的业务需求边界。

`System` 对应代码中的 `com.github.thundax.modules.sys`，负责后台用户主体、登录标识、认证凭据、角色、菜单、部门、字典和系统日志。认证登录流程由 `AUTH-REQUIREMENTS.md` 定义；`System` 只提供认证所需的用户资料、身份资料和凭据资料。

## 2. Scope

当前覆盖范围：

- 后台用户主体管理。
- 后台用户登录标识管理。
- 后台用户认证凭据管理。
- 角色管理。
- 角色用户关系维护。
- 角色菜单关系维护。
- 菜单树、权限编码和访问等级管理。
- 部门树管理。
- 字典管理。
- 系统日志记录、查询和批量清理。

当前不覆盖范围：

- 后台登录、token、OAuth2 和认证会话流程。
- 前台会员体系。
- 多租户组织模型。
- 服务端页面和标签库。
- 生产数据变更脚本。

## 3. Bounded Context

`System` 是后台管理基础域，承载管理端的组织、账号、授权资料和系统基础配置。

后台认证链路中：

- `User` 是后台用户主体。
- `UserIdentity` 是后台登录标识。
- `UserCredential` 是后台认证凭据。
- `AuthSession`、`AccessToken`、`OAuthAccessToken` 和 `OAuthRefreshToken` 归属 `Auth`。

后台授权链路中：

- `Role` 表达授权角色。
- `Menu` 表达菜单、权限编码和访问等级。
- `UserRole` 表达用户拥有的角色。
- `RoleMenu` 表达角色拥有的菜单权限。

树结构中：

- `Department.parentId` 和 `Menu.parentId` 表达业务父子关系。
- `lft` / `rgt` 是持久化 nested-set 索引，只存在于 `DO/DataObject`、Mapper 和 infra DAO implementation。

## 4. Module Mapping

- `sandwish-biz/src/main/java/com/github/thundax/modules/sys`
  - 定义 `User`、`UserIdentity`、`UserCredential`、`Role`、`Menu`、`Department`、`Dict`、`Log`、枚举、值对象、DAO interface、Service 和查询对象。
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys`
  - 实现 sys DAO，维护 DO、Mapper、缓存和持久化装配器。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys`
  - 提供后台系统管理 Controller、Request、Response、InterfaceAssembler、系统日志切面和入口工具。

## 5. Core Business Objects

### 5.1 User

`User` 是后台用户主体。

核心字段：

- `id`：后台用户 ID，使用 `EntityId`。
- `departmentId`：所属部门 ID。
- `email`：联系邮箱。
- `mobile`：联系手机号。
- `tel`：联系电话。
- `name`：用户名称。
- `rank`：用户访问等级，使用 `AccessRank`；持久化到 `sys_user.ranks`。
- `privilege`：用户权限等级。
- `status`：用户状态。
- `priority`：排序值。
- `remarks`：备注。
- `createDate` / `createUserId`：创建审计信息。
- `updateDate` / `updateUserId`：更新审计信息。

固定约束：

- `User` 不承载角色列表；用户角色通过 `sys_user_role` 动态读取。
- `User` 不承载认证行为数据，例如注册 IP、最近登录时间、最近登录 IP 和登录次数。
- `User.loginName` 只作为用户保存和修改账号的输入，正式登录标识由 `UserIdentity.identityValue` 承载。
- `User.loginPass` 只作为用户保存和重置密码的输入，正式认证凭据由 `UserCredential.credentialValue` 承载。
- `User.status = DISABLED` 时，该后台用户全部后台登录方式不可用。

### 5.2 UserIdentity

`UserIdentity` 是后台用户登录标识。

核心字段：

- `id`：登录标识 ID。
- `userId`：后台用户 ID。
- `identityType`：登录标识类型。
- `identityValue`：登录标识值。
- `status`：登录标识状态。

固定标识类型：

- `ACCOUNT`
- `MOBILE`
- `EMAIL`

固定状态：

- `ENABLED`
- `DISABLED`

固定约束：

- `identityType + identityValue` 必须唯一定位一个登录标识。
- 修改后台账号固定更新 `ACCOUNT` 类型 `UserIdentity`。
- `UserIdentity` 不保存密码哈希。
- `UserIdentity` 不承载通用审计字段。

### 5.3 UserCredential

`UserCredential` 是后台用户认证凭据。

核心字段：

- `id`：认证凭据 ID。
- `userId`：后台用户 ID。
- `identityId`：登录标识 ID。
- `credentialType`：凭据类型。
- `credentialValue`：凭据值。
- `status`：凭据状态。
- `needChangePassword`：是否需要强制改密。
- `failedCount`：连续失败次数。
- `failedLimit`：最大允许连续失败次数。
- `lockedUntil`：锁定截止时间。
- `expiresAt`：过期时间。
- `lastVerifiedAt`：最近验证时间。

固定凭据类型：

- `PASSWORD`

固定状态：

- `ACTIVE`
- `LOCKED`
- `EXPIRED`
- `DISABLED`

固定约束：

- 密码哈希固定保存到 `credentialValue`。
- 登录失败次数和锁定状态固定保存在 `UserCredential` 维度。
- `identityId + credentialType` 必须唯一定位一个认证凭据。
- `UserCredential` 不承载通用审计字段。

### 5.4 Role

`Role` 是后台授权角色。

核心字段：

- `id`：角色 ID。
- `name`：角色名称。
- `privilege`：角色权限等级。
- `status`：角色状态。
- `priority`：排序值。
- `remarks`：备注。
- `menuIdList`：角色授权菜单 ID 列表。
- `createDate` / `createUserId`：创建审计信息。
- `updateDate` / `updateUserId`：更新审计信息。

固定约束：

- `Role.menuIdList` 是 Service 编排关系时使用的业务字段，不落在 `sys_role` 主表。
- 角色与菜单关系通过 `sys_role_menu` 维护。
- 用户与角色关系通过 `sys_user_role` 维护。

### 5.5 Menu

`Menu` 是后台菜单和权限资源。

核心字段：

- `id`：菜单 ID。
- `parentId`：父菜单 ID。
- `name`：菜单名称。
- `perms`：权限编码，多个编码使用逗号分隔。
- `rank`：访问等级，使用 `AccessRank`；持久化到 `sys_menu.ranks`。
- `visibility`：显示状态。
- `displayParams`：显示参数 JSON。
- `url`：访问路径。
- `target`：打开目标。
- `priority`：排序值。
- `remarks`：备注。
- `createDate` / `createUserId`：创建审计信息。
- `updateDate` / `updateUserId`：更新审计信息。

固定约束：

- 菜单树通过 `parentId` 表达业务父子关系。
- 权限判断读取 `perms` 和角色菜单关系。
- 访问等级使用统一 `AccessRank`，不区分 `UserRank` 和 `MenuRank`。
- 显示状态使用 `MenuVisibility` 表达。

### 5.6 Department

`Department` 是后台组织部门。

核心字段：

- `id`：部门 ID。
- `parentId`：父部门 ID。
- `name`：部门名称。
- `shortName`：部门简称。
- `priority`：排序值。
- `remarks`：备注。
- `createDate` / `createUserId`：创建审计信息。
- `updateDate` / `updateUserId`：更新审计信息。

固定约束：

- 部门树通过 `parentId` 表达业务父子关系。
- 部门显示名称优先使用 `shortName`，没有简称时使用 `name`。
- 用户通过 `departmentId` 归属部门。

### 5.7 Dict

`Dict` 是系统字典项。

核心字段：

- `id`：字典项 ID。
- `type`：字典类型。
- `label`：显示标签。
- `value`：字典值。
- `priority`：排序值。
- `remarks`：备注。
- `createDate` / `createUserId`：创建审计信息。
- `updateDate` / `updateUserId`：更新审计信息。

固定约束：

- 字典按 `type` 分组。
- 字典修订号由字典持久化状态派生，用于前端或缓存判断字典是否变化。

### 5.8 Log

`Log` 是后台系统日志。

核心字段：

- `id`：日志 ID。
- `userId`：操作用户 ID。
- `type`：日志类型。
- `logDate`：日志发生时间。
- `title`：日志标题。
- `remoteAddr`：请求地址。
- `userAgent`：浏览器或客户端信息。
- `method`：请求方法。
- `requestUri`：请求路径。
- `requestParams`：请求参数摘要。
- `remarks`：备注。
- `createDate`：创建时间。

固定约束：

- 日志写入不参与核心业务事务判断。
- 请求参数必须裁剪长度。
- password 相关参数必须脱敏。
- 系统日志查询支持按用户、类型、地址、标题、URI 和时间范围过滤。
- `remarks` 和 `createDate` 是领域读取字段，当前 `sys_log` 持久化主表不落这两个字段。

## 6. Global Constraints

- Controller 只做入口适配、权限、参数接收和响应组装。
- Service 负责业务流程、事务、校验、跨 DAO 编排和关系维护。
- DAO interface 只暴露业务持久化契约。
- Service、DAO 和 Mapper 公开方法不得仅由测试代码调用；仅测试调用的方法必须删除、收窄为内部实现，或重塑为真实业务协作方。
- 暂未接入生产调用但确属稳定业务入口的方法，必须声明 `@LayerPublicApi(reason = "...")`，且 reason 不得以测试作为理由。
- Service `add` 方法必须返回新建主实体的 `EntityId`。
- Service 接口公开方法不得重载；批量、按条件、按 ID、级联等行为差异必须体现在方法名中。
- Service `*Query` 类级注解必须且只能包含 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor`。
- DO、Mapper、缓存和持久化装配器固定在 `sandwish-infra`。
- `UserIdentity` 和 `UserCredential` 属于 System 用户模型，认证使用方式由 Auth 编排。
- `AccessRank` 是用户和菜单共用的访问等级值对象。
- 角色、菜单、用户、部门和字典的排序使用 `priority`。
- 后台系统管理 API 固定归属 `sandwish-admin-api`。

## 7. Functional Requirements

### 7.1 用户管理

- 支持读取、列表、分页、新增、更新、启停、批量删除、上传头像和删除头像。
- 新增用户时必须创建或更新 `ACCOUNT` 类型 `UserIdentity`。
- 新增用户设置初始密码时必须创建 `PASSWORD` 类型 `UserCredential`。
- 修改登录名时必须更新 `ACCOUNT` 类型 `UserIdentity`。
- 重置密码时必须更新 `PASSWORD` 类型 `UserCredential`。
- 更新用户角色时必须重写 `sys_user_role` 关系。
- 查询用户角色时必须通过 `sys_user_role` 读取。

### 7.2 当前用户接口

- 当前登录后台用户接口固定使用 `/api/sys/current-user` 路径前缀。
- 支持读取当前用户信息、更新当前用户信息、更新当前用户密码、上传头像、删除头像、读取当前用户可见菜单和读取当前用户权限编码。
- 更新当前用户信息固定使用 `/api/sys/current-user/info/update`。
- 更新当前用户密码固定使用 `/api/sys/current-user/password/update`。
- 当前用户资料更新、密码更新和菜单计算必须由 `CurrentUserService` 承载，Controller 只做入口适配、传输解密和响应组装。
- 当前用户可见菜单必须按角色授权菜单和用户访问等级过滤。
- 当前用户权限编码必须来自认证上下文。

### 7.3 角色管理

- 支持读取、列表、分页、新增、更新、启停、排序、删除和授权菜单。
- 支持维护角色拥有的用户。
- 支持读取角色关联菜单。
- 角色删除时必须清理角色菜单和角色用户关系。

### 7.4 菜单管理

- 支持读取、列表、新增、更新、显示状态更新、删除、移动和排序。
- 菜单移动必须维护树结构持久化索引。
- 菜单权限编码必须通过 `perms` 表达。
- 按访问等级查询菜单时必须使用 `AccessRank`。

### 7.5 部门管理

- 支持读取、列表、分页、新增、更新、删除和移动。
- 部门移动必须维护树结构持久化索引。
- 删除部门前必须由业务流程确认是否允许删除。

### 7.6 字典管理

- 支持读取、列表、分页、新增、更新、删除、读取类型列表、读取标签列表和读取字典修订号。
- 字典列表按 `type`、`priority`、`createDate` 排序。

### 7.7 日志管理

- 支持读取、列表、分页、新增、更新、删除、批量插入和按条件批量删除。
- 日志分页按 `logDate` 倒序。
- 日志记录必须过滤密码参数。

## 8. Key Flows

### 8.1 新增用户

1. Controller 接收用户保存请求。
2. InterfaceAssembler 转换入口参数。
3. Service 保存 `User` 主体。
4. Service 创建 `ACCOUNT` 类型 `UserIdentity`。
5. Service 创建 `PASSWORD` 类型 `UserCredential`。
6. Service 写入 `sys_user_role` 关系。

### 8.2 修改用户登录名

1. Controller 接收用户更新请求。
2. Service 更新 `User` 主体资料。
3. Service 更新 `ACCOUNT` 类型 `UserIdentity.identityValue`。
4. Service 按请求重写用户角色关系。

### 8.3 角色授权菜单

1. Controller 接收角色菜单授权请求。
2. Service 校验角色存在。
3. Service 删除当前角色的 `sys_role_menu` 关系。
4. Service 写入新 `sys_role_menu` 关系。

### 8.4 菜单或部门移动

1. Controller 接收移动请求。
2. Service 校验源节点和目标节点。
3. Service 调用 DAO 移动树节点。
4. DAO implementation 更新 `parentId`、`lft` 和 `rgt`。

## 9. Non-Functional Requirements

- 用户、角色、菜单、部门、字典和日志 Service 需要保持单元测试覆盖。
- 持久化装配器字段转换需要测试覆盖。
- 树结构移动需要覆盖父子关系和 nested-set 索引更新。
- 用户、角色、菜单、部门和字典缓存失效需要覆盖新增、更新、删除和状态变化。
- 敏感字段落库和日志输出必须保持脱敏或加密。

## 10. Open Items

无
