package com.tili.livetranslation.config;

/**
 * Marker/reference for the cache name used by TranslationOrchestrator's
 * @CacheResult (identical source text + target language -> translated text,
 * per spec section 9: "Cache identical source text + language pair to reduce cost").
 *
 * Configure size/TTL in application.properties, e.g.:
 *   quarkus.cache.caffeine."translation-cache".expire-after-write=10M
 *   quarkus.cache.caffeine."translation-cache".maximum-size=5000
 */
public final class CacheNameConfig {
    public static final String TRANSLATION_CACHE = "translation-cache";

    private CacheNameConfig() {}
}
