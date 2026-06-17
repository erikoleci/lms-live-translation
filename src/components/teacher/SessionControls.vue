<template>
  <div class="session-controls">
    <div class="d-flex flex-column gap-2">
      <v-btn
        v-if="status === 'WAITING' || status === 'CREATED'"
        color="success" variant="flat" block prepend-icon="mdi-broadcast"
        :loading="loading" @click="emit('start')"
      >Start Session</v-btn>

      <div v-if="status === 'ACTIVE' || status === 'PAUSED'" class="d-flex gap-2">
        <v-btn
          v-if="status === 'ACTIVE'"
          color="warning" variant="flat" class="flex-grow-1"
          prepend-icon="mdi-pause" :loading="loading" @click="emit('pause')"
        >Pause</v-btn>
        <v-btn
          v-if="status === 'PAUSED'"
          color="success" variant="flat" class="flex-grow-1"
          prepend-icon="mdi-play" :loading="loading" @click="emit('resume')"
        >Resume</v-btn>
        <v-btn
          color="error" variant="tonal" class="flex-grow-1"
          prepend-icon="mdi-stop" :loading="loading" @click="confirmEnd = true"
        >End</v-btn>
      </div>

      <v-chip
        v-if="status === 'ENDED'"
        color="default" variant="tonal" block
        prepend-icon="mdi-check-circle" class="justify-center"
      >Session Ended</v-chip>
    </div>

    <v-dialog v-model="confirmEnd" max-width="400">
      <v-card rounded="xl">
        <v-card-title class="pt-6 px-6">
          <v-icon color="error" class="mr-2">mdi-alert</v-icon>
          End Session?
        </v-card-title>
        <v-card-text class="px-6">
          This will close the session for all <strong>{{ participantCount }}</strong> connected students. Transcripts will be finalized.
        </v-card-text>
        <v-card-actions class="px-6 pb-6 gap-2">
          <v-spacer />
          <v-btn variant="text" @click="confirmEnd = false">Cancel</v-btn>
          <v-btn color="error" variant="flat" @click="() => { confirmEnd = false; emit('end') }">End Session</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  status: String,
  loading: { type: Boolean, default: false },
  micActive: Boolean,
  muted: Boolean,
  participantCount: Number,
})

const emit = defineEmits(['start', 'pause', 'resume', 'end', 'toggleMic'])
const confirmEnd = ref(false)
</script>
