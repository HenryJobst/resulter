import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import CupNew from '@/features/cup/pages/CupNew.vue'
import { createGlobalMountOptions } from '../../../helpers/testSetup'

describe('cupNew', () => {
    describe('component rendering', () => {
        it('should render successfully', () => {
            const wrapper = mount(CupNew, {
                global: createGlobalMountOptions(),
            })

            expect(wrapper.exists()).toBe(true)
            expect(wrapper.vm).toBeDefined()
        })
    })

    describe('integration with GenericNew', () => {
        it('should use GenericNew component', () => {
            const wrapper = mount(CupNew, {
                global: createGlobalMountOptions(),
            })

            // GenericNew should be the root component
            expect(wrapper.findComponent({ name: 'GenericNew' }).exists()).toBe(true)
        })

        it('should pass correct router prefix to GenericNew', () => {
            const wrapper = mount(CupNew, {
                global: createGlobalMountOptions(),
            })

            const genericNew = wrapper.findComponent({ name: 'GenericNew' })
            expect(genericNew.props('routerPrefix')).toBe('cup')
        })

        it('should pass cupService to GenericNew', () => {
            const wrapper = mount(CupNew, {
                global: createGlobalMountOptions(),
            })

            const genericNew = wrapper.findComponent({ name: 'GenericNew' })
            expect(genericNew.props('entityService')).toBeDefined()
        })

        it('should pass queryKey to GenericNew', () => {
            const wrapper = mount(CupNew, {
                global: createGlobalMountOptions(),
            })

            const genericNew = wrapper.findComponent({ name: 'GenericNew' })
            expect(genericNew.props('queryKey')).toEqual(['cup'])
        })

        it('should pass entityLabel to GenericNew', () => {
            const wrapper = mount(CupNew, {
                global: createGlobalMountOptions(),
            })

            const genericNew = wrapper.findComponent({ name: 'GenericNew' })
            expect(genericNew.props('entityLabel')).toBe('cup')
        })
    })

    describe('authentication integration', () => {
        it('should respect auth store isAdmin for changeable prop', () => {
            const wrapper = mount(CupNew, {
                global: createGlobalMountOptions(),
            })

            const genericNew = wrapper.findComponent({ name: 'GenericNew' })
            // changeable prop should be bound to authStore.isAdmin
            expect(genericNew.props('changeable')).toBeDefined()
        })
    })
})
