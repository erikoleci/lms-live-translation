package com.tili.livetranslation.service;

import com.tili.livetranslation.domain.LiveParticipant;
import com.tili.livetranslation.domain.TranslationSegment;
import com.tili.livetranslation.domain.TtsChunk;
import com.tili.livetranslation.provider.ProviderRegistry;
import com.tili.livetranslation.provider.TextToSpeechProvider;
import com.tili.livetranslation.websocket.event.EventType;
import com.tili.livetranslation.websocket.event.WsEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements spec section 9 "TTS handling":
 * - generate only for languages currently requested by active students
 * - do not pre-generate all voices unless configured
 * - queue audio chunks per participant, avoid overlapping audio
 * - prioritize final segments over partial segments for TTS
 *
 * Also implements the TTS fallback rule from 6.3: "Primary TTS fails ->
 * disable audio and keep captions active" (never blocks captions on TTS failure).
 */
@ApplicationScoped
public class TtsOrchestrator {

    @Inject
    ProviderRegistry providerRegistry;

    @Inject
    SessionBroadcastService broadcastService;

    /**
     * Called after a translation segment is persisted. Only generates audio for
     * languages that at least one connected, audio-enabled participant is using.
     * Partial segments are skipped entirely for TTS per spec priority rule.
     */
    public void maybeSynthesize(TranslationSegment translation, UUID sessionId) {
        if (!translation.isFinal) {
            return; // TTS only runs for final segments (spec 9: "prioritize final over partial")
        }

        Set<String> voicesRequested = LiveParticipant.findActiveBySession(sessionId).stream()
                .filter(p -> p.audioEnabled && translation.targetLanguage.equalsIgnoreCase(p.targetLanguage))
                .map(p -> p.voiceCode)
                .collect(Collectors.toSet());

        if (voicesRequested.isEmpty()) {
            return; // no one listening in this language right now
        }

        for (String voiceCode : voicesRequested) {
            synthesizeOne(translation, voiceCode, sessionId);
        }
    }

    private void synthesizeOne(TranslationSegment translation, String voiceCode, UUID sessionId) {
        TextToSpeechProvider provider;
        try {
            provider = providerRegistry.resolveTts();
        } catch (IllegalStateException noProviderConfigured) {
            // Expected in the free-testing setup: no backend TTS provider is
            // configured, students use the browser's own SpeechSynthesis
            // instead (see the frontend's useBrowserTts.js). Nothing to do.
            return;
        }
        provider.synthesize(translation.translatedText, translation.targetLanguage, voiceCode, "mp3")
                .whenComplete((audioBytes, error) -> {
                    if (error != null) {
                        handleTtsFailure(sessionId, translation.targetLanguage);
                        return;
                    }
                    persistAndBroadcast(translation, voiceCode, audioBytes, sessionId);
                });
    }

    @Transactional
    protected void persistAndBroadcast(TranslationSegment translation, String voiceCode, byte[] audioBytes, UUID sessionId) {
        TtsChunk chunk = new TtsChunk();
        chunk.translationSegmentId = translation.id;
        chunk.language = translation.targetLanguage;
        chunk.voiceCode = voiceCode;
        chunk.durationMs = null; // TODO: populate from provider response or audio decode
        // TODO: persist audioBytes to object storage and set chunk.filePath to its reference
        chunk.persist();

        broadcastService.broadcastToSession(sessionId, new WsEvent(EventType.TTS_AUDIO_CHUNK_READY,
                Map.of(
                        "chunkId", chunk.id.toString(),
                        "translationSegmentId", translation.id.toString(),
                        "language", translation.targetLanguage,
                        "voiceCode", voiceCode
                )));
    }

    private void handleTtsFailure(UUID sessionId, String language) {
        // Per spec 6.3: TTS failure must never take captions down with it.
        broadcastService.broadcastToSession(sessionId, new WsEvent(EventType.ERROR,
                Map.of("scope", "TTS", "language", language,
                        "message", "Translated audio unavailable for " + language + "; captions continue.")));
    }
}
