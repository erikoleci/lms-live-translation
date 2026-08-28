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
          <div class="d-flex gap-1" role="group" aria-label="Zgjedh gjuhën e shfaqjes">
            <v-btn
              v-for="lang in availableLangs"
              :key="lang.value"
              :color="selectedLanguage === lang.value ? 'primary' : 'default'"
              :variant="selectedLanguage === lang.value ? 'flat' : 'text'"
              size="small"
              rounded="lg"
              class="px-2"
              :aria-label="`Shfaq caption-et në ${lang.shortLabel}`"
              :aria-pressed="selectedLanguage === lang.value"
              @click="selectedLanguage = lang.value"
            >
              <span style="font-size:16px" aria-hidden="true">{{ lang.flag }}</span>
              <span class="d-none d-sm-inline ml-1 text-caption font-weight-bold">{{ lang.shortLabel }}</span>
            </v-btn>
          </div>
          <v-divider vertical class="mx-1" style="height:24px" />
          <v-btn icon="mdi-format-font-size-decrease" size="small" variant="text"
            aria-label="Zvogëlo madhësinë e shkrimit"
            @click="uiStore.setCaptionFontSize(uiStore.captionFontSize - 2)" />
          <v-btn icon="mdi-format-font-size-increase" size="small" variant="text"
            aria-label="Zmadho madhësinë e shkrimit"
            @click="uiStore.setCaptionFontSize(uiStore.captionFontSize + 2)" />
          <v-btn
            :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
            variant="text" size="small" @click="uiStore.toggleDarkMode()"
            :aria-label="uiStore.darkMode ? 'Kalo në temë të çelët' : 'Kalo në temë të errët'" />
          <v-btn icon="mdi-contrast-circle" variant="text" size="small"
            @click="uiStore.toggleHighContrast()"
            :aria-label="uiStore.highContrastMode ? 'Çaktivizo kontrastin e lartë' : 'Aktivizo kontrastin e lartë'" />
          <v-btn
            v-if="session?.studentTranscriptDownloadEnabled"
            icon="mdi-download" variant="text" size="small"
            aria-label="Shkarko transkriptin (.txt)"
            @click="downloadTranscript" />
          <v-btn icon="mdi-cog-outline" variant="tonal" size="small" @click="showSettings = !showSettings"
            aria-label="Hap cilësimet" />
          <v-btn icon="mdi-exit-to-app" variant="tonal" size="small" color="error" @click="leave"
            aria-label="Largohu nga sesioni" />
        </div>
      </template>
    </v-app-bar>

    <v-navigation-drawer v-if="!smAndDown" v-model="showSettings" temporary location="right" width="300">
      <v-list-item class="py-4">
        <v-list-item-title class="text-body-1 font-weight-bold">Cilësimet</v-list-item-title>
        <template #append>
          <v-btn icon="mdi-close" size="small" variant="text" @click="showSettings = false" aria-label="Mbyll cilësimet" />
        </template>
      </v-list-item>
      <v-divider />
      <SettingsPanelContent
        v-model:selected-language="selectedLanguage"
        v-model:show-original="showOriginal"
        v-model:audio-enabled="audioEnabled"
        v-model:selected-voice="selectedVoice"
        :available-voices="[]"
        :participants="participants"
        :participant-count="session?.participantCount ?? 0"
        :caption-font-size="uiStore.captionFontSize"
        @update:caption-font-size="uiStore.setCaptionFontSize($event)"
      />
    </v-navigation-drawer>

    <v-main>
      <v-sheet
        :color="uiStore.darkMode ? '#121212' : '#F5F5F5'"
        class="d-flex flex-column"
        style="height:100%; position:relative"
      >
        <div v-if="loading" class="d-flex flex-column align-center justify-center flex-grow-1 gap-4">
          <v-progress-circular indeterminate color="primary" size="56" width="4" />
          <p class="text-body-1 font-weight-bold">Duke ngarkuar sesionin…</p>
        </div>

        <div v-else-if="!segments.length" class="d-flex flex-column align-center justify-center flex-grow-1 gap-4">
          <v-progress-circular indeterminate color="primary" size="56" width="4" />
          <div class="text-center">
            <p class="text-body-1 font-weight-bold mb-1">Duke pritur mësuesin…</p>
            <p class="text-body-2 text-medium-emphasis">Caption-et do të shfaqen këtu automatikisht</p>
          </div>
          <v-chip :color="langColor" variant="flat" prepend-icon="mdi-translate" size="large" class="mt-2">
            Duke parë: {{ currentLangLabel }}
          </v-chip>
        </div>

        <div v-else ref="captionContainer"
          class="flex-grow-1 overflow-hidden d-flex flex-column justify-end pa-4 pa-sm-6 pa-md-8">
          <v-slide-y-reverse-transition group leave-absolute>
            <div v-for="seg in visibleSegments" :key="seg.id" class="mb-3">
              <div v-if="!seg.isFinal" class="d-flex align-center gap-1 mb-1">
                <v-progress-circular indeterminate size="10" width="2" color="orange" />
                <span class="text-caption text-orange">Duke transkriptuar…</span>
              </div>
              <v-sheet
                rounded="2xl"
                :color="uiStore.darkMode ? '#1E1E1E' : 'white'"
                :style="`border-left: 4px solid ${seg.isFinal ? '#1565C0' : '#FF9800'}; opacity: ${seg.isFinal ? 1 : 0.85}`"
                class="pa-4 pa-sm-5"
                elevation="2"
              >
                <p class="ma-0 font-weight-medium"
                  :class="uiStore.darkMode ? 'text-white' : ''"
                  :style="`font-size: ${uiStore.captionFontSize}px; line-height: 1.6`">
                  {{ displayText(seg) }}
                </p>
                <div v-if="showOriginal && selectedLanguage !== seg.sourceLanguage"
                  class="d-flex align-center gap-1 mt-2 pt-2" style="border-top: 1px solid rgba(0,0,0,0.08)">
                  <v-icon size="12" color="grey">mdi-translate</v-icon>
                  <span class="text-caption text-medium-emphasis font-italic">{{ seg.originalText }}</span>
                </div>
              </v-sheet>
            </div>
          </v-slide-y-reverse-transition>
        </div>

        <div class="d-flex align-center gap-3 px-4 py-2 flex-shrink-0"
          style="border-top: 1px solid rgba(0,0,0,0.08)">
          <v-chip :color="langColor" size="small" variant="tonal">
            <span style="font-size:14px" class="mr-1">{{ currentLangFlag }}</span>
            {{ currentLangLabel }}
          </v-chip>
          <v-chip v-if="audioEnabled" color="success" size="small" variant="tonal" prepend-icon="mdi-volume-high">
            Audio ON
          </v-chip>
          <v-spacer />
          <span class="text-caption text-medium-emphasis">{{ segments.length }} segmente</span>
        </div>
      </v-sheet>
    </v-main>

    <v-bottom-sheet v-if="smAndDown" v-model="showSettings">
      <v-sheet rounded="t-xl" max-height="80vh" class="overflow-y-auto">
        <v-list-item class="py-4">
          <v-list-item-title class="text-body-1 font-weight-bold">Cilësimet</v-list-item-title>
          <template #append>
            <v-btn icon="mdi-close" size="small" variant="text" @click="showSettings = false" aria-label="Mbyll cilësimet" />
          </template>
        </v-list-item>
        <v-divider />
        <SettingsPanelContent
          v-model:selected-language="selectedLanguage"
          v-model:show-original="showOriginal"
          v-model:audio-enabled="audioEnabled"
          v-model:selected-voice="selectedVoice"
          :available-voices="[]"
          :participants="participants"
          :participant-count="session?.participantCount ?? 0"
          :caption-font-size="uiStore.captionFontSize"
          @update:caption-font-size="uiStore.setCaptionFontSize($event)"
        />
      </v-sheet>
    </v-bottom-sheet>
  </v-layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useSessionStore } from '../../stores/session.js'
