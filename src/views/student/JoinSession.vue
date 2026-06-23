<template>
  <v-sheet
    class="d-flex align-center justify-center pa-4"
    style="min-height:100vh; background: linear-gradient(135deg, #1565C0 0%, #0288D1 50%, #00BCD4 100%)"
  >
    <v-card rounded="2xl" elevation="12" max-width="440" width="100%">

      <!-- Header me logo -->
      <div class="d-flex flex-column align-center pa-8 pb-5">
        <v-avatar size="80" rounded="2xl" class="mb-4" elevation="4">
          <v-img src="/zana.png" alt="ZANA" cover />
        </v-avatar>
        <h1 class="text-h5 font-weight-bold text-center mb-1">Hyr në Sesion Live</h1>
        <p class="text-body-2 text-medium-emphasis text-center">
          Fute kodin e sesionit ose skano QR-kodin
        </p>
      </div>

      <v-card-text class="px-6 pb-2">

        <!-- Alert AI consent -->
        <v-alert type="info" variant="tonal" rounded="xl" density="compact" class="mb-5"
          icon="mdi-robot-outline" border="start">
          <span class="text-caption">Ky sesion përdor AI për caption dhe audio live. Duke hyrë, pranoni përpunimin AI.</span>
        </v-alert>

        <!-- Kodi i sesionit — i madh dhe i qartë -->
        <div class="text-center mb-2">
          <span class="text-caption font-weight-bold text-medium-emphasis text-uppercase" style="letter-spacing:0.8px">
            Kodi i Sesionit
          </span>
        </div>
        <v-otp-input
          v-if="false"
          v-model="form.code"
        />
        <v-text-field
          v-model="form.code"
          placeholder="p.sh. ML2024"
          prepend-inner-icon="mdi-key-variant"
          :rules="[r => !!r || 'Kodi është i detyrueshëm', r => r.length >= 4 || 'Kodi shumë i shkurtër']"
          variant="outlined"
          rounded="xl"
          class="mb-3"
          style="font-size:20px; letter-spacing:4px; font-weight:700"
          autofocus
          maxlength="8"
          @input="form.code = form.code.toUpperCase()"
        />

        <v-text-field
          v-model="form.name"
          label="Emri juaj (opsional)"
          placeholder="Anonim"
          prepend-inner-icon="mdi-account-outline"
          variant="outlined"
          rounded="xl"
          class="mb-5"
        />

        <!-- Zgjedhja e gjuhës — e qartë vizualisht -->
        <div class="text-caption font-weight-bold text-medium-emphasis text-uppercase mb-3" style="letter-spacing:0.8px">
          🌍 Gjuha e Caption-eve
        </div>
        <v-row class="mb-5" no-gutters>
          <v-col v-for="lang in languages" :key="lang.value" cols="4" class="pa-1">
            <v-card
              :color="form.language === lang.value ? 'primary' : 'default'"
              :variant="form.language === lang.value ? 'flat' : 'outlined'"
              rounded="xl"
              class="text-center pa-3 cursor-pointer"
              style="transition: all 0.2s"
              @click="form.language = lang.value"
            >
              <div style="font-size:28px">{{ lang.flag }}</div>
              <div class="text-caption font-weight-bold mt-1"
                :class="form.language === lang.value ? 'text-white' : ''">
                {{ lang.label }}
              </div>
            </v-card>
          </v-col>
        </v-row>

        <v-btn
          color="primary" size="x-large" block :loading="joining"
          prepend-icon="mdi-login" rounded="xl" class="mb-4" elevation="3"
          @click="submit"
        >
          Hyr në Sesion
        </v-btn>

      </v-card-text>

      <v-divider />
      <div class="pa-4 text-center">
        <span class="text-caption text-medium-emphasis">
          Jeni mësues?
          <v-btn variant="text" size="small" to="/teacher" color="primary" class="px-1">
            Shko te Dashboard →
          </v-btn>
        </span>
      </div>
    </v-card>

    <!-- Dialog: Sesioni plot -->
    <v-dialog v-model="fullDialog" max-width="360">
      <v-card rounded="xl">
        <v-card-text class="pa-8 text-center">
          <v-icon size="56" color="warning" class="mb-4">mdi-account-group</v-icon>
          <h3 class="text-h6 font-weight-bold mb-2">Sesioni është i plotë</h3>
          <p class="text-body-2 text-medium-emphasis">
            Ky sesion ka arritur numrin maksimal të studentëve.
          </p>
        </v-card-text>
        <v-card-actions class="px-6 pb-6">
          <v-btn block color="warning" variant="flat" rounded="xl" size="large" @click="fullDialog = false">OK</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog: Sesion i pagjendur -->
    <v-dialog v-model="errorDialog" max-width="360">
      <v-card rounded="xl">
        <v-card-text class="pa-8 text-center">
          <v-icon size="56" color="error" class="mb-4">mdi-alert-circle-outline</v-icon>
          <h3 class="text-h6 font-weight-bold mb-2">Kod i gabuar</h3>
          <p class="text-body-2 text-medium-emphasis">
            Nuk u gjet asnjë sesion aktiv me kodin <strong>{{ form.code }}</strong>. Kontrolloni kodin dhe provoni sërish.
          </p>
        </v-card-text>
        <v-card-actions class="px-6 pb-6">
          <v-btn block color="primary" variant="flat" rounded="xl" size="large" @click="errorDialog = false">
            Provo Sërish
          </v-btn>
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

const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()
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
  if (route.params.code) form.value.code = String(route.params.code).toUpperCase()
})

async function submit() {
  if (!form.value.code || form.value.code.length < 4) return
  joining.value = true
  await new Promise(r => setTimeout(r, 700))
  const session = sessionStore.sessions.find(
    s => s.joinCode === form.value.code && ['ACTIVE', 'PAUSED'].includes(s.status)
  )
  if (!session) { joining.value = false; errorDialog.value = true; return }
  if (session.participantCount >= session.maxParticipants) {
    joining.value = false; fullDialog.value = true; return
  }
  participantStore.joinSession(session.id, form.value.name, form.value.language)
  session.participantCount++
  joining.value = false
  router.push(`/student/session/${session.id}`)
}
</script>
