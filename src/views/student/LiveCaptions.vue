<template>
  <div class="captions-page" :class="{ 'captions-page--dark': uiStore.darkMode }">

    <!-- TOP BAR -->
    <div class="captions-topbar">
      <div class="d-flex align-center gap-2 flex-grow-1 min-w-0">
        <v-icon size="20" :color="uiStore.darkMode ? 'white' : 'primary'">mdi-translate</v-icon>
        <div class="min-w-0">
          <p class="text-body-2 font-weight-bold text-truncate">{{ session?.title ?? 'Live Session' }}</p>
          <div class="d-flex align-center gap-1">
            <StatusChip v-if="session" :status="session.status" />
            <ConnectionStatus :status="wsStatus" />
          </div>
        </div>
      </div>

      <div class="d-flex align-center gap-2">
        <!-- Language quick-switch -->
        <div class="lang-switch-row">
          <button
            v-for="lang in availableLangs"
            :key="lang.value"
            class="lang-btn-sm"
            :class="{ 'lang-btn-sm--active': selectedLanguage === lang.value }"
            @click="selectedLanguage = lang.value"
          >
            <span>{{ lang.flag }}</span>
            <span class="lang-btn-sm__label">{{ lang.shortLabel }}</span>
          </button>
        </div>

        <v-divider vertical style="height:28px" />

        <v-btn :icon="showSettings ? 'mdi-close' : 'mdi-tune'" variant="tonal" size="small" @click="showSettings = !showSettings" />
        <v-btn :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'" variant="tonal" size="small" @click="uiStore.toggleDarkMode()" />
        <v-btn icon="mdi-exit-to-app" variant="tonal" size="small" color="error" @click="leave" />
      </div>
    </div>

    <!-- BODY -->
    <div class="captions-body">
      <!-- Settings panel -->
      <transition name="slide-x">
        <aside v-if="showSettings" class="settings-panel">
          <div class="pa-4 d-flex flex-column gap-4">
            <v-card rounded="xl" elevation="0" border>
              <v-card-title class="text-body-2 font-weight-bold pt-3 px-4">
                <v-icon start size="16" color="primary">mdi-translate</v-icon>
                Display Language
              </v-card-title>
              <v-card-text class="px-4 pb-4">
                <LanguageSelector v-model="selectedLanguage" />
                <v-switch v-model="showOriginal" label="Show original text" color="primary" density="compact" hide-details class="mt-3" />
              </v-card-text>
            </v-card>

            <v-card rounded="xl" elevation="0" border>
              <v-card-title class="text-body-2 font-weight-bold pt-3 px-4">
                <v-icon start size="16" color="primary">mdi-format-size</v-icon>
                Caption Size
              </v-card-title>
              <v-card-text class="px-4 pb-4">
                <v-slider :model-value="uiStore.captionFontSize" min="12" max="36" step="2" color="primary" track-color="grey-lighten-2" thumb-label @update:model-value="uiStore.setCaptionFontSize($event)" />
                <div class="d-flex gap-2 flex-wrap">
                  <v-btn v-for="size in [14, 18, 24, 32]" :key="size" size="small" :variant="uiStore.captionFontSize === size ? 'flat' : 'outlined'" :color="uiStore.captionFontSize === size ? 'primary' : 'default'" @click="uiStore.setCaptionFontSize(size)">{{ size }}px</v-btn>
                </div>
              </v-card-text>
            </v-card>

            <AudioSettings v-model:audio-enabled="audioEnabled" v-model:selected-voice="selectedVoice" :voices="availableVoices" />

            <v-card rounded="xl" elevation="0" border>
              <v-card-title class="text-body-2 font-weight-bold pt-3 px-4">
                <v-icon start size="16" color="primary">mdi-account-group</v-icon>
                Participants ({{ session?.participantCount ?? 0 }})
              </v-card-title>
              <v-card-text class="px-4 pb-4">
                <div v-for="p in participants" :key="p.id" class="participant-row">
                  <v-avatar size="28" :color="p.connectionStatus === 'CONNECTED' ? 'primary' : 'grey'" class="mr-2">
                    <span class="text-caption text-white">{{ p.anonymousName[0] }}</span>
                  </v-avatar>
                  <span class="text-caption flex-grow-1">{{ p.anonymousName }}</span>
                  <LanguageBadge :lang="p.targetLanguage" />
                </div>
              </v-card-text>
            </v-card>
          </div>
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

    <!-- Audio on indicator -->
    <div v-if="audioEnabled" class="audio-indicator">
      <v-icon size="14" color="white">mdi-volume-high</v-icon>
      <span class="text-caption text-white ml-1">Audio ON · AI-generated</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSessionStore } from '../../stores/session.js'
