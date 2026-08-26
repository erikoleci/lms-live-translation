package com.tili.livetranslation.dto;

import com.tili.livetranslation.domain.enums.AccessMode;
import com.tili.livetranslation.domain.enums.Language;

import java.util.Set;

public class CreateSessionRequest {
    public String title;
    public String courseId;
    public AccessMode accessMode = AccessMode.OPEN;
    public Language sourceLanguage = Language.IT;
    public Set<Language> targetLanguages;
    public boolean recordingEnabled = false;
    public boolean studentTranscriptDownloadEnabled = true;
    public Integer maxParticipants = 300;
}
