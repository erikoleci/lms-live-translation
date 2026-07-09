<template>
  <v-chip :color="color" size="small" variant="tonal" class="font-weight-bold">
    <template #prepend>
      <span style="font-size:13px; line-height:1">{{ flag }}</span>
    </template>
    {{ label }}
  </v-chip>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ lang: String })
const data = { IT: { flag: '🇮🇹', label: 'Italisht', color: 'green' }, EN: { flag: '🇬🇧', label: 'Anglisht', color: 'blue' }, SQ: { flag: '🇦🇱', label: 'Shqip', color: 'red' } }
const flag = computed(() => data[props.lang]?.flag ?? '🌐')
const label = computed(() => data[props.lang]?.label ?? props.lang)
const color = computed(() => data[props.lang]?.color ?? 'grey')
</script>
