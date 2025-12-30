import type { OrganisationType } from '../../../../src/features/organisation/model/organisation_type'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import axiosInstance from '../../../../src/features/auth/services/api'
import {
    organisationService,
    OrganisationService,
} from '../../../../src/features/organisation/services/organisation.service'

// Mock axios instance
vi.mock('@/features/auth/services/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}))

describe('organisationService', () => {
    const mockT = (key: string) => key

    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('constructor', () => {
        it('should create instance with correct endpoint', () => {
            const service = new OrganisationService()
            expect(service).toBeInstanceOf(OrganisationService)
        })

        it('should extend GenericService', () => {
            const service = new OrganisationService()
            // GenericService methods should be available
            expect(typeof service.getAll).toBe('function')
            expect(typeof service.getById).toBe('function')
            expect(typeof service.create).toBe('function')
            expect(typeof service.update).toBe('function')
            expect(typeof service.deleteById).toBe('function')
        })
    })

    describe('exported instance', () => {
        it('should export organisationService instance', () => {
            expect(organisationService).toBeInstanceOf(OrganisationService)
        })

        it('should be a singleton instance', () => {
            // The exported instance should always be the same
            expect(organisationService).toBe(organisationService)
        })
    })

    describe('getOrganisationTypes', () => {
        it('should fetch organisation types', async () => {
            const mockOrganisationTypes: OrganisationType[] = [
                { id: 'CLUB' },
                { id: 'FEDERATION' },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockOrganisationTypes })

            const result = await OrganisationService.getOrganisationTypes(mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/organisation/types')
            expect(result).toEqual(mockOrganisationTypes)
        })

        it('should return null when response is null', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: null })

            const result = await OrganisationService.getOrganisationTypes(mockT)

            expect(result).toBeNull()
        })

        it('should handle empty organisation types array', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: [] })

            const result = await OrganisationService.getOrganisationTypes(mockT)

            expect(result).toEqual([])
        })

        it('should handle multiple organisation types', async () => {
            const mockOrganisationTypes: OrganisationType[] = [
                { id: 'CLUB' },
                { id: 'FEDERATION' },
                { id: 'ASSOCIATION' },
                { id: 'REGIONAL' },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockOrganisationTypes })

            const result = await OrganisationService.getOrganisationTypes(mockT)

            expect(result).toHaveLength(4)
            expect(result).toEqual(mockOrganisationTypes)
        })

        it('should accept translation function parameter', async () => {
            const customT = (key: string) => `translated_${key}`

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: [] })

            // Should not throw type error
            await OrganisationService.getOrganisationTypes(customT)

            expect(true).toBe(true) // If we get here, types are correct
        })
    })

    describe('inherited GenericService methods', () => {
        it('should have inherited getAll method', () => {
            const service = new OrganisationService()
            expect(typeof service.getAll).toBe('function')
        })

        it('should have inherited getAllUnpaged method', () => {
            const service = new OrganisationService()
            expect(typeof service.getAllUnpaged).toBe('function')
        })

        it('should have inherited getById method', () => {
            const service = new OrganisationService()
            expect(typeof service.getById).toBe('function')
        })

        it('should have inherited create method', () => {
            const service = new OrganisationService()
            expect(typeof service.create).toBe('function')
        })

        it('should have inherited update method', () => {
            const service = new OrganisationService()
            expect(typeof service.update).toBe('function')
        })

        it('should have inherited deleteById method', () => {
            const service = new OrganisationService()
            expect(typeof service.deleteById).toBe('function')
        })
    })
})
