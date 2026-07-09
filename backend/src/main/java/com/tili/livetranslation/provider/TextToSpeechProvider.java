package com.tili.livetranslation.provider;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * Abstraction over any TTS vendor (OpenAI TTS, Azure Neural TTS, AWS Polly,
 * Google TTS, or a local Piper/Coqui worker).
 */
public interface TextToSpeechProvider {

    String code();

    /** Synthesizes the full audio for a segment and returns the encoded bytes. */
    CompletionStage<byte[]> synthesize(String text, String language, String voice, String format);

    /** Streams audio chunks as they become available (lower latency than synthesize()). */
    Flow.Publisher<byte[]> stream(String text, String language, String voice);
}
