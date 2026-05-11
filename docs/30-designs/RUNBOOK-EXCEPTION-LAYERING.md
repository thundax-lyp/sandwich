# RUNBOOK EXCEPTION LAYERING

## 1. Purpose

本 RUNBOOK 用于执行 Sandwich 异常体系分层改造。

目标是把异常语义固定为分层边界：

- `Infra` 不定义业务异常，不把技术失败翻译成业务失败。
- `Biz` 使用业务异常表达领域规则和业务流程失败。
- `API` 使用对外响应异常表达 HTTP 状态、业务 code 和响应 message。
- 上层捕获下层异常后，通过明确的 `ExceptionTranslator` 转换异常语义。

本 RUNBOOK 是一次性执行手册，不作为长期治理规则保留。迁移完成后，稳定规则必须沉淀到治理文档，RUNBOOK 与对应 TODO 必须清理。

## 2. Scope

当前覆盖范围：

- `sandwish-common-core/src/main/java/com/github/thundax/common/exception`
- `sandwish-common-web/src/main/java/com/github/thundax/common/web/exception`
- `sandwish-biz/src/main/java/com/github/thundax/modules/**/exception`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/**/exception`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/**/exception`
- Controller、Service、DAO implementation 中异常抛出、捕获和声明。
- 异常对应的 i18n 文案和异常测试。
- 异常分层相关架构测试。

当前不覆盖范围：

- Java 标准库、Spring、MyBatis、OSS、Redis 等第三方异常自身定义。
- 测试方法中的宽泛 `throws Exception` 机械清理。
- 非异常体系的业务返回模型重构。
- 前端错误展示策略。
- 日志采集、监控告警和 traceId 输出格式。

## 3. Target Architecture

### 3.1 Infra Exception Boundary

`sandwish-infra` 固定不定义业务异常。

Infra 层规则：

- DAO implementation、Mapper 和存储适配器不抛 `BizException`、`DomainException` 或 API 响应异常。
- Infra 层允许第三方技术异常自然抛出。
- Infra 层需要补充技术上下文时，只使用 Java、Spring、MyBatis 或第三方技术异常类型。
- Sandwich 不新增 `InfraException`。
- Infra 层不依赖 `sandwish-common-web` 和 API 模块。

### 3.2 Biz Exception Boundary

`sandwish-biz` 固定使用业务异常表达业务失败。

Biz 层规则：

- 领域对象、值对象和领域枚举的不变量失败固定使用 `DomainException`。
- DAO / Infra 抛出的数据库、框架、网络、序列化等失败固定视为技术异常。
- Service 是 Biz 层对外异常边界，固定放行 `DomainException` 和 `BizException`。
- Service 捕获其他技术异常后，固定转换为 `BizException` 并保留原始 cause。
- Service 业务流程失败使用 `BizException`。
- `BizException` 固定携带 `String` 类型业务错误码、messageKey、defaultMessage 和 args，不携带 HTTP status。
- Biz 层不抛 `SandwishException`。
- Biz 层不依赖 `sandwish-common-web` 和 API 模块。

### 3.3 Service Exception Boundary Annotation

Service 方法固定使用 `@BizExceptionBoundary` 声明技术异常转换边界。

注解规则：

- `@BizExceptionBoundary` 只允许标注在 Service implementation 的 public 方法上。
- 对外 Service 方法必须标注 `@BizExceptionBoundary`，只有 getter 方法允许由架构测试明确排除。
- `@BizExceptionBoundary` 固定携带 fallback 业务 code、messageKey 和 defaultMessage。
- `@BizExceptionBoundary` 对 `DomainException` 和 `BizException` 固定放行。
- `@BizExceptionBoundary` 捕获其他异常后固定转换为 `BizException`，并保留原始 cause。
- `@BizExceptionBoundary` 不负责识别具体业务语义；唯一键冲突、并发修改、对象不存在等明确业务失败仍由 Service 显式转换为具体 `BizException`。
- Controller、DAO、Mapper、领域对象和值对象禁止标注 `@BizExceptionBoundary`。

### 3.4 API Exception Boundary

API 入口固定使用对外响应异常表达 HTTP 响应语义。

API 层规则：

