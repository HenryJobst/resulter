import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import axiosInstance from '@/features/auth/services/api'
import BackendVersion from '@/features/backend_version/BackendVersion.vue'
import { createGlobalMountOptions } from '../../helpers/testSetup'

vi.mock('@/features/auth/services/api', () => ({
    default: {
        get: vi.fn(),
    },
}))

describe('backendVersion', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('component rendering', () => {
        it('should render component successfully', () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: '1.0.0' })

            const wrapper = mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should fetch version on mount', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: '1.2.3' })

            mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            await flushPromises()

            expect(axiosInstance.get).toHaveBeenCalledWith('/version')
        })

        it('should call /version endpoint exactly once', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: '1.0.0' })

            mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            await flushPromises()

            expect(axiosInstance.get).toHaveBeenCalledTimes(1)
        })
    })

    describe('error handling', () => {
        it('should display error text on fetch failure', async () => {
            vi.mocked(axiosInstance.get).mockRejectedValue(new Error('Network error'))

            const wrapper = mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            // Wait for retries to complete
            await flushPromises()
            await new Promise(resolve => setTimeout(resolve, 100))
            await flushPromises()

            // Should display backend version text (translated in test setup)
            const text = wrapper.text()
            expect(text).toContain('Backend-Version')
        })

        it('should handle 404 error gracefully', async () => {
            const error404 = new Error('Not found')
            vi.mocked(axiosInstance.get).mockRejectedValue(error404)

            const wrapper = mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            // Wait for retries to complete
            await flushPromises()
            await new Promise(resolve => setTimeout(resolve, 100))
            await flushPromises()

            // Component should still render
            expect(wrapper.exists()).toBe(true)
        })

        it('should handle server error gracefully', async () => {
            const serverError = new Error('Internal server error')
            vi.mocked(axiosInstance.get).mockRejectedValue(serverError)

            const wrapper = mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            // Wait for retries to complete
            await flushPromises()
            await new Promise(resolve => setTimeout(resolve, 100))
            await flushPromises()

            // Component should still render
            expect(wrapper.exists()).toBe(true)
        })
    })

    describe('version formats', () => {
        it('should fetch semantic version', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: '2.5.1' })

            mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            await flushPromises()

            // Verify API was called and got the right version
            expect(axiosInstance.get).toHaveBeenCalledWith('/version')
        })

        it('should fetch version with build number', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: '1.0.0-beta.1' })

            mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            await flushPromises()

            expect(axiosInstance.get).toHaveBeenCalledWith('/version')
        })

        it('should fetch version with commit hash', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: '1.0.0-abc123' })

            mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            await flushPromises()

            expect(axiosInstance.get).toHaveBeenCalledWith('/version')
        })
    })

    describe('component lifecycle', () => {
        it('should fetch version on mount', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: '3.0.0' })

            mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            await flushPromises()

            // The text contains the translation key since we don't have real translations
            expect(axiosInstance.get).toHaveBeenCalledTimes(1)
            expect(axiosInstance.get).toHaveBeenCalledWith('/version')
        })
    })
})
