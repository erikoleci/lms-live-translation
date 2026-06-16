<template>
  <div class="live-transcript" ref="containerRef">
    <div v-if="segments.length === 0" class="empty-state">
      <v-icon size="48" color="grey-lighten-2">mdi-text-recognition</v-icon>
      <p class="text-body-2 text-disabled mt-2">Transcript will appear here once the session starts…</p>
    </div>
    <div v-for="seg in segments" :key="seg.id" class="segment" :class="{ 'segment--partial': !seg.isFinal }">
      <div class="segment-meta">
        <v-icon size="12" :color="seg.isFinal ? 'success' : 'warning'" class="mr-1">
          {{ seg.isFinal ? 'mdi-check-circle' : 'mdi-loading mdi-spin' }}
        </v-icon>
        <span class="text-caption text-disabled">#{{ seg.sequenceNo }}</span>
        <span class="text-caption text-disabled ml-2">{{ formatTime(seg.startOffsetMs) }}</span>
        <v-chip v-if="seg.confidence" size="x-small" variant="text" class="ml-1">
          {{ Math.round((seg.confidence ?? 0) * 100) }}%
        </v-chip>
      </div>
      <p class="original-text">{{ seg.originalText }}</p>
      <div v-if="seg.translations?.length" class="translations mt-1">
        <div v-for="tr in seg.translations" :key="tr.id" class="translation-row">
          <LanguageBadge :lang="tr.targetLanguage" />
          <span class="translation-text ml-2">{{ tr.translatedText }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import LanguageBadge from '../shared/LanguageBadge.vue'

const props = defineProps({ segments: Array })
const containerRef = ref(null)

watch(() => props.segments?.length, () => scrollToBottom(), { flush: 'post' })

function scrollToBottom() {
  nextTick(() => {
    if (containerRef.value) containerRef.value.scrollTop = containerRef.value.scrollHeight
  })
}

function formatTime(ms) {
  const s = Math.floor(ms / 1000)
  const m = Math.floor(s / 60)
  return `${String(m).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`
}

defineExpose({ scrollToBottom })
</script>

<style scoped>
.live-transcript { height: 100%; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 16px; scroll-behavior: smooth; }
.empty-state { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; opacity: 0.6; }
.segment { padding: 12px 16px; border-radius: 12px; background: rgba(0,0,0,0.03); border-left: 3px solid #1565C0; transition: opacity 0.3s; }
.segment--partial { border-left-color: #ff9800; opacity: 0.8; }
.segment-meta { display: flex; align-items: center; margin-bottom: 4px; }
.original-text { font-size: 15px; font-weight: 500; line-height: 1.5; margin: 0; }
.translations { display: flex; flex-direction: column; gap: 4px; }
.translation-row { display: flex; align-items: baseline; gap: 4px; }
.translation-text { font-size: 13px; color: #666; font-style: italic; }
</style>
