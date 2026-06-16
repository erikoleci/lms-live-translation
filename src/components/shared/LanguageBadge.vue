<template>
  <v-chip :color="color" size="small" variant="tonal" class="font-weight-bold language-badge">
    <template #prepend>
      <span class="flag mr-1">{{ flag }}</span>
    </template>
    {{ label }}
  </v-chip>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ lang: String })

const flag = computed(() => ({ IT: '🇮🇹', EN: '🇬🇧', SQ: '🇦🇱' }[props.lang]))
const label = computed(() => ({ IT: 'Italian', EN: 'English', SQ: 'Albanian' }[props.lang]))
const color = computed(() => ({ IT: 'green', EN: 'blue', SQ: 'red' }[props.lang]))
</script>

<style scoped>
.flag { font-size: 14px; line-height: 1; }
</style>
