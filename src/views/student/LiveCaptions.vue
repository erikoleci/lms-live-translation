<template>
  <div class="captions-page" :class="{ 'captions-page--dark': uiStore.darkMode }">

    <!-- TOP BAR -->
    <div class="captions-topbar">
      <div class="d-flex align-center gap-2 flex-grow-1 min-w-0">
        <v-avatar color="primary" size="30" rounded="lg" class="flex-shrink-0">
          <v-icon color="white" size="16">mdi-translate</v-icon>
        </v-avatar>
        <div class="min-w-0">
          <p class="text-body-2 font-weight-bold text-truncate" style="line-height:1.2">{{ session?.title ?? 'Live Session' }}</p>
          <div class="d-flex align-center gap-1">
            <StatusChip v-if="session" :status="session.status" />
            <ConnectionStatus :status="wsStatus" />
          </div>
        </div>
      </div>

      <!-- Language quick-switch -->
      <div class="lang-switch-row flex-shrink-0">
        <button
          v-for="lang in availableLangs"
          :key="lang.value"
          class="lang-btn"
          :class="{ 'lang-btn--active': selectedLanguage === lang.value }"
          @click="selectedLanguage = lang.value"
        >
          <span>{{ lang.flag }}</span>
          <span class="lang-label">{{ lang.shortLabel }}</span>
        </button>
      </div>

      <div class="d-flex align-center gap-1 flex-shrink-0">
        <v-btn icon="mdi-tune" variant="tonal" size="small" @click="openSettings" />
        <v-btn :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'" variant="tonal" size="small" @click="uiStore.toggleDarkMode()" />
        <v-btn icon="mdi-exit-to-app" variant="tonal" size="small" color="error" @click="leave" />
      </div>
    </div>

    <!-- BODY -->
    <div class="captions-body">

      <!-- Settings panel — side on desktop, bottom sheet on mobile -->
      <transition name="slide-x">
        <aside v-if="showSettings && !smAndDown" class="settings-panel">
          <SettingsPanelContent
            v-model:selected-language="selectedLanguage"
            v-model:show-original="showOriginal"
            v-model:audio-enabled="audioEnabled"
            v-model:selected-voice="selectedVoice"
            :available-voices="availableVoices"
            :participants="participants"
            :participant-count="session?.participantCount ?? 0"
            :caption-font-size="uiStore.captionFontSize"
            @update:caption-font-size="uiStore.setCaptionFontSize($event)"
            @close="showSettings = false"
          />
        </aside>
      </transition>

      <!-- Caption main area -->
      <main class="caption-main">
        <CaptionStream
          :segments="segments"
          :target-language="selectedLanguage"
          :font-size="uiStore.captionFontSize"
          :show-original="showOriginal"
          :dark="uiStore.darkMode"
        />
      </main>
    </div>

    <!-- Mobile bottom sheet settings -->
    <v-bottom-sheet v-if="smAndDown" v-model="showSettings" :inset="false">
      <v-sheet rounded="t-xl" max-height="80vh" style="overflow-y:auto">
        <div class="d-flex align-center justify-space-between px-4 pt-4 pb-2">
          <span class="text-body-1 font-weight-bold">Settings</span>
          <v-btn icon="mdi-close" size="small" variant="text" @click="showSettings = false" />
        </div>
        <v-divider />
        <SettingsPanelContent
          v-model:selected-language="selectedLanguage"
          v-model:show-original="showOriginal"
          v-model:audio-enabled="audioEnabled"
          v-model:selected-voice="selectedVoice"
          :available-voices="availableVoices"
          :participants="participants"
          :participant-count="session?.participantCount ?? 0"
          :caption-font-size="uiStore.captionFontSize"
          @update:caption-font-size="uiStore.setCaptionFontSize($event)"
          @close="showSettings = false"
          :hide-close="true"
        />
      </v-sheet>
    </v-bottom-sheet>

    <!-- Audio on indicator -->
    <div v-if="audioEnabled" class="audio-indicator">
      <v-icon size="14" color="white">mdi-volume-high</v-icon>
      <span class="text-caption text-white ml-1">Audio ON · AI</span>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useSessionStore } from '../../stores/session.js'
