import axios from 'axios'

// Base URL comes from .env / .env.local -> VITE_API_BASE_URL (see .env.example).
// Falls back to localhost:8080 so `npm run dev` works out of the box against
// a locally-running `mvn quarkus:dev` backend.
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

// TODO: once Keycloak/OIDC is wired on the frontend, attach the bearer token
// here, e.g.:
// api.interceptors.request.use(config => {
//   const token = authStore.accessToken
//   if (token) config.headers.Authorization = `Bearer ${token}`
//   return config
// })

export function wsUrl(path) {
  const base = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080'
  return `${base}${path}`
}

/** Normalizes axios errors into a small, UI-friendly shape. */
export function apiErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  return error?.response?.data?.error || error?.message || fallback
}
