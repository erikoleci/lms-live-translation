<template>
  <v-dialog v-model="model" max-width="560" persistent scrollable>
    <v-card rounded="xl">

      <!-- Header -->
      <v-sheet color="primary" rounded="t-xl" class="px-6 py-5">
        <div class="d-flex align-center gap-3">
          <v-avatar color="white" size="40" rounded="lg">
            <v-icon color="primary" size="22">mdi-plus-circle</v-icon>
          </v-avatar>
          <div>
            <div class="text-h6 font-weight-bold text-white">Sesion i Ri</div>
            <div class="text-caption text-white opacity-80">Plotëso të dhënat e sesionit</div>
          </div>
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" color="white" @click="model = false" />
        </div>
      </v-sheet>

      <v-card-text class="pa-6">
        <v-form ref="formRef">

          <!-- Step 1: Info bazë -->
          <div class="text-caption font-weight-bold text-medium-emphasis mb-3 text-uppercase" style="letter-spacing:0.8px">
            📋 Informacioni Bazë
          </div>

          <v-text-field
            v-model="form.title"
            label="Titulli i Sesionit"
            placeholder="p.sh. Hyrje në Machine Learning"
            prepend-inner-icon="mdi-text"
            :rules="[r => !!r || 'Titulli është i detyrueshëm']"
            variant="outlined"
            rounded="lg"
            class="mb-3"
          />

          <v-select
            v-model="form.courseId"
            label="Kursi"
            prepend-inner-icon="mdi-book-open-outline"
            :items="courses"
            item-title="name"
            item-value="id"
            variant="outlined"
            rounded="lg"
            class="mb-4"
          />

          <v-divider class="mb-4" />

          <!-- Step 2: Gjuhët -->
          <div class="text-caption font-weight-bold text-medium-emphasis mb-3 text-uppercase" style="letter-spacing:0.8px">
            🌍 Gjuhët
          </div>

          <v-select
            v-model="form.sourceLanguage"
            label="Gjuha e Mësuesit (burim)"
            prepend-inner-icon="mdi-microphone"
            :items="languageItems"
            variant="outlined"
            rounded="lg"
            class="mb-3"
            hint="Gjuha në të cilën do të flisni"
            persistent-hint
          />

          <v-select
            v-model="form.targetLanguages"
            label="Gjuhët e Studentëve (target)"
            prepend-inner-icon="mdi-translate"
            :items="targetLangItems"
            multiple
            chips
            closable-chips
            variant="outlined"
            rounded="lg"
            class="mb-4"
            :rules="[r => r.length > 0 || 'Zgjidhni të paktën një gjuhë']"
            hint="Studentët do të shohin caption-et në këto gjuhë"
            persistent-hint
          />

          <v-divider class="mb-4" />

          <!-- Step 3: Aksesi -->
          <div class="text-caption font-weight-bold text-medium-emphasis mb-3 text-uppercase" style="letter-spacing:0.8px">
            🔐 Aksesi & Limitet
          </div>

          <v-row class="mb-3">
            <v-col cols="12" sm="6">
              <v-select
                v-model="form.accessMode"
                label="Mënyra e Aksesit"
                prepend-inner-icon="mdi-shield-account"
                :items="accessModes"
                variant="outlined"
                rounded="lg"
              />
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model.number="form.maxParticipants"
                label="Max Studentë"
                type="number"
                prepend-inner-icon="mdi-account-group"
                min="1"
                max="1000"
                variant="outlined"
                rounded="lg"
              />
            </v-col>
          </v-row>

          <!-- Switches -->
          <v-sheet rounded="xl" border class="pa-4">
            <v-switch
              v-model="form.studentTranscriptDownloadEnabled"
              label="Studentët mund të shkarkojnë transcript-in"
              color="primary"
              density="compact"
              hide-details
              class="mb-2"
            />
            <v-switch
              v-model="form.recordingEnabled"
              label="Regjistro audio (server-side)"
              color="primary"
              density="compact"
              hide-details
            />
          </v-sheet>

        </v-form>
      </v-card-text>

      <v-divider />
      <v-card-actions class="px-6 py-4 gap-2">
        <v-btn variant="text" rounded="lg" size="large" @click="model = false">Anulo</v-btn>
        <v-spacer />
        <v-btn color="primary" variant="flat" prepend-icon="mdi-play-circle"
          size="large" rounded="xl" :loading="saving" @click="submit">
          Krijo & Hap
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useSessionStore } from '../../stores/session.js'
import { useWorkspaceStore } from '../../stores/workspace.js'

const model = defineModel()
const emit = defineEmits(['created'])
const sessionStore = useSessionStore()
const workspaceStore = useWorkspaceStore()
const formRef = ref()
const saving = ref(false)

const form = ref({
  title: '',
  courseId: 'cs401',
  sourceLanguage: 'IT',
  targetLanguages: ['EN', 'SQ'],
  accessMode: 'OPEN',
  maxParticipants: 300,
  recordingEnabled: false,
  studentTranscriptDownloadEnabled: true,
})

const courses = computed(() => workspaceStore.courses)

const languageItems = [
  { title: '🇮🇹 Italisht', value: 'IT' },
  { title: '🇬🇧 Anglisht', value: 'EN' },
  { title: '🇦🇱 Shqip', value: 'SQ' },
]

const targetLangItems = computed(() => languageItems.filter(l => l.value !== form.value.sourceLanguage))

const accessModes = [
  { title: '🔓 I Hapur — kushdo me QR/link', value: 'OPEN' },
  { title: '🔒 Privat — vetëm me llogari', value: 'CLOSED' },
]

function resetForm() {
  form.value = { title: '', courseId: 'cs401', sourceLanguage: 'IT', targetLanguages: ['EN', 'SQ'], accessMode: 'OPEN', maxParticipants: 300, recordingEnabled: false, studentTranscriptDownloadEnabled: true }
  formRef.value?.resetValidation()
}

async function submit() {
  const { valid } = await formRef.value.validate()
  if (!valid) return
  saving.value = true
  await new Promise(r => setTimeout(r, 600))
  const session = sessionStore.createSession(form.value)
  saving.value = false
  model.value = false
  resetForm()
  emit('created', session.id)
}
</script>
