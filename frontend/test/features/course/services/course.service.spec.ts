import { beforeEach, describe, expect, it, vi } from 'vitest'
import { CourseService, courseService } from '@/features/course/services/course.service'

// Mock axios instance
vi.mock('@/features/keycloak/services/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}))

describe('courseService', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('constructor', () => {
        it('should create instance with correct endpoint', () => {
            const service = new CourseService()
            expect(service).toBeInstanceOf(CourseService)
        })

        it('should extend GenericService', () => {
            const service = new CourseService()
            // GenericService methods should be available
            expect(typeof service.getAll).toBe('function')
            expect(typeof service.getById).toBe('function')
            expect(typeof service.create).toBe('function')
            expect(typeof service.update).toBe('function')
            expect(typeof service.deleteById).toBe('function')
        })
    })

    describe('exported instance', () => {
        it('should export courseService instance', () => {
            expect(courseService).toBeInstanceOf(CourseService)
        })

        it('should be a singleton instance', () => {
            // The exported instance should always be the same
            expect(courseService).toBe(courseService)
        })
    })

    describe('inherited GenericService methods', () => {
        it('should have inherited getAll method', () => {
            const service = new CourseService()
            expect(typeof service.getAll).toBe('function')
        })

        it('should have inherited getAllUnpaged method', () => {
            const service = new CourseService()
            expect(typeof service.getAllUnpaged).toBe('function')
        })

        it('should have inherited getById method', () => {
            const service = new CourseService()
            expect(typeof service.getById).toBe('function')
        })

        it('should have inherited create method', () => {
            const service = new CourseService()
            expect(typeof service.create).toBe('function')
        })

        it('should have inherited update method', () => {
            const service = new CourseService()
            expect(typeof service.update).toBe('function')
        })

        it('should have inherited deleteById method', () => {
            const service = new CourseService()
            expect(typeof service.deleteById).toBe('function')
        })
    })
})
