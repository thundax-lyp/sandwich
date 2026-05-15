# OPEN API REQUIREMENTS

## 1. Purpose

本文档定义 Sandwich 开放接口入口 `sandwish-open-api` 和第三方主体 `OpenClient` 的业务需求边界。

`Open API` 面向第三方系统提供业务模块接口。它不是后台管理接口的透传，也不是前台会员接口的复用；它是与 `sandwish-admin-api`、`sandwish-front-api` 并列的独立入口模块。

`OpenClient` 是与 `User`、`Member` 对等的可认证主体主数据。API KEY / API SECRET 认证继续复用 `auth_principal_identity` 和 `auth_principal_credential` 统一认证底座。

签名认证细节见 [`../30-designs/OPEN-API-AUTH-DESIGN.md`](../30-designs/OPEN-API-AUTH-DESIGN.md)。

错误码设计见 [`../30-designs/OPEN-API-ERROR-CODE-DESIGN.md`](../30-designs/OPEN-API-ERROR-CODE-DESIGN.md)。

## 2. Scope

当前覆盖范围：

- `sandwish-open-api` 入口模块。
- OpenClient 主体。
- OpenClient 权限。
- API KEY / API SECRET 认证边界。
- HMAC 签名认证。
- OpenClient 后台管理 API。
- OpenClient 后台管理页面。
- Open API Submission 创建入口。
- Open API Submission 图片上传入口。
- Open API 与 Audit 操作者上下文衔接。

当前不覆盖范围：

- 第三方开发者门户。
- SDK、示例工程或 Postman 集合。
- OAuth2 client credentials。
- 非对称签名。
- Open API 独立调用日志表。
- 除 Submission 以外的业务模块开放。

## 3. Bounded Context

`Open API` 固定表达“第三方系统以 OpenClient 身份调用 Sandwich 业务能力”。

`Open API` 不承载后台用户管理、前台会员登录、菜单权限或角色授权职责。

`OpenClient` 是第三方调用主体，归属开放接口主体管理能力。它只保存第三方主体业务资料、状态、过期时间、IP 白名单和备注等配置；API KEY 和 API SECRET 不保存到 `open_client` 主表。

`OpenClientPermission` 是 OpenClient 到业务权限码的直接映射。OpenClient 不使用后台 `sys_menu` / `sys_role` 组合授权模型。

Open API 写入业务对象时，业务对象不保存 OpenClient 来源字段。来源归属 Open API 认证上下文和 Audit operator。

## 4. Module Mapping

- `sandwish-open-api`
  - 提供第三方开放接口 HTTP 入口。
  - 提供 Open API auth filter / interceptor。
  - 读取 API KEY、timestamp、nonce、body hash 和 signature。
  - 校验 HMAC 签名、防重放、OpenClient 状态、过期和 IP 白名单。
  - 校验 OpenClient 权限。
  - 注入 `SandwishContextHolder`。
  - 提供 Open API 专属错误响应适配。
- `sandwish-biz/src/main/java/com/github/thundax/modules/open`
  - 定义 `OpenClient`、`OpenClientPermission`、状态枚举、DAO 契约和业务 Service。
  - 提供 OpenClient 主体查询、权限查询和状态校验能力。
- `sandwish-biz/src/main/java/com/github/thundax/modules/auth`
  - 继续承载 `PrincipalIdentity`、`PrincipalCredential` 和 Principal 认证底座。
  - 提供 Principal identity / credential 查询和状态校验能力。
- `sandwish-infra/src/main/java/com/github/thundax/modules/open`
  - 实现 `open_client`、`open_client_permission` 持久化对象、Mapper、DAO implementation 和持久化转换。
- `sandwish-infra/src/main/java/com/github/thundax/modules/auth`
  - 继续实现 `auth_principal_identity`、`auth_principal_credential` 持久化能力。
- `sandwish-admin-api`
  - 提供 OpenClient 管理入口。
- `sandwish-admin-web`
  - 提供 OpenClient 管理页面。

`sandwish-open-api` 不依赖 `sandwish-admin-api` 或 `sandwish-front-api`。

## 5. Core Business Objects

### 5.1 OpenClient

`OpenClient` 是开放接口第三方主体。

核心字段：

