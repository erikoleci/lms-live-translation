export type SessionStatus = 'CREATED' | 'WAITING' | 'ACTIVE' | 'PAUSED' | 'ENDED' | 'FAILED' | 'EXPIRED'
export type AccessMode = 'OPEN' | 'CLOSED'
export type Language = 'IT' | 'EN' | 'SQ'
export type ProviderType = 'STT' | 'TRANSLATION' | 'TTS'
export type ConnectionStatus = 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING'

export interface Session {
  id: string
  title: string
  courseId: string
  courseName: string
  teacherId: string
  accessMode: AccessMode
  sourceLanguage: Language
  targetLanguages: Language[]
  status: SessionStatus
  joinCode: string
  recordingEnabled: boolean
  studentTranscriptDownloadEnabled: boolean
  maxParticipants: number
  participantCount: number
  createdAt: string
  startedAt?: string
  endedAt?: string
  expiresAt?: string
}

export interface Participant {
  id: string
  sessionId: string
  userId?: string
  anonymousName: string
  targetLanguage: Language
  audioEnabled: boolean
  voiceCode: string
  joinedAt: string
  leftAt?: string
  connectionStatus: ConnectionStatus
}

export interface TranscriptSegment {
  id: string
  sessionId: string
  sequenceNo: number
  sourceLanguage: Language
  originalText: string
  isFinal: boolean
  confidence?: number
  startOffsetMs: number
  endOffsetMs?: number
  createdAt: string
  translations?: TranslationSegment[]
}

export interface TranslationSegment {
  id: string
  transcriptSegmentId: string
  targetLanguage: Language
  translatedText: string
  isFinal: boolean
  createdAt: string
}

export interface Voice {
  code: string
  name: string
  language: Language
  gender: 'MALE' | 'FEMALE' | 'NEUTRAL'
}

export interface Provider {
  id: string
  providerCode: string
  name: string
  providerType: ProviderType
  priority: number
  enabled: boolean
  supportedLanguages: Language[]
  supportedVoices?: Voice[]
  costLimit?: number
  timeoutMs: number
  fallbackProviderCode?: string
}

export interface UsageStat {
  date: string
  sttMinutes: number
  translationChars: number
  ttsChars: number
  sessionCount: number
  estimatedCost: number
}

export interface SessionCreatePayload {
  title: string
  courseId: string
  accessMode: AccessMode
  sourceLanguage: Language
  targetLanguages: Language[]
  recordingEnabled: boolean
  studentTranscriptDownloadEnabled: boolean
  maxParticipants: number
}
