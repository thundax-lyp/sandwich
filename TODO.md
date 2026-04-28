# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前主线顺序

1. MyBatis XML 消灭迁移
   - 按 [`docs/30-designs/XML-ELIMINATION-RUNBOOK.md`](docs/30-designs/XML-ELIMINATION-RUNBOOK.md) 执行。
   - 手册按垂直域说明迁移方法。
   - 任务按平行 entity 推进。
   - 每个 entity 一个 TODO、一个闭环提交。

## MyBatis XML 消灭迁移

### 第一批：有达梦 XML 的 entity

1. 迁移 `Role` 持久化链路
   - 以 `RoleMapper.xml` 达梦实现为行为基准。
   - 迁出旧 CRUD 基类，展开查询 DAO 参数。
   - 仅 `insert` / `update` 使用 `Role` 实体参数。
   - Mapper 保持最小 `BaseMapper<RoleDO>` 定义，业务在 `DaoImpl` 中实现。
   - 用 `DaoImpl` 中的 MyBatis-Plus / Java 持久化实现替代 XML。
   - 消除当前链路 PageHelper 依赖，删除对应 XML，补齐最小验证。
2. 迁移 `UploadFile` 持久化链路
   - 以 `UploadFileMapper.xml` 达梦实现为行为基准。
   - 迁出旧 CRUD 基类，展开查询 DAO 参数。
   - 仅 `insert` / `update` 使用 `UploadFile` 实体参数。
   - Mapper 保持最小 `BaseMapper<UploadFileDO>` 定义，业务在 `DaoImpl` 中实现。
   - 用 `DaoImpl` 中的 MyBatis-Plus / Java 持久化实现替代 XML。
   - 消除当前链路 PageHelper 依赖，删除对应 XML，补齐最小验证。
3. 迁移 `UserEncrypt` 持久化链路
   - 以 `UserEncryptMapper.xml` 达梦实现为行为基准。
   - 迁出旧 CRUD 基类，展开查询 DAO 参数。
   - 仅 `insert` / `update` 使用 `UserEncrypt` 实体参数。
   - Mapper 保持最小 `BaseMapper<UserEncryptDO>` 定义，业务在 `DaoImpl` 中实现。
   - 用 `DaoImpl` 中的 MyBatis-Plus / Java 持久化实现替代 XML。
   - 消除当前链路 PageHelper 依赖，删除对应 XML，补齐最小验证。
4. 迁移 `User` 持久化链路
    - 以 `UserMapper.xml` 达梦实现为行为基准。
    - 迁出旧 CRUD 基类，展开查询 DAO 参数。
    - 仅 `insert` / `update` 使用 `User` 实体参数。
    - Mapper 保持最小 `BaseMapper<UserDO>` 定义，业务在 `DaoImpl` 中实现。
    - 用 `DaoImpl` 中的 MyBatis-Plus / Java 持久化实现替代 XML。
    - 消除当前链路 PageHelper 依赖，删除对应 XML，补齐最小验证。

### 第二批：无达梦 XML 但需清理旧持久化形态的对象

1. 迁移 `Dict` 持久化链路
   - 清理旧 CRUD 基类和 PageHelper 依赖。
   - 展开查询 DAO 参数。
   - 保持 MyBatis-Plus 只在 `sandwish-infra` 内使用。
2. 迁移 `AsyncTask` 持久化链路
   - 清理旧 CRUD 基类和 PageHelper 依赖。
   - 展开查询 DAO 参数。
   - 保持 MyBatis-Plus 只在 `sandwish-infra` 内使用。
3. 迁移 `AccessToken` 持久化链路
   - 清理旧 CRUD 基类和 PageHelper 依赖。
   - 展开查询 DAO 参数。
   - 保持 MyBatis-Plus 只在 `sandwish-infra` 内使用。
4. 迁移 `LoginForm` 持久化链路
   - 清理旧 CRUD 基类和 PageHelper 依赖。
   - 展开查询 DAO 参数。
   - 保持 MyBatis-Plus 只在 `sandwish-infra` 内使用。
5. 迁移 `LoginLock` 持久化链路
   - 清理旧 CRUD 基类和 PageHelper 依赖。
   - 展开查询 DAO 参数。
   - 保持 MyBatis-Plus 只在 `sandwish-infra` 内使用。
6. 迁移 `Permission` 持久化链路
   - 清理旧 CRUD 基类和 PageHelper 依赖。
   - 展开查询 DAO 参数。
   - 保持 MyBatis-Plus 只在 `sandwish-infra` 内使用。
7. 迁移 `KeypairPrivateKey` 持久化链路
   - 清理旧 CRUD 基类和 PageHelper 依赖。
   - 展开查询 DAO 参数。
   - 保持 MyBatis-Plus 只在 `sandwish-infra` 内使用。
8. 迁移 `SmsValidateCode` 持久化链路
   - 清理旧 CRUD 基类和 PageHelper 依赖。
   - 展开查询 DAO 参数。
   - 保持 MyBatis-Plus 只在 `sandwish-infra` 内使用。

### 第三批：公共收口

1. 删除 PageHelper 依赖和公共支撑类。
2. 删除或收窄旧 `CrudDao` / `CrudServiceImpl` 基类。
3. 删除 Mapper XML 配置和空 mapping 目录。
4. 增加禁止新增 XML、PageHelper、旧 CRUD 基类依赖的架构测试或静态检查。
5. 同步长期数据库治理规则。
