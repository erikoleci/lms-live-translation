package com.tili.livetranslation.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tili.livetranslation.provider.TranslationProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Free, keyless translation via the MyMemory Translation API
 * (https://mymemory.translated.net) — good enough for testing the full
 * pipeline end-to-end before paying for OpenAI/Azure/DeepL.
 *
 * Rate limit: ~5000 words/day per client IP without an email/key. Fine for
 * dev; swap the provider_config priority once you're ready for a paid
 * provider — nothing else in the codebase needs to change.
 */
@ApplicationScoped
public class MyMemoryTranslationProvider implements TranslationProvider {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String code() {
        return "MYMEMORY_FREE";
    }

    @Override
    public CompletionStage<String> translate(String text, String sourceLang, String targetLang,
                                              Map<String, String> glossaryContext) {
        String encodedText = java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
        String langPair = sourceLang.toLowerCase() + "|" + targetLang.toLowerCase();
        String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + langPair;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("MyMemory returned HTTP " + response.statusCode());
                    }
                    try {
                        JsonNode root = mapper.readTree(response.body());
                        String translated = root.path("responseData").path("translatedText").asText(null);
                        if (translated == null || translated.isBlank()) {
                            throw new RuntimeException("MyMemory returned an empty translation");
                        }
                        return translated;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse MyMemory response", e);
                    }
                });
    }
}
