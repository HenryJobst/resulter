import axios from 'axios'
import type { AuthStore } from '../store/auth.store'

// Define the structure for environment variables
interface EnvVariables {
    VITE_API_ENDPOINT: string
    VITE_USE_BFF_AUTH?: string
}

// Ensure that environment variables are correctly typed
const env: EnvVariables = import.meta.env as unknown as EnvVariables

// Create Axios instance
const axiosInstance = axios.create({
    baseURL: `${env.VITE_API_ENDPOINT}`,
    headers: {
        'Content-Type': 'application/json',
    },
    // Enable credentials (cookies) for BFF mode
    withCredentials: env.VITE_USE_BFF_AUTH === 'true',
})

/**
 * Request interceptor
 * - For BFF mode: cookies are sent automatically (withCredentials: true)
 * - For Keycloak mode: Bearer token is added to Authorization header
 */
axiosInstance.interceptors.request.use(
    (config) => {
        const useBff = env.VITE_USE_BFF_AUTH === 'true'

        if (!useBff) {
            // Legacy Keycloak mode: add Bearer token
            // Import here to avoid circular dependency
            const { useAuthStore } = require('../store/auth.store')
            const authStore = useAuthStore() as AuthStore
            const token = authStore.user.token

            if (token) {
                config.headers.Authorization = `Bearer ${token}`
            }
        }
        // BFF mode: no Authorization header needed, cookies sent automatically

        return config
    },
    (error) => {
        return Promise.reject(error)
    },
)

/**
 * Response interceptor
 * Handles common error responses
 */
axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response) {
            const status = error.response.status

            // 401 Unauthorized
            if (status === 401) {
                const useBff = env.VITE_USE_BFF_AUTH === 'true'

                if (useBff) {
                    // BFF mode: session expired, redirect to login
                    // Store current path for post-login redirect
                    sessionStorage.setItem('bff_post_login_redirect', window.location.pathname)

                    // Clear auth store
                    const { useAuthStore } = require('../store/auth.store')
                    const authStore = useAuthStore() as AuthStore
                    authStore.clearUserData()

                    // Redirect to login (backend will handle OAuth2)
                    window.location.href = `${env.VITE_API_ENDPOINT}/oauth2/authorization/keycloak`
                }
                else {
                    // Legacy Keycloak mode: try to refresh token
                    const { useAuthStore } = require('../store/auth.store')
                    const authStore = useAuthStore() as AuthStore

                    return authStore.refreshUserToken()
                        .then(() => {
                            // Retry original request with new token
                            return axiosInstance(error.config)
                        })
                        .catch(() => {
                            // Token refresh failed, redirect to login
                            authStore.clearUserData()
                            authStore.login(window.location.pathname)
                            return Promise.reject(error)
                        })
                }
            }

            // 403 Forbidden
            if (status === 403) {
                console.error('Access forbidden:', error.response.data)
            }
        }

        return Promise.reject(error)
    },
)

export default axiosInstance
