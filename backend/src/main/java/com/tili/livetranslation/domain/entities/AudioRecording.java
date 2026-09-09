package com.tili.livetranslation.domain.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audio_recording")
public class AudioRecording extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    public LiveSession session;

    @Column(name = "file_path")
    public String filePath;        // In object storage (S3/MinIO) or local

    @Column(name = "format")
    public String format = "opus"; // or webm, wav, etc.

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "size_bytes")
    public Long sizeBytes;

    @Column(name = "retention_until")
    public Instant retentionUntil;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @Column(name = "deleted_at")
    public Instant deletedAt;
}
