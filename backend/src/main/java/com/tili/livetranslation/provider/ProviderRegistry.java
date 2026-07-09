package com.tili.livetranslation.provider;

import com.tili.livetranslation.domain.ProviderConfig;
import com.tili.livetranslation.domain.ProviderType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * Resolves the active STT/Translation/TTS provider implementation based on
 * provider_config priority + enabled flag, with automatic fallback
 * (spec section 6.3: "backend must support provider fallback").
 */
@ApplicationScoped
public class ProviderRegistry {

    @Inject
    Instance<SpeechToTextProvider> sttProviders;

    @Inject
    Instance<TranslationProvider> translationProviders;

    @Inject
    Instance<TextToSpeechProvider> ttsProviders;

    public SpeechToTextProvider resolveStt() {
        return resolve(ProviderType.STT, sttProviders, SpeechToTextProvider::code)
                .orElseThrow(() -> new IllegalStateException("No enabled STT provider configured"));
    }

    public TranslationProvider resolveTranslation() {
        return resolve(ProviderType.TRANSLATION, translationProviders, TranslationProvider::code)
                .orElseThrow(() -> new IllegalStateException("No enabled translation provider configured"));
    }

    public TextToSpeechProvider resolveTts() {
        return resolve(ProviderType.TTS, ttsProviders, TextToSpeechProvider::code)
                .orElseThrow(() -> new IllegalStateException("No enabled TTS provider configured"));
    }

    /**
     * Returns the fallback provider for a given failed provider code, if configured.
     * Callers (SessionAudioBridge, TranslationOrchestrator, TtsOrchestrator) use this
     * to implement "primary fails -> fallback" / "TTS fails -> disable audio, keep captions".
     */
    public Optional<String> fallbackCodeFor(String failedProviderCode, ProviderType type) {
        return ProviderConfig.<ProviderConfig>find("providerCode = ?1 and providerType = ?2", failedProviderCode, type)
                .firstResultOptional()
                .map(pc -> pc.fallbackProviderCode);
    }

    private <T> Optional<T> resolve(ProviderType type, Instance<T> candidates, java.util.function.Function<T, String> codeFn) {
        List<ProviderConfig> configs = ProviderConfig.findEnabledByType(type);
        for (ProviderConfig cfg : configs) {
            for (T candidate : candidates) {
                if (codeFn.apply(candidate).equals(cfg.providerCode)) {
                    return Optional.of(candidate);
                }
            }
        }
        return Optional.empty();
    }
}
