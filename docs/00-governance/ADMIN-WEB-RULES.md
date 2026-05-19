# Admin Web Rules

## Purpose

本文件固定 `sandwish-admin-web` 的前端治理规则。

本文件覆盖：

- 前端架构与分层
- 命名与目录归属
- UI 与交互规范
- Service、API、状态与权限边界
- 测试与验证规则

## Scope

当前范围：

- `sandwish-admin-web/src`
- `sandwish-admin-web/e2e`
- React、TypeScript、Vite、Ant Design 管理台代码

不在范围内：

- Java 后端命名、路径与分层规则
- HTTP API 后端契约
- 完整品牌手册
- 非 admin-web 前端工程

## Principles

- 一致性优先于灵活性。
- 约定优先于配置。
- 能机器门禁的规则优先机器门禁。
- 相同交互必须使用相同实现。
- 不依赖开发者记忆规则；规则应尽量沉淀到共享组件、TypeScript、ESLint、Playwright 和 Code Review。

## Rule Structure

规则分为两个层级：

- `Hard Rules`：必须可由 ESLint、TypeScript、测试或架构脚本稳定门禁。
- `Review Rules`：由 AI 或人工审阅执行，暂不强制门禁。

新增规则应先归入以下主题之一：

- `Architecture`
- `Naming`
- `Placement`
- `UI`
- `Service`
- `State`
- `Permission`
- `Testing`

`Hard Rules` 必须能够通过 ESLint、TypeScript、测试或架构脚本稳定门禁。
暂时没有门禁支撑的语义判断固定放入 `Review Rules`。
门禁报错信息必须包含本文件中的规则标签，例如 `ADMIN_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT`。

## Hard Rules

### Architecture

- `ADMIN_WEB_LAYER_FETCH_ONLY_HTTP`：`sandwish-admin-web/src` 下只有 `src/api/http.ts` 可以直接调用 `fetch`。
- `ADMIN_WEB_LAYER_POST_HELPER_SERVICE_ONLY`：`postJson` 和 `postFormData` 只允许在 `*-service.ts` 中导入和调用。
- `ADMIN_WEB_LAYER_SHARED_COMPONENT_NO_PAGE_SERVICE`：`src/components/` 下的共享组件不得导入页面目录中的 `*-service.ts`。
- `ADMIN_WEB_LAYER_API_NO_PAGE`：`src/api/` 不得导入 `src/pages/`、`src/layouts/` 或 `src/components/`。
- `ADMIN_WEB_LAYER_AUTH_NO_PAGE`：`src/auth/` 不得导入 `src/pages/`、`src/layouts/` 或页面 service。
- `ADMIN_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT`：`sandwish-admin-web/src` 下不得使用 `../../` 或更深层级的相对 import；同目录和父级目录引用可以使用 `./` 或 `../`，跨越两层及以上目录时使用 `@/` alias。
- `ADMIN_WEB_LAYER_PAGE_NO_PARENT_RELATIVE_IMPORT`：`src/pages/<module>/<domain>/<domain>-page.tsx` 不得使用 `../` 相对 import；本页目录内引用使用 `./`，跨页面域或共享目录引用使用 `@/`。
- `ADMIN_WEB_LAYER_PAGE_COMPONENT_NO_EXTERNAL_PAGE`：`src/pages/<module>/<domain>/components/` 下文件不得引用当前页面域之外的 `src/pages/` 内容；当前页面域内引用使用 `./` 或 `../`，共享能力使用 `@/`。

### Naming

