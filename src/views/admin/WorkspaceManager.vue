<template>
  <v-container fluid class="pa-6">
    <div class="d-flex align-center mb-6">
      <div>
        <h1 class="text-h5 font-weight-bold">Workspaces &amp; Classes</h1>
        <p class="text-body-2 text-medium-emphasis">Manage schools, institutes, and their classes</p>
      </div>
      <v-spacer />
      <v-btn color="primary" variant="flat" prepend-icon="mdi-plus" rounded="lg" @click="showCreateWs = true">
        New Workspace
      </v-btn>
    </div>

    <v-row>
      <!-- Workspace list -->
      <v-col cols="12" md="4">
        <v-card rounded="xl" elevation="0" border>
          <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
            <v-icon start color="primary">mdi-domain</v-icon>
            Workspaces
          </v-card-title>
          <v-list nav density="comfortable" class="px-2 pb-3">
            <v-list-item
              v-for="ws in store.workspaces" :key="ws.id"
              rounded="lg" :active="selectedWs?.id === ws.id" color="primary" class="mb-1"
              @click="selectedWs = ws"
            >
              <template #prepend>
                <v-avatar color="primary" size="36" rounded="lg">
                  <span class="text-caption font-weight-bold text-white">{{ ws.name.slice(0, 2) }}</span>
                </v-avatar>
              </template>
              <v-list-item-title class="font-weight-bold text-body-2">{{ ws.name }}</v-list-item-title>
              <v-list-item-subtitle class="text-caption">{{ ws.description }}</v-list-item-subtitle>
              <template #append>
                <v-chip size="x-small" color="primary" variant="tonal">
                  {{ store.getClassesForWorkspace(ws.id).length }} classes
                </v-chip>
              </template>
            </v-list-item>
          </v-list>
        </v-card>
      </v-col>

      <!-- Classes -->
      <v-col cols="12" md="8">
        <v-card rounded="xl" elevation="0" border>
          <v-card-title class="text-body-1 font-weight-bold pt-4 px-4 d-flex align-center">
            <v-icon start color="primary">mdi-google-classroom</v-icon>
            Classes
            <span v-if="selectedWs" class="ml-2 text-medium-emphasis text-body-2">— {{ selectedWs.name }}</span>
            <v-spacer />
            <v-btn v-if="selectedWs" size="small" color="primary" variant="tonal" prepend-icon="mdi-plus" rounded="lg" @click="showCreateClass = true">Add Class</v-btn>
          </v-card-title>

          <div v-if="!selectedWs" class="text-center py-12 px-4">
            <v-icon size="48" color="grey-lighten-2">mdi-cursor-default-click</v-icon>
            <p class="text-body-2 text-disabled mt-3">Select a workspace to see its classes</p>
          </div>

          <v-card-text v-else class="px-4 pb-4">
            <v-row>
              <v-col v-for="cls in selectedClasses" :key="cls.id" cols="12" sm="6">
                <v-card rounded="xl" elevation="0" border hover class="pa-4 h-100">
                  <div class="d-flex align-center gap-3 mb-3">
                    <v-avatar color="teal" size="40" rounded="lg">
                      <v-icon color="white" size="20">mdi-google-classroom</v-icon>
                    </v-avatar>
                    <div class="flex-grow-1 min-w-0">
                      <p class="text-body-2 font-weight-bold mb-0 text-truncate">{{ cls.name }}</p>
                      <p class="text-caption text-medium-emphasis text-truncate">{{ cls.description }}</p>
                    </div>
                    <v-btn icon="mdi-delete-outline" variant="text" size="small" color="error" @click="confirmDeleteClass(cls)" />
                  </div>

                  <v-divider class="mb-3" />

                  <v-row density="compact">
                    <v-col v-for="stat in classStatItems(cls.id)" :key="stat.label" cols="4">
                      <v-sheet color="grey-lighten-5" rounded="lg" class="pa-2 text-center">
                        <p class="text-h6 font-weight-bold mb-0" :class="`text-${stat.color}`">{{ stat.value }}</p>
                        <p class="text-caption text-medium-emphasis mb-0">{{ stat.label }}</p>
                      </v-sheet>
                    </v-col>
                  </v-row>
                </v-card>
              </v-col>

              <v-col v-if="!selectedClasses.length" cols="12">
                <div class="text-center py-8">
                  <v-icon size="48" color="grey-lighten-2">mdi-google-classroom</v-icon>
                  <p class="text-body-2 text-disabled mt-3">No classes yet. Add one above.</p>
                </div>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Create Workspace Dialog -->
    <v-dialog v-model="showCreateWs" max-width="440" persistent>
      <v-card rounded="xl">
        <v-card-title class="pt-6 px-6 d-flex align-center gap-2">
          <v-avatar color="primary" size="32" rounded="lg"><v-icon color="white" size="18">mdi-plus</v-icon></v-avatar>
          New Workspace
          <v-spacer /><v-btn icon="mdi-close" variant="text" @click="showCreateWs = false" />
        </v-card-title>
        <v-card-text class="px-6">
          <v-text-field v-model="wsForm.name" label="Workspace Name" placeholder="e.g. UNIZKM" prepend-inner-icon="mdi-domain" class="mb-3" />
          <v-text-field v-model="wsForm.description" label="Description" placeholder="e.g. Universiteti i Gjakovës" prepend-inner-icon="mdi-text" />
        </v-card-text>
        <v-card-actions class="px-6 pb-6 gap-2">
          <v-spacer />
          <v-btn variant="text" @click="showCreateWs = false">Cancel</v-btn>
          <v-btn color="primary" variant="flat" rounded="lg" @click="createWorkspace">Create</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Create Class Dialog -->
    <v-dialog v-model="showCreateClass" max-width="440" persistent>
      <v-card rounded="xl">
        <v-card-title class="pt-6 px-6 d-flex align-center gap-2">
          <v-avatar color="teal" size="32" rounded="lg"><v-icon color="white" size="18">mdi-plus</v-icon></v-avatar>
          New Class — {{ selectedWs?.name }}
          <v-spacer /><v-btn icon="mdi-close" variant="text" @click="showCreateClass = false" />
        </v-card-title>
        <v-card-text class="px-6">
          <v-text-field v-model="classForm.name" label="Class Name" placeholder="e.g. Klasa 001" prepend-inner-icon="mdi-google-classroom" class="mb-3" />
          <v-text-field v-model="classForm.description" label="Description" placeholder="e.g. Informatikë — Viti I" prepend-inner-icon="mdi-text" />
        </v-card-text>
        <v-card-actions class="px-6 pb-6 gap-2">
          <v-spacer />
          <v-btn variant="text" @click="showCreateClass = false">Cancel</v-btn>
          <v-btn color="teal" variant="flat" rounded="lg" @click="createClass">Create</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirm -->
    <v-dialog v-model="deleteDialog" max-width="380">
      <v-card rounded="xl">
        <v-card-text class="pa-6 text-center">
          <v-icon size="48" color="error" class="mb-3">mdi-alert-circle</v-icon>
          <p class="text-body-1 font-weight-bold">Delete "{{ deletingClass?.name }}"?</p>
          <p class="text-body-2 text-medium-emphasis mt-1">All session data linked to this class will be unlinked. This cannot be undone.</p>
        </v-card-text>
        <v-card-actions class="px-6 pb-6 gap-2">
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" variant="flat" rounded="lg" @click="doDelete">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useWorkspaceStore } from '../../stores/workspace.js'

