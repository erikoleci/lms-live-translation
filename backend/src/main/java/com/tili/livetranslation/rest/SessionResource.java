package com.tili.livetranslation.rest;

import com.tili.livetranslation.domain.LiveSession;
import com.tili.livetranslation.domain.SessionState;
import com.tili.livetranslation.dto.*;
import com.tili.livetranslation.service.SessionService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionResource {

    @Inject
    SessionService sessionService;

    @Inject
    SecurityIdentity identity;

    @POST
    @Transactional
    public Response create(@Valid CreateSessionRequest req) {
        String teacherId = currentTeacherId();
        LiveSession session = sessionService.createSession(teacherId, req);
        long count = sessionService.activeParticipantCount(session.id);
        return Response.status(Response.Status.CREATED)
                .entity(SessionResponse.from(session, count))
                .build();
    }

    @GET
    public List<SessionResponse> list(@QueryParam("courseId") String courseId,
                                       @QueryParam("status") String status) {
        String teacherId = currentTeacherId();
        String query = "teacherId = ?1";
        List<Object> params = new java.util.ArrayList<>(List.of(teacherId));
        int idx = 2;
        if (courseId != null) {
            query += " and courseId = ?" + idx++;
            params.add(courseId);
        }
        if (status != null) {
            query += " and status = ?" + idx++;
            params.add(SessionState.valueOf(status));
        }
        List<LiveSession> sessions = LiveSession.list(query, params.toArray());
        return sessions.stream()
                .map(s -> SessionResponse.from(s, sessionService.activeParticipantCount(s.id)))
                .toList();
    }

    @GET
    @Path("/{sessionId}")
    public SessionResponse get(@PathParam("sessionId") UUID sessionId) {
        LiveSession session = sessionService.getOrThrow(sessionId);
        return SessionResponse.from(session, sessionService.activeParticipantCount(sessionId));
    }

    @PATCH
    @Path("/{sessionId}")
    @Transactional
    public SessionResponse update(@PathParam("sessionId") UUID sessionId, SessionUpdateRequest req) {
        LiveSession session = sessionService.updateSession(sessionId, req);
        return SessionResponse.from(session, sessionService.activeParticipantCount(sessionId));
    }

    @DELETE
    @Path("/{sessionId}")
    @Transactional
    public Response delete(@PathParam("sessionId") UUID sessionId) {
        sessionService.deleteSession(sessionId);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{sessionId}/state")
    @Transactional
    public SessionResponse changeState(@PathParam("sessionId") UUID sessionId,
                                        @Valid SessionStateUpdateRequest req) {
        LiveSession session = sessionService.changeState(sessionId, SessionState.valueOf(req.state()));
        return SessionResponse.from(session, sessionService.activeParticipantCount(sessionId));
    }

    @GET
    @Path("/{sessionId}/join-info")
    public JoinInfoResponse joinInfo(@PathParam("sessionId") UUID sessionId) {
        LiveSession session = sessionService.getOrThrow(sessionId);
        return toJoinInfo(session);
    }

    /**
     * Public lookup used by the student "enter join code" screen: resolves a
     * short join code (e.g. "K7QX9P") to full join info without exposing the
     * internal session UUID until the student is ready to actually join.
     */
    @GET
    @Path("/by-code/{joinCode}")
    public JoinInfoResponse joinInfoByCode(@PathParam("joinCode") String joinCode) {
        LiveSession session = sessionService.getByJoinCodeOrThrow(joinCode.toUpperCase());
        return toJoinInfo(session);
    }

    private JoinInfoResponse toJoinInfo(LiveSession session) {
        long count = sessionService.activeParticipantCount(session.id);
        return new JoinInfoResponse(
                session.id, session.title, session.courseId, session.sourceLanguage,
                session.targetLanguageList(), session.status.name(), session.accessMode.name(),
                session.accessMode.name().equals("CLOSED"), count, session.maxParticipants
        );
    }

    /**
     * Resolves the calling teacher's identity, falling back to a stable
     * "dev-teacher" id when running without a real OIDC identity (dev/test
     * profile, see application.properties) instead of throwing on
     * identity.getPrincipal() == null.
     */
    private String currentTeacherId() {
        return identity.isAnonymous() ? "dev-teacher" : identity.getPrincipal().getName();
    }
}
