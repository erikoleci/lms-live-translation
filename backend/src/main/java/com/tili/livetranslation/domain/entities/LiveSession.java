package com.tili.livetranslation.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tili.livetranslation.domain.enums.AccessMode;
import com.tili.livetranslation.domain.enums.Language;
import com.tili.livetranslation.domain.enums.SessionStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "live_session")
public class LiveSession extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "source_language", nullable = false)
    public Language sourceLanguage = Language.IT;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_target_languages", joinColumns = @JoinColumn(name = "session_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "target_language")
    public Set<Language> targetLanguages = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SessionStatus status = SessionStatus.CREATED;

    @Column(name = "join_code", unique = true, length = 10)
    public String joinCode;

    @Column(name = "qr_code_url")
    public String qrCodeUrl;

    @Column(name = "recording_enabled")
    public boolean recordingEnabled = false;

    @Column(name = "student_transcript_download_enabled")
    public boolean studentTranscriptDownloadEnabled = true;

    @Column(name = "max_participants")
    public int maxParticipants = 300;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "started_at")
    public Instant startedAt;

    @Column(name = "ended_at")
    public Instant endedAt;

    @Column(name = "expires_at")
    public Instant expiresAt;

    @Column(name = "current_participant_count")
    public int currentParticipantCount = 0;

    @Column(name = "last_activity_at")
    public Instant lastActivityAt;

    @JsonIgnore
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public Set<LiveParticipant> participants = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public Set<TranscriptSegment> transcriptSegments = new HashSet<>();

    @JsonIgnore
    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public AudioRecording audioRecording;

    public boolean isActive() {
        return status == SessionStatus.ACTIVE || status == SessionStatus.PAUSED;
    }

    public boolean canAcceptParticipants() {
        return (status == SessionStatus.WAITING || status == SessionStatus.ACTIVE)
                && currentParticipantCount < maxParticipants;
    }

    public void addTargetLanguage(Language lang) {
        if (lang != sourceLanguage) {
            targetLanguages.add(lang);
        }
    }
}
