package com.tili.livetranslation.websocket.event;

/** WebSocket event types broadcast over the student/teacher/admin channels (spec 6.2). */
public enum EventType {
    SESSION_STATUS_CHANGED,
    PARTICIPANT_JOINED,
    PARTICIPANT_LEFT,
    TRANSCRIPT_PARTIAL,
    TRANSCRIPT_FINAL,
    TRANSLATION_PARTIAL,
    TRANSLATION_FINAL,
    TTS_AUDIO_CHUNK_READY,
    ERROR,
    HEARTBEAT
}
