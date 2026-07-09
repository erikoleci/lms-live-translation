package com.tili.livetranslation.dto;

import java.util.List;
import java.util.UUID;

/** Public, unauthenticated payload returned before a student joins. */
public record JoinInfoResponse(
        UUID sessionId,
        String title,
        String courseId,
        String sourceLanguage,
        List<String> targetLanguages,
        String status,
        String accessMode,
        boolean requiresAuthentication,
        long currentParticipants,
        int maxParticipants
) {}
