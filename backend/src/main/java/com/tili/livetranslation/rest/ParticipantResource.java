package com.tili.livetranslation.rest;

import com.tili.livetranslation.domain.LiveParticipant;
import com.tili.livetranslation.dto.JoinRequest;
import com.tili.livetranslation.dto.ParticipantResponse;
import com.tili.livetranslation.dto.ParticipantUpdateRequest;
import com.tili.livetranslation.service.ParticipantService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/sessions/{sessionId}/participants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ParticipantResource {

    @Inject
    ParticipantService participantService;

    @Inject
    SecurityIdentity identity;

    @POST
    @Transactional
    public Response join(@PathParam("sessionId") UUID sessionId, @Valid JoinRequest req) {
        String authenticatedUserId = identity.isAnonymous() ? null : identity.getPrincipal().getName();
        LiveParticipant participant = participantService.join(sessionId, authenticatedUserId, req);
        return Response.status(Response.Status.CREATED)
                .entity(ParticipantResponse.from(participant))
                .build();
    }

    @GET
    public List<ParticipantResponse> list(@PathParam("sessionId") UUID sessionId) {
        return participantService.list(sessionId).stream().map(ParticipantResponse::from).toList();
    }

    @GET
    @Path("/{participantId}")
    public ParticipantResponse get(@PathParam("sessionId") UUID sessionId,
                                    @PathParam("participantId") UUID participantId) {
        return ParticipantResponse.from(participantService.getOrThrow(sessionId, participantId));
    }

    @PATCH
    @Transactional
    @Path("/{participantId}")
    public ParticipantResponse update(@PathParam("sessionId") UUID sessionId,
                                       @PathParam("participantId") UUID participantId,
                                       ParticipantUpdateRequest req) {
        return ParticipantResponse.from(participantService.updatePreferences(sessionId, participantId, req));
    }

    @DELETE
    @Transactional
    @Path("/{participantId}")
    public Response leave(@PathParam("sessionId") UUID sessionId,
                           @PathParam("participantId") UUID participantId) {
        participantService.leave(sessionId, participantId);
        return Response.noContent().build();
    }
}
