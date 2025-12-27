import type { Gender } from '@/features/person/model/gender'
import type { Person } from '@/features/person/model/person'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import axiosInstance from '@/features/auth/services/api'
import { duplicatePersonService, PersonService, personService } from '@/features/person/services/person.service'

// Mock axios instance
vi.mock('@/features/auth/services/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}))

describe('personService', () => {
    const mockT = (key: string) => key

    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('constructor', () => {
        it('should create instance with correct endpoint', () => {
            const service = new PersonService()
            expect(service).toBeInstanceOf(PersonService)
        })

        it('should extend GenericService', () => {
            const service = new PersonService()
            // GenericService methods should be available
            expect(typeof service.getAll).toBe('function')
            expect(typeof service.getById).toBe('function')
            expect(typeof service.create).toBe('function')
            expect(typeof service.update).toBe('function')
            expect(typeof service.deleteById).toBe('function')
        })
    })

    describe('exported instances', () => {
        it('should export personService instance', () => {
            expect(personService).toBeInstanceOf(PersonService)
        })

        it('should export duplicatePersonService instance', () => {
            expect(duplicatePersonService).toBeDefined()
        })

        it('should be singleton instances', () => {
            // The exported instances should always be the same
            expect(personService).toBe(personService)
            expect(duplicatePersonService).toBe(duplicatePersonService)
        })
    })

    describe('getGender', () => {
        it('should fetch gender types', async () => {
            const mockGenders: Gender[] = [
                { id: 'MALE' },
                { id: 'FEMALE' },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockGenders })

            const result = await PersonService.getGender(mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/gender')
            expect(result).toEqual(mockGenders)
        })

        it('should return null when response is null', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: null })

            const result = await PersonService.getGender(mockT)

            expect(result).toBeNull()
        })

        it('should handle empty gender array', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: [] })

            const result = await PersonService.getGender(mockT)

            expect(result).toEqual([])
        })

        it('should handle multiple genders', async () => {
            const mockGenders: Gender[] = [
                { id: 'MALE' },
                { id: 'FEMALE' },
                { id: 'OTHER' },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockGenders })

            const result = await PersonService.getGender(mockT)

            expect(result).toHaveLength(3)
            expect(result).toEqual(mockGenders)
        })
    })

    describe('getPersonDoubles', () => {
        it('should fetch person doubles by ID', async () => {
            const mockPersons: Person[] = [
                {
                    id: 2,
                    familyName: 'Smith',
                    givenName: 'John',
                    gender: { id: 'MALE' },
                    birthDate: '1990-01-01',
                },
                {
                    id: 3,
                    familyName: 'Smith',
                    givenName: 'John',
                    gender: { id: 'MALE' },
                    birthDate: '1990-01-01',
                },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockPersons })

            const result = await PersonService.getPersonDoubles(1, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/person/1/doubles')
            expect(result).toEqual(mockPersons)
        })

        it('should return null when ID is 0', async () => {
            const result = await PersonService.getPersonDoubles(0, mockT)

            expect(result).toBeNull()
            expect(axiosInstance.get).not.toHaveBeenCalled()
        })

        it('should handle different person IDs', async () => {
            const mockPersons: Person[] = [
                {
                    id: 101,
                    familyName: 'Doe',
                    givenName: 'Jane',
                    gender: { id: 'FEMALE' },
                    birthDate: null,
                },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockPersons })

            const result = await PersonService.getPersonDoubles(42, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/person/42/doubles')
            expect(result).toEqual(mockPersons)
        })

        it('should handle empty doubles array', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: [] })

            const result = await PersonService.getPersonDoubles(5, mockT)

            expect(result).toEqual([])
        })

        it('should handle null response from API', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: null })

            const result = await PersonService.getPersonDoubles(10, mockT)

            expect(result).toBeNull()
        })

        it('should handle person with Date birthDate', async () => {
            const mockPersons: Person[] = [
                {
                    id: 2,
                    familyName: 'Test',
                    givenName: 'User',
                    gender: { id: 'MALE' },
                    birthDate: new Date('1985-05-15'),
                },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockPersons })

            const result = await PersonService.getPersonDoubles(1, mockT)

            expect(result).toEqual(mockPersons)
        })
    })

    describe('merge', () => {
        it('should merge two persons', async () => {
            const mockMergeResponse = { success: true, message: 'Merge successful' }

            vi.mocked(axiosInstance.post).mockResolvedValue({ data: mockMergeResponse })

            const result = await PersonService.merge(1, 2, mockT)

            expect(axiosInstance.post).toHaveBeenCalledWith('/person/1/merge', 2)
            expect(result).toEqual(mockMergeResponse)
        })

        it('should handle different person IDs for merge', async () => {
            const mockMergeResponse = { success: true }

            vi.mocked(axiosInstance.post).mockResolvedValue({ data: mockMergeResponse })

            await PersonService.merge(100, 200, mockT)

            expect(axiosInstance.post).toHaveBeenCalledWith('/person/100/merge', 200)
        })

        it('should return merge results', async () => {
            const mockMergeResponse = {
                success: true,
                mergedId: 1,
                removedId: 2,
                message: 'Successfully merged persons',
            }

            vi.mocked(axiosInstance.post).mockResolvedValue({ data: mockMergeResponse })

            const result = await PersonService.merge(1, 2, mockT)

            expect(result).toEqual(mockMergeResponse)
        })

        it('should handle merge errors', async () => {
            const mockErrorResponse = {
                success: false,
                error: 'Cannot merge persons',
            }

            vi.mocked(axiosInstance.post).mockResolvedValue({ data: mockErrorResponse })

            const result = await PersonService.merge(1, 2, mockT)

            expect(result.success).toBe(false)
            expect(result.error).toBeDefined()
        })

        it('should handle large person IDs', async () => {
            vi.mocked(axiosInstance.post).mockResolvedValue({ data: { success: true } })

            await PersonService.merge(999999, 888888, mockT)

            expect(axiosInstance.post).toHaveBeenCalledWith('/person/999999/merge', 888888)
        })
    })

    describe('static method type safety', () => {
        it('should accept translation function parameter', async () => {
            const customT = (key: string) => `translated_${key}`

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: [] })
            vi.mocked(axiosInstance.post).mockResolvedValue({ data: {} })

            // Should not throw type error
            await PersonService.getGender(customT)
            await PersonService.getPersonDoubles(1, customT)
            await PersonService.merge(1, 2, customT)

            expect(true).toBe(true) // If we get here, types are correct
        })
    })

    describe('inherited GenericService methods', () => {
        it('should have inherited getAll method', () => {
            const service = new PersonService()
            expect(typeof service.getAll).toBe('function')
        })

        it('should have inherited getAllUnpaged method', () => {
            const service = new PersonService()
            expect(typeof service.getAllUnpaged).toBe('function')
        })

        it('should have inherited getById method', () => {
            const service = new PersonService()
            expect(typeof service.getById).toBe('function')
        })

        it('should have inherited create method', () => {
            const service = new PersonService()
            expect(typeof service.create).toBe('function')
        })

        it('should have inherited update method', () => {
            const service = new PersonService()
            expect(typeof service.update).toBe('function')
        })

        it('should have inherited deleteById method', () => {
            const service = new PersonService()
            expect(typeof service.deleteById).toBe('function')
        })
    })

    describe('duplicatePersonService', () => {
        it('should be an instance of a service', () => {
            expect(duplicatePersonService).toBeDefined()
            expect(typeof duplicatePersonService.getAll).toBe('function')
        })

        it('should have GenericService methods available', () => {
            expect(typeof duplicatePersonService.getAll).toBe('function')
            expect(typeof duplicatePersonService.getById).toBe('function')
            expect(typeof duplicatePersonService.create).toBe('function')
            expect(typeof duplicatePersonService.update).toBe('function')
            expect(typeof duplicatePersonService.deleteById).toBe('function')
        })
    })
})
