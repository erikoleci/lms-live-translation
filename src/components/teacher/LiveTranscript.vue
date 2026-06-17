<template>
  <v-sheet ref="containerEl" class="h-100 overflow-y-auto pa-4 d-flex flex-column gap-3">
    <!-- Empty state -->
    <div v-if="!segments.length" class="d-flex flex-column align-center justify-center flex-grow-1 py-16 opacity-60">
      <v-icon size="48" color="grey-lighten-2">mdi-text-recognition</v-icon>
      <p class="text-body-2 text-disabled mt-3 text-center">Transcript will appear here once the session starts…</p>
    </div>

    <!-- Segments -->
    <v-sheet
      v-for="seg in segments"
      :key="seg.id"
      rounded="lg"
      :color="seg.isFinal ? undefined : 'orange-lighten-5'"
      border
      class="pa-3"
      :style="`border-left: 3px solid ${seg.isFinal ? '#1565C0' : '#ff9800'} !important`"
    >
      <div class="d-flex align-center gap-2 mb-1">
        <v-icon size="12" :color="seg.isFinal ? 'success' : 'warning'">
          {{ seg.isFinal ? 'mdi-check-circle' : 'mdi-loading mdi-spin' }}
        </v-icon>
        <span class="text-caption text-disabled">#{{ seg.sequenceNo }}</span>
        <span class="text-caption text-disabled">{{ formatTime(seg.startOffsetMs) }}</span>
        <v-chip v-if="seg.confidence" size="x-small" variant="text">
          {{ Math.round((seg.confidence ?? 0) * 100) }}%
        </v-chip>
      </div>

      <p class="text-body-2 font-weight-medium ma-0 mb-1">{{ seg.originalText }}</p>

      <div v-if="seg.translations?.length" class="d-flex flex-column gap-1 mt-1">
        <div v-for="tr in seg.translations" :key="tr.id" class="d-flex align-center gap-2">
          <LanguageBadge :lang="tr.targetLanguage" />
          <span class="text-caption text-medium-emphasis font-italic">{{ tr.translatedText }}</span>
        </div>
      </div>
    </v-sheet>
  </v-sheet>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import LanguageBadge from '../shared/LanguageBadge.vue'

const props = defineProps({ segments: Array })
const containerEl = ref(null)

watch(() => props.segments?.length, () => {
  nextTick(() => {
    const el = containerEl.value?.$el ?? containerEl.value
    if (el) el.scrollTop = el.scrollHeight
  })
}, { flush: 'post' })

function formatTime(ms) {
  const s = Math.floor(ms / 1000)
  return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`
}

defineExpose({ scrollToBottom: () => {
  const el = containerEl.value?.$el ?? containerEl.value
  if (el) el.scrollTop = el.scrollHeight
}})
</script>
