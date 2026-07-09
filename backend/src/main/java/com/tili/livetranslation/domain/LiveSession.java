package com.tili.livetranslation.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "live_session")
public class LiveSession extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String title;

    @Column(name = "teacher_id", nullable = false)
    public String teacherId;

    @Column(name = "course_id")
    public String courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_mode", nullable = false)
    public AccessMode accessMode = AccessMode.OPEN;

    @Column(name = "source_language", nullable = false)
    public String sourceLanguage; // IT / EN / SQ

    @Column(name = "auto_detect_source", nullable = false)
    public boolean autoDetectSource = false;

    /** Comma-separated language codes, e.g. "IT,EN,SQ" */
    @Column(name = "target_languages", nullable = false)
    public String targetLanguages;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SessionState status = SessionState.CREATED;

    @Column(name = "join_code", nullable = false, unique = true)
    public String joinCode;

    @Column(name = "recording_enabled", nullable = false)
    public boolean recordingEnabled = false;

    @Column(name = "student_transcript_download_enabled", nullable = false)
    public boolean studentTranscriptDownloadEnabled = false;

    @Column(name = "max_participants", nullable = false)
    public int maxParticipants = 300;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();

    @Column(name = "started_at")
    public Instant startedAt;

    @Column(name = "ended_at")
    public Instant endedAt;

    @Column(name = "expires_at")
    public Instant expiresAt;

    @Version
    public long version;

    public List<String> targetLanguageList() {
        return List.of(targetLanguages.split(","));
    }

    public static LiveSession findByJoinCode(String joinCode) {
        return find("joinCode", joinCode).firstResult();
    }

    public static List<LiveSession> findActiveExpirable(Instant cutoff) {
        return list("status in ?1 and createdAt < ?2",
                List.of(SessionState.WAITING, SessionState.CREATED), cutoff);
    }
}