import { useParticipantStore } from '../../stores/participant.js'
import { useUiStore } from '../../stores/ui.js'
import { useBrowserTts } from '../../composables/useBrowserTts.js'
import SettingsPanelContent from '../../components/student/SettingsPanelContent.vue'
import StatusChip from '../../components/shared/StatusChip.vue'
import ConnectionStatus from '../../components/shared/ConnectionStatus.vue'

const API_BASE = 'http://localhost:8080'

const route = useRoute()
const router = useRouter()
const { smAndDown } = useDisplay()
const sessionStore = useSessionStore()
const participantStore = useParticipantStore()
const uiStore = useUiStore()

const sessionId = route.params.id
const loading = ref(true)
const session = ref(null)

const segments = computed(() => {
  if (typeof sessionStore.getTranscript === 'function') {
    return sessionStore.getTranscript(sessionId) || []
  }
  return []
})
const visibleSegments = computed(() => segments.value.slice(-4))
const participants = computed(() => {
  if (typeof participantStore.getParticipantsForSession === 'function') {
    return participantStore.getParticipantsForSession(sessionId) || []
  }
  return []
})

const selectedLanguage = ref('SQ')
const showOriginal = ref(false)
const showSettings = ref(false)
const audioEnabled = ref(false)
const selectedVoice = ref('')

