<template>
  <v-container fluid class="pa-3 pa-sm-4 pa-md-6">

    <!-- Header -->
    <div class="d-flex align-center flex-wrap gap-3 mb-4 mb-sm-5">
      <div class="flex-grow-1">
        <h1 class="text-h6 text-sm-h5 font-weight-bold">Teacher Dashboard</h1>
        <p class="text-body-2 text-medium-emphasis">Manage your live translation sessions</p>
      </div>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="showCreate = true" rounded="lg">
        New Session
      </v-btn>
    </div>

    <!-- Workspace / Class filter bar -->
    <v-card rounded="xl" elevation="0" border class="mb-4 mb-sm-5">
      <div class="filter-bar pa-3 pa-sm-4">
        <div class="filter-group">
          <div class="filter-label">
            <v-icon color="primary" size="16">mdi-domain</v-icon>
            <span>Workspace</span>
          </div>
          <div class="chips-scroll">
            <v-chip
              v-for="ws in workspaceStore.workspaces"
              :key="ws.id"
              :color="workspaceStore.activeWorkspaceId === ws.id ? 'primary' : 'default'"
              :variant="workspaceStore.activeWorkspaceId === ws.id ? 'flat' : 'outlined'"
              size="small"
              class="font-weight-medium flex-shrink-0"
              @click="workspaceStore.setActiveWorkspace(ws.id)"
            >{{ ws.name }}</v-chip>
          </div>
        </div>

        <v-divider class="my-2 my-sm-0 mx-sm-2" :vertical="!smAndDown" style="max-height:28px; align-self:center" />

        <div class="filter-group">
          <div class="filter-label">
            <v-icon color="teal" size="16">mdi-google-classroom</v-icon>
            <span>Class</span>
          </div>
          <div class="chips-scroll">
            <v-chip
              color="default" :variant="workspaceStore.activeClassId === null ? 'flat' : 'outlined'"
              size="small" class="flex-shrink-0" @click="workspaceStore.setActiveClass(null)"
            >All</v-chip>
            <v-chip
              v-for="cls in activeWorkspaceClasses" :key="cls.id"
              :color="workspaceStore.activeClassId === cls.id ? 'teal' : 'default'"
              :variant="workspaceStore.activeClassId === cls.id ? 'flat' : 'outlined'"
              size="small" class="font-weight-medium flex-shrink-0"
              @click="workspaceStore.setActiveClass(cls.id)"
            >{{ cls.name }}</v-chip>
          </div>
        </div>
      </div>
    </v-card>

    <!-- Stats row -->
    <v-row class="mb-4 mb-sm-5">
      <v-col v-for="stat in stats" :key="stat.label" cols="6" sm="3">
        <v-card rounded="xl" elevation="0" border class="stat-card pa-3 pa-sm-4">
          <div class="d-flex align-center justify-space-between mb-1">
            <v-avatar :color="`${stat.color}-lighten-4`" size="36" rounded="lg">
              <v-icon :color="stat.color" size="18">{{ stat.icon }}</v-icon>
            </v-avatar>
          </div>
          <p class="text-h5 font-weight-bold mt-1 mb-0">{{ stat.value }}</p>
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
      <v-row class="mb-4 mb-sm-5">
        <v-col v-for="session in activeSessions" :key="session.id" cols="12" sm="6" lg="4">
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
        <v-col v-for="session in endedSessions" :key="session.id" cols="12" sm="6" lg="4">
          <SessionCard :session="session" :class-label="classLabel(session)" />
        </v-col>
      </v-row>
    </template>

    <!-- Empty state -->
    <div v-if="!filteredSessions.length" class="empty-state">
      <v-icon size="64" color="grey-lighten-2">mdi-video-wireless-outline</v-icon>
      <h3 class="text-h6 text-medium-emphasis mt-4">No sessions yet</h3>
      <p class="text-body-2 text-disabled text-center">Create your first session to get started.</p>
      <v-btn color="primary" class="mt-4" prepend-icon="mdi-plus" @click="showCreate = true" rounded="lg">Create Session</v-btn>
    </div>

    <CreateSessionModal v-model="showCreate" @created="onCreated" />
  </v-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useSessionStore } from '../../stores/session.js'
import { useWorkspaceStore } from '../../stores/workspace.js'
import SessionCard from '../../components/teacher/SessionCard.vue'
import CreateSessionModal from '../../components/teacher/CreateSessionModal.vue'

const sessionStore = useSessionStore()
const workspaceStore = useWorkspaceStore()
const router = useRouter()
const { smAndDown } = useDisplay()
const showCreate = ref(false)

const activeWorkspaceClasses = computed(() =>
  workspaceStore.getClassesForWorkspace(workspaceStore.activeWorkspaceId)
)
const filteredSessions = computed(() => {
  let s = sessionStore.sessions
  if (workspaceStore.activeClassId) s = s.filter(sess => sess.classId === workspaceStore.activeClassId)
  return s
})
const activeSessions = computed(() => filteredSessions.value.filter(s => ['ACTIVE','PAUSED','WAITING'].includes(s.status)))
const endedSessions = computed(() => filteredSessions.value.filter(s => ['ENDED','EXPIRED'].includes(s.status)))

const stats = computed(() => [
  { label: 'Total Sessions', value: sessionStore.sessions.length, icon: 'mdi-video-wireless', color: 'primary' },
  { label: 'Active Now', value: sessionStore.activeSessions.filter(s => s.status === 'ACTIVE').length, icon: 'mdi-broadcast', color: 'success' },
  { label: 'Participants', value: sessionStore.sessions.reduce((a, s) => a + s.participantCount, 0), icon: 'mdi-account-group', color: 'blue' },
  { label: 'Completed', value: sessionStore.endedSessions.length, icon: 'mdi-check-circle', color: 'teal' },
])

function classLabel(session) {
  const cls = workspaceStore.classes.find(c => c.id === session.classId)
  return cls?.name ?? null
}
function onCreated(id) { router.push(`/teacher/session/${id}`) }
</script>

<style scoped>
.filter-bar { display: flex; align-items: flex-start; flex-wrap: wrap; gap: 8px; }
.filter-group { display: flex; align-items: center; gap: 8px; min-width: 0; flex: 1; }
.filter-label { display: flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 600; color: #777; white-space: nowrap; }
.chips-scroll { display: flex; gap: 6px; flex-wrap: wrap; }

.stat-card { transition: box-shadow 0.2s; }
.stat-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08) !important; }

.live-dot { width: 10px; height: 10px; border-radius: 50%; background: #4caf50; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%,100% { opacity:1; box-shadow:0 0 0 0 rgba(76,175,80,0.4); } 50% { opacity:0.8; box-shadow:0 0 0 6px rgba(76,175,80,0); } }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 24px; text-align: center; }
</style>
