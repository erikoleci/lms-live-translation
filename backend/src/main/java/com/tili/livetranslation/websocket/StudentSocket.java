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

    // sessionId -> set of connections currently subscribed. We capture the actual
    // WebSocketConnection object here (during onOpen, while the WS context is valid)
    // instead of just an id, because broadcast() is called from plain REST-request
    // threads (e.g. teacher clicks Start/Pause/End) where there is no active
    // WebSocket connection context -- calling connection.getOpenConnections() from
    // there throws, since the injected `connection` field only resolves inside a
    // live WS callback (onOpen/onClose/onMessage) of that same connection.
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
            // Drop the map entry once a session has no more listeners, otherwise
            // sessionConnections grows forever across the app's lifetime.
            sessionConnections.computeIfPresent(sessionId, (id, remaining) -> remaining.isEmpty() ? null : remaining);
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
        Set<WebSocketConnection> conns = sessionConnections.get(sessionId);
        if (conns == null || conns.isEmpty()) return;
        String json = toJson(event);
        conns.forEach(c -> {
            if (c.isOpen()) c.sendTextAndAwait(json);
        });
    }

    private String toJson(WsEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize WebSocket event", e);
        }
    }
}
