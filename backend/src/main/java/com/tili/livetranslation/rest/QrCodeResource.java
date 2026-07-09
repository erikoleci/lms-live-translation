package com.tili.livetranslation.rest;

import com.tili.livetranslation.domain.LiveSession;
import com.tili.livetranslation.service.QrCodeService;
import com.tili.livetranslation.service.SessionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

@Path("/api/sessions/{sessionId}/qr-code")
public class QrCodeResource {

    @Inject
    QrCodeService qrCodeService;

    @Inject
    SessionService sessionService;

    /** Returns the raw QR PNG image, ready to be shown or projected by the teacher. */
    @GET
    @Produces("image/png")
    public Response getQrImage(@PathParam("sessionId") UUID sessionId) {
        LiveSession session = sessionService.getOrThrow(sessionId);
        byte[] png = qrCodeService.generateQrPng(session.joinCode);
        return Response.ok(png).build();
    }

    /** Returns the join payload (URL + code) as JSON, for clients that render their own QR widget. */
    @GET
    @Path("/payload")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getQrPayload(@PathParam("sessionId") UUID sessionId) {
        LiveSession session = sessionService.getOrThrow(sessionId);
        return Map.of(
                "joinCode", session.joinCode,
                "joinUrl", qrCodeService.buildJoinUrl(session.joinCode)
        );
    }
}
