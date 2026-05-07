# Admin Web 命名与目录规则

## Purpose

本文件固定 `sandwish-admin-web` 的前端命名与目录归属规则。

本文件只回答四个问题：

1. 前端页面、组件、service 和类型应该放在哪里。
2. 前端文件、组件、方法和类型应该叫什么名字。
3. 页面专属代码与共享代码如何判断归属。
4. 哪些目录和抽象不作为 admin-web 默认结构。

本文件采用二维结构：按 `Path/Layer/Naming` 组织规则，并按 `Hard Rules（门禁）` 与 `Review Rules（AI/人工审阅）` 分区。
新增规则必须先完成分类归位：先判定 `Hard/Review`，再归入 `Path/Layer/Naming`，禁止新增未分类规则。
`Hard Rules` 必须能够通过 ESLint、TypeScript、测试或架构脚本稳定门禁。
暂时没有门禁支撑的语义判断固定放入 `Review Rules`。

## Scope

当前范围：

- `sandwish-admin-web/src`
- React 页面、布局、组件和路由
- 页面专属 service、共享 service 和通用 API helper
- TypeScript request / response / view model 类型
- 全局 TypeScript 类型声明
- 页面专属样式和全局样式归属
- 前端测试文件放置与命名

不在范围内：

- 不定义 Java 后端 Controller / Service / DAO 命名规则，后端规则见 [`NAMING-AND-PLACEMENT-RULES.md`](./NAMING-AND-PLACEMENT-RULES.md)
- 不定义 HTTP API 后端契约，接口契约以 `sandwish-admin-api` 的 Controller request / response 为准
- 不定义视觉品牌和完整设计系统
- 不替代 `sandwish-admin-web/AGENTS.md` 的 AI 工作入口说明

## Hard Rules（门禁规则，必须稳定）

### Path

- `ADMIN_WEB_PATH_PAGE_SHAPE`：页面固定按 `sandwish-admin-web/src/pages/<module>/<domain>/<domain>-page.tsx` 放置。
- `ADMIN_WEB_PATH_PAGE_COMPONENTS`：页面专属组件固定放在 `sandwish-admin-web/src/pages/<module>/<domain>/components/`。
- `ADMIN_WEB_PATH_PAGE_SERVICE`：页面专属 service 固定放在页面目录，命名为 `sandwish-admin-web/src/pages/<module>/<domain>/<domain>-service.ts`。
- `ADMIN_WEB_PATH_AUTH`：token、权限和登录会话持久化固定放在 `sandwish-admin-web/src/auth/`。
- `ADMIN_WEB_PATH_ROUTER`：路由表和路由保护固定放在 `sandwish-admin-web/src/router/`。
- `ADMIN_WEB_PATH_QUERY`：TanStack Query client 基线固定放在 `sandwish-admin-web/src/query/`。
- `ADMIN_WEB_PATH_GLOBAL_TYPES`：第三方库声明、环境声明和真正跨页面共享的全局前端类型固定放在 `sandwish-admin-web/src/types/`。
- `ADMIN_WEB_PATH_TEST_SUPPORT`：测试支撑固定放在 `sandwish-admin-web/src/test/`。

### Layer

- `ADMIN_WEB_LAYER_PAGE_NO_FETCH`：`src/pages/` 下的页面和组件不得直接调用 `fetch`。
- `ADMIN_WEB_LAYER_LAYOUT_NO_FETCH`：`src/layouts/` 下的布局不得直接调用 `fetch`。
- `ADMIN_WEB_LAYER_SHARED_COMPONENT_NO_PAGE_SERVICE`：`src/components/` 下的共享组件不得导入页面目录中的 `*-service.ts`。
- `ADMIN_WEB_LAYER_API_NO_PAGE`：`src/api/` 不得导入 `src/pages/`、`src/layouts/` 或 `src/components/`。
- `ADMIN_WEB_LAYER_AUTH_NO_PAGE`：`src/auth/` 不得导入 `src/pages/`、`src/layouts/` 或页面 service。
- `ADMIN_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT`：`sandwish-admin-web/src` 下不得使用 `../../` 或更深层级的相对 import；同目录和父级目录引用可以使用 `./` 或 `../`，跨越两层及以上目录时使用 `@/` alias。

### Naming & Placement

- `ADMIN_WEB_NAME_FILE_KEBAB_CASE`：`sandwish-admin-web/src` 下新增文件名固定使用 kebab-case。
- `ADMIN_WEB_NAME_PAGE_FILE`：页面文件固定命名为 `<domain>-page.tsx`。
- `ADMIN_WEB_NAME_PAGE_SERVICE_FILE`：页面专属 service 文件固定命名为 `<domain>-service.ts`。
- `ADMIN_WEB_NAME_PAGE_TYPES_FILE`：页面专属类型文件固定命名为 `<domain>-types.ts`。
- `ADMIN_WEB_NAME_COMPONENT_EXPORT`：React 组件固定使用 PascalCase named export。
- `ADMIN_WEB_NAME_PAGE_EXPORT`：页面组件固定使用 `export const XxxPage = () => {}` 形态。
- `ADMIN_WEB_NAME_SERVICE_METHOD`：service 方法使用动词开头，表达 API 行为，例如 `pageDictionaries`、`addDictionary`、`updateDictionary`、`deleteDictionaries`。
- `ADMIN_WEB_NAME_BOOLEAN`：布尔变量使用 `is`、`has`、`can` 前缀，例如 `canEditDictionary`。
- `ADMIN_WEB_NAME_CONSTANT`：常量使用 `UPPER_SNAKE_CASE`。

