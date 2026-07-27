package com.tili.livetranslation.provider;

import com.tili.livetranslation.domain.enums.Language;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Abstraction for STT providers (OpenAI Realtime, Azure, Whisper, etc.)
 * Backend coordinates audio chunks from teacher WS and calls this.
 */
public interface SpeechToTextProvider {

    /**
     * Start a new transcription session for a live session.
     */
    UUID startSession(Language sourceLanguage, String sessionRef);

    /**
     * Send audio chunk (PCM16, Opus, or provider-expected format).
     * Chunk size typically 100-500ms.
     */
    void sendAudioChunk(UUID sessionRef, byte[] audioChunk, int offset, int length);

    /**
     * Register callback for partial transcripts (for low latency captions).
     */
    void onPartialTranscript(UUID sessionRef, Consumer<TranscriptEvent> callback);

    /**
     * Register callback for final transcripts (more accurate, replace partial).
     */
    void onFinalTranscript(UUID sessionRef, Consumer<TranscriptEvent> callback);

    /**
     * Close the STT session and release resources.
     */
    void closeSession(UUID sessionRef);

    record TranscriptEvent(
        String text,
        boolean isFinal,
        Double confidence,
        Long startMs,
        Long endMs,
        Language detectedLanguage // if auto-detect supported
    ) {}
}
