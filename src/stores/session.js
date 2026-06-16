import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const MOCK_SESSIONS = [
  {
    id: 'sess-001',
    title: 'Introduction to Machine Learning',
    courseId: 'cs401',
    courseName: 'Computer Science 401',
    teacherId: 'teacher-1',
    accessMode: 'OPEN',
    sourceLanguage: 'IT',
    targetLanguages: ['EN', 'SQ'],
    status: 'ACTIVE',
    joinCode: 'ML2024',
    recordingEnabled: false,
    studentTranscriptDownloadEnabled: true,
    maxParticipants: 300,
    participantCount: 47,
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    startedAt: new Date(Date.now() - 1800000).toISOString(),
  },
  {
    id: 'sess-002',
    title: 'Linear Algebra — Lecture 12',
    courseId: 'math201',
    courseName: 'Mathematics 201',
    teacherId: 'teacher-1',
    accessMode: 'CLOSED',
    sourceLanguage: 'EN',
    targetLanguages: ['IT', 'SQ'],
    status: 'PAUSED',
    joinCode: 'ALG012',
    recordingEnabled: true,
    studentTranscriptDownloadEnabled: false,
    maxParticipants: 150,
    participantCount: 32,
    createdAt: new Date(Date.now() - 7200000).toISOString(),
    startedAt: new Date(Date.now() - 5400000).toISOString(),
  },
  {
    id: 'sess-003',
    title: 'European History Seminar',
    courseId: 'hist301',
    courseName: 'History 301',
    teacherId: 'teacher-1',
    accessMode: 'OPEN',
    sourceLanguage: 'IT',
    targetLanguages: ['EN', 'SQ'],
    status: 'ENDED',
    joinCode: 'HIST09',
    recordingEnabled: false,
    studentTranscriptDownloadEnabled: true,
    maxParticipants: 300,
    participantCount: 0,
    createdAt: new Date(Date.now() - 86400000).toISOString(),
    startedAt: new Date(Date.now() - 82800000).toISOString(),
    endedAt: new Date(Date.now() - 79200000).toISOString(),
  },
]

const MOCK_TRANSCRIPT = [
  {
    id: 'seg-001',
    sessionId: 'sess-001',
    sequenceNo: 1,
    sourceLanguage: 'IT',
    originalText: "Benvenuti alla lezione di oggi sull'apprendimento automatico.",
    isFinal: true,
    confidence: 0.98,
    startOffsetMs: 0,
    endOffsetMs: 3200,
    createdAt: new Date(Date.now() - 900000).toISOString(),
    translations: [
      { id: 't1', transcriptSegmentId: 'seg-001', targetLanguage: 'EN', translatedText: "Welcome to today's lesson on machine learning.", isFinal: true, createdAt: new Date().toISOString() },
      { id: 't2', transcriptSegmentId: 'seg-001', targetLanguage: 'SQ', translatedText: 'Mirë se vini në mësimin e sotëm mbi mësimin e makinerive.', isFinal: true, createdAt: new Date().toISOString() },
    ],
  },
  {
    id: 'seg-002',
    sessionId: 'sess-001',
    sequenceNo: 2,
    sourceLanguage: 'IT',
    originalText: 'Oggi parleremo delle reti neurali e del loro funzionamento.',
    isFinal: true,
    confidence: 0.96,
    startOffsetMs: 3500,
    endOffsetMs: 7100,
    createdAt: new Date(Date.now() - 600000).toISOString(),
    translations: [
      { id: 't3', transcriptSegmentId: 'seg-002', targetLanguage: 'EN', translatedText: 'Today we will talk about neural networks and how they work.', isFinal: true, createdAt: new Date().toISOString() },
      { id: 't4', transcriptSegmentId: 'seg-002', targetLanguage: 'SQ', translatedText: 'Sot do të flasim për rrjetet nervore dhe mënyrën e funksionimit të tyre.', isFinal: true, createdAt: new Date().toISOString() },
    ],
  },
  {
    id: 'seg-003',
    sessionId: 'sess-001',
    sequenceNo: 3,
    sourceLanguage: 'IT',
    originalText: "Un'intelligenza artificiale impara dai dati attraverso algoritmi di ottimizzazione.",
    isFinal: true,
    confidence: 0.94,
    startOffsetMs: 7400,
    endOffsetMs: 11900,
    createdAt: new Date(Date.now() - 300000).toISOString(),
    translations: [
      { id: 't5', transcriptSegmentId: 'seg-003', targetLanguage: 'EN', translatedText: 'Artificial intelligence learns from data through optimization algorithms.', isFinal: true, createdAt: new Date().toISOString() },
      { id: 't6', transcriptSegmentId: 'seg-003', targetLanguage: 'SQ', translatedText: 'Inteligjenca artificiale mëson nga të dhënat përmes algoritmeve të optimizimit.', isFinal: true, createdAt: new Date().toISOString() },
    ],
  },
]

export const useSessionStore = defineStore('session', () => {
  const sessions = ref(MOCK_SESSIONS)
  const activeSessionId = ref(null)
  const transcript = ref(MOCK_TRANSCRIPT)
  const micActive = ref(false)
  const audioLevel = ref(0)
  const isMuted = ref(false)

  const activeSession = computed(() =>
    sessions.value.find(s => s.id === activeSessionId.value) ?? null
  )

  const activeSessions = computed(() =>
    sessions.value.filter(s => s.status === 'ACTIVE' || s.status === 'PAUSED' || s.status === 'WAITING')
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
