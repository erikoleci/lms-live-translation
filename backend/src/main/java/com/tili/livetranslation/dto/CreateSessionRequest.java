package com.tili.livetranslation.dto;

import com.tili.livetranslation.domain.enums.Language;

public class JoinSessionRequest {
    public String joinCode;
    public String anonymousName;
    public Language targetLanguage = Language.EN;
    public boolean audioEnabled = false;
    public String voiceCode;
}
