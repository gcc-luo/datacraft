package io.datacraft.auth.infrastructure.persistence;

import io.datacraft.auth.domain.UserAccount;
import io.datacraft.auth.domain.UserRepository;
import io.datacraft.auth.infrastructure.persistence.entity.UserEntity;
import io.datacraft.auth.infrastructure.persistence.mapper.RoleMapper;
import io.datacraft.auth.infrastructure.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public JdbcUserRepository(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        UserEntity entity = userMapper.findByUsername(username);
        if (entity == null) {
            return Optional.empty();
        }
        List<String> roles = roleMapper.findCodesByUserId(entity.getId());
        return Optional.of(new UserAccount(entity.getId(), entity.getUsername(), entity.getDisplayName(),
                entity.getPasswordHash(), Boolean.TRUE.equals(entity.getEnabled()), roles));
    }

    @Override
    @Transactional
    public UserAccount insert(UserAccount account) {
        UserEntity entity = new UserEntity();
        entity.setUsername(account.username());
        entity.setDisplayName(account.displayName());
        entity.setPasswordHash(account.passwordHash());
        entity.setEnabled(account.enabled());
        userMapper.insert(entity);
        return new UserAccount(entity.getId(), entity.getUsername(), entity.getDisplayName(), entity.getPasswordHash(),
                Boolean.TRUE.equals(entity.getEnabled()), account.roleCodes());
    }

    @Override
    public void assignRole(Long userId, String roleCode) {
        Long roleId = roleMapper.findIdByCode(roleCode);
        if (roleId == null) {
            throw new IllegalStateException("Role is not seeded: " + roleCode);
        }
        roleMapper.insertUserRole(userId, roleId);
    }
}
