# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
