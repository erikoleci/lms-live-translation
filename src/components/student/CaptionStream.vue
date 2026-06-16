<template>
  <div class="caption-stream" :class="{ 'caption-stream--dark': dark }">
    <transition-group name="caption-slide" tag="div" class="segments-wrapper">
      <div v-for="seg in visibleSegments" :key="seg.id" class="caption-block" :class="{ 'caption-block--partial': !seg.isFinal }">
        <p class="caption-text" :style="{ fontSize: `${fontSize}px`, lineHeight: '1.5' }">
          {{ displayText(seg) }}
        </p>
        <div v-if="showOriginal && targetLanguage !== seg.sourceLanguage" class="original-line">
          <v-icon size="12" color="grey" class="mr-1">mdi-translate</v-icon>
          <span class="text-caption text-disabled font-italic">{{ seg.originalText }}</span>
        </div>
      </div>
    </transition-group>
    <div v-if="visibleSegments.length === 0" class="waiting-state">
      <div class="pulse-ring" />
      <p class="text-body-2 text-disabled mt-4">Waiting for the teacher to start speaking…</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  segments: Array,
  targetLanguage: String,
  fontSize: Number,
  showOriginal: Boolean,
  dark: Boolean,
})

const visibleSegments = computed(() => props.segments.slice(-6))

function displayText(seg) {
  if (props.targetLanguage === seg.sourceLanguage) return seg.originalText
  const tr = seg.translations?.find(t => t.targetLanguage === props.targetLanguage)
  return tr?.translatedText ?? seg.originalText
}
</script>

<style scoped>
.caption-stream { height: 100%; overflow: hidden; display: flex; flex-direction: column; justify-content: flex-end; padding: 24px; background: transparent; }
.caption-stream--dark .caption-text { color: #fff; }
.caption-stream--dark .original-line { opacity: 0.6; }
.segments-wrapper { display: flex; flex-direction: column; gap: 16px; }
.caption-block { padding: 16px 20px; border-radius: 16px; background: rgba(0,0,0,0.06); border-left: 4px solid #1565C0; transition: opacity 0.3s; }
.caption-stream--dark .caption-block { background: rgba(255,255,255,0.08); border-left-color: #42A5F5; }
.caption-block--partial { opacity: 0.75; border-left-color: #ff9800; }
.caption-text { margin: 0; font-weight: 600; line-height: 1.5; }
.original-line { margin-top: 4px; display: flex; align-items: center; }
.waiting-state { display: flex; flex-direction: column; align-items: center; justify-content: center; flex: 1; opacity: 0.6; }
.pulse-ring { width: 48px; height: 48px; border-radius: 50%; border: 3px solid #1565C0; animation: ring-pulse 2s ease-in-out infinite; }
@keyframes ring-pulse { 0%, 100% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.3); opacity: 0.3; } }
.caption-slide-enter-from { opacity: 0; transform: translateY(20px); }
.caption-slide-enter-active { transition: all 0.4s ease; }
.caption-slide-leave-to { opacity: 0; transform: translateY(-10px); }
.caption-slide-leave-active { transition: all 0.2s ease; }
</style>
