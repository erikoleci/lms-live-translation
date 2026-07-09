package com.tili.livetranslation.provider.impl;

import com.tili.livetranslation.provider.TextToSpeechProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@ApplicationScoped
public class OpenAiTextToSpeechProvider implements TextToSpeechProvider {

    @ConfigProperty(name = "zana.provider.openai.api-key")
    String apiKey;

    @Override
    public String code() {
        return "OPENAI_TTS";
    }

    @Override
    public CompletionStage<byte[]> synthesize(String text, String language, String voice, String format) {
        // TODO: call OpenAI TTS endpoint and return the encoded audio bytes.
        return CompletableFuture.completedFuture(new byte[0]);
    }

    @Override
    public Flow.Publisher<byte[]> stream(String text, String language, String voice) {
        // TODO: stream audio chunks as they're generated instead of buffering the full clip.
        SubmissionPublisher<byte[]> publisher = new SubmissionPublisher<>();
        publisher.close();
        return publisher;
    }
}