import { useParticipantStore } from '../../stores/participant.js'
import { useProviderStore } from '../../stores/provider.js'
import { useUiStore } from '../../stores/ui.js'
import CaptionStream from '../../components/student/CaptionStream.vue'
import SettingsPanelContent from '../../components/student/SettingsPanelContent.vue'
import StatusChip from '../../components/shared/StatusChip.vue'
import ConnectionStatus from '../../components/shared/ConnectionStatus.vue'

const route = useRoute()
const router = useRouter()
const { smAndDown } = useDisplay()
const sessionStore = useSessionStore()
const participantStore = useParticipantStore()
const providerStore = useProviderStore()
const uiStore = useUiStore()

const sessionId = route.params.id
const session = computed(() => sessionStore.getSession(sessionId))
const segments = computed(() => sessionStore.getTranscript(sessionId))
const participants = computed(() => participantStore.getParticipantsForSession(sessionId))

const selectedLanguage = ref(participantStore.currentParticipant?.targetLanguage ?? 'EN')
const showOriginal = ref(false)
const showSettings = ref(false)
const audioEnabled = ref(false)
const selectedVoice = ref('en-US-alloy')
const wsStatus = ref('CONNECTED')

const availableLangs = [
  { value: 'IT', shortLabel: 'ITA', flag: '🇮🇹' },
  { value: 'EN', shortLabel: 'ENG', flag: '🇬🇧' },
  { value: 'SQ', shortLabel: 'SHQ', flag: '🇦🇱' },
]

const availableVoices = computed(() =>
  providerStore.providers
    .filter(p => p.providerType === 'TTS' && p.enabled)
    .flatMap(p => p.supportedVoices ?? [])
    .filter(v => v.language === selectedLanguage.value)
)

function openSettings() { showSettings.value = !showSettings.value }

function leave() {
  if (participantStore.currentParticipant) participantStore.leaveSession(participantStore.currentParticipant.id)
  router.push('/student/join')
}

onMounted(() => { if (!session.value) router.push('/student/join') })
onUnmounted(() => { wsStatus.value = 'DISCONNECTED' })
</script>

<style scoped>
.captions-page { display: flex; flex-direction: column; height: 100vh; overflow: hidden; background: #F5F7FA; transition: background 0.3s; }
.captions-page--dark { background: #12121F; color: white; }

.captions-topbar {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 12px; background: white;
  border-bottom: 1px solid rgba(0,0,0,0.08); min-height: 58px;
  flex-wrap: nowrap; overflow: hidden;
}
.captions-page--dark .captions-topbar { background: #1E1E2E; border-bottom-color: rgba(255,255,255,0.08); }

.lang-switch-row { display: flex; gap: 3px; background: rgba(0,0,0,0.05); border-radius: 10px; padding: 3px; }
.captions-page--dark .lang-switch-row { background: rgba(255,255,255,0.08); }
.lang-btn { display: flex; align-items: center; gap: 3px; padding: 4px 8px; border-radius: 7px; border: none; cursor: pointer; font-family: inherit; font-size: 12px; font-weight: 600; background: transparent; color: #666; transition: all 0.15s; white-space: nowrap; }
.lang-btn:hover { background: rgba(0,0,0,0.06); }
.lang-btn--active { background: white; color: #1565C0; box-shadow: 0 1px 4px rgba(0,0,0,0.12); }
.captions-page--dark .lang-btn--active { background: #2a2a3e; color: #42A5F5; }
.lang-label { font-size: 11px; }

@media (max-width: 480px) {
  .lang-label { display: none; }
  .lang-btn { padding: 5px 7px; }
}

.captions-body { flex: 1; display: flex; overflow: hidden; }
.settings-panel { width: 290px; min-width: 270px; border-right: 1px solid rgba(0,0,0,0.08); overflow-y: auto; background: white; }
.captions-page--dark .settings-panel { background: #1E1E2E; border-right-color: rgba(255,255,255,0.08); }
.caption-main { flex: 1; overflow: hidden; display: flex; flex-direction: column; min-width: 0; }

.audio-indicator { position: fixed; bottom: 16px; right: 16px; display: flex; align-items: center; padding: 6px 14px; background: rgba(0,0,0,0.72); border-radius: 20px; backdrop-filter: blur(8px); z-index: 100; }

.slide-x-enter-from { transform: translateX(-100%); opacity: 0; }
.slide-x-enter-active { transition: all 0.25s ease; }
.slide-x-leave-to { transform: translateX(-100%); opacity: 0; }
.slide-x-leave-active { transition: all 0.2s ease; }
</style>
