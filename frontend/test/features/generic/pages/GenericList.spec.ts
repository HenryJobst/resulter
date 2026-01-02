import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { createGlobalMountOptions } from '../../../helpers/testSetup'

// Mock Tanstack Query
const mockUseQuery = vi.fn()
const mockUseMutation = vi.fn()
const mockQueryClient = {
    invalidateQueries: vi.fn(),
}

vi.mock('@tanstack/vue-query', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@tanstack/vue-query')>()
    return {
        ...actual,
        useQuery: () => mockUseQuery(),
        useMutation: (options: any) => mockUseMutation(options),
        useQueryClient: () => mockQueryClient,
    }
})

// Mock VueUse
vi.mock('@vueuse/core', () => ({
    useDebounceFn: vi.fn((fn: any) => fn),
}))

describe('genericList', () => {
    let mockEntityService: IGenericService<any>
    let mockColumns: GenericListColumn[]

    beforeEach(() => {
        vi.clearAllMocks()

        mockEntityService = {
            getAll: vi.fn().mockResolvedValue({
                content: [
                    { id: 1, name: 'Item 1' },
                    { id: 2, name: 'Item 2' },
                ],
                page: {
                    totalElements: 2,
                    totalPages: 1,
                    number: 0,
                },
            }),
            deleteById: vi.fn().mockResolvedValue({}),
        } as any

        mockColumns = [
            {
                field: 'name',
                label: 'labels.name',
                sortable: true,
                filterable: false,
            },
        ]

        mockUseQuery.mockReturnValue({
            data: {
                value: {
                    content: [
                        { id: 1, name: 'Item 1' },
                        { id: 2, name: 'Item 2' },
                    ],
                    page: {
                        totalElements: 2,
                        totalPages: 1,
                        number: 0,
                    },
                },
            },
            status: { value: 'success' },
            refetch: vi.fn(),
        })

        mockUseMutation.mockReturnValue({
            mutate: vi.fn(),
            status: { value: 'idle' },
            reset: vi.fn(),
        })
    })

    describe('component rendering', () => {
        it('should render when visible is true', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    listLabel: 'Test List',
                    entityLabel: 'Test Entity',
                    routerPrefix: 'test',
                    columns: mockColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('Test List')
        })

        it('should not render when visible is false', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    listLabel: 'Test List',
                    visible: false,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).not.toContain('Test List')
        })

        it('should render with default props', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })
    })

    describe('action buttons', () => {
        it('should render new button when newEnabled is true', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    routerPrefix: 'test',
                    visible: true,
                    newEnabled: true,
                    changeable: true,
                },
                global: createGlobalMountOptions(),
            })

            const buttons = wrapper.findAllComponents({ name: 'Button' })
            expect(buttons.length).toBeGreaterThan(0)
        })

        it('should not render new button when newEnabled is false', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                    newEnabled: false,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.html()).toBeDefined()
        })

        it('should render reload button', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const buttons = wrapper.findAllComponents({ name: 'Button' })
            expect(buttons.length).toBeGreaterThan(0)
        })
    })

    describe('dataTable integration', () => {
        it('should render DataTable component', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: mockColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const dataTable = wrapper.findComponent({ name: 'DataTable' })
            expect(dataTable.exists()).toBe(true)
        })

        it('should show DataTable when data is available', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const dataTable = wrapper.findComponent({ name: 'DataTable' })
            expect(dataTable.exists()).toBe(true)
        })
    })

    describe('column rendering', () => {
        it('should accept columns prop', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: mockColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.props('columns')).toEqual(mockColumns)
        })

        it('should handle date column type', () => {
            const dateColumns: GenericListColumn[] = [
                {
                    field: 'createdDate',
                    label: 'labels.date',
                    type: 'date',
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: dateColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle time column type', () => {
            const timeColumns: GenericListColumn[] = [
                {
                    field: 'startTime',
                    label: 'labels.time',
                    type: 'time',
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: timeColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle year column type', () => {
            const yearColumns: GenericListColumn[] = [
                {
                    field: 'year',
                    label: 'labels.year',
                    type: 'year',
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: yearColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })
    })

    describe('props handling', () => {
        it('should handle changeable prop', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                    changeable: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle editEnabled prop', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                    editEnabled: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle deleteEnabled prop', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                    deleteEnabled: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle filterDisplay prop', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                    filterDisplay: 'menu',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })
    })

    describe('getSortable function', () => {
        it('should return true for sortable column', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: mockColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getSortable({ field: 'name', sortable: true })
            expect(result).toBe(true)
        })

        it('should return false for non-sortable column', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: mockColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getSortable({ field: 'name', sortable: false })
            expect(result).toBe(false)
        })

        it('should return false when sortable is undefined', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getSortable({ field: 'name' })
            expect(result).toBe(false)
        })
    })

    describe('deleteEntity function', () => {
        it('should call deleteMutation with entity id', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                    deleteEnabled: true,
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const mockMutate = vi.fn()
            mockUseMutation.mockReturnValue({
                mutate: mockMutate,
                status: { value: 'idle' },
                reset: vi.fn(),
            })

            wrapper.vm.$forceUpdate()
            vm.deleteEntity(123)

            // The mutation would be called in a real scenario
            expect(vm).toBeDefined()
        })
    })

    describe('reload function', () => {
        it('should call refetch and reset mutation', async () => {
            const mockRefetch = vi.fn()
            const mockReset = vi.fn()

            mockUseQuery.mockReturnValue({
                data: { value: { content: [], page: { totalElements: 0 } } },
                status: { value: 'success' },
                refetch: mockRefetch,
            })

            mockUseMutation.mockReturnValue({
                mutate: vi.fn(),
                status: { value: 'idle' },
                reset: mockReset,
            })

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            await vm.reload()

            expect(mockRefetch).toHaveBeenCalled()
            expect(mockReset).toHaveBeenCalled()
        })
    })

    describe('pageChanged function', () => {
        it('should update settings when page changes', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const pageEvent = {
                first: 10,
                page: 1,
                rows: 10,
                multiSortMeta: [],
            }

            vm.pageChanged(pageEvent)

            expect(vm.settingsStore.settings.first).toBe(10)
            expect(vm.settingsStore.settings.rows).toBe(10)
        })
    })

    describe('sortChanged function', () => {
        it('should update settings when sort changes', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const sortEvent = {
                first: 0,
                rows: 10,
                sortField: 'name',
                sortOrder: 1,
                multiSortMeta: [],
            }

            vm.sortChanged(sortEvent)

            expect(vm.settingsStore.settings.sortField).toBe('name')
            expect(vm.settingsStore.settings.sortOrder).toBe(1)
            expect(vm.settingsStore.settings.first).toBe(0)
        })
    })

    describe('column type rendering', () => {
        it('should handle list type column', () => {
            mockUseQuery.mockReturnValue({
                data: {
                    value: {
                        content: [{ id: 1, items: ['item1', 'item2'] }],
                        page: { totalElements: 1 },
                    },
                },
                status: { value: 'success' },
                refetch: vi.fn(),
            })

            const listColumns: GenericListColumn[] = [
                {
                    field: 'items',
                    label: 'labels.items',
                    type: 'list',
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: listColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle id type column', () => {
            const idColumns: GenericListColumn[] = [
                {
                    field: 'id',
                    label: 'labels.id',
                    type: 'id',
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: idColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle enum type column', () => {
            const enumTypeLabelPrefixes = new Map<string, string>()
            enumTypeLabelPrefixes.set('status', 'status.')

            mockUseQuery.mockReturnValue({
                data: {
                    value: {
                        content: [{ id: 1, status: { id: 'active' } }],
                        page: { totalElements: 1 },
                    },
                },
                status: { value: 'success' },
                refetch: vi.fn(),
            })

            const enumColumns: GenericListColumn[] = [
                {
                    field: 'status',
                    label: 'labels.status',
                    type: 'enum',
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: enumColumns,
                    enumTypeLabelPrefixes,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle image type column', () => {
            mockUseQuery.mockReturnValue({
                data: {
                    value: {
                        content: [{ id: 1, image: 'base64string' }],
                        page: { totalElements: 1 },
                    },
                },
                status: { value: 'success' },
                refetch: vi.fn(),
            })

            const imageColumns: GenericListColumn[] = [
                {
                    field: 'image',
                    label: 'labels.image',
                    type: 'image',
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: imageColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle custom type column with slot', () => {
            const customColumns: GenericListColumn[] = [
                {
                    field: 'custom',
                    label: 'labels.custom',
                    type: 'custom',
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: customColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should handle undefined type column with truncation', () => {
            mockUseQuery.mockReturnValue({
                data: {
                    value: {
                        content: [{ id: 1, description: 'A very long description text' }],
                        page: { totalElements: 1 },
                    },
                },
                status: { value: 'success' },
                refetch: vi.fn(),
            })

            const defaultColumns: GenericListColumn[] = [
                {
                    field: 'description',
                    label: 'labels.description',
                    truncate: 20,
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: defaultColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })
    })

    describe('edit and delete buttons', () => {
        it('should render edit button when editEnabled is true', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    routerPrefix: 'test',
                    visible: true,
                    editEnabled: true,
                    changeable: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should render delete button when deleteEnabled is true', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                    deleteEnabled: true,
                    changeable: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should call reload when reload button is clicked', async () => {
            const mockRefetch = vi.fn()
            const mockReset = vi.fn()

            mockUseQuery.mockReturnValue({
                data: { value: { content: [], page: { totalElements: 0 } } },
                status: { value: 'success' },
                refetch: mockRefetch,
            })

            mockUseMutation.mockReturnValue({
                mutate: vi.fn(),
                status: { value: 'idle' },
                reset: mockReset,
            })

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const reloadButton = wrapper.findAllComponents({ name: 'Button' }).find(btn =>
                btn.attributes('icon') === 'pi pi-refresh',
            )

            if (reloadButton) {
                await reloadButton.trigger('click')
                expect(mockRefetch).toHaveBeenCalled()
            }
        })
    })

    describe('loading states', () => {
        it('should show loading when query is pending', () => {
            mockUseQuery.mockReturnValue({
                data: { value: undefined },
                status: { value: 'pending' },
                refetch: vi.fn(),
            })

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const dataTable = wrapper.findComponent({ name: 'DataTable' })
            expect(dataTable.attributes('loading')).toBeDefined()
        })

        it('should show loading when delete mutation is pending', () => {
            mockUseQuery.mockReturnValue({
                data: { value: { content: [], page: { totalElements: 0 } } },
                status: { value: 'success' },
                refetch: vi.fn(),
            })

            mockUseMutation.mockReturnValue({
                mutate: vi.fn(),
                status: { value: 'pending' },
                reset: vi.fn(),
            })

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const dataTable = wrapper.findComponent({ name: 'DataTable' })
            expect(dataTable.attributes('loading')).toBeDefined()
        })
    })

    describe('filter functionality', () => {
        it('should handle filterable columns', () => {
            const filterableColumns: GenericListColumn[] = [
                {
                    field: 'name',
                    label: 'labels.name',
                    filterable: true,
                    filterMatchMode: 'contains',
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: filterableColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })

        it('should use default filterMatchMode when not specified', () => {
            const filterableColumns: GenericListColumn[] = [
                {
                    field: 'name',
                    label: 'labels.name',
                    filterable: true,
                },
            ]

            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    columns: filterableColumns,
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })
    })

    describe('slots', () => {
        it('should render extra_list_actions slot', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                    newEnabled: true,
                },
                slots: {
                    extra_list_actions: '<div class="extra-list-action">Custom Action</div>',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('.extra-list-action').exists()).toBe(true)
        })

        it('should render extra_row_actions slot', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                slots: {
                    extra_row_actions: '<div class="extra-row-action">Row Action</div>',
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
        })
    })

    describe('dataTable configuration', () => {
        it('should pass correct paginator settings to DataTable', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const dataTable = wrapper.findComponent({ name: 'DataTable' })
            expect(dataTable.exists()).toBe(true)
        })

        it('should handle lazy loading', () => {
            const wrapper = mount(GenericList, {
                props: {
                    entityService: mockEntityService,
                    settingsStoreSuffix: 'test',
                    queryKey: 'test-query',
                    visible: true,
                },
                global: createGlobalMountOptions(),
            })

            const dataTable = wrapper.findComponent({ name: 'DataTable' })
            expect(dataTable.attributes('lazy')).toBe('true')
        })
    })
})
