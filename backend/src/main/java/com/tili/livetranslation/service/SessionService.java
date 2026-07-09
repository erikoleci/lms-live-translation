package com.tili.livetranslation.service;

import com.tili.livetranslation.domain.*;
import com.tili.livetranslation.dto.*;
import com.tili.livetranslation.exception.*;
import com.tili.livetranslation.websocket.event.EventType;
import com.tili.livetranslation.websocket.event.WsEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionService {

    private static final String JOIN_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I
    private static final SecureRandom RANDOM = new SecureRandom();

    @ConfigProperty(name = "zana.session.default-max-participants")
    int defaultMaxParticipants;

    @Inject
    SessionBroadcastService broadcastService;

    @Inject
    QrCodeService qrCodeService;

    @Transactional
    public LiveSession createSession(String teacherId, CreateSessionRequest req) {
        LiveSession session = new LiveSession();
        session.title = req.title();
        session.teacherId = teacherId;
        session.courseId = req.courseId();
        session.sourceLanguage = req.sourceLanguage();
        session.autoDetectSource = req.autoDetectSource();
        session.targetLanguages = String.join(",", req.targetLanguages());
        session.accessMode = req.accessMode() != null
                ? AccessMode.valueOf(req.accessMode())
                : AccessMode.OPEN;
        session.recordingEnabled = Boolean.TRUE.equals(req.recordingEnabled());
        session.studentTranscriptDownloadEnabled = Boolean.TRUE.equals(req.studentTranscriptDownloadEnabled());
        session.maxParticipants = req.maxParticipants() != null ? req.maxParticipants() : defaultMaxParticipants;
        session.status = SessionState.CREATED;
        session.joinCode = generateUniqueJoinCode();
        session.persist();
        return session;
    }

    @Transactional
    public LiveSession getOrThrow(UUID sessionId) {
        LiveSession session = LiveSession.findById(sessionId);
        if (session == null) {
            throw new NotFoundException("Session not found: " + sessionId);
        }
        return session;
    }

    public LiveSession getByJoinCodeOrThrow(String joinCode) {
        LiveSession session = LiveSession.findByJoinCode(joinCode);
        if (session == null) {
            throw new NotFoundException("No session for join code: " + joinCode);
        }
        return session;
    }

    @Transactional
    public LiveSession updateSession(UUID sessionId, SessionUpdateRequest req) {
        LiveSession session = getOrThrow(sessionId);
        if (req.title() != null) session.title = req.title();
        if (req.targetLanguages() != null) session.targetLanguages = String.join(",", req.targetLanguages());
        if (req.accessMode() != null) session.accessMode = AccessMode.valueOf(req.accessMode());
        if (req.recordingEnabled() != null) session.recordingEnabled = req.recordingEnabled();
        if (req.studentTranscriptDownloadEnabled() != null) {
            session.studentTranscriptDownloadEnabled = req.studentTranscriptDownloadEnabled();
        }
        if (req.maxParticipants() != null) session.maxParticipants = req.maxParticipants();
        return session;
    }

    @Transactional
    public void deleteSession(UUID sessionId) {
        LiveSession session = getOrThrow(sessionId);
        if (session.status == SessionState.ACTIVE) {
            throw new InvalidStateTransitionException("Cannot delete an active session; end it first.");
        }
        session.delete();
    }

    /**
     * Applies a validated state transition and broadcasts SESSION_STATUS_CHANGED
     * to every connected channel (spec section 6.2).
     */
    @Transactional
    public LiveSession changeState(UUID sessionId, SessionState target) {
        LiveSession session = getOrThrow(sessionId);
        if (!session.status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition session from " + session.status + " to " + target);
        }
        session.status = target;
        Instant now = Instant.now();
        switch (target) {
            case ACTIVE -> {
                if (session.startedAt == null) session.startedAt = now;
            }
            case ENDED, FAILED, EXPIRED -> session.endedAt = now;
            default -> { /* no timestamp side effect */ }
        }

        broadcastService.broadcastToSession(sessionId, new WsEvent(EventType.SESSION_STATUS_CHANGED,
                java.util.Map.of("status", target.name())));
        return session;
    }

    @Transactional
    public long activeParticipantCount(UUID sessionId) {
        return LiveParticipant.countActiveBySession(sessionId);
    }

    /** Scheduled by SessionExpiryScheduler: expires stale CREATED/WAITING sessions. */
    @Transactional
    public void expireInactiveSessions(int inactivityMinutes) {
        Instant cutoff = Instant.now().minus(inactivityMinutes, ChronoUnit.MINUTES);
        List<LiveSession> stale = LiveSession.findActiveExpirable(cutoff);
        for (LiveSession s : stale) {
            s.status = SessionState.EXPIRED;
            s.endedAt = Instant.now();
            broadcastService.broadcastToSession(s.id, new WsEvent(EventType.SESSION_STATUS_CHANGED,
                java.util.Map.of("status", "EXPIRED")));
        }
    }

    private String generateUniqueJoinCode() {
        String code;
        do {
            code = randomJoinCode();
        } while (LiveSession.findByJoinCode(code) != null);
        return code;
    }

    private String randomJoinCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(JOIN_CODE_ALPHABET.charAt(RANDOM.nextInt(JOIN_CODE_ALPHABET.length())));
        }
        return sb.toString();
    }
}
