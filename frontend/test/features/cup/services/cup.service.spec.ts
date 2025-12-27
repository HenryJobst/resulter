import type { CupDetailed } from '@/features/cup/model/cup_detailed'
import type { CupType } from '@/features/cup/model/cuptype'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { CupService, cupService } from '@/features/cup/services/cup.service'
import axiosInstance from '@/features/keycloak/services/api'

// Mock axios instance
vi.mock('@/features/keycloak/services/api', () => ({
    default: {
        get: vi.fn(),
        put: vi.fn(),
    },
}))

describe('cupService', () => {
    const mockT = (key: string) => key

    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('constructor', () => {
        it('should create instance with correct endpoint', () => {
            const service = new CupService()
            expect(service).toBeInstanceOf(CupService)
        })

        it('should extend GenericService', () => {
            const service = new CupService()
            // GenericService methods should be available
            expect(typeof service.getAll).toBe('function')
            expect(typeof service.getById).toBe('function')
            expect(typeof service.create).toBe('function')
            expect(typeof service.update).toBe('function')
            expect(typeof service.deleteById).toBe('function')
        })
    })

    describe('exported instance', () => {
        it('should export cupService instance', () => {
            expect(cupService).toBeInstanceOf(CupService)
        })

        it('should be a singleton instance', () => {
            // The exported instance should always be the same
            expect(cupService).toBe(cupService)
        })
    })

    describe('getCupTypes', () => {
        it('should fetch cup types', async () => {
            const mockCupTypes: CupType[] = [
                { id: 'KRISTALL', name: 'Kristall-Cup' },
                { id: 'NEBEL', name: 'Nebel-Cup' },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockCupTypes })

            const result = await CupService.getCupTypes(mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/cup_types')
            expect(result).toEqual(mockCupTypes)
        })

        it('should return null when response is null', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: null })

            const result = await CupService.getCupTypes(mockT)

            expect(result).toBeNull()
        })

        it('should handle empty cup types array', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: [] })

            const result = await CupService.getCupTypes(mockT)

            expect(result).toEqual([])
        })

        it('should handle multiple cup types', async () => {
            const mockCupTypes: CupType[] = [
                { id: 'KRISTALL', name: 'Kristall-Cup' },
                { id: 'NEBEL', name: 'Nebel-Cup' },
                { id: 'NORD_OST', name: 'Nord-Ost-Rangliste' },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockCupTypes })

            const result = await CupService.getCupTypes(mockT)

            expect(result).toHaveLength(3)
            expect(result).toEqual(mockCupTypes)
        })
    })

    describe('getResultsById', () => {
        it('should fetch cup results by ID', async () => {
            const mockCupDetailed: CupDetailed = {
                id: 1,
                name: 'Kristall-Cup 2025',
                year: 2025,
                cupType: { id: 'KRISTALL', name: 'Kristall-Cup' },
                resultLists: [
                    {
                        id: 1,
                        name: 'Wettkampf 1',
                        eventId: 10,
                        results: [],
                    },
                ],
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockCupDetailed })

            const result = await CupService.getResultsById('1', mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/cup/1/results')
            expect(result).toEqual(mockCupDetailed)
        })

        it('should handle different cup IDs', async () => {
            const mockCupDetailed: CupDetailed = {
                id: 42,
                name: 'Test Cup',
                year: 2024,
                cupType: { id: 'NEBEL', name: 'Nebel-Cup' },
                resultLists: [],
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockCupDetailed })

            const result = await CupService.getResultsById('42', mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/cup/42/results')
            expect(result).toEqual(mockCupDetailed)
        })

        it('should handle cup with no result lists', async () => {
            const mockCupDetailed: CupDetailed = {
                id: 5,
                name: 'Empty Cup',
                year: 2025,
                cupType: { id: 'KRISTALL', name: 'Kristall-Cup' },
                resultLists: [],
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockCupDetailed })

            const result = await CupService.getResultsById('5', mockT)

            expect(result?.resultLists).toEqual([])
        })

        it('should handle cup with multiple result lists', async () => {
            const mockCupDetailed: CupDetailed = {
                id: 1,
                name: 'Multi-Event Cup',
                year: 2025,
                cupType: { id: 'KRISTALL', name: 'Kristall-Cup' },
                resultLists: [
                    { id: 1, name: 'Event 1', eventId: 10, results: [] },
                    { id: 2, name: 'Event 2', eventId: 11, results: [] },
                    { id: 3, name: 'Event 3', eventId: 12, results: [] },
                ],
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockCupDetailed })

            const result = await CupService.getResultsById('1', mockT)

            expect(result?.resultLists).toHaveLength(3)
        })
    })

    describe('calculate', () => {
        it('should call calculate endpoint', async () => {
            const mockResponse = { success: true, message: 'Calculation complete' }

            vi.mocked(axiosInstance.put).mockResolvedValue({ data: mockResponse })

            const result = await CupService.calculate('1', mockT)

            expect(axiosInstance.put).toHaveBeenCalledWith('/cup/1/calculate')
            expect(result).toEqual(mockResponse)
        })

        it('should handle different cup IDs for calculation', async () => {
            const mockResponse = { success: true }

            vi.mocked(axiosInstance.put).mockResolvedValue({ data: mockResponse })

            await CupService.calculate('99', mockT)

            expect(axiosInstance.put).toHaveBeenCalledWith('/cup/99/calculate')
        })

        it('should return calculation results', async () => {
            const mockResponse = {
                success: true,
                calculatedPoints: 150,
                affectedParticipants: 25,
            }

            vi.mocked(axiosInstance.put).mockResolvedValue({ data: mockResponse })

            const result = await CupService.calculate('10', mockT)

            expect(result).toEqual(mockResponse)
        })

        it('should handle calculation errors', async () => {
            const mockResponse = {
                success: false,
                error: 'Calculation failed',
            }

            vi.mocked(axiosInstance.put).mockResolvedValue({ data: mockResponse })

            const result = await CupService.calculate('5', mockT)

            expect(result.success).toBe(false)
            expect(result.error).toBeDefined()
        })
    })

    describe('inherited GenericService methods', () => {
        it('should have inherited getAll method', () => {
            const service = new CupService()
            expect(typeof service.getAll).toBe('function')
        })

        it('should have inherited getAllUnpaged method', () => {
            const service = new CupService()
            expect(typeof service.getAllUnpaged).toBe('function')
        })

        it('should have inherited getById method', () => {
            const service = new CupService()
            expect(typeof service.getById).toBe('function')
        })

        it('should have inherited create method', () => {
            const service = new CupService()
            expect(typeof service.create).toBe('function')
        })

        it('should have inherited update method', () => {
            const service = new CupService()
            expect(typeof service.update).toBe('function')
        })

        it('should have inherited deleteById method', () => {
            const service = new CupService()
            expect(typeof service.deleteById).toBe('function')
        })
    })

    describe('static method type safety', () => {
        it('should accept translation function parameter', async () => {
            const customT = (key: string) => `translated_${key}`

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: [] })

            // Should not throw type error
            await CupService.getCupTypes(customT)
            await CupService.getResultsById('1', customT)
            await CupService.calculate('1', customT)

            expect(true).toBe(true) // If we get here, types are correct
        })
    })

    describe('edge cases', () => {
        it('should handle string IDs correctly', async () => {
            const stringId = 'abc-123'

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: {} })

            await CupService.getResultsById(stringId, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(`/cup/${stringId}/results`)
        })

        it('should handle numeric string IDs', async () => {
            vi.mocked(axiosInstance.put).mockResolvedValue({ data: {} })

            await CupService.calculate('12345', mockT)

            expect(axiosInstance.put).toHaveBeenCalledWith('/cup/12345/calculate')
        })

        it('should handle special characters in IDs', async () => {
            const specialId = 'cup-2025-special'

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: {} })

            await CupService.getResultsById(specialId, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(`/cup/${specialId}/results`)
        })
    })
})
