package com.tili.livetranslation.dto;

import java.util.List;

/** Fields a teacher/admin may PATCH before or during a session (spec 6.1). */
public record SessionUpdateRequest(
        String title,
        List<String> targetLanguages,
        String accessMode,
        Boolean recordingEnabled,
        Boolean studentTranscriptDownloadEnabled,
        Integer maxParticipants
) {}
