import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import { createI18n } from 'vue-i18n'
import EventNew from '../../../src/features/event/pages/EventNew.vue'

// Mock der Keycloak-Module
vi.mock('@/features/keycloak/services/keycloak', () => ({
    getKeycloak: vi.fn(() => ({
        authenticated: true,
        hasRealmRole: vi.fn(() => true),
    })),
}))

// Mock der Vue Query Funktionen
vi.mock('@tanstack/vue-query', () => ({
    useQueryClient: vi.fn(() => ({
        invalidateQueries: vi.fn(),
        setQueryData: vi.fn(),
    })),
    useQuery: vi.fn(() => ({
        data: { value: [] },
        isLoading: { value: false },
        error: { value: null },
        status: { value: 'success' },
    })),
    useMutation: vi.fn(() => ({
        mutate: vi.fn(),
        isLoading: false,
        status: {
            value: 'idle',
        },
        isError: {
            value: false,
        },
        error: {
            value: null,
        },
    })),
    VueQueryPlugin: {
        install: vi.fn(),
    },
}))

// Mock für useToast
vi.mock('primevue/usetoast', () => ({
    useToast: vi.fn(() => ({
        add: vi.fn(),
        remove: vi.fn(),
        removeGroup: vi.fn(),
        removeAllGroups: vi.fn(),
    })),
}))

// Mock für vue-router
vi.mock('vue-router', () => ({
    useRouter: vi.fn(() => ({
        push: vi.fn(),
    })),
}))

describe('eventNew.vue', () => {
    beforeEach(() => {
        // Zurücksetzen der Mocks vor jedem Test
        vi.clearAllMocks()
    })

    it('renders correctly for admin user', async () => {
        const i18n = createI18n({
            legacy: false,
            locale: 'de',
            messages: {
                de: {
                    'labels.event': 'Wettkampf',
                    'messages.new_entity': '{entity} anlegen',
                    'messages.loading': 'Lädt...',
                },
            },
        })

        const wrapper = mount(EventNew, {
            global: {
                plugins: [
                    createTestingPinia({
                        stubActions: true,
                        createSpy: vi.fn,
                        initialState: {
                            authStore: {
                                authenticated: true,
                                user: { username: 'mockedUser', roles: ['admin'] },
                                isAdmin: true,
                                isAuthenticated: true,
                            },
                        },
                    }),
                    i18n,
                ],
                stubs: {
                    EventForm: false,
                    GenericNew: false,
                    Spinner: true,
                    Tooltip: true,
                },
                mocks: {
                    $router: {
                        push: vi.fn(),
                    },
                    $route: {
                        params: {
                            id: '1',
                        },
                    },
                },
            },
        })

        await nextTick()

        expect(wrapper.html()).toContain('Wettkampf anlegen')
    })
})
