package com.tili.livetranslation.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "live_participant")
public class LiveParticipant extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "session_id", nullable = false)
    public UUID sessionId;

    @Column(name = "user_id")
    public String userId; // null when anonymous / OPEN access

    @Column(name = "anonymous_name")
    public String anonymousName;

    @Column(name = "target_language", nullable = false)
    public String targetLanguage;

    @Column(name = "audio_enabled", nullable = false)
    public boolean audioEnabled = false;

    @Column(name = "voice_code")
    public String voiceCode;

    @Column(name = "joined_at", nullable = false)
    public Instant joinedAt = Instant.now();

    @Column(name = "left_at")
    public Instant leftAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false)
    public ConnectionStatus connectionStatus = ConnectionStatus.CONNECTED;

    public static List<LiveParticipant> findActiveBySession(UUID sessionId) {
        return list("sessionId = ?1 and connectionStatus = ?2", sessionId, ConnectionStatus.CONNECTED);
    }

    public static long countActiveBySession(UUID sessionId) {
        return count("sessionId = ?1 and connectionStatus = ?2", sessionId, ConnectionStatus.CONNECTED);
    }
}
