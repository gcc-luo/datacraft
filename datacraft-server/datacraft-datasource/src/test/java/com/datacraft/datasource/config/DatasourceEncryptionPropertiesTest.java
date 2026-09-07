package com.datacraft.datasource.config;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class DatasourceEncryptionPropertiesTest {

    @Test
    void acceptsExactly32DecodedBytes() {
        String key = Base64.getEncoder().encodeToString(new byte[32]);

        DatasourceEncryptionProperties properties = new DatasourceEncryptionProperties(key);

        assertThat(properties.keyBytes()).hasSize(32);
    }

    @Test
    void rejectsMissingOrShortKeys() {
        assertThatIllegalStateException().isThrownBy(() -> new DatasourceEncryptionProperties(null));
        assertThatIllegalStateException().isThrownBy(() -> new DatasourceEncryptionProperties(
                Base64.getEncoder().encodeToString(new byte[16])));
    }
}
