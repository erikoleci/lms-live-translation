# Zana Live Translation & Captioning — Backend (Quarkus)

Backend implementation of the *LMS Live Translation & Captioning* functional
and technical specification, built to pair with the Vue 3 / Vuetify frontend
at `erikoleci/lms-live-translation`.

> **Frontend status check:** as of this writing, that repo is a UI prototype —
> every screen runs on local Pinia stores with simulated data
> (`useSimulatedTranscript.js`, `useAudio.js` for the mic meter) and there are
> no `fetch`/`axios` calls yet. This backend is built to the spec directly and
> is ready to be wired in; see **Frontend integration notes** below for the
> exact contract the frontend's `useWebSocket.js` expects.

## Stack

- **Java 21**, Quarkus 3.15
- PostgreSQL (Hibernate ORM with Panache + Flyway migrations)
- Redis (session pub/sub placeholder for horizontal scaling)
- `quarkus-websockets-next` for the three realtime channels
- OIDC/Keycloak for LMS SSO (teacher endpoints; student join can stay anonymous)
- Pluggable AI provider architecture (OpenAI / Azure stubs wired, ready for
  AWS/Google/local Whisper-Piper-NLLB workers)

## Project layout

```
src/main/java/com/tili/livetranslation/
  domain/       JPA/Panache entities + enums (SessionState, AccessMode, ...)
  dto/          Request/response records for the REST layer
  rest/         JAX-RS resources (Sessions, Participants, QR, Transcript, Recording, AI providers)
  websocket/    Teacher audio / Student / Admin monitor channels (quarkus-websockets-next)
  service/      Business logic: SessionService, ParticipantService, TranscriptService,
                TranslationOrchestrator, TtsOrchestrator, QrCodeService, ProviderConfigService,
                SessionExpiryScheduler, SessionBroadcastService (fan-out point)
  provider/     STT/Translation/TTS abstractions + ProviderRegistry (priority + fallback)
    impl/       OpenAI + Azure adapter skeletons (HTTP calls left as TODOs, see below)
  exception/    Domain exceptions + a single JAX-RS ExceptionMapper
src/main/resources/
  application.properties
  db/migration/V1__init.sql   Full schema for every table in spec section 8
```

## What's fully implemented

- Session lifecycle state machine (`SessionState.canTransitionTo`) enforcing
  the exact transitions from spec 4.1.
- All REST endpoints from spec section 6.1 (sessions, state, join/participants,
  QR code, transcript/translations, recording, AI provider config).
- All three WebSocket channels from spec 6.2, with a broadcast fan-out service
  (`SessionBroadcastService`) as the single point every other service talks to.
- Partial→final transcript handling, translation debounce + cache + fallback
  hook, and TTS "only for languages currently requested, final segments only,
  never blocks captions on failure" rules — all per spec section 9.
- QR code generation (ZXing) and join-URL construction.
- Automatic expiry of stale `CREATED`/`WAITING` sessions via `@Scheduled`.
- Access control: `OPEN` vs `CLOSED` sessions, anonymous vs SSO join, teacher-only
  recording/config endpoints, transcript export gating on
  `studentTranscriptDownloadEnabled`.

## What's intentionally a stub (`TODO` in code)

These require live vendor credentials/SDKs and are the correct next step once
you pick concrete providers for MVP (spec recommends OpenAI Realtime or Azure
Speech for STT, either for translation, either for TTS):

- `OpenAiSpeechToTextProvider` / `AzureSpeechToTextProvider` — realtime
  websocket wiring to the vendor, ephemeral token issuance.
- `OpenAiTranslationProvider` / `AzureTranslationProvider` — actual HTTP call
  (currently passthrough).
- `OpenAiTextToSpeechProvider` / `AzureTextToSpeechProvider` — actual synthesis
  call + streaming.
- Object storage wiring for `audio_recording` and `tts_chunk` file paths
  (currently DB rows only, no file persisted).
