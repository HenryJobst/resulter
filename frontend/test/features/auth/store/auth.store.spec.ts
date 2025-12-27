import type { BffUserInfo } from '@/features/auth/model/bffUser'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { bffAuthService } from '@/features/auth/services/bffAuthService'
import { useAuthStore } from '@/features/auth/store/auth.store'

// Mock bffAuthService
vi.mock('@/features/auth/services/bffAuthService', () => ({
    bffAuthService: {
        login: vi.fn(),
        logout: vi.fn(),
        initAuth: vi.fn(),
    },
}))

// Mock window.location
const mockLocation = {
    pathname: '/test-path',
}
Object.defineProperty(window, 'location', {
    value: mockLocation,
    writable: true,
})

describe('authStore (BFF Pattern)', () => {
    let authStore: ReturnType<typeof useAuthStore>

    // Mock BFF user info
    const mockBffUserInfo: BffUserInfo = {
        username: 'testuser',
        email: 'test@example.com',
        name: 'Test User',
        roles: ['USER', 'ADMIN'],
        groups: ['developers'],
        permissions: {
            canManageEvents: true,
            canUploadResults: true,
            canManageCups: true,
            canViewReports: true,
            canManageUsers: true,
            canAccessAdmin: true,
        },
    }

    const mockBffUserInfoNoAdmin: BffUserInfo = {
        username: 'regularuser',
        email: 'regular@example.com',
        name: 'Regular User',
        roles: ['USER'],
        groups: ['users'],
        permissions: {
            canManageEvents: false,
            canUploadResults: false,
            canManageCups: false,
            canViewReports: true,
            canManageUsers: false,
            canAccessAdmin: false,
        },
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

        it('should have all permissions as falsy initially', () => {
            expect(authStore.canManageEvents).toBeFalsy()
            expect(authStore.canUploadResults).toBeFalsy()
            expect(authStore.canManageCups).toBeFalsy()
            expect(authStore.canManageUsers).toBeFalsy()
            expect(authStore.canAccessAdmin).toBeFalsy()
        })

        it('should have canViewReports as true by default', () => {
            expect(authStore.canViewReports).toBe(true)
        })
    })

    describe('setBffUser', () => {
        it('should set authenticated to true with valid user info', () => {
            authStore.setBffUser(mockBffUserInfo)

            expect(authStore.authenticated).toBe(true)
        })

        it('should extract user data from BFF user info', () => {
            authStore.setBffUser(mockBffUserInfo)

            expect(authStore.user.username).toBe('testuser')
            expect(authStore.user.email).toBe('test@example.com')
            expect(authStore.user.name).toBe('Test User')
            expect(authStore.user.roles).toEqual(['USER', 'ADMIN'])
            expect(authStore.user.groups).toEqual(['developers'])
        })

        it('should set permissions from BFF user info', () => {
            authStore.setBffUser(mockBffUserInfo)

            expect(authStore.user.permissions?.canManageEvents).toBe(true)
            expect(authStore.user.permissions?.canUploadResults).toBe(true)
            expect(authStore.user.permissions?.canManageCups).toBe(true)
            expect(authStore.user.permissions?.canViewReports).toBe(true)
            expect(authStore.user.permissions?.canManageUsers).toBe(true)
            expect(authStore.user.permissions?.canAccessAdmin).toBe(true)
        })

        it('should handle user info with no admin role', () => {
            authStore.setBffUser(mockBffUserInfoNoAdmin)

            expect(authStore.authenticated).toBe(true)
            expect(authStore.user.username).toBe('regularuser')
            expect(authStore.user.roles).toEqual(['USER'])
            expect(authStore.isAdmin).toBe(false)
        })

        it('should update user data on subsequent calls', () => {
            // First set admin user
            authStore.setBffUser(mockBffUserInfo)
            expect(authStore.user.username).toBe('testuser')
            expect(authStore.isAdmin).toBe(true)

            // Then set regular user
            authStore.setBffUser(mockBffUserInfoNoAdmin)
            expect(authStore.user.username).toBe('regularuser')
            expect(authStore.isAdmin).toBe(false)
        })
    })

    describe('login', () => {
        it('should call bffAuthService.login with default redirect path', async () => {
            vi.mocked(bffAuthService.login).mockResolvedValue(undefined)

            await authStore.login()

            expect(bffAuthService.login).toHaveBeenCalledWith('/test-path', 'de')
        })

        it('should call bffAuthService.login with custom URL', async () => {
            vi.mocked(bffAuthService.login).mockResolvedValue(undefined)

            await authStore.login('/custom-path')

            expect(bffAuthService.login).toHaveBeenCalledWith('/custom-path', 'de')
        })

        it('should call bffAuthService.login with custom locale', async () => {
            vi.mocked(bffAuthService.login).mockResolvedValue(undefined)

            await authStore.login('/custom-path', 'en')

            expect(bffAuthService.login).toHaveBeenCalledWith('/custom-path', 'en')
        })
    })

    describe('logout', () => {
        it('should call bffAuthService.logout', async () => {
            vi.mocked(bffAuthService.logout).mockResolvedValue(undefined)

            await authStore.logout()

            expect(bffAuthService.logout).toHaveBeenCalledWith('/test-path')
        })

        it('should clear user data after logout', async () => {
            // First set user
            authStore.setBffUser(mockBffUserInfo)
            expect(authStore.authenticated).toBe(true)

            vi.mocked(bffAuthService.logout).mockResolvedValue(undefined)

            // Then logout
            await authStore.logout()

            expect(authStore.authenticated).toBe(false)
            expect(authStore.user).toEqual({})
        })
    })

    describe('initAuth', () => {
        it('should call bffAuthService.initAuth', async () => {
            vi.mocked(bffAuthService.initAuth).mockResolvedValue(mockBffUserInfo)

            await authStore.initAuth()

            expect(bffAuthService.initAuth).toHaveBeenCalled()
        })

        it('should set user data when initAuth returns user info', async () => {
            vi.mocked(bffAuthService.initAuth).mockResolvedValue(mockBffUserInfo)

            const result = await authStore.initAuth()

            expect(result).toBe(true)
            expect(authStore.authenticated).toBe(true)
            expect(authStore.user.username).toBe('testuser')
        })

        it('should clear user data when initAuth returns null', async () => {
            // First set some user data
            authStore.setBffUser(mockBffUserInfo)
            expect(authStore.authenticated).toBe(true)

            vi.mocked(bffAuthService.initAuth).mockResolvedValue(null)

            const result = await authStore.initAuth()

            expect(result).toBe(false)
            expect(authStore.authenticated).toBe(false)
            expect(authStore.user).toEqual({})
        })

        it('should return true when user is authenticated', async () => {
            vi.mocked(bffAuthService.initAuth).mockResolvedValue(mockBffUserInfo)

            const result = await authStore.initAuth()

            expect(result).toBe(true)
        })

        it('should return false when user is not authenticated', async () => {
            vi.mocked(bffAuthService.initAuth).mockResolvedValue(null)

            const result = await authStore.initAuth()

            expect(result).toBe(false)
        })
    })

    describe('clearUserData', () => {
        it('should reset authenticated to false', () => {
            authStore.setBffUser(mockBffUserInfo)
            expect(authStore.authenticated).toBe(true)

            authStore.clearUserData()

            expect(authStore.authenticated).toBe(false)
        })

        it('should reset user to empty object', () => {
            authStore.setBffUser(mockBffUserInfo)
            expect(authStore.user.username).toBe('testuser')

            authStore.clearUserData()

            expect(authStore.user).toEqual({})
        })
    })

    describe('computed properties', () => {
        describe('isAuthenticated', () => {
            it('should return false when not authenticated', () => {
                expect(authStore.isAuthenticated).toBe(false)
            })

            it('should return true when authenticated', () => {
                authStore.setBffUser(mockBffUserInfo)

                expect(authStore.isAuthenticated).toBe(true)
            })

            it('should reactively update when authenticated changes', () => {
                expect(authStore.isAuthenticated).toBe(false)

                authStore.setBffUser(mockBffUserInfo)
                expect(authStore.isAuthenticated).toBe(true)

                authStore.clearUserData()
                expect(authStore.isAuthenticated).toBe(false)
            })
        })

        describe('isAdmin', () => {
            it('should return false when user has no roles', () => {
                expect(authStore.isAdmin).toBeFalsy()
            })

            it('should return true when user has ADMIN role', () => {
                authStore.setBffUser(mockBffUserInfo)

                expect(authStore.isAdmin).toBe(true)
            })

            it('should return true when user has admin role (lowercase)', () => {
                const userWithLowercaseAdmin: BffUserInfo = {
                    ...mockBffUserInfo,
                    roles: ['USER', 'admin'],
                }

                authStore.setBffUser(userWithLowercaseAdmin)

                expect(authStore.isAdmin).toBe(true)
            })

            it('should return false when user has roles but not admin', () => {
                authStore.setBffUser(mockBffUserInfoNoAdmin)

                expect(authStore.isAdmin).toBe(false)
            })

            it('should reactively update when roles change', () => {
                expect(authStore.isAdmin).toBeFalsy()

                authStore.setBffUser(mockBffUserInfo)
                expect(authStore.isAdmin).toBe(true)

                authStore.setBffUser(mockBffUserInfoNoAdmin)
                expect(authStore.isAdmin).toBe(false)
            })
        })

        describe('permissions', () => {
            it('should use explicit permissions when available', () => {
                authStore.setBffUser(mockBffUserInfoNoAdmin)

                expect(authStore.canManageEvents).toBe(false)
                expect(authStore.canUploadResults).toBe(false)
                expect(authStore.canManageCups).toBe(false)
                expect(authStore.canViewReports).toBe(true)
                expect(authStore.canManageUsers).toBe(false)
                expect(authStore.canAccessAdmin).toBe(false)
            })

            it('should fall back to admin check when permissions are undefined', () => {
                const userWithoutPermissions: BffUserInfo = {
                    ...mockBffUserInfo,
                    permissions: undefined,
                }

                authStore.setBffUser(userWithoutPermissions)

                // Should fall back to isAdmin (true)
                expect(authStore.canManageEvents).toBe(true)
                expect(authStore.canUploadResults).toBe(true)
                expect(authStore.canManageCups).toBe(true)
                expect(authStore.canManageUsers).toBe(true)
                expect(authStore.canAccessAdmin).toBe(true)
            })

            it('should allow viewing reports for all users by default', () => {
                const userWithoutPermissions: BffUserInfo = {
                    ...mockBffUserInfoNoAdmin,
                    permissions: undefined,
                }

                authStore.setBffUser(userWithoutPermissions)

                expect(authStore.canViewReports).toBe(true)
            })
        })
    })

    describe('edge cases', () => {
        it('should handle multiple login/logout cycles', async () => {
            vi.mocked(bffAuthService.initAuth).mockResolvedValue(mockBffUserInfo)
            vi.mocked(bffAuthService.logout).mockResolvedValue(undefined)

            // Cycle 1
            await authStore.initAuth()
            expect(authStore.authenticated).toBe(true)
            await authStore.logout()
            expect(authStore.authenticated).toBe(false)

            // Cycle 2
            vi.mocked(bffAuthService.initAuth).mockResolvedValue(mockBffUserInfo)
            await authStore.initAuth()
            expect(authStore.authenticated).toBe(true)
            await authStore.logout()
            expect(authStore.authenticated).toBe(false)
        })

        it('should handle setBffUser with minimal user info', () => {
            const minimalUserInfo: BffUserInfo = {
                username: 'minimal',
                email: 'minimal@example.com',
                name: 'Minimal User',
                roles: [],
                groups: [],
            }

            authStore.setBffUser(minimalUserInfo)

            expect(authStore.authenticated).toBe(true)
            expect(authStore.user.username).toBe('minimal')
            expect(authStore.user.roles).toEqual([])
            expect(authStore.isAdmin).toBe(false)
        })

        it('should handle setBffUser with empty roles array', () => {
            const userEmptyRoles: BffUserInfo = {
                ...mockBffUserInfo,
                roles: [],
            }

            authStore.setBffUser(userEmptyRoles)

            expect(authStore.isAdmin).toBe(false)
            expect(authStore.user.roles).toEqual([])
        })
    })
})
