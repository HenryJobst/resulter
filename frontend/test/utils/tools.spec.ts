import { describe, expect, it } from 'vitest'
import { getValueByPath, truncateString } from '../../src/utils/tools'

describe('tools utilities', () => {
    describe('getValueByPath', () => {
        it('should get simple property value', () => {
            const obj = { name: 'Test' }
            expect(getValueByPath(obj, 'name')).toBe('Test')
        })

        it('should get nested property value', () => {
            const obj = { user: { name: 'John', age: 30 } }
            expect(getValueByPath(obj, 'user.name')).toBe('John')
            expect(getValueByPath(obj, 'user.age')).toBe(30)
        })

        it('should get deeply nested property value', () => {
            const obj = {
                data: {
                    user: {
                        profile: {
                            firstName: 'Jane',
                        },
                    },
                },
            }
            expect(getValueByPath(obj, 'data.user.profile.firstName')).toBe('Jane')
        })

        it('should return undefined for non-existent property', () => {
            const obj = { name: 'Test' }
            expect(getValueByPath(obj, 'age')).toBeUndefined()
        })

        it('should return undefined for non-existent nested property', () => {
            const obj = { user: { name: 'John' } }
            expect(getValueByPath(obj, 'user.email')).toBeUndefined()
        })

        it('should handle null object gracefully', () => {
            expect(getValueByPath(null, 'name')).toBeNull()
        })

        it('should handle undefined object gracefully', () => {
            expect(getValueByPath(undefined, 'name')).toBeUndefined()
        })

        it('should handle array values', () => {
            const obj = { items: ['a', 'b', 'c'] }
            expect(getValueByPath(obj, 'items.0')).toBe('a')
            expect(getValueByPath(obj, 'items.1')).toBe('b')
        })
    })

    describe('truncateString', () => {
        it('should not truncate string shorter than max length', () => {
            const obj = { text: 'Short text' }
            expect(truncateString(obj, 'text', 100)).toBe('Short text')
        })

        it('should truncate string longer than max length', () => {
            const obj = { text: 'A'.repeat(1100) }
            const result = truncateString(obj, 'text', 1000)
            expect(result).toBe(`${'A'.repeat(1000)}...`)
            expect(result.length).toBe(1003) // 1000 chars + '...'
        })

        it('should use default max length of 1000', () => {
            const obj = { text: 'A'.repeat(1100) }
            const result = truncateString(obj, 'text')
            expect(result).toBe(`${'A'.repeat(1000)}...`)
        })

        it('should work with nested properties', () => {
            const obj = {
                data: {
                    description: 'B'.repeat(150),
                },
            }
            const result = truncateString(obj, 'data.description', 100)
            expect(result).toBe(`${'B'.repeat(100)}...`)
        })

        it('should return non-string values as-is', () => {
            const obj = { count: 42 }
            expect(truncateString(obj, 'count', 10)).toBe(42)
        })

        it('should handle undefined values', () => {
            const obj = { text: undefined }
            expect(truncateString(obj, 'text', 10)).toBeUndefined()
        })

        it('should handle null values', () => {
            const obj = { text: null }
            expect(truncateString(obj, 'text', 10)).toBeNull()
        })

        it('should handle empty string', () => {
            const obj = { text: '' }
            expect(truncateString(obj, 'text', 10)).toBe('')
        })

        it('should truncate at exact max length boundary', () => {
            const obj = { text: 'A'.repeat(1000) }
            const result = truncateString(obj, 'text', 1000)
            expect(result).toBe('A'.repeat(1000))
            expect(result.length).toBe(1000)
        })

        it('should truncate one character over max length', () => {
            const obj = { text: 'A'.repeat(1001) }
            const result = truncateString(obj, 'text', 1000)
            expect(result).toBe(`${'A'.repeat(1000)}...`)
        })

        it('should work with custom max length', () => {
            const obj = { text: 'Hello World!' }
            expect(truncateString(obj, 'text', 5)).toBe('Hello...')
            expect(truncateString(obj, 'text', 10)).toBe('Hello Worl...')
            expect(truncateString(obj, 'text', 20)).toBe('Hello World!')
        })
    })
})
