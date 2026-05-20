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

同一条规则只归入一个层级。已由 `Hard Rules` 稳定门禁的内容不得在 `Review Rules` 中重复表述；当 `Review Rules` 被沉淀为门禁后，必须从 `Review Rules` 删除或改写为未被门禁覆盖的语义审阅点。

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

- `ADMIN_WEB_LAYER_FETCH_ONLY_HTTP`：只有 `src/api/http.ts` 可直接调用 `fetch`。
- `ADMIN_WEB_LAYER_POST_HELPER_SERVICE_ONLY`：`postJson` / `postFormData` 只在 `*-service.ts` 使用。
- `ADMIN_WEB_LAYER_QUERY_FN_FROM_SERVICE`：`queryFn` / `mutationFn` 只调用 service 方法。
- `ADMIN_WEB_LAYER_SHARED_COMPONENT_NO_PAGE`：`src/components/` 不导入 `src/pages/`。
- `ADMIN_WEB_LAYER_SHARED_SERVICE_TYPES_ONLY`：`src/service/*-service.ts` 不导入页面目录；共享业务类型只从 `*-types.ts` 引用。
- `ADMIN_WEB_LAYER_COMPONENT_INDEX_EXPORT_ONLY`：`src/components/**/index.ts` 只包含带 `from` 的 re-export 声明。
- `ADMIN_WEB_LAYER_API_NO_PAGE`：`src/api/` 不导入页面、布局或组件。
- `ADMIN_WEB_LAYER_AUTH_NO_PAGE`：`src/auth/` 不导入页面、布局或页面 service。
- `ADMIN_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT`：`src` 下禁止 `../../` 及更深相对 import。
- `ADMIN_WEB_LAYER_PAGE_NO_PARENT_RELATIVE_IMPORT`：页面文件禁止 `../` import。
- `ADMIN_WEB_LAYER_PAGE_COMPONENT_NO_EXTERNAL_PAGE`：页面私有组件不引用其他页面域。
- `ADMIN_WEB_LAYER_PAGE_NO_EXTERNAL_SERVICE`：页面域代码不导入其他页面域的 service。
- `ADMIN_WEB_LAYER_SHARED_COMPONENT_CSS_LOCAL`：共享组件禁止 `../*.css` import。

### Naming

