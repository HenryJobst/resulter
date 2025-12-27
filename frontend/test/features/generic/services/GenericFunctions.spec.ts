import { describe, expect, it, vi } from 'vitest'
import {
    formatDate,
    formatDateAndTime,
    formatTime,
    formatYear,
} from '@/features/generic/services/GenericFunctions'

describe('genericFunctions', () => {
    // Use a fixed date for consistent testing
    const testDate = new Date('2025-01-15T14:30:45')
    const testDateString = '2025-01-15T14:30:45'

    describe('formatDate', () => {
        it('should format Date object with German locale', () => {
            const result = formatDate(testDate, 'de-DE')
            expect(result).toBe('15. Januar 2025')
        })

        it('should format Date object with English locale', () => {
            const result = formatDate(testDate, 'en-US')
            expect(result).toBe('January 15, 2025')
        })

        it('should format date string with German locale', () => {
            const result = formatDate(testDateString, 'de-DE')
            expect(result).toBe('15. Januar 2025')
        })

        it('should format date string with English locale', () => {
            const result = formatDate(testDateString, 'en-US')
            expect(result).toBe('January 15, 2025')
        })

        it('should return empty string for null input', () => {
            const result = formatDate(null as any, 'de-DE')
            expect(result).toBe('')
        })

        it('should return empty string for undefined input', () => {
            const result = formatDate(undefined as any, 'de-DE')
            expect(result).toBe('')
        })

        it('should return empty string for invalid date string', () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
            const result = formatDate('invalid-date', 'de-DE')
            expect(result).toBe('')
            expect(consoleErrorSpy).toHaveBeenCalled()
            consoleErrorSpy.mockRestore()
        })

        it('should handle different date formats', () => {
            const isoDate = '2025-12-25'
            const result = formatDate(isoDate, 'de-DE')
            expect(result).toContain('Dezember')
            expect(result).toContain('2025')
        })
    })

    describe('formatDateAndTime', () => {
        it('should format Date object with German locale', () => {
            const result = formatDateAndTime(testDate, 'de-DE')
            expect(result).toContain('15. Januar 2025')
            expect(result).toContain('14:30:45')
        })

        it('should format Date object with English locale', () => {
            const result = formatDateAndTime(testDate, 'en-US')
            expect(result).toContain('January 15, 2025')
            expect(result).toContain('14:30:45')
        })

        it('should format date string with German locale', () => {
            const result = formatDateAndTime(testDateString, 'de-DE')
            expect(result).toContain('15. Januar 2025')
            expect(result).toContain('14:30:45')
        })

        it('should format date string with English locale', () => {
            const result = formatDateAndTime(testDateString, 'en-US')
            expect(result).toContain('January 15, 2025')
            expect(result).toContain('14:30:45')
        })

        it('should return empty string for null input', () => {
            const result = formatDateAndTime(null as any, 'de-DE')
            expect(result).toBe('')
        })

        it('should return empty string for undefined input', () => {
            const result = formatDateAndTime(undefined as any, 'de-DE')
            expect(result).toBe('')
        })

        it('should return empty string for invalid date string', () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
            const result = formatDateAndTime('not-a-date', 'de-DE')
            expect(result).toBe('')
            expect(consoleErrorSpy).toHaveBeenCalled()
            consoleErrorSpy.mockRestore()
        })

        it('should include seconds in output', () => {
            const dateWithSeconds = new Date('2025-06-01T09:15:30')
            const result = formatDateAndTime(dateWithSeconds, 'en-US')
            expect(result).toContain('09:15:30')
        })
    })

    describe('formatYear', () => {
        it('should format year with German locale', () => {
            const result = formatYear(testDate, 'de-DE')
            expect(result).toBe('25')
        })

        it('should format year with English locale', () => {
            const result = formatYear(testDate, 'en-US')
            expect(result).toBe('25')
        })

        it('should format year from date string', () => {
            const result = formatYear(testDateString, 'de-DE')
            expect(result).toBe('25')
        })

        it('should return empty string for null input', () => {
            const result = formatYear(null as any, 'de-DE')
            expect(result).toBe('')
        })

        it('should return empty string for undefined input', () => {
            const result = formatYear(undefined as any, 'de-DE')
            expect(result).toBe('')
        })

        it('should return empty string for invalid date', () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
            const result = formatYear('invalid', 'de-DE')
            expect(result).toBe('')
            expect(consoleErrorSpy).toHaveBeenCalled()
            consoleErrorSpy.mockRestore()
        })

        it('should format different years correctly', () => {
            const date2020 = new Date('2020-01-01')
            const result = formatYear(date2020, 'en-US')
            expect(result).toBe('20')
        })
    })

    describe('formatTime', () => {
        it('should format time with German locale', () => {
            const result = formatTime(testDate, 'de-DE')
            expect(result).toBe('14:30')
        })

        it('should format time with English locale', () => {
            const result = formatTime(testDate, 'en-US')
            expect(result).toBe('14:30')
        })

        it('should format time from date string', () => {
            const result = formatTime(testDateString, 'de-DE')
            expect(result).toBe('14:30')
        })

        it('should return empty string for null input', () => {
            const result = formatTime(null as any, 'de-DE')
            expect(result).toBe('')
        })

        it('should return empty string for undefined input', () => {
            const result = formatTime(undefined as any, 'de-DE')
            expect(result).toBe('')
        })

        it('should return empty string for invalid date', () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
            const result = formatTime('not-valid', 'de-DE')
            expect(result).toBe('')
            expect(consoleErrorSpy).toHaveBeenCalled()
            consoleErrorSpy.mockRestore()
        })

        it('should use 24-hour format', () => {
            const afternoonTime = new Date('2025-01-15T18:45:00')
            const result = formatTime(afternoonTime, 'en-US')
            expect(result).toBe('18:45')
            expect(result).not.toContain('PM')
        })

        it('should handle midnight correctly', () => {
            const midnight = new Date('2025-01-15T00:00:00')
            const result = formatTime(midnight, 'de-DE')
            expect(result).toBe('00:00')
        })

        it('should handle noon correctly', () => {
            const noon = new Date('2025-01-15T12:00:00')
            const result = formatTime(noon, 'de-DE')
            expect(result).toBe('12:00')
        })

        it('should handle different times', () => {
            const morningTime = new Date('2025-01-15T08:15:00')
            const result = formatTime(morningTime, 'en-US')
            expect(result).toBe('08:15')
        })
    })

    describe('edge cases', () => {
        it('should handle leap year dates', () => {
            const leapDay = new Date('2024-02-29T12:00:00')
            const result = formatDate(leapDay, 'en-US')
            expect(result).toContain('February 29')
            expect(result).toContain('2024')
        })

        it('should handle year boundaries', () => {
            const newYear = new Date('2025-01-01T00:00:00')
            const result = formatDateAndTime(newYear, 'de-DE')
            expect(result).toContain('1. Januar 2025')
            expect(result).toContain('00:00:00')
        })

        it('should handle end of year', () => {
            const endOfYear = new Date('2025-12-31T23:59:59')
            const result = formatDateAndTime(endOfYear, 'en-US')
            expect(result).toContain('December 31, 2025')
            expect(result).toContain('23:59:59')
        })

        it('should handle very old dates', () => {
            const oldDate = new Date('1900-01-01T00:00:00')
            const result = formatDate(oldDate, 'en-US')
            expect(result).toContain('1900')
        })

        it('should handle future dates', () => {
            const futureDate = new Date('2099-12-31T23:59:59')
            const result = formatDate(futureDate, 'de-DE')
            expect(result).toContain('2099')
        })
    })

    describe('locale handling', () => {
        it('should handle French locale', () => {
            const result = formatDate(testDate, 'fr-FR')
            expect(result).toContain('janvier')
            expect(result).toContain('2025')
        })

        it('should handle Spanish locale', () => {
            const result = formatDate(testDate, 'es-ES')
            expect(result).toContain('enero')
            expect(result).toContain('2025')
        })

        it('should handle default locale when not specified', () => {
            const result = formatDate(testDate, undefined)
            // Should not throw and should return a string
            expect(typeof result).toBe('string')
            expect(result.length).toBeGreaterThan(0)
        })
    })

    describe('error handling', () => {
        it('should log error for invalid string date', () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

            formatDate('completely invalid date string', 'de-DE')

            expect(consoleErrorSpy).toHaveBeenCalledWith(
                expect.stringContaining('Fehler beim Formatieren des Datums'),
            )

            consoleErrorSpy.mockRestore()
        })

        it('should handle empty string as invalid date', () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
            const result = formatDate('', 'de-DE')
            expect(result).toBe('')
            consoleErrorSpy.mockRestore()
        })

        it('should handle whitespace-only string as invalid date', () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
            const result = formatDate('   ', 'de-DE')
            expect(result).toBe('')
            expect(consoleErrorSpy).toHaveBeenCalled()
            consoleErrorSpy.mockRestore()
        })
    })
})
