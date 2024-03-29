import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EventImport from '../../../src/features/event/pages/EventImport.vue'

describe('eventImport.vue', () => {
    it('renders correctly', () => {
        const wrapper = mount(EventImport)
        expect(wrapper.text()).toContain('messages.import_event')
    })
})
