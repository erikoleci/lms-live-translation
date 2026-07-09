package com.tili.livetranslation.service;

import com.tili.livetranslation.websocket.StudentSocket;
import com.tili.livetranslation.websocket.AdminMonitorSocket;
import com.tili.livetranslation.websocket.event.WsEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Central fan-out point: any service that mutates session/transcript/translation
 * state calls here instead of talking to WebSocket connections directly.
 *
 * In a single-node deployment this pushes straight to in-memory connections
 * (see StudentSocket/AdminMonitorSocket). In a horizontally-scaled deployment
 * (spec 5.2, "Redis for session pub/sub"), this should instead publish to a
 * Redis channel keyed by sessionId, and each node's WebSocket layer subscribes
 * to the channels for the sessions it holds connections for.
 */
@ApplicationScoped
public class SessionBroadcastService {

    @Inject
    StudentSocket studentSocket;

    @Inject
    AdminMonitorSocket adminMonitorSocket;

    public void broadcastToSession(UUID sessionId, WsEvent event) {
        studentSocket.broadcast(sessionId, event);
        adminMonitorSocket.broadcast(sessionId, event);
    }
}
