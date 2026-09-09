# Zana — Live Translation & Captioning (Frontend)

A Vue 3 + Vuetify frontend prototype for a live classroom translation/
captioning UI. This is a **UI-only** build: there is no backend, no API
calls, no WebSockets, and no authentication anywhere in this project.
Everything runs entirely in the browser using local, in-memory mock data.

## What this is

- Teacher dashboard: create/view sessions, launch an active-session screen
- Active session: mic on/off, live caption feed (simulated), QR join code,
  transcript export (.txt/.srt)
- Student flow: join by code, live captions with language switching and
  optional browser text-to-speech
- Admin screens: provider config, workspaces, usage stats (all mock data)

All "live" data (sessions, transcripts, participants, provider configs) is
generated and held in Pinia stores at runtime — nothing is persisted to a
server. A couple of small UI preferences (dark mode, caption font size) are
saved to `localStorage` via `pinia-plugin-persistedstate` for convenience
across reloads.

## Setup

```bash
npm install
npm run dev
```

No environment variables, no `.env` file, no backend to run first.

## Stack

- Vue 3 (Composition API, `<script setup>`)
- Vuetify 4 — all UI is built from Vuetify components and utility classes;
  there is no custom CSS/SCSS anywhere in the project
- Pinia (+ `pinia-plugin-persistedstate` for the two local UI prefs above)
- `qrcode.vue` for the join-code QR display

## Project structure

```
src/
  views/           one file per route/screen, grouped by role (teacher/student/admin)
  components/      reusable presentational pieces, grouped the same way
  stores/          Pinia stores holding mock/demo data + local UI state
  composables/      useSimulatedTranscript.js (fake live caption generator),
                    useBrowserTts.js (browser SpeechSynthesis wrapper)
  router/          route table (vue-router, hash history)
  plugins/         Vuetify setup
```
