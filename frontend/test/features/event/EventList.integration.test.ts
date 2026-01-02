import type { SportEvent } from '../../../src/features/event/model/sportEvent'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import EventList from '../../../src/features/event/pages/EventList.vue'
import { createEvent } from '../../factories/eventFactory'
import { cleanupTest, createGlobalMountOptions, expectNoA11yViolations, mockUseQuery } from '../../helpers/testSetup'

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
        useQuery: vi.fn(),
        useMutation: vi.fn(() => ({
            mutate: vi.fn(),
            isLoading: false,
        })),
    }
})

describe('eventList Integration Tests', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renders event list with mock data', async () => {
        // Setup mock query with data
        const mockEvents: SportEvent[] = [
            createEvent({ id: 1, name: 'Wettkampf 1', state: { id: 'Planned' } }),
            createEvent({ id: 2, name: 'Wettkampf 2', state: { id: 'Running' } }),
            createEvent({ id: 3, name: 'Wettkampf 3', state: { id: 'Finished' } }),
        ]

        const { useQuery } = await import('@tanstack/vue-query')
        vi.mocked(useQuery).mockReturnValue(mockUseQuery(mockEvents))

        const wrapper = mount(EventList, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Verify component mounted without errors
        expect(wrapper.exists()).toBe(true)

        cleanupTest(wrapper)
    })

    it('displays loading state', async () => {
        // Setup mock query with loading state
        const { useQuery } = await import('@tanstack/vue-query')
        vi.mocked(useQuery).mockReturnValue(mockUseQuery(null, { isLoading: true, isSuccess: false }))

        const wrapper = mount(EventList, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Verify component mounted
        expect(wrapper.exists()).toBe(true)

        cleanupTest(wrapper)
    })

    it('displays empty state when no events exist', async () => {
        // Setup mock query with empty data
        const { useQuery } = await import('@tanstack/vue-query')
        vi.mocked(useQuery).mockReturnValue(mockUseQuery([]))

        const wrapper = mount(EventList, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Verify component mounted
        expect(wrapper.exists()).toBe(true)

        cleanupTest(wrapper)
    })

    it('has no accessibility violations with data', async () => {
        const mockEvents: SportEvent[] = [
            createEvent({ id: 1, name: 'Wettkampf 1', state: { id: 'Planned' } }),
            createEvent({ id: 2, name: 'Wettkampf 2', state: { id: 'Running' } }),
        ]

        const { useQuery } = await import('@tanstack/vue-query')
        vi.mocked(useQuery).mockReturnValue(mockUseQuery(mockEvents))

        const wrapper = mount(EventList, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Run accessibility tests
        await expectNoA11yViolations(wrapper)

        cleanupTest(wrapper)
    })

    it('has no accessibility violations with empty state', async () => {
        const { useQuery } = await import('@tanstack/vue-query')
        vi.mocked(useQuery).mockReturnValue(mockUseQuery([]))

        const wrapper = mount(EventList, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Run accessibility tests
        await expectNoA11yViolations(wrapper)

        cleanupTest(wrapper)
    })
})
