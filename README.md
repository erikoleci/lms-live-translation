# Zana — Live Translation & Captioning

Monorepo: Vue 3 / Vuetify frontend (this root) + Quarkus backend (`/backend`).

```
/            Vue 3 frontend (this project)
/backend     Quarkus REST + WebSocket backend (Java 21, PostgreSQL, Redis)
```

See `/backend/README.md` for backend setup (`docker compose up -d && mvn quarkus:dev`).
See below for the frontend.

---

Vue 3 + Vuetify prototype for the Zana LMS live translation module, now wired
to the real Quarkus backend in `/backend`.

## What changed from the original prototype

The original repo ran entirely on local Pinia stores with simulated data
(`useSimulatedTranscript.js`, no `fetch`/`axios` calls anywhere). This version
adds a real integration layer:

```
src/services/
  api.js               axios instance (baseURL from VITE_API_BASE_URL) + wsUrl() helper
  sessionApi.js        -> SessionResource  (create/list/get/patch/state/join-info/qr/transcript)
  participantApi.js    -> ParticipantResource (join/list/update/leave)
  providerApi.js       -> AiProviderResource (admin provider config)

src/composables/
  useLiveEvents.js     connects to WS /ws/sessions/{id}/student, feeds
                       TRANSCRIPT_*/TRANSLATION_*/PARTICIPANT_*/SESSION_STATUS_CHANGED
                       events into the session/participant stores
  useAudio.js          (updated) now also streams MediaRecorder chunks to
                       WS /ws/sessions/{id}/teacher-audio when a sessionId is passed
```

The three Pinia stores (`session.js`, `participant.js`, `provider.js`) now call
these services instead of mutating local arrays directly. `useSimulatedTranscript.js`
is no longer used anywhere (left in the repo for reference — safe to delete).

## Free end-to-end testing (no OpenAI/Azure account needed)

This build defaults to a completely free test setup:

- **STT**: `useBrowserStt.js` uses the browser's built-in Web Speech API
  (Chrome/Edge only) instead of streaming raw audio. Recognized text is sent
  to `WS /ws/sessions/{id}/teacher-text`.
- **Translation**: the backend's `MyMemoryTranslationProvider` (keyless, free,
  ~5000 words/day) is enabled by default — nothing to configure.
- **TTS**: `useBrowserTts.js` speaks final translations out loud using the
  browser's own `SpeechSynthesis` API on the student's device — no backend
  TTS call happens at all.

This means: create a session, click the mic, start talking in Italian/English,
and you'll see live captions and translations appear for real, with zero
signup and zero cost. Voice quality (both STT accuracy and TTS pronunciation)
is noticeably lower than a paid cloud provider, especially for Albanian — this
is meant for functional testing, not production quality.

When you're ready to upgrade: add real credentials + implement the `TODO`s in
`OpenAiSpeechToTextProvider`/`OpenAiTextToSpeechProvider` (backend), then swap
`useBrowserStt`/`useBrowserTts` for the raw-audio-streaming version of
`useAudio.js` from the previous iteration of this repo (still in git history)
— or ask for that wiring again.

## Setup

```bash
npm install
cp .env.example .env.local
# edit .env.local if your backend isn't on localhost:8080
npm run dev
```

`.env.local`:
```
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=ws://localhost:8080
```

**The backend must be running first** (`docker compose up -d && mvn quarkus:dev`
in the `zana-backend` project) — the teacher dashboard calls `GET /api/sessions`
on mount and will show an empty list (not an error) if the backend is down,
since the fetch failure is currently swallowed with `.catch(() => {})`.
Open the browser console if a screen looks unexpectedly empty.

## What works end-to-end right now

- Teacher: create session -> real `POST /api/sessions` -> shows up in dashboard
- Teacher: Start/Pause/Resume/End -> real `PATCH /api/sessions/{id}/state`
- Teacher: mic on -> streams real audio chunks to `teacher-audio` WS
- Student: enter join code -> real `GET /api/sessions/by-code/{code}` -> real
  `POST /api/sessions/{id}/participants`
- Both: live captions/translations arrive over the real `student` WS channel
- Student: changing language/audio/voice -> `PATCH .../participants/{id}`
- Admin: provider list/toggle/edit -> real `GET/PATCH /api/ai-providers`

## What's still a stub (backend-side, not this repo)

Captions won't actually appear yet because the backend's STT/Translation/TTS
provider adapters are skeletons (`OpenAiSpeechToTextProvider` etc. — see the
backend README). Wire real vendor credentials there and this frontend needs
zero further changes to start showing live captions.

## Known field-naming gap

`ProviderCard.vue` reads `provider.costLimit` (dollars); the backend returns
`costLimitCents` (integer cents, to avoid float rounding on money). The store's
`toRequestPayload()` in `provider.js` already converts on write; if you want
reads to show dollars too, divide by 100 wherever `costLimit` is displayed, or
add a computed `costLimit` getter on the mapped provider objects in
`provider.js`'s `fetchProviders()`.

## Admin auth

`AiProviderResource` is `@RolesAllowed("admin")` on the backend. Until OIDC is
wired up on this frontend (see the `TODO` in `src/services/api.js` for the
axios request interceptor), admin screens only work while the backend is
running with `%dev.quarkus.http.auth.permission.authenticated.policy=permit`
(the default dev profile setting) — i.e. local development only. Do not ship
`/admin` routes to production before wiring real Keycloak login + token
attachment.
