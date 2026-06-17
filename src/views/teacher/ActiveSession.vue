<template>
  <div class="active-session-page" :class="{ 'dark-page': uiStore.darkMode }" v-if="session">

    <!-- TOP BAR -->
    <div class="session-topbar px-4 py-0">
      <v-btn icon="mdi-arrow-left" variant="text" size="small" to="/teacher" />
      <div class="flex-grow-1 min-w-0 ml-2">
        <div class="d-flex align-center gap-2">
          <h1 class="text-body-1 font-weight-bold text-truncate">{{ session.title }}</h1>
          <StatusChip :status="session.status" />
        </div>
        <p class="text-caption text-medium-emphasis">{{ session.courseName }}</p>
      </div>
      <div class="d-flex align-center gap-3 ml-3">
        <div class="d-flex align-center gap-1 text-caption">
          <v-icon size="15" color="primary">mdi-account-group</v-icon>
          <strong>{{ session.participantCount }}</strong>
          <span class="text-disabled">/ {{ session.maxParticipants }}</span>
        </div>
        <ConnectionStatus :status="wsStatus" />
        <v-btn :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'" variant="text" size="small" @click="uiStore.toggleDarkMode()" />
      </div>
    </div>

    <v-divider />

    <div class="session-body">

      <!-- LEFT SIDEBAR -->
      <aside class="session-sidebar">
        <div class="sidebar-scroll">

          <!-- ===== MIC CONTROLS ===== -->
          <v-card rounded="xl" elevation="0" border class="mb-3">
            <v-card-title class="text-body-2 font-weight-bold pt-3 px-3">
              <v-icon start size="16" color="primary">mdi-microphone</v-icon>
              Microphone
            </v-card-title>
            <v-card-text class="px-3 pb-3">
              <!-- Big status indicator -->
              <div class="mic-status-ring mb-3" :class="micStatusClass">
                <v-icon size="28" :color="micIconColor">{{ micIcon }}</v-icon>
              </div>
              <p class="text-caption text-center mb-3" :class="micStatusTextClass">{{ micStatusLabel }}</p>

              <!-- Audio level bar -->
              <AudioLevelMeter :level="sessionStore.audioLevel" :active="sessionStore.micActive" :muted="sessionStore.isMuted" class="mb-3" />

              <!-- START / PAUSE / STOP buttons -->
              <div class="mic-btn-row">
                <v-btn
                  v-if="!sessionStore.micActive"
                  color="success"
                  variant="flat"
                  size="large"
                  class="flex-grow-1"
                  prepend-icon="mdi-microphone"
                  :disabled="session.status !== 'ACTIVE'"
                  :loading="micLoading"
                  @click="startMic"
                >
                  Start
                </v-btn>
                <template v-else>
                  <v-btn
                    :color="sessionStore.isMuted ? 'warning' : 'default'"
                    :variant="sessionStore.isMuted ? 'flat' : 'tonal'"
                    size="large"
                    class="flex-grow-1"
                    :prepend-icon="sessionStore.isMuted ? 'mdi-microphone-off' : 'mdi-pause'"
                    @click="sessionStore.toggleMute()"
                  >
                    {{ sessionStore.isMuted ? 'Unmute' : 'Pause' }}
                  </v-btn>
                  <v-btn
                    color="error"
                    variant="tonal"
                    size="large"
                    icon="mdi-stop"
                    @click="stopMic"
                  />
                </template>
              </div>

              <v-alert v-if="micError" type="error" density="compact" variant="tonal" rounded="lg" class="mt-2 text-caption">{{ micError }}</v-alert>
              <v-alert v-if="sessionStore.micActive && sessionStore.audioLevel < 5 && !sessionStore.isMuted" type="warning" density="compact" variant="tonal" rounded="lg" class="mt-2 text-caption">
                <v-icon start size="12">mdi-alert</v-icon>No audio detected. Check mic.
              </v-alert>
            </v-card-text>
          </v-card>

          <!-- SESSION CONTROLS -->
          <v-card rounded="xl" elevation="0" border class="mb-3">
            <v-card-title class="text-body-2 font-weight-bold pt-3 px-3">
              <v-icon start size="16" color="primary">mdi-remote</v-icon>
              Session
            </v-card-title>
            <v-card-text class="px-3 pb-3">
              <SessionControls
                :status="session.status"
                :mic-active="sessionStore.micActive"
                :muted="sessionStore.isMuted"
                :participant-count="session.participantCount"
                :loading="actionLoading"
                @start="handleStart"
                @pause="handlePause"
                @resume="handleResume"
                @end="handleEnd"
                @toggle-mic="() => {}"
              />
            </v-card-text>
          </v-card>

          <!-- QR CODE -->
          <QrCodeDisplay :join-code="session.joinCode" :session-id="session.id" class="mb-3 mx-auto" />

          <!-- SESSION INFO -->
          <v-card rounded="xl" elevation="0" border>
            <v-card-title class="text-body-2 font-weight-bold pt-3 px-3">
              <v-icon start size="16" color="primary">mdi-information-outline</v-icon>
              Info
            </v-card-title>
            <v-card-text class="px-3 pb-3 d-flex flex-column gap-2">
              <div class="info-row"><span class="text-caption text-medium-emphasis">Source</span><LanguageBadge :lang="session.sourceLanguage" /></div>
              <div class="info-row">
                <span class="text-caption text-medium-emphasis">Targets</span>
                <div class="d-flex gap-1 flex-wrap"><LanguageBadge v-for="l in session.targetLanguages" :key="l" :lang="l" /></div>
              </div>
              <div class="info-row"><span class="text-caption text-medium-emphasis">Access</span><v-chip size="x-small" variant="tonal">{{ session.accessMode }}</v-chip></div>
              <div class="info-row"><span class="text-caption text-medium-emphasis">Duration</span><span class="text-caption font-weight-bold">{{ duration }}</span></div>
            </v-card-text>
          </v-card>

        </div>
      </aside>

      <!-- MAIN TRANSCRIPT AREA -->
      <main class="transcript-main">

        <!-- Teacher's own speech — what they're saying RIGHT NOW -->
        <div class="teacher-caption-bar" :class="{ 'active-bar': session.status === 'ACTIVE' && sessionStore.micActive && !sessionStore.isMuted }">
          <div class="d-flex align-center gap-2 mb-1">
            <div class="speak-dot" :class="{ 'speak-dot--active': session.status === 'ACTIVE' && sessionStore.micActive && !sessionStore.isMuted }" />
            <span class="text-caption font-weight-bold text-uppercase" style="letter-spacing:0.6px">
              {{ session.status === 'ACTIVE' && sessionStore.micActive && !sessionStore.isMuted ? 'You are speaking…' : 'Your speech will appear here' }}
            </span>
            <v-spacer />
            <LanguageBadge :lang="session.sourceLanguage" />
          </div>
          <p class="teacher-live-text" :class="{ 'text--active': liveText }">
            {{ liveText || (session.status === 'ACTIVE' ? 'Waiting for speech…' : 'Start the session and your microphone to begin.') }}
          </p>
        </div>

        <!-- Transcript panel header -->
        <div class="transcript-header px-4 py-2 d-flex align-center gap-2">
          <v-icon size="15" color="primary">mdi-text-recognition</v-icon>
          <span class="text-body-2 font-weight-bold">Live Transcript</span>
          <v-chip size="x-small" color="success" variant="tonal" v-if="session.status === 'ACTIVE'">
            <div class="live-dot-sm mr-1" />LIVE
          </v-chip>
          <v-spacer />
          <span class="text-caption text-disabled">{{ segments.length }} segments</span>
          <v-btn size="x-small" variant="text" :to="`/teacher/session/${session.id}/transcript`" prepend-icon="mdi-export">Export</v-btn>
        </div>
        <v-divider />
        <div class="transcript-scroll">
          <LiveTranscript :segments="segments" ref="transcriptRef" />
        </div>

      </main>
    </div>
  </div>

  <div v-else class="d-flex align-center justify-center" style="height:100vh">
    <div class="text-center">
      <v-icon size="64" color="grey-lighten-2">mdi-video-off</v-icon>
      <h2 class="text-h6 mt-4">Session not found</h2>
      <v-btn to="/teacher" class="mt-4" variant="tonal">Back to Dashboard</v-btn>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useSessionStore } from '../../stores/session.js'
