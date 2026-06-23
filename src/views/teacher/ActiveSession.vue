<template>
  <v-layout v-if="session">

    <!-- APP BAR -->
    <v-app-bar flat border="b" height="56" density="compact">
      <v-app-bar-nav-icon v-if="smAndDown" @click="sidebarOpen = !sidebarOpen" />
      <v-btn v-else icon="mdi-arrow-left" variant="text" to="/teacher" />

      <v-avatar size="28" rounded="lg" class="ml-1 mr-2">
        <v-img src="/zana.png" alt="ZANA" cover />
      </v-avatar>

      <v-app-bar-title>
        <div class="d-flex align-center gap-2">
          <span class="text-body-1 font-weight-bold text-truncate">{{ session.title }}</span>
          <StatusChip :status="session.status" />
        </div>
        <div class="text-caption text-medium-emphasis d-none d-sm-block">{{ session.courseName }}</div>
      </v-app-bar-title>

      <template #append>
        <div class="d-flex align-center gap-1 pr-1">
          <span class="text-caption d-none d-sm-flex align-center gap-1 mr-1">
            <v-icon size="14" color="primary">mdi-account-group</v-icon>
            <strong>{{ session.participantCount }}</strong>
            <span class="text-disabled">/ {{ session.maxParticipants }}</span>
          </span>
          <ConnectionStatus :status="wsStatus" />
          <v-btn
            size="small" rounded="lg"
            :color="qrFloat ? 'primary' : 'default'"
            :variant="qrFloat ? 'flat' : 'tonal'"
            @click="qrFloat = !qrFloat"
          >
            <v-icon size="16" :start="!smAndDown">mdi-qrcode</v-icon>
            <span class="d-none d-sm-inline">QR</span>
          </v-btn>
          <v-menu>
            <template #activator="{ props: menuProps }">
              <v-btn
                size="small" variant="tonal" rounded="lg"
                :disabled="!segments.length"
                v-bind="menuProps"
              >
                <v-icon size="16" :start="!smAndDown">mdi-download</v-icon>
                <span class="d-none d-sm-inline">Save</span>
                <v-icon size="14" end>mdi-chevron-down</v-icon>
              </v-btn>
            </template>
            <v-list density="compact" rounded="lg">
              <v-list-item prepend-icon="mdi-file-document-outline" title="Export .txt" @click="downloadTranscript('txt')" />
              <v-list-item prepend-icon="mdi-subtitles-outline" title="Export .srt" @click="downloadTranscript('srt')" />
            </v-list>
          </v-menu>
          <v-btn
            :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
            variant="text" size="small"
            @click="uiStore.toggleDarkMode()"
          />
        </div>
      </template>
    </v-app-bar>

    <!-- SESSION CONTROLS DRAWER -->
    <v-navigation-drawer
      v-model="sidebarOpen"
      :permanent="!smAndDown"
      :temporary="smAndDown"
      width="272"
      border="e"
    >
      <v-container class="pa-3 d-flex flex-column gap-3">

        <!-- MIC -->
        <v-card rounded="xl" border flat>
          <v-card-title class="text-body-2 font-weight-bold px-3 pt-3 pb-1">
            <v-icon start size="16" color="primary">mdi-microphone</v-icon>Microphone
          </v-card-title>
          <v-card-text class="px-3 pb-3">
            <!-- Mic indicator -->
            <div class="d-flex justify-center mb-3">
              <v-progress-circular
                v-if="sessionStore.micActive && !sessionStore.isMuted"
                indeterminate color="success" size="72" width="3"
              >
                <v-icon color="success" size="28">mdi-microphone</v-icon>
              </v-progress-circular>
              <v-avatar
                v-else
                size="72" rounded="circle"
                :color="sessionStore.isMuted ? 'warning-lighten-4' : 'grey-lighten-3'"
              >
                <v-icon size="28" :color="sessionStore.isMuted ? 'warning' : 'grey'">
                  {{ sessionStore.isMuted ? 'mdi-microphone-off' : 'mdi-microphone' }}
                </v-icon>
              </v-avatar>
            </div>

            <p class="text-caption text-center mb-2" :class="micStatusTextClass">{{ micStatusLabel }}</p>

            <!-- Level bar -->
            <v-sheet color="grey-lighten-4" rounded="lg" class="pa-2 d-flex align-center gap-2 mb-3">
              <v-icon size="16" :color="sessionStore.micActive ? 'success' : 'grey'">mdi-microphone</v-icon>
              <v-progress-linear
                :model-value="sessionStore.micActive && !sessionStore.isMuted ? sessionStore.audioLevel : 0"
                :color="sessionStore.audioLevel > 75 ? 'warning' : 'success'"
                rounded height="8" class="flex-grow-1"
              />
              <span class="text-caption text-medium-emphasis" style="min-width:30px">{{ Math.round(sessionStore.audioLevel) }}%</span>
            </v-sheet>

            <!-- Mic buttons -->
            <div class="d-flex gap-2">
              <v-btn
                v-if="!sessionStore.micActive"
                color="success" variant="flat" size="large"
                class="flex-grow-1" prepend-icon="mdi-microphone"
                :disabled="session.status !== 'ACTIVE'"
                :loading="micLoading"
                @click="startMic"
              >Start</v-btn>
              <template v-else>
                <v-btn
                  :color="sessionStore.isMuted ? 'warning' : 'default'"
                  :variant="sessionStore.isMuted ? 'flat' : 'tonal'"
                  size="large" class="flex-grow-1"
                  :prepend-icon="sessionStore.isMuted ? 'mdi-microphone-off' : 'mdi-pause'"
                  @click="sessionStore.toggleMute()"
                >{{ sessionStore.isMuted ? 'Unmute' : 'Pause' }}</v-btn>
                <v-btn color="error" variant="tonal" size="large" icon="mdi-stop" @click="stopMic" />
              </template>
            </div>
            <v-alert v-if="micError" type="error" density="compact" variant="tonal" rounded="lg" class="mt-2 text-caption">{{ micError }}</v-alert>
          </v-card-text>
        </v-card>

        <!-- SESSION CONTROLS -->
        <v-card rounded="xl" border flat>
          <v-card-title class="text-body-2 font-weight-bold px-3 pt-3 pb-1">
            <v-icon start size="16" color="primary">mdi-remote</v-icon>Session
          </v-card-title>
          <v-card-text class="px-3 pb-3">
            <SessionControls
              :status="session.status"
              :mic-active="sessionStore.micActive"
              :muted="sessionStore.isMuted"
              :participant-count="session.participantCount"
              :loading="actionLoading"
              @start="handleStart" @pause="handlePause"
              @resume="handleResume" @end="handleEnd"
              @toggle-mic="() => {}"
            />
          </v-card-text>
        </v-card>

        <!-- INFO -->
        <v-card rounded="xl" border flat>
          <v-card-title class="text-body-2 font-weight-bold px-3 pt-3 pb-1">
            <v-icon start size="16" color="primary">mdi-information-outline</v-icon>Info
          </v-card-title>
          <v-card-text class="px-3 pb-3">
            <v-list density="compact" nav class="pa-0">
              <v-list-item class="px-0">
                <template #prepend><span class="text-caption text-medium-emphasis mr-2" style="min-width:56px">Source</span></template>
                <LanguageBadge :lang="session.sourceLanguage" />
              </v-list-item>
              <v-list-item class="px-0">
                <template #prepend><span class="text-caption text-medium-emphasis mr-2" style="min-width:56px">Targets</span></template>
                <div class="d-flex gap-1 flex-wrap">
                  <LanguageBadge v-for="l in session.targetLanguages" :key="l" :lang="l" />
                </div>
              </v-list-item>
              <v-list-item class="px-0">
                <template #prepend><span class="text-caption text-medium-emphasis mr-2" style="min-width:56px">Duration</span></template>
                <span class="text-caption font-weight-bold">{{ duration }}</span>
              </v-list-item>
              <v-list-item class="px-0">
                <template #prepend><span class="text-caption text-medium-emphasis mr-2" style="min-width:56px">Code</span></template>
                <v-chip size="small" color="primary" variant="tonal" style="letter-spacing:2px" class="font-weight-bold">{{ session.joinCode }}</v-chip>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>

      </v-container>
    </v-navigation-drawer>

    <!-- MAIN AREA -->
    <v-main class="d-flex flex-column overflow-hidden" style="height:100vh">

      <!-- Live speech bar -->
      <v-sheet
        rounded="0" border="b"
        :color="isLive ? 'success-lighten-5' : undefined"
        class="pa-3 pa-sm-4 flex-shrink-0"
        style="min-height:80px"
      >
        <div class="d-flex align-center gap-2 mb-1">
          <v-badge
            :color="isLive ? 'success' : 'grey'"
            dot inline
          />
          <span class="text-caption font-weight-bold text-uppercase" style="letter-spacing:0.6px">
            {{ isLive ? 'You are speaking…' : 'Your speech will appear here' }}
          </span>
          <v-spacer />
          <LanguageBadge :lang="session.sourceLanguage" />
        </div>
        <p class="text-body-1 font-weight-semibold" :class="isLive ? 'text-primary' : 'text-medium-emphasis'">
          {{ liveText || (session.status === 'ACTIVE' ? 'Start your microphone to begin transcription…' : 'Start the session first.') }}
        </p>
      </v-sheet>

      <!-- Transcript toolbar -->
      <v-toolbar flat border="b" density="compact" height="44">
        <v-icon size="15" color="primary" class="ml-3">mdi-text-recognition</v-icon>
        <v-toolbar-title class="text-body-2 font-weight-bold ml-2">Live Transcript</v-toolbar-title>
        <v-chip v-if="session.status === 'ACTIVE'" size="x-small" color="success" variant="tonal" class="ml-1">
          <v-badge dot color="success" inline class="mr-1" />LIVE
        </v-chip>
        <v-spacer />
        <span class="text-caption text-disabled mr-2">{{ segments.length }} segments</span>
        <v-menu>
          <template #activator="{ props: menuProps }">
            <v-btn icon="mdi-download" size="x-small" variant="text" :disabled="!segments.length" v-bind="menuProps" class="mr-1" />
          </template>
          <v-list density="compact" rounded="lg">
            <v-list-item prepend-icon="mdi-file-document-outline" title="Export .txt" @click="downloadTranscript('txt')" />
            <v-list-item prepend-icon="mdi-subtitles-outline" title="Export .srt" @click="downloadTranscript('srt')" />
          </v-list>
        </v-menu>
      </v-toolbar>

      <!-- Transcript list -->
      <v-sheet class="flex-grow-1 overflow-y-auto pa-4 d-flex flex-column gap-4">
        <div v-if="!segments.length" class="d-flex flex-column align-center justify-center flex-grow-1 opacity-60 py-16">
          <v-icon size="48" color="grey-lighten-2">mdi-text-recognition</v-icon>
          <p class="text-body-2 text-disabled mt-2">Transcript will appear here once the session starts…</p>
        </div>
        <v-sheet
          v-for="seg in segments" :key="seg.id"
          rounded="lg"
          :color="seg.isFinal ? undefined : 'orange-lighten-5'"
          class="pa-3"
          :style="`border-left: 3px solid ${seg.isFinal ? '#1565C0' : '#ff9800'}`"
          border
        >
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon size="12" :color="seg.isFinal ? 'success' : 'warning'">{{ seg.isFinal ? 'mdi-check-circle' : 'mdi-loading mdi-spin' }}</v-icon>
            <span class="text-caption text-disabled">#{{ seg.sequenceNo }}</span>
            <span class="text-caption text-disabled">{{ formatTime(seg.startOffsetMs) }}</span>
            <v-chip v-if="seg.confidence" size="x-small" variant="text">{{ Math.round((seg.confidence ?? 0) * 100) }}%</v-chip>
          </div>
          <p class="text-body-2 font-weight-medium ma-0">{{ seg.originalText }}</p>
          <div v-if="seg.translations?.length" class="d-flex flex-column gap-1 mt-2">
            <div v-for="tr in seg.translations" :key="tr.id" class="d-flex align-center gap-2">
              <LanguageBadge :lang="tr.targetLanguage" />
              <span class="text-caption text-medium-emphasis font-italic">{{ tr.translatedText }}</span>
            </div>
          </div>
        </v-sheet>
      </v-sheet>
    </v-main>

    <!-- FLOATING QR WINDOW -->
    <v-card
      v-if="qrFloat"
      rounded="xl"
      elevation="8"
      width="256"
      :style="`position:fixed; top:${qrPos.y}px; left:${qrPos.x}px; z-index:500; cursor:move; user-select:none`"
      @mousedown="startDrag"
      @touchstart.prevent="startTouchDrag"
    >
      <v-toolbar flat density="compact" color="primary-lighten-5" border="b" rounded="t-xl">
        <v-icon size="14" color="primary" class="ml-2">mdi-qrcode</v-icon>
        <v-toolbar-title class="text-caption font-weight-bold ml-1">Join — {{ session.joinCode }}</v-toolbar-title>
        <v-btn icon="mdi-minus" size="x-small" variant="text" @click.stop="qrMinimized = !qrMinimized" />
        <v-btn icon="mdi-close" size="x-small" variant="text" @click.stop="qrFloat = false" class="mr-1" />
      </v-toolbar>

      <v-card-text v-if="!qrMinimized" class="d-flex flex-column align-center pa-4 gap-3">
        <v-sheet ref="qrSheetRef" rounded="lg" border class="pa-2 bg-white">
          <qrcode-vue :value="joinUrl" :size="180" level="H" :margin="2" />
        </v-sheet>
        <v-chip color="primary" variant="flat" size="large" class="font-weight-bold" style="letter-spacing:4px;font-size:16px">
          {{ session.joinCode }}
        </v-chip>
        <div class="d-flex gap-2">
          <v-btn size="small" variant="tonal" color="primary" prepend-icon="mdi-content-copy" @click.stop="copyLink">Copy</v-btn>
          <v-btn size="small" variant="tonal" color="secondary" prepend-icon="mdi-download" @click.stop="downloadQr">Save</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <!-- Snackbar -->
    <v-snackbar v-model="snack.show" :color="snack.color" timeout="2500" location="bottom">
      <v-icon start>mdi-check-circle</v-icon>{{ snack.text }}
    </v-snackbar>

  </v-layout>

  <v-sheet v-else class="d-flex align-center justify-center" style="height:100vh">
    <div class="text-center pa-6">
      <v-icon size="64" color="grey-lighten-2">mdi-video-off</v-icon>
      <h2 class="text-h6 mt-4">Session not found</h2>
      <v-btn to="/teacher" class="mt-4" variant="tonal" rounded="lg">Back to Dashboard</v-btn>
    </div>
  </v-sheet>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useDisplay } from 'vuetify'
