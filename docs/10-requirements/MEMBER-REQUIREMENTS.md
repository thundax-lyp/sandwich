# MEMBER REQUIREMENTS

## 1. Purpose

本文档定义 Sandwich 前台会员的最小业务需求边界。

本文档用于支撑 `Member` 主体、会员登录状态、会员资料和会员数据库设计的后续治理。数据库设计见 [`../20-database/MEMBER-DATABASE-DESIGN.md`](../20-database/MEMBER-DATABASE-DESIGN.md)。

## 2. Scope

当前覆盖范围：

- 前台会员主体 `Member`
- 会员姓名、性别和基础业务状态
- 会员生命周期状态

当前不覆盖范围：

- 会员登录标识和认证凭据
- 会员联系方式登录标识
- 会员注册与登录行为字段
- 会员地址、邮编等私密资料
- 会员 OAuth / 第三方身份绑定
- 会员等级、积分、权益和订单关系
- 会员实名认证资料
- 会员营销画像

## 3. Bounded Context

`Member` 是前台访问主体，不等同于后台 `User`。

后台 `User` 用于管理端登录、权限和审计。前台 `Member` 用于前台 API 登录态、前台资料和前台业务归属。

## 4. Module Mapping

- `sandwish-biz`：承载 `Member`、`MemberService` 和 `MemberDao`。
- `sandwish-infra`：承载 `MemberDO`、`MemberMapper`、`MemberDaoImpl` 和 `MemberPersistenceAssembler`。
- `sandwish-front-api`：承载会员登录状态入口和前台会员上下文适配。

## 5. Core Business Objects

- `Member`：前台会员主体。
- `MemberGender`：会员性别，固定表达男、女和保密。
- `MemberStatus`：会员生命周期状态，固定表达待激活、活跃、暂停和关闭。
- `MemberSecurityContext`：前台会员运行时身份上下文。

## 6. Global Constraints

- 会员主表固定为 `member_member`。
- 会员数据库表使用 `member_` 业务域前缀。
- 会员身份上下文不得与后台 `UserAccessHolder` 混用。
- `Member` 不承载登录标识、认证凭据、联系方式登录依据、地址、邮编或登录行为字段。

## 7. Functional Requirements

- 系统可以保存会员姓名、性别、生命周期状态、排序值和备注。
- 系统可以维护会员生命周期状态。
- 系统可以分页查询会员列表。

## 8. Key Flows

### 8.1 Member Profile Persistence

1. Service 校验会员资料。
2. DAO implementation 将 `Member` 转换为 `MemberDO`。
3. Mapper 持久化到 `member_member`。
4. DAO implementation 将持久化结果转换回 `Member`。

## 9. Non-Functional Requirements

- 会员主表使用 `InnoDB` 和 `utf8mb4`。
- 会员主键使用雪花 `Long`。
- 会员列表查询固定过滤 `del_flag = '0'`。
- 会员资料持久化规则必须与 `MEMBER-DATABASE-DESIGN.md` 保持一致。

## 10. Open Items

无
