<template>
  <div class="lang-selector">
    <p class="text-caption text-medium-emphasis font-weight-bold text-uppercase mb-2">Display Language</p>
    <div class="lang-grid">
      <button
        v-for="lang in LANGUAGES"
        :key="lang.value"
        class="lang-btn"
        :class="{ 'lang-btn--active': modelValue === lang.value }"
        @click="emit('update:modelValue', lang.value)"
      >
        <span class="lang-flag">{{ lang.flag }}</span>
        <span class="lang-name">{{ lang.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Language } from '../../types'

defineProps<{ modelValue: Language }>()
const emit = defineEmits<{ 'update:modelValue': [Language] }>()

const LANGUAGES = [
  { value: 'IT' as Language, label: 'Italian', flag: '🇮🇹' },
  { value: 'EN' as Language, label: 'English', flag: '🇬🇧' },
  { value: 'SQ' as Language, label: 'Albanian', flag: '🇦🇱' },
]
</script>

<style scoped>
.lang-grid {
  display: flex;
  gap: 8px;
}
.lang-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 16px;
  border: 2px solid transparent;
  border-radius: 12px;
  background: rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.15s;
  min-width: 80px;
  font-family: inherit;
}
.lang-btn:hover {
  background: rgba(21,101,192,0.08);
  border-color: rgba(21,101,192,0.3);
}
.lang-btn--active {
  background: rgba(21,101,192,0.12);
  border-color: #1565C0;
}
.lang-flag { font-size: 24px; }
.lang-name { font-size: 12px; font-weight: 600; color: #555; }
.lang-btn--active .lang-name { color: #1565C0; }
</style>
