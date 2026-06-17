import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
    const newSession = {
      id: `sess-${Date.now()}`,
      title: payload.title,
      courseId: payload.courseId,
      courseName: 'Demo Course',
      teacherId: 'teacher-1',
      accessMode: payload.accessMode,
      sourceLanguage: payload.sourceLanguage,
      targetLanguages: payload.targetLanguages,
      status: 'CREATED',
      joinCode: Math.random().toString(36).substring(2, 8).toUpperCase(),
      recordingEnabled: payload.recordingEnabled,
      studentTranscriptDownloadEnabled: payload.studentTranscriptDownloadEnabled,
      maxParticipants: payload.maxParticipants,
      participantCount: 0,
      createdAt: new Date().toISOString(),
    }
    sessions.value.unshift(newSession)
    return newSession
  }

  function updateSessionStatus(id, status) {
    const session = sessions.value.find(s => s.id === id)
    if (!session) return
    session.status = status
    if (status === 'ACTIVE' && !session.startedAt) {
      session.startedAt = new Date().toISOString()
    }
    if (status === 'ENDED') {
      session.endedAt = new Date().toISOString()
      session.participantCount = 0
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
    if (existing !== -1) {
      transcript.value[existing] = segment
    } else {
      transcript.value.push(segment)
    }
  }

  return {
    sessions, activeSessionId, transcript, micActive, audioLevel, isMuted,
    activeSession, activeSessions, endedSessions,
    getSession, getTranscript, createSession, updateSessionStatus,
    setActiveSession, setMicActive, setAudioLevel, toggleMute, addTranscriptSegment,
  }
})
