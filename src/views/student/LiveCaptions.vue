<template>
  <v-layout style="height:100vh; overflow:hidden">

    <!-- APP BAR -->
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

          <!-- Language switcher — i madh dhe i qartë -->
          <div class="d-flex gap-1">
            <v-btn
              v-for="lang in availableLangs"
              :key="lang.value"
              :color="selectedLanguage === lang.value ? 'primary' : 'default'"
              :variant="selectedLanguage === lang.value ? 'flat' : 'text'"
              size="small"
              rounded="lg"
              class="px-2"
              @click="selectedLanguage = lang.value"
            >
              <span style="font-size:16px">{{ lang.flag }}</span>
              <span class="d-none d-sm-inline ml-1 text-caption font-weight-bold">{{ lang.shortLabel }}</span>
            </v-btn>
          </div>

          <v-divider vertical class="mx-1" style="height:24px" />

          <!-- Font size -->
          <v-btn icon="mdi-format-font-size-decrease" size="small" variant="text"
            @click="uiStore.setCaptionFontSize(uiStore.captionFontSize - 2)" />
          <v-btn icon="mdi-format-font-size-increase" size="small" variant="text"
            @click="uiStore.setCaptionFontSize(uiStore.captionFontSize + 2)" />

          <v-btn
            :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
            variant="text" size="small" @click="uiStore.toggleDarkMode()" />

          <v-btn icon="mdi-cog-outline" variant="tonal" size="small" @click="showSettings = !showSettings" />

          <v-btn icon="mdi-exit-to-app" variant="tonal" size="small" color="error" @click="leave" />
        </div>
      </template>
    </v-app-bar>

    <!-- Settings drawer (desktop) -->
    <v-navigation-drawer v-if="!smAndDown" v-model="showSettings" temporary location="right" width="300">
      <v-list-item class="py-4">
        <v-list-item-title class="text-body-1 font-weight-bold">Cilësimet</v-list-item-title>
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
        :available-voices="[]"
        :participants="participants"
        :participant-count="session?.participantCount ?? 0"
        :caption-font-size="uiStore.captionFontSize"
        @update:caption-font-size="uiStore.setCaptionFontSize($event)"
      />
    </v-navigation-drawer>

    <!-- ZONA KRYESORE E CAPTION-EVE -->
    <v-main>
      <v-sheet
        :color="uiStore.darkMode ? '#121212' : '#F5F5F5'"
        class="d-flex flex-column"
        style="height:100%; position:relative"
      >

        <!-- Waiting state -->
        <div v-if="!segments.length" class="d-flex flex-column align-center justify-center flex-grow-1 gap-4">
          <v-progress-circular indeterminate color="primary" size="56" width="4" />
          <div class="text-center">
            <p class="text-body-1 font-weight-bold mb-1">Duke pritur mësuesin…</p>
            <p class="text-body-2 text-medium-emphasis">Caption-et do të shfaqen këtu automatikisht</p>
          </div>
          <!-- Language reminder -->
          <v-chip :color="langColor" variant="flat" prepend-icon="mdi-translate" size="large" class="mt-2">
            Duke parë: {{ currentLangLabel }}
          </v-chip>
        </div>

        <!-- Caption blocks -->
        <div v-else ref="captionContainer"
          class="flex-grow-1 overflow-hidden d-flex flex-column justify-end pa-4 pa-sm-6 pa-md-8">
          <v-slide-y-reverse-transition group leave-absolute>
            <div v-for="seg in visibleSegments" :key="seg.id" class="mb-3">
              <!-- Partial indicator -->
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

                <!-- Teksti origjinal (opsional) -->
                <div v-if="showOriginal && selectedLanguage !== seg.sourceLanguage"
                  class="d-flex align-center gap-1 mt-2 pt-2" style="border-top: 1px solid rgba(0,0,0,0.08)">
                  <v-icon size="12" color="grey">mdi-translate</v-icon>
                  <span class="text-caption text-medium-emphasis font-italic">{{ seg.originalText }}</span>
                </div>
              </v-sheet>
            </div>
          </v-slide-y-reverse-transition>
        </div>

        <!-- Info bar poshtë -->
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

    <!-- Mobile settings bottom sheet -->
    <v-bottom-sheet v-if="smAndDown" v-model="showSettings">
      <v-sheet rounded="t-xl" max-height="80vh" class="overflow-y-auto">
        <v-list-item class="py-4">
          <v-list-item-title class="text-body-1 font-weight-bold">Cilësimet</v-list-item-title>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useSessionStore } from '../../stores/session.js'
import { useParticipantStore } from '../../stores/participant.js'
import { useUiStore } from '../../stores/ui.js'
import SettingsPanelContent from '../../components/student/SettingsPanelContent.vue'
import StatusChip from '../../components/shared/StatusChip.vue'
import ConnectionStatus from '../../components/shared/ConnectionStatus.vue'

const route = useRoute()
const router = useRouter()
const { smAndDown } = useDisplay()
const sessionStore = useSessionStore()
const participantStore = useParticipantStore()
const uiStore = useUiStore()

const sessionId = route.params.id
const session = computed(() => sessionStore.getSession(sessionId))
const segments = computed(() => sessionStore.getTranscript(sessionId))
const visibleSegments = computed(() => segments.value.slice(-4))
const participants = computed(() => participantStore.getParticipantsForSession(sessionId))

const selectedLanguage = ref(participantStore.currentParticipant?.targetLanguage ?? 'SQ')
const showOriginal = ref(false)
const showSettings = ref(false)
const audioEnabled = ref(false)
const selectedVoice = ref('')
const wsStatus = ref('CONNECTED')

const availableLangs = [
  { value: 'SQ', shortLabel: 'SHQ', flag: '🇦🇱' },
  { value: 'EN', shortLabel: 'ENG', flag: '🇬🇧' },
  { value: 'IT', shortLabel: 'ITA', flag: '🇮🇹' },
]

const langMeta = { SQ: { label: 'Shqip', flag: '🇦🇱', color: 'red' }, EN: { label: 'English', flag: '🇬🇧', color: 'blue' }, IT: { label: 'Italiano', flag: '🇮🇹', color: 'green' } }
const currentLangLabel = computed(() => langMeta[selectedLanguage.value]?.label ?? selectedLanguage.value)
const currentLangFlag = computed(() => langMeta[selectedLanguage.value]?.flag ?? '')
const langColor = computed(() => langMeta[selectedLanguage.value]?.color ?? 'primary')

function displayText(seg) {
  if (selectedLanguage.value === seg.sourceLanguage) return seg.originalText
  const tr = seg.translations?.find(t => t.targetLanguage === selectedLanguage.value)
  return tr?.translatedText ?? seg.originalText
}

function leave() {
  if (participantStore.currentParticipant) participantStore.leaveSession(participantStore.currentParticipant.id)
  router.push('/student/join')
}

onMounted(() => {
  if (!session.value) { router.push('/student/join'); return }
  if (!participantStore.currentParticipant || participantStore.currentParticipant.sessionId !== sessionId) {
    router.push(`/student/join/${session.value.joinCode}`)
  }
})
onUnmounted(() => { wsStatus.value = 'DISCONNECTED' })
</script>
