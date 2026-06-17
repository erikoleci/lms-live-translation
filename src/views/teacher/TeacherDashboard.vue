<template>
  <v-container fluid class="pa-6">

    <!-- Header -->
    <div class="d-flex align-center mb-5">
      <div>
        <h1 class="text-h5 font-weight-bold">Teacher Dashboard</h1>
        <p class="text-body-2 text-medium-emphasis">Manage your live translation sessions</p>
      </div>
      <v-spacer />
      <v-btn color="primary" size="large" prepend-icon="mdi-plus" @click="showCreate = true">
        New Session
      </v-btn>
    </div>

    <!-- Workspace / Class filter bar -->
    <v-card rounded="xl" elevation="0" border class="mb-5 pa-3">
      <div class="d-flex align-center gap-3 flex-wrap">
        <v-icon color="primary" class="ml-1">mdi-domain</v-icon>
        <span class="text-body-2 font-weight-medium text-medium-emphasis">Workspace:</span>
        <div class="d-flex gap-2 flex-wrap">
          <v-chip
            v-for="ws in workspaceStore.workspaces"
            :key="ws.id"
            :color="workspaceStore.activeWorkspaceId === ws.id ? 'primary' : 'default'"
            :variant="workspaceStore.activeWorkspaceId === ws.id ? 'flat' : 'outlined'"
            size="small"
            class="font-weight-medium"
            @click="workspaceStore.setActiveWorkspace(ws.id)"
          >
            {{ ws.name }}
          </v-chip>
        </div>
        <v-divider vertical class="mx-1" style="height:20px" />
        <v-icon color="teal">mdi-google-classroom</v-icon>
        <span class="text-body-2 font-weight-medium text-medium-emphasis">Class:</span>
        <div class="d-flex gap-2 flex-wrap">
          <v-chip
            color="default"
            :variant="workspaceStore.activeClassId === null ? 'flat' : 'outlined'"
            size="small"
            @click="workspaceStore.setActiveClass(null)"
          >
            All
          </v-chip>
          <v-chip
            v-for="cls in activeWorkspaceClasses"
            :key="cls.id"
            :color="workspaceStore.activeClassId === cls.id ? 'teal' : 'default'"
            :variant="workspaceStore.activeClassId === cls.id ? 'flat' : 'outlined'"
            size="small"
            class="font-weight-medium"
            @click="workspaceStore.setActiveClass(cls.id)"
          >
            {{ cls.name }}
          </v-chip>
        </div>
      </div>
    </v-card>

    <!-- Stats row -->
    <v-row class="mb-5">
      <v-col v-for="stat in stats" :key="stat.label" cols="6" sm="3">
        <v-card rounded="xl" elevation="0" border class="text-center pa-4">
          <v-icon :color="stat.color" size="28" class="mb-1">{{ stat.icon }}</v-icon>
          <p class="text-h5 font-weight-bold">{{ stat.value }}</p>
          <p class="text-caption text-medium-emphasis">{{ stat.label }}</p>
        </v-card>
      </v-col>
    </v-row>

    <!-- Active & Ongoing sessions -->
    <template v-if="activeSessions.length">
      <div class="d-flex align-center mb-3">
        <div class="live-dot mr-2" />
        <h2 class="text-body-1 font-weight-bold">Active & Ongoing</h2>
        <v-chip size="small" color="success" variant="tonal" class="ml-2">{{ activeSessions.length }}</v-chip>
      </div>
      <v-row class="mb-5">
        <v-col v-for="session in activeSessions" :key="session.id" cols="12" md="6" lg="4">
          <SessionCard :session="session" :class-label="classLabel(session)" />
        </v-col>
      </v-row>
    </template>

    <!-- Past sessions -->
    <template v-if="endedSessions.length">
      <div class="d-flex align-center mb-3">
        <h2 class="text-body-1 font-weight-bold">Past Sessions</h2>
        <v-chip size="small" variant="tonal" class="ml-2">{{ endedSessions.length }}</v-chip>
      </div>
      <v-row>
        <v-col v-for="session in endedSessions" :key="session.id" cols="12" md="6" lg="4">
          <SessionCard :session="session" :class-label="classLabel(session)" />
        </v-col>
      </v-row>
    </template>

    <!-- Empty state -->
    <div v-if="!filteredSessions.length" class="empty-state">
      <v-icon size="72" color="grey-lighten-2">mdi-video-wireless-outline</v-icon>
      <h3 class="text-h6 text-medium-emphasis mt-4">No sessions yet</h3>
      <p class="text-body-2 text-disabled">Create a session in the selected class to get started.</p>
      <v-btn color="primary" class="mt-4" prepend-icon="mdi-plus" @click="showCreate = true">Create Session</v-btn>
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

const activeWorkspaceClasses = computed(() =>
  workspaceStore.getClassesForWorkspace(workspaceStore.activeWorkspaceId)
)

const filteredSessions = computed(() => {
  let s = sessionStore.sessions
  if (workspaceStore.activeClassId) {
    s = s.filter(sess => sess.classId === workspaceStore.activeClassId)
  }
  return s
})

const activeSessions = computed(() =>
  filteredSessions.value.filter(s => s.status === 'ACTIVE' || s.status === 'PAUSED' || s.status === 'WAITING')
)

const endedSessions = computed(() =>
  filteredSessions.value.filter(s => s.status === 'ENDED' || s.status === 'EXPIRED')
)

const stats = computed(() => [
  { label: 'Total Sessions', value: sessionStore.sessions.length, icon: 'mdi-video-wireless', color: 'primary' },
  { label: 'Active Now', value: sessionStore.activeSessions.filter(s => s.status === 'ACTIVE').length, icon: 'mdi-broadcast', color: 'success' },
  { label: 'Total Participants', value: sessionStore.sessions.reduce((a, s) => a + s.participantCount, 0), icon: 'mdi-account-group', color: 'blue' },
  { label: 'Completed', value: sessionStore.endedSessions.length, icon: 'mdi-check-circle', color: 'teal' },
])

function classLabel(session) {
  if (!session.classId) return null
  const cls = workspaceStore.classes.find(c => c.id === session.classId)
  return cls ? cls.name : null
}

function onCreated(id) {
  router.push(`/teacher/session/${id}`)
}
</script>

<style scoped>
.live-dot { width: 10px; height: 10px; border-radius: 50%; background: #4caf50; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(76,175,80,0.4); } 50% { opacity: 0.8; box-shadow: 0 0 0 6px rgba(76,175,80,0); } }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 24px; text-align: center; }
</style>
