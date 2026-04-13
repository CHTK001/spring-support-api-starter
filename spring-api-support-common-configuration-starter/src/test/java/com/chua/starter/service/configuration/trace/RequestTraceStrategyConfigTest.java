package com.chua.starter.service.configuration.trace;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTraceStrategyConfigTest {

    @Test
    void normalizeUsesRootLocaleForLowerCase() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
            config.setRawStorageType("FILE");
            config.setBehaviorStorageType("DATABASE");
            config.setAiModel("MODEL-I");

            config.normalize();

            assertThat(config.getRawStorageType()).isEqualTo("file");
            assertThat(config.getBehaviorStorageType()).isEqualTo("database");
            assertThat(config.getAiModel()).isEqualTo("model-i");
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void normalizeRestoresAiRateLimitDefaultsWhenValuesAreInvalid() {
        RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
        config.setAiRateLimitEnabled(null);
        config.setAiRateLimitWindowSeconds(0);
        config.setAiRateLimitMaxRequests(-1);
        config.setAiTimeoutMs(0L);

        config.normalize();

        assertThat(config.getAiRateLimitEnabled()).isTrue();
        assertThat(config.getAiRateLimitWindowSeconds()).isEqualTo(60);
        assertThat(config.getAiRateLimitMaxRequests()).isEqualTo(30);
        assertThat(config.getAiTimeoutMs()).isEqualTo(1500L);
    }
}
