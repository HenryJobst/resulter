import type { BffUserInfo, User } from '../model/bffUser'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import KeycloakService from '@/features/keycloak/services/keycloak'
import { bffAuthService } from '../services/bffAuthService'

/**
 * Authentication Store
 * Supports both Keycloak (legacy) and BFF (new) authentication flows
 * Controlled by VITE_USE_BFF_AUTH environment variable
 */
export const useAuthStore = defineStore(
    'authStore',
    () => {
        const authenticated = ref<boolean>(false)
        const user = ref<User>({})
        const useBff = ref<boolean>(import.meta.env.VITE_USE_BFF_AUTH === 'true')

        // Computed properties
        const isAuthenticated = computed(() => authenticated.value)
        const isAdmin = computed(() => user.value.roles?.includes('ADMIN') || user.value.roles?.includes('admin'))
        const canManageEvents = computed(() => user.value.permissions?.canManageEvents ?? isAdmin.value)
        const canUploadResults = computed(() => user.value.permissions?.canUploadResults ?? isAdmin.value)
        const canManageCups = computed(() => user.value.permissions?.canManageCups ?? isAdmin.value)
        const canViewReports = computed(() => user.value.permissions?.canViewReports ?? true)
        const canManageUsers = computed(() => user.value.permissions?.canManageUsers ?? isAdmin.value)
        const canAccessAdmin = computed(() => user.value.permissions?.canAccessAdmin ?? isAdmin.value)

        /**
         * Set BFF user information from /bff/user endpoint
         * @param userInfo BFF user info response
         */
        function setBffUser(userInfo: BffUserInfo) {
            console.log('[Auth Store] setBffUser called with:', userInfo)
            authenticated.value = true
            user.value = {
                username: userInfo.username,
                email: userInfo.email,
                name: userInfo.name,
                roles: userInfo.roles,
                groups: userInfo.groups,
                permissions: userInfo.permissions,
            }
            console.log('[Auth Store] After setBffUser - authenticated:', authenticated.value, 'isAdmin:', isAdmin.value)
        }

        /**
         * Take credentials from Keycloak (legacy flow)
         * @param keycloak Keycloak instance
         */
        function takeCredentials(keycloak: any) {
            authenticated.value = keycloak != null && keycloak.authenticated

            if (authenticated.value) {
                user.value.username = keycloak.idTokenParsed.preferred_username
                user.value.email = keycloak.idTokenParsed.email
                user.value.name = keycloak.idTokenParsed.name
                user.value.roles = keycloak.realmAccess.roles
                user.value.groups = keycloak.idTokenParsed.groups || []
                // Note: Legacy flow doesn't have structured permissions
                // UI will use isAdmin computed property
            }
            else {
                clearUserData()
            }
        }

        /**
         * Initialize OAuth (Keycloak legacy flow)
         * @param keycloak Keycloak instance
         * @param clearData Whether to clear existing data first
         */
        function initOauth(keycloak: any, clearData = true) {
            if (clearData)
                clearUserData()

            takeCredentials(keycloak)
        }

        /**
         * Login - delegates to BFF or Keycloak based on configuration
         * @param url Redirect URL after login
         * @param locale User locale
         */
        async function login(url?: string, locale?: string) {
            if (useBff.value) {
                // BFF flow: redirect to backend OAuth2 endpoint
                const redirectPath = url || window.location.pathname
                bffAuthService.login(redirectPath, locale || 'de')
            }
            else {
                // Legacy Keycloak flow
                const keycloak = await KeycloakService.callLogin(
                    url || import.meta.env.VITE_APP_URL,
                    locale,
                )
                takeCredentials(keycloak)
            }
        }

        /**
         * Logout - delegates to BFF or Keycloak based on configuration
         */
        async function logout() {
            if (useBff.value) {
                // BFF flow: call backend logout
                await bffAuthService.logout(window.location.pathname)
            }
            else {
                // Legacy Keycloak flow
                await KeycloakService.callLogout(import.meta.env.VITE_APP_URL)
            }
            clearUserData()
        }

        /**
         * Refresh user token (only for Keycloak legacy flow)
         * BFF flow uses automatic session refresh
         */
        async function refreshUserToken() {
            if (!useBff.value) {
                const keycloak = await KeycloakService.callTokenRefresh()
                initOauth(keycloak, false)
            }
            // BFF flow doesn't need manual token refresh
            // Session is managed by backend
        }

        /**
         * Initialize authentication
         * For BFF: calls /bff/user
         * For Keycloak: uses existing init
         */
        async function initAuth(): Promise<boolean> {
            console.log('[Auth Store] initAuth called, useBff:', useBff.value)
            if (useBff.value) {
                const userInfo = await bffAuthService.initAuth()
                console.log('[Auth Store] User info received from BFF:', userInfo)
                if (userInfo) {
                    setBffUser(userInfo)
                    return true
                }
                else {
                    console.log('[Auth Store] No user info, clearing data')
                    clearUserData()
                    return false
                }
            }
            // Keycloak init handled separately in main.ts
            return false
        }

        /**
         * Clear user data
         */
        function clearUserData() {
            authenticated.value = false
            user.value = {}
        }

        return {
            authenticated,
            user,
            useBff,
            isAuthenticated,
            isAdmin,
            canManageEvents,
            canUploadResults,
            canManageCups,
            canViewReports,
            canManageUsers,
            canAccessAdmin,
            login,
            logout,
            initOauth,
            initAuth,
            setBffUser,
            refreshUserToken,
            clearUserData,
        }
    },
    {
        persist: {
            storage: sessionStorage,
            paths: ['authenticated', 'user'], // Don't persist useBff, always read from env
        },
    },
)

// Export store type
export type AuthStore = ReturnType<typeof useAuthStore>