- `ADMIN_WEB_NAME_FILE_KEBAB_CASE`：`sandwish-admin-web/src` 下新增文件名固定使用 kebab-case。
- `ADMIN_WEB_NAME_PAGE_FILE`：页面文件固定命名为 `<domain>-page.tsx`。
- `ADMIN_WEB_NAME_PAGE_STYLE_FILE`：页面专属样式文件固定与页面同目录，命名为 `<domain>-page.css`。
- `ADMIN_WEB_NAME_PAGE_SERVICE_FILE`：页面专属 service 文件固定命名为 `<domain>-service.ts`。
- `ADMIN_WEB_NAME_PAGE_TYPES_FILE`：页面专属类型文件固定命名为 `<domain>-types.ts`。
- `ADMIN_WEB_NAME_COMPONENT_EXPORT`：React 组件固定使用 PascalCase named export。
- `ADMIN_WEB_COMPONENT_SINGLE_EXPORT`：`src/pages/<module>/<domain>/components/*.tsx` 每个文件最多导出一个 PascalCase React 组件；私有子组件不导出，可以留在同文件。
- `ADMIN_WEB_NAME_PAGE_EXPORT`：页面组件固定使用 `export const XxxPage = () => {}` 形态。
- `ADMIN_WEB_NAME_FUNCTION_ARROW`：前端方法默认使用箭头函数，不使用 function declaration。
- `ADMIN_WEB_NAME_NO_NESTED_TERNARY`：前端代码禁止使用嵌套三元表达式。
- `ADMIN_WEB_NAME_SERVICE_METHOD`：service 方法使用动词开头，表达 API 行为；允许的动词前缀固定为 `page`、`list`、`get`、`add`、`create`、`change`、`remove`、`sort`、`move`、`upload`、`download`、`reset`、`login`、`logout`、`refresh`、`load`、`save`；页面域主资源方法可以省略领域名，例如 `page`、`add`、`changeInfo`、`removeBatch`；非主资源或补充资源方法必须带对象名，例如 `listTypes`、`changePassword`、`uploadAvatar`。
- `ADMIN_WEB_NAME_BOOLEAN`：布尔变量使用 `is`、`has`、`can` 前缀，例如 `canEditDictionary`。
- `ADMIN_WEB_NAME_CONSTANT`：常量使用 `UPPER_SNAKE_CASE`。

### Placement

- `ADMIN_WEB_PATH_PAGE_SHAPE`：页面固定按 `sandwish-admin-web/src/pages/<module>/<domain>/<domain>-page.tsx` 放置。
- `ADMIN_WEB_PATH_PAGE_COMPONENTS`：页面专属组件固定放在 `sandwish-admin-web/src/pages/<module>/<domain>/components/`。
- `ADMIN_WEB_PATH_PAGE_SERVICE`：页面专属 service 固定放在页面目录，命名为 `sandwish-admin-web/src/pages/<module>/<domain>/<domain>-service.ts`。
- `ADMIN_WEB_PATH_AUTH`：token、权限和登录会话持久化固定放在 `sandwish-admin-web/src/auth/`。
- `ADMIN_WEB_PATH_ROUTER`：路由表和路由保护固定放在 `sandwish-admin-web/src/router/`。
- `ADMIN_WEB_PATH_QUERY`：TanStack Query client 基线固定放在 `sandwish-admin-web/src/query/`。
- `ADMIN_WEB_PATH_GLOBAL_TYPES`：第三方库声明、环境声明和真正跨页面共享的全局前端类型固定放在 `sandwish-admin-web/src/types/`。
- `ADMIN_WEB_PATH_TEST_SUPPORT`：测试支撑固定放在 `sandwish-admin-web/src/test/`。
- `ADMIN_WEB_PATH_E2E_PAGE_SPEC`：页面 E2E 测试固定放在 `sandwish-admin-web/e2e/<module>/<domain>/<domain>.spec.ts`，对应 `sandwish-admin-web/src/pages/<module>/<domain>/<domain>-page.tsx`。
- `ADMIN_WEB_PATH_E2E_LAYOUT_SPEC`：不归属单个页面的后台壳层、布局和跨页面导航 E2E 测试固定放在 `sandwish-admin-web/e2e/layout/*.spec.ts`。

### UI

### Service

### State

### Permission

### Testing

## Review Rules

### Architecture

- 复杂业务逻辑不得直接写在 JSX 中。
- 页面应优先复用项目已有共享组件和页面骨架。
- `@/` alias 固定指向 `sandwish-admin-web/src/`；跨根目录引用使用 `@/`。
- `src/router/` 不直接发起业务 API 请求；路由保护读取登录态和渲染路由组件。
- 共享组件不得依赖具体页面 service、路由路径、权限字符串或业务页面状态。
- `Sandwish*` 通用技术组件不承载业务语义，不引用业务 CSS token。
- `ListPage` 只承载列表页通用编排，查询条件、表格数据、批量动作和弹窗状态由业务页面拥有。
- 页面内部可以使用 `useQuery` / `useMutation` 编排请求，但请求函数应来自 service。

### Naming

