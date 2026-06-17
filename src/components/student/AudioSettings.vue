<template>
  <v-card rounded="xl" elevation="0" border>
    <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
      <v-icon start color="primary">mdi-volume-high</v-icon>
      Audio Settings
    </v-card-title>
    <v-card-text class="px-4 pb-4">
      <v-switch
        :model-value="audioEnabled"
        :color="audioEnabled ? 'success' : 'grey'"
        :label="audioEnabled ? 'Translated audio: ON' : 'Translated audio: OFF'"
        density="comfortable" hide-details class="mb-3"
        @update:model-value="v => emit('update:audioEnabled', v)"
      />

      <v-sheet color="orange-lighten-5" rounded="lg" class="d-flex align-center gap-2 pa-2 mb-4">
        <v-icon size="14" color="orange">mdi-robot</v-icon>
        <span class="text-caption text-medium-emphasis">Audio is AI-generated. Voice may not reflect a real person.</span>
      </v-sheet>

      <p class="text-caption text-medium-emphasis font-weight-bold text-uppercase mb-2">Select Voice</p>
      <div class="d-flex flex-wrap gap-2">
        <v-btn
          v-for="voice in voices" :key="voice.code"
          size="small" rounded="lg"
          :variant="selectedVoice === voice.code ? 'flat' : 'outlined'"
          :color="selectedVoice === voice.code ? 'primary' : 'default'"
          :prepend-icon="voice.gender === 'FEMALE' ? 'mdi-gender-female' : voice.gender === 'MALE' ? 'mdi-gender-male' : 'mdi-gender-non-binary'"
          @click="emit('update:selectedVoice', voice.code)"
        >{{ voice.name }}</v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup>
defineProps({ audioEnabled: Boolean, selectedVoice: String, voices: Array })
const emit = defineEmits(['update:audioEnabled', 'update:selectedVoice'])
</script>
