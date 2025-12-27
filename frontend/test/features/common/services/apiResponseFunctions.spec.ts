import type { ApiResponse } from '@/features/common/model/apiResponse'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getApiResponse } from '@/features/common/services/apiResponseFunctions'

describe('apiResponseFunctions', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('getApiResponse', () => {
        it('should extract ApiResponse from response.data', async () => {
            const mockApiResponse: ApiResponse<{ id: number }> = {
                success: true,
                message: { messageKey: { key: 'success' } },
                data: { id: 1 },
                errors: [],
                errorCode: 0,
                timestamp: Date.now(),
                path: '/api/test',
            }

            const response = {
                data: mockApiResponse,
            }

            const result = await getApiResponse(response)

            expect(result).toEqual(mockApiResponse)
            expect(result?.success).toBe(true)
            expect(result?.data).toEqual({ id: 1 })
        })

        it('should return undefined when response.data is null', async () => {
            const response = {
                data: null,
            }

            const result = await getApiResponse(response)

            expect(result).toBeUndefined()
        })

        it('should handle ApiResponse with success=false', async () => {
            const mockApiResponse: ApiResponse<null> = {
                success: false,
                message: { messageKey: { key: 'error.occurred' } },
                data: null,
                errors: ['Error 1', 'Error 2'],
                errorCode: 400,
                timestamp: Date.now(),
                path: '/api/test',
            }

            const response = {
                data: mockApiResponse,
            }

            const result = await getApiResponse(response)

            expect(result).toEqual(mockApiResponse)
            expect(result?.success).toBe(false)
            expect(result?.errors).toEqual(['Error 1', 'Error 2'])
            expect(result?.errorCode).toBe(400)
        })

        it('should handle null response', async () => {
            const result = await getApiResponse(null)

            expect(result).toBeUndefined()
        })

        it('should handle undefined response', async () => {
            const result = await getApiResponse(undefined)

            expect(result).toBeUndefined()
        })

        it('should handle response without data property', async () => {
            const response = {
                status: 200,
                statusText: 'OK',
            }

            const result = await getApiResponse(response)

            expect(result).toBeUndefined()
        })

        it('should validate ApiResponse structure with all required fields', async () => {
            const mockApiResponse: ApiResponse<string> = {
                success: true,
                message: { messageKey: { key: 'test.message' }, messageParameters: { name: 'Test' } },
                data: 'test data',
                errors: [],
                errorCode: 0,
                timestamp: 1234567890,
                path: '/api/path',
            }

            const response = {
                data: mockApiResponse,
            }

            const result = await getApiResponse(response)

            expect(result).toEqual(mockApiResponse)
            expect(result?.message.messageKey).toEqual({ key: 'test.message' })
        })

        it('should handle ApiResponse with null data', async () => {
            const mockApiResponse: ApiResponse<null> = {
                success: true,
                message: { messageKey: { key: 'no.data' } },
                data: null,
                errors: [],
                errorCode: 0,
                timestamp: Date.now(),
                path: '/api/test',
            }

            const response = {
                data: mockApiResponse,
            }

            const result = await getApiResponse(response)

            expect(result).toEqual(mockApiResponse)
            expect(result?.data).toBeNull()
        })

        it('should handle ApiResponse with complex nested data', async () => {
            const complexData = {
                user: {
                    id: 1,
                    name: 'Test User',
                    roles: ['admin', 'user'],
                },
                metadata: {
                    timestamp: Date.now(),
                    version: '1.0',
                },
            }

            const mockApiResponse: ApiResponse<typeof complexData> = {
                success: true,
                message: { messageKey: { key: 'complex.data' } },
                data: complexData,
                errors: [],
                errorCode: 0,
                timestamp: Date.now(),
                path: '/api/test',
            }

            const response = {
                data: mockApiResponse,
            }

            const result = await getApiResponse(response)

            expect(result).toEqual(mockApiResponse)
            expect(result?.data).toEqual(complexData)
        })

        it('should handle ApiResponse with array data', async () => {
            const arrayData = [
                { id: 1, name: 'Item 1' },
                { id: 2, name: 'Item 2' },
            ]

            const mockApiResponse: ApiResponse<typeof arrayData> = {
                success: true,
                message: { messageKey: { key: 'array.data' } },
                data: arrayData,
                errors: [],
                errorCode: 0,
                timestamp: Date.now(),
                path: '/api/items',
            }

            const response = {
                data: mockApiResponse,
            }

            const result = await getApiResponse(response)

            expect(result).toEqual(mockApiResponse)
            expect(result?.data).toEqual(arrayData)
            expect(Array.isArray(result?.data)).toBe(true)
        })
    })
})
