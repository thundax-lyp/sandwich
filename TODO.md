# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 以下任务项来自 `docs/00-governance/how-to/HOW-TO-REMODEL-DOMAIN-ENTITY.md`。
- 领域实体重塑拆解仍处于人工审阅阶段，审阅通过前不得执行。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `member-auth`：迁移会员与认证实体边界
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/base/BaseMember.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/Member.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AccessToken.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/LoginForm.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/assembler/MemberLoginInterfaceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/AuthPersistenceAssembler.java`
  - 处理动作：下沉会员实体字段，清理认证对象中的 Controller 层 JSON 注解。
  - 验收点：`Member` 不再继承 `AdminDataEntity`；`Member`、`AccessToken`、`LoginForm` 不再 import `com.fasterxml.jackson.annotation..`；JSON 日期格式移动到 API 模型。
  - 重要度：8/10

- [ ] `legacy-entity-base`：删除旧实体继承链
  - 范围文件：
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/BaseEntity.java`
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/DataEntity.java`
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/TreeEntity.java`
    - `sandwish-biz/src/main/java/com/github/thundax/common/persistence/AdminDataEntity.java`
    - `sandwish-biz/src/main/java/com/github/thundax/common/persistence/AdminTreeEntity.java`
  - 处理动作：删除已经没有生产引用的旧实体基类文件。
  - 验收点：旧基类文件已删除；生产代码不再引用 `BaseEntity`、`DataEntity`、`AdminDataEntity`、`TreeEntity`、`AdminTreeEntity`。
  - 重要度：10/10

- [ ] `migration-cleanup`：全量验证并清理迁移现场
  - 范围文件：
    - `docs/AGENT.md`
    - `docs/00-governance/how-to/HOW-TO-REMODEL-DOMAIN-ENTITY.md`
    - `TODO.md`
  - 处理动作：完成所有迁移后，删除临时 RUNBOOK、撤销临时读取路由，并清理 `TODO.md`。
  - 验收点：`docs/AGENT.md` 不再引用 `HOW-TO-REMODEL-DOMAIN-ENTITY.md`；迁移 RUNBOOK 已删除；`TODO.md` 删除已完成任务或只保留剩余精确任务。
  - 重要度：10/10
