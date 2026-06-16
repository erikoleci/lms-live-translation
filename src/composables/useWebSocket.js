import { ref, onUnmounted } from 'vue'

export function useWebSocket(url) {
  const status = ref('DISCONNECTED')
  const handlers = new Map()
  let ws = null
  let retryTimeout = null
  let retryCount = 0
  const MAX_RETRIES = 5

  function on(event, handler) {
    if (!handlers.has(event)) handlers.set(event, new Set())
    handlers.get(event).add(handler)
  }

  function off(event, handler) {
    handlers.get(event)?.delete(handler)
  }

  function emit(event, payload) {
    handlers.get(event)?.forEach(h => h(payload))
  }

  function connect() {
    if (ws && ws.readyState === WebSocket.OPEN) return
    status.value = retryCount > 0 ? 'RECONNECTING' : 'CONNECTING'
    try {
      ws = new WebSocket(url)
      ws.onopen = () => { status.value = 'CONNECTED'; retryCount = 0 }
      ws.onmessage = (e) => {
        try {
          const msg = JSON.parse(e.data)
          emit(msg.event, msg.payload)
        } catch { /* ignore malformed */ }
      }
      ws.onclose = () => { status.value = 'DISCONNECTED'; scheduleRetry() }
      ws.onerror = () => { status.value = 'DISCONNECTED' }
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

  function send(event, payload) {
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