const wsStatus = computed(() => (session.value?.status === 'ACTIVE' ? 'connected' : 'idle'))

const availableLangs = [
  { value: 'SQ', shortLabel: 'SHQ', flag: '🇦🇱' },
  { value: 'EN', shortLabel: 'ENG', flag: '🇬🇧' },
  { value: 'IT', shortLabel: 'ITA', flag: '🇮🇹' },
]
const langMeta = {
  SQ: { label: 'Shqip', flag: '🇦🇱', color: 'red' },
  EN: { label: 'English', flag: '🇬🇧', color: 'blue' },
  IT: { label: 'Italiano', flag: '🇮🇹', color: 'green' },
}
const currentLangLabel = computed(() => langMeta[selectedLanguage.value]?.label ?? selectedLanguage.value)
const currentLangFlag = computed(() => langMeta[selectedLanguage.value]?.flag ?? '')
const langColor = computed(() => langMeta[selectedLanguage.value]?.color ?? 'primary')

function displayText(seg) {
  if (selectedLanguage.value === seg.sourceLanguage) return seg.originalText
  const tr = seg.translations?.find(t => t.targetLanguage === selectedLanguage.value)
  return tr?.translatedText ?? seg.originalText
}

const { speak: speakTts } = useBrowserTts()
let lastSpokenSegmentId = null
watch(segments, (segs) => {
  if (!audioEnabled.value || !segs.length) return
  const last = segs[segs.length - 1]
  if (last.id === lastSpokenSegmentId) return
  const translation = last.translations?.find(t => t.targetLanguage === selectedLanguage.value && t.isFinal)
  if (!translation) return
  lastSpokenSegmentId = last.id
  speakTts(translation.translatedText, selectedLanguage.value)
}, { deep: true })

function leave() {
  sessionStorage.removeItem('participant')
  router.push('/student/join')
}

function downloadTranscript() {
  if (!session.value?.studentTranscriptDownloadEnabled) return
  const lines = segments.value
    .map((seg) => {
      const tr = seg.translations?.find((t) => t.targetLanguage === selectedLanguage.value)
      const translated = tr ? ` → ${tr.translatedText}` : ''
      return `[${seg.sourceLanguage}] ${seg.originalText}${translated}`
    })
    .join('\n')
  const a = document.createElement('a')
  a.href = URL.createObjectURL(new Blob([lines], { type: 'text/plain' }))
  a.download = `transcript-${sessionId}.txt`
  a.click()
}

onMounted(async () => {
  loading.value = true
  try {
    const raw = sessionStorage.getItem('participant')
    const joinData = raw ? JSON.parse(raw) : null

    if (joinData?.targetLanguage) {
      selectedLanguage.value = joinData.targetLanguage
    }
    if (joinData?.audioEnabled != null) {
      audioEnabled.value = joinData.audioEnabled
    }

    const res = await fetch(`${API_BASE}/api/sessions/${sessionId}`)
    if (!res.ok) {
      router.push('/student/join')
      return
    }
    const data = await res.json()
    session.value = {
      ...data,
      participantCount: data.currentParticipantCount ?? 0,
      courseName: data.courseId || '',
    }

    if (typeof sessionStore.setActiveSession === 'function') {
      sessionStore.setActiveSession(sessionId)
    }
  } catch (e) {
    console.error(e)
    router.push('/student/join')
  } finally {
    loading.value = false
  }
})
</script>
