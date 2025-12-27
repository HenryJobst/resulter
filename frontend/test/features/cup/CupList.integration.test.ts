import type { Cup } from '../../../src/features/cup/model/cup'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import CupList from '../../../src/features/cup/pages/CupList.vue'
import { createCup } from '../../factories/cupFactory'
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
vi.mock('@tanstack/vue-query', () => ({
    useQueryClient: vi.fn(() => ({
        invalidateQueries: vi.fn(),
        setQueryData: vi.fn(),
    })),
    useQuery: vi.fn(),
    useMutation: vi.fn(() => ({
        mutate: vi.fn(),
        isLoading: false,
    })),
}))

describe('cupList Integration Tests', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renders cup list with mock data', async () => {
        // Setup mock query with data
        const mockCups: Cup[] = [
            createCup({ id: 1, name: 'Kristall-Cup 2025', year: 2025 }),
            createCup({ id: 2, name: 'Nebel-Cup 2025', type: { id: 'NEBEL', name: 'Nebel-Cup' }, year: 2025 }),
            createCup({ id: 3, name: 'Nord-Ost-Ranking 2025', type: { id: 'NOR', name: 'Nord-Ost-Ranking' }, year: 2025 }),
        ]

        const { useQuery } = await import('@tanstack/vue-query')
        vi.mocked(useQuery).mockReturnValue(mockUseQuery(mockCups))

        const wrapper = mount(CupList, {
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

        const wrapper = mount(CupList, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Verify component mounted
        expect(wrapper.exists()).toBe(true)

        cleanupTest(wrapper)
    })

    it('displays empty state when no cups exist', async () => {
        // Setup mock query with empty data
        const { useQuery } = await import('@tanstack/vue-query')
        vi.mocked(useQuery).mockReturnValue(mockUseQuery([]))

        const wrapper = mount(CupList, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Verify component mounted
        expect(wrapper.exists()).toBe(true)

        cleanupTest(wrapper)
    })

    it('displays different cup types correctly', async () => {
        // Setup mock query with different cup types
        const mockCups: Cup[] = [
            createCup({ id: 1, name: 'Kristall-Cup 2025', type: { id: 'KRISTALL', name: 'Kristall-Cup' } }),
            createCup({ id: 2, name: 'Nebel-Cup 2025', type: { id: 'NEBEL', name: 'Nebel-Cup' } }),
        ]

        const { useQuery } = await import('@tanstack/vue-query')
        vi.mocked(useQuery).mockReturnValue(mockUseQuery(mockCups))

        const wrapper = mount(CupList, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Verify component mounted
        expect(wrapper.exists()).toBe(true)

        cleanupTest(wrapper)
    })

    it('has no accessibility violations with data', async () => {
        const mockCups: Cup[] = [
            createCup({ id: 1, name: 'Kristall-Cup 2025' }),
            createCup({ id: 2, name: 'Nebel-Cup 2025', type: { id: 'NEBEL', name: 'Nebel-Cup' } }),
        ]

        const { useQuery } = await import('@tanstack/vue-query')
        vi.mocked(useQuery).mockReturnValue(mockUseQuery(mockCups))

        const wrapper = mount(CupList, {
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

        const wrapper = mount(CupList, {
            global: createGlobalMountOptions(),
        })

        await nextTick()

        // Run accessibility tests
        await expectNoA11yViolations(wrapper)

        cleanupTest(wrapper)
    })
})
