import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import EventNew from '../../../src/features/event/pages/EventNew.vue'
import { cleanupTest, createGlobalMountOptions, expectNoA11yViolations } from '../../helpers/testSetup'

// Mock Keycloak
vi.mock('@/features/keycloak/services/keycloak', () => ({
    getKeycloak: vi.fn(() => ({
        authenticated: true,
        hasRealmRole: vi.fn(() => true),
    })),
}))

// Mock vue-router
vi.mock('vue-router', () => ({
    useRouter: vi.fn(() => ({
        push: vi.fn(),
    })),
    RouterLink: {
        name: 'RouterLink',
        template: '<a><slot /></a>',
    },
}))

// Mock Tanstack Query
vi.mock('@tanstack/vue-query', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@tanstack/vue-query')>()
    return {
        ...actual,
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
            status: { value: 'idle' },
            isError: { value: false },
            error: { value: null },
        })),
    }
})

describe('eventNew.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renders correctly for admin user', async () => {
        const wrapper = mount(EventNew, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Verify component mounted successfully
        expect(wrapper.exists()).toBe(true)

        cleanupTest(wrapper)
    })

    it('has no accessibility violations', async () => {
        const wrapper = mount(EventNew, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Run accessibility tests
        await expectNoA11yViolations(wrapper)

        cleanupTest(wrapper)
    })
})
