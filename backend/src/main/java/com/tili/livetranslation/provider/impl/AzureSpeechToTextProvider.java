package com.tili.livetranslation.provider.impl;

import com.tili.livetranslation.provider.SpeechToTextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;
import java.util.function.Consumer;

/** STT provider backed by Azure Cognitive Services Speech-to-Text. Fallback for OpenAI. */
@ApplicationScoped
public class AzureSpeechToTextProvider implements SpeechToTextProvider {

    // NOTE: apiKey/region will be re-added via @ConfigProperty once the real
    // Azure Speech SDK wiring below is implemented (see TODOs). Left out for
    // now since an unused, eagerly-injected @ConfigProperty caused startup
    // config-resolution failures in some container environments.

    @Override
    public String code() {
        return "AZURE_SPEECH";
    }

    @Override
    public SttSessionRef startSession(SttSessionConfig config) {
        // TODO: open Azure Speech SDK streaming recognizer for config.sourceLanguage().
        return new SttSessionRef("azure-" + UUID.randomUUID());
    }

    @Override
    public void sendAudioChunk(SttSessionRef sessionRef, byte[] chunk) {
        // TODO: push audio into the Azure push stream for sessionRef.
    }

    @Override
    public void onPartialTranscript(SttSessionRef sessionRef, Consumer<TranscriptEvent> callback) {
        // TODO: wire recognizer.recognizing event.
    }

    @Override
    public void onFinalTranscript(SttSessionRef sessionRef, Consumer<TranscriptEvent> callback) {
        // TODO: wire recognizer.recognized event.
    }

    @Override
    public void closeSession(SttSessionRef sessionRef) {
        // TODO: stop/close recognizer.
    }
}