import QrcodeVue from 'qrcode.vue'
import { useSessionStore } from '../../stores/session.js'
import { useUiStore } from '../../stores/ui.js'
import { useAudio } from '../../composables/useAudio.js'
import { useSimulatedTranscript } from '../../composables/useSimulatedTranscript.js'
import { useWebSocket } from '../../composables/useWebSocket.js'
import SessionControls from '../../components/teacher/SessionControls.vue'
import StatusChip from '../../components/shared/StatusChip.vue'
import ConnectionStatus from '../../components/shared/ConnectionStatus.vue'
import LanguageBadge from '../../components/shared/LanguageBadge.vue'

const route = useRoute()
const { smAndDown } = useDisplay()
const sessionStore = useSessionStore()
const uiStore = useUiStore()
const { requestMic, stopMic: stopMicHw, error: micError } = useAudio()

const sessionId = route.params.id
const session = computed(() => sessionStore.getSession(sessionId))
const segments = computed(() => sessionStore.getTranscript(sessionId))
const actionLoading = ref(false)
const micLoading = ref(false)
const sidebarOpen = ref(true)

const wsUrl = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws/session/${sessionId}`
const { status: wsStatus, connect: wsConnect, disconnect: wsDisconnect, send: wsSend } = useWebSocket(wsUrl)

const qrFloat = ref(true)
const qrMinimized = ref(false)
const qrPos = ref({ x: Math.max(0, window.innerWidth - 290), y: 80 })
const qrSheetRef = ref(null)
const snack = ref({ show: false, text: '', color: 'success' })

const isLive = computed(() => session.value?.status === 'ACTIVE' && sessionStore.micActive && !sessionStore.isMuted)
const joinUrl = computed(() => `${window.location.origin}${window.location.pathname}#/student/join/${session.value?.joinCode}`)

