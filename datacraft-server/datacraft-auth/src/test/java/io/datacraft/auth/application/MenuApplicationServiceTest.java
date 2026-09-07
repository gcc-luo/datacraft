package io.datacraft.auth.application;

import io.datacraft.auth.domain.MenuItem;
import io.datacraft.auth.domain.MenuRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MenuApplicationServiceTest {
    @Test
    void buildsSortedMenuTreeFromRoleMenus() {
        MenuRepository repository = roles -> List.of(
                new MenuItem(2L, "child", "子菜单", "/child", "child", 1L, 20, true),
                new MenuItem(1L, "root", "根菜单", "/root", "root", null, 10, true));

        var menus = new MenuApplicationService(repository).menusForRoles(List.of("ADMIN"));

        assertThat(menus).singleElement().satisfies(root -> {
            assertThat(root.code()).isEqualTo("root");
            assertThat(root.children()).extracting("code").containsExactly("child");
        });
    }
}
