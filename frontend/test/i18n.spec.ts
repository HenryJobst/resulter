import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getFlagClass, getLocale, SUPPORT_LOCALES, setI18nLanguage, setLocale, setupI18n } from '@/i18n'
import type { I18n } from 'vue-i18n'

// Mock PrimeVue
vi.mock('primevue/config', () => ({
    usePrimeVue: vi.fn(() => ({
        config: {
            locale: {},
        },
    })),
}))

// Mock PrimevueMessages
vi.mock('@/PrimevueMessages', () => ({
    primevueLocaleMessages: {
        de: { dayNames: ['Sonntag', 'Montag'] },
        en: { dayNames: ['Sunday', 'Monday'] },
    },
}))

describe('i18n utilities', () => {
    describe('SUPPORT_LOCALES', () => {
        it('should contain en and de locales', () => {
            expect(SUPPORT_LOCALES).toEqual(['en', 'de'])
        })

        it('should have exactly 2 supported locales', () => {
            expect(SUPPORT_LOCALES).toHaveLength(2)
        })

        it('should include English locale', () => {
            expect(SUPPORT_LOCALES).toContain('en')
        })

        it('should include German locale', () => {
            expect(SUPPORT_LOCALES).toContain('de')
        })
    })

    describe('getFlagClass', () => {
        it('should return gb flag class for en locale', () => {
            const result = getFlagClass('en')
            expect(result).toBe('fi fi-gb')
        })

        it('should return de flag class for de locale', () => {
            const result = getFlagClass('de')
            expect(result).toBe('fi fi-de')
        })

        it('should return fr flag class for fr locale', () => {
            const result = getFlagClass('fr')
            expect(result).toBe('fi fi-fr')
        })

        it('should return es flag class for es locale', () => {
            const result = getFlagClass('es')
            expect(result).toBe('fi fi-es')
        })

        it('should return it flag class for it locale', () => {
            const result = getFlagClass('it')
            expect(result).toBe('fi fi-it')
        })
    })

    describe('setupI18n', () => {
        it('should create i18n instance with default locale', () => {
            const i18n = setupI18n()
            expect(i18n).toBeDefined()
            expect(i18n.global).toBeDefined()
        })

        it('should create i18n instance with specified locale', () => {
            const i18n = setupI18n({ locale: 'de' })
            const locale = getLocale(i18n)
            expect(locale).toBe('de')
        })

        it('should create i18n instance with en locale', () => {
            const i18n = setupI18n({ locale: 'en' })
            const locale = getLocale(i18n)
            expect(locale).toBe('en')
        })

        it('should create i18n with messages', () => {
            const i18n = setupI18n({
                locale: 'en',
                messages: {
                    en: { hello: 'Hello' },
                    de: { hello: 'Hallo' },
                },
            })
            expect(i18n.global.messages).toBeDefined()
        })
    })

    describe('getLocale', () => {
        let i18n: I18n

        beforeEach(() => {
            i18n = setupI18n({ locale: 'de' })
        })

        it('should return current locale', () => {
            const locale = getLocale(i18n)
            expect(locale).toBe('de')
        })

        it('should return en when locale is set to en', () => {
            setLocale(i18n, 'en')
            const locale = getLocale(i18n)
            expect(locale).toBe('en')
        })

        it('should return de when locale is set to de', () => {
            setLocale(i18n, 'de')
            const locale = getLocale(i18n)
            expect(locale).toBe('de')
        })
    })

    describe('setLocale', () => {
        let i18n: I18n

        beforeEach(() => {
            i18n = setupI18n({ locale: 'en' })
        })

        it('should change locale to de', () => {
            setLocale(i18n, 'de')
            const locale = getLocale(i18n)
            expect(locale).toBe('de')
        })

        it('should change locale to en', () => {
            setLocale(i18n, 'en')
            const locale = getLocale(i18n)
            expect(locale).toBe('en')
        })

        it('should update locale from de to en', () => {
            setLocale(i18n, 'de')
            expect(getLocale(i18n)).toBe('de')

            setLocale(i18n, 'en')
            expect(getLocale(i18n)).toBe('en')
        })

        it('should allow setting locale multiple times', () => {
            setLocale(i18n, 'en')
            setLocale(i18n, 'de')
            setLocale(i18n, 'en')

            expect(getLocale(i18n)).toBe('en')
        })
    })

    describe('setI18nLanguage', () => {
        let i18n: I18n

        beforeEach(() => {
            i18n = setupI18n({ locale: 'en' })
            // Mock document
            document.querySelector = vi.fn((selector: string) => {
                if (selector === 'html') {
                    return {
                        setAttribute: vi.fn(),
                    } as any
                }
                return null
            })
        })

        it('should set locale and update html lang attribute', () => {
            const mockSetAttribute = vi.fn()
            document.querySelector = vi.fn(() => ({
                setAttribute: mockSetAttribute,
            } as any))

            setI18nLanguage(i18n, 'de')

            expect(getLocale(i18n)).toBe('de')
            expect(mockSetAttribute).toHaveBeenCalledWith('lang', 'de')
        })

        it('should set locale to en and update html lang', () => {
            const mockSetAttribute = vi.fn()
            document.querySelector = vi.fn(() => ({
                setAttribute: mockSetAttribute,
            } as any))

            setI18nLanguage(i18n, 'en')

            expect(getLocale(i18n)).toBe('en')
            expect(mockSetAttribute).toHaveBeenCalledWith('lang', 'en')
        })

        it('should update html lang attribute for different locales', () => {
            const mockSetAttribute = vi.fn()
            document.querySelector = vi.fn(() => ({
                setAttribute: mockSetAttribute,
            } as any))

            setI18nLanguage(i18n, 'de')
            setI18nLanguage(i18n, 'en')

            expect(mockSetAttribute).toHaveBeenCalledTimes(2)
            expect(mockSetAttribute).toHaveBeenNthCalledWith(1, 'lang', 'de')
            expect(mockSetAttribute).toHaveBeenNthCalledWith(2, 'lang', 'en')
        })
    })
})
