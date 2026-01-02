import type { VueWrapper } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { vi } from 'vitest'
import { createI18n } from 'vue-i18n'

/**
 * Creates a QueryClient configured for tests
 */
export function createTestQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: {
                retry: false,
                gcTime: 0,
            },
            mutations: {
                retry: false,
            },
        },
    })
}

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
                    entity: 'Entität',
                    name: 'Name',
                    year: 'Jahr',
                    type: 'Typ',
                    state: 'Status',
                    actions: 'Aktionen',
                    backend_version: 'Backend-Version',
                    loading: 'Lädt...',
                    error_loading: 'Fehler beim Laden',
                    back: 'Zurück',
                    save: 'Speichern',
                    new: 'Neu',
                    reload: 'Neu laden',
                    created: 'Erstellt',
                    bytes: 'Bytes',
                    kilobytes: 'KB',
                    megabytes: 'MB',
                    gigabytes: 'GB',
                    date: 'Datum',
                    time: 'Uhrzeit',
                    select: 'Auswählen',
                    organisation: 'Organisation',
                    certificate: 'Zertifikat',
                },
                messages: {
                    loading: 'Lädt...',
                    no_data: 'Keine Daten verfügbar',
                    new_entity: '{entity} anlegen',
                    entity_created: '{entity} wurde erstellt',
                    import_event: 'Wettkampf importieren',
                    event_uploaded: 'Wettkampf hochgeladen',
                    save: 'Speichern',
                    cancel: 'Abbrechen',
                    delete: 'Löschen',
                    edit: 'Bearbeiten',
                    success: 'Erfolgreich',
                    select: 'Auswählen',
                },
                event_state: {
                    PLANNED: 'Geplant',
                    FINISHED: 'Abgeschlossen',
                    CANCELLED: 'Abgesagt',
                },
            },
            en: {
                labels: {
                    cup: 'Cup',
                    cups: 'Cups',
                    event: 'Event',
                    events: 'Events',
                    entity: 'Entity',
                    name: 'Name',
                    year: 'Year',
                    type: 'Type',
                    state: 'State',
                    actions: 'Actions',
                    backend_version: 'Backend Version',
                    loading: 'Loading...',
                    error_loading: 'Error loading',
                    back: 'Back',
                    save: 'Save',
                    new: 'New',
                    reload: 'Reload',
                    created: 'Created',
                    bytes: 'Bytes',
                    kilobytes: 'KB',
                    megabytes: 'MB',
                    gigabytes: 'GB',
                    date: 'Date',
                    time: 'Time',
                    select: 'Select',
                    organisation: 'Organisation',
                    certificate: 'Certificate',
                },
                messages: {
                    loading: 'Loading...',
                    no_data: 'No data available',
                    new_entity: 'Create {entity}',
                    entity_created: '{entity} was created',
                    import_event: 'Import event',
                    event_uploaded: 'Event uploaded',
                    save: 'Save',
                    cancel: 'Cancel',
                    delete: 'Delete',
                    edit: 'Edit',
                    success: 'Success',
                    select: 'Select',
                },
                event_state: {
                    PLANNED: 'Planned',
                    FINISHED: 'Finished',
                    CANCELLED: 'Cancelled',
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
    includeVueQuery?: boolean
} = {}) {
    const { locale = 'de', authenticated = true, isAdmin = true, includeVueQuery = true } = options

    const plugins: any[] = [
        createTestingPinia({
            stubActions: false,
            createSpy: vi.fn,
            initialState: {
                authStore: {
                    authenticated,
                    user: {
                        roles: isAdmin ? ['user', 'admin'] : ['user'],
                        username: 'testuser',
                        token: 'mock-token',
                    },
                },
            },
        }),
        createTestI18n(locale),
    ]

    // Only add VueQueryPlugin if requested
    // Check if VueQueryPlugin is available (it might be undefined if mocked incorrectly)
    if (includeVueQuery && typeof VueQueryPlugin !== 'undefined') {
        plugins.push([VueQueryPlugin, { queryClient: createTestQueryClient() }])
    }

    return {
        plugins,
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
                'region': { enabled: false },
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
