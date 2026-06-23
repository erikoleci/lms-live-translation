<template>
  <v-container fluid class="pa-3 pa-sm-4 pa-md-6">

    <!-- Header -->
    <div class="d-flex align-center flex-wrap gap-3 mb-4 mb-sm-5">
      <div class="flex-grow-1">
        <h1 class="text-h6 text-sm-h5 font-weight-bold">Teacher Dashboard</h1>
        <p class="text-body-2 text-medium-emphasis">Manage your live translation sessions</p>
      </div>
      <v-btn color="primary" prepend-icon="mdi-plus" rounded="lg" @click="showCreate = true">
        New Session
      </v-btn>
    </div>

    <!-- Workspace / Class filter bar — only shown when workspaces exist -->
    <v-card v-if="workspaceStore.workspaces.length" rounded="xl" elevation="0" border class="mb-4 mb-sm-5">
      <div class="d-flex align-start flex-wrap gap-2 pa-3 pa-sm-4">
        <!-- Workspace group -->
        <div class="d-flex align-center gap-2 flex-grow-1 min-w-0">
          <div class="d-flex align-center gap-1 text-caption font-weight-bold text-medium-emphasis" style="white-space:nowrap">
            <v-icon color="primary" size="16">mdi-domain</v-icon>
            Workspace
          </div>
          <div class="d-flex gap-2 flex-wrap">
            <v-chip
              v-for="ws in workspaceStore.workspaces" :key="ws.id"
              :color="workspaceStore.activeWorkspaceId === ws.id ? 'primary' : 'default'"
              :variant="workspaceStore.activeWorkspaceId === ws.id ? 'flat' : 'outlined'"
              size="small" class="font-weight-medium"
              @click="workspaceStore.setActiveWorkspace(ws.id)"
            >{{ ws.name }}</v-chip>
          </div>
        </div>

        <v-divider v-if="activeWorkspaceClasses.length" class="my-1 mx-1 d-none d-sm-block" vertical style="max-height:28px; align-self:center" />

        <!-- Class group — only shown when workspace has classes -->
        <div v-if="activeWorkspaceClasses.length" class="d-flex align-center gap-2 flex-grow-1 min-w-0">
          <div class="d-flex align-center gap-1 text-caption font-weight-bold text-medium-emphasis" style="white-space:nowrap">
            <v-icon color="teal" size="16">mdi-google-classroom</v-icon>
            Class
          </div>
          <div class="d-flex gap-2 flex-wrap">
            <v-chip
              color="default" size="small"
              :variant="workspaceStore.activeClassId === null ? 'flat' : 'outlined'"
              @click="workspaceStore.setActiveClass(null)"
            >All</v-chip>
            <v-chip
              v-for="cls in activeWorkspaceClasses" :key="cls.id"
              :color="workspaceStore.activeClassId === cls.id ? 'teal' : 'default'"
              :variant="workspaceStore.activeClassId === cls.id ? 'flat' : 'outlined'"
              size="small" class="font-weight-medium"
              @click="workspaceStore.setActiveClass(cls.id)"
            >{{ cls.name }}</v-chip>
          </div>
        </div>
      </div>
    </v-card>

    <!-- Stats -->
    <v-row class="mb-4 mb-sm-5">
      <v-col v-for="stat in stats" :key="stat.label" cols="6" sm="3">
        <v-card rounded="xl" elevation="0" border class="pa-3 pa-sm-4">
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

    <!-- Pending sessions -->
    <template v-if="pendingSessions.length">
      <div class="d-flex align-center gap-2 mb-3">
        <v-icon size="10" color="blue">mdi-circle-outline</v-icon>
        <h2 class="text-body-1 font-weight-bold">Ready to Launch</h2>
        <v-chip size="small" color="blue" variant="tonal">{{ pendingSessions.length }}</v-chip>
      </div>
      <v-row class="mb-4 mb-sm-5">
        <v-col v-for="session in pendingSessions" :key="session.id" cols="12" sm="6" lg="4">
          <SessionCard :session="session" :class-label="classLabel(session)" />
        </v-col>
      </v-row>
    </template>

    <!-- Active sessions -->
    <template v-if="activeSessions.length">
      <div class="d-flex align-center gap-2 mb-3">
        <v-icon size="10" color="success">mdi-circle</v-icon>
        <h2 class="text-body-1 font-weight-bold">Active &amp; Ongoing</h2>
        <v-chip size="small" color="success" variant="tonal">{{ activeSessions.length }}</v-chip>
      </div>
      <v-row class="mb-4 mb-sm-5">
        <v-col v-for="session in activeSessions" :key="session.id" cols="12" sm="6" lg="4">
          <SessionCard :session="session" :class-label="classLabel(session)" />
        </v-col>
      </v-row>
    </template>

    <!-- Past sessions -->
    <template v-if="endedSessions.length">
      <div class="d-flex align-center gap-2 mb-3">
        <h2 class="text-body-1 font-weight-bold">Past Sessions</h2>
        <v-chip size="small" variant="tonal">{{ endedSessions.length }}</v-chip>
      </div>
      <v-row>
        <v-col v-for="session in endedSessions" :key="session.id" cols="12" sm="6" lg="4">
          <SessionCard :session="session" :class-label="classLabel(session)" />
        </v-col>
      </v-row>
    </template>

    <!-- Empty state -->
    <div v-if="!pendingSessions.length && !activeSessions.length && !endedSessions.length" class="d-flex flex-column align-center justify-center py-16 text-center">
      <v-icon size="64" color="grey-lighten-2">mdi-video-wireless-outline</v-icon>
      <h3 class="text-h6 text-medium-emphasis mt-4">No sessions yet</h3>
      <p class="text-body-2 text-disabled">Create your first session to get started.</p>
      <v-btn color="primary" class="mt-4" prepend-icon="mdi-plus" rounded="lg" @click="showCreate = true">Create Session</v-btn>
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
const pendingSessions = computed(() => filteredSessions.value.filter(s => ['CREATED', 'WAITING'].includes(s.status)))
const activeSessions = computed(() => filteredSessions.value.filter(s => ['ACTIVE', 'PAUSED'].includes(s.status)))
const endedSessions = computed(() => filteredSessions.value.filter(s => ['ENDED', 'EXPIRED'].includes(s.status)))

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
