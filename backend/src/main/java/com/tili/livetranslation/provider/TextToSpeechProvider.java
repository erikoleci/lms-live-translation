package com.tili.livetranslation.provider;

import com.tili.livetranslation.domain.enums.Language;

import java.util.UUID;

/**
 * TTS abstraction.
 * Supports streaming chunks where possible (OpenAI, Azure support streaming).
 */
public interface TextToSpeechProvider {

    /**
     * Synthesize full audio for text (for short segments).
     * Returns audio bytes + metadata.
     */
    AudioResult synthesize(String text, Language language, String voiceCode);

    /**
     * Stream synthesis (preferred for low latency). Callback receives audio chunks.
     */
    UUID stream(String text, Language language, String voiceCode, AudioChunkConsumer consumer);

    record AudioResult(byte[] audioData, String format, long durationMs) {}

    @FunctionalInterface
    interface AudioChunkConsumer {
        void accept(byte[] chunk, boolean isFinalChunk);
    }
}
