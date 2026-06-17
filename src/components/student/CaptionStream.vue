<template>
  <v-sheet
    :color="dark ? 'grey-darken-4' : 'grey-lighten-5'"
    class="d-flex flex-column justify-end h-100 overflow-hidden pa-4 pa-sm-6"
  >
    <!-- Waiting state -->
    <div v-if="!visibleSegments.length" class="d-flex flex-column align-center justify-center flex-grow-1 opacity-60">
      <v-progress-circular indeterminate color="primary" size="48" />
      <p class="text-body-2 text-medium-emphasis mt-4">Waiting for the teacher to start speaking…</p>
    </div>

    <!-- Caption blocks -->
    <v-slide-y-reverse-transition group>
      <v-sheet
        v-for="seg in visibleSegments"
        :key="seg.id"
        rounded="xl"
        :color="dark ? 'grey-darken-3' : 'white'"
        :elevation="dark ? 0 : 1"
        class="pa-4 pa-sm-5 mb-3"
        :style="`
          border-left: 4px solid ${seg.isFinal ? (dark ? '#42A5F5' : '#1565C0') : '#ff9800'};
          opacity: ${seg.isFinal ? 1 : 0.8}
        `"
      >
        <p
          class="font-weight-semibold ma-0"
          :class="dark ? 'text-white' : ''"
          :style="`font-size: ${fontSize}px; line-height: 1.5`"
        >{{ displayText(seg) }}</p>

        <div v-if="showOriginal" class="d-flex align-center gap-1 mt-1">
          <v-icon size="12" color="grey">mdi-translate</v-icon>
          <span class="text-caption text-disabled font-italic">{{ seg.originalText }}</span>
        </div>
      </v-sheet>
    </v-slide-y-reverse-transition>
  </v-sheet>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  segments: Array,
  targetLanguage: String,
  fontSize: { type: Number, default: 18 },
  showOriginal: Boolean,
  dark: Boolean,
})

const visibleSegments = computed(() => props.segments.slice(-5))

function displayText(seg) {
  if (props.targetLanguage === seg.sourceLanguage) return seg.originalText
  const tr = seg.translations?.find(t => t.targetLanguage === props.targetLanguage)
  return tr?.translatedText ?? seg.originalText
}
</script>
