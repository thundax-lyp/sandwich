# Integration Test Database Scripts

本文档定义 `deploy/integration/db/` 下集成测试 SQL 的目录职责和执行顺序。

正式数据库基线仍归属根目录 `db/`。本目录只服务集成测试，不作为产品部署初始化数据。

## Directory Map

```text
deploy/integration/db/
  README.md
  00-schema/
  10-baseline/
  20-scenarios/
  90-cleanup/
```

## Execution Order

固定执行顺序：

1. `00-schema/`
2. `90-cleanup/`
3. `10-baseline/`
4. `20-scenarios/`

`90-cleanup/` 在 baseline 和 scenario 前执行，保证每次测试从干净数据状态开始。

## 00-schema

`00-schema/` 固定作为正式 schema 的集成测试装载入口。

规则：

- schema 来源必须可追溯到根目录 `db/schema/`。
- 不在本目录发明不同于正式 schema 的表结构。
- 如果正式 schema 发生变化，集成测试 schema 装载入口必须同步。
- schema 脚本按正式 `db/AGENT.md` 的业务域顺序装载。

## 10-baseline

`10-baseline/` 固定保存所有集成测试共享的基础数据。

基础数据包括：

- 后台用户、角色、菜单、权限、部门。
- 字典、日志、审计元数据。
- OpenClient、OpenClient 权限、密钥和 IP 白名单。
- 前台会员、登录标识、认证凭据和会话。
- 存储对象、对象引用、分片上传基础记录。
- submission 和 submission image 基础记录。

规则：

- 文件名使用三位数字序号加业务域说明，例如 `001-admin-user.sql`。
- 数据主键使用稳定雪花 ID。
- 业务键使用稳定测试值。
- 密码、token、client secret、验证码等敏感值不保存明文。
- baseline 脚本必须可重复执行。

## 20-scenarios

`20-scenarios/` 固定保存单个业务场景专用数据。

规则：

- 一个 SQL 文件只服务一个业务域或一条可验收链路。
- 场景数据不得替代 baseline 数据。
- 场景文件名使用三位数字序号加业务域和场景说明。
- 场景脚本之间不得依赖执行顺序。

## 90-cleanup

`90-cleanup/` 固定保存清理脚本。

规则：

- cleanup 脚本必须可重复执行。
- cleanup 只清理集成测试数据。
- cleanup 不删除正式初始化数据。
- 业务数据清理和认证运行态清理分文件维护。
- Redis key、OSS 临时目录和 MQ 运行态清理由测试支撑类处理，SQL 只处理数据库数据。

## Verification

每批 SQL 变更完成后必须确认：

- schema 与正式建表脚本一致。
- baseline 可重复装载。
- scenario 不依赖其他 scenario 的执行顺序。
- cleanup 可重复执行。
- 清理后不会破坏正式初始化数据。
