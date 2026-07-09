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

    private final Map<UUID, Set<String>> sessionConnections = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen() {
        UUID sessionId = UUID.fromString(connection.pathParam("sessionId"));
        sessionConnections
                .computeIfAbsent(sessionId, id -> new CopyOnWriteArraySet<>())
                .add(connection.id());
    }

    @OnClose
    public void onClose() {
        UUID sessionId = UUID.fromString(connection.pathParam("sessionId"));
        Set<String> ids = sessionConnections.get(sessionId);
        if (ids != null) ids.remove(connection.id());
    }

    public void broadcast(UUID sessionId, WsEvent event) {
        Set<String> ids = sessionConnections.get(sessionId);
        if (ids == null || ids.isEmpty()) return;
        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize admin monitor event", e);
        }
        connection.getOpenConnections().stream()
                .filter(c -> ids.contains(c.id()))
                .forEach(c -> c.sendTextAndAwait(json));
    }
}
