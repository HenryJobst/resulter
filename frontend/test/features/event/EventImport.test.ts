import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import EventImport from '../../../src/features/event/pages/EventImport.vue'
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
        status: { value: 'idle' },
        isError: { value: false },
        error: { value: null },
    })),
}))

describe('eventImport.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renders correctly', async () => {
        const wrapper = mount(EventImport, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Verify component mounted successfully
        expect(wrapper.exists()).toBe(true)

        cleanupTest(wrapper)
    })

    it('has no accessibility violations', async () => {
        const wrapper = mount(EventImport, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Run accessibility tests
        await expectNoA11yViolations(wrapper)

        cleanupTest(wrapper)
    })
})
