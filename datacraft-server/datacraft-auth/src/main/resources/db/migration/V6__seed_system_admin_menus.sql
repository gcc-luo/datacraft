INSERT INTO dc_menu(code, title, path, icon, parent_id, sort_order)
SELECT seed.code, seed.title, seed.path, seed.icon, parent.id, seed.sort_order
FROM (VALUES
    ('system-users', '用户管理', '/system/users', 'users', 10),
    ('system-roles', '角色管理', '/system/roles', 'shield', 20),
    ('system-menus', '菜单管理', '/system/menus', 'menu', 30)
) AS seed(code, title, path, icon, sort_order)
JOIN dc_menu parent ON parent.code = 'system'
ON CONFLICT (code) DO NOTHING;

INSERT INTO dc_role_menu(role_id, menu_id)
SELECT role.id, menu.id
FROM dc_role role
JOIN dc_menu menu ON menu.code IN ('system-users', 'system-roles', 'system-menus')
WHERE role.code = 'ADMIN'
ON CONFLICT DO NOTHING;
