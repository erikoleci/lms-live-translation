package com.tili.livetranslation.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinRequest(
        @NotBlank String targetLanguage,
        String anonymousName, // used when accessMode = OPEN and no SSO identity
        boolean audioEnabled,
        String voiceCode
) {}