import { useParticipantStore } from '../../stores/participant.js'
import { useProviderStore } from '../../stores/provider.js'
import { useUiStore } from '../../stores/ui.js'
import CaptionStream from '../../components/student/CaptionStream.vue'
import AudioSettings from '../../components/student/AudioSettings.vue'
import LanguageSelector from '../../components/student/LanguageSelector.vue'
import StatusChip from '../../components/shared/StatusChip.vue'
import ConnectionStatus from '../../components/shared/ConnectionStatus.vue'
import LanguageBadge from '../../components/shared/LanguageBadge.vue'

const route = useRoute()
const router = useRouter()
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

function leave() {
  if (participantStore.currentParticipant) {
    participantStore.leaveSession(participantStore.currentParticipant.id)
  }
  router.push('/student/join')
}

onMounted(() => {
  if (!session.value) router.push('/student/join')
})

onUnmounted(() => {
  wsStatus.value = 'DISCONNECTED'
})
</script>

<style scoped>
.captions-page { display: flex; flex-direction: column; height: 100vh; overflow: hidden; background: #F5F7FA; transition: background 0.3s; }
.captions-page--dark { background: #12121F; color: white; }

.captions-topbar { display: flex; align-items: center; gap: 12px; padding: 8px 16px; background: white; border-bottom: 1px solid rgba(0,0,0,0.08); min-height: 58px; }
.captions-page--dark .captions-topbar { background: #1E1E2E; border-bottom-color: rgba(255,255,255,0.08); }

/* Quick language switcher in topbar */
.lang-switch-row { display: flex; gap: 4px; background: rgba(0,0,0,0.05); border-radius: 10px; padding: 3px; }
.captions-page--dark .lang-switch-row { background: rgba(255,255,255,0.08); }
.lang-btn-sm { display: flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 7px; border: none; cursor: pointer; font-family: inherit; font-size: 12px; font-weight: 600; background: transparent; color: #666; transition: all 0.15s; }
.lang-btn-sm:hover { background: rgba(0,0,0,0.06); }
.lang-btn-sm--active { background: white; color: #1565C0; box-shadow: 0 1px 4px rgba(0,0,0,0.12); }
.captions-page--dark .lang-btn-sm--active { background: #2a2a3e; color: #42A5F5; }
.lang-btn-sm__label { font-size: 11px; }

.captions-body { flex: 1; display: flex; overflow: hidden; }
.settings-panel { width: 300px; min-width: 280px; border-right: 1px solid rgba(0,0,0,0.08); overflow-y: auto; background: white; }
.captions-page--dark .settings-panel { background: #1E1E2E; border-right-color: rgba(255,255,255,0.08); }
.caption-main { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
.participant-row { display: flex; align-items: center; padding: 4px 0; }
.audio-indicator { position: fixed; bottom: 16px; right: 16px; display: flex; align-items: center; padding: 6px 12px; background: rgba(0,0,0,0.7); border-radius: 20px; backdrop-filter: blur(8px); z-index: 100; }
.slide-x-enter-from { transform: translateX(-100%); opacity: 0; }
.slide-x-enter-active { transition: all 0.25s ease; }
.slide-x-leave-to { transform: translateX(-100%); opacity: 0; }
.slide-x-leave-active { transition: all 0.2s ease; }
</style>
