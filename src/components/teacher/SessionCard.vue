<template>
  <v-card rounded="xl" :elevation="isActive ? 4 : 1" :border="isActive ? 'success md' : true" class="h-100">

    <!-- Status banner for active -->
    <div v-if="isActive" class="d-flex align-center gap-2 px-4 py-2"
      style="background: linear-gradient(90deg, #e8f5e9, #f1f8e9)">
      <v-badge dot color="success" inline />
      <span class="text-caption font-weight-bold text-success">LIVE TANI</span>
      <v-spacer />
      <span class="text-caption text-medium-emphasis">{{ session.participantCount }} studentë</span>
    </div>

    <v-card-item class="pt-3">
      <template #prepend>
        <v-avatar :color="statusColor" size="44" rounded="xl" class="mr-2">
          <v-icon color="white" size="22">{{ statusIcon }}</v-icon>
        </v-avatar>
      </template>
      <v-card-title class="text-body-1 font-weight-bold">{{ session.title }}</v-card-title>
      <v-card-subtitle>{{ session.courseName }}</v-card-subtitle>
      <template #append>
        <StatusChip :status="session.status" />
      </template>
    </v-card-item>

    <v-card-text class="pt-1 pb-2">
      <!-- Languages -->
      <div class="d-flex align-center gap-1 flex-wrap mb-3">
        <LanguageBadge :lang="session.sourceLanguage" />
        <v-icon size="14" color="grey">mdi-arrow-right</v-icon>
        <LanguageBadge v-for="lang in session.targetLanguages" :key="lang" :lang="lang" />
      </div>

      <!-- Info chips -->
      <div class="d-flex flex-wrap gap-2">
        <v-chip size="small" variant="tonal" color="primary" prepend-icon="mdi-key-variant"
          style="letter-spacing:2px; font-weight:700">{{ session.joinCode }}</v-chip>
        <v-chip size="small" variant="tonal"
          :prepend-icon="session.accessMode === 'CLOSED' ? 'mdi-lock' : 'mdi-lock-open-outline'">
          {{ session.accessMode === 'CLOSED' ? 'Privat' : 'I Hapur' }}
        </v-chip>
        <v-chip v-if="session.startedAt" size="small" variant="tonal" prepend-icon="mdi-clock-outline">
          {{ formatDate(session.startedAt) }}
        </v-chip>
      </div>
    </v-card-text>

    <v-divider />
    <v-card-actions class="px-4 py-3 gap-2">
      <v-btn v-if="['ACTIVE','PAUSED'].includes(session.status)"
        color="primary" variant="flat" prepend-icon="mdi-open-in-app" rounded="xl"
        :to="`/teacher/session/${session.id}`">Hap</v-btn>

      <v-btn v-if="session.status === 'ENDED'"
        color="secondary" variant="tonal" prepend-icon="mdi-file-document-outline" rounded="xl"
        :to="`/teacher/session/${session.id}/transcript`">Transcript</v-btn>

      <v-btn v-if="['CREATED','WAITING'].includes(session.status)"
        color="success" variant="flat" prepend-icon="mdi-play" rounded="xl"
        :to="`/teacher/session/${session.id}`">Fillo Sesionin</v-btn>

      <v-spacer />
      <v-btn size="small" variant="text" icon="mdi-dots-vertical" />
    </v-card-actions>
  </v-card>
</template>

<script setup>
import { computed } from 'vue'
import StatusChip from '../shared/StatusChip.vue'
import LanguageBadge from '../shared/LanguageBadge.vue'

const props = defineProps({ session: Object })

const isActive = computed(() => props.session.status === 'ACTIVE')
const statusColor = computed(() => ({
  CREATED: 'grey', WAITING: 'blue', ACTIVE: 'success',
  PAUSED: 'warning', ENDED: 'blue-grey', FAILED: 'error', EXPIRED: 'grey'
}[props.session.status] ?? 'grey'))
const statusIcon = computed(() => ({
  CREATED: 'mdi-clock-outline', WAITING: 'mdi-dots-horizontal-circle',
  ACTIVE: 'mdi-broadcast', PAUSED: 'mdi-pause', ENDED: 'mdi-check',
  FAILED: 'mdi-alert', EXPIRED: 'mdi-timer-off'
}[props.session.status] ?? 'mdi-help'))

function formatDate(iso) {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}
</script>
