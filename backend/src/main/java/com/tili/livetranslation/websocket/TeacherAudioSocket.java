package com.tili.livetranslation.websocket;

import com.tili.livetranslation.domain.LiveSession;
import com.tili.livetranslation.provider.ProviderRegistry;
import com.tili.livetranslation.provider.SpeechToTextProvider;
import com.tili.livetranslation.provider.SpeechToTextProvider.SttSessionConfig;
import com.tili.livetranslation.provider.SpeechToTextProvider.SttSessionRef;
import com.tili.livetranslation.service.SessionService;
import com.tili.livetranslation.service.TranscriptService;
import io.quarkus.websockets.next.OnBinaryMessage;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WS /ws/sessions/{sessionId}/teacher-audio
 *
 * Receives small (100-500ms) audio chunks from the teacher's browser
 * (spec section 9 "Realtime Processing Logic"), forwards them to the
 * currently-resolved STT provider, and wires the provider's partial/final
 * transcript callbacks into TranscriptService, which handles persistence,
 * broadcast, and downstream translation.
 *
 * One provider streaming session is opened per teacher-audio WebSocket
 * connection and torn down on close (or on session PAUSE/END, handled by
 * SessionService triggering a close through this bridge -- wiring point
 * left as a TODO since it requires a connection registry keyed by sessionId,
 * matching the one already used for broadcast in StudentSocket).
 */
@WebSocket(path = "/ws/sessions/{sessionId}/teacher-audio")
@ApplicationScoped
public class TeacherAudioSocket {

    @Inject
    WebSocketConnection connection;

    @Inject
    ProviderRegistry providerRegistry;

    @Inject
    TranscriptService transcriptService;

    @Inject
    SessionService sessionService;

    /** Tracks the open provider-side STT session per WebSocket connection id. */
    private final Map<String, SttSessionRef> activeSttSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen() {
        UUID sessionId = UUID.fromString(connection.pathParam("sessionId"));
        LiveSession session = sessionService.getOrThrow(sessionId);

        SpeechToTextProvider stt = providerRegistry.resolveStt();
        SttSessionRef ref = stt.startSession(new SttSessionConfig(
                sessionId,
                session.sourceLanguage,
                session.autoDetectSource,
                "OPUS_WEBM",
                48000
        ));
        activeSttSessions.put(connection.id(), ref);

        stt.onPartialTranscript(ref, event -> transcriptService.recordSegment(
                sessionId, resolveLanguage(session, event.detectedLanguage()), event.text(),
                false, event.confidence(), event.startOffsetMs(), event.endOffsetMs()));

        stt.onFinalTranscript(ref, event -> transcriptService.recordSegment(
                sessionId, resolveLanguage(session, event.detectedLanguage()), event.text(),
                true, event.confidence(), event.startOffsetMs(), event.endOffsetMs()));
    }

    @OnBinaryMessage
    public void onAudioChunk(Buffer chunk) {
        SttSessionRef ref = activeSttSessions.get(connection.id());
        if (ref == null) return; // connection not fully initialized yet, or already closed
        providerRegistry.resolveStt().sendAudioChunk(ref, chunk.getBytes());
    }

    @OnClose
    public void onClose() {
        SttSessionRef ref = activeSttSessions.remove(connection.id());
        if (ref != null) {
            providerRegistry.resolveStt().closeSession(ref);
        }
        UUID sessionId = UUID.fromString(connection.pathParam("sessionId"));
        transcriptService.resetSequenceCounter(sessionId);
    }

    private String resolveLanguage(LiveSession session, String detectedLanguage) {
        return session.autoDetectSource && detectedLanguage != null ? detectedLanguage : session.sourceLanguage;
    }
}
