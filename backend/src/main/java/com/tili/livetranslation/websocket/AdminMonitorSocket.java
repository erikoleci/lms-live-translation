package com.tili.livetranslation.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tili.livetranslation.websocket.event.WsEvent;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WS /ws/admin/live-sessions/{sessionId}
 *
 * Read-only monitoring channel for admins: mirrors every event also sent to
 * students, plus provider fallback/error events, so ops staff can watch a
 * session live without joining as a participant.
 */
@WebSocket(path = "/ws/admin/live-sessions/{sessionId}")
@ApplicationScoped
public class AdminMonitorSocket {

    @Inject
    WebSocketConnection connection;

    @Inject
    ObjectMapper objectMapper;

    // See StudentSocket for why we store actual WebSocketConnection objects
    // (captured during onOpen, while the WS context is valid) rather than ids
    // resolved later via connection.getOpenConnections() -- broadcast() is
    // called from plain REST-request threads with no active WS context.
    private final Map<UUID, Set<WebSocketConnection>> sessionConnections = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen() {
        UUID sessionId = UUID.fromString(connection.pathParam("sessionId"));
        sessionConnections
                .computeIfAbsent(sessionId, id -> new CopyOnWriteArraySet<>())
                .add(connection);
    }

    @OnClose
    public void onClose() {
        UUID sessionId = UUID.fromString(connection.pathParam("sessionId"));
        Set<WebSocketConnection> conns = sessionConnections.get(sessionId);
        if (conns != null) {
            conns.remove(connection);
            sessionConnections.computeIfPresent(sessionId, (id, remaining) -> remaining.isEmpty() ? null : remaining);
        }
    }

    public void broadcast(UUID sessionId, WsEvent event) {
        Set<WebSocketConnection> conns = sessionConnections.get(sessionId);
        if (conns == null || conns.isEmpty()) return;
        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize admin monitor event", e);
        }
        conns.forEach(c -> {
            if (c.isOpen()) c.sendTextAndAwait(json);
        });
    }
}
