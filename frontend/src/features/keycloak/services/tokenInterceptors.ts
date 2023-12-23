// file: src/services/tokenInterceptors.ts

//import axios, { AxiosRequestConfig, AxiosResponse, AxiosError } from "axios";
import axiosInstance from '@/features/keycloak/services/api'

interface Store {
  authenticated: boolean
  user: {
    token: string
  }
  refreshUserToken: () => Promise<void>
}

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
          // Handle token refresh failure (e.g., redirect to login page)
        }
      }

      return Promise.reject(error)
    }
  )
}

export default setup
