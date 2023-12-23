import { defineStore } from 'pinia'
import KeycloakService from '@/features/keycloak/services/keycloak'

// Typdefinitionen für den Zustand

interface User {
  username?: string
  token?: string
  refToken?: string
}

interface AuthStoreState {
  authenticated: boolean
  user: User
}

export const useAuthStore = defineStore({
  id: 'storeAuth',
  state: (): AuthStoreState => ({
    authenticated: false,
    user: {}
  }),
  //persist: true,
  actions: {
    initOauth(keycloak: any, clearData = true) {
      // Consider defining a more specific type for keycloak
      if (clearData) {
        this.clearUserData()
      }

      this.authenticated = keycloak.authenticated

      if (this.authenticated) {
        this.user.username = keycloak.idTokenParsed.preferred_username
        this.user.token = keycloak.token
        this.user.refToken = keycloak.refreshToken
      } else {
        this.user.username = undefined
        this.user.token = undefined
        this.user.refToken = undefined
      }
    },
    async logout() {
      try {
        await KeycloakService.callLogout(import.meta.env.VITE_APP_URL)
        this.clearUserData()
      } catch (error) {
        console.error(error)
        // Implement additional error handling as needed
      }
    },
    async refreshUserToken() {
      try {
        const keycloak = await KeycloakService.callTokenRefresh()
        this.initOauth(keycloak, false)
      } catch (error) {
        console.error(error)
        // Implement additional error handling as needed
      }
    },
    clearUserData() {
      this.authenticated = false
      this.user = {}
    }
  }
})
