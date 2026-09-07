# 系统管理模块设计

## 背景

DataCraft 已有用户、角色、菜单及用户/角色、角色/菜单关联表，也已经在登录和菜单查询链路中使用这些数据。但当前只有登录态和按角色返回菜单的后端能力，没有管理员配置界面和管理 API，导致角色、菜单和用户只能依赖数据库初始化数据维护。

## 目标

补齐一个受管理员角色保护的系统管理模块，采用已确认的“授权工作台”布局：左侧固定显示“用户管理、角色管理、菜单管理”三个资源入口，右侧显示对应列表和编辑区域。

本次完成后，管理员可以：

- 查看、新增、编辑、启用/停用用户，并为用户分配角色。
- 查看、新增、编辑、启用/停用角色，并为角色配置菜单权限。
- 查看、新增、编辑、启用/停用菜单，配置父级菜单、路由、图标和排序。
- 从现有动态导航进入 `/system` 系统管理页面。

## 非目标

- 不新增权限点、按钮级权限或数据范围权限模型。
- 不新增数据库表；沿用现有认证迁移中的五张表。
- 不实现审计日志、批量导入、密码找回和组织架构。
- 不改变现有登录、JWT 签发和普通业务模块鉴权行为。

## 后端设计

### 权限边界

新增系统管理 API 全部位于 `/api/v1/system/admin`，通过 Spring Security 的 `ROLE_ADMIN` 保护。非认证用户返回 401，已认证但非管理员用户返回 403。现有 `/api/v1/system/menus` 继续面向所有已认证用户，仅返回其角色已授权且启用的菜单。

### 管理 API

用户：

```text
GET    /api/v1/system/admin/users
POST   /api/v1/system/admin/users
PUT    /api/v1/system/admin/users/{id}
```

用户创建要求用户名、显示名、初始密码和角色；用户编辑允许修改显示名、密码、启用状态和角色，密码为空表示保留原密码。用户响应只返回账号资料、启用状态、角色编码和时间，不返回密码哈希。

角色：

```text
GET    /api/v1/system/admin/roles
POST   /api/v1/system/admin/roles
PUT    /api/v1/system/admin/roles/{id}
```

角色创建/编辑维护编码、名称、启用状态和菜单 ID 集合。角色编码保持唯一且规范化为大写；管理员角色不能被停用，避免系统失去管理入口。

菜单：

```text
GET    /api/v1/system/admin/menus
POST   /api/v1/system/admin/menus
PUT    /api/v1/system/admin/menus/{id}
```

菜单创建/编辑维护编码、标题、路径、图标、父级菜单、排序和启用状态。菜单响应同时返回 `parentTitle`，方便平面表格展示。暂不提供物理删除，停用菜单作为可恢复的下线操作；根菜单不能把自己设置为父级，父级必须存在且不能形成循环。

### 应用服务与持久化

新增 `SystemAdminApplicationService`，负责参数校验、密码 BCrypt 哈希、角色/菜单关联替换、管理员保护和 DTO 映射。Controller 只处理 HTTP 参数和响应，不直接调用 Mapper。

沿用 MyBatis-Plus 实体与 Mapper，在 `UserMapper`、`RoleMapper`、`MenuMapper` 增加列表、详情、关联查询和关联替换所需 SQL。关联替换在事务内先删除旧关联，再批量插入新关联，空角色或空菜单集合表示清空授权。

### 错误处理

新增稳定错误码：

- `SYSTEM_ADMIN_FORBIDDEN`
- `SYSTEM_USER_NOT_FOUND`
- `SYSTEM_ROLE_NOT_FOUND`
- `SYSTEM_MENU_NOT_FOUND`
- `SYSTEM_DUPLICATE_USERNAME`
- `SYSTEM_DUPLICATE_ROLE_CODE`
- `SYSTEM_DUPLICATE_MENU_CODE`
- `SYSTEM_INVALID_MENU_PARENT`
- `SYSTEM_ADMIN_PROTECTED`

不返回数据库异常、密码哈希或内部堆栈信息。

## 前端设计

新增 `SystemManagementView.vue` 和 `/system` 路由。页面使用与现有控制台一致的深蓝侧栏、白色内容卡片和蓝色主操作色，但把系统管理内部导航固定在内容区域左侧：

- 用户管理：用户列表、创建/编辑抽屉、角色多选、启用状态。
- 角色管理：角色列表、创建/编辑抽屉、菜单树勾选授权、启用状态。
- 菜单管理：菜单列表、创建/编辑抽屉、父级菜单选择、路由与排序编辑。

系统管理页面只在后端菜单包含 `/system` 时从动态导航进入。接口返回 403 时显示权限不足提示，不回退到空白页面。创建/编辑成功后刷新当前列表，并显示成功提示；加载和提交过程显示禁用状态；表单错误显示后端返回消息。

新增：

```text
datacraft-web/src/api/system.ts
datacraft-web/src/types/system.ts
datacraft-web/src/views/SystemManagementView.vue
datacraft-web/src/views/SystemManagementView.test.ts
datacraft-web/src/styles/system.scss
```

## 测试设计

后端：

- 应用服务测试覆盖用户/角色/菜单新增编辑、关联替换、密码不回显、管理员不可停用、菜单父级循环校验。
- Controller 测试覆盖管理员访问、非管理员 403、列表和校验错误响应。
- Mapper 迁移测试继续确认现有五张认证表和约束可用。

前端：

- API 类型和请求路径测试通过 Mock API 验证。
- 页面测试覆盖三类资源切换、用户新增、角色菜单授权提交、菜单编辑和 403 提示。
- `npm run build` 验证 TypeScript、Vue 模板和生产构建。

## 验收标准

1. 管理员登录后能从侧栏进入“系统管理”。
2. 管理员能在同一页面完成用户、角色、菜单的查看和编辑。
3. 角色授权变更后，重新登录或刷新菜单时导航按授权结果变化。
4. 非管理员无法调用系统管理 API。
5. 任意用户 API 响应都不包含密码或密码哈希。
6. 现有登录、数据源、资产、Pipeline、质量页面测试和构建不受影响。
