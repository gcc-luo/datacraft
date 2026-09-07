CREATE TABLE IF NOT EXISTS dc_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dc_role (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dc_user_role (
    user_id BIGINT NOT NULL REFERENCES dc_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES dc_role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS dc_menu (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(128) NOT NULL,
    path VARCHAR(255) NOT NULL,
    icon VARCHAR(64),
    parent_id BIGINT REFERENCES dc_menu(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dc_role_menu (
    role_id BIGINT NOT NULL REFERENCES dc_role(id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES dc_menu(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, menu_id)
);

CREATE INDEX IF NOT EXISTS idx_dc_user_role_user ON dc_user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_dc_role_menu_role ON dc_role_menu(role_id);
CREATE INDEX IF NOT EXISTS idx_dc_menu_parent_sort ON dc_menu(parent_id, sort_order);

INSERT INTO dc_role(code, name) VALUES ('ADMIN', '管理员') ON CONFLICT (code) DO NOTHING;

INSERT INTO dc_menu(code, title, path, icon, parent_id, sort_order) VALUES
    ('home', '工作台', '/', 'home', NULL, 10),
    ('datasource', '数据源', '/datasources', 'database', NULL, 20),
    ('asset', '数据资产', '/assets', 'grid', NULL, 30),
    ('pipeline', '数据处理', '/pipelines', 'flow', NULL, 40),
    ('quality', '数据质量', '/quality', 'check', NULL, 50),
    ('execution', '任务中心', '/executions', 'clock', NULL, 60),
    ('system', '系统管理', '/system', 'settings', NULL, 70)
ON CONFLICT (code) DO NOTHING;

INSERT INTO dc_role_menu(role_id, menu_id)
SELECT role.id, menu.id
FROM dc_role role CROSS JOIN dc_menu menu
WHERE role.code = 'ADMIN'
ON CONFLICT DO NOTHING;
