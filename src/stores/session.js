import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

function uid() {
  return crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

function randomJoinCode() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  return Array.from({ length: 6 }, () => chars[Math.floor(Math.random() * chars.length)]).join('')
}

const DEMO_SESSIONS = [
  {
    id: 'demo-1', title: 'Machine Learning — Lecture 4', courseName: 'AI Fundamentals',
    sourceLanguage: 'IT', targetLanguages: ['EN', 'SQ'], status: 'ACTIVE',
    joinCode: 'ML4X9K', accessMode: 'OPEN', participantCount: 12, maxParticipants: 60,
    startedAt: new Date(Date.now() - 12 * 60000).toISOString(), endedAt: null,
  },
  {
    id: 'demo-2', title: 'European History — Seminar 2', courseName: 'History 201',
    sourceLanguage: 'EN', targetLanguages: ['IT', 'SQ'], status: 'ENDED',
    joinCode: 'HIS2A7', accessMode: 'OPEN', participantCount: 28, maxParticipants: 60,
    startedAt: new Date(Date.now() - 86400000).toISOString(),
    endedAt: new Date(Date.now() - 82800000).toISOString(),
  },
]

/**
 * Pure client-side UI state for the session views — no backend, no network.
 * Sessions live only in memory (optionally persisted to localStorage for a
 * nicer demo experience across reloads). This is a presentation-layer demo
 * store: everything here is mock data meant to make every screen look and
 * feel complete without any server behind it.
 */
export const useSessionStore = defineStore('session', () => {
  const sessions = ref(DEMO_SESSIONS.map(s => ({ ...s })))
  const activeSessionId = ref(null)
  const transcript = ref([])
  const micActive = ref(false)
  const audioLevel = ref(0)
  const isMuted = ref(false)

  const activeSession = computed(() =>
    sessions.value.find(s => s.id === activeSessionId.value) ?? null
  )

  const activeSessions = computed(() =>
    sessions.value.filter(s => ['CREATED', 'WAITING', 'ACTIVE', 'PAUSED'].includes(s.status))
  )

  const endedSessions = computed(() =>
    sessions.value.filter(s => s.status === 'ENDED' || s.status === 'EXPIRED')
  )

  function getSession(id) {
    return sessions.value.find(s => s.id === id)
  }

  function getTranscript(sessionId) {
    return transcript.value.filter(s => s.sessionId === sessionId)
  }

  function createSession(payload) {
    const created = {
      id: uid(),
      title: payload.title || 'Untitled Session',
      courseName: payload.courseName || '',
      sourceLanguage: payload.sourceLanguage || 'IT',
      targetLanguages: payload.targetLanguages || ['EN'],
      status: 'CREATED',
      joinCode: randomJoinCode(),
      accessMode: payload.accessMode || 'OPEN',
      participantCount: 0,
      maxParticipants: payload.maxParticipants || 60,
      startedAt: null,
      endedAt: null,
    }
    sessions.value.unshift(created)
    return created
  }

  function updateSessionStatus(id, status) {
    const session = sessions.value.find(s => s.id === id)
    if (!session) return
    session.status = status
    if (status === 'ACTIVE' && !session.startedAt) {
      session.startedAt = new Date().toISOString()
    }
    if (status === 'ENDED' || status === 'EXPIRED' || status === 'FAILED') {
      session.endedAt = new Date().toISOString()
    }
  }

  function changeSessionState(id, state) {
    updateSessionStatus(id, state)
    return getSession(id)
  }

  function setActiveSession(id) {
    activeSessionId.value = id
  }

  function setMicActive(active) {
    micActive.value = active
  }

  function setAudioLevel(level) {
    audioLevel.value = level
  }

  function toggleMute() {
    isMuted.value = !isMuted.value
  }

  function addTranscriptSegment(segment) {
    const existing = transcript.value.findIndex(
      s => s.sequenceNo === segment.sequenceNo && s.sessionId === segment.sessionId
    )
    if (existing !== -1) transcript.value[existing] = segment
    else transcript.value.push(segment)
  }

  return {
    sessions, activeSessionId, transcript, micActive, audioLevel, isMuted,
    activeSession, activeSessions, endedSessions,
    getSession, getTranscript, createSession, updateSessionStatus, changeSessionState,
    setActiveSession, setMicActive, setAudioLevel, toggleMute, addTranscriptSegment,
  }
})
