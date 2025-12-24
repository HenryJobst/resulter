import { createPinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getMessageDetailStore } from '@/features/common/stores/getMessageDetailStore'
import { useMessageDetailStore } from '@/features/common/stores/useMessageDetailStore'

// Mock pinia from main
vi.mock('@/main', () => ({
    pinia: createPinia(),
}))

describe('getMessageDetailStore', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        // Clear store state between tests
        const store = getMessageDetailStore()
        store.hide()
    })

    describe('getMessageDetailStore function', () => {
        it('should return message detail store instance', () => {
            const store = getMessageDetailStore()

            expect(store).toBeDefined()
            expect(typeof store.show).toBe('function')
            expect(typeof store.hide).toBe('function')
        })

        it('should return same store instance on multiple calls', () => {
            const store1 = getMessageDetailStore()
            const store2 = getMessageDetailStore()

            expect(store1).toBe(store2)
        })

        it('should return functional store that can show messages', () => {
            const store = getMessageDetailStore()

            store.show('Test message')

            expect(store.visible).toBe(true)
            expect(store.currentDetails).toBe('Test message')
        })

        it('should return functional store that can hide messages', () => {
            const store = getMessageDetailStore()

            store.show('Test message')
            store.hide()

            expect(store.visible).toBe(false)
            expect(store.currentDetails).toBe('')
        })

        it('should set active pinia when called', () => {
            // This test verifies that setActivePinia is called internally
            const store = getMessageDetailStore()

            // Should not throw an error, which means pinia is active
            expect(() => useMessageDetailStore()).not.toThrow()
        })

        it('should return store with initial state', () => {
            const store = getMessageDetailStore()

            // Reset to initial state
            store.hide()

            expect(store.visible).toBe(false)
            expect(store.currentDetails).toBe('')
        })

        it('should return store with getters', () => {
            const store = getMessageDetailStore()

            store.hide()

            expect(store.isVisible).toBe(false)
            expect(store.getDetails()).toBe('')
        })

        it('should return store that maintains state across calls', () => {
            const store1 = getMessageDetailStore()
            store1.show('Persistent message')

            const store2 = getMessageDetailStore()

            // Both references should point to same store with same state
            expect(store2.currentDetails).toBe('Persistent message')
            expect(store2.visible).toBe(true)
        })
    })
})
