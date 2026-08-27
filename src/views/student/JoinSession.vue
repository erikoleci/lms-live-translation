<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useParticipantStore } from '../../stores/participant.js'

const API_BASE = 'http://localhost:8080'

const route = useRoute()
const router = useRouter()
const participantStore = useParticipantStore()

const joining = ref(false)
const errorDialog = ref(false)
const fullDialog = ref(false)
const form = ref({ code: '', name: '', language: 'SQ' })

const languages = [
  { value: 'SQ', flag: '🇦🇱', label: 'Shqip' },
  { value: 'EN', flag: '🇬🇧', label: 'English' },
  { value: 'IT', flag: '🇮🇹', label: 'Italiano' },
]

onMounted(() => {
  if (route.params.code) {
    form.value.code = String(route.params.code).toUpperCase()
  }
})

async function submit() {
  if (!form.value.code || form.value.code.length < 4) return

  joining.value = true
  errorDialog.value = false
  fullDialog.value = false

  try {
    const res = await fetch(`${API_BASE}/api/sessions/join`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        joinCode: form.value.code.trim().toUpperCase(),
        anonymousName: form.value.name || undefined,
        targetLanguage: form.value.language || 'SQ',
        audioEnabled: false
      })
    })

    if (res.status === 404) {
      errorDialog.value = true
      return
    }
    if (res.status === 400) {
      const text = await res.text()
      if (text.toLowerCase().includes('full')) {
        fullDialog.value = true
      } else {
        errorDialog.value = true
      }
      return
    }
    if (!res.ok) {
      errorDialog.value = true
      return
    }

    const data = await res.json()

    if (typeof participantStore.joinSession === 'function') {
      participantStore.joinSession(
        data.sessionId,
        data.anonymousName,
        data.targetLanguage,
        data.participantId
      )
    }

    sessionStorage.setItem('participant', JSON.stringify(data))

    router.push(`/student/session/${data.sessionId}`)
  } catch (err) {
    console.error('Join failed', err)
    errorDialog.value = true
  } finally {
    joining.value = false
  }
}
</script>
