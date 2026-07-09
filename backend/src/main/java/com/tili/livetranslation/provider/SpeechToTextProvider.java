package com.tili.livetranslation.provider;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Abstraction over any STT vendor (OpenAI Realtime, Azure Speech, AWS Transcribe,
 * Google STT, or a local whisper.cpp/faster-whisper worker).
 *
 * One instance is created per live session's audio stream (see SessionAudioBridge).
 */
public interface SpeechToTextProvider {

    /** Unique code matching provider_config.provider_code, e.g. "OPENAI_REALTIME". */
    String code();

    /**
     * Opens a streaming STT session with the given provider.
     *
     * @param config source language, sample rate, encoding, etc.
     * @return an opaque session reference passed back into sendAudioChunk/closeSession.
     */
    SttSessionRef startSession(SttSessionConfig config);

    /** Forward one small audio chunk (100-500ms as per spec section 9). */
    void sendAudioChunk(SttSessionRef sessionRef, byte[] chunk);

    /** Register callback invoked whenever the provider emits a partial (unstable) transcript. */
    void onPartialTranscript(SttSessionRef sessionRef, Consumer<TranscriptEvent> callback);

    /** Register callback invoked whenever the provider emits a finalized transcript segment. */
    void onFinalTranscript(SttSessionRef sessionRef, Consumer<TranscriptEvent> callback);

    /** Gracefully tears down the provider-side session (e.g. on pause/end). */
    void closeSession(SttSessionRef sessionRef);

    record SttSessionConfig(
            UUID liveSessionId,
            String sourceLanguage, // null/"" if autoDetect = true
            boolean autoDetect,
            String audioFormat,    // "OPUS_WEBM" | "PCM16"
            int sampleRateHz
    ) {}

    record SttSessionRef(String providerSessionId) {}

    record TranscriptEvent(
            String text,
            String detectedLanguage,
            Float confidence,
            long startOffsetMs,
            long endOffsetMs,
            boolean isFinal
    ) {}
}
