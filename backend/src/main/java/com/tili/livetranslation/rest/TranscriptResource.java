package com.tili.livetranslation.rest;

import com.tili.livetranslation.domain.LiveSession;
import com.tili.livetranslation.domain.TranscriptSegment;
import com.tili.livetranslation.domain.TranslationSegment;
import com.tili.livetranslation.dto.TranscriptSegmentDto;
import com.tili.livetranslation.dto.TranslationSegmentDto;
import com.tili.livetranslation.service.SessionService;
import com.tili.livetranslation.service.TranscriptService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/sessions/{sessionId}")
@Produces(MediaType.APPLICATION_JSON)
public class TranscriptResource {

    @Inject
    TranscriptService transcriptService;

    @Inject
    SessionService sessionService;

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("/transcript")
    public List<TranscriptSegmentDto> getTranscript(@PathParam("sessionId") UUID sessionId) {
        boolean allowed = isTranscriptAccessAllowed(sessionId);
        return transcriptService.listForSession(sessionId, allowed).stream()
                .map(TranscriptSegmentDto::from)
                .toList();
    }

    @GET
    @Path("/transcript/segments")
    public List<TranscriptSegmentDto> getSegments(@PathParam("sessionId") UUID sessionId) {
        boolean allowed = isTranscriptAccessAllowed(sessionId);
        return transcriptService.listForSession(sessionId, allowed).stream()
                .map(TranscriptSegmentDto::from)
                .toList();
    }

    @GET
    @Path("/translations")
    public List<TranslationSegmentDto> getTranslations(@PathParam("sessionId") UUID sessionId,
                                                         @QueryParam("language") String language) {
        boolean allowed = isTranscriptAccessAllowed(sessionId);
        List<TranscriptSegment> segments = transcriptService.listForSession(sessionId, allowed);
        return segments.stream()
                .flatMap(seg -> TranslationSegment.findByTranscriptSegment(seg.id).stream())
                .filter(t -> language == null || t.targetLanguage.equalsIgnoreCase(language))
                .map(TranslationSegmentDto::from)
                .toList();
    }

    /**
     * Transcript/translation export is gated by studentTranscriptDownloadEnabled
     * (spec 4.1 session settings + 11 "allow teacher/admin to disable transcript export"),
     * unless the requester is the owning teacher.
     */
    private boolean isTranscriptAccessAllowed(UUID sessionId) {
        LiveSession session = sessionService.getOrThrow(sessionId);
        boolean isOwningTeacher = !identity.isAnonymous()
                && identity.getPrincipal().getName().equals(session.teacherId);
        return isOwningTeacher || session.studentTranscriptDownloadEnabled;
    }
}
