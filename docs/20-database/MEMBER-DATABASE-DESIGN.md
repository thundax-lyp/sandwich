# MEMBER DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich 前台会员主表、字段映射和持久化规则。

本文档以 `MEMBER-REQUIREMENTS.md` 的前台会员主体模型为基础。建表 SQL 见 [`../../db/schema/member.sql`](../../db/schema/member.sql)，初始化脚本见 [`../../db/data/member.sql`](../../db/data/member.sql)。

## 2. Scope

当前覆盖范围：

- `member_member`
- `member_identity`
- `member_credential`
- `MemberDO`
- `MemberIdentityDO`
- `MemberCredentialDO`
- `MemberMapper`
- `MemberIdentityMapper`
- `MemberCredentialMapper`
- `MemberDaoImpl`
- `MemberIdentityDaoImpl`
- `MemberCredentialDaoImpl`
- `MemberPersistenceAssembler`
- `MemberIdentityPersistenceAssembler`
- `MemberCredentialPersistenceAssembler`

当前不覆盖范围：

- 会员认证会话和 token 表
- 会员地址、邮编等私密资料
- 会员登录表单，`MemberLoginForm` 只使用 Redis / JetCache 运行态，不建立数据库表
- 会员实名认证表
- 会员等级、积分和权益表
- 会员操作审计表

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- 存储引擎优先使用 `InnoDB`。
- 字符集优先使用 `utf8mb4`。
- 独立数据库表主键数据库类型固定为 `bigint`，Java 类型固定为 `Long`。
- 独立数据库表主键由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- 会员主表使用 `member_` 业务域前缀。
- 会员主表固定使用 `member_member`。
- DAO list/page 查询固定追加 `del_flag = '0'` 条件。
- `DO/DataObject` 不暴露给 Controller 或 Service。

## 4. Naming Rules

- 会员主表固定为 `member_member`。
- 主键字段固定为 `id`。
- 会员状态字段固定为 `status`。
- 审计字段固定为 `create_date`、`create_by`、`update_date`、`update_by`。
- 逻辑删除字段固定为 `del_flag`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `member_member` | `MemberDO` | `MemberMapper` | `Member` |
| `member_identity` | `MemberIdentityDO` | `MemberIdentityMapper` | `MemberIdentity` |
| `member_credential` | `MemberCredentialDO` | `MemberCredentialMapper` | `MemberCredential` |

## 6. Table Design

### 6.1 member_member

`member_member` 保存前台会员主体必要业务数据。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 会员主键 |
| `name` | `name` | `name` | 否 | 姓名 |
| `gender` | `gender` | `gender` | 否 | 性别 |
| `status` | `status` | `status` | 是 | 会员生命周期状态 |
| `priority` | `priority` | `priority` | 是 | 排序值 |
| `remarks` | `remarks` | `remarks` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `create_by` | `createBy` | `createUserId` | 否 | 创建人 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `update_by` | `updateBy` | `updateUserId` | 否 | 更新人 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `gender` 通过 `MemberGender.value()` 写入。
- `gender` 固定使用状态值：`MALE`、`FEMALE`、`PRIVATE`。
- `status` 通过 `MemberStatus.value()` 写入。
- `status` 固定使用状态值：`PENDING`、`ACTIVE`、`SUSPENDED`、`CLOSED`。
- `priority` 默认值固定为 `0`。
- `del_flag` 默认值固定为 `'0'`，`Entity` 与 `DO/DataObject` 不声明 `delFlag`。

索引设计：

- 主键：`pk_member_member(id)`
- 普通索引：`idx_member_member_status(status, priority, create_date)`
- 普通索引：`idx_member_member_del_flag(del_flag)`

### 6.2 member_identity

`member_identity` 保存前台会员登录标识。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 标识主键 |
| `member_id` | `memberId` | `memberId` | 是 | 会员主键 |
| `identity_type` | `identityType` | `identityType` | 是 | 标识类型 |
| `identity_value` | `identityValue` | `identityValue` | 是 | 标识值 |
| `status` | `status` | `status` | 是 | 标识状态 |

字段规则：

