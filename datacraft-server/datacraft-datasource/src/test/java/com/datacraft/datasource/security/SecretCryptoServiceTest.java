package com.datacraft.datasource.security;

import com.datacraft.datasource.config.DatasourceEncryptionProperties;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SecretCryptoServiceTest {

    private final SecretCryptoService crypto = new SecretCryptoService(new DatasourceEncryptionProperties(
            Base64.getEncoder().encodeToString(new byte[32])));

    @Test
    void encryptsAndDecryptsWithoutStoringPlaintext() {
        String ciphertext = crypto.encrypt("p@ssword");

        assertThat(ciphertext).startsWith("v1:").doesNotContain("p@ssword");
        assertThat(crypto.decrypt(ciphertext)).isEqualTo("p@ssword");
    }

    @Test
    void usesRandomNonceForEachEncryption() {
        assertThat(crypto.encrypt("same")).isNotEqualTo(crypto.encrypt("same"));
    }

    @Test
    void rejectsTamperedCiphertext() {
        String ciphertext = crypto.encrypt("secret");
        String tampered = ciphertext.substring(0, ciphertext.length() - 2) + "AA";

        assertThatIllegalArgumentException().isThrownBy(() -> crypto.decrypt(tampered));
    }
}