- `id`：OpenClient ID。
- `name`：第三方主体名称。
- `status`：OpenClient 状态。
- `expiredAt`：过期时间。
- `ipWhitelist`：IP 白名单。
- `remarks`：备注。
- `permissions`：OpenClient 权限列表。

固定状态：

- `ENABLED`：可调用 Open API。
- `DISABLED`：不可调用 Open API。

固定约束：

- `OpenClient` 是与 `User`、`Member` 对等的可认证主体。
- API KEY 固定通过 `auth_principal_identity` 保存。
- API SECRET 固定通过 `auth_principal_credential` 保存可验证材料。
- API SECRET 明文只在创建或重置时返回一次。
- OpenClient 禁用后全部 API KEY 和 API SECRET 均不可用于 Open API 调用。
- OpenClient 过期后不得继续调用 Open API。
- IP 白名单为空时固定表示不限制调用来源 IP。
- `ipWhitelist` 固定使用 JSON array 字符串保存。
- `ipWhitelist` 元素固定支持单 IP 和 CIDR。
- `ipWhitelist` 不支持域名。

### 5.2 OpenClientPermission

`OpenClientPermission` 是 OpenClient 到业务权限码的直接映射。

核心字段：

- `id`：权限映射 ID。
- `clientId`：OpenClient ID。
- `permission`：业务权限码。

固定约束：

- 不引入 role。
- 不引入 menu。
- 不引入权限树。
- `clientId + permission` 必须唯一。
- 权限码使用业务模块权限码，不使用 `open:` 前缀。

Submission 最小开放权限：

- `submission:submission:create`
- `submission:submission:image:upload`

### 5.3 API KEY

API KEY 是 OpenClient 的登录标识。

固定映射：

```text
auth_principal_identity.principal_type = OPEN_CLIENT
auth_principal_identity.principal_id = open_client.id
auth_principal_identity.identity_type = API_KEY
auth_principal_identity.identity_value = apiKey
```

固定约束：

- API KEY 必须全局唯一。
- API KEY 可被禁用。
- API KEY 不保存到 `open_client` 主表。
- API KEY 可在后台管理入口创建或重置。

### 5.4 API SECRET

API SECRET 是 OpenClient 的认证凭据。

固定映射：

```text
auth_principal_credential.principal_type = OPEN_CLIENT
auth_principal_credential.principal_id = open_client.id
auth_principal_credential.identity_id = API KEY identity id
auth_principal_credential.credential_type = API_SECRET
```

固定约束：

- API SECRET 明文只在创建或重置时返回一次。
- 系统不得提供 API SECRET 明文查询能力。
- Open API 使用 HMAC 签名认证时，服务端必须保存可验证材料。
- API SECRET 不按普通 password hash 语义处理。
- API SECRET 可验证材料固定使用加密密文保存。
- 服务端校验签名前解密得到 signing secret。

## 6. Global Constraints

### 6.1 Entry Boundary

Open API 入口模块固定命名为：

```text
sandwish-open-api
```

应用入口固定命名为：

```text
com.github.thundax.OpenApiApplication
```

默认 context path 固定为：

```text
/open-api
```

Open API Controller 固定按业务 module 平移，不复用 admin-api Controller。

### 6.2 Auth Boundary

Open API 不使用登录态 token。

Open API 不使用以下流程：

- pre-auth-session。
- captcha。
- 后台账号密码登录。
- 前台会员登录。
- access token / refresh token。
- OAuth2 authorize / token。
- 后台菜单权限会话。

Open API 可以复用 `biz.auth` 的以下能力：

- PrincipalIdentity 查询。
- PrincipalCredential 查询。
- Principal / identity / credential 状态校验。
- Principal 与业务主体坐标关系。
- 认证失败的通用异常封装。

HTTP header 读取、签名串构造、HMAC 校验、nonce 防重放和 OpenClient 上下文注入固定归属 `sandwish-open-api`。

### 6.3 Signature Boundary

Open API 固定使用 HMAC-SHA256 签名认证。

请求头固定为：

```text
X-Sandwish-Api-Key
X-Sandwish-Timestamp
X-Sandwish-Nonce
X-Sandwish-Content-SHA256
X-Sandwish-Signature
```

签名 canonical request 固定包含：