// Drag
let dragOffset = { x: 0, y: 0 }
function startDrag(e) {
  if (e.target.closest('button') || e.target.closest('.v-btn')) return
  dragOffset = { x: e.clientX - qrPos.value.x, y: e.clientY - qrPos.value.y }
  window.addEventListener('mousemove', onDrag)
  window.addEventListener('mouseup', endDrag)
}
function onDrag(e) { qrPos.value = { x: e.clientX - dragOffset.x, y: e.clientY - dragOffset.y } }
function endDrag() { window.removeEventListener('mousemove', onDrag); window.removeEventListener('mouseup', endDrag) }
function startTouchDrag(e) {
  const t = e.touches[0]
  dragOffset = { x: t.clientX - qrPos.value.x, y: t.clientY - qrPos.value.y }
  window.addEventListener('touchmove', onTouchDrag, { passive: false })
  window.addEventListener('touchend', endTouchDrag)
}
function onTouchDrag(e) { e.preventDefault(); const t = e.touches[0]; qrPos.value = { x: t.clientX - dragOffset.x, y: t.clientY - dragOffset.y } }
function endTouchDrag() { window.removeEventListener('touchmove', onTouchDrag); window.removeEventListener('touchend', endTouchDrag) }

async function copyLink() { await navigator.clipboard.writeText(joinUrl.value); snack.value = { show: true, text: 'Link copied!', color: 'success' } }
function downloadQr() {
  const canvas = qrSheetRef.value?.$el?.querySelector('canvas')
  if (!canvas) return
  const a = document.createElement('a'); a.download = `join-${session.value?.joinCode}.png`; a.href = canvas.toDataURL(); a.click()
}

