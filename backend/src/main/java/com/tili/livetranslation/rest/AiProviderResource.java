package com.tili.livetranslation.rest;

import com.tili.livetranslation.dto.ProviderConfigRequest;
import com.tili.livetranslation.dto.ProviderConfigResponse;
import com.tili.livetranslation.service.ProviderConfigService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/** Admin-only endpoints for configuring STT/Translation/TTS providers (spec 6.1, 12). */
@Path("/api/ai-providers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
public class AiProviderResource {

    @Inject
    ProviderConfigService providerConfigService;

    @POST
    @Transactional
    public Response create(@Valid ProviderConfigRequest req) {
        var created = providerConfigService.create(req);
        return Response.status(Response.Status.CREATED)
                .entity(ProviderConfigResponse.from(created))
                .build();
    }

    @GET
    public List<ProviderConfigResponse> list() {
        return providerConfigService.listAll().stream().map(ProviderConfigResponse::from).toList();
    }

    @GET
    @Path("/{providerId}")
    public ProviderConfigResponse get(@PathParam("providerId") UUID providerId) {
        return ProviderConfigResponse.from(providerConfigService.getOrThrow(providerId));
    }

    @PATCH
    @Transactional
    @Path("/{providerId}")
    public ProviderConfigResponse update(@PathParam("providerId") UUID providerId, ProviderConfigRequest req) {
        return ProviderConfigResponse.from(providerConfigService.update(providerId, req));
    }

    @DELETE
    @Transactional
    @Path("/{providerId}")
    public Response disable(@PathParam("providerId") UUID providerId) {
        providerConfigService.disable(providerId);
        return Response.noContent().build();
    }
}
