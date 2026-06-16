<template>
  <v-chip :color="color" :variant="variant" size="small" class="font-weight-bold text-uppercase">
    <template #prepend>
      <v-icon size="12" class="mr-1">{{ icon }}</v-icon>
    </template>
    {{ status }}
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

const icon = computed(() => ({
  CREATED: 'mdi-clock-outline', WAITING: 'mdi-dots-horizontal-circle',
  ACTIVE: 'mdi-broadcast', PAUSED: 'mdi-pause-circle',
  ENDED: 'mdi-check-circle', FAILED: 'mdi-alert-circle', EXPIRED: 'mdi-timer-off',
}[props.status] ?? 'mdi-help-circle'))
</script>
