package com.tili.livetranslation.dto;

import com.tili.livetranslation.domain.TranslationSegment;

import java.time.Instant;
import java.util.UUID;

public record TranslationSegmentDto(
        UUID id,
        UUID transcriptSegmentId,
        String targetLanguage,
        String translatedText,
        boolean isFinal,
        Instant createdAt
) {
    public static TranslationSegmentDto from(TranslationSegment t) {
        return new TranslationSegmentDto(t.id, t.transcriptSegmentId, t.targetLanguage,
                t.translatedText, t.isFinal, t.createdAt);
    }
}
