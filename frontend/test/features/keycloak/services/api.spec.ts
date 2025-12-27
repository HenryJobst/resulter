import type { InternalAxiosRequestConfig } from 'axios'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import axiosInstance from '@/features/keycloak/services/api'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

vi.mock('@/features/keycloak/store/auth.store', () => ({
    useAuthStore: vi.fn(),
}))

describe('api (axios instance)', () => {
    let mockAuthStore: any

    beforeEach(() => {
        vi.clearAllMocks()
        setActivePinia(createPinia())

        mockAuthStore = {
            user: {
                token: null,
            },
        }

        vi.mocked(useAuthStore).mockReturnValue(mockAuthStore)
    })

    describe('axios instance configuration', () => {
        it('should create axios instance with correct baseURL', () => {
            expect(axiosInstance.defaults.baseURL).toBeDefined()
        })

        it('should have default Content-Type header', () => {
            expect(axiosInstance.defaults.headers['Content-Type']).toBe('application/json')
        })
    })

    describe('request interceptor', () => {
        it('should add Authorization header when token is present', async () => {
            mockAuthStore.user.token = 'test-token-123'

            const config: InternalAxiosRequestConfig = {
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers.Authorization).toBe('Bearer test-token-123')
        })

        it('should not add Authorization header when token is null', async () => {
            mockAuthStore.user.token = null

            const config: InternalAxiosRequestConfig = {
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers.Authorization).toBeUndefined()
        })

        it('should not add Authorization header when token is empty string', async () => {
            mockAuthStore.user.token = ''

            const config: InternalAxiosRequestConfig = {
                headers: {} as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers.Authorization).toBeUndefined()
        })

        it('should return config object', async () => {
            mockAuthStore.user.token = 'test-token'

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

        it('should preserve existing headers when adding Authorization', async () => {
            mockAuthStore.user.token = 'test-token'

            const config: InternalAxiosRequestConfig = {
                headers: {
                    'X-Custom-Header': 'custom-value',
                    'Accept': 'application/json',
                } as any,
            } as InternalAxiosRequestConfig

            const interceptor = axiosInstance.interceptors.request.handlers[0]
            const result = await interceptor.fulfilled(config)

            expect(result.headers.Authorization).toBe('Bearer test-token')
            expect(result.headers['X-Custom-Header']).toBe('custom-value')
            expect(result.headers.Accept).toBe('application/json')
        })

        it('should handle different token formats', async () => {
            const tokens = [
                'short',
                'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U',
                'very-long-token-with-many-characters-1234567890-abcdefghijklmnopqrstuvwxyz',
            ]

            for (const token of tokens) {
                mockAuthStore.user.token = token

                const config: InternalAxiosRequestConfig = {
                    headers: {} as any,
                } as InternalAxiosRequestConfig

                const interceptor = axiosInstance.interceptors.request.handlers[0]
                const result = await interceptor.fulfilled(config)

                expect(result.headers.Authorization).toBe(`Bearer ${token}`)
            }
        })
    })

    describe('interceptor registration', () => {
        it('should have request interceptor registered', () => {
            expect(axiosInstance.interceptors.request.handlers.length).toBeGreaterThan(0)
        })

        it('should have both fulfilled and rejected handlers', () => {
            const interceptor = axiosInstance.interceptors.request.handlers[0]

            expect(interceptor.fulfilled).toBeDefined()
            expect(typeof interceptor.fulfilled).toBe('function')
            expect(interceptor.rejected).toBeDefined()
            expect(typeof interceptor.rejected).toBe('function')
        })
    })
})
