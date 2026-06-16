<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <div class="d-flex align-center mb-6">
      <div>
        <h1 class="text-h5 font-weight-bold">My Sessions</h1>
        <p class="text-body-2 text-medium-emphasis">Manage your live translation sessions</p>
      </div>
      <v-spacer />
      <v-btn
        color="primary"
        size="large"
        prepend-icon="mdi-plus"
        @click="showCreate = true"
      >
        New Session
      </v-btn>
    </div>

    <!-- Stats Strip -->
    <v-row class="mb-6">
      <v-col cols="6" sm="3" v-for="stat in stats" :key="stat.label">
        <v-card rounded="xl" elevation="0" border class="text-center pa-4">
          <v-icon :color="stat.color" size="28" class="mb-1">{{ stat.icon }}</v-icon>
          <p class="text-h5 font-weight-bold">{{ stat.value }}</p>
          <p class="text-caption text-medium-emphasis">{{ stat.label }}</p>
        </v-card>
      </v-col>
    </v-row>

    <!-- Active Sessions -->
    <template v-if="sessionStore.activeSessions.length">
      <div class="section-header mb-3">
        <div class="live-dot" />
        <h2 class="text-body-1 font-weight-bold ml-2">Active & Ongoing</h2>
        <v-chip size="small" color="success" variant="tonal" class="ml-2">
          {{ sessionStore.activeSessions.length }}
        </v-chip>
      </div>
      <v-row class="mb-6">
        <v-col v-for="session in sessionStore.activeSessions" :key="session.id" cols="12" md="6" lg="4">
          <SessionCard :session="session" />
        </v-col>
      </v-row>
    </template>

    <!-- Past Sessions -->
    <template v-if="sessionStore.endedSessions.length">
      <div class="d-flex align-center mb-3">
        <h2 class="text-body-1 font-weight-bold">Past Sessions</h2>
        <v-chip size="small" variant="tonal" class="ml-2">{{ sessionStore.endedSessions.length }}</v-chip>
      </div>
      <v-row>
        <v-col v-for="session in sessionStore.endedSessions" :key="session.id" cols="12" md="6" lg="4">
          <SessionCard :session="session" />
        </v-col>
      </v-row>
    </template>

    <!-- Empty state -->
    <div v-if="!sessionStore.sessions.length" class="empty-state">
      <v-icon size="72" color="grey-lighten-2">mdi-video-wireless-outline</v-icon>
      <h3 class="text-h6 text-medium-emphasis mt-4">No sessions yet</h3>
      <p class="text-body-2 text-disabled">Create your first live translation session to get started.</p>
      <v-btn color="primary" class="mt-4" prepend-icon="mdi-plus" @click="showCreate = true">
        Create Session
      </v-btn>
    </div>

    <CreateSessionModal v-model="showCreate" @created="onCreated" />
  </v-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useSessionStore } from '../../stores/session'
import SessionCard from '../../components/teacher/SessionCard.vue'
import CreateSessionModal from '../../components/teacher/CreateSessionModal.vue'

const sessionStore = useSessionStore()
const router = useRouter()
const showCreate = ref(false)

const stats = computed(() => [
  {
    label: 'Total Sessions',
    value: sessionStore.sessions.length,
    icon: 'mdi-video-wireless',
    color: 'primary',
  },
  {
    label: 'Active Now',
    value: sessionStore.activeSessions.filter(s => s.status === 'ACTIVE').length,
    icon: 'mdi-broadcast',
    color: 'success',
  },
  {
    label: 'Total Participants',
    value: sessionStore.sessions.reduce((a, s) => a + s.participantCount, 0),
    icon: 'mdi-account-group',
    color: 'blue',
  },
  {
    label: 'Completed',
    value: sessionStore.endedSessions.length,
    icon: 'mdi-check-circle',
    color: 'teal',
  },
])

function onCreated(id: string) {
  router.push(`/teacher/session/${id}`)
}
</script>

<style scoped>
.section-header {
  display: flex;
  align-items: center;
}
.live-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #4caf50;
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(76,175,80,0.4); }
  50% { opacity: 0.8; box-shadow: 0 0 0 6px rgba(76,175,80,0); }
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  text-align: center;
}
</style>
