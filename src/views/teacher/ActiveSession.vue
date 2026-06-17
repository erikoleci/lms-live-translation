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
      <div class="d-flex align-center gap-2 ml-3">
        <div class="d-flex align-center gap-1 text-caption">
          <v-icon size="15" color="primary">mdi-account-group</v-icon>
          <strong>{{ session.participantCount }}</strong>
          <span class="text-disabled">/ {{ session.maxParticipants }}</span>
        </div>
        <ConnectionStatus :status="wsStatus" />

        <!-- QR toggle button -->
        <v-btn
          :color="qrFloat ? 'primary' : 'default'"
          :variant="qrFloat ? 'flat' : 'tonal'"
          size="small"
          prepend-icon="mdi-qrcode"
          rounded="lg"
          @click="qrFloat = !qrFloat"
        >
          QR
        </v-btn>

        <!-- Download transcript -->
        <v-btn
          color="default"
          variant="tonal"
          size="small"
          prepend-icon="mdi-download"
          rounded="lg"
          :disabled="!segments.length"
          @click="downloadTranscript"
        >
          Download
        </v-btn>

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
              <div class="mic-status-ring mb-3" :class="micStatusClass">
                <v-icon size="28" :color="micIconColor">{{ micIcon }}</v-icon>
              </div>
              <p class="text-caption text-center mb-3" :class="micStatusTextClass">{{ micStatusLabel }}</p>

              <AudioLevelMeter :level="sessionStore.audioLevel" :active="sessionStore.micActive" :muted="sessionStore.isMuted" class="mb-3" />

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
                  <v-btn color="error" variant="tonal" size="large" icon="mdi-stop" @click="stopMic" />
                </template>
              </div>

              <v-alert v-if="micError" type="error" density="compact" variant="tonal" rounded="lg" class="mt-2 text-caption">{{ micError }}</v-alert>
              <v-alert v-if="sessionStore.micActive && sessionStore.audioLevel < 5 && !sessionStore.isMuted" type="warning" density="compact" variant="tonal" rounded="lg" class="mt-2 text-caption">
                <v-icon start size="12">mdi-alert</v-icon>No audio detected.
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
              <div class="info-row"><span class="text-caption text-medium-emphasis">Join code</span>
                <v-chip size="small" color="primary" variant="tonal" class="font-weight-bold" style="letter-spacing:2px">{{ session.joinCode }}</v-chip>
              </div>
            </v-card-text>
          </v-card>

        </div>
      </aside>

      <!-- MAIN TRANSCRIPT AREA -->
      <main class="transcript-main">

        <!-- Teacher's live speech bar -->
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
            {{ liveText || (session.status === 'ACTIVE' ? 'Start your microphone to begin transcription…' : 'Start the session first.') }}
          </p>
        </div>

        <!-- Transcript header -->
        <div class="transcript-header px-4 py-2 d-flex align-center gap-2">
          <v-icon size="15" color="primary">mdi-text-recognition</v-icon>
          <span class="text-body-2 font-weight-bold">Live Transcript</span>
          <v-chip size="x-small" color="success" variant="tonal" v-if="session.status === 'ACTIVE'">
            <div class="live-dot-sm mr-1" />LIVE
          </v-chip>
          <v-spacer />
          <span class="text-caption text-disabled">{{ segments.length }} segments</span>
          <v-btn size="x-small" variant="text" prepend-icon="mdi-download" :disabled="!segments.length" @click="downloadTranscript">
            Download
          </v-btn>
        </div>
        <v-divider />
        <div class="transcript-scroll">
          <LiveTranscript :segments="segments" ref="transcriptRef" />
        </div>

      </main>
    </div>

    <!-- ===================== FLOATING QR WINDOW ===================== -->
    <transition name="qr-pop">
      <div
        v-if="qrFloat"
        class="qr-float-window"
        :style="{ top: qrPos.y + 'px', left: qrPos.x + 'px' }"
        @mousedown="startDrag"
      >
        <!-- Window title bar -->
        <div class="qr-title-bar">
          <v-icon size="14" color="primary" class="mr-1">mdi-qrcode</v-icon>
          <span class="text-caption font-weight-bold">Join QR — {{ session.joinCode }}</span>
          <v-spacer />
          <v-btn icon="mdi-minus" size="x-small" variant="text" @click.stop="qrMinimized = !qrMinimized" />
          <v-btn icon="mdi-close" size="x-small" variant="text" @click.stop="qrFloat = false" />
        </div>

        <!-- QR body -->
        <div v-if="!qrMinimized" class="qr-float-body">
          <div class="qr-canvas-wrap">
            <qrcode-vue :value="joinUrl" :size="200" level="H" :margin="2" />
          </div>
          <v-chip color="primary" variant="flat" size="large" class="font-weight-bold join-code-chip mt-3">
            {{ session.joinCode }}
          </v-chip>
          <div class="d-flex gap-2 mt-3">
            <v-btn size="small" variant="tonal" color="primary" prepend-icon="mdi-content-copy" @click.stop="copyLink">Copy</v-btn>
            <v-btn size="small" variant="tonal" color="secondary" prepend-icon="mdi-download" @click.stop="downloadQr">Save QR</v-btn>
          </div>
        </div>
      </div>
    </transition>

    <!-- Snackbar -->
    <v-snackbar v-model="snack.show" :color="snack.color" timeout="2500" location="bottom">
      <v-icon start>mdi-check-circle</v-icon>{{ snack.text }}
    </v-snackbar>

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
import QrcodeVue from 'qrcode.vue'
import { useSessionStore } from '../../stores/session.js'
import { useUiStore } from '../../stores/ui.js'
import { useAudio } from '../../composables/useAudio.js'
import { useSimulatedTranscript } from '../../composables/useSimulatedTranscript.js'
import SessionControls from '../../components/teacher/SessionControls.vue'
import AudioLevelMeter from '../../components/teacher/AudioLevelMeter.vue'
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

