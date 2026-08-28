<template>
  <v-layout style="height:100vh; overflow:hidden" :class="{ 'zana-high-contrast': uiStore.highContrastMode }">
    <v-app-bar flat border="b" height="60" density="compact" color="surface">
      <div class="d-flex align-center gap-2 ml-3">
        <v-avatar size="32" rounded="lg">
          <v-img src="/zana.png" alt="ZANA" cover />
        </v-avatar>
        <div>
          <div class="text-body-2 font-weight-bold">{{ session?.title ?? 'Sesion Live' }}</div>
          <div class="d-flex align-center gap-1">
            <StatusChip v-if="session" :status="session.status" />
            <ConnectionStatus :status="wsStatus" />
          </div>
        </div>
      </div>
      <template #append>
        <div class="d-flex align-center gap-1 pr-2">
          <div class="d-flex gap-1">
            <v-btn
              v-for="lang in availableLangs"
              :key="lang.value"
              :color="selectedLanguage === lang.value ? 'primary' : 'default'"
              :variant="selectedLanguage === lang.value ? 'flat' : 'text'"
              size="small" rounded="lg" class="px-2"
              @click="selectedLanguage = lang.value"
            >
              <span class="text-caption font-weight-bold">{{ lang.shortLabel }}</span>
            </v-btn>
          </div>
          <v-btn icon="mdi-exit-to-app" variant="tonal" size="small" color="error" @click="leave" />
        </div>
      </template>
    </v-app-bar>

    <v-main>
      <v-sheet :color="uiStore.darkMode ? '#121212' : '#F5F5F5'" class="d-flex flex-column" style="height:100%">
        <div v-if="loading" class="d-flex flex-column align-center justify-center flex-grow-1 gap-4">
          <v-progress-circular indeterminate color="primary" size="56" width="4" />
          <p class="text-body-1 font-weight-bold">Duke ngarkuar sesionin...</p>
        </div>

        <div v-else-if="!segments.length" class="d-flex flex-column align-center justify-center flex-grow-1 gap-4">
          <v-progress-circular indeterminate color="primary" size="56" width="4" />
          <div class="text-center">
            <p class="text-body-1 font-weight-bold mb-1">Duke pritur mesuesin...</p>
            <p class="text-body-2 text-medium-emphasis">Caption-et do te shfaqen ketu</p>
          </div>
          <v-chip color="primary" variant="flat" size="large">{{ selectedLanguage }}</v-chip>
        </div>

        <div v-else class="flex-grow-1 overflow-hidden d-flex flex-column justify-end pa-4 pa-sm-6">
          <div v-for="seg in visibleSegments" :key="seg.id" class="mb-3">
            <v-sheet
              rounded="2xl"
              :color="uiStore.darkMode ? '#1E1E1E' : 'white'"
              style="border-left: 4px solid #1565C0"
              class="pa-4"
              elevation="2"
            >
              <p class="ma-0 font-weight-medium" :style="`font-size: ${uiStore.captionFontSize || 20}px; line-height: 1.6`">
                {{ displayText(seg) }}
              </p>
            </v-sheet>
          </div>
        </div>

        <div class="d-flex align-center gap-3 px-4 py-2" style="border-top: 1px solid rgba(0,0,0,0.08)">
          <v-chip size="small" variant="tonal">{{ selectedLanguage }}</v-chip>
          <v-spacer />
          <span class="text-caption text-medium-emphasis">{{ segments.length }} segmente</span>
        </div>
      </v-sheet>
    </v-main>
  </v-layout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUiStore } from '../../stores/ui.js'
import { useSessionWs } from '../../composables/useSessionWs.js'
import StatusChip from '../../components/shared/StatusChip.vue'
import ConnectionStatus from '../../components/shared/ConnectionStatus.vue'

const API_BASE = 'http://localhost:8080'
const route = useRoute()
const router = useRouter()
const uiStore = useUiStore()

const sessionId = route.params.id
const loading = ref(true)
const session = ref(null)
const localSegments = ref([])
const selectedLanguage = ref('SQ')

const segments = computed(() => localSegments.value)
const visibleSegments = computed(() => segments.value.slice(-6))

const { status: wsConnStatus, connect, disconnect } = useSessionWs(sessionId)
const wsStatus = computed(() => {
  if (wsConnStatus.value === 'connected') return 'connected'
  if (wsConnStatus.value === 'connecting') return 'connecting'
  if (wsConnStatus.value === 'error') return 'error'
  return 'idle'
})

const availableLangs = [
  { value: 'SQ', shortLabel: 'SHQ' },
  { value: 'EN', shortLabel: 'ENG' },
  { value: 'IT', shortLabel: 'ITA' },
]

function displayText(seg) {
  if (selectedLanguage.value === seg.sourceLanguage) return seg.originalText
  const tr = seg.translations?.find(t => t.targetLanguage === selectedLanguage.value)
  return tr?.translatedText ?? seg.originalText
}

function leave() {
  sessionStorage.removeItem('participant')
  disconnect()
  router.push('/student/join')
}

onMounted(async () => {
  loading.value = true
  try {
    const raw = sessionStorage.getItem('participant')
    const joinData = raw ? JSON.parse(raw) : null
    if (joinData?.targetLanguage) selectedLanguage.value = joinData.targetLanguage

    const res = await fetch(`${API_BASE}/api/sessions/${sessionId}`)
    if (!res.ok) {
      router.push('/student/join')
      return
    }
    const data = await res.json()
    session.value = {
      ...data,
      participantCount: data.currentParticipantCount ?? 0,
    }

    connect((msg) => {
      const text = msg.text || msg.originalText || ''
      if (!text) return
      localSegments.value.push({
        id: msg.id || crypto.randomUUID(),
        originalText: text,
        sourceLanguage: msg.sourceLanguage || session.value?.sourceLanguage || 'IT',
        isFinal: msg.isFinal !== false,
        translations: msg.translations || [],
      })
    })
  } catch (e) {
    console.error(e)
    router.push('/student/join')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => disconnect())
</script>
