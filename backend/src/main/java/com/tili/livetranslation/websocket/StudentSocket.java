package com.tili.livetranslation.websocket;

import com.tili.livetranslation.websocket.event.WsEvent;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WS /ws/sessions/{sessionId}/student
 *
 * Broadcasts caption/translation/TTS/status events to every student connected
 * to a given session (spec 6.2). Fan-out for a single node; see
 * SessionBroadcastService for the note on scaling this via Redis pub/sub.
 */
@WebSocket(path = "/ws/sessions/{sessionId}/student")
@ApplicationScoped
public class StudentSocket {

    @Inject
    WebSocketConnection connection;

    // sessionId -> set of connection ids currently subscribed
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
        if (ids != null) {
            ids.remove(connection.id());
        }
    }

    @OnTextMessage
    public void onMessage(String message) {
        // Students only receive events on this channel; inbound messages are
        // limited to client-side heartbeats/acks and are intentionally ignored.
    }

    @Inject
    ObjectMapper objectMapper;

    public void broadcast(UUID sessionId, WsEvent event) {
        Set<String> ids = sessionConnections.get(sessionId);
        if (ids == null || ids.isEmpty()) return;
        String json = toJson(event);
        connection.getOpenConnections().stream()
                .filter(c -> ids.contains(c.id()))
                .forEach(c -> c.sendTextAndAwait(json));
    }

    private String toJson(WsEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize WebSocket event", e);
        }
    }
}
