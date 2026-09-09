package com.tili.livetranslation.provider;

import com.tili.livetranslation.domain.enums.Language;

import java.util.Map;

/**
 * Translation provider abstraction.
 * Supports glossary/custom terminology in future phases.
 */
public interface TranslationProvider {

    /**
     * Translate text from source to target.
     * For partial text: use fast model or debounce.
     * For final: re-translate with better quality if needed.
     */
    String translate(String text, Language sourceLang, Language targetLang, Map<String, String> glossaryContext);

    /**
     * Batch translate (useful for final segments or history).
     */
    default Map<Language, String> translateBatch(String text, Language sourceLang, Iterable<Language> targets, Map<String, String> glossary) {
        // Default impl calls single translate; override for efficiency
        Map<Language, String> result = new java.util.HashMap<>();
        for (Language t : targets) {
            result.put(t, translate(text, sourceLang, t, glossary));
        }
        return result;
    }
}
