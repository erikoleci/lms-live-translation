<template>
  <v-chip :color="color" size="x-small" variant="tonal">
    <template #prepend>
      <v-icon size="8" class="mr-1">mdi-circle</v-icon>
    </template>
    {{ label }}
  </v-chip>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ status: String })
const color = computed(() => ({ CONNECTED: 'success', DISCONNECTED: 'error', RECONNECTING: 'warning', CONNECTING: 'info' }[props.status] ?? 'grey'))
const label = computed(() => ({ CONNECTED: 'Lidhur', DISCONNECTED: 'Pa lidhje', RECONNECTING: 'Duke u lidhur...', CONNECTING: 'Duke u lidhur...' }[props.status] ?? props.status))
</script>