- `SandwishException` 固定归属 `sandwish-common-web`。
- `SandwishException` 是对外响应异常，固定携带 HTTP status、`String` 类型业务 code 和 message。
- Controller 可以抛 `SandwishException`。
- Controller 不直接向外抛 `BizException`、`DomainException` 或 Infra 技术异常。
- API 层通过 `ExceptionTranslator` 把 Biz / Domain / framework 异常转换为 `SandwishException`。
- 权限、认证、参数格式和内部系统级错误由 HTTP status 表达大类。
- 业务规则失败默认使用 HTTP `400 Bad Request`，并通过 `String` 业务 code 表达具体原因。
- 明确资源状态冲突、乐观锁冲突和并发修改使用 HTTP `409 Conflict`。
- 认证失败使用 HTTP `401 Unauthorized`。
- 权限不足使用 HTTP `403 Forbidden`。
- 资源不存在使用 HTTP `404 Not Found`。
- 未识别系统错误使用 HTTP `500 Internal Server Error`。
- 后台和前台不共享业务 code；后台 code 与前台 code 分别由 `AdminExceptionTranslator` 和 `FrontExceptionTranslator` 维护。

### 3.5 API Error Code Document Boundary

API error code 是前后端协议的一部分，固定形成项目文档。

错误码规则：

- API response code 固定为 `String`。
- API response code 格式固定为 `<DOMAIN>-<NUMBER>`。
- `DOMAIN` 固定使用大写业务域标识，例如 `COMMON`、`AUTH`、`SYS`、`USER`、`ROLE`、`MENU`、`DICT`、`STORAGE`、`MEMBER`、`AUDIT`。
- `NUMBER` 固定使用 5 位数字，从 `00001` 开始递增。
- 后台和前台分别维护 error code 文档，不共享编号空间。
- 同一入口内 error code 不得重复。
- error code 不表达 reason，不携带 HTTP status，不跟随文案变化。
- reason 固定由 messageKey、defaultMessage 和响应 message 表达。
- i18n messageKey 不要求等于 error code。
- 新增业务失败时，必须同步 error code 文档、translator 和契约测试。

文档规则：

- 后台 error code 文档固定为 `docs/30-designs/ADMIN-API-ERROR-CODE-DESIGN.md`。
- 前台 error code 文档固定为 `docs/30-designs/FRONT-API-ERROR-CODE-DESIGN.md`。
- 后台 error code 文档固定记录后台 API 响应 code。
- 前台 error code 文档固定记录前台 API 响应 code。
- 每个 error code 条目固定包含 code、HTTP status、messageKey、defaultMessage、触发场景和归属入口。
- error code 文档在 RUNBOOK 执行阶段作为专项设计文档维护；迁移完成后沉淀到长期 API 协议文档或治理文档。

### 3.6 Global Handler Boundary

`GlobalExceptionHandler` 固定只做响应组装，不承载业务判断。

Handler 规则：

- `GlobalExceptionHandler` 处理 `SandwishException`、参数绑定异常和兜底异常。
- `GlobalExceptionHandler` 不直接理解业务模块异常。
- `GlobalExceptionHandler` 不访问 Service、DAO 或数据库。
- 未被转换的异常统一映射为系统错误。

## 4. Execution Order

### 4.1 固定当前异常现状

1. 统计现有 `*Exception.java`、`ErrorCode`、`GlobalExceptionHandler` 和异常使用点。
2. 删除无生产和测试引用的无效异常类。
3. 清理只服务于已删除异常的 i18n key。
4. 运行 `mvn -pl sandwish-admin-api -am -DfailIfNoTests=false test`。

验收点：

- 无效异常类无残留引用。
- 现有测试通过。
- 当前工作区中无关改动已被隔离。

### 4.2 设计稳定异常模型

1. 在 `sandwish-common-core` 明确业务异常基类：
   - `BizException`
   - `DomainException`
2. 在 API 支撑模块明确响应异常模型：
   - `sandwish-common-web` 中的 `SandwishException`
   - `String` 类型 API response code。
3. 明确 `ErrorCode` 的归属：
   - 业务 code 使用 `String`，不携带 HTTP status。
   - API code 或响应异常携带 HTTP status。
4. 删除 checked `ApiException`。

验收点：

- 异常基类职责唯一。
- `BizException` 不携带 HTTP 语义。
- `SandwishException` 不下沉到 Biz 或 Infra。
- `ApiException` 无生产代码残留。
- API 响应 code 字段为 `String`。

### 4.3 设计 API Error Code 文档

