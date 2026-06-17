<template>
  <v-app :theme="uiStore.darkMode ? 'dark' : 'light'">

    <!-- MOBILE TOP BAR -->
    <v-app-bar v-if="!isFullscreen" color="primary" elevation="0" height="56" class="d-lg-none">
      <v-app-bar-nav-icon color="white" @click="mobileDrawer = !mobileDrawer" />
      <v-app-bar-title>
        <span class="font-weight-bold text-white" style="font-size:15px">LMS Live Translation</span>
      </v-app-bar-title>
      <template #append>
        <v-btn :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'" variant="text" color="white" size="small" @click="uiStore.toggleDarkMode()" />
      </template>
    </v-app-bar>

    <!-- SIDEBAR — permanent on lg+, temporary on mobile -->
    <v-navigation-drawer
      v-if="!isFullscreen"
      v-model="sidebarOpen"
      :permanent="lgAndUp"
      :temporary="!lgAndUp"
      width="240"
      class="sidebar"
    >
      <!-- Logo -->
      <div class="sidebar-logo px-4 py-4 d-flex align-center gap-3">
        <v-avatar color="primary" size="36" rounded="lg">
          <v-icon color="white" size="20">mdi-translate</v-icon>
        </v-avatar>
        <div class="flex-grow-1 min-w-0">
          <div class="text-body-2 font-weight-bold text-truncate">LMS Live</div>
          <div class="text-caption text-medium-emphasis">Translation</div>
        </div>
        <v-btn :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'" variant="text" size="x-small" @click="uiStore.toggleDarkMode()" />
      </div>

      <v-divider />

      <v-list nav density="compact" class="pt-2">

        <!-- TEACHER -->
        <div class="nav-group-label px-3 mt-3 mb-1">
          <v-icon size="13" color="primary" class="mr-1">mdi-human-male-board</v-icon>
          <span>Teacher</span>
        </div>
        <v-list-item
          v-for="item in teacherNav"
          :key="item.to"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.label"
          rounded="lg"
          class="nav-item mb-1"
          active-class="nav-item--active"
        />

        <v-divider class="my-2" />

        <!-- STUDENT -->
        <div class="nav-group-label px-3 mb-1">
          <v-icon size="13" color="teal" class="mr-1">mdi-school</v-icon>
          <span>Student</span>
        </div>
        <v-list-item
          v-for="item in studentNav"
          :key="item.to"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.label"
          rounded="lg"
          class="nav-item mb-1"
          active-class="nav-item--active"
        />

        <v-divider class="my-2" />

        <!-- ADMIN -->
        <div class="nav-group-label px-3 mb-1">
          <v-icon size="13" color="orange" class="mr-1">mdi-shield-account</v-icon>
          <span>Admin</span>
        </div>
        <v-list-item
          v-for="item in adminNav"
          :key="item.to"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.label"
          rounded="lg"
          class="nav-item mb-1"
          active-class="nav-item--active"
        />
      </v-list>

      <!-- Bottom workspace chip -->
      <template #append>
        <v-divider />
        <div class="px-3 py-3">
          <v-chip
            color="primary"
            variant="tonal"
            size="small"
            prepend-icon="mdi-domain"
            class="w-100 justify-start"
            style="max-width:100%"
          >
            {{ workspaceStore.activeWorkspace?.name ?? 'No Workspace' }}
          </v-chip>
        </div>
      </template>
    </v-navigation-drawer>

    <!-- MAIN CONTENT -->
    <v-main>
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
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
  { to: '/student/join', icon: 'mdi-login-variant', label: 'Join Session' },
]

const adminNav = [
  { to: '/admin', icon: 'mdi-chart-line', label: 'Overview' },
  { to: '/admin/workspaces', icon: 'mdi-domain', label: 'Workspaces' },
  { to: '/admin/stats', icon: 'mdi-chart-bar', label: 'Usage Stats' },
  { to: '/admin/providers', icon: 'mdi-server-network', label: 'Providers' },
]
</script>

<style>
:root { font-family: 'Inter', 'Roboto', sans-serif; }
.page-fade-enter-from, .page-fade-leave-to { opacity: 0; transform: translateY(4px); }
.page-fade-enter-active, .page-fade-leave-active { transition: all 0.15s ease; }
::-webkit-scrollbar { width: 5px; height: 5px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.12); border-radius: 3px; }
</style>

<style scoped>
.sidebar { border-right: 1px solid rgba(0,0,0,0.07) !important; }
.sidebar-logo { min-height: 64px; }
.nav-group-label { display: flex; align-items: center; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.8px; color: #999; }
.nav-item { font-size: 13px !important; }
.nav-item--active { background: rgba(21,101,192,0.1) !important; color: #1565C0 !important; font-weight: 600; }
</style>
