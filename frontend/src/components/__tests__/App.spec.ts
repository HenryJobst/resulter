import { mount } from '@vue/test-utils'

import { describe, expect, it } from 'vitest'
import App from '../../App.vue'

describe('app', () => {
    it('renders properly', () => {
        const wrapper = mount(App, {})
        expect(wrapper.text()).toContain('Resulter')
    })
})
