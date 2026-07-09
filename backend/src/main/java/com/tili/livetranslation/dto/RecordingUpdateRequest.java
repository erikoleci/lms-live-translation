package com.tili.livetranslation.dto;

import java.time.Instant;

public record RecordingUpdateRequest(Instant retentionUntil) {}
