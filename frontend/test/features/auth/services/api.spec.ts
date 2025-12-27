import type { InternalAxiosRequestConfig } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import axiosInstance from '@/features/auth/services/api'
import { useAuthStore } from '@/features/auth/store/auth.store'

vi.mock('@/features/auth/store/auth.store', () => ({
    useAuthStore: vi.fn(),
}))

// Mock document.cookie
Object.defineProperty(document, 'cookie', {
    writable: true,
    value: '',
})

// Mock window.location
const mockLocation = {
    href: '',
    pathname: '/test-path',
}
Object.defineProperty(window, 'location', {
    value: mockLocation,
    writable: true,
})

// Mock sessionStorage
const sessionStorageMock = {
    getItem: vi.fn(),
    setItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn(),
}
Object.defineProperty(window, 'sessionStorage', {
    value: sessionStorageMock,
})

describe('api (axios instance) - BFF Pattern', () => {
    let mockAuthStore: any

    beforeEach(() => {
        vi.clearAllMocks()

        mockAuthStore = {
            clearUserData: vi.fn(),
        }

        vi.mocked(useAuthStore).mockReturnValue(mockAuthStore)

        // Reset document.cookie
        document.cookie = ''

        // Reset window.location
        mockLocation.href = ''
        mockLocation.pathname = '/test-path'

        // Reset sessionStorage
        sessionStorageMock.setItem.mockClear()
    })

    describe('axios instance configuration', () => {
        it('should create axios instance with correct baseURL', () => {
            expect(axiosInstance.defaults.baseURL).toBeDefined()
        })

        it('should have default Content-Type header', () => {
            expect(axiosInstance.defaults.headers['Content-Type']).toBe('application/json')
        })

        it('should have withCredentials set to true for BFF cookies', () => {
            expect(axiosInstance.defaults.withCredentials).toBe(true)
        })
    })

    describe('request interceptor - BFF mode', () => {
        it('should NOT add Authorization header (BFF uses cookies)', async () => {
            const config: InternalAxiosRequestConfig = {
                method: 'GET',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers.Authorization).toBeUndefined()
        })

        it('should add CSRF token header for POST requests', async () => {
            // Set CSRF token in cookie
            document.cookie = 'XSRF-TOKEN=test-csrf-token; path=/'

            const config: InternalAxiosRequestConfig = {
                method: 'POST',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers['X-XSRF-TOKEN']).toBe('test-csrf-token')
        })

        it('should add CSRF token header for PUT requests', async () => {
            document.cookie = 'XSRF-TOKEN=csrf-put-token; path=/'

            const config: InternalAxiosRequestConfig = {
                method: 'PUT',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers['X-XSRF-TOKEN']).toBe('csrf-put-token')
        })

        it('should add CSRF token header for DELETE requests', async () => {
            document.cookie = 'XSRF-TOKEN=csrf-delete-token; path=/'

            const config: InternalAxiosRequestConfig = {
                method: 'DELETE',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers['X-XSRF-TOKEN']).toBe('csrf-delete-token')
        })

        it('should add CSRF token header for PATCH requests', async () => {
            document.cookie = 'XSRF-TOKEN=csrf-patch-token; path=/'

            const config: InternalAxiosRequestConfig = {
                method: 'PATCH',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers['X-XSRF-TOKEN']).toBe('csrf-patch-token')
        })

        it('should NOT add CSRF token header for GET requests', async () => {
            document.cookie = 'XSRF-TOKEN=csrf-token; path=/'

            const config: InternalAxiosRequestConfig = {
                method: 'GET',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers['X-XSRF-TOKEN']).toBeUndefined()
        })

        it('should handle case-insensitive HTTP methods', async () => {
            document.cookie = 'XSRF-TOKEN=csrf-token; path=/'

            const config: InternalAxiosRequestConfig = {
                method: 'post', // lowercase
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers['X-XSRF-TOKEN']).toBe('csrf-token')
        })

        it('should not add CSRF header when token is not in cookie', async () => {
            document.cookie = '' // No CSRF token

            const config: InternalAxiosRequestConfig = {
                method: 'POST',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers['X-XSRF-TOKEN']).toBeUndefined()
        })

        it('should preserve existing headers', async () => {
            document.cookie = 'XSRF-TOKEN=csrf-token; path=/'

            const config: InternalAxiosRequestConfig = {
                method: 'POST',
                headers: {
                    'X-Custom-Header': 'custom-value',
                    'Accept': 'application/json',
                } as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers['X-XSRF-TOKEN']).toBe('csrf-token')
            expect(result.headers['X-Custom-Header']).toBe('custom-value')
            expect(result.headers.Accept).toBe('application/json')
        })

        it('should return config object', async () => {
            const config: InternalAxiosRequestConfig = {
                headers: {} as any,
                url: '/api/test',
                method: 'GET',
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result).toBe(config)
            expect(result.url).toBe('/api/test')
            expect(result.method).toBe('GET')
        })

        it('should handle request errors', async () => {
            const error = new Error('Request setup failed')

            const interceptor = axiosInstance.interceptors.request.handlers[0]

            await expect(interceptor.rejected(error)).rejects.toThrow('Request setup failed')
        })
    })

    describe('response interceptor - BFF mode', () => {
        it('should pass through successful responses', async () => {
            const response = {
                status: 200,
                data: { success: true },
            }

            const interceptor = axiosInstance.interceptors.response.handlers[0]
            const result = await interceptor.fulfilled(response)

            expect(result).toBe(response)
        })

        it('should handle 401 Unauthorized errors by redirecting to login', async () => {
            const error = {
                response: {
                    status: 401,
                    data: { message: 'Unauthorized' },
                },
            }

            const interceptor = axiosInstance.interceptors.response.handlers[0]

            await expect(interceptor.rejected(error)).rejects.toEqual(error)

            // Should save redirect path to sessionStorage
            expect(sessionStorageMock.setItem).toHaveBeenCalledWith(
                'bff_post_login_redirect',
                '/test-path',
            )

            // Should clear auth store
            expect(mockAuthStore.clearUserData).toHaveBeenCalled()

            // Should redirect to backend OAuth2 login
            expect(mockLocation.href).toContain('/oauth2/authorization/keycloak')
        })

        it('should handle 403 Forbidden errors', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

            const error = {
                response: {
                    status: 403,
                    data: { message: 'Forbidden' },
                },
            }

            const interceptor = axiosInstance.interceptors.response.handlers[0]

            await expect(interceptor.rejected(error)).rejects.toEqual(error)

            expect(consoleErrorSpy).toHaveBeenCalledWith('Access forbidden:', { message: 'Forbidden' })

            consoleErrorSpy.mockRestore()
        })

        it('should handle errors without response object', async () => {
            const error = new Error('Network error')

            const interceptor = axiosInstance.interceptors.response.handlers[0]

            await expect(interceptor.rejected(error)).rejects.toEqual(error)
        })

        it('should handle errors with different status codes', async () => {
            const error = {
                response: {
                    status: 500,
                    data: { message: 'Internal server error' },
                },
            }

            const interceptor = axiosInstance.interceptors.response.handlers[0]

            await expect(interceptor.rejected(error)).rejects.toEqual(error)
        })
    })

    describe('interceptor registration', () => {
        it('should have request interceptor registered', () => {
            expect(axiosInstance.interceptors.request.handlers.length).toBeGreaterThan(0)
        })

        it('should have response interceptor registered', () => {
            expect(axiosInstance.interceptors.response.handlers.length).toBeGreaterThan(0)
        })

        it('should have both fulfilled and rejected handlers for request', () => {
            const interceptor = axiosInstance.interceptors.request.handlers[0]

            expect(interceptor.fulfilled).toBeDefined()
            expect(typeof interceptor.fulfilled).toBe('function')
            expect(interceptor.rejected).toBeDefined()
            expect(typeof interceptor.rejected).toBe('function')
        })

        it('should have both fulfilled and rejected handlers for response', () => {
            const interceptor = axiosInstance.interceptors.response.handlers[0]

            expect(interceptor.fulfilled).toBeDefined()
            expect(typeof interceptor.fulfilled).toBe('function')
            expect(interceptor.rejected).toBeDefined()
            expect(typeof interceptor.rejected).toBe('function')
        })
    })

    describe('cSRF token parsing', () => {
        it('should extract CSRF token from cookie with multiple cookies', () => {
            document.cookie = 'session=abc123; XSRF-TOKEN=my-csrf-token; other=value'

            const config: InternalAxiosRequestConfig = {
                method: 'POST',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = interceptor.fulfilled(config) as InternalAxiosRequestConfig

            expect(result.headers['X-XSRF-TOKEN']).toBe('my-csrf-token')
        })

        it('should handle CSRF token at the beginning of cookie string', () => {
            document.cookie = 'XSRF-TOKEN=first-token; session=abc123'

            const config: InternalAxiosRequestConfig = {
                method: 'POST',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = interceptor.fulfilled(config) as InternalAxiosRequestConfig

            expect(result.headers['X-XSRF-TOKEN']).toBe('first-token')
        })

        it('should handle CSRF token at the end of cookie string', () => {
            document.cookie = 'session=abc123; other=value; XSRF-TOKEN=last-token'

            const config: InternalAxiosRequestConfig = {
                method: 'POST',
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = interceptor.fulfilled(config) as InternalAxiosRequestConfig

            expect(result.headers['X-XSRF-TOKEN']).toBe('last-token')
        })
    })
})
