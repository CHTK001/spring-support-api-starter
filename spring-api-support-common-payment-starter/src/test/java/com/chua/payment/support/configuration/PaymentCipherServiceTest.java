package com.chua.payment.support.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentCipherServiceTest {

    @Test
    void shouldUseConfiguredKeyForEncryptionAndDecryption() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("payment.encryption.key", "custom-payment-key");
        PaymentCipherService service = new PaymentCipherService(environment);

        String encrypted = service.encrypt("secret-value");

        assertEquals("secret-value", service.decrypt(encrypted));
    }

    @Test
    void shouldDecryptLegacyCipherTextWithFallbackKey() {
        PaymentCipherService legacyService = new PaymentCipherService(new MockEnvironment());
        String encrypted = legacyService.encrypt("legacy-value");

        MockEnvironment environment = new MockEnvironment()
                .withProperty("payment.encryption.key", "new-payment-key")
                .withProperty("payment.encryption.legacy-keys", PaymentCipherService.LEGACY_DEFAULT_KEY);
        PaymentCipherService service = new PaymentCipherService(environment);

        assertEquals("legacy-value", service.decrypt(encrypted));
    }

    @Test
    void shouldFailWhenCipherTextDoesNotMatchAnyConfiguredKey() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("payment.encryption.key", "new-payment-key")
                .withProperty("payment.encryption.legacy-keys", "another-key");
        PaymentCipherService service = new PaymentCipherService(environment);

        assertThrows(RuntimeException.class, () -> service.decrypt("not-a-valid-cipher-text"));
    }
}
