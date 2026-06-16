import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/teacher' },
    {
      path: '/teacher',
      component: () => import('../views/teacher/TeacherDashboard.vue'),
      meta: { role: 'teacher', title: 'Teacher Dashboard' },
    },
    {
      path: '/teacher/session/:id',
      component: () => import('../views/teacher/ActiveSession.vue'),
      meta: { role: 'teacher', title: 'Active Session' },
    },
    {
      path: '/teacher/session/:id/transcript',
      component: () => import('../views/teacher/TranscriptExport.vue'),
      meta: { role: 'teacher', title: 'Session Transcript' },
    },
    {
      path: '/student/join',
      component: () => import('../views/student/JoinSession.vue'),
      meta: { role: 'student', title: 'Join Session' },
    },
    {
      path: '/student/join/:code',
      component: () => import('../views/student/JoinSession.vue'),
      meta: { role: 'student', title: 'Join Session' },
    },
    {
      path: '/student/session/:id',
      component: () => import('../views/student/LiveCaptions.vue'),
      meta: { role: 'student', title: 'Live Captions' },
    },
    {
      path: '/admin',
      component: () => import('../views/admin/AdminDashboard.vue'),
      meta: { role: 'admin', title: 'Admin Dashboard' },
    },
    {
      path: '/admin/providers',
      component: () => import('../views/admin/ProviderConfig.vue'),
      meta: { role: 'admin', title: 'Provider Configuration' },
    },
  ],
})

router.afterEach((to) => {
  document.title = `${to.meta.title ?? 'LMS'} — Live Translation`
})

export default router
