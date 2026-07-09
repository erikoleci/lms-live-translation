package com.tili.livetranslation.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "translation_segment")
public class TranslationSegment extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "transcript_segment_id", nullable = false)
    public UUID transcriptSegmentId;

    @Column(name = "target_language", nullable = false)
    public String targetLanguage;

    @Column(name = "translated_text", nullable = false, columnDefinition = "TEXT")
    public String translatedText;

    @Column(name = "is_final", nullable = false)
    public boolean isFinal = false;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();

    public static List<TranslationSegment> findByTranscriptSegment(UUID transcriptSegmentId) {
        return list("transcriptSegmentId", transcriptSegmentId);
    }
}
