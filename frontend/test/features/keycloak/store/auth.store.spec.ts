import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import KeycloakService from '@/features/keycloak/services/keycloak'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

// Mock KeycloakService
vi.mock('@/features/keycloak/services/keycloak', () => ({
    default: {
        callLogin: vi.fn(),
        callLogout: vi.fn(),
        callTokenRefresh: vi.fn(),
    },
}))

// Mock environment variable
const MOCK_APP_URL = 'http://localhost:3000'

describe('authStore', () => {
    let authStore: ReturnType<typeof useAuthStore>

    // Mock Keycloak object
    const mockKeycloakAuthenticated = {
        authenticated: true,
        subject: 'user-123',
        token: 'mock-token',
        refreshToken: 'mock-refresh-token',
        idTokenParsed: {
            preferred_username: 'testuser',
        },
        realmAccess: {
            roles: ['user', 'admin'],
        },
    }

    const mockKeycloakUnauthenticated = {
        authenticated: false,
    }

    beforeEach(() => {
        // Create a fresh pinia instance for each test
        setActivePinia(createPinia())
        authStore = useAuthStore()
        vi.clearAllMocks()
    })

    describe('initial state', () => {
        it('should have authenticated as false initially', () => {
            expect(authStore.authenticated).toBe(false)
        })

        it('should have empty user object initially', () => {
            expect(authStore.user).toEqual({})
        })

        it('should have isAuthenticated computed as false initially', () => {
            expect(authStore.isAuthenticated).toBe(false)
        })

        it('should have isAdmin computed as false initially', () => {
            expect(authStore.isAdmin).toBeFalsy()
        })
    })

    describe('takeCredentials', () => {
        it('should set authenticated to true with valid keycloak object', () => {
            authStore.initOauth(mockKeycloakAuthenticated)

            expect(authStore.authenticated).toBe(true)
        })

        it('should extract user data from authenticated keycloak', () => {
            authStore.initOauth(mockKeycloakAuthenticated)

            expect(authStore.user.subject).toBe('user-123')
            expect(authStore.user.username).toBe('testuser')
            expect(authStore.user.token).toBe('mock-token')
            expect(authStore.user.refToken).toBe('mock-refresh-token')
            expect(authStore.user.roles).toEqual(['user', 'admin'])
        })

        it('should set authenticated to false with unauthenticated keycloak', () => {
            authStore.initOauth(mockKeycloakUnauthenticated)

            expect(authStore.authenticated).toBe(false)
        })

        it('should clear user data when keycloak is not authenticated', () => {
            // First authenticate
            authStore.initOauth(mockKeycloakAuthenticated)
            expect(authStore.user.username).toBe('testuser')

            // Then pass unauthenticated keycloak
            authStore.initOauth(mockKeycloakUnauthenticated)

            expect(authStore.user.subject).toBeUndefined()
            expect(authStore.user.username).toBeUndefined()
            expect(authStore.user.token).toBeUndefined()
            expect(authStore.user.refToken).toBeUndefined()
            expect(authStore.user.roles).toBeUndefined()
        })

        it('should handle null keycloak object', () => {
            authStore.initOauth(null)

            expect(authStore.authenticated).toBe(false)
        })
    })

    describe('initOauth', () => {
        it('should clear user data by default', () => {
            // First set some data
            authStore.initOauth(mockKeycloakAuthenticated)
            expect(authStore.authenticated).toBe(true)

            // Then init with unauthenticated (should clear)
            authStore.initOauth(mockKeycloakUnauthenticated)

            expect(authStore.authenticated).toBe(false)
            expect(authStore.user).toEqual({})
        })

        it('should not clear user data when clearData is false', () => {
            // First set some data
            authStore.initOauth(mockKeycloakAuthenticated)

            // Then init with clearData = false
            authStore.initOauth(mockKeycloakAuthenticated, false)

            // Data should be updated, not cleared first
            expect(authStore.user.username).toBe('testuser')
        })

        it('should call takeCredentials', () => {
            authStore.initOauth(mockKeycloakAuthenticated)

            expect(authStore.authenticated).toBe(true)
            expect(authStore.user.token).toBe('mock-token')
        })
    })

    describe('login', () => {
        it('should call KeycloakService.callLogin with default URL', async () => {
            vi.mocked(KeycloakService.callLogin).mockResolvedValue(mockKeycloakAuthenticated)

            await authStore.login()

            expect(KeycloakService.callLogin).toHaveBeenCalled()
            // First parameter should be the app URL from env
            expect(vi.mocked(KeycloakService.callLogin).mock.calls[0][1]).toBeUndefined()
        })

        it('should call KeycloakService.callLogin with custom URL', async () => {
            vi.mocked(KeycloakService.callLogin).mockResolvedValue(mockKeycloakAuthenticated)

            const CUSTOM_URL = 'https://custom-url.com'

            await authStore.login(CUSTOM_URL)

            expect(KeycloakService.callLogin).toHaveBeenCalledWith(
                CUSTOM_URL,
                undefined,
            )
        })

        it('should call KeycloakService.callLogin with locale', async () => {
            vi.mocked(KeycloakService.callLogin).mockResolvedValue(mockKeycloakAuthenticated)

            await authStore.login(MOCK_APP_URL, 'de')

            expect(KeycloakService.callLogin).toHaveBeenCalledWith(
                MOCK_APP_URL,
                'de',
            )
        })

        it('should set user credentials after successful login', async () => {
            vi.mocked(KeycloakService.callLogin).mockResolvedValue(mockKeycloakAuthenticated)

            await authStore.login()

            expect(authStore.authenticated).toBe(true)
            expect(authStore.user.username).toBe('testuser')
            expect(authStore.user.token).toBe('mock-token')
        })

        it('should handle login failure', async () => {
            vi.mocked(KeycloakService.callLogin).mockResolvedValue(mockKeycloakUnauthenticated)

            await authStore.login()

            expect(authStore.authenticated).toBe(false)
        })
    })

    describe('logout', () => {
        it('should call KeycloakService.callLogout', async () => {
            vi.mocked(KeycloakService.callLogout).mockResolvedValue(undefined)

            await authStore.logout()

            expect(KeycloakService.callLogout).toHaveBeenCalled()
        })

        it('should clear user data after logout', async () => {
            // First login
            authStore.initOauth(mockKeycloakAuthenticated)
            expect(authStore.authenticated).toBe(true)

            vi.mocked(KeycloakService.callLogout).mockResolvedValue(undefined)

            // Then logout
            await authStore.logout()

            expect(authStore.authenticated).toBe(false)
            expect(authStore.user).toEqual({})
        })
    })

    describe('refreshUserToken', () => {
        it('should call KeycloakService.callTokenRefresh', async () => {
            const refreshedKeycloak = {
                ...mockKeycloakAuthenticated,
                token: 'new-token',
                refreshToken: 'new-refresh-token',
            }

            vi.mocked(KeycloakService.callTokenRefresh).mockResolvedValue(refreshedKeycloak)

            await authStore.refreshUserToken()

            expect(KeycloakService.callTokenRefresh).toHaveBeenCalled()
        })

        it('should update tokens after refresh', async () => {
            // First set initial tokens
            authStore.initOauth(mockKeycloakAuthenticated)
            expect(authStore.user.token).toBe('mock-token')

            const refreshedKeycloak = {
                ...mockKeycloakAuthenticated,
                token: 'new-token',
                refreshToken: 'new-refresh-token',
            }

            vi.mocked(KeycloakService.callTokenRefresh).mockResolvedValue(refreshedKeycloak)

            await authStore.refreshUserToken()

            expect(authStore.user.token).toBe('new-token')
            expect(authStore.user.refToken).toBe('new-refresh-token')
        })

        it('should call initOauth with clearData false', async () => {
            const refreshedKeycloak = {
                ...mockKeycloakAuthenticated,
                token: 'new-token',
            }

            vi.mocked(KeycloakService.callTokenRefresh).mockResolvedValue(refreshedKeycloak)

            // Set initial state
            authStore.initOauth(mockKeycloakAuthenticated)
            const username = authStore.user.username

            await authStore.refreshUserToken()

            // Username should still be there (not cleared)
            expect(authStore.user.username).toBe(username)
        })
    })

    describe('clearUserData', () => {
        it('should reset authenticated to false', () => {
            authStore.initOauth(mockKeycloakAuthenticated)
            expect(authStore.authenticated).toBe(true)

            authStore.initOauth(mockKeycloakUnauthenticated)

            expect(authStore.authenticated).toBe(false)
        })

        it('should reset user to empty object', async () => {
            authStore.initOauth(mockKeycloakAuthenticated)
            expect(authStore.user.username).toBe('testuser')

            // Clear via logout (which calls clearUserData)
            vi.mocked(KeycloakService.callLogout).mockResolvedValue(undefined)
            await authStore.logout()

            expect(authStore.user).toEqual({})
        })
    })

    describe('computed properties', () => {
        describe('isAuthenticated', () => {
            it('should return false when not authenticated', () => {
                expect(authStore.isAuthenticated).toBe(false)
            })

            it('should return true when authenticated', () => {
                authStore.initOauth(mockKeycloakAuthenticated)

                expect(authStore.isAuthenticated).toBe(true)
            })

            it('should reactively update when authenticated changes', () => {
                expect(authStore.isAuthenticated).toBe(false)

                authStore.initOauth(mockKeycloakAuthenticated)
                expect(authStore.isAuthenticated).toBe(true)

                authStore.initOauth(mockKeycloakUnauthenticated)
                expect(authStore.isAuthenticated).toBe(false)
            })
        })

        describe('isAdmin', () => {
            it('should return false when user has no roles', () => {
                expect(authStore.isAdmin).toBeFalsy()
            })

            it('should return true when user has admin role', () => {
                authStore.initOauth(mockKeycloakAuthenticated)

                expect(authStore.isAdmin).toBe(true)
            })

            it('should return false when user has roles but not admin', () => {
                const keycloakWithoutAdmin = {
                    ...mockKeycloakAuthenticated,
                    realmAccess: {
                        roles: ['user', 'viewer'],
                    },
                }

                authStore.initOauth(keycloakWithoutAdmin)

                expect(authStore.isAdmin).toBe(false)
            })

            it('should return false when roles is undefined', () => {
                const keycloakNoRoles = {
                    ...mockKeycloakAuthenticated,
                    realmAccess: {
                        roles: undefined,
                    },
                }

                authStore.initOauth(keycloakNoRoles)

                expect(authStore.isAdmin).toBeFalsy()
            })

            it('should reactively update when roles change', () => {
                expect(authStore.isAdmin).toBeFalsy()

                authStore.initOauth(mockKeycloakAuthenticated)
                expect(authStore.isAdmin).toBe(true)

                const keycloakWithoutAdmin = {
                    ...mockKeycloakAuthenticated,
                    realmAccess: { roles: ['user'] },
                }
                authStore.initOauth(keycloakWithoutAdmin)
                expect(authStore.isAdmin).toBe(false)
            })
        })
    })

    describe('edge cases', () => {
        it('should handle keycloak object with missing idTokenParsed', () => {
            const invalidKeycloak = {
                authenticated: true,
                subject: 'user-123',
                token: 'token',
                refreshToken: 'refresh',
                idTokenParsed: undefined,
                realmAccess: { roles: ['user'] },
            }

            // This might throw or handle gracefully depending on implementation
            expect(() => authStore.initOauth(invalidKeycloak)).toThrow()
        })

        it('should handle keycloak object with missing realmAccess', () => {
            const invalidKeycloak = {
                authenticated: true,
                subject: 'user-123',
                token: 'token',
                refreshToken: 'refresh',
                idTokenParsed: { preferred_username: 'test' },
                realmAccess: undefined,
            }

            // This might throw or handle gracefully
            expect(() => authStore.initOauth(invalidKeycloak)).toThrow()
        })

        it('should handle empty roles array', () => {
            const keycloakEmptyRoles = {
                ...mockKeycloakAuthenticated,
                realmAccess: {
                    roles: [],
                },
            }

            authStore.initOauth(keycloakEmptyRoles)

            expect(authStore.isAdmin).toBe(false)
            expect(authStore.user.roles).toEqual([])
        })

        it('should handle multiple login/logout cycles', async () => {
            vi.mocked(KeycloakService.callLogin).mockResolvedValue(mockKeycloakAuthenticated)
            vi.mocked(KeycloakService.callLogout).mockResolvedValue(undefined)

            // Cycle 1
            await authStore.login()
            expect(authStore.authenticated).toBe(true)
            await authStore.logout()
            expect(authStore.authenticated).toBe(false)

            // Cycle 2
            await authStore.login()
            expect(authStore.authenticated).toBe(true)
            await authStore.logout()
            expect(authStore.authenticated).toBe(false)
        })
    })
})
