import type { BffUserInfo, User } from '../model/bffUser'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { bffAuthService } from '../services/bffAuthService'

/**
 * Authentication Store
 * Uses BFF (Backend-for-Frontend) authentication with session cookies
 */
export const useAuthStore = defineStore(
    'authStore',
    () => {
        const authenticated = ref<boolean>(false)
        const user = ref<User>({})

        // Computed properties
        const isAuthenticated = computed(() => authenticated.value)
        const isAdmin = computed(() =>
            user.value.roles?.includes('ADMIN')
            || user.value.roles?.includes('admin')
            || user.value.groups?.includes('ADMIN')
            || user.value.groups?.includes('admin'),
        )
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
         * Login - redirects to backend OAuth2 endpoint
         * @param url Redirect URL after login
         * @param locale User locale
         */
        async function login(url?: string, locale?: string) {
            const redirectPath = url || window.location.pathname
            bffAuthService.login(redirectPath, locale || 'de')
        }

        /**
         * Logout - calls backend logout endpoint
         */
        async function logout() {
            await bffAuthService.logout(window.location.pathname)
            clearUserData()
        }

        /**
         * Initialize authentication by checking current session
         * Calls /bff/user to retrieve user information and /bff/csrf to get CSRF token
         */
        async function initAuth(): Promise<boolean> {
            console.log('[Auth Store] initAuth called')
            const userInfo = await bffAuthService.initAuth()
            console.log('[Auth Store] User info received from BFF:', userInfo)
            if (userInfo) {
                setBffUser(userInfo)
                // Get CSRF token for authenticated users
                console.log('[Auth Store] Fetching CSRF token...')
                await bffAuthService.getCsrfToken()
                console.log('[Auth Store] CSRF token fetched')
                return true
            }
            else {
                console.log('[Auth Store] No user info, clearing data')
                clearUserData()
                return false
            }
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
            initAuth,
            setBffUser,
            clearUserData,
        }
    },
    {
        persist: {
            storage: sessionStorage,
            paths: ['authenticated', 'user'],
        },
    },
)

// Export store type
export type AuthStore = ReturnType<typeof useAuthStore>
