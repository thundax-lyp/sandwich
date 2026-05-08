# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `login-form-entry-cleanup`：清理入口模块登录表单残留依赖
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/AdminAuthService.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/MemberAuthService.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/testsupport/InMemoryLoginFormDaoImpl.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/controller/AuthControllerContractTest.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/controller/CaptchaControllerContractTest.java`
  - 处理动作：清理入口 Service、Controller 测试和测试支撑中的旧登录表单依赖口径
  - 验收点：入口生产代码只依赖登录表单 biz Service，不直接编排登录表单 DAO
  - 重要度：7/10

- [ ] `login-form-document-sync`：同步登录表单目标文档
  - 范围文件：
    - `docs/10-requirements/AUTH-REQUIREMENTS.md`
    - `docs/20-database/AUTH-DATABASE-DESIGN.md`
  - 处理动作：将登录表单业务 Service 归属、容量控制和登录成功释放规则同步为 biz auth 目标态
  - 验收点：文档不保留迁移历史，明确 Controller 只做入口适配和验证码图片输出，并明确 `PreAuthSession` 与登录后会话容量边界
  - 重要度：7/10

- [ ] `login-form-migration-verification`：执行登录表单迁移验收和 RUNBOOK 收口
  - 范围文件：
    - `docs/30-designs/RUNBOOK-LOGIN-FORM-BIZ-MIGRATION.md`
    - `TODO.md`
  - 处理动作：执行残留引用扫描和 Maven 测试，删除或收窄已完成 TODO，并清理 RUNBOOK
  - 验收点：入口模块生产代码无登录表单 DAO 直连，全量 Maven 测试通过，工作区干净
  - 重要度：8/10

## 待讨论项
