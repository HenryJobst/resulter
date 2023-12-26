import axiosInstance from '@/features/keycloak/services/api'
import KeycloakService from '@/features/keycloak/services/keycloak'

const setup = (store: any) => {
  axiosInstance.interceptors.request.use(
    (config) => {
      if (store.authenticated) {
        config.headers = config.headers ?? {}
        config.headers['x-access-token'] = store.user.token
      }
      return config
    },
    (error) => Promise.reject(error)
  )

  axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalConfig = error.config

      if (error.response?.status === 401 && !originalConfig._retry) {
        originalConfig._retry = true
        try {
          await store.refreshUserToken()
          originalConfig.headers = originalConfig.headers ?? {}
          originalConfig.headers['x-access-token'] = store.user.token
          return axiosInstance(originalConfig)
        } catch (_error) {
          console.error('Refresh token failed', _error)
          await KeycloakService.callLogin(originalConfig.url)
        }
      }

      return Promise.reject(error)
    }
  )
}

export default setup
