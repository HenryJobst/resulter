import type { AuthStore } from '../store/auth.store'
import axios from 'axios'

// Define the structure for environment variables
interface EnvVariables {
    VITE_API_ENDPOINT: string
}

// Ensure that environment variables are correctly typed
const env: EnvVariables = import.meta.env as unknown as EnvVariables

// Lazy loader for auth store to avoid circular dependencies
let authStoreModule: typeof import('../store/auth.store') | null = null

async function getAuthStore(): Promise<AuthStore> {
    if (!authStoreModule) {
        authStoreModule = await import('../store/auth.store')
    }
    return authStoreModule.useAuthStore()
}

// Create Axios instance
const axiosInstance = axios.create({
    baseURL: `${env.VITE_API_ENDPOINT}`,
    headers: {
        'Content-Type': 'application/json',
    },
    // Enable credentials (cookies) for BFF authentication
    withCredentials: true,
})

/**
 * Get CSRF token from cookie
 */
function getCsrfToken(): string | null {
    const name = 'XSRF-TOKEN'
    const value = `; ${document.cookie}`
    const parts = value.split(`; ${name}=`)
    if (parts.length === 2) {
        return parts.pop()?.split(';').shift() || null
    }
    return null
}

/**
 * Request interceptor
 * BFF mode: cookies are sent automatically (withCredentials: true)
 * CSRF token: read from cookie and send as header
 */
axiosInstance.interceptors.request.use(
    (config) => {
        // BFF mode: no Authorization header needed, cookies sent automatically

        // Add CSRF token to header for state-changing requests
        if (config.method && ['post', 'put', 'delete', 'patch'].includes(config.method.toLowerCase())) {
            const csrfToken = getCsrfToken()
            if (csrfToken) {
                config.headers['X-XSRF-TOKEN'] = csrfToken
            }
        }

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
    response => response,
    async (error) => {
        if (error.response) {
            const status = error.response.status

            // 401 Unauthorized
            if (status === 401) {
                // BFF mode: session expired, redirect to login
                // Store current path for post-login redirect
                sessionStorage.setItem('bff_post_login_redirect', window.location.pathname)

                // Clear auth store
                const authStore = await getAuthStore()
                authStore.clearUserData()

                // Redirect to login (backend will handle OAuth2)
                window.location.href = `${env.VITE_API_ENDPOINT}/oauth2/authorization/keycloak`
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
