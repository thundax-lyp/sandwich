# Context Propagation Rules

## 1. Purpose

本文档定义 Sandwich 请求上下文透传的统一规则。

目标是固定后台用户、后台 token、前台会员 token 和线程上下文在请求入口、业务编排、持久化访问和异步执行中的建立方式、读取方式、清理方式与审阅口径。

## 2. Scope

当前范围：

- 后台请求基于 access token 建立 `UserAccessHolder`
- 前台请求基于 member access token 建立 `MemberSecurityContext`
- Controller、Service、DAO、Mapper 对当前用户或会员上下文的读取边界
- `PooledThreadLocal` 在请求结束后的清理边界
- 异步任务、线程池、定时任务等非标准入口的上下文要求
- 判断“上下文透传是否完成”时的统一验收口径

不在范围内：

- 不定义登录、发 token、刷新 token 的业务流程
- 不定义数据库表结构
- 不定义权限编码和权限匹配规则
- 不引入租户隔离模型

## 3. Bounded Context

Sandwich 当前存在两类运行时身份上下文：

- 后台管理端上下文：`UserAccessHolder`
- 前台会员端上下文：`MemberSecurityContext`

两类上下文不得混用。

后台 API 不从 `MemberSecurityContext` 读取当前用户。前台 API 不从 `UserAccessHolder` 读取当前会员。

## 4. Core Model

### 4.1 Admin Context

后台请求上下文建立流程固定如下：

1. 客户端提交 access token。
2. 后台认证过滤器校验 token。
3. 校验通过后，把 `userId` 和 token 写入 `UserAccessHolder`。
4. 后续后台链路通过 `UserAccessHolder.currentUserId()` 和 `UserAccessHolder.currentToken()` 读取。
5. 后台认证过滤器完成请求后必须调用 `UserAccessHolder.clear()` 清理后台身份上下文。
6. 请求完成后，通过 `PooledThreadLocalFilter` 兜底清理线程上下文。

### 4.2 Front Member Context

前台会员上下文建立流程固定如下：

1. 前台认证过滤器完成会员认证。
2. Spring Security `Authentication` 保存 `MemberSpringPrincipal`。
3. 后续前台链路通过 `MemberSecurityContext.getPrincipal()` 或 `MemberSecurityContext.getCurrentMemberId()` 读取。
4. 登出时按 access token 撤销认证态。

## 5. Module Mapping

- `sandwish-admin-api`
  - 后台认证过滤器负责建立 `UserAccessHolder`
  - 后台 Controller、日志、审计和文件访问可以读取 `UserAccessHolder`
- `sandwish-front-api`
  - 前台 Spring Security 过滤器负责建立 `MemberSecurityContext`
  - 前台 Controller 和会员访问服务读取 `MemberSecurityContext`
- `sandwish-biz`
  - Service 可以读取当前后台用户上下文，但不得直接感知 HTTP request
  - 共享业务能力不得把前台会员上下文和后台用户上下文混为同一模型
- `sandwish-infra`
  - 审计字段填充、私有数据过滤和持久化约束可以消费已建立的后台用户上下文
  - DAO / Mapper 不得自行解析 token、session 或 HTTP request
- `sandwish-common-core`
  - `PooledThreadLocal` 提供线程上下文容器
  - `PooledThreadLocalFilter` 负责请求结束后的线程上下文清理

## 6. Global Constraints

### 6.1 Source Of Truth

运行时身份上下文的可信来源固定如下：

- 后台当前用户：`UserAccessHolder`
- 后台当前 token：`UserAccessHolder`
- 前台当前会员：`MemberSecurityContext`
不得在 Controller、Service、DAO 或 Mapper 中重新解析 token、session、cookie 来绕过上述上下文入口。

### 6.2 Entry Rule

所有依赖当前身份的标准 HTTP 入口都必须先建立上下文，再进入业务方法。

后台入口固定规则：

- access token 校验通过后才能写入 `UserAccessHolder`
- token 校验失败不得进入业务 Controller
- 请求完成后必须显式清理 `UserAccessHolder`
- 请求完成后必须通过 `PooledThreadLocalFilter` 兜底清理 `PooledThreadLocal`

