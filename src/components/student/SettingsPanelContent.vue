<template>
  <div class="pa-4 d-flex flex-column gap-4">

    <!-- Language -->
    <v-card rounded="xl" elevation="0" border>
      <v-card-title class="text-body-2 font-weight-bold pt-3 px-4">
        <v-icon start size="16" color="primary">mdi-translate</v-icon>
        Display Language
      </v-card-title>
      <v-card-text class="px-4 pb-4">
        <LanguageSelector :model-value="selectedLanguage" @update:model-value="emit('update:selectedLanguage', $event)" />
        <v-switch
          :model-value="showOriginal"
          label="Show original text"
          color="primary"
          density="compact"
          hide-details
          class="mt-3"
          @update:model-value="emit('update:showOriginal', $event)"
        />
      </v-card-text>
    </v-card>

    <!-- Caption size -->
    <v-card rounded="xl" elevation="0" border>
      <v-card-title class="text-body-2 font-weight-bold pt-3 px-4">
        <v-icon start size="16" color="primary">mdi-format-size</v-icon>
        Caption Size
      </v-card-title>
      <v-card-text class="px-4 pb-4">
        <v-slider
          :model-value="captionFontSize"
          min="12" max="36" step="2" color="primary"
          track-color="grey-lighten-2" thumb-label
          @update:model-value="emit('update:captionFontSize', $event)"
        />
        <div class="d-flex gap-2 flex-wrap">
          <v-btn
            v-for="size in [14, 18, 24, 32]" :key="size"
            size="small" rounded="lg"
            :variant="captionFontSize === size ? 'flat' : 'outlined'"
            :color="captionFontSize === size ? 'primary' : 'default'"
            @click="emit('update:captionFontSize', size)"
          >{{ size }}px</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <!-- Audio settings -->
    <v-card rounded="xl" elevation="0" border>
      <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
        <v-icon start color="primary">mdi-volume-high</v-icon>
        Audio
      </v-card-title>
      <v-card-text class="px-4 pb-4">
        <v-switch
          :model-value="audioEnabled"
          :color="audioEnabled ? 'success' : 'grey'"
          :label="audioEnabled ? 'Translated audio: ON' : 'Translated audio: OFF'"
          density="comfortable"
          hide-details
          class="mb-3"
          @update:model-value="emit('update:audioEnabled', $event)"
        />
        <div class="ai-disclosure mb-4">
          <v-icon size="14" color="orange" class="mr-1">mdi-robot</v-icon>
          <span class="text-caption text-medium-emphasis">Audio is AI-generated. Voice may not reflect a real person.</span>
        </div>
        <p class="text-caption text-medium-emphasis font-weight-bold text-uppercase mb-2">Select Voice</p>
        <div class="d-flex flex-wrap gap-2">
          <v-btn
            v-for="voice in availableVoices" :key="voice.code"
            :variant="selectedVoice === voice.code ? 'flat' : 'outlined'"
            :color="selectedVoice === voice.code ? 'primary' : 'default'"
            size="small" rounded="lg"
            @click="emit('update:selectedVoice', voice.code)"
          >
            <v-icon start size="14">{{ voice.gender === 'FEMALE' ? 'mdi-gender-female' : voice.gender === 'MALE' ? 'mdi-gender-male' : 'mdi-gender-non-binary' }}</v-icon>
            {{ voice.name }}
          </v-btn>
          <p v-if="!availableVoices.length" class="text-caption text-disabled ma-0">No voices available for this language.</p>
        </div>
      </v-card-text>
    </v-card>

    <!-- Participants -->
    <v-card rounded="xl" elevation="0" border>
      <v-card-title class="text-body-2 font-weight-bold pt-3 px-4">
        <v-icon start size="16" color="primary">mdi-account-group</v-icon>
        Participants ({{ participantCount }})
      </v-card-title>
      <v-card-text class="px-4 pb-4">
        <div v-if="participants.length" class="d-flex flex-column gap-1">
          <div v-for="p in participants" :key="p.id" class="participant-row">
            <v-avatar size="28" :color="p.connectionStatus === 'CONNECTED' ? 'primary' : 'grey'" class="flex-shrink-0">
              <span class="text-caption text-white font-weight-bold">{{ p.anonymousName[0] }}</span>
            </v-avatar>
            <span class="text-caption flex-grow-1 text-truncate">{{ p.anonymousName }}</span>
            <LanguageBadge :lang="p.targetLanguage" />
          </div>
        </div>
        <p v-else class="text-caption text-disabled ma-0">No participants yet.</p>
      </v-card-text>
    </v-card>

  </div>
</template>

<script setup>
import LanguageSelector from './LanguageSelector.vue'
import LanguageBadge from '../shared/LanguageBadge.vue'

defineProps({
  selectedLanguage: String,
  showOriginal: Boolean,
  audioEnabled: Boolean,
  selectedVoice: String,
  availableVoices: { type: Array, default: () => [] },
  participants: { type: Array, default: () => [] },
  participantCount: { type: Number, default: 0 },
  captionFontSize: { type: Number, default: 18 },
  hideClose: Boolean,
})

const emit = defineEmits([
  'update:selectedLanguage',
  'update:showOriginal',
  'update:audioEnabled',
  'update:selectedVoice',
  'update:captionFontSize',
  'close',
])
</script>

<style scoped>
.ai-disclosure { display: flex; align-items: center; padding: 8px 10px; background: rgba(255,152,0,0.08); border-radius: 8px; border: 1px solid rgba(255,152,0,0.2); }
.participant-row { display: flex; align-items: center; gap: 8px; padding: 4px 0; }
</style>