const store = useWorkspaceStore()
const selectedWs = ref(store.workspaces[0] ?? null)
const showCreateWs = ref(false)
const showCreateClass = ref(false)
const deleteDialog = ref(false)
const deletingClass = ref(null)
const wsForm = ref({ name: '', description: '' })
const classForm = ref({ name: '', description: '' })

const selectedClasses = computed(() => selectedWs.value ? store.getClassesForWorkspace(selectedWs.value.id) : [])

function classStats(classId) {
  const rows = store.getStatsForClass(classId)
  return { sessionCount: rows.reduce((a, b) => a + b.sessionCount, 0), totalMinutes: rows.reduce((a, b) => a + b.totalMinutes, 0), participantCount: rows.reduce((a, b) => a + b.participantCount, 0) }
}

function classStatItems(classId) {
  const s = classStats(classId)
  return [
    { label: 'Sessions', value: s.sessionCount, color: 'primary' },
    { label: 'Total Hours', value: formatHours(s.totalMinutes), color: 'success' },
    { label: 'Participants', value: s.participantCount, color: 'orange' },
  ]
}

function formatHours(min) { const h = Math.floor(min / 60); const m = min % 60; return `${h}h ${m}m` }

function createWorkspace() {
  if (!wsForm.value.name.trim()) return
  const ws = store.createWorkspace(wsForm.value); selectedWs.value = ws
  wsForm.value = { name: '', description: '' }; showCreateWs.value = false
}
function createClass() {
  if (!classForm.value.name.trim() || !selectedWs.value) return
  store.createClass({ ...classForm.value, workspaceId: selectedWs.value.id })
  classForm.value = { name: '', description: '' }; showCreateClass.value = false
}
function confirmDeleteClass(cls) { deletingClass.value = cls; deleteDialog.value = true }
function doDelete() { if (deletingClass.value) store.deleteClass(deletingClass.value.id); deleteDialog.value = false; deletingClass.value = null }
</script>
