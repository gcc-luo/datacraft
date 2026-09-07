package io.datacraft.auth.application;

import io.datacraft.api.system.MenuDto;
import io.datacraft.auth.domain.MenuItem;
import io.datacraft.auth.domain.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
public class MenuApplicationService {
    private final MenuRepository menuRepository;

    public MenuApplicationService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<MenuDto> menusForRoles(Collection<String> roles) {
        List<MenuItem> items = menuRepository.findForRoles(roles);
        return buildChildren(items, null);
    }

    private List<MenuDto> buildChildren(List<MenuItem> items, Long parentId) {
        return items.stream()
                .filter(MenuItem::enabled)
                .filter(item -> parentId == null ? item.parentId() == null : parentId.equals(item.parentId()))
                .sorted(Comparator.comparingInt(MenuItem::sortOrder).thenComparing(MenuItem::id))
                .map(item -> new MenuDto(item.code(), item.title(), item.path(), item.icon(), item.parentId(), item.sortOrder(),
                        buildChildren(items, item.id())))
                .toList();
    }
}
