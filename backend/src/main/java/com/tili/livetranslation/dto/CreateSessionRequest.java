package com.tili.livetranslation.dto;

import com.tili.livetranslation.domain.enums.AccessMode;
import com.tili.livetranslation.domain.enums.Language;

import java.util.List;

public class CreateSessionRequest {
    public String title;
    public String courseId;
    public AccessMode accessMode;
    public Language sourceLanguage;
    public List<Language> targetLanguages;
    public boolean recordingEnabled = false;
    public boolean studentTranscriptDownloadEnabled = false;
    public Integer maxParticipants;
}
