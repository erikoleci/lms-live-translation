package com.tili.livetranslation.dto;

import com.tili.livetranslation.domain.TranscriptSegment;

import java.time.Instant;
import java.util.UUID;

public record TranscriptSegmentDto(
        UUID id,
        long sequenceNo,
        String sourceLanguage,
        String originalText,
        boolean isFinal,
        Float confidence,
        Long startOffsetMs,
        Long endOffsetMs,
        Instant createdAt
) {
    public static TranscriptSegmentDto from(TranscriptSegment s) {
        return new TranscriptSegmentDto(s.id, s.sequenceNo, s.sourceLanguage, s.originalText,
                s.isFinal, s.confidence, s.startOffsetMs, s.endOffsetMs, s.createdAt);
    }
}
