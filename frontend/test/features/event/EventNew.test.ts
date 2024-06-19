import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import { createTestingPinia } from '@pinia/testing'
import EventNew from '../../../src/features/event/pages/EventNew.vue'

describe('eventNew.vue', () => {
    beforeEach(() => {
    })

    it('renders correctly for admin user, ', async () => {
        const wrapper = mount(EventNew, {
            global: {
                plugins: [createTestingPinia({
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
                })],
                mocks: {
                    $t: (tKey: string) => tKey,
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
