package io.datacraft.auth.infrastructure.persistence;

import io.datacraft.auth.domain.MenuItem;
import io.datacraft.auth.domain.UserAccount;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryMappingTest {

    @Test
    void disabledUserCannotAuthenticate() {
        UserAccount account = new UserAccount(1L, "operator", "Operator", "hash", false, List.of("VIEWER"));

        assertThat(account.canAuthenticate()).isFalse();
    }

    @Test
    void menuRetainsParentRelationshipWhenMapped() {
        MenuItem item = new MenuItem(3L, "datasource", "数据源", "/datasources", "database", 10L, 2, true);

        assertThat(item.parentId()).isEqualTo(10L);
        assertThat(item.path()).isEqualTo("/datasources");
    }
}
