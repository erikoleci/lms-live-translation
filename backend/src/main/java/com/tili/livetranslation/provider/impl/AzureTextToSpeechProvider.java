package com.tili.livetranslation.provider.impl;

import com.tili.livetranslation.provider.TextToSpeechProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@ApplicationScoped
public class AzureTextToSpeechProvider implements TextToSpeechProvider {

    // NOTE: apiKey will be re-added via @ConfigProperty once the real Azure
    // Neural TTS wiring below is implemented (see TODOs).

    @Override
    public String code() {
        return "AZURE_NEURAL_TTS";
    }

    @Override
    public CompletionStage<byte[]> synthesize(String text, String language, String voice, String format) {
        // TODO: call Azure Neural TTS REST/SDK endpoint.
        return CompletableFuture.completedFuture(new byte[0]);
    }

    @Override
    public Flow.Publisher<byte[]> stream(String text, String language, String voice) {
        SubmissionPublisher<byte[]> publisher = new SubmissionPublisher<>();
        publisher.close();
        return publisher;
    }
}
