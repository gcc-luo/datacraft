package com.datacraft.datasource.application;

import com.datacraft.api.datasource.DatasourceRequest;
import com.datacraft.api.datasource.DatasourceStatus;
import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.datasource.domain.ConnectionTestOutcome;
import com.datacraft.datasource.domain.Datasource;
import com.datacraft.datasource.domain.DatasourceConnectionTester;
import com.datacraft.datasource.domain.DatasourceRepository;
import com.datacraft.datasource.security.SecretCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DatasourceApplicationServiceTest {
    private final DatasourceRepository repository = mock(DatasourceRepository.class);
    private final SecretCryptoService crypto = mock(SecretCryptoService.class);
    private final DatasourceConnectionTester connectionTester = mock(DatasourceConnectionTester.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-07T02:00:00Z"), ZoneOffset.UTC);
    private DatasourceApplicationService service;

    @BeforeEach
    void setUp() {
        service = new DatasourceApplicationService(repository, crypto, connectionTester, clock);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void encryptsPasswordBeforeSavingAndNeverReturnsIt() {
        DatasourceRequest request = request("warehouse", "secret");
        when(crypto.encrypt("secret")).thenReturn("v1:ciphertext");

        var response = service.create(request);

        ArgumentCaptor<Datasource> saved = ArgumentCaptor.forClass(Datasource.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().passwordCiphertext()).isEqualTo("v1:ciphertext");
        assertThat(response.name()).isEqualTo("warehouse");
        assertThat(response.toString()).doesNotContain("secret", "ciphertext");
    }

    @Test
    void keepsExistingCiphertextWhenUpdatePasswordIsBlank() {
        Datasource existing = datasource(7L, "warehouse", "v1:old");
        when(repository.findById(7L)).thenReturn(Optional.of(existing));

        service.update(7L, request("warehouse-v2", " "));

        ArgumentCaptor<Datasource> saved = ArgumentCaptor.forClass(Datasource.class);
        verify(repository).save(saved.capture());
        verify(crypto, never()).encrypt(any());
        assertThat(saved.getValue().passwordCiphertext()).isEqualTo("v1:old");
    }

    @Test
    void rejectsDuplicateNames() {
        when(repository.existsByName("warehouse", null)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("warehouse", "secret")))
                .isInstanceOf(DatasourceDuplicateException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void testsConnectionDecryptsOnlyForTesterAndPersistsSuccess() {
        Datasource existing = datasource(7L, "warehouse", "v1:cipher");
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(crypto.decrypt("v1:cipher")).thenReturn("secret");
        when(connectionTester.test(existing, "secret")).thenReturn(ConnectionTestOutcome.success(42));

        var result = service.testConnection(7L);

        assertThat(result.success()).isTrue();
        assertThat(result.status()).isEqualTo(DatasourceStatus.SUCCESS);
        assertThat(result.latencyMs()).isEqualTo(42);
        ArgumentCaptor<Datasource> saved = ArgumentCaptor.forClass(Datasource.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().status()).isEqualTo(DatasourceStatus.SUCCESS);
        assertThat(saved.getValue().lastTestMessage()).isEqualTo("连接成功");
    }

    @Test
    void testsUnsavedConfigurationWithoutPersistingIt() {
        DatasourceRequest request = request("warehouse", "secret");
        when(connectionTester.test(any(Datasource.class), eq("secret")))
                .thenReturn(ConnectionTestOutcome.success(18));

        var result = service.testConnection(request);

        assertThat(result.success()).isTrue();
        assertThat(result.status()).isEqualTo(DatasourceStatus.SUCCESS);
        assertThat(result.latencyMs()).isEqualTo(18);
        verify(repository, never()).save(any());
        verify(crypto, never()).encrypt(any());
    }

    @Test
    void testsEditedConfigurationWithExistingPasswordWhenPasswordIsBlank() {
        Datasource existing = datasource(7L, "warehouse", "v1:cipher");
        DatasourceRequest request = request("warehouse-v2", " ");
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(crypto.decrypt("v1:cipher")).thenReturn("secret");
        when(connectionTester.test(any(Datasource.class), eq("secret")))
                .thenReturn(ConnectionTestOutcome.success(21));

        var result = service.testConnection(7L, request);

        assertThat(result.success()).isTrue();
        assertThat(result.latencyMs()).isEqualTo(21);
        verify(repository, never()).save(any());
        verify(crypto, never()).encrypt(any());
    }

    @Test
    void failsWhenDatasourceDoesNotExist() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(404L)).isInstanceOf(DatasourceNotFoundException.class);
    }

    private DatasourceRequest request(String name, String password) {
        return new DatasourceRequest(name, DatasourceType.POSTGRESQL, "localhost", 5432,
                "datacraft", "reader", password, null);
    }

    private Datasource datasource(Long id, String name, String cipher) {
        return new Datasource(id, name, DatasourceType.POSTGRESQL, "localhost", 5432, "datacraft", "reader", cipher,
                null, DatasourceStatus.UNKNOWN, null, null, null, Instant.parse("2026-09-06T02:00:00Z"),
                Instant.parse("2026-09-06T02:00:00Z"));
    }
}
