<template>
  <v-container fluid class="pa-4 pa-sm-6">

    <!-- Header -->
    <div class="d-flex align-center flex-wrap gap-3 mb-6">
      <v-avatar size="48" rounded="xl" class="mr-1">
        <v-img src="/zana.png" alt="ZANA" cover />
      </v-avatar>
      <div class="flex-grow-1">
        <h1 class="text-h5 font-weight-bold">Dashboard Mësuesi</h1>
        <p class="text-body-2 text-medium-emphasis">Krijoni dhe menaxhoni sesiont tuaja live</p>
      </div>
      <v-btn color="primary" prepend-icon="mdi-plus" size="large" rounded="xl" elevation="2" @click="showCreate = true">
        Sesion i Ri
      </v-btn>
    </div>

    <!-- Stats cards -->
    <v-row class="mb-6">
      <v-col v-for="stat in stats" :key="stat.label" cols="6" sm="3">
        <v-card rounded="xl" elevation="0" border class="pa-4">
          <v-avatar :color="stat.color" size="40" rounded="lg" class="mb-3">
            <v-icon color="white" size="20">{{ stat.icon }}</v-icon>
          </v-avatar>
          <div class="text-h4 font-weight-bold mb-0">{{ stat.value }}</div>
          <div class="text-caption text-medium-emphasis mt-1">{{ stat.label }}</div>
        </v-card>
      </v-col>
    </v-row>

    <!-- Ready to launch -->
    <template v-if="pendingSessions.length">
      <div class="d-flex align-center gap-2 mb-3">
        <v-avatar color="blue" size="10" class="opacity-70" />
        <span class="text-body-1 font-weight-bold">Gati për Nisje</span>
        <v-chip size="small" color="blue" variant="tonal">{{ pendingSessions.length }}</v-chip>
      </div>
      <v-row class="mb-6">
        <v-col v-for="s in pendingSessions" :key="s.id" cols="12" sm="6" lg="4">
          <SessionCard :session="s" />
        </v-col>
      </v-row>
    </template>

    <!-- Active -->
    <template v-if="activeSessions.length">
      <div class="d-flex align-center gap-2 mb-3">
        <v-avatar color="success" size="10" />
        <span class="text-body-1 font-weight-bold">Aktive Tani</span>
        <v-chip size="small" color="success" variant="tonal">{{ activeSessions.length }}</v-chip>
      </div>
      <v-row class="mb-6">
        <v-col v-for="s in activeSessions" :key="s.id" cols="12" sm="6" lg="4">
          <SessionCard :session="s" />
        </v-col>
      </v-row>
    </template>

    <!-- Past -->
    <template v-if="endedSessions.length">
      <div class="d-flex align-center gap-2 mb-3">
        <span class="text-body-1 font-weight-bold">Sesiont e Kaluara</span>
        <v-chip size="small" variant="tonal">{{ endedSessions.length }}</v-chip>
      </div>
      <v-row>
        <v-col v-for="s in endedSessions" :key="s.id" cols="12" sm="6" lg="4">
          <SessionCard :session="s" />
        </v-col>
      </v-row>
    </template>

    <!-- Empty state -->
    <div v-if="!pendingSessions.length && !activeSessions.length && !endedSessions.length"
      class="d-flex flex-column align-center justify-center py-16 text-center">
      <v-sheet rounded="circle" color="primary-lighten-5" class="pa-6 mb-4">
        <v-icon size="56" color="primary">mdi-video-wireless-outline</v-icon>
      </v-sheet>
      <h3 class="text-h6 font-weight-bold mb-2">Nuk ka sesione ende</h3>
      <p class="text-body-2 text-medium-emphasis mb-6" style="max-width:320px">
        Krijoni sesionin e parë dhe studentët mund t'ju dëgjojnë live me përkthim automatik!
      </p>
      <v-btn color="primary" size="large" prepend-icon="mdi-plus" rounded="xl" @click="showCreate = true">
        Krijo Sesionin e Parë
      </v-btn>
    </div>

    <CreateSessionModal v-model="showCreate" @created="onCreated" />
  </v-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useSessionStore } from '../../stores/session.js'
import { useWorkspaceStore } from '../../stores/workspace.js'
import SessionCard from '../../components/teacher/SessionCard.vue'
import CreateSessionModal from '../../components/teacher/CreateSessionModal.vue'

const sessionStore = useSessionStore()
const workspaceStore = useWorkspaceStore()
const router = useRouter()
const showCreate = ref(false)

const pendingSessions = computed(() => sessionStore.sessions.filter(s => ['CREATED', 'WAITING'].includes(s.status)))
const activeSessions = computed(() => sessionStore.sessions.filter(s => ['ACTIVE', 'PAUSED'].includes(s.status)))
const endedSessions = computed(() => sessionStore.sessions.filter(s => ['ENDED', 'EXPIRED'].includes(s.status)))

const stats = computed(() => [
  { label: 'Gjithsej Sesione', value: sessionStore.sessions.length, icon: 'mdi-video-wireless', color: 'primary' },
  { label: 'Aktive Tani', value: activeSessions.value.filter(s => s.status === 'ACTIVE').length, icon: 'mdi-broadcast', color: 'success' },
  { label: 'Studentë Total', value: sessionStore.sessions.reduce((a, s) => a + s.participantCount, 0), icon: 'mdi-account-group', color: 'blue' },
  { label: 'Të Përfunduara', value: endedSessions.value.length, icon: 'mdi-check-circle', color: 'teal' },
])

function onCreated(id) { router.push(`/teacher/session/${id}`) }
</script>
