<template>
  <v-app :theme="uiStore.darkMode ? 'dark' : 'light'">

    <!-- Faqet fullscreen (ActiveSession, LiveCaptions) nuk kanë sidebar -->
    <template v-if="!isFullscreen">

      <!-- MOBILE TOP BAR -->
      <v-app-bar color="primary" elevation="2" height="58" class="d-lg-none">
        <v-app-bar-nav-icon color="white" @click="mobileDrawer = !mobileDrawer" />
        <v-app-bar-title>
          <div class="d-flex align-center gap-2">
            <v-avatar size="28" rounded="lg" style="background:rgba(255,255,255,0.15)">
              <v-img src="/zana.png" alt="ZANA" cover />
            </v-avatar>
            <span class="font-weight-bold text-white" style="font-size:16px; letter-spacing:1px">ZANA</span>
          </div>
        </v-app-bar-title>
        <template #append>
          <v-btn :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
            variant="text" color="white" size="small" @click="uiStore.toggleDarkMode()" />
        </template>
      </v-app-bar>

      <!-- SIDEBAR -->
      <v-navigation-drawer
        v-model="sidebarOpen"
        :permanent="lgAndUp"
        :temporary="!lgAndUp"
        width="248"
        border="e"
      >
        <!-- Logo -->
        <div class="pa-4 d-flex align-center gap-3" style="min-height:68px">
          <v-avatar size="40" rounded="lg">
            <v-img src="/zana.png" alt="ZANA" cover />
          </v-avatar>
          <div>
            <div class="text-body-1 font-weight-bold" style="letter-spacing:1px">ZANA</div>
            <div class="text-caption text-medium-emphasis">Live Translation</div>
          </div>
          <v-spacer />
          <v-btn :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
            variant="text" size="x-small" @click="uiStore.toggleDarkMode()" />
        </div>

        <v-divider />

        <v-list nav density="comfortable" class="px-2 pt-3">

          <!-- TEACHER -->
          <div class="d-flex align-center gap-1 px-2 mb-2 mt-1">
            <v-icon size="13" color="primary">mdi-human-male-board</v-icon>
            <span class="text-caption font-weight-bold text-medium-emphasis" style="letter-spacing:0.8px">MËSUES</span>
          </div>
          <v-list-item v-for="item in teacherNav" :key="item.to"
            :to="item.to" :prepend-icon="item.icon" :title="item.label"
            rounded="xl" color="primary" class="mb-1" />

          <v-divider class="my-3" />

          <!-- STUDENT -->
          <div class="d-flex align-center gap-1 px-2 mb-2">
            <v-icon size="13" color="teal">mdi-school</v-icon>
            <span class="text-caption font-weight-bold text-medium-emphasis" style="letter-spacing:0.8px">STUDENT</span>
          </div>
          <v-list-item v-for="item in studentNav" :key="item.to"
            :to="item.to" :prepend-icon="item.icon" :title="item.label"
            rounded="xl" color="teal" class="mb-1" />

          <v-divider class="my-3" />

          <!-- ADMIN -->
          <div class="d-flex align-center gap-1 px-2 mb-2">
            <v-icon size="13" color="deep-orange">mdi-shield-account</v-icon>
            <span class="text-caption font-weight-bold text-medium-emphasis" style="letter-spacing:0.8px">ADMIN</span>
          </div>
          <v-list-item v-for="item in adminNav" :key="item.to"
            :to="item.to" :prepend-icon="item.icon" :title="item.label"
            rounded="xl" color="deep-orange" class="mb-1" />

        </v-list>

        <!-- Footer workspace -->
        <template #append>
          <v-divider />
          <div class="pa-3">
            <v-sheet rounded="xl" color="primary" class="pa-3 d-flex align-center gap-2">
              <v-icon color="white" size="16">mdi-domain</v-icon>
              <span class="text-caption font-weight-bold text-white text-truncate">
                {{ workspaceStore.activeWorkspace?.name ?? 'Demo Workspace' }}
              </span>
            </v-sheet>
          </div>
        </template>
      </v-navigation-drawer>
    </template>

    <!-- MAIN CONTENT -->
    <v-main>
      <router-view v-slot="{ Component }">
        <v-fade-transition mode="out-in">
          <component :is="Component" />
        </v-fade-transition>
      </router-view>
    </v-main>

  </v-app>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useUiStore } from './stores/ui.js'
import { useWorkspaceStore } from './stores/workspace.js'

const uiStore = useUiStore()
const workspaceStore = useWorkspaceStore()
const route = useRoute()
const { lgAndUp } = useDisplay()

const mobileDrawer = ref(false)
const sidebarOpen = computed({
  get: () => lgAndUp.value ? true : mobileDrawer.value,
  set: (v) => { if (!lgAndUp.value) mobileDrawer.value = v },
})

const isFullscreen = computed(() => !!route.meta.fullscreen)

const teacherNav = [
  { to: '/teacher', icon: 'mdi-view-dashboard-outline', label: 'Dashboard' },
]
const studentNav = [
  { to: '/student/join', icon: 'mdi-login-variant', label: 'Hyr në Sesion' },
]
const adminNav = [
  { to: '/admin', icon: 'mdi-chart-line', label: 'Përmbledhje' },
  { to: '/admin/workspaces', icon: 'mdi-domain', label: 'Workspaces' },
  { to: '/admin/stats', icon: 'mdi-chart-bar', label: 'Statistika' },
  { to: '/admin/providers', icon: 'mdi-server-network', label: 'Providers' },
]
</script>