// QR float window state
const qrFloat = ref(true)
const qrMinimized = ref(false)
const qrPos = ref({ x: window.innerWidth - 320, y: 80 })
const snack = ref({ show: false, text: '', color: 'success' })

const joinUrl = computed(() =>
  `${window.location.origin}${window.location.pathname}#/student/join/${session.value?.joinCode}`
)

// Dragging
let dragOffset = { x: 0, y: 0 }
function startDrag(e) {
  if (e.target.closest('button') || e.target.closest('.v-btn')) return
  dragOffset = { x: e.clientX - qrPos.value.x, y: e.clientY - qrPos.value.y }
  window.addEventListener('mousemove', onDrag)
  window.addEventListener('mouseup', stopDragFn)
}
function onDrag(e) {
  qrPos.value = {
    x: Math.max(0, Math.min(window.innerWidth - 280, e.clientX - dragOffset.x)),
    y: Math.max(0, Math.min(window.innerHeight - 60, e.clientY - dragOffset.y)),
  }
}
function stopDragFn() {
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', stopDragFn)
}

async function copyLink() {
  await navigator.clipboard.writeText(joinUrl.value)
  snack.value = { show: true, text: 'Link copied to clipboard!', color: 'success' }
}

function downloadQr() {
  const canvas = document.querySelector('.qr-float-window canvas')
  if (!canvas) return
  const a = document.createElement('a')
  a.download = `join-${session.value?.joinCode}.png`
  a.href = canvas.toDataURL()
  a.click()
}

// ---- Transcript download ----
function downloadTranscript() {
  const sess = session.value
  const segs = segments.value
  if (!segs.length) return

  const lines = []
  lines.push(`Session: ${sess.title}`)
  lines.push(`Course:  ${sess.courseName}`)
  lines.push(`Date:    ${new Date(sess.startedAt ?? sess.createdAt).toLocaleDateString()}`)
  lines.push(`Languages: ${sess.sourceLanguage} → ${sess.targetLanguages.join(', ')}`)
  lines.push('─'.repeat(60))
  lines.push('')

  for (const seg of segs) {
    const t = Math.floor(seg.startOffsetMs / 1000)
    const ts = `${String(Math.floor(t / 60)).padStart(2, '0')}:${String(t % 60).padStart(2, '0')}`
    lines.push(`[${ts}] ${seg.originalText}`)
    for (const tr of seg.translations ?? []) {
      lines.push(`  [${tr.targetLanguage}] ${tr.translatedText}`)
    }
    lines.push('')
  }

  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `transcript-${sess.joinCode}-${new Date().toISOString().slice(0, 10)}.txt`
  a.click()
  snack.value = { show: true, text: 'Transcript downloaded!', color: 'success' }
}

