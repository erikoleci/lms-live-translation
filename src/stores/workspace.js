import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useWorkspaceStore = defineStore('workspace', () => {
  const workspaces = ref([])
  const classes = ref([])
  const monthlyStats = ref([])
  const activeWorkspaceId = ref(null)
  const activeClassId = ref(null)

  const activeWorkspace = computed(() =>
    workspaces.value.find(w => w.id === activeWorkspaceId.value) ?? null
  )

  function getClassesForWorkspace(workspaceId) {
    return classes.value.filter(c => c.workspaceId === workspaceId)
  }

  function getStatsForClass(classId, month) {
    return monthlyStats.value.filter(s => s.classId === classId && (!month || s.month === month))
  }

  function getStatsForWorkspace(workspaceId, month) {
    return monthlyStats.value.filter(s => s.workspaceId === workspaceId && (!month || s.month === month))
  }

  function getWorkspaceTotal(workspaceId, month) {
    const rows = getStatsForWorkspace(workspaceId, month)
    return {
      totalMinutes: rows.reduce((a, b) => a + b.totalMinutes, 0),
      sessionCount: rows.reduce((a, b) => a + b.sessionCount, 0),
      participantCount: rows.reduce((a, b) => a + b.participantCount, 0),
    }
  }

  function createWorkspace(payload) {
    const ws = { id: `ws-${Date.now()}`, name: payload.name, description: payload.description, createdAt: new Date().toISOString() }
    workspaces.value.push(ws)
    if (!activeWorkspaceId.value) activeWorkspaceId.value = ws.id
    return ws
  }

  function createClass(payload) {
    const cls = { id: `cls-${Date.now()}`, workspaceId: payload.workspaceId, name: payload.name, description: payload.description, createdAt: new Date().toISOString() }
    classes.value.push(cls)
    return cls
  }

  function deleteClass(id) {
    classes.value = classes.value.filter(c => c.id !== id)
  }

  function setActiveWorkspace(id) {
    activeWorkspaceId.value = id
    activeClassId.value = null
  }

  function setActiveClass(id) {
    activeClassId.value = id
  }

  return {
    workspaces, classes, monthlyStats, activeWorkspaceId, activeClassId,
    activeWorkspace,
    getClassesForWorkspace, getStatsForClass, getStatsForWorkspace, getWorkspaceTotal,
    createWorkspace, createClass, deleteClass, setActiveWorkspace, setActiveClass,
  }
})
