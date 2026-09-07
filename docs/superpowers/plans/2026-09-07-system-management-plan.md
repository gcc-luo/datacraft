# 系统管理模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有认证模型上补齐受 ADMIN 保护的用户、角色、菜单管理 API，以及方案 B 的授权工作台前端。

**Architecture:** 复用 `dc_user`、`dc_role`、`dc_menu`、`dc_user_role`、`dc_role_menu` 五张现有表，不新增数据库迁移。后端由 `SystemAdminApplicationService` 负责校验、密码哈希、关联替换和 DTO 映射，Controller 只处理 HTTP；前端新增 `/system` 单页工作台，内部用左侧资源导航切换用户、角色、菜单。

**Tech Stack:** Java 21, Spring Boot 3, Spring Security, MyBatis-Plus, JUnit 5, Vue 3, TypeScript, Pinia, Vue Router, Vitest.

---

### Task 1: 扩展系统管理 API 契约和持久化查询

**Files:**
- Create: `datacraft-server/datacraft-api/src/main/java/com/datacraft/api/system/AdminUserResponse.java`
- Create: `datacraft-server/datacraft-api/src/main/java/com/datacraft/api/system/AdminRoleResponse.java`
- Create: `datacraft-server/datacraft-api/src/main/java/com/datacraft/api/system/AdminMenuResponse.java`
- Create: `datacraft-server/datacraft-api/src/main/java/com/datacraft/api/system/AdminUserRequest.java`
- Create: `datacraft-server/datacraft-api/src/main/java/com/datacraft/api/system/AdminRoleRequest.java`
- Create: `datacraft-server/datacraft-api/src/main/java/com/datacraft/api/system/AdminMenuRequest.java`
- Modify: `datacraft-server/datacraft-auth/src/main/java/com/datacraft/auth/infrastructure/persistence/mapper/UserMapper.java`
- Modify: `datacraft-server/datacraft-auth/src/main/java/com/datacraft/auth/infrastructure/persistence/mapper/RoleMapper.java`
- Modify: `datacraft-server/datacraft-auth/src/main/java/com/datacraft/auth/infrastructure/persistence/mapper/MenuMapper.java`
- Test: `datacraft-server/datacraft-auth/src/test/java/com/datacraft/auth/infrastructure/RepositoryMappingTest.java`

- [x] **Step 1: Write failing contract tests** for request validation and mapper method signatures needed to list users with roles, list roles with menu IDs, and list menus with parent titles.
- [x] **Step 2: Run the focused auth tests** with `mvn -q -pl datacraft-server/datacraft-auth -am -Dsurefire.failIfNoSpecifiedTests=false -Dtest=RepositoryMappingTest test`; confirm the new contract test fails because the admin API types and mapper methods do not exist.
- [x] **Step 3: Add immutable API records** with Jakarta validation: user username/display name/password/role codes/enabled, role code/name/menu IDs/enabled, menu code/title/path/icon/parent ID/sort order/enabled. Response records must omit password and password hash.
- [x] **Step 4: Add mapper queries and update SQL** for list/detail data, role codes by user, menu IDs by role, parent title, duplicate checks, role assignment replacement, and menu assignment replacement. Use explicit column lists and `ON CONFLICT DO NOTHING` only for idempotent association inserts.
- [x] **Step 5: Run the focused auth tests** and confirm the contract/mapping tests pass.

### Task 2: Implement admin application service and validation

**Files:**
- Create: `datacraft-server/datacraft-auth/src/main/java/com/datacraft/auth/application/SystemAdminApplicationService.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/com/datacraft/auth/application/SystemAdminException.java`
- Modify: `datacraft-server/datacraft-auth/src/main/java/com/datacraft/auth/web/ApiExceptionHandler.java`
- Test: `datacraft-server/datacraft-auth/src/test/java/com/datacraft/auth/application/SystemAdminApplicationServiceTest.java`

- [x] **Step 1: Write failing service tests** for user creation with BCrypt, blank edit password preserving the existing hash, role menu replacement, duplicate username/role/menu rejection, admin role protection, missing references, and menu parent cycle rejection.
- [x] **Step 2: Run `SystemAdminApplicationServiceTest`** and verify the tests fail for missing service behavior rather than test setup errors.
- [x] **Step 3: Implement the service** with constructor-injected mappers, `PasswordEncoder`, and transactions. Normalize role/menu codes, validate references before writes, replace associations transactionally, and map only safe response fields.
- [x] **Step 4: Implement typed exception subclasses or error codes** for the design document’s stable system-management errors and map them in `ApiExceptionHandler` without exposing SQL or password details.
- [x] **Step 5: Run the service tests** and confirm all service cases pass.

