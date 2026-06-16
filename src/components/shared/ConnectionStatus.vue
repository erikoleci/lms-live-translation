<template>
  <v-chip :color="color" size="small" variant="flat" class="connection-status">
    <template #prepend>
      <span class="status-dot" :class="dotClass" />
    </template>
    {{ label }}
  </v-chip>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING' | 'CONNECTING' }>()

const color = computed(() => ({
  CONNECTED: 'success',
  DISCONNECTED: 'error',
  RECONNECTING: 'warning',
  CONNECTING: 'info',
}[props.status]))

const label = computed(() => ({
  CONNECTED: 'Connected',
  DISCONNECTED: 'Disconnected',
  RECONNECTING: 'Reconnecting…',
  CONNECTING: 'Connecting…',
}[props.status]))

const dotClass = computed(() => ({
  CONNECTED: 'dot-pulse-green',
  DISCONNECTED: 'dot-static-red',
  RECONNECTING: 'dot-pulse-orange',
  CONNECTING: 'dot-pulse-blue',
}[props.status]))
</script>

<style scoped>
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;
}
.dot-pulse-green { background: #4caf50; animation: pulse 1.5s infinite; }
.dot-pulse-orange { background: #ff9800; animation: pulse 1s infinite; }
.dot-pulse-blue { background: #2196f3; animation: pulse 1s infinite; }
.dot-static-red { background: #f44336; }
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
