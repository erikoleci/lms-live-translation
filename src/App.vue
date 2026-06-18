<template>
  <v-app :theme="uiStore.darkMode ? 'dark' : 'light'">

    <!-- MOBILE TOP BAR -->
    <v-app-bar v-if="!isFullscreen" color="primary" elevation="0" height="56" class="d-lg-none">
      <v-app-bar-nav-icon color="white" @click="mobileDrawer = !mobileDrawer" />
      <v-app-bar-title>
        <span class="font-weight-bold text-white" style="font-size:15px">ZANA</span>
      </v-app-bar-title>
      <template #append>
        <v-btn
          :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
          variant="text" color="white" size="small"
          @click="uiStore.toggleDarkMode()"
        />
      </template>
    </v-app-bar>

    <!-- SIDEBAR -->
    <v-navigation-drawer
      v-if="!isFullscreen"
      v-model="sidebarOpen"
      :permanent="lgAndUp"
      :temporary="!lgAndUp"
      width="240"
      border="e"
    >
      <!-- Logo row -->
      <v-list-item class="py-3 px-4" style="min-height:64px">
        <template #prepend>
          <v-avatar color="primary" size="36" rounded="lg" class="mr-3">
            <v-icon color="white" size="20">mdi-translate</v-icon>
          </v-avatar>
        </template>
        <v-list-item-title class="text-body-2 font-weight-bold">ZANA</v-list-item-title>
        <template #append>
          <v-btn
            :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
            variant="text" size="x-small"
            @click="uiStore.toggleDarkMode()"
          />
        </template>
      </v-list-item>

      <v-divider />

      <v-list nav density="compact" class="pt-2">

        <!-- TEACHER -->
        <v-list-subheader class="text-caption font-weight-bold mt-2 mb-1" style="letter-spacing:0.8px">
          <v-icon size="13" color="primary" class="mr-1">mdi-human-male-board</v-icon>
          TEACHER
        </v-list-subheader>
        <v-list-item
          v-for="item in teacherNav" :key="item.to"
          :to="item.to" :prepend-icon="item.icon" :title="item.label"
          rounded="lg" color="primary" class="mb-1"
        />

        <v-divider class="my-2" />

        <!-- STUDENT -->
        <v-list-subheader class="text-caption font-weight-bold mb-1" style="letter-spacing:0.8px">
          <v-icon size="13" color="teal" class="mr-1">mdi-school</v-icon>
          STUDENT
        </v-list-subheader>
        <v-list-item
          v-for="item in studentNav" :key="item.to"
          :to="item.to" :prepend-icon="item.icon" :title="item.label"
          rounded="lg" color="teal" class="mb-1"
        />

        <v-divider class="my-2" />

        <!-- ADMIN -->
        <v-list-subheader class="text-caption font-weight-bold mb-1" style="letter-spacing:0.8px">
          <v-icon size="13" color="orange" class="mr-1">mdi-shield-account</v-icon>
          ADMIN
        </v-list-subheader>
        <v-list-item
          v-for="item in adminNav" :key="item.to"
          :to="item.to" :prepend-icon="item.icon" :title="item.label"
          rounded="lg" color="orange" class="mb-1"
        />
      </v-list>

      <!-- Workspace chip -->
      <template #append>
        <v-divider />
        <div class="pa-3">
          <v-chip
            color="primary" variant="tonal" size="small"
            prepend-icon="mdi-domain"
            class="w-100 justify-start"
            style="max-width:100%"
          >{{ workspaceStore.activeWorkspace?.name ?? 'No Workspace' }}</v-chip>
        </div>
      </template>
    </v-navigation-drawer>

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
  { to: '/student/join', icon: 'mdi-login-variant', label: 'Join Session' },
]
const adminNav = [
  { to: '/admin', icon: 'mdi-chart-line', label: 'Overview' },
  { to: '/admin/workspaces', icon: 'mdi-domain', label: 'Workspaces' },
  { to: '/admin/stats', icon: 'mdi-chart-bar', label: 'Usage Stats' },
  { to: '/admin/providers', icon: 'mdi-server-network', label: 'Providers' },
]
</script>
