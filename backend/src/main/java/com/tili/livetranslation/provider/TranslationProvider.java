package com.tili.livetranslation.provider;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Abstraction over any translation vendor (OpenAI, Azure Translator, Google Cloud
 * Translation, AWS Translate, DeepL, or a local NLLB/MarianMT/LibreTranslate worker).
 */
public interface TranslationProvider {

    String code();

    /**
     * Translates a single segment. Implementations should apply glossaryContext
     * (custom terminology, phase 2) when provided, and are expected to be
     * cheap/fast enough to run on both partial and final transcript segments.
     */
    CompletionStage<String> translate(
            String text,
            String sourceLang,
            String targetLang,
            Map<String, String> glossaryContext
    );
}
