<template>
  <v-card variant="outlined" rounded="xl" class="qr-card text-center pa-4">
    <p class="text-caption text-medium-emphasis mb-2 text-uppercase font-weight-bold letter-spacing">
      Scan to Join
    </p>
    <div class="qr-wrapper mx-auto mb-3">
      <qrcode-vue
        :value="joinUrl"
        :size="180"
        level="H"
        :margin="2"
      />
    </div>
    <v-chip color="primary" variant="flat" size="large" class="font-weight-bold join-code mb-3">
      <v-icon start>mdi-key-variant</v-icon>
      {{ joinCode }}
    </v-chip>
    <div class="d-flex gap-2 justify-center flex-wrap">
      <v-btn
        size="small"
        variant="tonal"
        color="primary"
        prepend-icon="mdi-content-copy"
        @click="copyLink"
      >
        Copy Link
      </v-btn>
      <v-btn
        size="small"
        variant="tonal"
        color="secondary"
        prepend-icon="mdi-download"
        @click="downloadQr"
      >
        Download QR
      </v-btn>
    </div>
    <v-snackbar v-model="copied" timeout="2000" color="success" location="bottom">
      <v-icon start>mdi-check</v-icon> Link copied!
    </v-snackbar>
  </v-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import QrcodeVue from 'qrcode.vue'

const props = defineProps<{ joinCode: string; sessionId: string }>()

const copied = ref(false)
const joinUrl = `${window.location.origin}${window.location.pathname}#/student/join/${props.joinCode}`

async function copyLink() {
  await navigator.clipboard.writeText(joinUrl)
  copied.value = true
}

function downloadQr() {
  const canvas = document.querySelector('.qr-card canvas') as HTMLCanvasElement
  if (!canvas) return
  const a = document.createElement('a')
  a.download = `join-${props.joinCode}.png`
  a.href = canvas.toDataURL()
  a.click()
}
</script>

<style scoped>
.qr-card { max-width: 280px; }
.qr-wrapper {
  display: inline-flex;
  padding: 8px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.join-code { letter-spacing: 4px; font-size: 20px !important; }
.letter-spacing { letter-spacing: 1px; }
</style>
