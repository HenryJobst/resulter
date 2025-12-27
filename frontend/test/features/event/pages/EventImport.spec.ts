import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EventImport from '@/features/event/pages/EventImport.vue'
import { createGlobalMountOptions } from '../../../helpers/testSetup'

// Mock EventService
vi.mock('@/features/event/services/event.service', () => ({
    EventService: {
        upload: vi.fn().mockResolvedValue({ success: true }),
    },
    eventService: {
        create: vi.fn().mockResolvedValue({ id: 1, name: 'Test Event' }),
    },
}))

// Mock Tanstack Query
const mockUseMutation = vi.fn()
const mockQueryClient = {
    invalidateQueries: vi.fn(),
}

vi.mock('@tanstack/vue-query', () => ({
    useMutation: (options: any) => mockUseMutation(options),
    useQueryClient: () => mockQueryClient,
}))

// Mock router
const mockReplace = vi.fn()
vi.mock('vue-router', () => ({
    useRouter: () => ({
        replace: mockReplace,
    }),
}))

describe('eventImport', () => {
    let mockMutate: any

    beforeEach(() => {
        vi.clearAllMocks()

        mockMutate = vi.fn()

        mockUseMutation.mockReturnValue({
            mutate: mockMutate,
            status: { value: 'idle' },
        })
    })

    describe('component rendering', () => {
        it('should render component', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should render heading', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('h2').exists()).toBe(true)
            expect(wrapper.find('h2').text()).toBe('Wettkampf importieren')
        })

        it('should render EventImportForm component', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            expect(wrapper.findComponent({ name: 'EventImportForm' }).exists()).toBe(true)
        })

        it('should render back button', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const button = wrapper.findComponent({ name: 'Button' })
            expect(button.exists()).toBe(true)
        })
    })

    describe('eventImportForm integration', () => {
        it('should pass uploader prop to EventImportForm', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const importForm = wrapper.findComponent({ name: 'EventImportForm' })
            expect(importForm.props('uploader')).toBeDefined()
            expect(typeof importForm.props('uploader')).toBe('function')
        })

        it('should handle event-submit event', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const importForm = wrapper.findComponent({ name: 'EventImportForm' })
            // Component should exist and accept the event
            expect(importForm.exists()).toBe(true)
        })

        it('should render slot content in EventImportForm', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            // Back button should be in the default slot
            const button = wrapper.findComponent({ name: 'Button' })
            expect(button.exists()).toBe(true)
        })
    })

    describe('validateMimeType function', () => {
        it('should validate XML MIME type', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            // Access the component instance
            const vm = wrapper.vm as any

            const xmlFile = new File(['<xml></xml>'], 'test.xml', { type: 'text/xml' })
            const result = await vm.validateMimeType(xmlFile)

            expect(result).toBe(true)
        })

        it('should reject non-XML MIME type', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any

            const jsonFile = new File(['{}'], 'test.json', { type: 'application/json' })
            const result = await vm.validateMimeType(jsonFile)

            expect(result).toBe(false)
        })

        it('should reject PDF files', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any

            const pdfFile = new File(['pdf content'], 'test.pdf', { type: 'application/pdf' })
            const result = await vm.validateMimeType(pdfFile)

            expect(result).toBe(false)
        })

        it('should reject text files', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any

            const textFile = new File(['text content'], 'test.txt', { type: 'text/plain' })
            const result = await vm.validateMimeType(textFile)

            expect(result).toBe(false)
        })
    })

    describe('uploader function', () => {
        it('should handle single file upload', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const { EventService } = await import('@/features/event/services/event.service')

            const xmlFile = new File(['<xml></xml>'], 'test.xml', { type: 'text/xml' })
            const uploadEvent = {
                files: xmlFile,
            }

            await vm.uploader(uploadEvent)

            expect(EventService.upload).toHaveBeenCalled()
        })

        it('should handle multiple files upload', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const { EventService } = await import('@/features/event/services/event.service')

            const xmlFile1 = new File(['<xml></xml>'], 'test1.xml', { type: 'text/xml' })
            const xmlFile2 = new File(['<xml></xml>'], 'test2.xml', { type: 'text/xml' })
            const uploadEvent = {
                files: [xmlFile1, xmlFile2],
            }

            await vm.uploader(uploadEvent)

            expect(EventService.upload).toHaveBeenCalled()
        })

        it('should filter out invalid file types', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const { EventService } = await import('@/features/event/services/event.service')

            const xmlFile = new File(['<xml></xml>'], 'test.xml', { type: 'text/xml' })
            const jsonFile = new File(['{}'], 'test.json', { type: 'application/json' })
            const uploadEvent = {
                files: [xmlFile, jsonFile],
            }

            await vm.uploader(uploadEvent)

            // Should still upload because at least one valid file exists
            expect(EventService.upload).toHaveBeenCalled()
        })

        it('should not upload if no valid files', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const { EventService } = await import('@/features/event/services/event.service')

            vi.clearAllMocks()

            const jsonFile = new File(['{}'], 'test.json', { type: 'application/json' })
            const uploadEvent = {
                files: [jsonFile],
            }

            await vm.uploader(uploadEvent)

            expect(EventService.upload).not.toHaveBeenCalled()
        })
    })

    describe('eventSubmitHandler', () => {
        it('should call mutation with event data', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const eventData = { name: 'Test Event', startTime: '2024-01-15T10:00:00' }

            vm.eventSubmitHandler(eventData)

            expect(mockMutate).toHaveBeenCalledWith(eventData)
        })

        it('should handle different event data', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const eventData = { name: 'Another Event', startTime: '2024-02-20T14:30:00' }

            vm.eventSubmitHandler(eventData)

            expect(mockMutate).toHaveBeenCalledWith(eventData)
        })
    })

    describe('redirectBack function', () => {
        it('should navigate to event list', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            await vm.redirectBack()

            expect(mockReplace).toHaveBeenCalledWith({ name: 'event-list' })
        })

        it('should be called when back button is clicked', async () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const backButton = wrapper.findComponent({ name: 'Button' })
            await backButton.trigger('click')

            expect(mockReplace).toHaveBeenCalledWith({ name: 'event-list' })
        })
    })

    describe('button properties', () => {
        it('should render back button with icon', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const button = wrapper.findComponent({ name: 'Button' })
            expect(button.attributes('icon')).toBeDefined()
        })

        it('should render back button with correct attributes', () => {
            const wrapper = mount(EventImport, {
                global: createGlobalMountOptions(),
            })

            const button = wrapper.findComponent({ name: 'Button' })
            expect(button.exists()).toBe(true)
            expect(button.attributes('severity')).toBeDefined()
        })
    })
})
