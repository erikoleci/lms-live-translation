<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useDisplay } from 'vuetify'
import QrcodeVue from 'qrcode.vue'
import { useSessionStore } from '../../stores/session.js'
import { useUiStore } from '../../stores/ui.js'
import { useSimulatedTranscript } from '../../composables/useSimulatedTranscript.js'
import { useSessionWs } from '../../composables/useSessionWs.js'
import { useBrowserStt } from '../../composables/useBrowserStt.js'
import SessionControls from '../../components/teacher/SessionControls.vue'
import StatusChip from '../../components/shared/StatusChip.vue'
import ConnectionStatus from '../../components/shared/ConnectionStatus.vue'
import LanguageBadge from '../../components/shared/LanguageBadge.vue'

const route = useRoute()
const { smAndDown } = useDisplay()
const sessionStore = useSessionStore()
const uiStore = useUiStore()
const sessionId = route.params.id
const captionDraft = ref('')
const {
  status: wsConnStatus,
  connect: connectWs,
  sendCaption,
  disconnect: disconnectWs,
} = useSessionWs(sessionId)
const browserStt = useBrowserStt()

const session = computed(() => sessionStore.getSession(sessionId))
const segments = computed(() => sessionStore.getTranscript(sessionId))
const actionLoading = ref(false)
const micLoading = ref(false)
const sidebarOpen = ref(true)
const micError = ref(null)
const sttError = ref(null)
const sttSupported = browserStt.isSupported()

const { start: startSimStt, stop: stopSimStt } = useSimulatedTranscript(sessionId)

let levelInterval = null
function startFakeLevel() {
  levelInterval = setInterval(() => {
    sessionStore.setAudioLevel(20 + Math.random() * 70)
  }, 300)
}
function stopFakeLevel() {
  if (levelInterval) {
    clearInterval(levelInterval)
    levelInterval = null
  }
}

const wsStatus = computed(() => {
  if (wsConnStatus.value === 'connected') return 'connected'
  if (wsConnStatus.value === 'connecting') return 'connecting'
  if (wsConnStatus.value === 'error') return 'error'
  return session.value?.status === 'ACTIVE' ? 'connected' : 'idle'
})

const qrFloat = ref(true)
const qrMinimized = ref(false)
const qrPos = ref({ x: Math.max(0, window.innerWidth - 290), y: 80 })
const qrSheetRef = ref(null)
const snack = ref({ show: false, text: '', color: 'success' })
const isLive = computed(() =>
  session.value?.status === 'ACTIVE' && sessionStore.micActive && !sessionStore.isMuted
)
const joinUrl = computed(() =>
  `${window.location.origin}${window.location.pathname}#/student/join/${session.value?.joinCode}`
)

let dragOffset = { x: 0, y: 0 }
function startDrag(e) {
  if (e.target.closest('button') || e.target.closest('.v-btn')) return
  dragOffset = { x: e.clientX - qrPos.value.x, y: e.clientY - qrPos.value.y }
  window.addEventListener('mousemove', onDrag)
  window.addEventListener('mouseup', endDrag)
}
function onDrag(e) {
  qrPos.value = { x: e.clientX - dragOffset.x, y: e.clientY - dragOffset.y }
}
function endDrag() {
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', endDrag)
}
function startTouchDrag(e) {
  const t = e.touches[0]
  dragOffset = { x: t.clientX - qrPos.value.x, y: t.clientY - qrPos.value.y }
  window.addEventListener('touchmove', onTouchDrag, { passive: false })
  window.addEventListener('touchend', endTouchDrag)
}
function onTouchDrag(e) {
  e.preventDefault()
  const t = e.touches[0]
  qrPos.value = { x: t.clientX - dragOffset.x, y: t.clientY - dragOffset.y }
}
function endTouchDrag() {
  window.removeEventListener('touchmove', onTouchDrag)
  window.removeEventListener('touchend', endTouchDrag)
}

async function copyLink() {
  await navigator.clipboard.writeText(joinUrl.value)
  snack.value = { show: true, text: 'Link copied!', color: 'success' }
}
function downloadQr() {
  const canvas = qrSheetRef.value?.$el?.querySelector('canvas')
  if (!canvas) return
  const a = document.createElement('a')
  a.download = `join-${session.value?.joinCode}.png`
  a.href = canvas.toDataURL()
  a.click()
}

function msToSrtTime(ms) {
  const h = Math.floor(ms / 3600000)
  const m = Math.floor((ms % 3600000) / 60000)
  const s = Math.floor((ms % 60000) / 1000)
  const ms2 = ms % 1000
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')},${String(ms2).padStart(3, '0')}`
}
function downloadTranscript(format = 'txt') {
  const sess = session.value
  const segs = segments.value
  if (!segs.length) return
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
    content = lines.join('\n')
    filename = `transcript-${sess.joinCode}.srt`
    mime = 'text/srt'
  } else {
    const lines = [
      `Session: ${sess.title}`,
      `Course: ${sess.courseName}`,
      `Languages: ${sess.sourceLanguage} → ${(sess.targetLanguages || []).join(', ')}`,
      '─'.repeat(60),
      ''
    ]
    for (const seg of segs) {
      const t = Math.floor(seg.startOffsetMs / 1000)
      lines.push(`[${String(Math.floor(t / 60)).padStart(2, '0')}:${String(t % 60).padStart(2, '0')}] ${seg.originalText}`)
      for (const tr of seg.translations ?? []) lines.push(`  [${tr.targetLanguage}] ${tr.translatedText}`)
      lines.push('')
    }
    content = lines.join('\n')
    filename = `transcript-${sess.joinCode}.txt`
    mime = 'text/plain'
  }
  const a = document.createElement('a')
  a.href = URL.createObjectURL(new Blob([content], { type: mime }))
  a.download = filename
  a.click()
  snack.value = { show: true, text: `Transcript downloaded as .${format}!`, color: 'success' }
}

