package com.tili.livetranslation.dto;

import com.tili.livetranslation.domain.LiveParticipant;

import java.time.Instant;
import java.util.UUID;

public record ParticipantResponse(
        UUID id,
        UUID sessionId,
        String displayName,
        String targetLanguage,
        boolean audioEnabled,
        String voiceCode,
        String connectionStatus,
        Instant joinedAt,
        Instant leftAt
) {
    public static ParticipantResponse from(LiveParticipant p) {
        String displayName = p.userId != null ? p.userId : p.anonymousName;
        return new ParticipantResponse(p.id, p.sessionId, displayName, p.targetLanguage,
                p.audioEnabled, p.voiceCode, p.connectionStatus.name(), p.joinedAt, p.leftAt);
    }
}
