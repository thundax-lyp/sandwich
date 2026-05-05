# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `common-security`：增加当前用户基础契约
  - 范围文件：
    - `sandwish-common/sandwish-common-security/src/main/java/com/github/thundax/common/security/user/CurrentUser.java`
    - `sandwish-common/sandwish-common-security/src/main/java/com/github/thundax/common/security/user/CurrentUserProvider.java`
    - `sandwish-common/sandwish-common-security/src/main/java/com/github/thundax/common/security/user/AnonymousCurrentUserProvider.java`
    - `sandwish-common/sandwish-common-security/src/test/java/com/github/thundax/common/security/user/CurrentUserTest.java`
  - 处理动作：新增不依赖业务 `User` Entity 的当前用户读取契约。
  - 验收点：当前用户模型只暴露稳定身份和权限字段，匿名 Provider 有测试覆盖。
  - 重要度：8/10

- [ ] `common-core`：增加统一标识值对象和编码器
  - 范围文件：
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/EntityId.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/EntityIdCodec.java`
    - `sandwish-common/sandwish-common-core/src/test/java/com/github/thundax/common/id/EntityIdTest.java`
    - `sandwish-common/sandwish-common-core/src/test/java/com/github/thundax/common/id/EntityIdCodecTest.java`
  - 处理动作：收敛通用实体标识值对象和字符串编码转换能力。
  - 验收点：`EntityId` 能表达非空标识、相等性和字符串转换，编码器有单元测试覆盖。
  - 重要度：8/10

- [ ] `common-core`：增加统一 ID 生成契约
  - 范围文件：
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/IdGenerator.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/UuidIdGenerator.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/SnowflakeIdGenerator.java`
    - `sandwish-common/sandwish-common-core/src/test/java/com/github/thundax/common/id/UuidIdGeneratorTest.java`
    - `sandwish-common/sandwish-common-core/src/test/java/com/github/thundax/common/id/SnowflakeIdGeneratorTest.java`
  - 处理动作：新增 ID 生成接口和默认 UUID、Snowflake 实现。
  - 验收点：两类生成器都能生成非空且可区分的 ID，并有单元测试覆盖。
  - 重要度：7/10

- [ ] `common-mybatis`：增加通用 EntityId 类型处理器
  - 范围文件：
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/mybatis/typehandler/EntityIdTypeHandler.java`
    - `sandwish-common/sandwish-common-mybatis/src/test/java/com/github/thundax/common/mybatis/typehandler/EntityIdTypeHandlerTest.java`
    - `sandwish-common/sandwish-common-mybatis/pom.xml`
  - 处理动作：新增 `EntityId` 与数据库字符串字段的 MyBatis 类型转换。
  - 验收点：类型处理器能正确写入字符串 ID 并从结果集读取为 `EntityId`，有单元测试覆盖。
  - 重要度：7/10

- [ ] `common-mybatis`：增加通用 JSON 列表类型处理器
  - 范围文件：
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/mybatis/typehandler/StringListJsonTypeHandler.java`
    - `sandwish-common/sandwish-common-mybatis/src/test/java/com/github/thundax/common/mybatis/typehandler/StringListJsonTypeHandlerTest.java`
    - `sandwish-common/sandwish-common-mybatis/pom.xml`
  - 处理动作：新增字符串列表与数据库 JSON 字符串字段的 MyBatis 类型转换。
  - 验收点：类型处理器能正确处理空列表、非空列表和空数据库值，并有单元测试覆盖。
  - 重要度：6/10

## 待审阅任务项

## 待讨论项