- `ADMIN_WEB_NAME_FILE_KEBAB_CASE`：`src` 文件名使用 kebab-case。
- `ADMIN_WEB_NAME_PAGE_FILE`：页面文件命名为 `<domain>-page.tsx`。
- `ADMIN_WEB_NAME_PAGE_STYLE_FILE`：页面样式命名为 `<domain>-page.css`。
- `ADMIN_WEB_NAME_PAGE_SERVICE_FILE`：页面 service 命名为 `<domain>-service.ts`。
- `ADMIN_WEB_NAME_PAGE_TYPES_FILE`：页面类型命名为 `<domain>-types.ts`。
- `ADMIN_WEB_NAME_COMPONENT_EXPORT`：React 组件使用 PascalCase named export。
- `ADMIN_WEB_COMPONENT_SINGLE_EXPORT`：页面私有组件文件最多导出一个 PascalCase 组件。
- `ADMIN_WEB_NAME_PAGE_EXPORT`：页面组件使用 `export const XxxPage = () => {}`。
- `ADMIN_WEB_NAME_FUNCTION_ARROW`：前端方法默认使用箭头函数。
- `ADMIN_WEB_NAME_NO_NESTED_TERNARY`：禁止嵌套三元表达式。
- `ADMIN_WEB_NAME_CAMEL_CASE`：变量和方法使用 camelCase。
- `ADMIN_WEB_NAME_SERVICE_METHOD`：service 方法必须使用固定动词前缀。
- `ADMIN_WEB_NAME_SERVICE_METHOD_INPUT`：service 方法入参固定为无入参、单个 `XxxQuery`、单个 `XxxCommand` 或最多 3 个 plain parameters。
- `ADMIN_WEB_NAME_SERVICE_HELPER_TYPE`：service helper 泛型固定为 `XxxQuery`、`XxxCommand`、inline payload、plain value、`XxxRecord`、`XxxNode`、`Page<XxxRecord/XxxNode>` 或数组。
- `ADMIN_WEB_NAME_API_CONTRACT_TYPE_LOCATION`：`XxxRequest` / `XxxResponse` 只定义在 `*-service.ts` 或 `src/api/`。
- `ADMIN_WEB_NAME_API_CONTRACT_TYPE_EXPOSURE`：`XxxRequest` / `XxxResponse` 是 service 内部 API 契约，不从 service 导出。
- `ADMIN_WEB_NAME_SERVICE_TYPE_EXPOSURE`：页面和组件只从 service 引用 `XxxQuery` / `XxxCommand`；`XxxRecord` / `XxxNode` 从 `*-types.ts` 引用。
- `ADMIN_WEB_NAME_SERVICE_INPUT_TYPE_LOCATION`：`XxxQuery` / `XxxCommand` 只定义在 `*-service.ts`；`PageQuery<T>` 只定义在 `src/types/page.ts`。
- `ADMIN_WEB_NAME_BUSINESS_DATA_TYPE_LOCATION`：`XxxRecord` / `XxxNode` 只定义在明确边界的 `*-types.ts`。
- `ADMIN_WEB_NAME_NO_DTO`：admin-web 不使用 `DTO` 类型名；service 输出对象使用 `XxxRecord` / `XxxNode`。
- `ADMIN_WEB_NAME_BOOLEAN`：布尔变量使用 `is`、`has`、`can` 前缀。
- `ADMIN_WEB_NAME_CONSTANT`：常量使用 `UPPER_SNAKE_CASE`。
- `ADMIN_WEB_NAME_SANDWISH_COMPONENT`：`Sandwish*` 只在 `src/components/` 定义。
- `ADMIN_WEB_NAME_PAGE_CLASS_PREFIX`：页面 `className` 使用页面域前缀；共享组件 class 使用 `sandwish-`。
- `ADMIN_WEB_STYLE_COMPONENT_CLASS_LOCATION`：组件域 CSS class 只定义在对应组件 CSS 文件中。

### Placement

- `ADMIN_WEB_PATH_PAGE_SHAPE`：页面放在 `src/pages/<module>/<domain>/<domain>-page.tsx`。
- `ADMIN_WEB_PATH_PAGE_COMPONENTS`：页面私有组件放在同页面域 `components/`。
- `ADMIN_WEB_PATH_PAGE_SERVICE`：页面 service 放在同页面域 `<domain>-service.ts`。
- `ADMIN_WEB_PATH_AUTH`：认证、token、权限持久化放在 `src/auth/`。
- `ADMIN_WEB_PATH_ROUTER`：路由放在 `src/router/`。
- `ADMIN_WEB_PATH_QUERY`：TanStack Query 基线放在 `src/query/`。
- `ADMIN_WEB_PATH_HOOK_FILE`：hook 文件放在 `*/hooks/use-<name>.ts`。
- `ADMIN_WEB_PATH_GLOBAL_TYPES`：跨页面通用类型放在 `src/types/`。
- `ADMIN_WEB_PATH_TEST_SUPPORT`：测试支撑放在 `src/test/`。
- `ADMIN_WEB_PATH_E2E_PAGE_SPEC`：页面 E2E 放在 `e2e/<module>/<domain>/<domain>.spec.ts`。
- `ADMIN_WEB_PATH_E2E_LAYOUT_SPEC`：布局 E2E 放在 `e2e/layout/*.spec.ts`。

## Review Rules

### Architecture

- 复杂业务逻辑不得直接写在 JSX 中。
- 页面应优先复用项目已有共享组件和页面骨架。

### Naming

- 前端自有按钮、菜单项和确认弹窗文案应表达具体动作，例如 `重置密码`、`移除头像`、`刷新密钥`；避免只写 `操作`、`变更状态`、`处理`。
- 页面状态变量命名贴近 UI 含义，例如 `query`、`selectedRowKeys`、`editingDictionary`。

### Placement

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
- 不为了提前复用抽象出空 `utils/`、`models/`、`stores/` 等目录。

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