- React 组件使用 PascalCase。
- Hook 使用 `useXxx`。
- 常量使用 `UPPER_SNAKE_CASE`。
- 状态动作文案使用明确业务动词，例如 `启用`、`禁用`、`发布`、`下线`、`归档`、`恢复`。
- 避免使用泛化动作文案，例如 `操作`、`变更状态`、`处理`。
- `<module>` 使用稳定业务模块名，例如 `system`、`auth`、`dashboard`、`storage`。
- `<domain>` 使用页面域名，例如 `dictionary`、`department`、`login`。
- TypeScript interface 请求类型命名优先沿用后端模型语义，例如 `DictPageRequest`、`DictSaveRequest`。
- TypeScript interface 响应类型命名优先沿用后端模型语义，例如 `DictResponse`。
- `src/types/` 下的声明文件固定使用 kebab-case，并以 `.d.ts` 结尾，例如 `sm-crypto.d.ts`。
- 页面内部展示用类型可使用 `XxxViewModel`、`XxxTableRecord` 或 `XxxFormValues`。
- 普通方法和变量使用 camelCase。
- 页面状态变量命名贴近 UI 含义，例如 `query`、`selectedRowKeys`、`editingDictionary`。
- 权限判断变量使用 `canXxx`。
- `Sandwish*` 只用于项目自有通用 UI 技术组件，不承载具体业务价值；业务页面、业务表单、业务弹窗和业务操作组件使用真实业务名，例如 `UserPage`、`UserFilterForm`、`UserEditDrawer`。
- 通用业务页面骨架不使用 `Sandwish*` 前缀，例如 `ListPage`；它表达稳定页面范式，内部可以组合 `Sandwish*` 技术组件。
- 不使用 `SandwishUserTable`、`SandwishDictionaryEditor` 这类混合命名；如果组件表达用户、字典、部门等业务语义，优先使用对应业务前缀。
- 页面级 className 必须带页面域前缀，例如 `dictionary-page`、`dictionary-list-panel`。
- 共享组件样式必须使用组件域前缀，避免污染页面样式。

### Placement

- 页面目录只承载该页面域直接拥有的文件，不作为跨域共享目录。
- 页面专属 service 不被其他页面域直接导入；如果出现跨页面复用，应先提升到 `src/service/`。
- `src/service/` 中的共享 service 不依赖页面组件、页面状态或页面目录中的类型。
- 跨页面、跨布局或跨路由共享的 service 放在 `src/service/`。
- 通用请求能力、API 协议类型、响应包装解析、token header、base URL 和 API error 放在 `src/api/`。
- 只服务单个页面域的组件放在页面目录下的 `components/`。
- 多个页面域复用的组件放在 `src/components/`。
- 项目自有通用 UI 技术组件放在 `src/components/<component-name>/index.ts` 目录入口下，目录名使用 `sandwish-*` 前缀，组件名和样式名使用 `Sandwish` / `sandwish` 前缀。
- `index.ts` 只作为组件目录的 public API，负责导出允许外部使用的组件、类型和常量；包含 JSX 的实现放在同目录的 kebab-case `.tsx` 文件中。
- 通用 UI 技术组件的样式与组件同目录放置，例如 `sandwish-table/sandwish-table.css`；组件样式不放入 `src/assets/main.css`。
- 通用 UI 技术组件的内部子组件、私有 helper 和私有类型留在该组件目录下；只有跨组件复用时才提升到更高层级。
- 页面专属组件不得从其他页面域目录直接导入。
- 请求 / 响应类型少且只被 service 与同页面 page 使用时，不单独拆文件。
- 类型被同页面多个组件复用，或 service 文件过长时，拆到 `<domain>-types.ts`。
- 类型被多个页面域复用时，提升到 `src/service/` 对应共享 service 或新增明确边界的共享 types 文件。
- API 响应包装、分页响应等后端 API 协议类型放在 `src/api/`，例如 `PageResponse<T>` 放在 `src/api/page-response.ts`。
- 第三方库缺失类型声明、Vite 环境声明和全局前端扩展类型放在 `src/types/`。
- `src/types/` 不承载页面专属 request / response / form values / table record 类型。
- 页面专属样式固定与页面同目录放置，形成 `pages/<module>/<domain>/<domain>-page.tsx` + `<domain>-page.css` 组合；页面组件由 `*-page.tsx` 显式 import 同目录 CSS。
- 页面当前没有专属样式时，允许同目录 `*-page.css` 为空文件，用于保留稳定页面样式槽位。
- `src/assets/main.css` 只承载全局 token、布局基线和真正跨页面共享的样式，不承载具体业务页面样式。
- 路由、登录态、权限、请求 hook、布局行为和关键页面加载行为优先覆盖在 `src/app.test.tsx`。
- 页面交互复杂度明显上升时，可以新增同目录或测试目录下的聚焦测试。

### UI

#### Page Layout

后台业务页面默认遵循以下信息顺序：

```text
Page
  PageHeader
  FilterBar
  Toolbar
  Content
  Pagination
```

禁止：

- 分页放在页面顶部。
- 筛选控件放在表格主体中。
- 页面区块顺序随机。
- 同一页面出现多个主操作。

#### PageHeader

`PageHeader` 默认结构：

