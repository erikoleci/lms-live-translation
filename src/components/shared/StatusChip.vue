<template>
  <v-chip :color="color" :variant="variant" size="small" class="font-weight-bold">
    <template #prepend>
      <v-icon size="10" class="mr-1" :class="status === 'ACTIVE' ? 'status-pulse' : ''">mdi-circle</v-icon>
    </template>
    {{ label }}
  </v-chip>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: String,
  variant: { type: String, default: 'tonal' },
})

const color = computed(() => ({
  CREATED: 'grey', WAITING: 'blue', ACTIVE: 'success',
  PAUSED: 'warning', ENDED: 'default', FAILED: 'error', EXPIRED: 'grey',
}[props.status] ?? 'grey'))

const label = computed(() => ({
  CREATED: 'I Krijuar', WAITING: 'Duke Pritur', ACTIVE: 'Aktiv',
  PAUSED: 'Pauzuar', ENDED: 'Përfunduar', FAILED: 'Gabim', EXPIRED: 'Skaduar',
}[props.status] ?? props.status))
</script>

<style scoped>
.status-pulse {
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
