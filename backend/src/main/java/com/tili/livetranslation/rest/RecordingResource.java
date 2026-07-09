package com.tili.livetranslation.rest;

import com.tili.livetranslation.domain.AudioRecording;
import com.tili.livetranslation.domain.LiveSession;
import com.tili.livetranslation.dto.RecordingUpdateRequest;
import com.tili.livetranslation.exception.ForbiddenException;
import com.tili.livetranslation.exception.NotFoundException;
import com.tili.livetranslation.service.SessionService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.UUID;

/**
 * Spec 4.2: recording is optional, off by default, must never be exposed to
 * students unless explicitly allowed, and follows the configured retention policy.
 */
@Path("/api/sessions/{sessionId}/recording")
public class RecordingResource {

    @Inject
    SessionService sessionService;

    @Inject
    SecurityIdentity identity;

    @GET
    public Response download(@PathParam("sessionId") UUID sessionId) {
        LiveSession session = sessionService.getOrThrow(sessionId);
        requireTeacherOrAdmin(session);

        AudioRecording recording = AudioRecording.findBySession(sessionId);
        if (recording == null || recording.deletedAt != null) {
            throw new NotFoundException("No recording available for this session.");
        }
        // TODO: stream the file from object storage instead of a stub redirect.
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("Recording storage streaming not yet wired to an object store backend.")
                .build();
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateRetention(@PathParam("sessionId") UUID sessionId, RecordingUpdateRequest req) {
        LiveSession session = sessionService.getOrThrow(sessionId);
        requireTeacherOrAdmin(session);

        AudioRecording recording = AudioRecording.findBySession(sessionId);
        if (recording == null) {
            throw new NotFoundException("No recording available for this session.");
        }
        recording.retentionUntil = req.retentionUntil();
        return Response.ok().build();
    }

    @DELETE
    @Transactional
    public Response delete(@PathParam("sessionId") UUID sessionId) {
        LiveSession session = sessionService.getOrThrow(sessionId);
        requireTeacherOrAdmin(session);

        AudioRecording recording = AudioRecording.findBySession(sessionId);
        if (recording != null) {
            recording.deletedAt = Instant.now();
            // TODO: also delete the underlying object-storage file.
        }
        return Response.noContent().build();
    }

    private void requireTeacherOrAdmin(LiveSession session) {
        if (identity.isAnonymous() || !identity.getPrincipal().getName().equals(session.teacherId)) {
            throw new ForbiddenException("Recordings are only accessible to the owning teacher/admin.");
        }
    }
}
