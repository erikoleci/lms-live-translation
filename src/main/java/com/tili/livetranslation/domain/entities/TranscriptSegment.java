package com.tili.livetranslation.domain.entities;

import com.tili.livetranslation.domain.enums.Language;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transcript_segment", indexes = {
    @Index(name = "idx_transcript_session_seq", columnList = "session_id, sequence_no")
})
public class TranscriptSegment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    public LiveSession session;

    @Column(name = "sequence_no", nullable = false)
    public long sequenceNo;   // Monotonic increasing per session

    @Enumerated(EnumType.STRING)
    @Column(name = "source_language", nullable = false)
    public Language sourceLanguage;

    @Column(name = "original_text", columnDefinition = "TEXT", nullable = false)
    public String originalText;

    @Column(name = "is_final")
    public boolean isFinal = false;

    @Column(name = "confidence")
    public Double confidence;   // 0.0 - 1.0 from STT

    @Column(name = "start_offset_ms")
    public Long startOffsetMs;

    @Column(name = "end_offset_ms")
    public Long endOffsetMs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    // For partial updates: we can have a "replaced_by" or just update text when final comes
}
