# Context Propagation Rules

## 1. Purpose

本文档定义 Sandwich 请求上下文透传的统一规则。

目标是固定后台用户、后台 token、前台会员 token 和线程上下文在请求入口、业务编排、持久化访问和异步执行中的建立方式、读取方式、清理方式与审阅口径。

## 2. Scope

当前范围：

- 后台请求基于 access token 建立 `SandwishContextHolder` 中的 `ADMIN_USER` 主体
- 前台请求基于 member access token 建立 `SandwishContextHolder` 中的 `FRONT_MEMBER` 主体
- Controller、Service、DAO、Mapper 对当前用户或会员上下文的读取边界
- 异步任务、线程池、定时任务等非标准入口的上下文要求
- 判断“上下文透传是否完成”时的统一验收口径

不在范围内：

- 不定义登录、发 token、刷新 token 的业务流程
- 不定义数据库表结构
- 不定义权限编码和权限匹配规则
- 不引入租户隔离模型

## 3. Bounded Context

Sandwich 当前通过统一 `SandwishContextHolder` 表达运行时身份上下文：

- 后台管理端主体：`SandwishSubjectType.ADMIN_USER`
- 前台会员端主体：`SandwishSubjectType.FRONT_MEMBER`

两类上下文不得混用。

业务代码不得直接访问 Spring Security `SecurityContextHolder`，统一通过 `SandwishContextHolder` 读取当前主体。

## 4. Core Model

### 4.1 Admin Context

后台请求上下文建立流程固定如下：

1. 客户端提交 access token。
2. 后台认证过滤器校验 token。
3. 校验通过后，把后台主体写入 `SandwishContextHolder`，主体类型固定为 `ADMIN_USER`。
4. 后续后台链路通过 `SandwishContextHolder` 读取当前主体。
5. Spring Security 负责标准 HTTP 请求结束后的后台身份上下文清理。

### 4.2 Front Member Context

前台会员上下文建立流程固定如下：

1. 前台认证过滤器完成会员认证。
2. 校验通过后，把会员主体写入 `SandwishContextHolder`，主体类型固定为 `FRONT_MEMBER`。
3. 后续前台链路通过 `SandwishContextHolder` 读取当前主体。
4. 登出时按 access token 撤销认证态。

## 5. Module Mapping

- `sandwish-admin-api`
  - 后台认证过滤器负责建立 `SandwishContextHolder` 中的后台主体
  - 后台 Controller、日志、审计和文件访问通过 `SandwishContextHolder` 读取后台身份
- `sandwish-front-api`
  - 前台认证过滤器负责建立 `SandwishContextHolder` 中的会员主体
  - 前台 Controller 和会员访问服务读取 `SandwishContextHolder`
- `sandwish-biz`
  - Service 可以读取已建立的 Sandwich 主体上下文，但不得直接感知 HTTP request
  - 共享业务能力不得把前台会员上下文和后台用户上下文混为同一模型
- `sandwish-infra`
  - 审计字段填充、私有数据过滤和持久化约束可以消费已建立的后台用户上下文
  - DAO / Mapper 不得自行解析 token、session 或 HTTP request
- `sandwish-common-web`
  - `SandwishContextFilter` 负责通过 `SandwishContextHolder` 建立和清理通用请求元数据

## 6. Global Constraints

### 6.1 Source Of Truth

运行时身份上下文的可信来源固定如下：

- 后台当前用户：`SandwishContextHolder` 中的 `ADMIN_USER`
- 后台当前 token：`SandwishContextHolder`
- 前台当前会员：`SandwishContextHolder` 中的 `FRONT_MEMBER`
不得在 Controller、Service、DAO 或 Mapper 中重新解析 token、session、cookie 来绕过上述上下文入口。

### 6.2 Entry Rule

所有依赖当前身份的标准 HTTP 入口都必须先建立上下文，再进入业务方法。

后台入口固定规则：

- access token 校验通过后才能写入 `SandwishContextHolder`
- token 校验失败不得进入业务 Controller
- 请求完成后由 Spring Security 清理后台身份上下文；手工测试或非标准入口必须显式清理
  `SandwishContextHolder`

前台入口固定规则：

- 会员认证通过后才能写入 `SandwishContextHolder`
- 受保护前台路径不得绕过 `SandwishContextHolder` 读取会员身份
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
- Service 需要当前后台用户时，读取 `SandwishContextHolder` 并校验主体类型为 `ADMIN_USER`
- Service 需要当前前台会员时，读取 `SandwishContextHolder` 并校验主体类型为 `FRONT_MEMBER`
- 前后台共享 Service 若同时服务两端，必须让调用方显式传入稳定业务参数，不隐式猜测身份来源

### 6.5 DAO And Mapper Rule

DAO / Mapper 不感知 HTTP、Session 和权限适配。

固定规则：

- DAO / Mapper 不直接接收 `HttpServletRequest`、`HttpServletResponse` 或 `HttpSession`
- DAO / Mapper 不解析 token、cookie、header
- DAO implementation 可以在持久化审计、私有数据过滤等固定场景消费 `SandwishContextHolder` 中已建立的主体
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

- 通用请求元数据固定通过 `SandwishContextHolder` 表达，标准 HTTP 入口由 `SandwishContextFilter` 建立并清理
- 跨线程任务不得默认透传 `SandwishContextHolder`；确需 requestId 等请求元数据时，调用方必须显式读取并作为普通参数传入
- 需要当前用户或会员身份的异步任务，必须显式传入稳定业务参数，或在进入业务前手工建立对应上下文
- 不得假定 `SandwishContextHolder` 会自动跨线程存在
- 异步任务完成后必须通过 `SandwishContextHolder.clear()` 清理手工建立的身份上下文
- 线程池、MQ、定时任务等非 HTTP 入口手工建立 `SandwishContextHolder` 时，必须在任务结束的 `finally` 中清理对应上下文
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

- 在业务代码中直接读取 Spring Security `SecurityContextHolder`
- 在前台代码中把 `ADMIN_USER` 当作会员
- 在后台代码中把 `FRONT_MEMBER` 当作后台用户
- 在 Service 中直接接收 `HttpServletRequest`
- 在 DAO / Mapper 中解析 token 或 session
- 异步任务中直接读取当前线程上下文
- 前台登出只清理 Spring Security，不撤销 access token
- 手工设置 `SandwishContextHolder` 后没有清理

## 9. Open Items

无