```text
METHOD
PATH
QUERY_STRING
TIMESTAMP
NONCE
CONTENT_SHA256
```

固定约束：

- 服务端必须校验 timestamp 时间窗。
- 服务端必须校验 nonce 不重复。
- 服务端必须校验 body hash。
- 服务端必须使用常量时间比较签名。
- 认证失败不得进入业务 Service。

### 6.4 Permission Boundary

Open API 权限固定来自 `open_client_permission`。

权限码使用业务模块权限码，例如：

```text
submission:submission:create
submission:submission:image:upload
```

Open API 不使用后台 `sys_menu`、`sys_role` 或 `PrincipalAuthSession.values["PERMISSIONS"]`。

### 6.5 Audit Boundary

Open API 认证成功后，必须注入统一主体上下文。

固定主体语义：

```text
SandwishSubjectType.OPEN_CLIENT
```

Audit operator resolver 必须支持：

```text
SandwishSubjectType.OPEN_CLIENT -> AuditOperatorType.OPEN_CLIENT
```

业务写操作产生 Audit 时，操作者必须能追踪到 OpenClient。

### 6.6 Submission Boundary

Open API 当前只开放 Submission 最小写入能力。

Open API Submission 入口固定调用 `SubmissionService`，不得直接访问 DAO、Mapper、Storage store 或底层对象存储。

Open API 上传 Submission 图片时，必须复用 Storage 上传能力，上传后的存储对象 `ownerType` 固定为 `SUBMISSION`。

## 7. Functional Requirements

### 7.1 Manage OpenClient

系统必须支持后台管理 OpenClient。

管理能力固定包含：

- 创建 OpenClient。
- 启用 OpenClient。
- 禁用 OpenClient。
- 重置 API SECRET。
- 维护 IP 白名单。
- 维护过期时间。
- 维护 OpenClient 权限。

首批后台管理权限固定为：

- `open:client:view`
- `open:client:edit`

OpenClient 管理权限随开放业务模块增加同步扩展。

创建 OpenClient 成功后：

- 写入 `open_client`。
- 写入 API KEY 对应的 `auth_principal_identity`。
- 写入 API SECRET 对应的 `auth_principal_credential` 可验证材料。
- 返回 API KEY。
- 返回 API SECRET 明文一次。

### 7.2 Authenticate Open API Request

系统必须支持使用 API KEY / API SECRET 签名认证 Open API 请求。

认证成功后：

- OpenClient 身份被解析。
- OpenClient 状态、过期时间和 IP 白名单被校验。
- nonce 被记录到短期运行态存储。
- OpenClient 上下文被注入。
- 请求继续进入权限校验。

认证失败后：

- 请求不得进入业务 Service。
- 响应 Open API 认证失败错误。
- 不记录业务数据审计。

### 7.3 Authorize Open API Request

系统必须支持按 OpenClient 权限校验业务接口调用。

权限校验成功后，请求继续进入 Controller。

权限校验失败后，请求不得进入业务 Service。

### 7.4 Create Submission

Open API 必须支持第三方创建 Submission。

固定入口：

| Method | URL | Permission | Description |
| --- | --- | --- | --- |
| `POST` | `/api/submission/submission/create` | `submission:submission:create` | 创建提交内容 |

创建请求固定包含：

- `title`
- `content`
- `imageObjectIds`

创建成功后：

- 写入 Submission。
- 写入 SubmissionImage 列表。
- 建立 Storage 引用关系。
- 记录 `CREATE` 审计日志。
- Audit 操作者为 OpenClient。

创建请求固定不包含：

- OpenClient ID。
- API KEY。
- priority。
- status。
- Storage 元数据。

### 7.5 Upload Submission Image

Open API 必须支持第三方上传 Submission 图片。

固定入口：

| Method | URL | Permission | Description |
| --- | --- | --- | --- |
| `POST` | `/api/submission/submission/image/upload` | `submission:submission:image:upload` | 上传提交内容图片 |

上传成功后：

- 返回 Storage 对象 ID。
- Storage 对象 `ownerType` 固定为 `SUBMISSION`。
- Storage 对象暂不绑定 Submission ID。
- 创建 Submission 时通过 `imageObjectIds` 建立业务引用。

