package com.datacraft.auth.application;

import com.datacraft.api.system.AdminMenuRequest;
import com.datacraft.api.system.AdminMenuResponse;
import com.datacraft.api.system.AdminRoleRequest;
import com.datacraft.api.system.AdminRoleResponse;
import com.datacraft.api.system.AdminUserRequest;
import com.datacraft.api.system.AdminUserResponse;
import com.datacraft.auth.infrastructure.persistence.entity.MenuEntity;
import com.datacraft.auth.infrastructure.persistence.entity.RoleEntity;
import com.datacraft.auth.infrastructure.persistence.entity.UserEntity;
import com.datacraft.auth.infrastructure.persistence.mapper.MenuMapper;
import com.datacraft.auth.infrastructure.persistence.mapper.RoleMapper;
import com.datacraft.auth.infrastructure.persistence.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class SystemAdminApplicationService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;

    public SystemAdminApplicationService(UserMapper userMapper, RoleMapper roleMapper, MenuMapper menuMapper,
                                         PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserResponse> listUsers() {
        return userMapper.findForAdmin().stream().map(this::toUserResponse).toList();
    }

    @Transactional
    public AdminUserResponse createUser(AdminUserRequest request) {
        String username = required(request.username(), "用户名不能为空");
        if (userMapper.findByUsername(username) != null) {
            throw error("SYSTEM_DUPLICATE_USERNAME", "用户名已存在");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw error("VALIDATION_ERROR", "新用户密码不能为空");
        }
        List<Long> roleIds = validateRoleCodes(request.roleCodes());
        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setDisplayName(required(request.displayName(), "显示名不能为空"));
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        entity.setEnabled(request.enabled() == null || request.enabled());
        userMapper.insert(entity);
        replaceUserRoles(entity.getId(), roleIds);
        return getUser(entity.getId());
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, AdminUserRequest request) {
        UserEntity existing = userMapper.findForAdminById(id);
        if (existing == null) {
            throw error("SYSTEM_USER_NOT_FOUND", "用户不存在");
        }
        String username = required(request.username(), "用户名不能为空");
        UserEntity duplicate = userMapper.findByUsername(username);
        if (duplicate != null && !id.equals(duplicate.getId())) {
            throw error("SYSTEM_DUPLICATE_USERNAME", "用户名已存在");
        }
        List<Long> roleIds = validateRoleCodes(request.roleCodes());
        String passwordHash = request.password() == null || request.password().isBlank()
                ? existing.getPasswordHash() : passwordEncoder.encode(request.password());
        userMapper.updateAdmin(id, username, required(request.displayName(), "显示名不能为空"), passwordHash,
                request.enabled() == null || request.enabled());
        replaceUserRoles(id, roleIds);
        return getUser(id);
    }

    public List<AdminRoleResponse> listRoles() {
        return roleMapper.findForAdmin().stream().map(this::toRoleResponse).toList();
    }

    @Transactional
    public AdminRoleResponse createRole(AdminRoleRequest request) {
        String code = normalizeCode(request.code(), "角色编码不能为空");
        if (!roleMapper.findIdsByCodes(List.of(code)).isEmpty()) {
            throw error("SYSTEM_DUPLICATE_ROLE_CODE", "角色编码已存在");
        }
        List<Long> menuIds = validateMenuIds(request.menuIds());
        RoleEntity entity = new RoleEntity();
        entity.setCode(code);
        entity.setName(required(request.name(), "角色名称不能为空"));
        entity.setEnabled(request.enabled() == null || request.enabled());
        roleMapper.insert(entity);
        replaceRoleMenus(entity.getId(), menuIds);
        return getRole(entity.getId());
    }

    @Transactional
    public AdminRoleResponse updateRole(Long id, AdminRoleRequest request) {
        RoleEntity existing = roleMapper.findForAdminById(id);
        if (existing == null) {
            throw error("SYSTEM_ROLE_NOT_FOUND", "角色不存在");
        }
        String code = normalizeCode(request.code(), "角色编码不能为空");
        if ("ADMIN".equals(existing.getCode()) && (!"ADMIN".equals(code)
                || Boolean.FALSE.equals(request.enabled()))) {
            throw error("SYSTEM_ADMIN_PROTECTED", "管理员角色不能改名或停用");
        }
        List<Long> sameCode = roleMapper.findIdsByCodes(List.of(code));
        if (sameCode.stream().anyMatch(existingId -> !id.equals(existingId))) {
            throw error("SYSTEM_DUPLICATE_ROLE_CODE", "角色编码已存在");
        }
        List<Long> menuIds = validateMenuIds(request.menuIds());
        roleMapper.updateAdmin(id, code, required(request.name(), "角色名称不能为空"),
                request.enabled() == null || request.enabled());
        replaceRoleMenus(id, menuIds);
        return getRole(id);
    }

    public List<AdminMenuResponse> listMenus() {
        return menuMapper.findForAdmin().stream().map(this::toMenuResponse).toList();
    }

    @Transactional
    public AdminMenuResponse createMenu(AdminMenuRequest request) {
        String code = normalizeCode(request.code(), "菜单编码不能为空");
        if (menuMapper.findForAdmin().stream().anyMatch(menu -> code.equals(menu.getCode()))) {
            throw error("SYSTEM_DUPLICATE_MENU_CODE", "菜单编码已存在");
        }
        Long parentId = validateMenuParent(null, request.parentId());
        MenuEntity entity = newMenu(code, request, parentId);
        menuMapper.insertAdmin(entity);
        return getMenu(entity.getId());
    }

    @Transactional
    public AdminMenuResponse updateMenu(Long id, AdminMenuRequest request) {
        MenuEntity existing = menuMapper.findForAdminById(id);
        if (existing == null) {
            throw error("SYSTEM_MENU_NOT_FOUND", "菜单不存在");
        }
        String code = normalizeCode(request.code(), "菜单编码不能为空");
        if (menuMapper.findForAdmin().stream().anyMatch(menu -> code.equals(menu.getCode()) && !id.equals(menu.getId()))) {
            throw error("SYSTEM_DUPLICATE_MENU_CODE", "菜单编码已存在");
        }
        Long parentId = validateMenuParent(id, request.parentId());
        menuMapper.updateAdmin(id, code, required(request.title(), "菜单标题不能为空"),
                required(request.path(), "菜单路径不能为空"), blankToNull(request.icon()), parentId,
                request.sortOrder() == null ? 0 : request.sortOrder(), request.enabled() == null || request.enabled());
        return getMenu(id);
    }

    private AdminUserResponse getUser(Long id) {
        UserEntity entity = userMapper.findForAdminById(id);
        if (entity == null) throw error("SYSTEM_USER_NOT_FOUND", "用户不存在");
        return toUserResponse(entity);
    }

    private AdminRoleResponse getRole(Long id) {
        RoleEntity entity = roleMapper.findForAdminById(id);
        if (entity == null) throw error("SYSTEM_ROLE_NOT_FOUND", "角色不存在");
        return toRoleResponse(entity);
    }

    private AdminMenuResponse getMenu(Long id) {
        MenuEntity entity = menuMapper.findForAdminById(id);
        if (entity == null) throw error("SYSTEM_MENU_NOT_FOUND", "菜单不存在");
        return toMenuResponse(entity);
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        roleMapper.deleteUserRoles(userId);
        roleIds.forEach(roleId -> roleMapper.insertUserRole(userId, roleId));
    }

    private void replaceRoleMenus(Long roleId, List<Long> menuIds) {
        roleMapper.deleteRoleMenus(roleId);
        menuIds.forEach(menuId -> roleMapper.insertRoleMenu(roleId, menuId));
    }

    private List<Long> validateRoleCodes(List<String> roleCodes) {
        List<String> normalized = distinctUpper(roleCodes);
        if (normalized.isEmpty()) return List.of();
        List<Long> ids = roleMapper.findIdsByCodes(normalized);
        if (ids.size() != normalized.size()) {
            throw error("SYSTEM_ROLE_NOT_FOUND", "包含不存在的角色");
        }
        return ids;
    }

    private List<Long> validateMenuIds(List<Long> menuIds) {
        List<Long> normalized = menuIds == null ? List.of() : menuIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalized.isEmpty()) return List.of();
        Set<Long> existing = new HashSet<>(menuMapper.findForAdmin().stream().map(MenuEntity::getId).toList());
        if (!existing.containsAll(normalized)) {
            throw error("SYSTEM_MENU_NOT_FOUND", "包含不存在的菜单");
        }
        return normalized;
    }

    private Long validateMenuParent(Long menuId, Long parentId) {
        if (parentId == null) return null;
        List<MenuEntity> menus = menuMapper.findForAdmin();
        Map<Long, Long> parents = new HashMap<>();
        menus.forEach(menu -> parents.put(menu.getId(), menu.getParentId()));
        if (!parents.containsKey(parentId)) {
            throw error("SYSTEM_INVALID_MENU_PARENT", "父级菜单不存在");
        }
        if (menuId != null && menuId.equals(parentId)) {
            throw error("SYSTEM_INVALID_MENU_PARENT", "菜单不能将自己设为父级");
        }
        Set<Long> visited = new HashSet<>();
        Long cursor = parentId;
        while (cursor != null) {
            if (!visited.add(cursor) || (menuId != null && menuId.equals(cursor))) {
                throw error("SYSTEM_INVALID_MENU_PARENT", "菜单父级关系不能形成循环");
            }
            cursor = parents.get(cursor);
        }
        return parentId;
    }

    private MenuEntity newMenu(String code, AdminMenuRequest request, Long parentId) {
        MenuEntity entity = new MenuEntity();
        entity.setCode(code);
        entity.setTitle(required(request.title(), "菜单标题不能为空"));
        entity.setPath(required(request.path(), "菜单路径不能为空"));
        entity.setIcon(blankToNull(request.icon()));
        entity.setParentId(parentId);
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setEnabled(request.enabled() == null || request.enabled());
        return entity;
    }

    private AdminUserResponse toUserResponse(UserEntity entity) {
        return new AdminUserResponse(entity.getId(), entity.getUsername(), entity.getDisplayName(),
                Boolean.TRUE.equals(entity.getEnabled()), safeList(roleMapper.findCodesByUserId(entity.getId())),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private AdminRoleResponse toRoleResponse(RoleEntity entity) {
        return new AdminRoleResponse(entity.getId(), entity.getCode(), entity.getName(),
                Boolean.TRUE.equals(entity.getEnabled()), safeList(roleMapper.findMenuIdsByRoleId(entity.getId())),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private AdminMenuResponse toMenuResponse(MenuEntity entity) {
        return new AdminMenuResponse(entity.getId(), entity.getCode(), entity.getTitle(), entity.getPath(),
                entity.getIcon(), entity.getParentId(), entity.getParentTitle(), entity.getSortOrder() == null ? 0 : entity.getSortOrder(),
                Boolean.TRUE.equals(entity.getEnabled()), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private String normalizeCode(String value, String message) {
        return required(value, message).toUpperCase(Locale.ROOT);
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw error("VALIDATION_ERROR", message);
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<String> distinctUpper(List<String> values) {
        if (values == null) return List.of();
        return values.stream().filter(Objects::nonNull).map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isBlank()).distinct().toList();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : List.copyOf(values);
    }

    private SystemAdminException error(String code, String message) {
        return new SystemAdminException(code, message);
    }
}
