import { describe, expect, it } from 'vitest'
import { primevueLocaleMessages } from '@/PrimevueMessages'

describe('primevueMessages', () => {
    describe('primevueLocaleMessages export', () => {
        it('should export primevueLocaleMessages object', () => {
            expect(primevueLocaleMessages).toBeDefined()
            expect(typeof primevueLocaleMessages).toBe('object')
        })

        it('should contain de locale messages', () => {
            expect(primevueLocaleMessages.de).toBeDefined()
            expect(typeof primevueLocaleMessages.de).toBe('object')
        })

        it('should contain en locale messages', () => {
            expect(primevueLocaleMessages.en).toBeDefined()
            expect(typeof primevueLocaleMessages.en).toBe('object')
        })
    })

    describe('de locale', () => {
        it('should have filter messages', () => {
            expect(primevueLocaleMessages.de.startsWith).toBe('Beginnt mit')
            expect(primevueLocaleMessages.de.contains).toBe('Enthält')
            expect(primevueLocaleMessages.de.endsWith).toBe('Endet mit')
        })

        it('should have day names', () => {
            expect(primevueLocaleMessages.de.dayNames).toBeDefined()
            expect(primevueLocaleMessages.de.dayNames).toHaveLength(7)
            expect(primevueLocaleMessages.de.dayNames[0]).toBe('Sonntag')
            expect(primevueLocaleMessages.de.dayNames[1]).toBe('Montag')
        })

        it('should have month names', () => {
            expect(primevueLocaleMessages.de.monthNames).toBeDefined()
            expect(primevueLocaleMessages.de.monthNames).toHaveLength(12)
            expect(primevueLocaleMessages.de.monthNames[0]).toBe('Januar')
            expect(primevueLocaleMessages.de.monthNames[11]).toBe('Dezember')
        })

        it('should have file size types', () => {
            expect(primevueLocaleMessages.de.fileSizeTypes).toBeDefined()
            expect(primevueLocaleMessages.de.fileSizeTypes).toEqual(['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'])
        })

        it('should have aria labels', () => {
            expect(primevueLocaleMessages.de.aria).toBeDefined()
            expect(primevueLocaleMessages.de.aria.close).toBe('Schließen')
            expect(primevueLocaleMessages.de.aria.next).toBe('Nächste')
            expect(primevueLocaleMessages.de.aria.previous).toBe('Vorherige')
        })

        it('should have correct firstDayOfWeek', () => {
            expect(primevueLocaleMessages.de.firstDayOfWeek).toBe(1)
        })

        it('should have correct dateFormat', () => {
            expect(primevueLocaleMessages.de.dateFormat).toBe('dd.mm.yy')
        })
    })

    describe('en locale', () => {
        it('should have filter messages', () => {
            expect(primevueLocaleMessages.en.startsWith).toBe('Starts with')
            expect(primevueLocaleMessages.en.contains).toBe('Contains')
            expect(primevueLocaleMessages.en.endsWith).toBe('Ends with')
        })

        it('should have day names', () => {
            expect(primevueLocaleMessages.en.dayNames).toBeDefined()
            expect(primevueLocaleMessages.en.dayNames).toHaveLength(7)
            expect(primevueLocaleMessages.en.dayNames[0]).toBe('Sunday')
            expect(primevueLocaleMessages.en.dayNames[1]).toBe('Monday')
        })

        it('should have month names', () => {
            expect(primevueLocaleMessages.en.monthNames).toBeDefined()
            expect(primevueLocaleMessages.en.monthNames).toHaveLength(12)
            expect(primevueLocaleMessages.en.monthNames[0]).toBe('January')
            expect(primevueLocaleMessages.en.monthNames[11]).toBe('December')
        })

        it('should have file size types', () => {
            expect(primevueLocaleMessages.en.fileSizeTypes).toBeDefined()
            expect(primevueLocaleMessages.en.fileSizeTypes).toEqual(['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'])
        })

        it('should have aria labels', () => {
            expect(primevueLocaleMessages.en.aria).toBeDefined()
            expect(primevueLocaleMessages.en.aria.close).toBe('Close')
            expect(primevueLocaleMessages.en.aria.next).toBe('Next')
            expect(primevueLocaleMessages.en.aria.previous).toBe('Previous')
        })

        it('should have correct firstDayOfWeek', () => {
            expect(primevueLocaleMessages.en.firstDayOfWeek).toBe(0)
        })

        it('should have correct dateFormat', () => {
            expect(primevueLocaleMessages.en.dateFormat).toBe('mm/dd/yy')
        })
    })

    describe('locale completeness', () => {
        it('should have same keys in both locales', () => {
            const deKeys = Object.keys(primevueLocaleMessages.de)
            const enKeys = Object.keys(primevueLocaleMessages.en)

            expect(deKeys).toHaveLength(enKeys.length)
        })

        it('should both have dayNamesShort', () => {
            expect(primevueLocaleMessages.de.dayNamesShort).toHaveLength(7)
            expect(primevueLocaleMessages.en.dayNamesShort).toHaveLength(7)
        })

        it('should both have dayNamesMin', () => {
            expect(primevueLocaleMessages.de.dayNamesMin).toHaveLength(7)
            expect(primevueLocaleMessages.en.dayNamesMin).toHaveLength(7)
        })

        it('should both have monthNamesShort', () => {
            expect(primevueLocaleMessages.de.monthNamesShort).toHaveLength(12)
            expect(primevueLocaleMessages.en.monthNamesShort).toHaveLength(12)
        })
    })
})