1. 新增 `docs/30-designs/ADMIN-API-ERROR-CODE-DESIGN.md`。
2. 新增 `docs/30-designs/FRONT-API-ERROR-CODE-DESIGN.md`。
3. 固定 error code 格式为 `<DOMAIN>-<NUMBER>`。
4. 按入口分别记录 `COMMON`、`AUTH`、`SYS`、`USER`、`ROLE`、`MENU`、`DICT`、`STORAGE`、`MEMBER`、`AUDIT` 等 code 段。
5. 为已有异常和 translator fallback 预留初始 code。
6. 明确每个 code 的 HTTP status、messageKey、defaultMessage、触发场景和入口归属。

验收点：

- 后台和前台 error code 文档均存在。
- 同一入口内 error code 无重复。
- error code 格式统一为 `<DOMAIN>-<NUMBER>`。
- messageKey / defaultMessage 表达 reason。
- translator 使用的 code 均可在文档中找到。

### 4.4 增加异常转换层

1. 在 API 入口侧新增 `ExceptionTranslator`。
2. 后台入口和前台入口按需要分别提供 translator：
   - `AdminExceptionTranslator`
   - `FrontExceptionTranslator`
3. translator 固定把以下异常转换为 `SandwishException`：
   - `BizException`
   - `DomainException`
   - 权限 / 认证异常
   - 参数绑定异常
   - 未知技术异常
4. `GlobalExceptionHandler` 调用 translator 或只接收 translator 输出后的异常。

验收点：

- Controller 对外失败统一经由 `SandwishException`。
- 业务异常转换不散落在 Controller 方法内部。
- 后台和前台使用不同 `String` code 命名空间。
- translator 输出的业务 code 已记录在对应入口 error code 文档。

### 4.5 迁移 Common 异常

1. 删除 `ApiException` 和全部 `ApiException` 子类。
2. 将可复用的通用异常归并为：
   - 业务层异常
   - API 响应异常
   - 技术异常
3. 把 `NullBeanException`、`MoveTreeNodeException`、`InsertBeanExistException` 等泛业务异常迁移为 `BizException` code。
4. 保留 `BadRequestException`、`UnauthorizedException`、`ForbiddenException`、`NotFoundException`、`ConflictException`、`SystemException` 时，固定它们只作为 API 响应异常或直接并入 `SandwishException` 工厂方法。

验收点：

- `sandwish-common-core` 不再混放 HTTP 响应异常和业务异常。
- checked `ApiException` 已删除。
- 异常 code 和 message 来源明确。

### 4.6 迁移 Biz 层调用点

1. 将 Service 方法签名中的 `throws ApiException` 改为不声明 checked 业务异常。
2. 将业务失败改为抛 `BizException` 或 `DomainException`。
3. 将领域枚举解析失败统一抛 `DomainException`。
4. 增加 `@BizExceptionBoundary` 注解和对应 AOP。
5. 为对外 Service implementation public 方法补充 `@BizExceptionBoundary`。
6. AOP 固定放行 `DomainException` 和 `BizException`。
7. AOP 固定捕获其他技术异常并转换为 `BizException`，保留原始 cause。
8. 删除 Biz 层对 API 响应异常的依赖。

验收点：

- `sandwish-biz` 不依赖 `SandwishException`。
- `sandwish-biz` 不声明 `throws ApiException`。
- 对外 Service implementation public 方法具备 `@BizExceptionBoundary` 门禁覆盖。
- getter 方法以外的 Service implementation public 方法缺少 `@BizExceptionBoundary` 时门禁失败。
- DAO / Infra 技术异常在 Service 边界被转换为 `BizException`。
- 业务异常测试覆盖主要错误码。

### 4.7 迁移 API 层调用点

1. Controller 捕获或透传 translator 转换后的 `SandwishException`。
2. 删除 Controller 方法中业务 checked exception 声明。
3. 认证、权限、参数错误和业务错误统一走响应异常模型。
4. API response code 从 `int` 迁移为 `String`。
5. 后台和前台分别维护业务 code 映射。
6. 更新 Controller contract tests。

验收点：

- Controller 对外错误响应稳定。
- 权限和认证使用正确 HTTP status。
- 业务失败响应包含 `String` 业务 code。
- 业务失败默认 HTTP status 为 `400 Bad Request`。
- API response code 已同步到对应入口 error code 文档。

### 4.8 迁移 Infra 层调用点

