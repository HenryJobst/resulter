// file: src/services/api.ts

import axios from 'axios'

// Define the structure for environment variables if needed
interface EnvVariables {
  VITE_API_ENDPOINT: string
}

// Ensure that environment variables are correctly typed
const env: EnvVariables = import.meta.env as unknown as EnvVariables

// Creating an Axios instance
const instance = axios.create({
  baseURL: `${env.VITE_API_ENDPOINT}`,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Token Interceptor (example)
instance.interceptors.request.use(
  (config) => {
    const token = 'your_token_retrieval_logic' // Replace with your token retrieval logic
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export default instance