function msToSrtTime(ms) {
  const h = Math.floor(ms / 3600000)
  const m = Math.floor((ms % 3600000) / 60000)
  const s = Math.floor((ms % 60000) / 1000)
  const ms2 = ms % 1000
  return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')},${String(ms2).padStart(3,'0')}`
}

function downloadTranscript(format = 'txt') {
  const sess = session.value; const segs = segments.value; if (!segs.length) return
  let content, filename, mime

  if (format === 'srt') {
    const lines = []
    segs.forEach((seg, i) => {
      lines.push(String(i + 1))
      lines.push(`${msToSrtTime(seg.startOffsetMs)} --> ${msToSrtTime(seg.endOffsetMs ?? seg.startOffsetMs + 4000)}`)
      lines.push(seg.originalText)
      for (const tr of seg.translations ?? []) lines.push(`[${tr.targetLanguage}] ${tr.translatedText}`)
      lines.push('')
    })
    content = lines.join('\n'); filename = `transcript-${sess.joinCode}.srt`; mime = 'text/srt'
  } else {
    const lines = [`Session: ${sess.title}`, `Course: ${sess.courseName}`, `Languages: ${sess.sourceLanguage} → ${sess.targetLanguages.join(', ')}`, '─'.repeat(60), '']
    for (const seg of segs) {
      const t = Math.floor(seg.startOffsetMs / 1000)
      lines.push(`[${String(Math.floor(t / 60)).padStart(2, '0')}:${String(t % 60).padStart(2, '0')}] ${seg.originalText}`)
      for (const tr of seg.translations ?? []) lines.push(`  [${tr.targetLanguage}] ${tr.translatedText}`)
      lines.push('')
    }
    content = lines.join('\n'); filename = `transcript-${sess.joinCode}.txt`; mime = 'text/plain'
  }

  const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([content], { type: mime }))
  a.download = filename; a.click()
  snack.value = { show: true, text: `Transcript downloaded as .${format}!`, color: 'success' }
}