1. 扫描 `sandwish-infra` 是否抛业务异常。
2. DAO implementation 不把数据库缺失、唯一冲突或状态冲突直接翻译为业务异常。
3. Service 捕获需要识别的技术异常后翻译为业务异常。
4. 保留第三方异常原始 cause 以便排查。

验收点：

- `sandwish-infra` 无业务异常依赖。
- `sandwish-infra` 无 `InfraException`。
- 技术异常不会被误包装为 API 响应异常。

### 4.9 增加架构门禁

1. 增加 Biz 不依赖 API 响应异常的 ArchUnit 测试。
2. 增加 Infra 不依赖 Biz/API 异常的 ArchUnit 测试。
3. 增加 Controller 不直接抛 Biz / Domain 异常的 ArchUnit 测试。
4. 增加 `ApiException` 禁用或限定使用范围的 ArchUnit 测试。
5. 增加 `@BizExceptionBoundary` 只能标注 Service implementation public 方法的 ArchUnit 测试。
6. 增加对外 Service implementation public 方法必须标注 `@BizExceptionBoundary` 的 ArchUnit 测试，getter 方法除外。
7. 增加 DAO / Mapper / Controller / 领域对象禁止标注 `@BizExceptionBoundary` 的 ArchUnit 测试。
8. 增加 API response code 字段必须为 `String` 的架构或契约测试。
9. 增加 API response code 格式必须符合 `<DOMAIN>-<NUMBER>` 的契约测试。

验收点：

- 异常分层规则可由测试稳定验证。
- 新增异常类违反归属时测试失败。
- 非 getter Service 方法缺少 `@BizExceptionBoundary` 时测试失败。
- `ApiException` 残留时测试失败。
- API response code 格式错误时测试失败。

### 4.10 同步治理文档和任务收口

1. 将稳定异常分层规则写入治理文档。
2. 将 API error code 文档沉淀到长期 API 协议文档或治理文档。
3. 更新 `docs/AGENT.md` 的异常任务和 API error code 任务路由。
4. 根据最终执行结果清理本 RUNBOOK。
5. 删除或收窄对应 `TODO.md` 项。
6. 运行全量或相关模块测试。

验收点：

- 治理文档是长期规则来源。
- RUNBOOK 不作为完成历史保留。
- `TODO.md` 只保留未关闭任务。

## 5. Verification

迁移期间按阶段执行：

```bash
mvn -pl sandwish-common/sandwish-common-core,sandwish-common/sandwish-common-web -am -DfailIfNoTests=false test
mvn -pl sandwish-biz -am -DfailIfNoTests=false test
mvn -pl sandwish-admin-api -am -DfailIfNoTests=false test
mvn -pl sandwish-front-api -am -DfailIfNoTests=false test
```

收口前执行：

```bash
mvn test
rg -n "throws ApiException|new ApiException|extends ApiException" --glob "*.java"
rg -n "class ApiException|interface ApiException" --glob "*.java"
rg -n "SandwishException" sandwish-biz sandwish-infra --glob "*.java"
rg -n "BizException|DomainException" sandwish-admin-api sandwish-front-api --glob "*.java"
rg -n "@BizExceptionBoundary" sandwish-biz sandwish-admin-api sandwish-front-api sandwish-infra --glob "*.java"
rg -n "int code|Integer code|getCode\\(\\)" sandwish-common/sandwish-common-web sandwish-admin-api sandwish-front-api --glob "*.java"
rg -n "\"[A-Z]+-[0-9]{5}\"" sandwish-admin-api sandwish-front-api docs --glob "*.java" --glob "*.md"
```

允许保留的残留必须由对应架构测试或文档显式说明。

## 6. TODO Decomposition Guide

RUNBOOK 审阅通过后，`TODO.md` 固定拆为待审阅任务项。

建议拆分顺序：

1. 异常现状清理任务。
2. 异常模型定义任务。
3. API error code 文档设计任务。
4. Service 异常边界注解和 AOP 任务。
5. API translator 任务。
6. Common 异常迁移任务。
7. Biz Service 异常迁移任务。
8. API Controller 异常迁移任务。
9. Infra 异常边界检查任务。
10. 架构门禁任务。
11. 治理文档同步和 RUNBOOK 清理任务。

每个 TODO 必须精确列出范围文件，不得写目录级泛任务；只有盘点任务允许写搜索模式。

## 7. Open Items

无
