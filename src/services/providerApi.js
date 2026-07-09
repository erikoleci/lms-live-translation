import { api } from './api.js'

/** Maps to AiProviderResource — admin-only (spec 6.1, 12). Requires an
 * authenticated request with the "admin" role once OIDC is wired up. */
export const providerApi = {
  list() {
    return api.get('/api/ai-providers').then(r => r.data)
  },

  create(payload) {
    return api.post('/api/ai-providers', payload).then(r => r.data)
  },

  update(providerId, patch) {
    return api.patch(`/api/ai-providers/${providerId}`, patch).then(r => r.data)
  },

  disable(providerId) {
    return api.delete(`/api/ai-providers/${providerId}`)
  },
}
