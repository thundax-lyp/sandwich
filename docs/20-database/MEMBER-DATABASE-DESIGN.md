# MEMBER DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich 前台会员主表、字段映射和持久化规则。

本文档以 `MEMBER-REQUIREMENTS.md` 的前台会员主体模型为基础。建表 SQL 见 [`../../db/schema/member.sql`](../../db/schema/member.sql)，初始化脚本见 [`../../db/data/member.sql`](../../db/data/member.sql)。

## 2. Scope

当前覆盖范围：

- `member_member`
- `MemberDO`
- `MemberMapper`
- `MemberDaoImpl`
- `MemberPersistenceAssembler`

当前不覆盖范围：

- 会员第三方登录标识表
- 会员实名认证表
- 会员等级、积分和权益表
- 会员操作审计表

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- 存储引擎优先使用 `InnoDB`。
- 字符集优先使用 `utf8mb4`。
- `MemberDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- 会员主表使用 `member_` 业务域前缀。
- 会员主表固定使用 `member_member`。
- `email`、`mobile` 和 `address` 由持久化类型处理器加密处理。
- `login_pass` 不保存明文。
- DAO list/page 查询固定追加 `del_flag = '0'` 条件。
- `DO/DataObject` 不暴露给 Controller 或 Service。

## 4. Naming Rules

- 会员主表固定为 `member_member`。
- 主键字段固定为 `id`。
- 登录名字段固定为 `login_name`。
- 密码字段固定为 `login_pass`。
- 会员状态字段固定为 `enable_flag`。
- 注册行为字段固定为 `register_ip` 和 `register_date`。
- 最近登录行为字段固定为 `last_login_ip`、`last_login_date` 和 `login_count`。
- 外部同步标识字段固定为 `ywtb_id`。
- 审计字段固定为 `create_date`、`create_by`、`update_date`、`update_by`。
- 逻辑删除字段固定为 `del_flag`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `member_member` | `MemberDO` | `MemberMapper` | `Member` |

## 6. Table Design

### 6.1 member_member

`member_member` 保存前台会员主体资料和必要登录行为字段。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 会员主键 |
| `login_name` | `loginName` | `loginName` | 是 | 登录名 |
| `login_pass` | `loginPass` | `loginPass` | 否 | 密码哈希 |
| `email` | `email` | `email` | 否 | 邮箱 |
| `name` | `name` | `name` | 否 | 姓名 |
| `gender` | `gender` | `gender` | 否 | 性别 |
| `mobile` | `mobile` | `mobile` | 否 | 手机号 |
| `address` | `address` | `address` | 否 | 地址 |
| `zipcode` | `zipcode` | `zipcode` | 否 | 邮编 |
| `enable_flag` | `enableFlag` | `status` | 是 | 会员状态 |
| `register_ip` | `registerIp` | `registerIp` | 否 | 注册 IP |
| `register_date` | `registerDate` | `registerDate` | 否 | 注册时间 |
| `last_login_ip` | `lastLoginIp` | `lastLoginIp` | 否 | 最近登录 IP |
| `last_login_date` | `lastLoginDate` | `lastLoginDate` | 否 | 最近登录时间 |
| `ywtb_id` | `ywtbId` | `ywtbId` | 否 | 外部同步标识 |
| `login_count` | `loginCount` | `loginCount` | 是 | 登录次数 |
| `priority` | `priority` | `priority` | 是 | 排序值 |
| `remarks` | `remarks` | `remarks` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 MyBatis-Plus `IdType.ASSIGN_UUID` 生成。
- `enable_flag` 通过 `MemberStatus.value()` 写入。
- `login_count` 默认值固定为 `0`。
- `priority` 默认值固定为 `0`。
- `del_flag` 默认值固定为 `'0'`，`Entity` 与 `DO/DataObject` 不声明 `delFlag`。

索引设计：

- 主键：`pk_member_member(id)`
- 唯一索引：`uk_member_member_login_name(login_name)`
- 唯一索引：`uk_member_member_ywtb_id(ywtb_id)`
- 普通索引：`idx_member_member_status(enable_flag, priority, create_date)`
- 普通索引：`idx_member_member_email(email)`
- 普通索引：`idx_member_member_mobile(mobile)`
- 普通索引：`idx_member_member_del_flag(del_flag)`

## 7. Relationship Rules

- 当前会员主表不强制数据库外键。
- 后续会员扩展表必须通过稳定会员主键 `member_id` 关联 `member_member.id`。

## 8. Persistence Rules

- `MemberDO` 固定映射 `member_member`。
- `MemberMapper` 固定保持 `public interface MemberMapper extends BaseMapper<MemberDO> {}`。
- `MemberDaoImpl` 固定负责 `Member <-> MemberDO` 转换、查询条件拆解、分页和逻辑删除过滤。
- `MemberPersistenceAssembler` 只负责 `Member <-> MemberDO` 字段转换。
- Service 不感知 `MemberDO`。
- Controller 不直接依赖 `MemberMapper`、`MemberDO` 或 `MemberPersistenceAssembler`。

## 9. Query Model Rules

- `list/page` 查询固定过滤 `del_flag = '0'`。
- 会员列表默认按 `priority` 和 `email` 升序。
- `pageNo` / `pageSize` 由 Service 校验，DAO implementation 只按已校验参数执行分页。

## 10. Open Items

无
