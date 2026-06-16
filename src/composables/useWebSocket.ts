import { ref, onUnmounted } from 'vue'

export type WsEvent =
  | 'SESSION_STATUS_CHANGED'
  | 'PARTICIPANT_JOINED'
  | 'PARTICIPANT_LEFT'
  | 'TRANSCRIPT_PARTIAL'
  | 'TRANSCRIPT_FINAL'
  | 'TRANSLATION_PARTIAL'
  | 'TRANSLATION_FINAL'
  | 'TTS_AUDIO_CHUNK_READY'
  | 'ERROR'
  | 'HEARTBEAT'

export type WsStatus = 'CONNECTING' | 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING'

type EventHandler = (payload: unknown) => void

export function useWebSocket(url: string) {
  const status = ref<WsStatus>('DISCONNECTED')
  const handlers = new Map<WsEvent, Set<EventHandler>>()
  let ws: WebSocket | null = null
  let retryTimeout: ReturnType<typeof setTimeout> | null = null
  let retryCount = 0
  const MAX_RETRIES = 5

  function on(event: WsEvent, handler: EventHandler) {
    if (!handlers.has(event)) handlers.set(event, new Set())
    handlers.get(event)!.add(handler)
  }

  function off(event: WsEvent, handler: EventHandler) {
    handlers.get(event)?.delete(handler)
  }

  function emit(event: WsEvent, payload: unknown) {
    handlers.get(event)?.forEach(h => h(payload))
  }

  function connect() {
    if (ws && ws.readyState === WebSocket.OPEN) return
    status.value = retryCount > 0 ? 'RECONNECTING' : 'CONNECTING'
    try {
      ws = new WebSocket(url)
      ws.onopen = () => {
        status.value = 'CONNECTED'
        retryCount = 0
      }
      ws.onmessage = (e) => {
        try {
          const msg = JSON.parse(e.data)
          emit(msg.event, msg.payload)
        } catch { /* ignore malformed */ }
      }
      ws.onclose = () => {
        status.value = 'DISCONNECTED'
        scheduleRetry()
      }
      ws.onerror = () => {
        status.value = 'DISCONNECTED'
      }
    } catch {
      status.value = 'DISCONNECTED'
      scheduleRetry()
    }
  }

  function scheduleRetry() {
    if (retryCount >= MAX_RETRIES) return
    retryCount++
    const delay = Math.min(1000 * 2 ** retryCount, 30000)
    retryTimeout = setTimeout(connect, delay)
  }

  function send(event: string, payload: unknown) {
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ event, payload }))
    }
  }

  function disconnect() {
    if (retryTimeout) clearTimeout(retryTimeout)
    ws?.close()
    ws = null
    status.value = 'DISCONNECTED'
  }

  onUnmounted(disconnect)

  return { status, connect, disconnect, send, on, off }
}
