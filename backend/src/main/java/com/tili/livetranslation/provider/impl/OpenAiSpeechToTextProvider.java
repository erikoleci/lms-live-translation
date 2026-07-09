package com.tili.livetranslation.provider.impl;

import com.tili.livetranslation.provider.SpeechToTextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * STT provider backed by OpenAI's realtime transcription API.
 *
 * NOTE: This is an MVP-shaped adapter skeleton. The actual realtime websocket
 * plumbing to platform.openai.com is intentionally left as a TODO wired point
 * so the rest of the pipeline (SessionAudioBridge, TranscriptService,
 * broadcasting) can be built and tested against it independently of any
 * specific vendor SDK version.
 */
@ApplicationScoped
public class OpenAiSpeechToTextProvider implements SpeechToTextProvider {

    @ConfigProperty(name = "zana.provider.openai.api-key")
    String apiKey;

    private final Map<String, Consumer<TranscriptEvent>> partialCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Consumer<TranscriptEvent>> finalCallbacks = new ConcurrentHashMap<>();

    @Override
    public String code() {
        return "OPENAI_REALTIME";
    }

    @Override
    public SttSessionRef startSession(SttSessionConfig config) {
        // TODO: open a websocket to OpenAI's realtime transcription endpoint using
        // an ephemeral/short-lived token (never the raw apiKey) scoped to this session.
        String providerSessionId = "openai-" + UUID.randomUUID();
        return new SttSessionRef(providerSessionId);
    }

    @Override
    public void sendAudioChunk(SttSessionRef sessionRef, byte[] chunk) {
        // TODO: forward chunk over the open websocket connection for sessionRef.
    }

    @Override
    public void onPartialTranscript(SttSessionRef sessionRef, Consumer<TranscriptEvent> callback) {
        partialCallbacks.put(sessionRef.providerSessionId(), callback);
    }

    @Override
    public void onFinalTranscript(SttSessionRef sessionRef, Consumer<TranscriptEvent> callback) {
        finalCallbacks.put(sessionRef.providerSessionId(), callback);
    }

    @Override
    public void closeSession(SttSessionRef sessionRef) {
        partialCallbacks.remove(sessionRef.providerSessionId());
        finalCallbacks.remove(sessionRef.providerSessionId());
        // TODO: close the underlying websocket connection.
    }
}