前台入口固定规则：

- 会员认证通过后才能写入 Spring Security `Authentication`
- 受保护前台路径不得绕过 Spring Security 读取会员身份
- 登出必须撤销 access token，不依赖 HTTP session

### 6.3 Controller Rule

Controller 负责请求入口适配，可以读取当前上下文用于：

- 当前用户或会员身份适配
- 权限、日志、上传 owner 等入口级参数补充
- 响应组装中需要的当前 token 或当前会员信息

Controller 不得把上下文解析逻辑复制成私有工具方法。

### 6.4 Service Rule

Service 可以消费已建立的当前身份上下文，但不得依赖 HTTP API 模型。

固定规则：

- Service 不直接接收 `HttpServletRequest`、`HttpServletResponse` 或 `HttpSession`
- Service 不直接解析 token、cookie、header
- Service 需要当前后台用户时，读取 `UserAccessHolder`
- Service 需要当前前台会员时，由前台专用 Service 读取 `MemberSecurityContext`
- 前后台共享 Service 若同时服务两端，必须让调用方显式传入稳定业务参数，不隐式猜测身份来源

### 6.5 DAO And Mapper Rule

DAO / Mapper 不感知 HTTP、Session 和权限适配。

固定规则：

- DAO / Mapper 不直接接收 `HttpServletRequest`、`HttpServletResponse` 或 `HttpSession`
- DAO / Mapper 不解析 token、cookie、header
- DAO implementation 可以在持久化审计、私有数据过滤等固定场景消费 `UserAccessHolder.currentUserId()`
- Mapper interface 和 Mapper XML / 注解 SQL 不直接引用上下文工具

### 6.6 Async And Non-Standard Entry Rule

所有跨线程执行都必须视为新的上下文边界。

适用对象包括：

- `@Async`
- 手工线程池
- `CompletableFuture`
- 定时任务
- 启动任务
- 人工修复脚本

固定规则：

- 通用请求元数据透传使用 `sandwish-common-core` 中已有的 `ContextSnapshot`、`ContextAwareRunnable` 和 `ContextAwareCallable`
- `ContextAwareRunnable` / `ContextAwareCallable` 只负责 `SandwishContextHolder` 中的通用请求元数据，不自动搬运后台 `UserAccessHolder` 或前台 Spring Security `Authentication`
- 需要当前用户或会员身份的异步任务，必须显式传入稳定业务参数，或在进入业务前手工建立对应上下文
- 不得假定 `PooledThreadLocal` 或 Spring Security 上下文会自动跨线程存在
- 异步任务完成后必须清理手工建立的线程上下文；后台身份上下文使用 `UserAccessHolder.clear()` 清理
- 无法建立完整上下文的非标准入口，不得进入依赖当前身份的业务逻辑

### 6.7 Cache Rule

后台业务缓存不得把当前用户或 token 隐式藏在全局 key 中；如果缓存内容与当前用户有关，缓存 key 必须显式表达用户维度。

## 7. Review Checklist

新增或修改依赖当前身份的代码时，固定检查：

1. 当前链路属于后台用户上下文还是前台会员上下文。
2. 标准 HTTP 入口是否已经建立对应上下文。
3. 是否存在重复解析 token、cookie、session、header 的代码。
4. Service 是否直接依赖 HTTP API 模型或 Servlet 对象。
5. DAO / Mapper 是否直接感知 HTTP、Session 或权限适配。
6. 异步、定时或非标准入口是否显式建立或传递身份参数。
7. 请求结束或任务完成后是否清理线程上下文。

## 8. Common Mistakes

- 在前台代码中读取 `UserAccessHolder`
- 在后台代码中读取 `MemberSecurityContext`
- 在 Service 中直接接收 `HttpServletRequest`
- 在 DAO / Mapper 中解析 token 或 session
- 异步任务中直接读取当前线程上下文
- 前台登出只清理 Spring Security，不撤销 access token
- 手工设置 `PooledThreadLocal` 后没有清理

## 9. Open Items

无
