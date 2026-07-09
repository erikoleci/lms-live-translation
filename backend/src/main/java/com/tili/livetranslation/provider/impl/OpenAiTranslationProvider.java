package com.tili.livetranslation.provider.impl;

import com.tili.livetranslation.provider.TranslationProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class OpenAiTranslationProvider implements TranslationProvider {

    @ConfigProperty(name = "zana.provider.openai.api-key")
    String apiKey;

    @Override
    public String code() {
        return "OPENAI_TRANSLATE";
    }

    @Override
    public CompletionStage<String> translate(String text, String sourceLang, String targetLang,
                                              Map<String, String> glossaryContext) {
        // TODO: call OpenAI chat/completions with a translation-only system prompt,
        // injecting glossaryContext as terminology hints ("academic terminology"
        // requirement, spec 4.4). Cache identical (text, targetLang) pairs upstream
        // in TranslationOrchestrator to reduce cost, per spec section 9.
        return CompletableFuture.completedFuture(text); // placeholder passthrough
    }
}