- `TeacherAudioSocket` → `SessionService` pause/end hook to force-close the
  open STT provider session (needs a connection registry keyed by sessionId,
  the same pattern already used for broadcast in `StudentSocket`).

None of these are hard to fill in — they're isolated behind the
`SpeechToTextProvider` / `TranslationProvider` / `TextToSpeechProvider`
interfaces, so swapping or adding a provider never touches REST/WebSocket/DB
code.

## Free end-to-end testing path (no vendor account needed)

For trying out the full pipeline before paying for OpenAI/Azure, this backend
ships with:

- **Translation**: `MyMemoryTranslationProvider` — real HTTP calls to the free,
  keyless `api.mymemory.translated.net` API (~5000 words/day). Seeded as the
  default enabled provider in `V2__seed_free_test_providers.sql`.
- **STT**: not done server-side at all in this path — `TeacherTextSocket`
  (`WS /ws/sessions/{id}/teacher-text`) accepts already-recognized text from
  the teacher's browser (Web Speech API, see the frontend's
  `useBrowserStt.js`) and feeds it into the normal transcript pipeline.
- **TTS**: not done server-side either — `TtsOrchestrator` simply skips
  synthesis when no TTS provider is configured (see the `IllegalStateException`
  catch in `synthesizeOne`), and the frontend speaks translations client-side
  via the browser's `SpeechSynthesis` API instead (`useBrowserTts.js`).

Nothing about this path is a dead end: swap in `OPENAI_REALTIME`/`AZURE_SPEECH`
STT and a real TTS provider later by inserting/enabling rows in
`provider_config` — the REST/WebSocket/DB layers don't change.

## Frontend integration notes

The frontend's `useWebSocket.js` composable parses every incoming message as:

```js
const msg = JSON.parse(e.data)
emit(msg.event, msg.payload)   // e.g. msg.event === 'TRANSCRIPT_FINAL'
```

This backend's `WsEvent` record serializes to exactly that shape:
`{"event": "TRANSCRIPT_FINAL", "payload": {...}}`. When you wire the frontend
up for real, point it at:

- Teacher mic capture → `wss://.../ws/sessions/{sessionId}/teacher-audio` (binary frames)
- Student caption feed → `wss://.../ws/sessions/{sessionId}/student`
- Admin monitor → `wss://.../ws/admin/live-sessions/{sessionId}`

and replace `useSimulatedTranscript.js` with a listener on `TRANSCRIPT_PARTIAL`
/ `TRANSCRIPT_FINAL` / `TRANSLATION_PARTIAL` / `TRANSLATION_FINAL` events,
appending to `sessionStore.addTranscriptSegment(...)` the same way the
simulator already does — the shapes line up field-for-field
(`sequenceNo`, `sourceLanguage`, `originalText`, `isFinal`, `confidence`,
`startOffsetMs`, `endOffsetMs`).

One naming note: the Pinia `provider` store/`ProviderCard.vue` reads
`provider.costLimit`; this API returns `costLimitCents` (to avoid float money
bugs). Divide by 100 in the frontend, or rename in `ProviderConfigResponse` if
you'd rather keep dollars end-to-end.

The frontend also has an admin `workspace`/`class`/`course` store
(multi-tenant grouping of sessions) that isn't in the original functional
spec. That's out of scope here — add a `Workspace`/`SchoolClass` entity pair
and a couple of CRUD endpoints under `/api/admin/workspaces` if you want to
carry that concept into the real backend; it doesn't affect anything already
built.

## Running locally

```bash
docker compose up -d          # postgres + redis
mvn quarkus:dev                # http://localhost:8080, Dev UI at /q/dev
```

OpenAPI/Swagger UI is available at `/q/swagger-ui` once running.

Set real credentials via environment variables before enabling live providers:
`OPENAI_API_KEY`, `AZURE_SPEECH_KEY`, `AZURE_REGION`, `OIDC_CLIENT_SECRET`, etc.
— see the top of `application.properties` for the full list.
