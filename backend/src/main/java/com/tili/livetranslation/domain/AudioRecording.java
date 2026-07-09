package com.tili.livetranslation.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audio_recording")
public class AudioRecording extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "session_id", nullable = false)
    public UUID sessionId;

    @Column(name = "file_path")
    public String filePath;

    public String format;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "size_bytes")
    public Long sizeBytes;

    @Column(name = "retention_until")
    public Instant retentionUntil;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();

    @Column(name = "deleted_at")
    public Instant deletedAt;

    public static AudioRecording findBySession(UUID sessionId) {
        return find("sessionId", sessionId).firstResult();
    }
}
