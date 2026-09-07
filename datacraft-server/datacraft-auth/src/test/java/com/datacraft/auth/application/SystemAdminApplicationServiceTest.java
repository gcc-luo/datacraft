package com.datacraft.auth.application;

import com.datacraft.api.system.AdminUserRequest;
import com.datacraft.api.system.AdminUserResponse;
import com.datacraft.auth.infrastructure.persistence.entity.UserEntity;
import com.datacraft.auth.infrastructure.persistence.mapper.MenuMapper;
import com.datacraft.auth.infrastructure.persistence.mapper.RoleMapper;
import com.datacraft.auth.infrastructure.persistence.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemAdminApplicationServiceTest {
    private final UserMapper userMapper = mock(UserMapper.class);
    private final RoleMapper roleMapper = mock(RoleMapper.class);
    private final MenuMapper menuMapper = mock(MenuMapper.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final SystemAdminApplicationService service = new SystemAdminApplicationService(
            userMapper, roleMapper, menuMapper, passwordEncoder);

    @Test
    void createsUserWithHashedPasswordAndSafeResponse() {
        when(userMapper.findByUsername("analyst")).thenReturn(null);
        when(roleMapper.findIdsByCodes(List.of("ANALYST"))).thenReturn(List.of(7L));
        when(passwordEncoder.encode("secret")).thenReturn("bcrypt-hash");
        when(userMapper.insert(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(11L);
            return 1;
        });
        when(userMapper.findForAdminById(11L)).thenReturn(user("analyst", "分析员", "bcrypt-hash", true));
        when(roleMapper.findCodesByUserId(11L)).thenReturn(List.of("ANALYST"));

        AdminUserResponse response = service.createUser(
                new AdminUserRequest("analyst", "分析员", "secret", List.of("ANALYST"), true));

        assertThat(response.username()).isEqualTo("analyst");
        assertThat(response.roleCodes()).containsExactly("ANALYST");
        assertThat(response).hasToString(response.toString());
        verify(userMapper).insert(any(UserEntity.class));
    }

    @Test
    void refusesToDisableAdminRole() {
        var role = new com.datacraft.auth.infrastructure.persistence.entity.RoleEntity();
        role.setId(1L);
        role.setCode("ADMIN");
        role.setName("管理员");
        role.setEnabled(true);
        when(roleMapper.findForAdminById(1L)).thenReturn(role);

                org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.updateRole(
                        1L, new com.datacraft.api.system.AdminRoleRequest("ADMIN", "管理员", List.of(), false)))
                .isInstanceOf(SystemAdminException.class)
                .hasMessageContaining("管理员角色不能改名或停用");
    }

    private UserEntity user(String username, String displayName, String passwordHash, boolean enabled) {
        UserEntity entity = new UserEntity();
        entity.setId(11L);
        entity.setUsername(username);
        entity.setDisplayName(displayName);
        entity.setPasswordHash(passwordHash);
        entity.setEnabled(enabled);
        return entity;
    }
}
