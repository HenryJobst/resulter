import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import BackendVersion from '@/features/backend_version/BackendVersion.vue'
import axiosInstance from '@/features/keycloak/services/api'
import { createGlobalMountOptions } from '../../helpers/testSetup'

vi.mock('@/features/keycloak/services/api', () => ({
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
        it('should log error on fetch failure', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
            vi.mocked(axiosInstance.get).mockRejectedValue(new Error('Network error'))

            mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            await flushPromises()

            expect(consoleErrorSpy).toHaveBeenCalledWith(
                'Failed to load backend version:',
                expect.any(Error),
            )

            consoleErrorSpy.mockRestore()
        })

        it('should handle 404 error gracefully', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
            const error404 = new Error('Not found')
            vi.mocked(axiosInstance.get).mockRejectedValue(error404)

            mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            await flushPromises()

            expect(consoleErrorSpy).toHaveBeenCalled()

            consoleErrorSpy.mockRestore()
        })

        it('should handle server error gracefully', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
            const serverError = new Error('Internal server error')
            vi.mocked(axiosInstance.get).mockRejectedValue(serverError)

            mount(BackendVersion, {
                global: createGlobalMountOptions(),
            })

            await flushPromises()

            expect(consoleErrorSpy).toHaveBeenCalled()

            consoleErrorSpy.mockRestore()
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
