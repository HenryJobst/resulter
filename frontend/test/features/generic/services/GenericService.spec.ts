import type { GenericEntity } from '@/features/generic/models/GenericEntity'
import type { RestPageResult } from '@/features/generic/models/rest_page_result'
import type { TableSettings } from '@/features/generic/models/table_settings'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { GenericService } from '@/features/generic/services/GenericService'
import axiosInstance from '@/features/keycloak/services/api'

// Mock axios instance
vi.mock('@/features/keycloak/services/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}))

interface TestEntity extends GenericEntity {
    id: number
    name: string
    description?: string
}

// Helper to create default TableSettings with optional overrides
function createTableSettings(overrides: Partial<TableSettings> = {}): TableSettings {
    return {
        first: 0,
        rows: 10,
        page: 0,
        paginator: false,
        paginatorPosition: undefined,
        rowsPerPageOptions: [10, 20, 50],
        sortMode: undefined,
        multiSortMeta: undefined,
        sortField: undefined,
        sortOrder: undefined,
        nullSortOrder: 1,
        defaultSortOrder: 1,
        filters: undefined,
        removableSort: false,
        rowHover: false,
        stateStorage: 'session',
        stateKey: undefined,
        scrollable: false,
        stripedRows: false,
        ...overrides,
    }
}

describe('genericService', () => {
    let genericService: GenericService<TestEntity>
    const mockT = (key: string) => key

    beforeEach(() => {
        vi.clearAllMocks()
        genericService = new GenericService<TestEntity>('/api/test')
    })

    describe('getAll', () => {
        it('should fetch all entities without table settings', async () => {
            const mockData: RestPageResult<TestEntity> = {
                content: [
                    { id: 1, name: 'Test 1' },
                    { id: 2, name: 'Test 2' },
                ],
                totalElements: 2,
                totalPages: 1,
                number: 0,
                size: 10,
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockData })

            const result = await genericService.getAll(mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/api/test', {
                params: expect.any(URLSearchParams),
            })
            expect(result).toEqual(mockData)
        })

        it('should fetch all entities with pagination', async () => {
            const tableSettings = createTableSettings({
                paginator: true,
                page: 2,
                rows: 20,
            })

            const mockData: RestPageResult<TestEntity> = {
                content: [],
                totalElements: 100,
                totalPages: 5,
                number: 2,
                size: 20,
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockData })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.get('page')).toBe('2')
            expect(callParams.get('size')).toBe('20')
        })

        it('should handle single field sorting', async () => {
            const tableSettings = createTableSettings({
                sortField: 'name',
                sortOrder: 1, // ascending
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.get('sort')).toBe('name,asc')
        })

        it('should handle descending sort order', async () => {
            const tableSettings = createTableSettings({
                sortField: 'name',
                sortOrder: -1, // descending
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.get('sort')).toBe('name,desc')
        })

        it('should handle multi-column sorting', async () => {
            const tableSettings = createTableSettings({
                multiSortMeta: [
                    { field: 'name', order: 1 },
                    { field: 'description', order: -1 },
                ],
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            const sortParams = callParams.getAll('sort')
            expect(sortParams).toContain('name,asc')
            expect(sortParams).toContain('description,desc')
        })

        it('should filter out zero-order sorting in multiSortMeta', async () => {
            const tableSettings = createTableSettings({
                multiSortMeta: [
                    { field: 'name', order: 1 },
                    { field: 'description', order: 0 }, // Should be filtered out
                    { field: 'id', order: -1 },
                ],
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            const sortParams = callParams.getAll('sort')
            expect(sortParams).toHaveLength(2)
            expect(sortParams).toContain('name,asc')
            expect(sortParams).toContain('id,desc')
        })

        it('should handle function-based sortField', async () => {
            const sortFunction = (_item: any) => 'customField'
            const tableSettings = createTableSettings({
                sortField: sortFunction,
                sortOrder: 1,
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.get('sort')).toBe('customField,asc')
        })

        it('should not add default sortField if already in multiSortMeta', async () => {
            const tableSettings = createTableSettings({
                sortField: 'name',
                sortOrder: 1,
                multiSortMeta: [
                    { field: 'name', order: -1 }, // Already has 'name' field
                ],
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            const sortParams = callParams.getAll('sort')
            // Should only have one 'name' sort (from multiSortMeta)
            expect(sortParams).toHaveLength(1)
            expect(sortParams[0]).toBe('name,desc')
        })

        it('should handle nullSortOrder parameter', async () => {
            const tableSettings = createTableSettings({
                nullSortOrder: -1,
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.get('nullSortOrder')).toBe('-1')
        })

        it('should handle defaultSortOrder parameter', async () => {
            const tableSettings = createTableSettings({
                defaultSortOrder: -1,
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.get('defaultSortOrder')).toBe('-1')
        })

        it('should not add nullSortOrder if value is 1 (default)', async () => {
            const tableSettings = createTableSettings({
                nullSortOrder: 1,
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.has('nullSortOrder')).toBe(false)
        })

        it('should handle equals filter', async () => {
            const tableSettings = createTableSettings({
                filters: {
                    name: { value: 'Test', matchMode: 'equals' },
                },
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            const filterParam = callParams.get('filter')
            expect(filterParam).toBeDefined()
            expect(filterParam).toContain('name')
        })

        it('should handle contains filter', async () => {
            const tableSettings = createTableSettings({
                filters: {
                    description: { value: 'search', matchMode: 'contains' },
                },
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            const filterParam = callParams.get('filter')
            expect(filterParam).toBeDefined()
            expect(filterParam).toContain('description')
        })

        it('should handle multiple filters', async () => {
            const tableSettings = createTableSettings({
                filters: {
                    name: { value: 'Test', matchMode: 'equals' },
                    description: { value: 'search', matchMode: 'contains' },
                },
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            const filterParam = callParams.get('filter')
            expect(filterParam).toBeDefined()
            expect(filterParam).toContain('name')
            expect(filterParam).toContain('description')
        })

        it('should ignore filters without value', async () => {
            const tableSettings = createTableSettings({
                filters: {
                    name: { value: null, matchMode: 'equals' },
                    description: { value: '', matchMode: 'contains' },
                },
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.has('filter')).toBe(false)
        })

        it('should handle complex table settings with all options', async () => {
            const tableSettings = createTableSettings({
                paginator: true,
                page: 1,
                rows: 25,
                sortField: 'name',
                sortOrder: 1,
                multiSortMeta: [
                    { field: 'id', order: -1 },
                ],
                filters: {
                    name: { value: 'Test', matchMode: 'contains' },
                },
                nullSortOrder: -1,
                defaultSortOrder: 1,
            })

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await genericService.getAll(mockT, tableSettings)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams

            // Verify all parameters are set
            expect(callParams.get('page')).toBe('1')
            expect(callParams.get('size')).toBe('25')
            expect(callParams.get('nullSortOrder')).toBe('-1')
            expect(callParams.has('filter')).toBe(true)

            const sortParams = callParams.getAll('sort')
            expect(sortParams.length).toBeGreaterThan(0)
        })
    })

    describe('getAll with extra params', () => {
        it('should merge extra params from subclass hook', async () => {
            // Create a subclass that overrides getExtraParams
            class CustomService extends GenericService<TestEntity> {
                protected override getExtraParams(): Record<string, string | number | boolean> {
                    return {
                        duplicates: true,
                        customParam: 'test',
                        numericParam: 42,
                    }
                }
            }

            const customService = new CustomService('/api/custom')

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await customService.getAll(mockT)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.get('duplicates')).toBe('true')
            expect(callParams.get('customParam')).toBe('test')
            expect(callParams.get('numericParam')).toBe('42')
        })

        it('should skip undefined/null values in extra params', async () => {
            class CustomService extends GenericService<TestEntity> {
                protected override getExtraParams(): Record<string, string | number | boolean> {
                    return {
                        definedParam: 'value',
                        undefinedParam: undefined as any,
                        nullParam: null as any,
                    }
                }
            }

            const customService = new CustomService('/api/custom')

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })

            await customService.getAll(mockT)

            const callParams = vi.mocked(axiosInstance.get).mock.calls[0][1]?.params as URLSearchParams
            expect(callParams.get('definedParam')).toBe('value')
            expect(callParams.has('undefinedParam')).toBe(false)
            expect(callParams.has('nullParam')).toBe(false)
        })
    })

    describe('getAllUnpaged', () => {
        it('should fetch all entities without pagination', async () => {
            const mockEntities: TestEntity[] = [
                { id: 1, name: 'Test 1' },
                { id: 2, name: 'Test 2' },
                { id: 3, name: 'Test 3' },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockEntities })

            const result = await genericService.getAllUnpaged(mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/api/test/all')
            expect(result).toEqual(mockEntities)
        })

        it('should return null when response is null', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: null })

            const result = await genericService.getAllUnpaged(mockT)

            expect(result).toBeNull()
        })
    })

    describe('getById', () => {
        it('should fetch entity by ID', async () => {
            const mockEntity: TestEntity = { id: 1, name: 'Test Entity' }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockEntity })

            const result = await genericService.getById(1, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/api/test/1')
            expect(result).toEqual(mockEntity)
        })

        it('should handle different entity IDs', async () => {
            const mockEntity: TestEntity = { id: 999, name: 'Entity 999' }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockEntity })

            const result = await genericService.getById(999, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/api/test/999')
            expect(result).toEqual(mockEntity)
        })
    })

    describe('create', () => {
        it('should create new entity', async () => {
            const newEntity = { name: 'New Entity', description: 'Test description' }
            const createdEntity: TestEntity = { id: 1, ...newEntity }

            vi.mocked(axiosInstance.post).mockResolvedValue({ data: createdEntity })

            const result = await genericService.create(newEntity, mockT)

            expect(axiosInstance.post).toHaveBeenCalledWith('/api/test', newEntity)
            expect(result).toEqual(createdEntity)
        })

        it('should handle entity without optional fields', async () => {
            const newEntity = { name: 'Simple Entity' }
            const createdEntity: TestEntity = { id: 2, ...newEntity }

            vi.mocked(axiosInstance.post).mockResolvedValue({ data: createdEntity })

            const result = await genericService.create(newEntity, mockT)

            expect(result).toEqual(createdEntity)
        })
    })

    describe('update', () => {
        it('should update existing entity', async () => {
            const entityToUpdate: TestEntity = { id: 1, name: 'Updated Entity' }
            const updatedEntity: TestEntity = { ...entityToUpdate }

            vi.mocked(axiosInstance.put).mockResolvedValue({ data: updatedEntity })

            const result = await genericService.update(entityToUpdate, mockT)

            expect(axiosInstance.put).toHaveBeenCalledWith('/api/test/1', entityToUpdate)
            expect(result).toEqual(updatedEntity)
        })

        it('should handle entity with all fields', async () => {
            const entityToUpdate: TestEntity = {
                id: 5,
                name: 'Full Entity',
                description: 'Complete description',
            }

            vi.mocked(axiosInstance.put).mockResolvedValue({ data: entityToUpdate })

            const result = await genericService.update(entityToUpdate, mockT)

            expect(axiosInstance.put).toHaveBeenCalledWith('/api/test/5', entityToUpdate)
            expect(result).toEqual(entityToUpdate)
        })
    })

    describe('deleteById', () => {
        it('should delete entity by ID', async () => {
            vi.mocked(axiosInstance.delete).mockResolvedValue({ data: undefined })

            await genericService.deleteById(1, mockT)

            expect(axiosInstance.delete).toHaveBeenCalledWith('/api/test/1')
        })

        it('should handle different entity IDs for deletion', async () => {
            vi.mocked(axiosInstance.delete).mockResolvedValue({ data: undefined })

            await genericService.deleteById(42, mockT)

            expect(axiosInstance.delete).toHaveBeenCalledWith('/api/test/42')
        })
    })

    describe('endpoint handling', () => {
        it('should use correct endpoint for all operations', async () => {
            const customService = new GenericService<TestEntity>('/api/custom-endpoint')

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: { content: [] } })
            vi.mocked(axiosInstance.post).mockResolvedValue({ data: {} })
            vi.mocked(axiosInstance.put).mockResolvedValue({ data: {} })
            vi.mocked(axiosInstance.delete).mockResolvedValue({ data: undefined })

            await customService.getAll(mockT)
            expect(vi.mocked(axiosInstance.get).mock.calls[0][0]).toBe('/api/custom-endpoint')

            await customService.getAllUnpaged(mockT)
            expect(vi.mocked(axiosInstance.get).mock.calls[1][0]).toBe('/api/custom-endpoint/all')

            await customService.getById(1, mockT)
            expect(vi.mocked(axiosInstance.get).mock.calls[2][0]).toBe('/api/custom-endpoint/1')

            await customService.create({ name: 'Test' }, mockT)
            expect(vi.mocked(axiosInstance.post).mock.calls[0][0]).toBe('/api/custom-endpoint')

            await customService.update({ id: 1, name: 'Test' }, mockT)
            expect(vi.mocked(axiosInstance.put).mock.calls[0][0]).toBe('/api/custom-endpoint/1')

            await customService.deleteById(1, mockT)
            expect(vi.mocked(axiosInstance.delete).mock.calls[0][0]).toBe('/api/custom-endpoint/1')
        })
    })
})
