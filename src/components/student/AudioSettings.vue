<template>
  <v-card rounded="xl" elevation="0" border>
    <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
      <v-icon start color="primary">mdi-volume-high</v-icon>
      Audio Settings
    </v-card-title>
    <v-card-text class="px-4 pb-4">
      <v-switch
        v-model="audioEnabled"
        :color="audioEnabled ? 'success' : 'grey'"
        :label="audioEnabled ? 'Translated audio: ON' : 'Translated audio: OFF'"
        density="comfortable"
        hide-details
        class="mb-3"
        @update:model-value="v => emit('update:audioEnabled', v)"
      />

      <div class="ai-disclosure mb-4">
        <v-icon size="14" color="orange" class="mr-1">mdi-robot</v-icon>
        <span class="text-caption text-medium-emphasis">
          Audio is AI-generated. Voice may not reflect a real person.
        </span>
      </div>

      <p class="text-caption text-medium-emphasis font-weight-bold text-uppercase mb-2">Select Voice</p>
      <div class="voice-grid">
        <v-btn
          v-for="voice in voices"
          :key="voice.code"
          :variant="selectedVoice === voice.code ? 'flat' : 'outlined'"
          :color="selectedVoice === voice.code ? 'primary' : 'default'"
          size="small"
          rounded="lg"
          class="voice-btn"
          @click="emit('update:selectedVoice', voice.code)"
        >
          <v-icon start size="14">
            {{ voice.gender === 'FEMALE' ? 'mdi-gender-female' : voice.gender === 'MALE' ? 'mdi-gender-male' : 'mdi-gender-non-binary' }}
          </v-icon>
          {{ voice.name }}
        </v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import type { Voice } from '../../types'

defineProps<{
  audioEnabled: boolean
  selectedVoice: string
  voices: Voice[]
}>()

const emit = defineEmits<{
  'update:audioEnabled': [boolean]
  'update:selectedVoice': [string]
}>()
</script>

<style scoped>
.ai-disclosure {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  background: rgba(255, 152, 0, 0.08);
  border-radius: 8px;
  border: 1px solid rgba(255, 152, 0, 0.2);
}
.voice-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
