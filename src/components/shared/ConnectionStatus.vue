<template>
  <v-chip :color="color" size="small" variant="flat">
    <template #prepend>
      <v-icon size="10" class="mr-1">mdi-circle</v-icon>
    </template>
    {{ label }}
  </v-chip>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ status: String })

const color = computed(() => ({ CONNECTED: 'success', DISCONNECTED: 'error', RECONNECTING: 'warning', CONNECTING: 'info' }[props.status]))
const label = computed(() => ({ CONNECTED: 'Connected', DISCONNECTED: 'Disconnected', RECONNECTING: 'Reconnecting…', CONNECTING: 'Connecting…' }[props.status]))
</script>
