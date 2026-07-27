package com.tili.livetranslation.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tili.livetranslation.domain.enums.Language;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_participant")
public class LiveParticipant extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    public LiveSession session;

    @Column(name = "user_id")
    public String userId;           // Nullable for anonymous QR join

    @Column(name = "anonymous_name")
    public String anonymousName;    // e.g. "Student-4721" or provided name

    @Enumerated(EnumType.STRING)
    @Column(name = "target_language", nullable = false)
    public Language targetLanguage = Language.EN;

    @Column(name = "audio_enabled")
    public boolean audioEnabled = false;

    @Column(name = "voice_code")
    public String voiceCode;        // e.g. "alloy", "shqip-neural-v1", or provider specific

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    public Instant joinedAt;

    @Column(name = "left_at")
    public Instant leftAt;

    @Column(name = "connection_status")
    public String connectionStatus = "CONNECTED";  // CONNECTED, DISCONNECTED, RECONNECTING

    @Column(name = "last_heartbeat_at")
    public Instant lastHeartbeatAt;
}