function formatTime(ms) {
  const s = Math.floor(ms / 1000); return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`
}

const { start: startSim, stop: stopSim } = useSimulatedTranscript(sessionId)

const now = ref(Date.now())
let tickInterval = null
onMounted(() => { tickInterval = setInterval(() => { now.value = Date.now() }, 1000) })
onUnmounted(() => clearInterval(tickInterval))

const liveText = computed(() => {
  if (!sessionStore.micActive || sessionStore.isMuted) return ''
  const all = segments.value; return all.length ? all[all.length - 1].originalText ?? '' : ''
})

const duration = computed(() => {
  if (!session.value?.startedAt) return '—'
  const ms = now.value - new Date(session.value.startedAt).getTime()
  return `${Math.floor(ms / 60000)}m ${Math.floor((ms % 60000) / 1000)}s`
})

const micStatusTextClass = computed(() => !sessionStore.micActive ? 'text-disabled' : sessionStore.isMuted ? 'text-warning' : 'text-success font-weight-bold')
const micStatusLabel = computed(() => !sessionStore.micActive ? 'Microphone off' : sessionStore.isMuted ? 'Muted' : `Live — ${Math.round(sessionStore.audioLevel)}%`)

onMounted(() => {
  sessionStore.setActiveSession(sessionId)
  if (session.value?.status === 'ACTIVE') { wsConnect(); startSim() }
})
onUnmounted(() => { stopSim(); stopMicHw(); wsDisconnect(); sessionStore.setActiveSession(null); endDrag() })
watch(() => session.value?.status, (s) => {
  if (s === 'ACTIVE') { wsConnect(); startSim() }
  else if (s === 'PAUSED') { stopSim() }
  else if (s === 'ENDED') { wsDisconnect(); stopSim(); stopMicHw() }
})

async function startMic() { micLoading.value = true; await requestMic(); micLoading.value = false }
function stopMic() { stopMicHw(); sessionStore.setMicActive(false); sessionStore.setAudioLevel(0) }
async function handleStart() { actionLoading.value = true; await new Promise(r => setTimeout(r, 800)); sessionStore.updateSessionStatus(sessionId, 'ACTIVE'); wsStatus.value = 'CONNECTED'; actionLoading.value = false }
async function handlePause() { actionLoading.value = true; await new Promise(r => setTimeout(r, 400)); sessionStore.updateSessionStatus(sessionId, 'PAUSED'); actionLoading.value = false }
async function handleResume() { actionLoading.value = true; await new Promise(r => setTimeout(r, 400)); sessionStore.updateSessionStatus(sessionId, 'ACTIVE'); actionLoading.value = false }
async function handleEnd() { actionLoading.value = true; stopSim(); stopMicHw(); await new Promise(r => setTimeout(r, 600)); sessionStore.updateSessionStatus(sessionId, 'ENDED'); wsStatus.value = 'DISCONNECTED'; actionLoading.value = false }
</script>