### Task 3: Add secured system-management controllers

**Files:**
- Create: `datacraft-server/datacraft-auth/src/main/java/com/datacraft/auth/web/SystemAdminController.java`
- Modify: `datacraft-server/datacraft-auth/src/main/java/com/datacraft/auth/security/SecurityConfig.java`
- Test: `datacraft-server/datacraft-auth/src/test/java/com/datacraft/auth/web/SystemAdminControllerTest.java`

- [x] **Step 1: Write failing MVC tests** for admin list/create/update endpoints, unauthenticated 401, non-admin 403, safe user responses, and validation errors.
- [x] **Step 2: Run the focused MVC tests** and confirm they fail because the controller and ADMIN method security are not present.
- [x] **Step 3: Enable method security** and annotate the `/api/v1/system/admin` controller with `@PreAuthorize("hasRole('ADMIN')")`; keep `/api/v1/system/menus` available to all authenticated users.
- [x] **Step 4: Implement controller endpoints** for `/users`, `/roles`, and `/menus`, delegating all business work to `SystemAdminApplicationService` and returning `ApiResponse` DTOs.
- [x] **Step 5: Run auth module tests** and confirm the controller/security suite passes without breaking existing login/menu tests.

### Task 4: Add frontend system-management API and view tests

**Files:**
- Create: `datacraft-web/src/api/system.ts`
- Create: `datacraft-web/src/types/system.ts`
- Create: `datacraft-web/src/views/SystemManagementView.test.ts`

- [x] **Step 1: Write failing Vitest cases** for resource switching, user creation payload, role menu authorization payload, menu edit payload, and rendering a 403 permission message.
- [x] **Step 2: Run `npm test -- --run src/views/SystemManagementView.test.ts`** and verify the tests fail because the API module and view do not exist.
- [x] **Step 3: Add typed API functions** for listing/creating/updating users, roles, and menus; reuse the existing HTTP wrapper and response unwrapping convention.
- [x] **Step 4: Add `SystemManagementView.vue`** with the approved workspace layout: fixed resource navigation, list panels, create/edit forms, role multi-select, menu checkbox authorization, and inline loading/error/success states.
- [x] **Step 5: Run the focused Vitest suite** and confirm the page behavior passes.

### Task 5: Wire navigation, styling, and accessibility

**Files:**
- Modify: `datacraft-web/src/router/index.ts`
- Create: `datacraft-web/src/styles/system.scss`
- Modify: `datacraft-web/src/styles/main.scss`
- Modify: `datacraft-web/src/views/SystemManagementView.vue`
- Modify: `datacraft-web/src/router/index.ts`

- [x] **Step 1: Add the `/system` route** before the catch-all route and load `SystemManagementView.vue` lazily.
- [x] **Step 2: Import scoped system styles** and implement responsive layout, visible focus states, semantic labels, disabled submit states, and a compact mobile fallback.
- [x] **Step 3: Add route/view tests** confirming `/system` resolves to the management view and existing routes continue resolving.
- [x] **Step 4: Run frontend tests** with `npm test -- --run` and fix any regressions.
- [x] **Step 5: Run `npm run build`** and confirm the production bundle compiles.

### Task 6: Full verification and project documentation

**Files:**
- Modify: `DEV_PROGRESS.md`
- Modify: `README.md`

- [x] **Step 1: Run backend tests** with `mvn test` using the project’s JDK 21 environment; confirm zero failures and zero errors.
- [x] **Step 2: Run backend package build** with `mvn package` and frontend production build with `npm run build`.
- [x] **Step 3: Run `git diff --check`** and inspect the final diff for secret leakage, password fields in responses, direct Controller-to-Mapper calls, and unrelated changes.
- [x] **Step 4: Update `DEV_PROGRESS.md`** with the completed system-management module and next phase, and update README API/feature boundaries without claiming scheduler or external engines are complete.
- [x] **Step 5: Report the implemented routes, tests, builds, and any remaining product limitations.**
