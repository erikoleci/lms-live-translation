package com.tili.livetranslation.domain.entities;

import com.tili.livetranslation.domain.enums.Language;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "translation_segment", indexes = {
    @Index(name = "idx_translation_segment_lang", columnList = "transcript_segment_id, target_language")
})
public class TranslationSegment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_segment_id", nullable = false)
    public TranscriptSegment transcriptSegment;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_language", nullable = false)
    public Language targetLanguage;

    @Column(name = "translated_text", columnDefinition = "TEXT")
    public String translatedText;

    @Column(name = "is_final")
    public boolean isFinal = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;
}
