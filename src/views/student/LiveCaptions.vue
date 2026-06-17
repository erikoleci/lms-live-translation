<template>
  <v-layout>

    <!-- APP BAR -->
    <v-app-bar flat border="b" height="56" density="compact">
      <v-avatar color="primary" size="28" rounded="lg" class="ml-3">
        <v-icon color="white" size="15">mdi-translate</v-icon>
      </v-avatar>
      <v-app-bar-title class="ml-2">
        <span class="text-body-2 font-weight-bold">{{ session?.title ?? 'Live Session' }}</span>
        <div class="d-flex align-center gap-1">
          <StatusChip v-if="session" :status="session.status" />
          <ConnectionStatus :status="wsStatus" />
        </div>
      </v-app-bar-title>

      <template #append>
        <div class="d-flex align-center gap-1 pr-1">
          <!-- Language toggle -->
          <v-btn-toggle
            v-model="selectedLanguage"
            mandatory density="compact"
            rounded="lg" divided color="primary"
            class="mr-1"
          >
            <v-btn
              v-for="lang in availableLangs" :key="lang.value"
              :value="lang.value" size="small"
              class="px-2 px-sm-3"
            >
              <span>{{ lang.flag }}</span>
              <span class="d-none d-sm-inline ml-1 text-caption">{{ lang.shortLabel }}</span>
            </v-btn>
          </v-btn-toggle>

          <v-btn
            :icon="showSettings ? 'mdi-close' : 'mdi-tune'"
            variant="tonal" size="small"
            @click="showSettings = !showSettings"
          />
          <v-btn
            :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
            variant="tonal" size="small"
            @click="uiStore.toggleDarkMode()"
          />
          <v-btn icon="mdi-exit-to-app" variant="tonal" size="small" color="error" @click="leave" />
        </div>
      </template>
    </v-app-bar>

    <!-- SETTINGS DRAWER — desktop only -->
    <v-navigation-drawer
      v-if="!smAndDown"
      v-model="showSettings"
      temporary
      location="right"
      width="300"
      border="s"
    >
      <v-list-item class="py-3">
        <v-list-item-title class="text-body-1 font-weight-bold">Settings</v-list-item-title>
        <template #append>
          <v-btn icon="mdi-close" size="small" variant="text" @click="showSettings = false" />
        </template>
      </v-list-item>
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
      />
    </v-navigation-drawer>

    <!-- MAIN CAPTION AREA -->
    <v-main>
      <v-sheet
        :color="uiStore.darkMode ? 'grey-darken-4' : 'grey-lighten-4'"
        class="d-flex flex-column overflow-hidden"
        style="height:100%"
      >
        <!-- Empty waiting state -->
        <div v-if="!segments.length" class="d-flex flex-column align-center justify-center flex-grow-1">
          <v-progress-circular indeterminate color="primary" size="48" class="mb-4" />
          <p class="text-body-2 text-medium-emphasis">Waiting for the teacher to start speaking…</p>
        </div>

        <!-- Caption blocks -->
        <div v-else class="d-flex flex-column justify-end pa-4 pa-sm-6 h-100 overflow-hidden">
          <v-slide-y-reverse-transition group>
            <v-sheet
              v-for="seg in visibleSegments" :key="seg.id"
              rounded="xl"
              :color="uiStore.darkMode ? 'grey-darken-3' : 'white'"
              class="pa-4 pa-sm-5 mb-3"
              :style="`border-left: 4px solid ${seg.isFinal ? '#1565C0' : '#ff9800'}; opacity: ${seg.isFinal ? 1 : 0.8}`"
              elevation="1"
            >
              <p
                class="font-weight-semibold ma-0"
                :class="uiStore.darkMode ? 'text-white' : 'text-on-surface'"
                :style="`font-size: ${uiStore.captionFontSize}px; line-height: 1.5`"
              >{{ displayText(seg) }}</p>
              <div v-if="showOriginal" class="d-flex align-center gap-1 mt-1">
                <v-icon size="12" color="grey">mdi-translate</v-icon>
                <span class="text-caption text-disabled font-italic">{{ seg.originalText }}</span>
              </div>
            </v-sheet>
          </v-slide-y-reverse-transition>
        </div>
      </v-sheet>
    </v-main>

    <!-- MOBILE bottom-sheet settings -->
    <v-bottom-sheet v-if="smAndDown" v-model="showSettings" :inset="false">
      <v-sheet rounded="t-xl" max-height="80vh" class="overflow-y-auto">
        <v-list-item class="py-3">
          <v-list-item-title class="text-body-1 font-weight-bold">Settings</v-list-item-title>
          <template #append>
            <v-btn icon="mdi-close" size="small" variant="text" @click="showSettings = false" />
          </template>
        </v-list-item>
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
        />
      </v-sheet>
    </v-bottom-sheet>

    <!-- Audio indicator -->
    <v-chip
      v-if="audioEnabled"
      color="black"
      size="small"
      prepend-icon="mdi-volume-high"
      class="text-white"
      style="position:fixed; bottom:16px; right:16px; z-index:100; opacity:0.85"
    >Audio ON · AI</v-chip>

  </v-layout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useSessionStore } from '../../stores/session.js'
import { useParticipantStore } from '../../stores/participant.js'
import { useProviderStore } from '../../stores/provider.js'
import { useUiStore } from '../../stores/ui.js'
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
const visibleSegments = computed(() => segments.value.slice(-5))
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

function displayText(seg) {
  if (selectedLanguage.value === seg.sourceLanguage) return seg.originalText
  const tr = seg.translations?.find(t => t.targetLanguage === selectedLanguage.value)
  return tr?.translatedText ?? seg.originalText
}

function leave() {
  if (participantStore.currentParticipant) participantStore.leaveSession(participantStore.currentParticipant.id)
  router.push('/student/join')
}

onMounted(() => { if (!session.value) router.push('/student/join') })
onUnmounted(() => { wsStatus.value = 'DISCONNECTED' })
</script>
