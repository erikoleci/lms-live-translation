package com.tili.livetranslation.dto;

import com.tili.livetranslation.domain.ProviderConfig;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record ProviderConfigResponse(
        UUID id,
        String providerCode,
        String providerType,
        int priority,
        boolean enabled,
        String credentialsRef,
        List<String> supportedLanguages,
        List<String> supportedVoices,
        Long costLimitCents,
        int timeoutMs,
        String fallbackProviderCode
) {
    public static ProviderConfigResponse from(ProviderConfig c) {
        return new ProviderConfigResponse(
                c.id, c.providerCode, c.providerType.name(), c.priority, c.enabled,
                c.credentialsRef,
                c.supportedLanguages == null ? List.of() : Arrays.asList(c.supportedLanguages.split(",")),
                c.supportedVoices == null ? List.of() : Arrays.asList(c.supportedVoices.split(",")),
                c.costLimitCents, c.timeoutMs, c.fallbackProviderCode
        );
    }
}
