import { useAuthStore } from '@/features/keycloak/store/auth.store'
import axios from 'axios'

// Define the structure for environment variables if needed
interface EnvVariables {
    VITE_API_ENDPOINT: string
}

// Ensure that environment variables are correctly typed
const env: EnvVariables = import.meta.env as unknown as EnvVariables

// Creating an Axios instance
const axiosInstance = axios.create({
    baseURL: `${env.VITE_API_ENDPOINT}`,
    headers: {
        'Content-Type': 'application/json',
    },
})

// Token Interceptor
axiosInstance.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore()
        const token = authStore.user.token
        if (token)
            config.headers.Authorization = `Bearer ${token}`

        return config
    },
    (error) => {
        return Promise.reject(error)
    },
)

export default axiosInstance
