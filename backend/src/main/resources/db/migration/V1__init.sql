-- ==========================================================
-- Zana Live Translation & Captioning -- Initial schema
-- ==========================================================

CREATE TABLE live_session (
    id                                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title                               VARCHAR(255) NOT NULL,
    teacher_id                          VARCHAR(128) NOT NULL,
    course_id                           VARCHAR(128),
    access_mode                         VARCHAR(16)  NOT NULL DEFAULT 'OPEN',      -- OPEN / CLOSED
    source_language                     VARCHAR(8)   NOT NULL,                      -- IT / EN / SQ
    auto_detect_source                  BOOLEAN      NOT NULL DEFAULT FALSE,
    target_languages                    VARCHAR(64)  NOT NULL,                      -- comma separated: IT,EN,SQ
    status                              VARCHAR(16)  NOT NULL DEFAULT 'CREATED',    -- CREATED/WAITING/ACTIVE/PAUSED/ENDED/FAILED/EXPIRED
    join_code                           VARCHAR(12)  NOT NULL UNIQUE,
    recording_enabled                   BOOLEAN      NOT NULL DEFAULT FALSE,
    student_transcript_download_enabled BOOLEAN      NOT NULL DEFAULT FALSE,
    max_participants                    INTEGER      NOT NULL DEFAULT 300,
    created_at                          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    started_at                          TIMESTAMPTZ,
    ended_at                            TIMESTAMPTZ,
    expires_at                          TIMESTAMPTZ,
    version                             BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_live_session_teacher ON live_session(teacher_id);
CREATE INDEX idx_live_session_status ON live_session(status);
CREATE UNIQUE INDEX idx_live_session_join_code ON live_session(join_code);

CREATE TABLE audio_recording (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id       UUID NOT NULL REFERENCES live_session(id) ON DELETE CASCADE,
    file_path        VARCHAR(512),
    format           VARCHAR(16),
    duration_ms      BIGINT,
    size_bytes       BIGINT,
    retention_until  TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at       TIMESTAMPTZ
);

CREATE INDEX idx_audio_recording_session ON audio_recording(session_id);

CREATE TABLE live_participant (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id        UUID NOT NULL REFERENCES live_session(id) ON DELETE CASCADE,
    user_id           VARCHAR(128),
    anonymous_name    VARCHAR(128),
    target_language   VARCHAR(8) NOT NULL,
    audio_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    voice_code        VARCHAR(64),
    joined_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    left_at           TIMESTAMPTZ,
    connection_status VARCHAR(16) NOT NULL DEFAULT 'CONNECTED' -- CONNECTED / DISCONNECTED
);

CREATE INDEX idx_live_participant_session ON live_participant(session_id);

CREATE TABLE transcript_segment (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id       UUID NOT NULL REFERENCES live_session(id) ON DELETE CASCADE,
    sequence_no      BIGINT NOT NULL,
    source_language  VARCHAR(8) NOT NULL,
    original_text    TEXT NOT NULL,
    is_final         BOOLEAN NOT NULL DEFAULT FALSE,
    confidence       REAL,
    start_offset_ms  BIGINT,
    end_offset_ms    BIGINT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transcript_segment_session_seq ON transcript_segment(session_id, sequence_no);

CREATE TABLE translation_segment (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transcript_segment_id  UUID NOT NULL REFERENCES transcript_segment(id) ON DELETE CASCADE,
    target_language        VARCHAR(8) NOT NULL,
    translated_text        TEXT NOT NULL,
    is_final               BOOLEAN NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_translation_segment_transcript ON translation_segment(transcript_segment_id);
CREATE INDEX idx_translation_segment_lang ON translation_segment(target_language);

CREATE TABLE tts_chunk (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    translation_segment_id  UUID NOT NULL REFERENCES translation_segment(id) ON DELETE CASCADE,
    language                VARCHAR(8) NOT NULL,
    voice_code              VARCHAR(64) NOT NULL,
    file_path               VARCHAR(512),
    duration_ms             BIGINT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tts_chunk_translation ON tts_chunk(translation_segment_id);

CREATE TABLE provider_config (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_code          VARCHAR(64) NOT NULL,           -- e.g. OPENAI_REALTIME, AZURE_SPEECH
    provider_type          VARCHAR(16) NOT NULL,           -- STT / TRANSLATION / TTS
    priority               INTEGER NOT NULL DEFAULT 100,
    enabled                BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_ref        VARCHAR(255),                   -- reference/alias into secret store, never the raw key
    supported_languages    VARCHAR(255),                   -- comma separated
    supported_voices       VARCHAR(512),                   -- comma separated
    cost_limit_cents       BIGINT,
    timeout_ms             INTEGER NOT NULL DEFAULT 8000,
    fallback_provider_code VARCHAR(64),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_provider_config_code_type ON provider_config(provider_code, provider_type);

CREATE TABLE usage_log (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id        UUID REFERENCES live_session(id) ON DELETE SET NULL,
    provider_code     VARCHAR(64) NOT NULL,
    provider_type     VARCHAR(16) NOT NULL,
    units             BIGINT,          -- e.g. characters translated, seconds transcribed, chars synthesized
    cost_estimate_cents BIGINT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_usage_log_session ON usage_log(session_id);
CREATE INDEX idx_usage_log_provider ON usage_log(provider_code, provider_type);
