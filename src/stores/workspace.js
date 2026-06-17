import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const MOCK_WORKSPACES = [
  { id: 'ws-1', name: 'UNIZKM', description: 'Universiteti i Gjakovës', createdAt: '2026-01-15T09:00:00Z' },
  { id: 'ws-2', name: 'TILI', description: 'Tirana International Language Institute', createdAt: '2026-02-01T09:00:00Z' },
]

const MOCK_CLASSES = [
  { id: 'cls-001', workspaceId: 'ws-1', name: 'Klasa 001', description: 'Informatikë — Viti I', createdAt: '2026-02-01T09:00:00Z' },
  { id: 'cls-002', workspaceId: 'ws-1', name: 'Klasa 002', description: 'Matematikë — Viti II', createdAt: '2026-02-01T09:00:00Z' },
  { id: 'cls-003', workspaceId: 'ws-1', name: 'Klasa 003', description: 'Histori — Viti III', createdAt: '2026-03-01T09:00:00Z' },
  { id: 'cls-004', workspaceId: 'ws-2', name: 'Klasa A', description: 'Anglisht — Niveli I', createdAt: '2026-02-10T09:00:00Z' },
  { id: 'cls-005', workspaceId: 'ws-2', name: 'Klasa B', description: 'Italisht — Niveli II', createdAt: '2026-02-10T09:00:00Z' },
]

function makeMonthlyStats() {
  const stats = []
  const months = ['2026-03', '2026-04', '2026-05', '2026-06']
  for (const cls of MOCK_CLASSES) {
    for (const month of months) {
      stats.push({
        classId: cls.id,
        workspaceId: cls.workspaceId,
        month,
        totalMinutes: Math.floor(Math.random() * 400 + 60),
        sessionCount: Math.floor(Math.random() * 12 + 2),
        participantCount: Math.floor(Math.random() * 200 + 20),
      })
    }
  }
  return stats
}

export const useWorkspaceStore = defineStore('workspace', () => {
  const workspaces = ref(MOCK_WORKSPACES)
  const classes = ref(MOCK_CLASSES)
  const monthlyStats = ref(makeMonthlyStats())
  const activeWorkspaceId = ref('ws-1')
  const activeClassId = ref(null)

  const activeWorkspace = computed(() => workspaces.value.find(w => w.id === activeWorkspaceId.value) ?? null)

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