import { useUiStore } from '../../stores/ui.js'
import { useAudio } from '../../composables/useAudio.js'
import { useSimulatedTranscript } from '../../composables/useSimulatedTranscript.js'
import SessionControls from '../../components/teacher/SessionControls.vue'
import AudioLevelMeter from '../../components/teacher/AudioLevelMeter.vue'
import QrCodeDisplay from '../../components/teacher/QrCodeDisplay.vue'
import LiveTranscript from '../../components/teacher/LiveTranscript.vue'
import StatusChip from '../../components/shared/StatusChip.vue'
import ConnectionStatus from '../../components/shared/ConnectionStatus.vue'
import LanguageBadge from '../../components/shared/LanguageBadge.vue'

const route = useRoute()
const sessionStore = useSessionStore()
const uiStore = useUiStore()
const { requestMic, stopMic: stopMicHw, error: micError } = useAudio()

const sessionId = route.params.id
const session = computed(() => sessionStore.getSession(sessionId))
const segments = computed(() => sessionStore.getTranscript(sessionId))
const actionLoading = ref(false)
const micLoading = ref(false)
const wsStatus = ref('DISCONNECTED')
const transcriptRef = ref(null)

const { start: startSim, stop: stopSim } = useSimulatedTranscript(sessionId)

// Live text — shows the most recent partial/final segment text
const liveText = computed(() => {
  if (!sessionStore.micActive || sessionStore.isMuted) return ''
  const all = segments.value
  if (!all.length) return ''
  return all[all.length - 1].originalText ?? ''
})

