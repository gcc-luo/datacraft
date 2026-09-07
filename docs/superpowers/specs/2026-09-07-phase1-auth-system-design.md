# Phase 1 Auth & System Design

## Scope

Implement the first product phase defined by `DESIGN.md`: User, Role, JWT login, application layout, and role-driven menus. PostgreSQL remains the DataCraft platform database; MySQL and PostgreSQL are data sources for later phases. This phase does not implement datasource management, metadata collection, pipelines, engines, scheduling, or quality workflows.

## Product direction

Use the approved “console-first” direction (option A): a concise login page followed by a dark navigation sidebar, a light workspace, and a compact operator dashboard. Unimplemented product areas remain placeholder pages so the shell can be exercised without prematurely implementing later phases.

## Backend architecture

Add a `datacraft-auth` module to the modular monolith. The bootstrap module assembles the application; auth owns its domain, application services, persistence adapters, security configuration, and web controllers. Controllers call application services and never call mappers directly. API request/response records remain in the API boundary module; entities are not returned directly.

The public endpoints are:

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `GET /api/v1/system/menus`

The login service loads an enabled user, verifies the BCrypt password, and signs a short-lived JWT access token. Spring Security runs statelessly with a Bearer token filter. There is no refresh token in Phase 1. JWT signing configuration is read from environment variables; raw tokens and passwords are never logged or persisted.

Authentication failures deliberately do not disclose whether the username or password was incorrect. Missing/invalid tokens return 401, insufficient authorization returns 403, validation failures return 400, and unexpected failures return a request id without stack traces or secrets.

## Persistence model

Flyway migrations create tables with the `dc_` prefix:

- `dc_user`: username, display name, BCrypt password hash, enabled state, audit timestamps
- `dc_role`: role code, name, enabled state, audit timestamps
- `dc_user_role`: user/role relationship with uniqueness constraints
- `dc_menu`: menu code, title, route path, icon, parent id, sort order, enabled state
- `dc_role_menu`: role/menu relationship with uniqueness constraints

Migration seed data creates the `ADMIN` role and Phase 1 menu entries, then grants those entries to `ADMIN`. An idempotent post-migration administrator initializer reads `DATACRAFT_ADMIN_USERNAME` and `DATACRAFT_ADMIN_PASSWORD`, hashes the password immediately with BCrypt, and creates the account only when it does not already exist. It never overwrites an existing password. Missing administrator configuration fails startup with an actionable message rather than creating a default credential.

## Frontend architecture

Add an auth store for token, user summary, login, logout, and session restoration. Axios injects the Bearer token, and a 401 response clears the session and routes to `/login`. The router protects the application shell and keeps the login route public.

Add `LoginView`, `AppLayout`, `HomeView`, and `PlaceholderView`. `AppLayout` renders the backend-provided menu tree in a dark sidebar, a top user area, and a light content region. Phase 1 menu labels are 工作台, 数据源, 数据资产, 数据处理, 数据质量, 任务中心, and 系统管理. Future areas route to the placeholder view. The layout collapses on small screens without changing authorization behavior.

## API and response conventions

Use the shared response envelope:

```json
{"code":"0","message":"success","data":{}}
```

Login data contains the access token, token type, expiry seconds, and a user summary. `/me` returns the user summary and roles; `/menus` returns a tree-shaped menu DTO. Field validation errors use HTTP 400; authentication uses 401; authorization uses 403; server errors include a request id only.

## Testing and acceptance

Backend unit and web tests cover BCrypt behavior, JWT signing/parsing/expiry/signature failures, successful and rejected login, disabled users, idempotent administrator initialization, role-filtered menu trees, security endpoint rules, response envelopes, and validation. Frontend tests cover login submission, route guards, session restoration, 401 cleanup, and menu rendering where the current toolchain supports them.

Phase 1 is accepted when PostgreSQL/Redis start from the existing compose file, Flyway creates the schema and administrator from environment variables, admin login returns a JWT, protected routes reject anonymous requests, the A layout renders after login, refresh restores the session, logout returns to login, and `mvn test`, `mvn package`, and `npm run build` complete successfully.
