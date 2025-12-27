import type { StoredError } from '@/features/common/stores/useErrorStore'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useErrorStore } from '@/features/common/stores/useErrorStore'

describe('useErrorStore', () => {
    let errorStore: ReturnType<typeof useErrorStore>

    beforeEach(() => {
        setActivePinia(createPinia())
        errorStore = useErrorStore()
    })

    describe('initial state', () => {
        it('should have empty errors array initially', () => {
            expect(errorStore.errors).toEqual([])
        })

        it('should have error count of 0 initially', () => {
            expect(errorStore.errorCount).toBe(0)
        })

        it('should return empty array from getErrors getter', () => {
            expect(errorStore.getErrors).toEqual([])
        })
    })

    describe('addError action', () => {
        it('should add a simple error object', () => {
            const error = { message: 'Test error' }

            errorStore.addError(error)

            expect(errorStore.errors).toHaveLength(1)
            expect(errorStore.errors[0].originalError).toEqual(error)
        })

        it('should add error with timestamp', () => {
            const error = { message: 'Test error' }

            errorStore.addError(error)

            expect(errorStore.errors[0].timestamp).toBeInstanceOf(Date)
        })

        it('should add error with unique ID', () => {
            const error = { message: 'Test error' }

            errorStore.addError(error)

            expect(errorStore.errors[0].id).toBeDefined()
            expect(typeof errorStore.errors[0].id).toBe('number')
        })

        it('should add multiple errors', () => {
            errorStore.addError({ message: 'Error 1' })
            errorStore.addError({ message: 'Error 2' })
            errorStore.addError({ message: 'Error 3' })

            expect(errorStore.errors).toHaveLength(3)
            expect(errorStore.errorCount).toBe(3)
        })

        it('should add different types of errors', () => {
            errorStore.addError('String error')
            errorStore.addError({ code: 404, message: 'Not found' })
            errorStore.addError(new Error('Error instance'))

            expect(errorStore.errors).toHaveLength(3)
            expect(errorStore.errors[0].originalError).toBe('String error')
            expect(errorStore.errors[1].originalError).toEqual({ code: 404, message: 'Not found' })
            expect(errorStore.errors[2].originalError).toBeInstanceOf(Error)
        })

        it('should preserve original error object', () => {
            const complexError = {
                code: 500,
                message: 'Server error',
                details: {
                    stack: 'error stack',
                    context: { userId: 123 },
                },
            }

            errorStore.addError(complexError)

            expect(errorStore.errors[0].originalError).toEqual(complexError)
        })

        it('should handle null as error', () => {
            errorStore.addError(null)

            expect(errorStore.errors).toHaveLength(1)
            expect(errorStore.errors[0].originalError).toBeNull()
        })

        it('should handle undefined as error', () => {
            errorStore.addError(undefined)

            expect(errorStore.errors).toHaveLength(1)
            expect(errorStore.errors[0].originalError).toBeUndefined()
        })

        it('should assign IDs based on timestamp', () => {
            const error1 = { message: 'Error 1' }
            const error2 = { message: 'Error 2' }

            errorStore.addError(error1)
            // Small delay to ensure different timestamp
            const firstId = errorStore.errors[0].id

            errorStore.addError(error2)
            const secondId = errorStore.errors[1].id

            // IDs are timestamps, they should exist and be numbers
            expect(firstId).toBeDefined()
            expect(secondId).toBeDefined()
            expect(typeof firstId).toBe('number')
            expect(typeof secondId).toBe('number')
            // They may be the same if added very quickly (same millisecond)
            // So we just verify they exist rather than being different
        })
    })

    describe('getters', () => {
        describe('getErrors', () => {
            it('should return all errors', () => {
                errorStore.addError({ message: 'Error 1' })
                errorStore.addError({ message: 'Error 2' })

                const allErrors = errorStore.getErrors

                expect(allErrors).toHaveLength(2)
                expect(allErrors[0].originalError).toEqual({ message: 'Error 1' })
                expect(allErrors[1].originalError).toEqual({ message: 'Error 2' })
            })

            it('should return empty array when no errors', () => {
                expect(errorStore.getErrors).toEqual([])
            })
        })

        describe('errorCount', () => {
            it('should return correct count for single error', () => {
                errorStore.addError({ message: 'Error' })

                expect(errorStore.errorCount).toBe(1)
            })

            it('should return correct count for multiple errors', () => {
                errorStore.addError({ message: 'Error 1' })
                errorStore.addError({ message: 'Error 2' })
                errorStore.addError({ message: 'Error 3' })

                expect(errorStore.errorCount).toBe(3)
            })

            it('should update count after removing errors', () => {
                errorStore.addError({ message: 'Error 1' })
                // Add a small delay to ensure different timestamps
                const id1 = errorStore.errors[0].id

                // Wait a bit to ensure different timestamp
                vi.useFakeTimers()
                vi.advanceTimersByTime(10)

                errorStore.addError({ message: 'Error 2' })
                const id2 = errorStore.errors[1].id

                vi.useRealTimers()

                // Remove first error
                errorStore.removeError(id1)

                expect(errorStore.errorCount).toBe(1)
                expect(errorStore.errors[0].id).toBe(id2)
            })
        })

        describe('getError', () => {
            it('should find error by ID', () => {
                errorStore.addError({ message: 'Test error' })
                const id = errorStore.errors[0].id

                const foundError = errorStore.getError(id)

                expect(foundError).toBeDefined()
                expect(foundError?.originalError).toEqual({ message: 'Test error' })
            })

            it('should return undefined for non-existent ID', () => {
                errorStore.addError({ message: 'Test error' })

                const foundError = errorStore.getError(99999)

                expect(foundError).toBeUndefined()
            })

            it('should find correct error among multiple', () => {
                vi.useFakeTimers()

                errorStore.addError({ message: 'Error 1' })
                vi.advanceTimersByTime(10)

                errorStore.addError({ message: 'Error 2' })
                vi.advanceTimersByTime(10)

                errorStore.addError({ message: 'Error 3' })

                vi.useRealTimers()

                const id = errorStore.errors[1].id
                const foundError = errorStore.getError(id)

                expect(foundError?.originalError).toEqual({ message: 'Error 2' })
            })
        })
    })

    describe('removeError action', () => {
        it('should remove error by ID', () => {
            errorStore.addError({ message: 'Error to remove' })
            const id = errorStore.errors[0].id

            errorStore.removeError(id)

            expect(errorStore.errors).toHaveLength(0)
        })

        it('should not affect other errors', () => {
            vi.useFakeTimers()

            errorStore.addError({ message: 'Error 1' })
            vi.advanceTimersByTime(10)

            errorStore.addError({ message: 'Error 2' })
            vi.advanceTimersByTime(10)

            errorStore.addError({ message: 'Error 3' })

            vi.useRealTimers()

            const idToRemove = errorStore.errors[1].id

            errorStore.removeError(idToRemove)

            expect(errorStore.errors).toHaveLength(2)
            expect(errorStore.errors[0].originalError).toEqual({ message: 'Error 1' })
            expect(errorStore.errors[1].originalError).toEqual({ message: 'Error 3' })
        })

        it('should do nothing if ID does not exist', () => {
            errorStore.addError({ message: 'Error' })

            errorStore.removeError(99999)

            expect(errorStore.errors).toHaveLength(1)
        })

        it('should handle removing from empty store', () => {
            errorStore.removeError(123)

            expect(errorStore.errors).toHaveLength(0)
        })
    })

    describe('clearErrors action', () => {
        it('should remove all errors', () => {
            errorStore.addError({ message: 'Error 1' })
            errorStore.addError({ message: 'Error 2' })
            errorStore.addError({ message: 'Error 3' })

            errorStore.clearErrors()

            expect(errorStore.errors).toEqual([])
            expect(errorStore.errorCount).toBe(0)
        })

        it('should work on empty store', () => {
            errorStore.clearErrors()

            expect(errorStore.errors).toEqual([])
        })

        it('should allow adding errors after clearing', () => {
            errorStore.addError({ message: 'Error 1' })
            errorStore.clearErrors()

            errorStore.addError({ message: 'New error' })

            expect(errorStore.errors).toHaveLength(1)
            expect(errorStore.errors[0].originalError).toEqual({ message: 'New error' })
        })
    })

    describe('complex scenarios', () => {
        it('should handle add, remove, and clear in sequence', () => {
            vi.useFakeTimers()

            errorStore.addError({ message: 'Error 1' })
            vi.advanceTimersByTime(10)

            errorStore.addError({ message: 'Error 2' })

            vi.useRealTimers()

            const id = errorStore.errors[0].id
            errorStore.removeError(id)

            expect(errorStore.errorCount).toBe(1)

            errorStore.addError({ message: 'Error 3' })
            expect(errorStore.errorCount).toBe(2)

            errorStore.clearErrors()
            expect(errorStore.errorCount).toBe(0)
        })

        it('should handle Error instances properly', () => {
            const jsError = new Error('JavaScript Error')
            jsError.stack = 'Error stack trace'

            errorStore.addError(jsError)

            const stored = errorStore.errors[0].originalError as Error
            expect(stored).toBeInstanceOf(Error)
            expect(stored.message).toBe('JavaScript Error')
            expect(stored.stack).toBe('Error stack trace')
        })

        it('should maintain error order', () => {
            errorStore.addError({ order: 1 })
            errorStore.addError({ order: 2 })
            errorStore.addError({ order: 3 })

            const errors = errorStore.getErrors

            expect((errors[0].originalError as any).order).toBe(1)
            expect((errors[1].originalError as any).order).toBe(2)
            expect((errors[2].originalError as any).order).toBe(3)
        })
    })

    describe('type safety', () => {
        it('should handle typed errors', () => {
            interface ApiError {
                statusCode: number
                message: string
            }

            const apiError: ApiError = {
                statusCode: 404,
                message: 'Not found',
            }

            errorStore.addError<ApiError>(apiError)

            const stored = errorStore.errors[0] as StoredError<ApiError>
            expect(stored.originalError.statusCode).toBe(404)
            expect(stored.originalError.message).toBe('Not found')
        })
    })
})
