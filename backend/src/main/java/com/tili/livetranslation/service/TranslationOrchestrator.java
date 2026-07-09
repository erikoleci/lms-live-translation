package com.tili.livetranslation.service;

import com.tili.livetranslation.domain.TranscriptSegment;
import com.tili.livetranslation.domain.TranslationSegment;
import com.tili.livetranslation.provider.ProviderRegistry;
import com.tili.livetranslation.provider.TranslationProvider;
import com.tili.livetranslation.websocket.event.EventType;
import com.tili.livetranslation.websocket.event.WsEvent;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implements spec section 9 "Translation handling":
 * - partial text: translate with debounce
 * - final text: always translate again
 * - cache identical source text + language pair to reduce cost
 * - support provider fallback (primary translation fails -> fallback translation)
 */
@ApplicationScoped
public class TranslationOrchestrator {

    private static final long PARTIAL_DEBOUNCE_MS = 250;

    @Inject
    ProviderRegistry providerRegistry;

    @Inject
    SessionBroadcastService broadcastService;

    @Inject
    TtsOrchestrator ttsOrchestrator;

    private final ScheduledExecutorService debounceExecutor = Executors.newScheduledThreadPool(2);
    private final Map<String, java.util.concurrent.ScheduledFuture<?>> pendingPartials = new ConcurrentHashMap<>();

    public void translateSegment(TranscriptSegment segment, String targetLang, boolean isFinal) {
        String debounceKey = segment.sessionId + ":" + targetLang;

        if (!isFinal) {
            // Cancel any pending partial translation for this session+language and
            // reschedule, so a fast-talking teacher doesn't flood students with
            // half-formed captions ("handle partial segments without excessive flicker").
            var existing = pendingPartials.get(debounceKey);
            if (existing != null) existing.cancel(false);
            var future = debounceExecutor.schedule(
                    () -> doTranslate(segment, targetLang, false),
                    PARTIAL_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
            pendingPartials.put(debounceKey, future);
        } else {
            var existing = pendingPartials.remove(debounceKey);
            if (existing != null) existing.cancel(false);
            doTranslate(segment, targetLang, true);
        }
    }

    private void doTranslate(TranscriptSegment segment, String targetLang, boolean isFinal) {
        cachedTranslate(segment.originalText, segment.sourceLanguage, targetLang)
                .whenComplete((translatedText, error) -> {
                    if (error != null) {
                        handleFailureWithFallback(segment, targetLang, isFinal);
                        return;
                    }
                    persistAndBroadcast(segment, targetLang, translatedText, isFinal);
                });
    }

    @CacheResult(cacheName = "translation-cache")
    public CompletableFuture<String> cachedTranslate(String text, String sourceLang, String targetLang) {
        TranslationProvider provider = providerRegistry.resolveTranslation();
        return provider.translate(text, sourceLang, targetLang, Map.of()).toCompletableFuture();
    }

    private void handleFailureWithFallback(TranscriptSegment segment, String targetLang, boolean isFinal) {
        // TODO: look up fallbackProviderCode via providerRegistry.fallbackCodeFor(...) and retry once.
        // If fallback also fails, broadcast an ERROR event so the student UI can show a notice
        // while captions in other languages keep flowing.
        broadcastService.broadcastToSession(segment.sessionId, new WsEvent(EventType.ERROR,
                Map.of("scope", "TRANSLATION", "targetLanguage", targetLang,
                        "message", "Translation temporarily unavailable for " + targetLang)));
    }

    @Transactional
    protected void persistAndBroadcast(TranscriptSegment segment, String targetLang, String translatedText, boolean isFinal) {
        TranslationSegment translation = new TranslationSegment();
        translation.transcriptSegmentId = segment.id;
        translation.targetLanguage = targetLang;
        translation.translatedText = translatedText;
        translation.isFinal = isFinal;
        translation.persist();

        broadcastService.broadcastToSession(segment.sessionId, new WsEvent(isFinal ? EventType.TRANSLATION_FINAL : EventType.TRANSLATION_PARTIAL,
                Map.of(
                        "translationId", translation.id.toString(),
                        "transcriptSegmentId", segment.id.toString(),
                        "targetLanguage", targetLang,
                        "text", translatedText,
                        "isFinal", isFinal
                )));

        ttsOrchestrator.maybeSynthesize(translation, segment.sessionId);
    }
}