## Review Rules（AI/人工审阅，暂不强门禁）

### Path

- 页面目录只承载该页面域直接拥有的文件，不作为跨域共享目录。
- 页面专属 service 不被其他页面域直接导入；如果出现跨页面复用，应先提升到 `src/service/`。
- `src/service/` 中的共享 service 不依赖页面组件、页面状态或页面目录中的类型。
- 跨页面、跨布局或跨路由共享的 service 放在 `src/service/`。
- 通用请求能力、API 协议类型、响应包装解析、token header、base URL 和 API error 放在 `src/api/`。
- 只服务单个页面域的组件放在页面目录下的 `components/`。
- 多个页面域复用的组件放在 `src/components/`。
- 页面专属组件不得从其他页面域目录直接导入。
- 请求 / 响应类型少且只被 service 与同页面 page 使用时，不单独拆文件。
- 类型被同页面多个组件复用，或 service 文件过长时，拆到 `<domain>-types.ts`。
- 类型被多个页面域复用时，提升到 `src/service/` 对应共享 service 或新增明确边界的共享 types 文件。
- API 响应包装、分页响应等后端 API 协议类型放在 `src/api/`，例如 `PageResponse<T>` 放在 `src/api/page-response.ts`。
- 第三方库缺失类型声明、Vite 环境声明和全局前端扩展类型放在 `src/types/`。
- `src/types/` 不承载页面专属 request / response / form values / table record 类型。
- 页面专属样式可以暂时放在 `src/assets/main.css`，但 className 必须用页面域前缀隔离。
- 路由、登录态、权限、请求 hook、布局行为和关键页面加载行为优先覆盖在 `src/app.test.tsx`。
- 页面交互复杂度明显上升时，可以新增同目录或测试目录下的聚焦测试。

### Layer

- `page` 负责页面状态、用户交互、表格/表单组装和调用 service。
- `components` 负责可复用 UI 片段，不直接知道 HTTP 细节。
- `service` 负责 API path、request / response 类型和调用 `postJson`。
- `api` 负责通用 HTTP 能力，不承载业务页面语义。
- `auth` 负责 token、权限和登录会话持久化，不承载页面 UI。
- `router` 负责路由表和路由保护，不承载页面业务交互。
- `query` 负责 TanStack Query client 基线，不承载业务 query key 拼装策略之外的页面逻辑。
- 页面 service 可以调用 `src/api/http.ts`，但不直接处理 token、base URL 或响应包装通用规则。
- `@/` alias 固定指向 `sandwish-admin-web/src/`；跨根目录引用使用 `@/`，同目录或父级目录内引用可以使用 `./` 或 `../`。
- `src/router/` 不直接发起业务 API 请求；路由保护读取登录态和渲染路由组件。
- 共享组件不得依赖具体页面 service、路由路径、权限字符串或业务页面状态。
- 页面内部可以使用 `useQuery` / `useMutation` 编排请求，但请求函数应来自 service。
- 权限字符串优先集中在页面或专门 helper 中，不在多个无关组件中重复散落。
- 测试应验证用户可见结果和关键请求契约，不验证 Ant Design 内部 DOM 细节。

### Naming & Placement

- `<module>` 使用稳定业务模块名，例如 `system`、`auth`、`dashboard`、`storage`。
- `<domain>` 使用页面域名，例如 `dictionary`、`department`、`login`。
- TypeScript interface 请求类型命名优先沿用后端模型语义，例如 `DictPageRequest`、`DictSaveRequest`。
- TypeScript interface 响应类型命名优先沿用后端模型语义，例如 `DictResponse`。
- `src/types/` 下的声明文件固定使用 kebab-case，并以 `.d.ts` 结尾，例如 `sm-crypto.d.ts`。
- 页面内部展示用类型可使用 `XxxViewModel`、`XxxTableRecord` 或 `XxxFormValues`。
- 普通方法和变量使用 camelCase。
- 页面状态变量命名贴近 UI 含义，例如 `query`、`selectedRowKeys`、`editingDictionary`。
- 权限判断变量使用 `canXxx`。
- 页面级 className 必须带页面域前缀，例如 `dictionary-page`、`dictionary-list-panel`。
- 共享组件样式必须使用组件域前缀，避免污染页面样式。

### Forbidden Defaults

- 不新增无边界的 `common`、`base`、`shared` 子目录作为默认归属。
- 不新增与当前技术栈无关的状态管理层，例如全局 store，除非有明确跨页面状态需求。
- 不新增第二套路由、请求、权限或样式体系。
- 不把后端 Java 分层术语机械套进前端目录，例如 `controller`、`dao`、`mapper`、`repository`。
- 不新增 CSS module、styled-components、Tailwind 或其他样式体系，除非先形成明确前端治理决策。
- 不为了“目录统一”进行大规模机械迁移；优先在新增页面或正在修改的页面执行本规则。
- 不为了提前复用抽象出空 `hooks/`、`utils/`、`models/`、`stores/` 等目录。

## Open Items

- 是否把页面专属样式从 `src/assets/main.css` 拆到页面同目录，待页面数量和样式规模增长后再决策。
- 是否为复杂页面固定引入 `<domain>-hooks.ts`，待出现重复且稳定的页面逻辑后再决策。
- 是否为本文件中的 Hard Rules 补充专门的前端架构测试或 ESLint import 限制，待规则定稿后执行。
