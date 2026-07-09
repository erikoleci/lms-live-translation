package com.tili.livetranslation.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tts_chunk")
public class TtsChunk extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "translation_segment_id", nullable = false)
    public UUID translationSegmentId;

    public String language;

    @Column(name = "voice_code", nullable = false)
    public String voiceCode;

    @Column(name = "file_path")
    public String filePath;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();
}
