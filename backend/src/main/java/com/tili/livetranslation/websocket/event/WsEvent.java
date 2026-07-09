package com.tili.livetranslation.websocket.event;

import java.util.Map;

/**
 * Envelope for every message sent down a WebSocket channel.
 *
 * Wire shape is intentionally {"event": "...", "payload": {...}} to match the
 * Zana Vue frontend's useWebSocket.js composable, which does:
 *   const msg = JSON.parse(e.data); emit(msg.event, msg.payload)
 */
public record WsEvent(EventType event, Map<String, Object> payload) {
}