- `identity_type` 固定使用 `ACCOUNT`、`MOBILE`、`EMAIL`。
- `status` 固定使用 `ENABLED`、`DISABLED`。
- `identity_type + identity_value` 全局唯一，手机号和邮箱登录依据以本表为准。

索引设计：

- 主键：`pk_member_identity(id)`
- 唯一索引：`uk_member_identity_type_value(identity_type, identity_value)`
- 普通索引：`idx_member_identity_member(member_id, status)`
- 普通索引：`idx_member_identity_member_type(member_id, identity_type)`

### 6.3 member_credential

`member_credential` 保存前台会员认证凭据。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 凭据主键 |
| `member_id` | `memberId` | `memberId` | 是 | 会员主键 |
| `identity_id` | `identityId` | `identityId` | 是 | 登录标识主键 |
| `credential_type` | `credentialType` | `credentialType` | 是 | 凭据类型 |
| `credential_value` | `credentialValue` | `credentialValue` | 是 | 凭据密文或哈希值 |
| `status` | `status` | `status` | 是 | 凭据状态 |
| `need_change_password` | `needChangePassword` | `needChangePassword` | 是 | 是否需要改密 |
| `failed_count` | `failedCount` | `failedCount` | 是 | 失败次数 |
| `failed_limit` | `failedLimit` | `failedLimit` | 是 | 失败上限 |
| `locked_until` | `lockedUntil` | `lockedUntil` | 否 | 锁定截止时间 |
| `expires_at` | `expiresAt` | `expiresAt` | 否 | 过期时间 |
| `last_verified_at` | `lastVerifiedAt` | `lastVerifiedAt` | 否 | 最近验证时间 |

字段规则：

- `credential_type` 首轮固定使用 `PASSWORD`。
- `status` 固定使用 `ACTIVE`、`LOCKED`、`EXPIRED`、`DISABLED`。
- `credential_value` 不保存明文密码。
- 登录失败次数、锁定、过期和最近验证时间固定在凭据维度维护。

索引设计：

- 主键：`pk_member_credential(id)`
- 唯一索引：`uk_member_credential_identity_type(identity_id, credential_type)`
- 普通索引：`idx_member_credential_member(member_id, status)`
- 普通索引：`idx_member_credential_identity_status(identity_id, status)`
- 普通索引：`idx_member_credential_locked(locked_until)`

## 7. Relationship Rules

- 当前会员主表不强制数据库外键。
- 后续会员扩展表必须通过稳定会员主键 `member_id` 关联 `member_member.id`。
- `member_identity.member_id` 关联 `member_member.id`。
- `member_credential.member_id` 关联 `member_member.id`。
- `member_credential.identity_id` 关联 `member_identity.id`。

## 8. Persistence Rules

- `MemberDO` 固定映射 `member_member`。
- `MemberMapper` 固定保持 `public interface MemberMapper extends BaseMapper<MemberDO> {}`。
- `MemberDaoImpl` 固定负责 `Member <-> MemberDO` 转换、查询条件拆解、分页和逻辑删除过滤。
- `MemberIdentityDaoImpl` 固定负责 `MemberIdentity <-> MemberIdentityDO` 转换和标识查询。
- `MemberCredentialDaoImpl` 固定负责 `MemberCredential <-> MemberCredentialDO` 转换和凭据查询。
- `MemberPersistenceAssembler` 只负责 `Member <-> MemberDO` 字段转换。
- `MemberIdentityPersistenceAssembler` 只负责 `MemberIdentity <-> MemberIdentityDO` 字段转换。
- `MemberCredentialPersistenceAssembler` 只负责 `MemberCredential <-> MemberCredentialDO` 字段转换。
- Service 不感知 `MemberDO`。
- Controller 不直接依赖 `MemberMapper`、`MemberDO` 或 `MemberPersistenceAssembler`。

## 9. Query Model Rules

- `list/page` 查询固定过滤 `del_flag = '0'`。
- 会员列表默认按 `priority` 和 `name` 升序。
- `pageNo` / `pageSize` 由 Service 校验，DAO implementation 只按已校验参数执行分页。

## 10. Open Items

无
