import { beforeEach, describe, expect, it, vi } from 'vitest'
import axiosInstance from '@/features/keycloak/services/api'
import { MediaService, mediaService } from '@/features/media/services/media.service'

vi.mock('@/features/keycloak/services/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}))

describe('mediaService', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('constructor', () => {
        it('should create instance with correct endpoint', () => {
            const service = new MediaService()

            expect(service).toBeInstanceOf(MediaService)
        })

        it('should extend GenericService', () => {
            const service = new MediaService()

            // Check that it has GenericService methods
            expect(typeof service.getAll).toBe('function')
            expect(typeof service.getById).toBe('function')
            expect(typeof service.getAllUnpaged).toBe('function')
            expect(typeof service.create).toBe('function')
            expect(typeof service.update).toBe('function')
            expect(typeof service.deleteById).toBe('function')
        })
    })

    describe('static upload method', () => {
        it('should upload file with FormData', async () => {
            const mockFormData = new FormData()
            mockFormData.append('file', new Blob(['test']), 'test.jpg')
            const mockResponse = {
                data: {
                    success: true,
                    message: { key: 'upload.success' },
                    data: { id: 1, filename: 'test.jpg' },
                },
            }
            const mockT = vi.fn((key: string) => key)

            vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse)

            const result = await MediaService.upload(mockFormData, mockT)

            expect(axiosInstance.post).toHaveBeenCalledWith(
                '/media/upload',
                mockFormData,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                },
            )
            expect(result).toEqual(mockResponse.data)
        })

        it('should pass correct headers for multipart form data', async () => {
            const mockFormData = new FormData()
            const mockResponse = { data: { success: true } }
            const mockT = vi.fn((key: string) => key)

            vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse)

            await MediaService.upload(mockFormData, mockT)

            expect(axiosInstance.post).toHaveBeenCalledWith(
                expect.any(String),
                expect.any(FormData),
                expect.objectContaining({
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                }),
            )
        })

        it('should handle upload errors', async () => {
            const mockFormData = new FormData()
            const mockT = vi.fn((key: string) => key)
            const mockError = new Error('Upload failed')

            vi.mocked(axiosInstance.post).mockRejectedValue(mockError)

            await expect(MediaService.upload(mockFormData, mockT)).rejects.toThrow('Upload failed')
        })

        it('should return response data directly', async () => {
            const mockFormData = new FormData()
            const mockT = vi.fn((key: string) => key)
            const expectedData = {
                success: true,
                data: { id: 123, url: '/media/123.jpg' },
            }
            const mockResponse = { data: expectedData }

            vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse)

            const result = await MediaService.upload(mockFormData, mockT)

            expect(result).toEqual(expectedData)
        })
    })

    describe('exported instance', () => {
        it('should export mediaService instance', () => {
            expect(mediaService).toBeInstanceOf(MediaService)
        })

        it('should have singleton pattern', () => {
            // The exported instance should be the same across imports
            expect(mediaService).toBeDefined()
            expect(mediaService).toBeInstanceOf(MediaService)
        })
    })
})
