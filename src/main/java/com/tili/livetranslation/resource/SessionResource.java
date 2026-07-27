package com.tili.livetranslation.resource;

import com.tili.livetranslation.domain.entities.LiveSession;
import com.tili.livetranslation.domain.enums.AccessMode;
import com.tili.livetranslation.domain.enums.Language;
import com.tili.livetranslation.domain.enums.SessionStatus;
import com.tili.livetranslation.dto.CreateSessionRequest;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
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
        session.recordingEnabled = req.recordingEnabled;
        session.studentTranscriptDownloadEnabled = req.studentTranscriptDownloadEnabled;
        session.maxParticipants = req.maxParticipants != null ? req.maxParticipants : 300;

        if (req.targetLanguages != null) {
            session.targetLanguages.addAll(req.targetLanguages);
        } else {
            for (Language l : Language.values()) {
                if (l != session.sourceLanguage) session.targetLanguages.add(l);
            }
        }

        session.status = SessionStatus.CREATED;
        session.joinCode = generateJoinCode();

        session.persist();

        return Response.status(Response.Status.CREATED).entity(session).build();
    }

    private String generateJoinCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @GET
    public List<LiveSession> listSessions(@QueryParam("status") SessionStatus status) {
        if (status != null) {
            return LiveSession.list("status", Sort.descending("createdAt"), status);
        }
        return LiveSession.listAll(Sort.descending("createdAt"));
    }

    private LiveSession getSession(UUID sessionId) {
    LiveSession session = LiveSession.findById(sessionId);
    if (session == null) {
        throw new NotFoundException("Session not found");
    }
    return session;
}

    @PATCH
    @Path("/{sessionId}")
    @Transactional
    public LiveSession updateSession(@PathParam("sessionId") UUID sessionId, LiveSession update) {
        LiveSession session = getSession(sessionId);
        if (update.title != null) session.title = update.title;
        if (update.accessMode != null) session.accessMode = update.accessMode;
        if (update.recordingEnabled != session.recordingEnabled) session.recordingEnabled = update.recordingEnabled;
        return session;
    }

    @PATCH
    @Path("/{sessionId}/state")
    @Transactional
    public LiveSession changeState(@PathParam("sessionId") UUID sessionId, 
                                   @QueryParam("state") SessionStatus newState) {
        LiveSession session = getSession(sessionId);
        SessionStatus current = session.status;

        if (newState == SessionStatus.ACTIVE && current == SessionStatus.CREATED) {
            session.status = SessionStatus.WAITING;
            session.startedAt = Instant.now();
        } else if (newState == SessionStatus.ACTIVE && current == SessionStatus.WAITING) {
            session.status = SessionStatus.ACTIVE;
        } else if (newState == SessionStatus.PAUSED && current == SessionStatus.ACTIVE) {
            session.status = SessionStatus.PAUSED;
        } else if (newState == SessionStatus.ACTIVE && current == SessionStatus.PAUSED) {
            session.status = SessionStatus.ACTIVE;
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
    public Response getJoinInfo(@PathParam("sessionId") UUID sessionId) {
        LiveSession session = getSession(sessionId);
        return Response.ok()
                .entity(java.util.Map.of(
                    "sessionId", session.id,
                    "joinCode", session.joinCode,
                    "title", session.title,
                    "sourceLanguage", session.sourceLanguage,
                    "targetLanguages", session.targetLanguages,
                    "status", session.status,
                    "accessMode", session.accessMode
                ))
                .build();
    }
}