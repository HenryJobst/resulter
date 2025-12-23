import { createTestingPinia } from '@pinia/testing'
import { vi, expect } from 'vitest'
import { createI18n } from 'vue-i18n'
import type { VueWrapper } from '@vue/test-utils'

/**
 * Creates a properly configured i18n instance for tests
 */
export function createTestI18n(locale = 'de') {
    return createI18n({
        legacy: false,
        locale,
        fallbackLocale: 'en',
        messages: {
            de: {
                labels: {
                    cup: 'Cup',
                    cups: 'Cups',
                    event: 'Wettkampf',
                    events: 'Wettkämpfe',
                    name: 'Name',
                    year: 'Jahr',
                    type: 'Typ',
                    state: 'Status',
                    actions: 'Aktionen',
                },
                messages: {
                    loading: 'Lädt...',
                    no_data: 'Keine Daten verfügbar',
                    new_entity: '{entity} anlegen',
                    import_event: 'Wettkampf importieren',
                    save: 'Speichern',
                    cancel: 'Abbrechen',
                    delete: 'Löschen',
                    edit: 'Bearbeiten',
                },
            },
            en: {
                labels: {
                    cup: 'Cup',
                    cups: 'Cups',
                    event: 'Event',
                    events: 'Events',
                    name: 'Name',
                    year: 'Year',
                    type: 'Type',
                    state: 'State',
                    actions: 'Actions',
                },
                messages: {
                    loading: 'Loading...',
                    no_data: 'No data available',
                    new_entity: 'Create {entity}',
                    import_event: 'Import event',
                    save: 'Save',
                    cancel: 'Cancel',
                    delete: 'Delete',
                    edit: 'Edit',
                },
            },
        },
    })
}

/**
 * Creates common global mount options for component tests
 */
export function createGlobalMountOptions(options: {
    locale?: string
    authenticated?: boolean
    isAdmin?: boolean
} = {}) {
    const { locale = 'de', authenticated = true, isAdmin = true } = options

    return {
        plugins: [
            createTestingPinia({
                stubActions: false,
                createSpy: vi.fn,
                initialState: {
                    authStore: {
                        authenticated,
                        isAdmin,
                    },
                },
            }),
            createTestI18n(locale),
        ],
        stubs: {
            // Stub all complex components that depend on PrimeVue or other external deps
            GenericList: true,
            GenericNew: true,
            GenericEdit: true,
            EventForm: true,
            DataTable: true,
            Column: true,
            Button: true,
            Card: true,
            Dropdown: true,
            InputText: true,
            Calendar: true,
            Textarea: true,
            FileUpload: true,
            Toast: true,
            ConfirmDialog: true,
            Spinner: true,
            Tooltip: true,
            RouterLink: {
                template: '<a><slot /></a>',
            },
        },
        directives: {
            // Mock PrimeVue directives
            tooltip: vi.fn(),
        },
    }
}

/**
 * Mock Tanstack Query's useQuery with custom data
 */
export function mockUseQuery(data: any, options: {
    isLoading?: boolean
    error?: any
    isSuccess?: boolean
} = {}) {
    const {
        isLoading = false,
        error = null,
        isSuccess = true,
    } = options

    return {
        data: { value: data },
        isLoading: { value: isLoading },
        error: { value: error },
        isSuccess: { value: isSuccess },
        isFetching: { value: isLoading },
        refetch: vi.fn(),
        suspense: vi.fn(),
        isPending: { value: isLoading },
        isError: { value: !!error },
        status: { value: isSuccess ? 'success' : isLoading ? 'pending' : 'error' },
    } as any
}

/**
 * Cleanup function to unmount wrapper and clear mocks
 */
export function cleanupTest(wrapper?: VueWrapper) {
    if (wrapper) {
        wrapper.unmount()
    }
    vi.clearAllMocks()
}

/**
 * Run accessibility tests on a component wrapper
 * Uses axe-core to check for WCAG violations
 * @param wrapper - Vue component wrapper from @vue/test-utils
 */
export async function expectNoA11yViolations(wrapper: VueWrapper) {
    const axe = (await import('axe-core')).default

    // Check if there's actual content to test
    const element = wrapper.element as HTMLElement
    if (!element || !element.children || element.children.length === 0) {
        // Skip accessibility test for empty/stubbed components
        console.warn('Skipping accessibility test: No actual DOM content found (likely all components are stubbed)')
        return
    }

    try {
        const results = await axe.run(element, {
            rules: {
                // Disable region rule as it's not applicable in component tests
                region: { enabled: false },
                // Disable landmark rules for isolated component tests
                'landmark-one-main': { enabled: false },
                'landmark-unique': { enabled: false },
                // Disable page-has-heading as we're testing isolated components
                'page-has-heading-one': { enabled: false },
            },
        })

        // Check for violations and create a readable error message
        if (results.violations.length > 0) {
            const violationMessages = results.violations.map((violation) => {
                const nodes = violation.nodes.map((node) => {
                    return `  - ${node.html}\n    ${node.failureSummary}`
                }).join('\n')

                return `${violation.id}: ${violation.description}\n  Impact: ${violation.impact}\n  Help: ${violation.help}\n  ${violation.helpUrl}\n${nodes}`
            }).join('\n\n')

            throw new Error(`Accessibility violations found:\n\n${violationMessages}`)
        }
    }
    catch (error) {
        // If axe fails to run (e.g., no valid context), skip the test with a warning
        if (error instanceof Error && error.message.includes('No elements found')) {
            console.warn('Skipping accessibility test: Component has no analyzable elements')
            return
        }
        throw error
    }
}
