package com.datacraft.datasource.application;

import com.datacraft.datasource.domain.DatasourceRepository;
import com.datacraft.datasource.security.SecretCryptoService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcDatasourceConnectionProviderTest {
    @Test
    void rejectsUnknownDatasourceWithoutAttemptingConnection() {
        DatasourceRepository repository = mock(DatasourceRepository.class);
        when(repository.findById(99L)).thenReturn(java.util.Optional.empty());
        JdbcDatasourceConnectionProvider provider = new JdbcDatasourceConnectionProvider(repository,
                mock(SecretCryptoService.class), mock(JdbcUrlBuilder.class));

        assertThatThrownBy(() -> provider.open(99L))
                .isInstanceOf(DatasourceNotFoundException.class);
    }
}
