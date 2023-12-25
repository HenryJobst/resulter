import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import KeycloakService from '@/features/keycloak/services/keycloak'
import type { User } from '@/features/keycloak/model/user'

export const useAuthStore = defineStore('authStore', () => {
  const authenticated = ref<boolean>(false)
  const user = ref<User>({})

  const isAuthenticated = computed(() => authenticated.value)

  function takeCredentials(keycloak: any) {
    authenticated.value = keycloak.authenticated

    if (authenticated.value) {
      user.value.username = keycloak.idTokenParsed.preferred_username
      user.value.token = keycloak.token
      user.value.refToken = keycloak.refreshToken
    } else {
      user.value.username = undefined
      user.value.token = undefined
      user.value.refToken = undefined
    }
  }

  function initOauth(keycloak: any, clearData = true) {
    // Consider defining a more specific type for keycloak
    if (clearData) {
      clearUserData()
    }
    takeCredentials(keycloak)
  }

  async function login(url?: string | null) {
    try {
      const keycloak = await KeycloakService.callLogin(url ? url : import.meta.env.VITE_APP_URL)
      takeCredentials(keycloak)
    } catch (error) {
      console.error(error)
      // Implement additional error handling as needed
    }
  }

  async function logout() {
    try {
      await KeycloakService.callLogout(import.meta.env.VITE_APP_URL)
      clearUserData()
    } catch (error) {
      console.error(error)
      // Implement additional error handling as needed
    }
  }

  async function refreshUserToken() {
    try {
      const keycloak = await KeycloakService.callTokenRefresh()
      initOauth(keycloak, false)
    } catch (error) {
      console.error(error)
      // Implement additional error handling as needed
    }
  }

  function clearUserData() {
    authenticated.value = false
    user.value = {}
  }

  return { authenticated, user, isAuthenticated, login, logout, initOauth, refreshUserToken }
})