### 7.6 Reset API SECRET

系统必须支持重置 API SECRET。

重置成功后：

- 旧 API SECRET 立即失效。
- 新 API SECRET 明文只在响应中出现一次。
- 服务端保存新的可验证材料。

## 8. Key Flows

### 8.1 Open API Authentication Flow

Open API 请求认证流程固定如下：

1. 读取 `X-Sandwish-Api-Key`。
2. 根据 API KEY 查询 `auth_principal_identity`。
3. 校验 identity 存在且状态可用。
4. 校验 identity 的 `principal_type=OPEN_CLIENT`。
5. 使用 `principal_id` 查询 `open_client`。
6. 校验 `open_client` 存在、状态可用、未过期。
7. 校验调用方 IP 命中 `open_client` 白名单。
8. 查询 `auth_principal_credential` 中 `credential_type=API_SECRET` 的凭据。
9. 读取服务端签名校验材料。
10. 校验 `X-Sandwish-Timestamp` 在允许时间窗内。
11. 校验 `X-Sandwish-Nonce` 在同一 API KEY 的时间窗内未使用。
12. 校验 request body SHA-256 与 `X-Sandwish-Content-SHA256` 一致。
13. 重建 canonical request。
14. 计算 HMAC-SHA256。
15. 常量时间比较服务端签名和 `X-Sandwish-Signature`。
16. 查询 `open_client_permission`。
17. 注入 OpenClient 上下文。
18. 进入 Controller 权限校验和业务处理。

### 8.2 Submission Image And Create Flow

第三方提交图片和内容流程固定如下：

1. 第三方调用 Submission 图片上传接口。
2. Open API 完成认证和 `submission:submission:image:upload` 权限校验。
3. Storage 上传能力写入存储对象，`ownerType=SUBMISSION`。
4. 接口返回 Storage 对象 ID。
5. 第三方调用 Submission 创建接口并传入 `imageObjectIds`。
6. Open API 完成认证和 `submission:submission:create` 权限校验。
7. `SubmissionService` 写入 Submission 和 SubmissionImage。
8. `SubmissionService` 绑定 Storage 引用关系。
9. Audit 记录 OpenClient 操作者。

## 9. Non-Functional Requirements

### 9.1 Timestamp Window

Open API 请求 timestamp 必须在服务端允许时间窗内。

默认时间窗固定为：

```text
5 minutes
```

超过时间窗的请求必须拒绝。

### 9.2 Nonce Store

nonce 必须按 API KEY 维度防重放。

固定 key：

```text
open-api:nonce:{apiKey}:{nonce}
```

nonce TTL 必须等于或略大于 timestamp 时间窗。

nonce 写入必须满足“仅当不存在才写入”的原子语义。

首批实现使用 `sandwish-open-api` 进程内内存 TTL store，不依赖磁盘落盘。多节点部署前必须替换为 Redis、JetCache 或等价集中式原子存储，并保持上述 key、TTL 和原子写入语义。

### 9.3 Logging

首批 Open API 不增加独立调用日志表。

Open API 运行日志不得保存 API SECRET 明文。

日志中不得输出可验证签名材料。

认证失败日志应能定位失败类型，但不得泄露签名串细节、secret 或完整敏感 header。

### 9.4 Security

- API SECRET 明文只展示一次。
- API SECRET 不允许通过查询接口读取。
- API SECRET、加密密文和解密后的 signing secret 按敏感凭据处理。
- 签名比较必须使用常量时间比较。
- body hash 必须参与签名。
- timestamp 和 nonce 必须参与签名。
- OpenClient 禁用或过期时必须拒绝请求。
- OpenClient 缺少业务权限时必须拒绝请求。
- Open API 请求不得绕过 `SubmissionService` 的业务校验和审计。

## 10. Integration Requirements

Open API 与现有模块的集成规则：

- 与 `auth` 集成：复用 `PrincipalIdentity` 和 `PrincipalCredential`。
- 与 `submission` 集成：复用 `SubmissionService`。
- 与 `storage` 集成：复用 Storage 上传和引用能力。
- 与 `audit` 集成：写操作以 OpenClient 作为操作者。
- 与 `sys` 集成：不复用 menu / role / 用户权限会话。

## 11. Open Items

无
