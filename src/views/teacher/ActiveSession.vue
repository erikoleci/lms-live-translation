<template>
  <div class="active-session-page" v-if="session">
    <div class="session-topbar px-6 py-3">
      <v-btn icon="mdi-arrow-left" variant="text" size="small" to="/teacher" class="mr-2" />
      <div class="flex-grow-1 min-w-0">
        <div class="d-flex align-center gap-2">
          <h1 class="text-body-1 font-weight-bold text-truncate">{{ session.title }}</h1>
          <StatusChip :status="session.status" />
        </div>
        <p class="text-caption text-medium-emphasis">{{ session.courseName }}</p>
      </div>
      <div class="d-flex align-center gap-3 ml-4">
        <div class="d-flex align-center gap-1 text-caption">
          <v-icon size="16" color="primary">mdi-account-group</v-icon>
          <strong>{{ session.participantCount }}</strong>
          <span class="text-disabled">/ {{ session.maxParticipants }}</span>
        </div>
        <ConnectionStatus :status="wsStatus" />
        <v-btn variant="text" icon="mdi-cog" size="small" />
      </div>
    </div>

    <v-divider />

    <div class="session-body">
      <aside class="session-sidebar">
        <div class="pa-4 d-flex flex-column gap-4" style="height: 100%; overflow-y: auto;">
          <v-card rounded="xl" elevation="0" border>
            <v-card-title class="text-body-2 font-weight-bold pt-3 px-4">
              <v-icon start size="16" color="primary">mdi-remote</v-icon>
              Session Controls
            </v-card-title>
            <v-card-text class="px-4 pb-4">
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
                @toggle-mic="handleToggleMic"
              />
            </v-card-text>
          </v-card>

          <v-card rounded="xl" elevation="0" border>
            <v-card-title class="text-body-2 font-weight-bold pt-3 px-4">
              <v-icon start size="16" color="primary">mdi-waveform</v-icon>
              Microphone
            </v-card-title>
            <v-card-text class="px-4 pb-4">
              <AudioLevelMeter :level="sessionStore.audioLevel" :active="sessionStore.micActive" :muted="sessionStore.isMuted" />
              <v-alert v-if="micError" type="error" density="compact" variant="tonal" rounded="lg" class="mt-3 text-caption">{{ micError }}</v-alert>
              <v-alert v-if="sessionStore.micActive && sessionStore.audioLevel < 5 && !sessionStore.isMuted" type="warning" density="compact" variant="tonal" rounded="lg" class="mt-3 text-caption">
                No audio detected. Check your microphone.
              </v-alert>
            </v-card-text>
          </v-card>

          <QrCodeDisplay :join-code="session.joinCode" :session-id="session.id" class="mx-auto" />

          <v-card rounded="xl" elevation="0" border>
            <v-card-title class="text-body-2 font-weight-bold pt-3 px-4">
              <v-icon start size="16" color="primary">mdi-information-outline</v-icon>
              Session Info
            </v-card-title>
            <v-card-text class="px-4 pb-4 d-flex flex-column gap-2">
              <div class="info-row"><span class="text-caption text-medium-emphasis">Source</span><LanguageBadge :lang="session.sourceLanguage" /></div>
              <div class="info-row">
                <span class="text-caption text-medium-emphasis">Targets</span>
                <div class="d-flex gap-1 flex-wrap"><LanguageBadge v-for="l in session.targetLanguages" :key="l" :lang="l" /></div>
              </div>
              <div class="info-row"><span class="text-caption text-medium-emphasis">Access</span><v-chip size="x-small" variant="tonal">{{ session.accessMode }}</v-chip></div>
              <div class="info-row" v-if="session.startedAt"><span class="text-caption text-medium-emphasis">Started</span><span class="text-caption">{{ formatTime(session.startedAt) }}</span></div>
              <div class="info-row"><span class="text-caption text-medium-emphasis">Duration</span><span class="text-caption">{{ duration }}</span></div>
            </v-card-text>
          </v-card>
        </div>
      </aside>

      <main class="transcript-area">
        <div class="transcript-header px-4 py-2 d-flex align-center gap-2">
          <v-icon size="16" color="primary">mdi-text-recognition</v-icon>
          <span class="text-body-2 font-weight-bold">Live Transcript</span>
          <v-chip size="x-small" color="success" variant="tonal" v-if="session.status === 'ACTIVE'">
            <div class="live-dot-sm mr-1" />LIVE
          </v-chip>
          <v-spacer />
          <span class="text-caption text-disabled">{{ segments.length }} segments</span>
          <v-btn size="x-small" variant="text" icon="mdi-arrow-down" @click="transcriptRef?.scrollToBottom()" />
        </div>
        <v-divider />
        <div class="transcript-scroll">
          <LiveTranscript :segments="segments" ref="transcriptRef" />
        </div>
      </main>
    </div>
  </div>

  <div v-else class="d-flex align-center justify-center" style="height: 100vh;">
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
const { requestMic, stopMic, error: micError } = useAudio()

const sessionId = route.params.id
const session = computed(() => sessionStore.getSession(sessionId))
const segments = computed(() => sessionStore.getTranscript(sessionId))
const actionLoading = ref(false)
const wsStatus = ref('DISCONNECTED')
const transcriptRef = ref(null)

const { start: startSim, stop: stopSim } = useSimulatedTranscript(sessionId)

const duration = computed(() => {
  if (!session.value?.startedAt) return '—'
  const ms = Date.now() - new Date(session.value.startedAt).getTime()
  const m = Math.floor(ms / 60000)
  const s = Math.floor((ms % 60000) / 1000)
  return `${m}m ${s}s`
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
  stopMic()
  sessionStore.setActiveSession(null)
})

watch(() => session.value?.status, (status) => {
  if (status === 'ACTIVE') { wsStatus.value = 'CONNECTED'; startSim() }
  else if (status === 'PAUSED') { wsStatus.value = 'CONNECTED'; stopSim() }
  else if (status === 'ENDED') { wsStatus.value = 'DISCONNECTED'; stopSim(); stopMic() }
})

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
  stopMic()
  await new Promise(r => setTimeout(r, 600))
  sessionStore.updateSessionStatus(sessionId, 'ENDED')
  wsStatus.value = 'DISCONNECTED'
  actionLoading.value = false
}

async function handleToggleMic() {
  if (!sessionStore.micActive) {
    await requestMic()
  } else {
    sessionStore.toggleMute()
  }
}

function formatTime(iso) {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.active-session-page { display: flex; flex-direction: column; height: 100vh; overflow: hidden; }
.session-topbar { display: flex; align-items: center; background: white; min-height: 60px; }
.session-body { display: flex; flex: 1; overflow: hidden; }
.session-sidebar { width: 320px; min-width: 280px; border-right: 1px solid rgba(0,0,0,0.08); overflow: hidden; background: #FAFAFA; }
.transcript-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.transcript-header { background: white; border-bottom: 1px solid rgba(0,0,0,0.08); min-height: 44px; }
.transcript-scroll { flex: 1; overflow: hidden; }
.info-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.live-dot-sm { display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: #4caf50; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
</style>