function formatTime(ms) {
  const s = Math.floor(ms / 1000)
  return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`
}

const now = ref(Date.now())
let tickInterval = null
const liveText = computed(() => {
  if (!sessionStore.micActive || sessionStore.isMuted) return ''
  const all = segments.value
  return all.length ? all[all.length - 1].originalText ?? '' : ''
})
const duration = computed(() => {
  if (!session.value?.startedAt) return '—'
  const ms = now.value - new Date(session.value.startedAt).getTime()
  return `${Math.floor(ms / 60000)}m ${Math.floor((ms % 60000) / 1000)}s`
})
const micStatusTextClass = computed(() =>
  !sessionStore.micActive ? 'text-disabled' : sessionStore.isMuted ? 'text-warning' : 'text-success font-weight-bold'
)
const micStatusLabel = computed(() =>
  !sessionStore.micActive
    ? 'Microphone off'
    : sessionStore.isMuted
      ? 'Muted'
      : `Live — ${Math.round(sessionStore.audioLevel)}%`
)

function pushCaption(text) {
  if (!text) return
  const src = session.value?.sourceLanguage || 'IT'
  sendCaption({
    type: 'caption',
    text,
    isFinal: true,
    sourceLanguage: src,
    id: crypto.randomUUID(),
    ts: Date.now(),
  })
  if (typeof sessionStore.addTranscriptSegment === 'function') {
    sessionStore.addTranscriptSegment({
      id: crypto.randomUUID(),
      sessionId,
      sequenceNo: Date.now(),
      originalText: text,
      sourceLanguage: src,
      isFinal: true,
      startOffsetMs: 0,
      translations: [],
    })
  }
}

function sendDemoCaption() {
  const t = captionDraft.value.trim()
  if (!t) return
  const ok = sendCaption({
    type: 'caption',
    text: t,
    isFinal: true,
    sourceLanguage: session.value?.sourceLanguage || 'IT',
    id: crypto.randomUUID(),
    ts: Date.now(),
  })
  if (ok) {
    pushCaption(t)
    captionDraft.value = ''
    snack.value = { show: true, text: 'Caption u dergua', color: 'success' }
  } else {
    snack.value = { show: true, text: 'WebSocket jo i lidhur', color: 'error' }
  }
}

onMounted(async () => {
  tickInterval = setInterval(() => {
    now.value = Date.now()
  }, 1000)
  sessionStore.setActiveSession(sessionId)
  if (typeof sessionStore.fetchSession === 'function') {
    await sessionStore.fetchSession(sessionId)
  }
  connectWs()
})

onUnmounted(() => {
  clearInterval(tickInterval)
  stopFakeLevel()
  stopSimStt()
  browserStt.stop()
  disconnectWs()
  sessionStore.setActiveSession(null)
  endDrag()
})

watch(
  () => session.value?.status,
  (s) => {
    if (s === 'ENDED' || s === 'EXPIRED' || s === 'FAILED') {
      stopFakeLevel()
      stopSimStt()
      browserStt.stop()
    }
  }
)

async function startMic() {
  micLoading.value = true
  sttError.value = null
  sessionStore.setMicActive(true)
  startFakeLevel()

  const langMap = { IT: 'it-IT', EN: 'en-US', SQ: 'sq-AL' }
  const sttLang = langMap[session.value?.sourceLanguage] || 'it-IT'

  const ok = browserStt.start({
    lang: sttLang,
    onResult: ({ text, isFinal }) => {
      if (!text || !isFinal) return
      pushCaption(text)
    },
    onError: (err) => {
      console.warn('STT error', err)
      sttError.value = String(err)
      // fallback simulim nese browser STT deshton
      startSimStt()
    },
  })

  if (!ok) {
    sttError.value = 'Speech recognition not supported - using Chrome recommended'
    startSimStt()
  }

  if (session.value && !['ACTIVE', 'ENDED', 'EXPIRED', 'FAILED'].includes(session.value.status)) {
    await changeState('ACTIVE')
  }
  micLoading.value = false
}

function stopMic() {
  stopFakeLevel()
  stopSimStt()
  browserStt.stop()
  sessionStore.setMicActive(false)
  sessionStore.setAudioLevel(0)
  if (session.value && !['ENDED', 'EXPIRED', 'FAILED'].includes(session.value.status)) {
    changeState('ENDED')
  }
}

async function changeState(newState) {
  actionLoading.value = true
  try {
    await sessionStore.changeSessionState(sessionId, newState)
  } finally {
    actionLoading.value = false
  }
}

async function handleStart() {
  await changeState('ACTIVE')
}
async function handlePause() {
  await changeState('PAUSED')
  browserStt.stop()
}
async function handleResume() {
  await changeState('ACTIVE')
}
async function handleEnd() {
  stopFakeLevel()
  stopSimStt()
  browserStt.stop()
  await changeState('ENDED')
}
</script>
