<template>
  <v-card
    rounded="xl"
    :elevation="session.status === 'ACTIVE' ? 3 : 1"
    :border="session.status === 'ACTIVE' ? 'success md' : true"
    class="h-100"
  >
    <v-card-item>
      <template #prepend>
        <v-avatar :color="statusColor" size="44" rounded="lg" class="mr-2">
          <v-icon color="white" size="22">{{ statusIcon }}</v-icon>
        </v-avatar>
      </template>
      <v-card-title class="text-body-1 font-weight-bold">{{ session.title }}</v-card-title>
      <v-card-subtitle>{{ session.courseName }}</v-card-subtitle>
      <template #append>
        <StatusChip :status="session.status" />
      </template>
    </v-card-item>

    <v-card-text class="pt-0">
      <div class="d-flex flex-wrap gap-2 align-center mb-3">
        <LanguageBadge :lang="session.sourceLanguage" />
        <v-icon size="14" color="grey">mdi-arrow-right</v-icon>
        <LanguageBadge v-for="lang in session.targetLanguages" :key="lang" :lang="lang" />
        <v-spacer />
        <v-chip size="small" variant="tonal" prepend-icon="mdi-account-group">{{ session.participantCount }}</v-chip>
      </div>

      <div class="d-flex flex-wrap gap-3">
        <v-chip size="small" variant="text" prepend-icon="mdi-key-variant" class="text-caption">{{ session.joinCode }}</v-chip>
        <v-chip size="small" variant="text" :prepend-icon="session.accessMode === 'CLOSED' ? 'mdi-lock' : 'mdi-lock-open'" class="text-caption">{{ session.accessMode }}</v-chip>
        <v-chip v-if="session.startedAt" size="small" variant="text" prepend-icon="mdi-clock-start" class="text-caption">{{ formatDate(session.startedAt) }}</v-chip>
      </div>
    </v-card-text>

    <v-divider />
    <v-card-actions class="px-4">
      <v-btn v-if="['ACTIVE','PAUSED'].includes(session.status)" color="primary" variant="flat" size="small" prepend-icon="mdi-open-in-app" rounded="lg" :to="`/teacher/session/${session.id}`">Open</v-btn>
      <v-btn v-if="session.status === 'ENDED'" color="secondary" variant="tonal" size="small" prepend-icon="mdi-file-document" rounded="lg" :to="`/teacher/session/${session.id}/transcript`">Transcript</v-btn>
      <v-btn v-if="['CREATED','WAITING'].includes(session.status)" color="success" variant="flat" size="small" prepend-icon="mdi-play" rounded="lg" :to="`/teacher/session/${session.id}`">Launch</v-btn>
      <v-spacer />
      <v-btn size="small" variant="text" icon="mdi-dots-vertical" />
    </v-card-actions>
  </v-card>
</template>

<script setup>
import { computed } from 'vue'
import StatusChip from '../shared/StatusChip.vue'
import LanguageBadge from '../shared/LanguageBadge.vue'

const props = defineProps({ session: Object, classLabel: String })

const statusColor = computed(() => ({ CREATED: 'grey', WAITING: 'blue', ACTIVE: 'success', PAUSED: 'warning', ENDED: 'blue-grey', FAILED: 'error', EXPIRED: 'grey' }[props.session.status] ?? 'grey'))
const statusIcon = computed(() => ({ CREATED: 'mdi-clock-outline', WAITING: 'mdi-dots-horizontal-circle', ACTIVE: 'mdi-broadcast', PAUSED: 'mdi-pause', ENDED: 'mdi-check', FAILED: 'mdi-alert', EXPIRED: 'mdi-timer-off' }[props.session.status] ?? 'mdi-help'))

function formatDate(iso) { return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }
</script>
