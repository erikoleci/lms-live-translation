<template>
  <v-card
    rounded="xl"
    :elevation="session.status === 'ACTIVE' ? 4 : 1"
    class="session-card"
    :class="{ 'session-card--active': session.status === 'ACTIVE' }"
  >
    <v-card-item>
      <template #prepend>
        <v-avatar :color="statusColor" size="44" class="mr-2">
          <v-icon :color="session.status === 'ACTIVE' ? 'white' : 'white'" size="22">{{ statusIcon }}</v-icon>
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
        <v-chip size="small" variant="text" prepend-icon="mdi-account-group">
          {{ session.participantCount }}
        </v-chip>
      </div>

      <div class="d-flex flex-wrap gap-2 text-caption text-medium-emphasis">
        <span>
          <v-icon size="12">mdi-key-variant</v-icon>
          {{ session.joinCode }}
        </span>
        <span>
          <v-icon size="12">mdi-lock{{ session.accessMode === 'CLOSED' ? '' : '-open' }}</v-icon>
          {{ session.accessMode }}
        </span>
        <span v-if="session.startedAt">
          <v-icon size="12">mdi-clock-start</v-icon>
          {{ formatDate(session.startedAt) }}
        </span>
      </div>
    </v-card-text>

    <v-divider />
    <v-card-actions class="px-4">
      <v-btn
        v-if="session.status === 'ACTIVE' || session.status === 'PAUSED'"
        color="primary"
        variant="flat"
        size="small"
        prepend-icon="mdi-open-in-app"
        :to="`/teacher/session/${session.id}`"
      >
        Open Session
      </v-btn>
      <v-btn
        v-if="session.status === 'ENDED'"
        color="secondary"
        variant="tonal"
        size="small"
        prepend-icon="mdi-file-document"
        :to="`/teacher/session/${session.id}/transcript`"
      >
        View Transcript
      </v-btn>
      <v-btn
        v-if="session.status === 'CREATED' || session.status === 'WAITING'"
        color="success"
        variant="flat"
        size="small"
        prepend-icon="mdi-play"
        :to="`/teacher/session/${session.id}`"
      >
        Launch
      </v-btn>
      <v-spacer />
      <v-btn size="small" variant="text" icon="mdi-dots-vertical" />
    </v-card-actions>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Session } from '../../types'
import StatusChip from '../shared/StatusChip.vue'
import LanguageBadge from '../shared/LanguageBadge.vue'

const props = defineProps<{ session: Session }>()

const statusColor = computed(() => ({
  CREATED: 'grey',
  WAITING: 'blue',
  ACTIVE: 'success',
  PAUSED: 'warning',
  ENDED: 'blue-grey',
  FAILED: 'error',
  EXPIRED: 'grey',
}[props.session.status] ?? 'grey'))

const statusIcon = computed(() => ({
  CREATED: 'mdi-clock-outline',
  WAITING: 'mdi-dots-horizontal-circle',
  ACTIVE: 'mdi-broadcast',
  PAUSED: 'mdi-pause',
  ENDED: 'mdi-check',
  FAILED: 'mdi-alert',
  EXPIRED: 'mdi-timer-off',
}[props.session.status] ?? 'mdi-help'))

function formatDate(iso: string) {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.session-card {
  transition: box-shadow 0.2s, transform 0.2s;
}
.session-card:hover { transform: translateY(-2px); }
.session-card--active {
  border: 2px solid #4caf50 !important;
}
</style>
