package com.tili.livetranslation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSessionRequest(
        @NotBlank String title,
        String courseId,
        @NotBlank String sourceLanguage,
        boolean autoDetectSource,
        @NotNull java.util.List<String> targetLanguages,
        String accessMode,               // OPEN / CLOSED, defaults to OPEN
        Boolean recordingEnabled,
        Boolean studentTranscriptDownloadEnabled,
        Integer maxParticipants
) {}
