import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const API_BASE = 'http://localhost:8080'

function uid() {
  return crypto.randomUUID
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

function randomJoinCode() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  return Array.from({ length: 6 }, () => chars[Math.floor(Math.random() * chars.length)]).join('')
}

function normalizeSession(data) {
  if (!data) return data
  return {
    ...data,
    participantCount: data.currentParticipantCount ?? data.participantCount ?? 0,
    courseName: data.courseName || data.courseId || '',
  }
}

export const useSessionStore = defineStore('session', () => {
  const sessions = ref([])
  const activeSessionId = ref(null)
  const transcript = ref([])
  const micActive = ref(false)
  const audioLevel = ref(0)
  const isMuted = ref(false)

  const activeSession = computed(() =>
    sessions.value.find(s => s.id === activeSessionId.value) ?? null
  )

  const activeSessions = computed(() =>
    sessions.value.filter(s =>
      ['CREATED', 'WAITING', 'ACTIVE', 'PAUSED'].includes(s.status)
    )
  )

  const endedSessions = computed(() =>
    sessions.value.filter(s => s.status === 'ENDED' || s.status === 'EXPIRED')
  )

  // ========== API CALLS ==========
  async function fetchSessions() {
    try {
      const res = await fetch(`${API_BASE}/api/sessions`)
      if (!res.ok) throw new Error('Failed to fetch sessions')
      const data = await res.json()
      sessions.value = (data || []).map(normalizeSession)
      return sessions.value
    } catch (err) {
      console.warn('Backend not available', err)
    }
  }

  async function createSession(payload) {
    try {
      const res = await fetch(`${API_BASE}/api/sessions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: payload.title || 'Untitled Session',
          courseId: payload.courseId || payload.courseName || null,
          accessMode: payload.accessMode || 'OPEN',
          sourceLanguage: payload.sourceLanguage || 'IT',
          targetLanguages: payload.targetLanguages || ['EN', 'SQ'],
          recordingEnabled: payload.recordingEnabled ?? false,
          studentTranscriptDownloadEnabled: payload.studentTranscriptDownloadEnabled ?? true,
          maxParticipants: payload.maxParticipants || 300
        })
      })

      if (!res.ok) {
        const text = await res.text()
        throw new Error(text || 'Failed to create session')
      }

      const data = normalizeSession(await res.json())
      sessions.value.unshift(data)
      return data
    } catch (err) {
      console.error('Error creating session on backend:', err)

      // fallback lokal
      const created = {
        id: uid(),
        title: payload.title || 'Untitled Session',
        courseName: payload.courseName || payload.courseId || '',
        courseId: payload.courseId || null,
        sourceLanguage: payload.sourceLanguage || 'IT',
        targetLanguages: payload.targetLanguages || ['EN', 'SQ'],
        status: 'CREATED',
        joinCode: randomJoinCode(),
        accessMode: payload.accessMode || 'OPEN',
        participantCount: 0,
        maxParticipants: payload.maxParticipants || 300,
        recordingEnabled: payload.recordingEnabled ?? false,
        studentTranscriptDownloadEnabled: payload.studentTranscriptDownloadEnabled ?? true,
        startedAt: null,
        endedAt: null,
      }
      sessions.value.unshift(created)
      return created
    }
  }

  // Sync – për computed në Vue
  function getSession(id) {
    return sessions.value.find(s => s.id === id) || null
  }

  // Async – ngarkon nga backend
  async function fetchSession(id) {
    try {
      const res = await fetch(`${API_BASE}/api/sessions/${id}`)
      if (!res.ok) throw new Error('Session not found')
      const data = normalizeSession(await res.json())
      const idx = sessions.value.findIndex(s => s.id === id)
      if (idx !== -1) sessions.value[idx] = data
      else sessions.value.unshift(data)
      return data
    } catch (err) {
      console.warn('fetchSession failed', err)
      return sessions.value.find(s => s.id === id) || null
    }
  }

  async function changeSessionState(id, state) {
    try {
      const res = await fetch(`${API_BASE}/api/sessions/${id}/state?state=${state}`, {
        method: 'PATCH'
      })
      if (!res.ok) throw new Error('Failed to change state')
      const updated = normalizeSession(await res.json())
      const idx = sessions.value.findIndex(s => s.id === id)
      if (idx !== -1) sessions.value[idx] = updated
      return updated
    } catch (err) {
      console.error(err)
      updateSessionStatus(id, state)
      return getSession(id)
    }
  }

  // ========== LOCAL HELPERS ==========
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

  function getTranscript(sessionId) {
    return transcript.value.filter(s => s.sessionId === sessionId)
  }

  return {
    sessions,
    activeSessionId,
    transcript,
    micActive,
    audioLevel,
    isMuted,
    activeSession,
    activeSessions,
    endedSessions,
    fetchSessions,
    createSession,
    getSession,
    fetchSession,
    changeSessionState,
    updateSessionStatus,
    setActiveSession,
    setMicActive,
    setAudioLevel,
    toggleMute,
    addTranscriptSegment,
    getTranscript,
  }
})
