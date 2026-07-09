package com.tili.livetranslation.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tili.livetranslation.domain.LiveSession;
import com.tili.livetranslation.service.SessionService;
import com.tili.livetranslation.service.TranscriptService;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * WS /ws/sessions/{sessionId}/teacher-text
 *
 * FREE-TESTING PATH: accepts already-recognized text from the teacher's
 * browser (Web Speech API, see the frontend's useBrowserStt.js) instead of
 * raw audio. This skips the backend STT provider entirely — useful for
 * testing the full transcript -> translation -> caption pipeline with zero
 * vendor cost/setup. Runs through the exact same TranscriptService as the
 * real STT path (TeacherAudioSocket), so translation/broadcast/persistence
 * behave identically either way.
 *
 * Expected message shape (JSON):
 *   {"text": "...", "isFinal": true, "sourceLanguage": "IT", "confidence": 0.9}
 *
 * sourceLanguage is optional; falls back to the session's configured
 * sourceLanguage if omitted.
 */
@WebSocket(path = "/ws/sessions/{sessionId}/teacher-text")
@ApplicationScoped
public class TeacherTextSocket {

    @Inject
    WebSocketConnection connection;

    @Inject
    TranscriptService transcriptService;

    @Inject
    SessionService sessionService;

    private final ObjectMapper mapper = new ObjectMapper();

    @OnTextMessage
    public void onMessage(String json) {
        UUID sessionId = UUID.fromString(connection.pathParam("sessionId"));
        LiveSession session = sessionService.getOrThrow(sessionId);

        try {
            JsonNode node = mapper.readTree(json);
            String text = node.path("text").asText("");
            if (text.isBlank()) return;
            boolean isFinal = node.path("isFinal").asBoolean(false);
            String sourceLanguage = node.hasNonNull("sourceLanguage")
                    ? node.get("sourceLanguage").asText()
                    : session.sourceLanguage;
            Float confidence = node.hasNonNull("confidence")
                    ? (float) node.get("confidence").asDouble()
                    : null;

            transcriptService.recordSegment(sessionId, sourceLanguage, text, isFinal, confidence, null, null);
        } catch (Exception e) {
            // Malformed message from the browser; ignore rather than tear down the socket.
        }
    }

    @OnClose
    public void onClose() {
        UUID sessionId = UUID.fromString(connection.pathParam("sessionId"));
        transcriptService.resetSequenceCounter(sessionId);
    }
}
