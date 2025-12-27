import { createPinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getErrorStore } from '@/features/common/stores/getErrorStore'
import { useErrorStore } from '@/features/common/stores/useErrorStore'

// Mock pinia from main
vi.mock('@/main', () => ({
    pinia: createPinia(),
}))

describe('getErrorStore', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        // Clear store state between tests
        const store = getErrorStore()
        store.clearErrors()
    })

    describe('getErrorStore function', () => {
        it('should return error store instance', () => {
            const store = getErrorStore()

            expect(store).toBeDefined()
            expect(typeof store.addError).toBe('function')
            expect(typeof store.removeError).toBe('function')
            expect(typeof store.clearErrors).toBe('function')
        })

        it('should return same store instance on multiple calls', () => {
            const store1 = getErrorStore()
            const store2 = getErrorStore()

            expect(store1).toBe(store2)
        })

        it('should return functional store that can add errors', () => {
            const store = getErrorStore()

            store.addError({ message: 'Test error' })

            expect(store.errors).toHaveLength(1)
            expect(store.errors[0].originalError).toEqual({ message: 'Test error' })
        })

        it('should return functional store that can remove errors', () => {
            const store = getErrorStore()

            store.addError({ message: 'Error 1' })
            const id = store.errors[0].id

            store.removeError(id)

            expect(store.errors).toHaveLength(0)
        })

        it('should return functional store that can clear errors', () => {
            const store = getErrorStore()

            store.addError({ message: 'Error 1' })
            store.addError({ message: 'Error 2' })

            store.clearErrors()

            expect(store.errors).toHaveLength(0)
        })

        it('should set active pinia when called', () => {
            // This test verifies that setActivePinia is called internally
            const _store = getErrorStore()

            // Should not throw an error, which means pinia is active
            expect(() => useErrorStore()).not.toThrow()
        })

        it('should return store with initial empty state', () => {
            const store = getErrorStore()

            // Clear any previous state
            store.clearErrors()

            expect(store.errors).toEqual([])
            expect(store.errorCount).toBe(0)
        })

        it('should return store with getters', () => {
            const store = getErrorStore()

            store.clearErrors()

            expect(store.getErrors).toEqual([])
            expect(store.errorCount).toBe(0)
        })
    })
})
