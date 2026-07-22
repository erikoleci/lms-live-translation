package com.tili.livetranslation.provider.impl;

import com.tili.livetranslation.provider.TranslationProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class AzureTranslationProvider implements TranslationProvider {

    @ConfigProperty(name = "zana.provider.azure.api-key", defaultValue = "")
    String apiKey;

    @Override
    public String code() {
        return "AZURE_TRANSLATOR";
    }

    @Override
    public CompletionStage<String> translate(String text, String sourceLang, String targetLang,
                                              Map<String, String> glossaryContext) {
        // TODO: call Azure Translator REST API.
        return CompletableFuture.completedFuture(text); // placeholder passthrough
    }
}