const duration = computed(() => {
  if (!session.value?.startedAt) return '—'
  const ms = Date.now() - new Date(session.value.startedAt).getTime()
  const m = Math.floor(ms / 60000)
  const s = Math.floor((ms % 60000) / 1000)
  return `${m}m ${s}s`
})

// Mic status helpers
const micStatusClass = computed(() => {
  if (!sessionStore.micActive) return 'ring--idle'
  if (sessionStore.isMuted) return 'ring--muted'
  return 'ring--live'
})
const micIconColor = computed(() => {
  if (!sessionStore.micActive) return 'grey'
  if (sessionStore.isMuted) return 'warning'
  return 'success'
})
const micIcon = computed(() => {
  if (!sessionStore.micActive) return 'mdi-microphone-off'
  if (sessionStore.isMuted) return 'mdi-microphone-off'
  return 'mdi-microphone'
})
const micStatusLabel = computed(() => {
  if (!sessionStore.micActive) return 'Microphone off'
  if (sessionStore.isMuted) return 'Muted'
  return `Live — ${Math.round(sessionStore.audioLevel)}%`
})
const micStatusTextClass = computed(() => {
  if (!sessionStore.micActive) return 'text-disabled'
  if (sessionStore.isMuted) return 'text-warning'
  return 'text-success font-weight-bold'
})

let durationInterval

onMounted(() => {
  sessionStore.setActiveSession(sessionId)
  durationInterval = setInterval(() => {}, 1000)
  if (session.value?.status === 'ACTIVE') {
    wsStatus.value = 'CONNECTED'
    startSim()
  }
})

onUnmounted(() => {
  clearInterval(durationInterval)
  stopSim()
  stopMicHw()
  sessionStore.setActiveSession(null)
})

watch(() => session.value?.status, (status) => {
  if (status === 'ACTIVE') { wsStatus.value = 'CONNECTED'; startSim() }
  else if (status === 'PAUSED') { wsStatus.value = 'CONNECTED'; stopSim() }
  else if (status === 'ENDED') { wsStatus.value = 'DISCONNECTED'; stopSim(); stopMicHw() }
})

async function startMic() {
  micLoading.value = true
  await requestMic()
  micLoading.value = false
}

function stopMic() {
  stopMicHw()
  sessionStore.setMicActive(false)
  sessionStore.setAudioLevel(0)
}

