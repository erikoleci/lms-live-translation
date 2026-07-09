package com.tili.livetranslation.dto;

import com.tili.livetranslation.domain.LiveSession;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String title,
        String teacherId,
        String courseId,
        String accessMode,
        String sourceLanguage,
        boolean autoDetectSource,
        List<String> targetLanguages,
        String status,
        String joinCode,
        boolean recordingEnabled,
        boolean studentTranscriptDownloadEnabled,
        int maxParticipants,
        long participantCount,
        Instant createdAt,
        Instant startedAt,
        Instant endedAt,
        Instant expiresAt
) {
    public static SessionResponse from(LiveSession s, long participantCount) {
        return new SessionResponse(
                s.id, s.title, s.teacherId, s.courseId,
                s.accessMode.name(), s.sourceLanguage, s.autoDetectSource,
                s.targetLanguageList(), s.status.name(), s.joinCode,
                s.recordingEnabled, s.studentTranscriptDownloadEnabled, s.maxParticipants,
                participantCount, s.createdAt, s.startedAt, s.endedAt, s.expiresAt
        );
    }
}
