package com.tili.livetranslation.resource;

import com.tili.livetranslation.domain.entities.LiveParticipant;
import com.tili.livetranslation.domain.entities.LiveSession;
import com.tili.livetranslation.domain.enums.AccessMode;
import com.tili.livetranslation.domain.enums.Language;
import com.tili.livetranslation.domain.enums.SessionStatus;
import com.tili.livetranslation.dto.CreateSessionRequest;
import com.tili.livetranslation.dto.JoinSessionRequest;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Sessions", description = "Live Translation Session Management")
public class SessionResource {

    @POST
    @Transactional
    @Operation(summary = "Create a new live translation session")
    public Response createSession(CreateSessionRequest req) {
        String teacherId = "teacher-demo-123";

        LiveSession session = new LiveSession();
        session.title = req.title != null ? req.title : "Live Session " + Instant.now();
        session.teacherId = teacherId;
        session.courseId = req.courseId;
        session.accessMode = req.accessMode != null ? req.accessMode : AccessMode.OPEN;
        session.sourceLanguage = req.sourceLanguage != null ? req.sourceLanguage : Language.IT;
        session.targetLanguages = req.targetLanguages != null
                ? new HashSet<>(req.targetLanguages)
                : new HashSet<>(List.of(Language.EN, Language.SQ));
        session.status = SessionStatus.CREATED;
        session.recordingEnabled = req.recordingEnabled;
        session.studentTranscriptDownloadEnabled = req.studentTranscriptDownloadEnabled;
        session.maxParticipants = req.maxParticipants > 0 ? req.maxParticipants : 300;
        session.joinCode = generateJoinCode();
        session.persist();

        return Response.status(Response.Status.CREATED).entity(session).build();
    }

    @GET
    @Operation(summary = "List sessions")
    public List<LiveSession> listSessions(@QueryParam("status") SessionStatus status) {
        if (status != null) {
            return LiveSession.list("status", Sort.by("createdAt").descending(), status);
        }
        return LiveSession.listAll(Sort.by("createdAt").descending());
    }

    @GET
    @Path("/{sessionId}")
    @Operation(summary = "Get session by id")
    public LiveSession getSession(@PathParam("sessionId") UUID sessionId) {
        LiveSession session = LiveSession.findById(sessionId);
        if (session == null) {
            throw new NotFoundException("Session not found");
        }
        return session;
    }

    @PATCH
    @Path("/{sessionId}/state")
    @Transactional
    @Operation(summary = "Change session state")
    public LiveSession changeState(
            @PathParam("sessionId") UUID sessionId,
            @QueryParam("state") SessionStatus newState) {

        LiveSession session = LiveSession.findById(sessionId);
        if (session == null) {
            throw new NotFoundException("Session not found");
        }
        if (newState == null) {
            throw new BadRequestException("state is required");
        }

        SessionStatus current = session.status;

        if (newState == SessionStatus.ACTIVE
                && (current == SessionStatus.CREATED
                || current == SessionStatus.WAITING
                || current == SessionStatus.PAUSED)) {
            session.status = SessionStatus.ACTIVE;
            if (session.startedAt == null) {
                session.startedAt = Instant.now();
            }
        } else if (newState == SessionStatus.WAITING && current == SessionStatus.CREATED) {
            session.status = SessionStatus.WAITING;
        } else if (newState == SessionStatus.PAUSED && current == SessionStatus.ACTIVE) {
            session.status = SessionStatus.PAUSED;
        } else if (newState == SessionStatus.ENDED) {
            session.status = SessionStatus.ENDED;
            session.endedAt = Instant.now();
        } else {
            throw new BadRequestException("Invalid state transition from " + current + " to " + newState);
        }

        session.lastActivityAt = Instant.now();
        return session;
    }

    @GET
    @Path("/{sessionId}/join-info")
    @Operation(summary = "Public join info by session id")
    public Response getJoinInfo(@PathParam("sessionId") UUID sessionId) {
        LiveSession session = LiveSession.findById(sessionId);
        if (session == null) {
            throw new NotFoundException("Session not found");
        }
        return Response.ok(java.util.Map.of(
                "sessionId", session.id,
                "joinCode", session.joinCode,
                "title", session.title,
                "sourceLanguage", session.sourceLanguage,
                "targetLanguages", session.targetLanguages,
                "status", session.status,
                "accessMode", session.accessMode
        )).build();
    }

    @GET
    @Path("/by-code/{joinCode}")
    @Operation(summary = "Get public session info by join code")
    public Response getByJoinCode(@PathParam("joinCode") String joinCode) {
        LiveSession session = LiveSession
                .find("joinCode", joinCode.trim().toUpperCase())
                .firstResult();

        if (session == null) {
            throw new NotFoundException("Session not found");
        }

        return Response.ok(java.util.Map.of(
                "sessionId", session.id,
                "joinCode", session.joinCode,
                "title", session.title,
                "sourceLanguage", session.sourceLanguage,
                "targetLanguages", session.targetLanguages,
                "status", session.status,
                "accessMode", session.accessMode,
                "currentParticipantCount", session.currentParticipantCount,
                "maxParticipants", session.maxParticipants
        )).build();
    }

    @POST
    @Path("/join")
    @Transactional
    @Operation(summary = "Join a session by join code")
    public Response joinByCode(JoinSessionRequest req) {
        if (req == null || req.joinCode == null || req.joinCode.isBlank()) {
            throw new BadRequestException("joinCode is required");
        }

        LiveSession session = LiveSession
                .find("joinCode", req.joinCode.trim().toUpperCase())
                .firstResult();

        if (session == null) {
            throw new NotFoundException("Session not found for join code: " + req.joinCode);
        }

        if (session.status == SessionStatus.ENDED
                || session.status == SessionStatus.EXPIRED
                || session.status == SessionStatus.FAILED) {
            throw new BadRequestException("Session is closed");
        }

        if (session.maxParticipants > 0
                && session.currentParticipantCount >= session.maxParticipants) {
            throw new BadRequestException("Session is full");
        }

        LiveParticipant participant = new LiveParticipant();
        participant.session = session;
        participant.anonymousName = (req.anonymousName != null && !req.anonymousName.isBlank())
                ? req.anonymousName
                : "Student-" + (session.currentParticipantCount + 1);
        participant.targetLanguage = req.targetLanguage != null ? req.targetLanguage : Language.EN;
        participant.audioEnabled = req.audioEnabled;
        participant.voiceCode = req.voiceCode;
        participant.connectionStatus = "CONNECTED";
        participant.lastHeartbeatAt = Instant.now();
        participant.persist();

        session.currentParticipantCount = session.currentParticipantCount + 1;
        session.lastActivityAt = Instant.now();

        return Response.status(Response.Status.CREATED)
                .entity(java.util.Map.of(
                        "participantId", participant.id,
                        "sessionId", session.id,
                        "joinCode", session.joinCode,
                        "title", session.title,
                        "sourceLanguage", session.sourceLanguage,
                        "targetLanguages", session.targetLanguages,
                        "status", session.status,
                        "targetLanguage", participant.targetLanguage,
                        "anonymousName", participant.anonymousName,
                        "audioEnabled", participant.audioEnabled
                ))
                .build();
    }

    private String generateJoinCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}
