package com.tili.livetranslation.service;

import com.tili.livetranslation.domain.*;
import com.tili.livetranslation.dto.JoinRequest;
import com.tili.livetranslation.dto.ParticipantUpdateRequest;
import com.tili.livetranslation.exception.ForbiddenException;
import com.tili.livetranslation.exception.NotFoundException;
import com.tili.livetranslation.exception.SessionFullException;
import com.tili.livetranslation.websocket.event.EventType;
import com.tili.livetranslation.websocket.event.WsEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ParticipantService {

    @Inject
    SessionService sessionService;

    @Inject
    SessionBroadcastService broadcastService;

    @Transactional
    public LiveParticipant join(UUID sessionId, String authenticatedUserId, JoinRequest req) {
        LiveSession session = sessionService.getOrThrow(sessionId);

        if (session.accessMode == AccessMode.CLOSED && authenticatedUserId == null) {
            throw new ForbiddenException("This session requires an authenticated LMS user.");
        }
        if (session.status == SessionState.ENDED || session.status == SessionState.EXPIRED
                || session.status == SessionState.FAILED) {
            throw new ForbiddenException("This session is no longer accepting participants.");
        }

        long activeCount = LiveParticipant.countActiveBySession(sessionId);
        if (activeCount >= session.maxParticipants) {
            throw new SessionFullException("Session has reached its maximum of "
                    + session.maxParticipants + " participants.");
        }

        LiveParticipant participant = new LiveParticipant();
        participant.sessionId = sessionId;
        participant.userId = authenticatedUserId;
        participant.anonymousName = authenticatedUserId == null
                ? (req.anonymousName() != null ? req.anonymousName() : "Guest")
                : null;
        participant.targetLanguage = req.targetLanguage();
        participant.audioEnabled = req.audioEnabled();
        participant.voiceCode = req.voiceCode();
        participant.connectionStatus = ConnectionStatus.CONNECTED;
        participant.persist();

        broadcastService.broadcastToSession(sessionId, new WsEvent(EventType.PARTICIPANT_JOINED,
                Map.of("participantId", participant.id.toString(),
                        "participantCount", LiveParticipant.countActiveBySession(sessionId))));

        return participant;
    }

    @Transactional
    public List<LiveParticipant> list(UUID sessionId) {
        sessionService.getOrThrow(sessionId);
        return LiveParticipant.list("sessionId", sessionId);
    }

    @Transactional
    public LiveParticipant getOrThrow(UUID sessionId, UUID participantId) {
        LiveParticipant p = LiveParticipant.findById(participantId);
        if (p == null || !p.sessionId.equals(sessionId)) {
            throw new NotFoundException("Participant not found: " + participantId);
        }
        return p;
    }

    @Transactional
    public LiveParticipant updatePreferences(UUID sessionId, UUID participantId, ParticipantUpdateRequest req) {
        LiveParticipant p = getOrThrow(sessionId, participantId);
        if (req.targetLanguage() != null) p.targetLanguage = req.targetLanguage();
        if (req.audioEnabled() != null) p.audioEnabled = req.audioEnabled();
        if (req.voiceCode() != null) p.voiceCode = req.voiceCode();
        return p;
    }

    @Transactional
    public void leave(UUID sessionId, UUID participantId) {
        LiveParticipant p = getOrThrow(sessionId, participantId);
        p.connectionStatus = ConnectionStatus.DISCONNECTED;
        p.leftAt = Instant.now();

        broadcastService.broadcastToSession(sessionId, new WsEvent(EventType.PARTICIPANT_LEFT,
                Map.of("participantId", participantId.toString(),
                        "participantCount", LiveParticipant.countActiveBySession(sessionId))));
    }
}
