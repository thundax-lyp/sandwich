# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `sys-user-write-orchestration`：收敛用户写入身份和凭据协作
  - 范围文件：`sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java`
    `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserIdentityServiceImpl.java`
    `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserCredentialServiceImpl.java`
    `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserServiceImplTest.java`
    `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserIdentityServiceImplTest.java`
    `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserCredentialServiceImplTest.java`
  - 处理动作：让 `UserServiceImpl.add` 和 `UserServiceImpl.update` 通过身份和凭据 Service 协作完成账号身份、密码凭据写入。
  - 验收点：用户新增和更新仍由 `UserService` 作为事务入口，Controller 不新增跨 Service 写事务编排，用户、角色、身份、凭据和签名测试通过。
  - 重要度：7/10

- [ ] `sys-user-service-boundary-docs`：固定用户主体、身份和凭据 Service 边界
  - 范围文件：`docs/10-requirements/SYSTEM-REQUIREMENTS.md`
    `sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceNamingArchitectureTest.java`
    `sandwish-common/sandwish-common-test/src/main/java/com/github/thundax/common/test/architecture/NamingArchitectureRuleSupport.java`
  - 处理动作：同步系统需求和架构测试，固定 `UserService`、`UserIdentityService`、`UserCredentialService` 的公开方法边界。
  - 验收点：文档说明拆分后的职责边界，架构测试能阻止身份和凭据专用方法回流到 `UserService`。
  - 重要度：6/10

- [ ] `sys-user-account-runbook-cleanup`：清理用户账号服务拆分 RUNBOOK
  - 范围文件：`docs/30-designs/RUNBOOK-USER-ACCOUNT-SERVICE-SPLIT.md`
    `TODO.md`
  - 处理动作：用户账号服务拆分任务全部关闭后删除临时 RUNBOOK，并从 `TODO.md` 删除本清理项。
  - 验收点：拆分完成后仓库不再保留临时 RUNBOOK，剩余 TODO 只包含未关闭任务。
  - 重要度：5/10

## 待讨论项
