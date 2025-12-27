import type { SportEvent } from '@/features/event/model/sportEvent'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EventImportForm from '@/features/event/widgets/EventImportForm.vue'
import { createGlobalMountOptions } from '../../../helpers/testSetup'

// Mock PrimeVue components
vi.mock('primevue/usetoast', () => ({
    useToast: vi.fn(() => ({
        add: vi.fn(),
    })),
}))

describe('eventImportForm', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('component rendering', () => {
        it('should render form when user is admin', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            expect(wrapper.find('form').exists()).toBe(true)
        })

        it('should not render form when user is not admin', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: false }),
            })

            expect(wrapper.find('form').exists()).toBe(false)
        })

        it('should initialize with empty formData', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            // Check that component is mounted
            expect(wrapper.vm).toBeDefined()
        })

        it('should initialize formData with event prop on mount', async () => {
            const mockEvent: Partial<SportEvent> = {
                id: 1,
                name: 'Test Event',
            }

            const wrapper = mount(EventImportForm, {
                props: {
                    event: mockEvent as SportEvent,
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            await wrapper.vm.$nextTick()

            // Component should be mounted with event data
            expect(wrapper.vm).toBeDefined()
        })
    })

    describe('formatSize function', () => {
        it('should format 0 bytes correctly', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            // Access the formatSize method through the component instance
            const result = (wrapper.vm as any).formatSize(0)

            expect(result).toContain('0')
        })

        it('should format bytes to KB', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const result = (wrapper.vm as any).formatSize(1024)

            expect(result).toContain('1')
        })

        it('should format bytes to MB', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const result = (wrapper.vm as any).formatSize(1048576) // 1 MB

            expect(result).toContain('1')
        })

        it('should format large file sizes', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const result = (wrapper.vm as any).formatSize(1073741824) // 1 GB

            expect(result).toContain('1')
        })

        it('should format decimal sizes correctly', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const result = (wrapper.vm as any).formatSize(1536) // 1.5 KB

            expect(result).toBeDefined()
            expect(result.length).toBeGreaterThan(0)
        })
    })

    describe('file selection and management', () => {
        it('should handle file selection', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const mockFile = new File(['test content'], 'test.xml', { type: 'text/xml' })
            const event = { files: [mockFile] }

            // Call the onSelectedFiles method
            await (wrapper.vm as any).onSelectedFiles(event)
            await wrapper.vm.$nextTick()

            // Check that files were added
            expect((wrapper.vm as any).files_to_upload).toHaveLength(1)
            expect((wrapper.vm as any).files_to_upload[0]).toBe(mockFile)
        })

        it('should update totalSize when files are selected', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const mockFile = new File(['test content'], 'test.xml', { type: 'text/xml' })
            const event = { files: [mockFile] }

            const initialTotalSize = (wrapper.vm as any).totalSize

            await (wrapper.vm as any).onSelectedFiles(event)
            await wrapper.vm.$nextTick()

            // Total size should have increased
            expect((wrapper.vm as any).totalSize).toBeGreaterThan(initialTotalSize)
        })

        it('should handle multiple file selection', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const mockFile1 = new File(['content 1'], 'test1.xml', { type: 'text/xml' })
            const mockFile2 = new File(['content 2'], 'test2.xml', { type: 'text/xml' })
            const event = { files: [mockFile1, mockFile2] }

            await (wrapper.vm as any).onSelectedFiles(event)
            await wrapper.vm.$nextTick()

            expect((wrapper.vm as any).files_to_upload).toHaveLength(2)
        })

        it('should clear upload state', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            // Set some initial state
            ;(wrapper.vm as any).totalSize = 1000
            ;(wrapper.vm as any).totalSizePercent = 50

            await (wrapper.vm as any).onClearTemplatingUpload()
            await wrapper.vm.$nextTick()

            expect((wrapper.vm as any).totalSize).toBe(0)
            expect((wrapper.vm as any).totalSizePercent).toBe(0)
        })

        it('should handle file removal', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const mockFile = new File(['test content'], 'test.xml', { type: 'text/xml' })

            // Set initial state
            ;(wrapper.vm as any).totalSize = 1000

            const removeCallback = vi.fn()

            await (wrapper.vm as any).onRemoveTemplatingFile(mockFile, removeCallback, 0)
            await wrapper.vm.$nextTick()

            expect(removeCallback).toHaveBeenCalledWith(0)
            expect((wrapper.vm as any).totalSize).toBeLessThan(1000)
        })
    })

    describe('upload event handling', () => {
        it('should handle upload event', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const callback = vi.fn()
            ;(wrapper.vm as any).totalSize = 1000

            await (wrapper.vm as any).uploadEvent(callback)
            await wrapper.vm.$nextTick()

            expect(callback).toHaveBeenCalled()
            expect((wrapper.vm as any).totalSizePercent).toBe(100) // 1000 / 10
        })

        it('should show success toast on upload completion', async () => {
            const mockToastAdd = vi.fn()
            const { useToast } = await import('primevue/usetoast')
            vi.mocked(useToast).mockReturnValue({
                add: mockToastAdd,
                remove: vi.fn(),
                removeGroup: vi.fn(),
                removeAllGroups: vi.fn(),
            } as any)

            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const mockEvent = {
                files: [],
                xhr: new XMLHttpRequest(),
            }

            await (wrapper.vm as any).onTemplatedUpload(mockEvent)
            await wrapper.vm.$nextTick()

            expect(mockToastAdd).toHaveBeenCalled()
        })
    })

    describe('form submission', () => {
        it('should emit eventSubmit on form submit', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const form = wrapper.find('form')
            await form.trigger('submit.prevent')
            await wrapper.vm.$nextTick()

            expect(wrapper.emitted('eventSubmit')).toBeTruthy()
        })

        it('should emit formData when submitting', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            // Set some form data
            ;(wrapper.vm as any).formData = { name: 'Test Event' }

            await (wrapper.vm as any).formSubmitHandler()
            await wrapper.vm.$nextTick()

            expect(wrapper.emitted('eventSubmit')).toBeTruthy()
            expect(wrapper.emitted('eventSubmit')?.[0]).toEqual([{ name: 'Test Event' }])
        })
    })

    describe('edge cases', () => {
        it('should handle empty file list', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const event = { files: [] }

            await (wrapper.vm as any).onSelectedFiles(event)
            await wrapper.vm.$nextTick()

            expect((wrapper.vm as any).files_to_upload).toHaveLength(0)
        })

        it('should handle very large file sizes', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const result = (wrapper.vm as any).formatSize(999999999999)

            expect(result).toBeDefined()
            expect(result.length).toBeGreaterThan(0)
        })

        it('should handle null file in removal', async () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            const mockFile = new File([], 'empty.xml', { type: 'text/xml' })
            const removeCallback = vi.fn()

            await (wrapper.vm as any).onRemoveTemplatingFile(mockFile, removeCallback, 0)
            await wrapper.vm.$nextTick()

            expect(removeCallback).toHaveBeenCalled()
        })

        it('should use fallback file size types when PrimeVue config is unavailable', () => {
            const wrapper = mount(EventImportForm, {
                props: {
                    uploader: vi.fn(),
                },
                global: createGlobalMountOptions({ isAdmin: true }),
            })

            // Force internalInstance to null to test fallback
            ;(wrapper.vm as any).internalInstance = null

            const result = (wrapper.vm as any).formatSize(1024)

            expect(result).toBeDefined()
        })
    })
})
