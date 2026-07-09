package com.tili.livetranslation.dto;

/** Student-controlled preferences (spec 4.5 / 7.2): language, audio on/off, voice. */
public record ParticipantUpdateRequest(
        String targetLanguage,
        Boolean audioEnabled,
        String voiceCode
) {}
