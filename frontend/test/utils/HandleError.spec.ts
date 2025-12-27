import type { ApiResponse } from '@/features/common/model/apiResponse'
import { AxiosError } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as apiResponseFunctions from '@/features/common/services/apiResponseFunctions'
import {
    BackendException,
    getDetail,
    getMessage,
    handleApiError,
} from '@/utils/HandleError'

vi.mock('@/features/common/services/apiResponseFunctions', () => ({
    getApiResponse: vi.fn(),
}))

describe('handleError', () => {
    let mockT: (key: string, params?: any) => string

    beforeEach(() => {
        vi.clearAllMocks()
        mockT = vi.fn((key: string, _params?: any) => {
            // Simply return the key, as a real i18n function would return the translated string
            return key
        })
    })

    describe('backendException', () => {
        it('should create BackendException with base error', () => {
            const baseError = new Error('Base error')
            const exception = new BackendException(baseError)

            expect(exception).toBeInstanceOf(BackendException)
            expect(exception.baseError).toBe(baseError)
            expect(exception.message).toBe('Error')
        })

        it('should create BackendException with custom message', () => {
            const baseError = new Error('Base error')
            const customMessage = 'Custom error message'
            const exception = new BackendException(baseError, customMessage)

            expect(exception.message).toBe(customMessage)
            expect(exception.baseError).toBe(baseError)
        })

        it('should preserve stack trace from base error', () => {
            const baseError = new Error('Base error')
            const exception = new BackendException(baseError)

            expect(exception.stack).toBe(baseError.stack)
        })

        it('should have correct prototype chain', () => {
            const baseError = new Error('Base error')
            const exception = new BackendException(baseError)

            expect(exception instanceof Error).toBe(true)
            expect(exception instanceof BackendException).toBe(true)
        })
    })

    describe('handleApiError', () => {
        describe('axiosError with response', () => {
            it('should handle AxiosError with ApiResponse', async () => {
                const mockApiResponse: ApiResponse<any> = {
                    success: false,
                    message: {
                        messageKey: { key: 'error.notFound' },
                        messageParameters: {},
                    },
                    data: null,
                    errors: ['Not found'],
                    errorCode: 404,
                    timestamp: Date.now(),
                    path: '/api/test',
                }

                const axiosError = new AxiosError('Request failed')
                axiosError.response = {
                    data: mockApiResponse,
                    status: 404,
                    statusText: 'Not Found',
                    headers: {},
                    config: {} as any,
                }

                vi.mocked(apiResponseFunctions.getApiResponse).mockResolvedValue(mockApiResponse)

                await expect(handleApiError(axiosError, mockT)).rejects.toThrow(BackendException)

                try {
                    await handleApiError(axiosError, mockT)
                }
                catch (error) {
                    expect(error).toBeInstanceOf(BackendException)
                    expect((error as BackendException).message).toBe('backend.error.notFound')
                }
            })

            it('should handle AxiosError with response but no ApiResponse', async () => {
                const axiosError = new AxiosError('Request failed')
                axiosError.response = {
                    data: 'Invalid response',
                    status: 500,
                    statusText: 'Internal Server Error',
                    headers: {},
                    config: {} as any,
                }

                vi.mocked(apiResponseFunctions.getApiResponse).mockResolvedValue(undefined)

                await expect(handleApiError(axiosError, mockT)).rejects.toThrow(BackendException)

                try {
                    await handleApiError(axiosError, mockT)
                }
                catch (error) {
                    expect(error).toBeInstanceOf(BackendException)
                    expect((error as BackendException).message).toBe('Request failed')
                }
            })

            it('should translate message using messageParameters', async () => {
                const mockApiResponse: ApiResponse<any> = {
                    success: false,
                    message: {
                        messageKey: { key: 'error.validation' },
                        messageParameters: { field: 'email', value: 'invalid' },
                    },
                    data: null,
                    errors: [],
                    errorCode: 400,
                    timestamp: Date.now(),
                    path: '/api/test',
                }

                const axiosError = new AxiosError('Validation failed')
                axiosError.response = {
                    data: mockApiResponse,
                    status: 400,
                    statusText: 'Bad Request',
                    headers: {},
                    config: {} as any,
                }

                vi.mocked(apiResponseFunctions.getApiResponse).mockResolvedValue(mockApiResponse)

                await expect(handleApiError(axiosError, mockT)).rejects.toThrow(BackendException)

                expect(mockT).toHaveBeenCalledWith(
                    'backend.error.validation',
                    { field: 'email', value: 'invalid' },
                )
            })
        })

        describe('axiosError without response', () => {
            it('should handle network error (ERR_NETWORK)', async () => {
                const axiosError = new AxiosError('Network Error')
                axiosError.code = 'ERR_NETWORK'
                axiosError.request = {}

                await expect(handleApiError(axiosError, mockT)).rejects.toThrow()

                try {
                    await handleApiError(axiosError, mockT)
                }
                catch (error) {
                    expect((error as Error).name).toBe('errors.networkError')
                }
            })

            it('should handle other request errors', async () => {
                const axiosError = new AxiosError('Timeout')
                axiosError.code = 'ECONNABORTED'
                axiosError.name = 'TimeoutError'
                axiosError.request = {}

                await expect(handleApiError(axiosError, mockT)).rejects.toThrow()

                try {
                    await handleApiError(axiosError, mockT)
                }
                catch (error) {
                    expect((error as Error).message).toBe('errors.noResponseError')
                    expect(mockT).toHaveBeenCalledWith(
                        'errors.noResponseError',
                        {
                            name: 'TimeoutError',
                            message: 'Timeout',
                            code: 'ECONNABORTED',
                        },
                    )
                }
            })
        })

        describe('non-AxiosError handling', () => {
            it('should rethrow Error instances', async () => {
                const error = new Error('Generic error')

                await expect(handleApiError(error, mockT)).rejects.toThrow('Generic error')
            })

            it('should wrap non-Error objects', async () => {
                const error = 'String error'

                await expect(handleApiError(error, mockT)).rejects.toThrow()

                try {
                    await handleApiError(error, mockT)
                }
                catch (err) {
                    expect(err).toBeInstanceOf(Error)
                    expect((err as Error).message).toBe('errors.unknownError')
                }
            })

            it('should handle null error', async () => {
                await expect(handleApiError(null, mockT)).rejects.toThrow()

                try {
                    await handleApiError(null, mockT)
                }
                catch (err) {
                    expect(err).toBeInstanceOf(Error)
                }
            })

            it('should handle undefined error', async () => {
                await expect(handleApiError(undefined, mockT)).rejects.toThrow()
            })
        })
    })

    describe('getMessage', () => {
        it('should get message from BackendException', () => {
            const baseError = new Error('Base error')
            const exception = new BackendException(baseError, 'Custom message')

            const message = getMessage(exception)

            expect(message).toBe('Custom message')
        })

        it('should get name from non-BackendException error', () => {
            const error = new Error('Test error')
            error.name = 'TestError'

            const message = getMessage(error)

            expect(message).toBe('TestError')
        })

        it('should get message if name is not available', () => {
            const error = { message: 'Error message' }

            const message = getMessage(error)

            expect(message).toBe('Error message')
        })

        it('should handle error with both name and message', () => {
            const error = { name: 'CustomError', message: 'Custom message' }

            const message = getMessage(error)

            expect(message).toBe('CustomError')
        })
    })

    describe('getDetail', () => {
        it('should get detail from BackendException with errors array', async () => {
            const mockApiResponse: ApiResponse<any> = {
                success: false,
                message: { messageKey: { key: 'error' }, messageParameters: {} },
                data: null,
                errors: ['Error 1', 'Error 2', 'Error 3'],
                errorCode: 400,
                timestamp: Date.now(),
                path: '/api/test',
            }

            const axiosError = new AxiosError('Request failed')
            axiosError.response = {
                data: mockApiResponse,
                status: 400,
                statusText: 'Bad Request',
                headers: {},
                config: {} as any,
            }

            const exception = new BackendException(axiosError, 'Test error')

            vi.mocked(apiResponseFunctions.getApiResponse).mockResolvedValue(mockApiResponse)

            const detail = await getDetail(exception, mockT)

            expect(detail).toBe('Error 1, Error 2, Error 3')
        })

        it('should get detail from BackendException with translated message', async () => {
            const mockApiResponse: ApiResponse<any> = {
                success: false,
                message: {
                    messageKey: { key: 'validation.failed' },
                    messageParameters: { field: 'email' },
                },
                data: null,
                errors: null as any, // null so it falls through to the message translation
                errorCode: 400,
                timestamp: Date.now(),
                path: '/api/test',
            }

            const axiosError = new AxiosError('Validation failed')
            axiosError.response = {
                data: mockApiResponse,
                status: 400,
                statusText: 'Bad Request',
                headers: {},
                config: {} as any,
            }

            const exception = new BackendException(axiosError, 'Test error')

            vi.mocked(apiResponseFunctions.getApiResponse).mockResolvedValue(mockApiResponse)

            const detail = await getDetail(exception, mockT)

            expect(detail).toBe('backend.validation.failed')
            expect(mockT).toHaveBeenCalledWith('backend.validation.failed', { field: 'email' })
        })

        it('should return stack trace for non-BackendException', async () => {
            const error = new Error('Generic error')
            error.stack = 'Error: Generic error\n    at test.js:1:1'

            const detail = await getDetail(error, mockT)

            expect(detail).toBe(error.stack)
        })

        it('should return stack trace for BackendException without AxiosError', async () => {
            const baseError = new Error('Base error')
            baseError.stack = 'Error: Base error\n    at test.js:1:1'
            const exception = new BackendException(baseError)

            const detail = await getDetail(exception, mockT)

            expect(detail).toBe(baseError.stack)
        })

        it('should return stack trace for BackendException with AxiosError but no response', async () => {
            const axiosError = new AxiosError('Request failed')
            axiosError.stack = 'AxiosError: Request failed'
            const exception = new BackendException(axiosError)

            const detail = await getDetail(exception, mockT)

            expect(detail).toBe(axiosError.stack)
        })

        it('should return stack trace when ApiResponse is undefined', async () => {
            const axiosError = new AxiosError('Request failed')
            axiosError.response = {
                data: {},
                status: 500,
                statusText: 'Internal Server Error',
                headers: {},
                config: {} as any,
            }
            axiosError.stack = 'AxiosError: Request failed'

            const exception = new BackendException(axiosError)

            vi.mocked(apiResponseFunctions.getApiResponse).mockResolvedValue(undefined)

            const detail = await getDetail(exception, mockT)

            expect(detail).toBe(axiosError.stack)
        })

        it('should handle error object without stack', async () => {
            const error = { message: 'Error without stack' }

            const detail = await getDetail(error, mockT)

            expect(detail).toBeUndefined()
        })
    })
})
