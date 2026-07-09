import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { sessionApi } from '../services/sessionApi.js'

export const useSessionStore = defineStore('session', () => {
  const sessions = ref([])
  const activeSessionId = ref(null)
  const transcript = ref([])
  const micActive = ref(false)
  const audioLevel = ref(0)
  const isMuted = ref(false)
  const loading = ref(false)
  const error = ref(null)

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

  /** Loads the teacher's sessions from the backend (call on dashboard mount). */
  async function fetchSessions(params = {}) {
    loading.value = true
    error.value = null
    try {
      const data = await sessionApi.list(params)
      sessions.value = data
      return data
    } catch (e) {
      error.value = e
      throw e
    } finally {
      loading.value = false
    }
  }

  /** Loads a single session (e.g. when landing directly on a session URL). */
  async function fetchSession(sessionId) {
    const data = await sessionApi.get(sessionId)
    upsertLocal(data)
    return data
  }

  async function createSession(payload) {
    const created = await sessionApi.create(payload)
    sessions.value.unshift(created)
    return created
  }

  /**
   * Calls the backend state-machine endpoint and applies the confirmed
   * result locally. Also used as the local-only setter when a
   * SESSION_STATUS_CHANGED WebSocket event arrives (see useLiveEvents.js) —
   * in that case the change already happened server-side, so no API call.
   */
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

  async function changeSessionState(id, state) {
    const updated = await sessionApi.changeState(id, state)
    upsertLocal(updated)
    return updated
  }

  function upsertLocal(data) {
    const idx = sessions.value.findIndex(s => s.id === data.id)
    if (idx !== -1) sessions.value[idx] = data
    else sessions.value.unshift(data)
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
    if (existing !== -1) {
      transcript.value[existing] = segment
    } else {
      transcript.value.push(segment)
    }
  }

  /** Loads the full transcript for a session (e.g. TranscriptExport.vue). */
  async function fetchTranscript(sessionId) {
    const segments = await sessionApi.transcript(sessionId)
    transcript.value = transcript.value.filter(s => s.sessionId !== sessionId)
    for (const seg of segments) {
      transcript.value.push({ ...seg, sessionId, translations: [] })
    }
    return segments
  }

  return {
    sessions, activeSessionId, transcript, micActive, audioLevel, isMuted, loading, error,
    activeSession, activeSessions, endedSessions,
    getSession, getTranscript, createSession, updateSessionStatus, changeSessionState,
    setActiveSession, setMicActive, setAudioLevel, toggleMute, addTranscriptSegment,
    fetchSessions, fetchSession, fetchTranscript, upsertLocal,
  }
}, { persist: { pick: ['sessions', 'transcript'] } })
