-- Seeds a default, working, zero-cost translation provider so the pipeline
-- can be exercised end-to-end without any vendor account/API key.
--
-- STT and TTS are intentionally NOT seeded here: for free testing, the
-- frontend uses the browser's own Web Speech API (STT) and SpeechSynthesis
-- (TTS) directly, bypassing the backend provider abstraction entirely (see
-- TeacherTextSocket + the frontend's useBrowserStt.js / useBrowserTts.js).
-- Add OPENAI_REALTIME / AZURE_SPEECH / OPENAI_TTS rows here once you have
-- real credentials and want the backend to own STT/TTS instead.

INSERT INTO provider_config (
    provider_code, provider_type, priority, enabled,
    credentials_ref, supported_languages, timeout_ms
) VALUES (
    'MYMEMORY_FREE', 'TRANSLATION', 10, true,
    NULL, 'IT,EN,SQ', 8000
);
