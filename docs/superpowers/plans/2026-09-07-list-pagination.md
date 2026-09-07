# 全量列表分页展示 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为当前前端所有业务记录列表提供统一的分页展示、页容量切换和筛选后回到第一页的交互。

**Architecture:** 采用前端统一分页，保持现有后端列表接口和 API 类型不变，避免破坏当前调用方。新增 `usePagination` 负责数组分页状态与边界收敛，新增 `PaginationBar` 负责统一视觉和交互；数据源、数据资产及字段、Pipeline、质量结果、系统用户/角色/菜单分别接入自己的分页状态。

**Tech Stack:** Vue 3 Composition API, TypeScript, Element Plus-free custom Vue component, Vitest, Vue Test Utils, Vite.

---

### Task 1: 建立分页公共能力

**Files:**
- Create: `datacraft-web/src/composables/usePagination.ts`
- Create: `datacraft-web/src/components/common/PaginationBar.vue`
- Create: `datacraft-web/src/components/common/PaginationBar.test.ts`

- [x] **Step 1: Write the failing test**

在 `PaginationBar.test.ts` 中验证总数、页码按钮、上一页/下一页禁用状态，以及点击页码后发出 `update:currentPage`。

```ts
it('renders count and emits the selected page', async () => {
  const wrapper = mount(PaginationBar, {
    props: { currentPage: 1, pageSize: 10, total: 25 },
  })

  expect(wrapper.text()).toContain('共 25 条')
  expect(wrapper.get('[data-testid="pagination-prev"]').attributes('disabled')).toBeDefined()

  await wrapper.get('[data-testid="pagination-page-2"]').trigger('click')
  expect(wrapper.emitted('update:currentPage')?.at(-1)).toEqual([2])
})
```

- [x] **Step 2: Run the test to verify it fails**

Run: `npm run test -- --run src/components/common/PaginationBar.test.ts`

Expected: FAIL because the shared component does not exist.

- [x] **Step 3: Write minimal implementation**

`usePagination<T>(source, defaultPageSize = 10)` 暴露 `currentPage`、`pageSize`、`total`、`totalPages`、`paginatedItems`、`resetPage`、`setPage` 和 `setPageSize`；页码变化时限制在有效范围内，数据减少时自动回到最后一页。`PaginationBar.vue` 接收 `currentPage/pageSize/total/pageSizes`，渲染总数、页容量选择、页码和前后页按钮，并通过 `update:currentPage/update:pageSize` 通知父级。

- [x] **Step 4: Run the test to verify it passes**

Run: `npm run test -- --run src/components/common/PaginationBar.test.ts`

Expected: PASS.

### Task 2: 为普通业务列表接入分页

**Files:**
- Modify: `datacraft-web/src/views/DatasourceListView.vue`
- Modify: `datacraft-web/src/views/AssetListView.vue`
- Modify: `datacraft-web/src/views/PipelineListView.vue`
- Modify: `datacraft-web/src/views/QualityView.vue`
- Test: `datacraft-web/src/views/DatasourceListView.test.ts`
- Test: `datacraft-web/src/views/AssetListView.test.ts`
- Test: `datacraft-web/src/views/PipelineListView.test.ts`
- Test: `datacraft-web/src/views/QualityView.test.ts`

- [x] **Step 1: Write the failing tests**

在四个页面测试中分别断言分页控件存在；资产页同时断言数据集导航和字段表各有一个分页控件。测试选择器统一使用 `[data-testid="pagination-bar"]`，以验证每个用户可见记录列表均已接入。

- [x] **Step 2: Run focused tests to verify they fail**

Run: `npm run test -- --run src/views/DatasourceListView.test.ts src/views/AssetListView.test.ts src/views/PipelineListView.test.ts src/views/QualityView.test.ts`

Expected: FAIL because these views do not render `PaginationBar`.

- [x] **Step 3: Write minimal implementation**

各页面将完整数组作为 `usePagination` 的 source，并将模板中的 `v-for` 改为分页数组：

```ts
const pagination = usePagination(computed(() => rows.value))
```

数据资产页对筛选后的数据集和当前数据集字段各建立分页状态；关键词变化、数据源切换和数据集切换调用对应 `resetPage()`。数据集按分页结果再分组展示，字段表使用字段分页结果。数据源、Pipeline、质量结果列表在列表下方插入共享组件；质量结果新增结果后保持在第一页。

- [x] **Step 4: Run focused tests to verify they pass**

Run: `npm run test -- --run src/views/DatasourceListView.test.ts src/views/AssetListView.test.ts src/views/PipelineListView.test.ts src/views/QualityView.test.ts`

Expected: PASS.

### Task 3: 为系统管理下的用户、角色、菜单接入分页

**Files:**
- Modify: `datacraft-web/src/views/SystemManagementView.vue`
- Test: `datacraft-web/src/views/SystemManagementView.test.ts`

- [x] **Step 1: Write the failing test**

补充用户、角色、菜单三个路由页面的分页控件断言，确保嵌套路由切换后对应列表仍有分页，不把三个列表错误地共用页码状态。

- [x] **Step 2: Run the focused test to verify it fails**

Run: `npm run test -- --run src/views/SystemManagementView.test.ts`

Expected: FAIL because the system-management tables do not render pagination controls.

- [x] **Step 3: Write minimal implementation**

分别创建 `userPagination`、`rolePagination`、`menuPagination`，三个表格使用各自的 `paginatedItems`，在对应卡片底部渲染 `PaginationBar`。创建、编辑、删除和授权保存后沿用现有刷新逻辑；刷新后分页状态会根据新总数自动收敛，切换二级菜单不会丢失各列表当前页。

- [x] **Step 4: Run the focused test to verify it passes**

Run: `npm run test -- --run src/views/SystemManagementView.test.ts`

Expected: PASS.

### Task 4: 全量验证并更新进度

**Files:**
- Modify: `DEV_PROGRESS.md`

- [x] **Step 1: Run frontend tests**

Run: `npm run test -- --run`

Expected: all frontend tests pass.

- [x] **Step 2: Run frontend build and repository checks**

Run: `npm run build` and `git diff --check`

Expected: Vite production build succeeds and `git diff --check` has no output.

- [x] **Step 3: Update progress documentation**

在 `DEV_PROGRESS.md` 记录统一分页能力、覆盖的列表范围、前端验证命令和结果；明确本次保持后端数组接口不变，后续数据量显著增长时可在同一分页组件基础上迁移为服务端分页。
