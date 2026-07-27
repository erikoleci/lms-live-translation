package com.tili.livetranslation.domain.entities;

import com.tili.livetranslation.domain.enums.Language;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tts_chunk")
public class TtsChunk extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "translation_segment_id", nullable = false)
    public TranslationSegment translationSegment;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    public Language language;

    @Column(name = "voice_code")
    public String voiceCode;

    @Column(name = "file_path")   // Optional: if we store audio files in object storage
    public String filePath;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "audio_data", columnDefinition = "BYTEA") // For small chunks or if not using object storage
    public byte[] audioData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;
}