async function handleStart() {
  actionLoading.value = true
  await new Promise(r => setTimeout(r, 800))
  sessionStore.updateSessionStatus(sessionId, 'ACTIVE')
  wsStatus.value = 'CONNECTED'
  actionLoading.value = false
}

async function handlePause() {
  actionLoading.value = true
  await new Promise(r => setTimeout(r, 400))
  sessionStore.updateSessionStatus(sessionId, 'PAUSED')
  actionLoading.value = false
}

async function handleResume() {
  actionLoading.value = true
  await new Promise(r => setTimeout(r, 400))
  sessionStore.updateSessionStatus(sessionId, 'ACTIVE')
  actionLoading.value = false
}

async function handleEnd() {
  actionLoading.value = true
  stopSim()
  stopMicHw()
  await new Promise(r => setTimeout(r, 600))
  sessionStore.updateSessionStatus(sessionId, 'ENDED')
  wsStatus.value = 'DISCONNECTED'
  actionLoading.value = false
}
</script>

<style scoped>
.active-session-page { display: flex; flex-direction: column; height: 100vh; overflow: hidden; background: #F5F7FA; }
.dark-page { background: #12121F; }
.session-topbar { display: flex; align-items: center; background: white; min-height: 58px; }
.dark-page .session-topbar { background: #1E1E2E; }
.session-body { display: flex; flex: 1; overflow: hidden; }

/* Sidebar */
.session-sidebar { width: 280px; min-width: 260px; border-right: 1px solid rgba(0,0,0,0.07); background: #FAFAFA; display: flex; flex-direction: column; }
.dark-page .session-sidebar { background: #1A1A2E; border-right-color: rgba(255,255,255,0.07); }
.sidebar-scroll { flex: 1; overflow-y: auto; padding: 12px; }

/* Mic controls */
.mic-status-ring { width: 72px; height: 72px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto; transition: all 0.3s; border: 3px solid transparent; }
.ring--idle { background: rgba(0,0,0,0.05); border-color: rgba(0,0,0,0.1); }
.ring--live { background: rgba(76,175,80,0.1); border-color: #4caf50; box-shadow: 0 0 0 6px rgba(76,175,80,0.15); animation: ring-pulse 1.5s ease infinite; }
.ring--muted { background: rgba(255,152,0,0.1); border-color: #ff9800; }
@keyframes ring-pulse { 0%,100% { box-shadow: 0 0 0 6px rgba(76,175,80,0.15); } 50% { box-shadow: 0 0 0 12px rgba(76,175,80,0.05); } }

.mic-btn-row { display: flex; gap: 8px; }

/* Teacher live caption bar */
.transcript-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.teacher-caption-bar { padding: 14px 20px; background: rgba(21,101,192,0.04); border-bottom: 2px solid rgba(21,101,192,0.1); min-height: 84px; transition: all 0.3s; }
.active-bar { background: rgba(76,175,80,0.06); border-bottom-color: rgba(76,175,80,0.25); }
.dark-page .teacher-caption-bar { background: rgba(66,165,245,0.06); border-bottom-color: rgba(66,165,245,0.15); }
.speak-dot { width: 8px; height: 8px; border-radius: 50%; background: #ccc; transition: background 0.2s; }
.speak-dot--active { background: #4caf50; animation: pulse-dot 1s infinite; }
@keyframes pulse-dot { 0%,100%{opacity:1}50%{opacity:0.3} }
.teacher-live-text { font-size: 17px; font-weight: 600; line-height: 1.5; color: #333; margin: 0; min-height: 26px; transition: color 0.2s; }
.teacher-live-text.text--active { color: #1565C0; }
.dark-page .teacher-live-text { color: #ccc; }
.dark-page .teacher-live-text.text--active { color: #42A5F5; }

.transcript-header { background: white; min-height: 44px; border-bottom: 1px solid rgba(0,0,0,0.07); }
.dark-page .transcript-header { background: #1E1E2E; border-bottom-color: rgba(255,255,255,0.07); }
.transcript-scroll { flex: 1; overflow: hidden; }
.info-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.live-dot-sm { display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: #4caf50; animation: pulse-dot 1.5s infinite; }
</style>
