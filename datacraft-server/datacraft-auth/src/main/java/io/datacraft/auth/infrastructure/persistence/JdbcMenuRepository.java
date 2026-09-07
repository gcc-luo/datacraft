package io.datacraft.auth.infrastructure.persistence;

import io.datacraft.auth.domain.MenuItem;
import io.datacraft.auth.domain.MenuRepository;
import io.datacraft.auth.infrastructure.persistence.entity.MenuEntity;
import io.datacraft.auth.infrastructure.persistence.mapper.MenuMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class JdbcMenuRepository implements MenuRepository {
    private final MenuMapper menuMapper;

    public JdbcMenuRepository(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public List<MenuItem> findForRoles(Collection<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        return menuMapper.findForRoles(roleCodes.stream().sorted().toList()).stream().map(this::toDomain).toList();
    }

    private MenuItem toDomain(MenuEntity entity) {
        return new MenuItem(entity.getId(), entity.getCode(), entity.getTitle(), entity.getPath(), entity.getIcon(),
                entity.getParentId(), entity.getSortOrder() == null ? 0 : entity.getSortOrder(),
                Boolean.TRUE.equals(entity.getEnabled()));
    }
}