```text
Left:
  Title
  Description

Right:
  SecondaryActions
  PrimaryAction
```

规则：

- 最多 1 个主按钮。
- 最多 2 个次级按钮。
- 更多操作收敛到下拉菜单。
- 操作顺序固定为次级操作在前，主操作在后。

示例：

```text
导入 | 导出 | 新建
```

#### FilterBar

`FilterBar` 必须位于 `DataTable` 上方。

筛选项较少时直接展示；筛选项较多时应通过高级筛选或筛选面板收敛。

搜索框 placeholder 必须表达可搜索对象。

禁止：

```text
搜索
请输入
```

推荐：

```text
搜索用户名 / 手机号
搜索订单号 / 客户名称
```

行为规则：

- 回车触发搜索。
- 重置恢复默认筛选状态。
- 输入即搜索时必须 debounce `300-500ms`。
- 搜索按钮在前，重置按钮在后。

#### Toolbar

`Toolbar` 默认结构：

```text
Left:
  SelectedState
  BatchActions

Right:
  PageActions
```

批量操作规则：

- 未选中数据时禁用。
- 危险操作必须二次确认。
- 批量删除必须放在最后。

#### DataTable

列表页默认结构：

```text
FilterBar
Toolbar
DataTable
Pagination
```

表格列默认顺序：

```text
Identifier
BusinessFields
Status
Time
Actions
```

示例：

```text
名称 | 类型 | 状态 | 创建人 | 更新时间 | 操作
```

#### Action Column

表格存在横向滚动或操作列可能被遮挡时，操作列应固定在右侧。

```tsx
fixed: "right"
```

操作默认顺序：

```text
查看
编辑
复制
启用 / 禁用
删除
```

删除操作必须：

- 放在最后。
- 使用危险样式。
- 要求二次确认。

操作数量过多时应收敛到下拉菜单。下拉菜单中的危险操作必须放在最后。

```tsx
[
    { key: "copy", label: "复制" },
    { key: "disable", label: "禁用" },
    { type: "divider" },
    { key: "delete", label: "删除", danger: true },
]
```

#### Pagination

分页默认位于页面或表格区域右下角。

以下场景页码必须重置为 `1`：

- 筛选条件变化。
- 每页条数变化。

#### Form

短表单优先使用 Modal；存在复杂关系、权限、树选择或多区块内容时，优先使用 Drawer 或 Page。

表单按钮默认位于右下角，顺序固定为：

```text
取消 | 保存
```

删除按钮必须：

- 与保存按钮分离。
- 使用危险样式。
- 要求二次确认。

禁止：

```text
删除 | 保存
```

表单 label 必须使用用户可读业务文案。

禁止：

```text
userName
phone
effectiveAt
```

推荐：

```text
用户名称
手机号
生效时间
```

校验消息必须说明具体字段或失败原因。

禁止：

```text
参数错误
必填
```

推荐：

```text
请输入用户名称
请选择角色
结束时间不能早于开始时间
```

校验行为：

- 失焦或提交时校验。
- 提交失败时滚动到第一个错误字段。
- 异步校验必须 debounce `500ms`。

#### Modal And Drawer

默认用途：

| Type | Usage |
| --- | --- |
| Modal | 确认、短表单 |
| Drawer | 复杂表单、详情 |

底部按钮顺序：

```text
取消 | 确认
取消 | 保存
取消 | 删除
```

#### Status

状态展示必须使用项目稳定的状态组件或状态样式，不直接裸露原始状态值。

禁止：

```tsx
<span>{status}</span>
```

#### Empty State

空状态必须包含：

- 空状态说明。
- 推荐下一步操作。

示例：

```text
暂无文章
新建第一篇文章
```

#### Error State

页面错误状态必须包含：

- 错误说明。
- 恢复操作。

示例：

```text
页面加载失败
重试
```

禁止：

- 白屏。
- 静默失败。

#### Loading

表格必须使用 loading 状态：

```tsx
<Table loading={loading} />
```

提交按钮必须使用 loading 状态：

```tsx
<Button loading={submitting}>保存</Button>
```

禁止：

- 重复提交。
- 无加载反馈。

#### Upload

上传入口必须展示：

- 支持格式。
- 文件大小限制。
- 文件数量限制。

示例：

```text
JPG / PNG
最大 5MB
最多 9 个文件
```

上传过程必须表达以下状态：

```text
上传中
上传成功
上传失败
重试
删除
```

#### Toast And Message

成功提示格式：

```text
动作 + 成功
```

示例：

```text
保存成功
删除成功
```

失败提示格式：

```text
动作 + 失败 + 原因
```

示例：

