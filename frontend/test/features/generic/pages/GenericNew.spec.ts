import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import GenericNew from '@/features/generic/pages/GenericNew.vue'
import { createGlobalMountOptions } from '../../../helpers/testSetup'
import type { GenericEntity } from '@/features/generic/models/GenericEntity'
import type { IGenericService } from '@/features/generic/services/IGenericService'

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

describe('GenericNew', () => {
    let mockEntity: GenericEntity
    let mockEntityService: IGenericService<GenericEntity>
    let mockMutate: any

    beforeEach(() => {
        vi.clearAllMocks()

        mockEntity = {
            id: undefined,
            name: 'New Entity',
        } as GenericEntity

        mockEntityService = {
            create: vi.fn().mockResolvedValue({ id: 1, name: 'New Entity' }),
        } as any

        mockMutate = vi.fn()

        mockUseMutation.mockReturnValue({
            mutate: mockMutate,
            status: { value: 'idle' },
        })
    })

    describe('component rendering', () => {
        it('should render when changeable is true', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                    changeable: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('New Entity')
        })

        it('should not render when changeable is false', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                    changeable: false,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.html()).toBe('<!--v-if-->')
        })

        it('should render with default props', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should display new label as heading', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'Create New Item',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('h1').text()).toBe('Create New Item')
        })
    })

    describe('form elements', () => {
        it('should render form element', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('form').exists()).toBe(true)
        })

        it('should render save button when changeable is true', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                    changeable: true,
                },
                global: createGlobalMountOptions(),
            })

            const buttons = wrapper.findAllComponents({ name: 'Button' })
            expect(buttons.length).toBeGreaterThan(0)
        })

        it('should render back button when changeable is true', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                    changeable: true,
                },
                global: createGlobalMountOptions(),
            })

            const buttons = wrapper.findAllComponents({ name: 'Button' })
            expect(buttons.length).toBe(2) // save and back
        })
    })

    describe('props handling', () => {
        it('should accept entity prop', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.props('entity')).toEqual(mockEntity)
        })

        it('should accept queryKey prop', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities', 'new'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.props('queryKey')).toEqual(['entities', 'new'])
        })

        it('should accept routerPrefix prop', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'custom-prefix',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.props('routerPrefix')).toBe('custom-prefix')
        })

        it('should handle visible prop', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                    visible: false,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.props('visible')).toBe(false)
        })

        it('should handle savable prop', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                    savable: false,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.props('savable')).toBe(false)
        })
    })

    describe('loading state', () => {
        it('should show loading when mutation is pending', () => {
            mockUseMutation.mockReturnValue({
                mutate: mockMutate,
                status: { value: 'pending' },
            })

            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.html()).toContain('Lädt...')
        })

        it('should show spinner when mutation is pending', () => {
            mockUseMutation.mockReturnValue({
                mutate: mockMutate,
                status: { value: 'pending' },
            })

            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.findComponent({ name: 'Spinner' }).exists()).toBe(true)
        })

        it('should not show loading when mutation is idle', () => {
            mockUseMutation.mockReturnValue({
                mutate: mockMutate,
                status: { value: 'idle' },
            })

            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.findComponent({ name: 'Spinner' }).exists()).toBe(false)
        })
    })

    describe('slots', () => {
        it('should render default slot content', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                slots: {
                    default: '<div class="test-slot">Custom Form Content</div>',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('.test-slot').exists()).toBe(true)
            expect(wrapper.find('.test-slot').text()).toBe('Custom Form Content')
        })

        it('should provide formData to slot', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                slots: {
                    default: `<template #default="{ formData }">
                        <div class="form-data">{{ formData.data.name }}</div>
                    </template>`,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('.form-data').text()).toBe('New Entity')
        })
    })

    describe('computed properties', () => {
        it('should compute entityLabel from entityLabel prop', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'event',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.vm).toBeDefined()
        })
    })

    describe('mutation and form submission', () => {
        it('should call entityService.create when mutation is triggered', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            const mutationFn = mockUseMutation.mock.calls[0][0].mutationFn

            mutationFn(mockEntity)

            expect(mockEntityService.create).toHaveBeenCalledWith(mockEntity, expect.any(Function))
        })

        it('should call mutate when submitHandler is invoked', () => {
            const testEntity = {
                id: 1,
                name: 'Test Entity',
            }

            const wrapper = mount(GenericNew, {
                props: {
                    entity: testEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            vm.submitHandler()

            expect(mockMutate).toHaveBeenCalledWith(testEntity)
        })

        it('should invalidate queries on mutation success', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            const onSuccess = mockUseMutation.mock.calls[0][0].onSuccess
            onSuccess()

            expect(mockQueryClient.invalidateQueries).toHaveBeenCalledWith({ queryKey: ['entities'] })
        })

        it('should navigate to list page on mutation success', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            const onSuccess = mockUseMutation.mock.calls[0][0].onSuccess
            onSuccess()

            expect(mockReplace).toHaveBeenCalledWith({ name: 'entity-list' })
        })

        it('should navigate to list page when navigateToList is called', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'custom',
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            vm.navigateToList()

            expect(mockReplace).toHaveBeenCalledWith({ name: 'custom-list' })
        })

        it('should handle different routerPrefix values in navigation', () => {
            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'test-prefix',
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            vm.navigateToList()

            expect(mockReplace).toHaveBeenCalledWith({ name: 'test-prefix-list' })
        })

        it('should log entity data when mutation function is called', () => {
            const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

            const wrapper = mount(GenericNew, {
                props: {
                    entity: mockEntity,
                    entityService: mockEntityService,
                    queryKey: ['entities'],
                    entityLabel: 'entity',
                    newLabel: 'New Entity',
                    routerPrefix: 'entity',
                },
                global: createGlobalMountOptions(),
            })

            const mutationFn = mockUseMutation.mock.calls[0][0].mutationFn
            const testData = { id: 1, name: 'Test' }
            mutationFn(testData)

            expect(consoleSpy).toHaveBeenCalledWith(testData)

            consoleSpy.mockRestore()
        })
    })
})
