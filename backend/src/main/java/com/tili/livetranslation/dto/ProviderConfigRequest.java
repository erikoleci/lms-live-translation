package com.tili.livetranslation.dto;

import jakarta.validation.constraints.NotBlank;

public record ProviderConfigRequest(
        @NotBlank String providerCode,
        @NotBlank String providerType, // STT / TRANSLATION / TTS
        Integer priority,
        Boolean enabled,
        String credentialsRef,
        java.util.List<String> supportedLanguages,
        java.util.List<String> supportedVoices,
        Long costLimitCents,
        Integer timeoutMs,
        String fallbackProviderCode
) {}