```text
删除失败，存在关联订单
```

禁止：

```text
操作失败
系统错误
```

### Service

### State

### Permission

- 无权限的操作默认隐藏。
- 因当前业务状态不可执行的操作默认禁用，并说明禁用原因。
- 权限字符串优先集中在页面或专门 helper 中，不在多个无关组件中重复散落。

### Testing

- Playwright locator 优先级固定为：

```text
getByRole
getByLabel
getByText
getByTestId
CSS Selector
```

- Playwright 测试禁止使用 `waitForTimeout`。
- Playwright 测试禁止使用复杂 CSS selector。
- E2E 测试之间不得依赖共享状态。
- 避免过长 E2E 流程。
- 测试应验证用户可见结果和关键请求契约，不验证 Ant Design 内部 DOM 细节。

默认覆盖重点：

- 登录。
- 权限。
- CRUD。
- 搜索。
- 分页。

示例：

```ts
test("delete requires confirmation", async ({ page }) => {
    await page.goto("/users");

    await page.getByRole("button", { name: "删除" }).click();

    await expect(page.getByText("确认删除")).toBeVisible();
});
```

## Code Review Checklist

### Table

- [ ] 操作列在需要时固定右侧。
- [ ] 删除操作放在最后。
- [ ] 操作数量符合收敛规则。
- [ ] 存在 loading 状态。
- [ ] 存在空状态。
- [ ] 存在错误状态。

### Form

- [ ] 校验消息明确。
- [ ] 提交失败后滚动到错误字段。
- [ ] 防止重复提交。
- [ ] 按钮顺序正确。

### General

- [ ] 使用已有共享组件。
- [ ] 无 `any`。
- [ ] 无 `console.log`。
- [ ] 无复杂 JSX。
- [ ] 目录结构正确。

## Forbidden Defaults

- 不新增无边界的 `common`、`base`、`shared` 子目录作为默认归属。
- 不新增与当前技术栈无关的状态管理层，例如全局 store，除非有明确跨页面状态需求。
- 不新增第二套路由、请求、权限或样式体系。
- 不把后端 Java 分层术语机械套进前端目录，例如 `controller`、`dao`、`mapper`、`repository`。
- 不新增 CSS module、styled-components、Tailwind 或其他样式体系，除非先形成明确前端治理决策。
- 不为了“目录统一”进行大规模机械迁移；优先在新增页面或正在修改的页面执行本规则。
- 不为了提前复用抽象出空 `hooks/`、`utils/`、`models/`、`stores/` 等目录。

## Open Items

- 是否把部分 UI 规则沉淀为共享组件默认行为或测试门禁。
- 是否将筛选项数量固定为 `<= 4` 直接展示、`5-8` 收起高级筛选、`> 8` 使用独立筛选面板。
- 是否为列表工具栏固定右侧能力集合：刷新、密度、列设置、导出。
- 是否将表格操作数量固定为 `<= 3` 直接展示、`4-5` 部分收敛、`> 5` 必须使用下拉菜单。
- 是否要求表格操作下拉菜单中的危险操作前必须使用分隔线。
- 是否统一分页默认页大小和可选页大小；当前多数页面默认 `10`，基础稿建议默认 `20`、选项 `10 / 20 / 50 / 100`。
- 是否将表单承载形式固定为 `<= 6` 使用 Modal、`7-20` 使用 Drawer、`> 20` 使用 Page。
- 是否继续使用 Modal 具体宽度规则：确认 `400px`、小表单 `520px`、中表单 `640px`、超过 `640px` 改用 Drawer。
- Modal 和 Drawer 宽度规则是否应改为使用 `SandwishDrawer` 的 `small`、`middle`、`large`、`full` 尺寸档位。
- 是否固定详情页结构为 `PageHeader`、`Summary`、`BasicInfo`、`BusinessInfo`、`Timeline`、`Logs`。
- 是否固定详情页操作顺序为编辑、复制、导出、删除。
- 是否新增统一 `StatusTag` 组件，替代当前页面内分散的 Ant Design `Tag` 状态样式。
- 是否把导入流程纳入 admin-web 通用规则：下载模板、上传文件、校验数据、确认导入、导入结果。
- 是否把导入结果固定为展示成功数量、失败数量、失败原因和错误报告下载入口。
- 是否把导出默认范围固定为当前筛选结果。
- 是否为大数据量导出引入任务中心或等价异步任务反馈机制。
- Playwright 默认覆盖重点是否包含上传、导入 / 导出和核心业务流程。
- 是否为复杂页面固定引入 `<domain>-hooks.ts`，待出现重复且稳定的页面逻辑后再决策。
