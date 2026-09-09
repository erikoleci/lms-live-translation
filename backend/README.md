# Live Translation & Captioning Backend - Quarkus

Ky është backend-i për modulin Live Translation & Captioning (si Wordly për akademikë) brenda TILI.

**Stack:** Java 21 + Quarkus + PostgreSQL + WebSocket (websockets-next) + Panache ORM

## Si të fillosh projektin (në makinën tënde lokale)

1. Sigurohu që ke Quarkus CLI ose përdor https://code.quarkus.io

   Rekomanduar:
   ```bash
   quarkus create app com.tili.livetranslation \
     --extensions="rest-jackson,hibernate-orm-panache,jdbc-postgresql,websockets-next,smallrye-openapi,oidc,scheduler,cache,redis-client,micrometer,smallrye-health,config-yaml,hibernate-validator"
   ```

   Ose në https://code.quarkus.io zgjidh extensions sipas listës në spec (seksioni 6).

2. Kopjo skedarët nga kjo strukturë (domain, resource, etj.) në projektin tënd.

3. Konfiguro `application.properties` ose `application.yml` për DB, OIDC, Redis, etj.

4. Krijo migrimet e DB me Flyway ose Hibernate hbm2ddl (për dev).

## Çfarë është implementuar këtu (faza fillestare)

- **Enums**: SessionStatus, AccessMode, Language, ProviderType
- **Entities Panache**: LiveSession, LiveParticipant, TranscriptSegment, TranslationSegment, TtsChunk, AudioRecording
- **DTOs** bazë (për create/update)
- **Abstraksion Providers**: Interfaca për STT, Translation, TTS (me fallback support)
- **REST Resources** shembull për Sessions (me disa endpoints kryesore)
- **WebSocket** skica për teacher-audio dhe student channels

## Hapat e ardhshëm që mund të të ndihmoj

- Implementim i plotë i SessionResource + business logic
- WebSocket handlers me broadcast realtime
- Orchestrator për AI pipeline (STT → Translate → TTS)
- Provider impl për OpenAI / Azure (stub fillimisht)
- Security me OIDC + JWT për teacher
- QR code generation (zxing ose lib tjetër)
- Transcript export (PDF/Word me pdf skill ose docx)
- Redis pub/sub për scaling
- Teste me Quarkus @QuarkusTest

Më thuaj çfarë do të duash të zhvillojmë tani:
- Të plotësojmë një endpoint specifik?
- Të shtojmë WebSocket?
- Të bëjmë një service për audio processing?
- Ose të krijojmë skedarë të tjerë?

---

## Data Model (nga specifikimi)

Shih `domain/entities/` për implementimin Panache.

Tabela kryesore:
- live_session
- live_participant
- transcript_segment
- translation_segment
- tts_chunk
- audio_recording (opsionale)

## Realtime Flow (shkurt)

1. Teacher → WS /ws/sessions/{id}/teacher-audio → Backend → STT Provider
2. STT onPartial/onFinal → Backend → Translate (debounce + final re-translate) → Broadcast TRANSLATION_* via student WS
3. Për TTS: vetëm për studentë që kanë audio_enabled → generate chunk → send TTS_AUDIO_CHUNK_READY

Latency target: <2s partial caption, <5s translated.

## Provider Strategy (MVP)

- STT: OpenAI Realtime Transcription ose Azure Speech
- Translation: OpenAI ose Azure Translator
- TTS: OpenAI TTS ose Azure Neural TTS

Më vonë: local faster-whisper + NLLB + Piper (me worker services të veçantë).

---

**Shënim:** Ky është fillimi. Kodi është i pastër, me komente, dhe i gatshëm për tu zgjeruar. Përdor Panache për thjeshtësi dhe performancë.

Nëse ke nevojë për ndihmë me frontend Vue/Vuetify ose integrim LMS, më thuaj!
