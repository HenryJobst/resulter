import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { useMessageDetailStore } from '@/features/common/stores/useMessageDetailStore'

describe('useMessageDetailStore', () => {
    let messageDetailStore: ReturnType<typeof useMessageDetailStore>

    beforeEach(() => {
        setActivePinia(createPinia())
        messageDetailStore = useMessageDetailStore()
    })

    describe('initial state', () => {
        it('should be hidden initially', () => {
            expect(messageDetailStore.visible).toBe(false)
        })

        it('should have empty details initially', () => {
            expect(messageDetailStore.currentDetails).toBe('')
        })

        it('should return false from isVisible getter initially', () => {
            expect(messageDetailStore.isVisible).toBe(false)
        })

        it('should return empty string from getDetails getter initially', () => {
            expect(messageDetailStore.getDetails()).toBe('')
        })
    })

    describe('show action', () => {
        it('should show message with details', () => {
            const details = 'Test message details'

            messageDetailStore.show(details)

            expect(messageDetailStore.visible).toBe(true)
            expect(messageDetailStore.currentDetails).toBe(details)
        })

        it('should update isVisible getter to true', () => {
            messageDetailStore.show('Details')

            expect(messageDetailStore.isVisible).toBe(true)
        })

        it('should update getDetails getter', () => {
            messageDetailStore.show('Test details')

            expect(messageDetailStore.getDetails()).toBe('Test details')
        })

        it('should handle empty string details', () => {
            messageDetailStore.show('')

            expect(messageDetailStore.visible).toBe(true)
            expect(messageDetailStore.currentDetails).toBe('')
        })

        it('should handle long text details', () => {
            const longText = 'A'.repeat(1000)

            messageDetailStore.show(longText)

            expect(messageDetailStore.currentDetails).toBe(longText)
            expect(messageDetailStore.visible).toBe(true)
        })

        it('should handle special characters in details', () => {
            const specialChars = 'Test\n\t<script>alert("xss")</script>'

            messageDetailStore.show(specialChars)

            expect(messageDetailStore.currentDetails).toBe(specialChars)
        })

        it('should handle unicode characters', () => {
            const unicode = '🎉 Test message 中文 العربية'

            messageDetailStore.show(unicode)

            expect(messageDetailStore.currentDetails).toBe(unicode)
        })

        it('should replace previous details when shown multiple times', () => {
            messageDetailStore.show('First details')
            messageDetailStore.show('Second details')

            expect(messageDetailStore.currentDetails).toBe('Second details')
        })

        it('should remain visible when shown multiple times', () => {
            messageDetailStore.show('First')
            messageDetailStore.show('Second')

            expect(messageDetailStore.visible).toBe(true)
        })
    })

    describe('hide action', () => {
        it('should hide message', () => {
            messageDetailStore.show('Test')

            messageDetailStore.hide()

            expect(messageDetailStore.visible).toBe(false)
        })

        it('should clear details', () => {
            messageDetailStore.show('Test details')

            messageDetailStore.hide()

            expect(messageDetailStore.currentDetails).toBe('')
        })

        it('should update isVisible getter to false', () => {
            messageDetailStore.show('Test')
            messageDetailStore.hide()

            expect(messageDetailStore.isVisible).toBe(false)
        })

        it('should update getDetails getter to empty string', () => {
            messageDetailStore.show('Test')
            messageDetailStore.hide()

            expect(messageDetailStore.getDetails()).toBe('')
        })

        it('should work when already hidden', () => {
            messageDetailStore.hide()

            expect(messageDetailStore.visible).toBe(false)
            expect(messageDetailStore.currentDetails).toBe('')
        })

        it('should clear long details', () => {
            const longDetails = 'A'.repeat(1000)
            messageDetailStore.show(longDetails)

            messageDetailStore.hide()

            expect(messageDetailStore.currentDetails).toBe('')
        })
    })

    describe('show and hide cycle', () => {
        it('should handle multiple show/hide cycles', () => {
            messageDetailStore.show('First')
            expect(messageDetailStore.visible).toBe(true)

            messageDetailStore.hide()
            expect(messageDetailStore.visible).toBe(false)

            messageDetailStore.show('Second')
            expect(messageDetailStore.visible).toBe(true)
            expect(messageDetailStore.currentDetails).toBe('Second')

            messageDetailStore.hide()
            expect(messageDetailStore.visible).toBe(false)
        })

        it('should not retain previous details after hide and show', () => {
            messageDetailStore.show('First details')
            messageDetailStore.hide()
            messageDetailStore.show('Second details')

            expect(messageDetailStore.currentDetails).toBe('Second details')
        })
    })

    describe('getters reactivity', () => {
        it('should update isVisible getter when state changes', () => {
            expect(messageDetailStore.isVisible).toBe(false)

            messageDetailStore.show('Test')
            expect(messageDetailStore.isVisible).toBe(true)

            messageDetailStore.hide()
            expect(messageDetailStore.isVisible).toBe(false)
        })

        it('should update getDetails getter when state changes', () => {
            expect(messageDetailStore.getDetails()).toBe('')

            messageDetailStore.show('First')
            expect(messageDetailStore.getDetails()).toBe('First')

            messageDetailStore.show('Second')
            expect(messageDetailStore.getDetails()).toBe('Second')

            messageDetailStore.hide()
            expect(messageDetailStore.getDetails()).toBe('')
        })
    })

    describe('edge cases', () => {
        it('should handle whitespace-only details', () => {
            messageDetailStore.show('   ')

            expect(messageDetailStore.currentDetails).toBe('   ')
            expect(messageDetailStore.visible).toBe(true)
        })

        it('should handle newlines in details', () => {
            const multiline = 'Line 1\nLine 2\nLine 3'

            messageDetailStore.show(multiline)

            expect(messageDetailStore.currentDetails).toBe(multiline)
        })

        it('should handle HTML-like content', () => {
            const htmlLike = '<div>Test</div>'

            messageDetailStore.show(htmlLike)

            expect(messageDetailStore.currentDetails).toBe(htmlLike)
        })

        it('should handle JSON string', () => {
            const jsonString = JSON.stringify({ message: 'Test', code: 123 })

            messageDetailStore.show(jsonString)

            expect(messageDetailStore.currentDetails).toBe(jsonString)
        })

        it('should handle very long details efficiently', () => {
            const veryLongText = 'x'.repeat(10000)

            messageDetailStore.show(veryLongText)

            expect(messageDetailStore.currentDetails).toBe(veryLongText)
            expect(messageDetailStore.currentDetails.length).toBe(10000)
        })
    })

    describe('state isolation', () => {
        it('should maintain independent state in different store instances', () => {
            const store1 = useMessageDetailStore()
            const store2 = useMessageDetailStore()

            store1.show('Store 1 details')

            // In Pinia, useMessageDetailStore() returns the same singleton instance
            expect(store2.currentDetails).toBe('Store 1 details')
        })
    })

    describe('complex scenarios', () => {
        it('should handle rapid show/hide operations', () => {
            messageDetailStore.show('Message 1')
            messageDetailStore.hide()
            messageDetailStore.show('Message 2')
            messageDetailStore.hide()
            messageDetailStore.show('Message 3')

            expect(messageDetailStore.visible).toBe(true)
            expect(messageDetailStore.currentDetails).toBe('Message 3')
        })

        it('should preserve details when shown again without hiding', () => {
            messageDetailStore.show('First')
            messageDetailStore.show('Second')
            messageDetailStore.show('Third')

            expect(messageDetailStore.currentDetails).toBe('Third')
            expect(messageDetailStore.visible).toBe(true)
        })
    })
})