// ---- Simulation ----
const { start: startSim, stop: stopSim } = useSimulatedTranscript(sessionId)

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

const micStatusClass = computed(() => {
  if (!sessionStore.micActive) return 'ring--idle'
  if (sessionStore.isMuted) return 'ring--muted'
  return 'ring--live'
})
const micIconColor = computed(() => {
  if (!sessionStore.micActive) return 'grey'
  return sessionStore.isMuted ? 'warning' : 'success'
})
const micIcon = computed(() => sessionStore.micActive && !sessionStore.isMuted ? 'mdi-microphone' : 'mdi-microphone-off')
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
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', stopDragFn)
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
  stopSim(); stopMicHw()
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

.session-sidebar { width: 280px; min-width: 260px; border-right: 1px solid rgba(0,0,0,0.07); background: #FAFAFA; display: flex; flex-direction: column; }
.dark-page .session-sidebar { background: #1A1A2E; border-right-color: rgba(255,255,255,0.07); }
.sidebar-scroll { flex: 1; overflow-y: auto; padding: 12px; }

.mic-status-ring { width: 72px; height: 72px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto; transition: all 0.3s; border: 3px solid transparent; }
.ring--idle { background: rgba(0,0,0,0.05); border-color: rgba(0,0,0,0.1); }
.ring--live { background: rgba(76,175,80,0.1); border-color: #4caf50; box-shadow: 0 0 0 6px rgba(76,175,80,0.15); animation: ring-pulse 1.5s ease infinite; }
.ring--muted { background: rgba(255,152,0,0.1); border-color: #ff9800; }
@keyframes ring-pulse { 0%,100% { box-shadow: 0 0 0 6px rgba(76,175,80,0.15); } 50% { box-shadow: 0 0 0 14px rgba(76,175,80,0.04); } }

.mic-btn-row { display: flex; gap: 8px; }

.transcript-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.teacher-caption-bar { padding: 14px 20px; background: rgba(21,101,192,0.04); border-bottom: 2px solid rgba(21,101,192,0.1); min-height: 84px; transition: all 0.3s; }
.active-bar { background: rgba(76,175,80,0.06); border-bottom-color: rgba(76,175,80,0.25); }
.speak-dot { width: 8px; height: 8px; border-radius: 50%; background: #ccc; transition: background 0.2s; }
.speak-dot--active { background: #4caf50; animation: pulse-dot 1s infinite; }
@keyframes pulse-dot { 0%,100%{opacity:1} 50%{opacity:0.3} }
.teacher-live-text { font-size: 17px; font-weight: 600; line-height: 1.5; color: #555; margin: 0; }
.teacher-live-text.text--active { color: #1565C0; }
.dark-page .teacher-live-text { color: #aaa; }
.dark-page .teacher-live-text.text--active { color: #42A5F5; }

.transcript-header { background: white; min-height: 44px; border-bottom: 1px solid rgba(0,0,0,0.07); }
.dark-page .transcript-header { background: #1E1E2E; }
.transcript-scroll { flex: 1; overflow: hidden; }
.info-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.live-dot-sm { display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: #4caf50; animation: pulse-dot 1.5s infinite; }

/* ===== FLOATING QR WINDOW ===== */
.qr-float-window {
  position: fixed;
  z-index: 500;
  width: 264px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.22), 0 2px 8px rgba(0,0,0,0.12);
  border: 1px solid rgba(0,0,0,0.08);
  overflow: hidden;
  cursor: move;
  user-select: none;
}
.dark-page .qr-float-window { background: #1E1E2E; border-color: rgba(255,255,255,0.1); }

.qr-title-bar {
  display: flex;
  align-items: center;
  padding: 6px 8px 6px 12px;
  background: rgba(21,101,192,0.06);
  border-bottom: 1px solid rgba(0,0,0,0.07);
  cursor: move;
}

.qr-float-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  gap: 4px;
}

.qr-canvas-wrap {
  padding: 10px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.join-code-chip {
  letter-spacing: 4px;
  font-size: 18px !important;
}

/* QR pop animation */
.qr-pop-enter-from { opacity: 0; transform: scale(0.85); }
.qr-pop-enter-active { transition: all 0.2s cubic-bezier(0.34,1.56,0.64,1); }
.qr-pop-leave-to { opacity: 0; transform: scale(0.85); }
.qr-pop-leave-active { transition: all 0.15s ease; }
</style>
