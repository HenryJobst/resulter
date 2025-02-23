import type { User } from '@/features/keycloak/model/user'
import KeycloakService from '@/features/keycloak/services/keycloak'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useAuthStore = defineStore(
    'authStore',
    () => {
        const authenticated = ref<boolean>(false)
        const user = ref<User>({})

        const isAuthenticated = computed(() => authenticated.value)
        const isAdmin = computed(() => user.value.roles?.includes('admin'))

        function takeCredentials(keycloak: any) {
            authenticated.value = keycloak != null && keycloak.authenticated

            if (authenticated.value) {
                user.value.subject = keycloak.subject
                user.value.username = keycloak.idTokenParsed.preferred_username
                user.value.token = keycloak.token
                user.value.refToken = keycloak.refreshToken
                user.value.roles = keycloak.realmAccess.roles
            }
            else {
                user.value.subject = undefined
                user.value.username = undefined
                user.value.token = undefined
                user.value.refToken = undefined
                user.value.roles = undefined
            }
        }

        function initOauth(keycloak: any, clearData = true) {
            // Consider defining a more specific type for keycloak
            if (clearData)
                clearUserData()

            takeCredentials(keycloak)
        }

        async function login(url?: string, locale?: string) {
            const keycloak = await KeycloakService.callLogin(
                url || import.meta.env.VITE_APP_URL,
                locale,
            )
            takeCredentials(keycloak)
        }

        async function logout() {
            await KeycloakService.callLogout(import.meta.env.VITE_APP_URL)
            clearUserData()
        }

        async function refreshUserToken() {
            const keycloak = await KeycloakService.callTokenRefresh()
            initOauth(keycloak, false)
        }

        function clearUserData() {
            authenticated.value = false
            user.value = {}
        }

        return {
            authenticated,
            user,
            isAuthenticated,
            isAdmin,
            login,
            logout,
            initOauth,
            refreshUserToken,
        }
    },
    {
        persist: {
            storage: sessionStorage,
        },
    },
)

// Exportiere den Typ des Stores
export type AuthStore = ReturnType<typeof useAuthStore>
