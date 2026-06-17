<template>
  <v-sheet
    color="primary-lighten-5"
    class="d-flex align-center justify-center pa-4"
    style="min-height:100vh; background: linear-gradient(135deg, #E3F2FD 0%, #E8EAF6 100%)"
  >
    <v-card rounded="xl" elevation="6" max-width="460" width="100%">

      <!-- Header -->
      <v-sheet
        color="primary"
        rounded="t-xl"
        class="pa-8 pb-6"
        style="background: linear-gradient(135deg, #1565C0, #0288D1)"
      >
        <v-avatar color="white" size="56" class="mb-4" elevation="3">
          <v-icon color="primary" size="32">mdi-translate</v-icon>
        </v-avatar>
        <h1 class="text-h5 font-weight-bold text-white">Join Live Session</h1>
        <p class="text-body-2 text-white mt-1 opacity-85">Enter your session code or scan the QR to join</p>
      </v-sheet>

      <v-card-text class="pa-6">
        <v-alert type="info" variant="tonal" rounded="lg" density="compact" class="mb-5 text-caption" icon="mdi-shield-lock">
          This session uses AI for real-time captions and audio. By joining, you consent to AI processing.
        </v-alert>

        <v-form ref="formRef" @submit.prevent="submit">
          <v-text-field
            v-model="form.code"
            label="Session Code"
            placeholder="e.g. ML2024"
            prepend-inner-icon="mdi-key-variant"
            :rules="[r => !!r || 'Session code is required', r => r.length >= 4 || 'Code too short']"
            variant="outlined"
            rounded="lg"
            class="mb-3"
            autofocus
            @input="form.code = form.code.toUpperCase()"
          />
          <v-text-field
            v-model="form.name"
            label="Your Name (optional)"
            placeholder="Anonymous"
            prepend-inner-icon="mdi-account"
            variant="outlined"
            rounded="lg"
            class="mb-4"
          />

          <p class="text-caption font-weight-bold text-uppercase text-medium-emphasis mb-3">Select Display Language</p>
          <LanguageSelector v-model="form.language" class="mb-5" />

          <v-btn type="submit" color="primary" size="large" block :loading="joining" prepend-icon="mdi-login" rounded="lg">
            Join Session
          </v-btn>
        </v-form>
      </v-card-text>

      <v-divider />
      <v-card-text class="pa-4 text-center">
        <span class="text-caption text-medium-emphasis">
          Are you a teacher?
          <v-btn variant="text" size="small" to="/teacher" class="px-1">Go to Dashboard →</v-btn>
        </span>
      </v-card-text>
    </v-card>

    <!-- Error dialog -->
    <v-dialog v-model="errorDialog" max-width="360">
      <v-card rounded="xl">
        <v-card-text class="pa-6 text-center">
          <v-icon size="48" color="error" class="mb-3">mdi-alert-circle</v-icon>
          <p class="text-body-1 font-weight-bold">Session Not Found</p>
          <p class="text-body-2 text-medium-emphasis mt-1">
            No active session found with code <strong>{{ form.code }}</strong>. Check the code and try again.
          </p>
        </v-card-text>
        <v-card-actions class="px-6 pb-6">
          <v-btn block color="primary" variant="flat" rounded="lg" @click="errorDialog = false">Try Again</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-sheet>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSessionStore } from '../../stores/session.js'
import { useParticipantStore } from '../../stores/participant.js'
import LanguageSelector from '../../components/student/LanguageSelector.vue'

const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()
const participantStore = useParticipantStore()
const formRef = ref()
const joining = ref(false)
const errorDialog = ref(false)
const form = ref({ code: '', name: '', language: 'EN' })

onMounted(() => { if (route.params.code) form.value.code = route.params.code.toUpperCase() })

async function submit() {
  const { valid } = await formRef.value.validate()
  if (!valid) return
  joining.value = true
  await new Promise(r => setTimeout(r, 800))
  const session = sessionStore.sessions.find(s => s.joinCode === form.value.code && ['ACTIVE','PAUSED','WAITING'].includes(s.status))
  if (!session) { joining.value = false; errorDialog.value = true; return }
  participantStore.joinSession(session.id, form.value.name, form.value.language)
  session.participantCount++
  joining.value = false
  router.push(`/student/session/${session.id}`)
}
</script>
