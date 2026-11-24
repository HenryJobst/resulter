import { mount } from '@vue/test-utils'
import OpenVue from 'openvue/config'
import { describe, expect, it } from 'vitest'
import { createI18n } from 'vue-i18n'
import CertificateLayoutCanvas from '@/features/certificate/widgets/CertificateLayoutCanvas.vue'
import de from '@/locales/de.json'

const i18n = createI18n({ legacy: false, locale: 'de', messages: { de } })

const layout = JSON.stringify({
    paragraphs: [
        { marginTop: 10, blocks: [{ block: { text: '{{GIVEN_NAME}} {{FAMILY_NAME}}', fontSize: 20 } }] },
        {
            tabStops: [{ position: 10, alignment: 'LEFT' }, { position: 200, alignment: 'RIGHT' }],
            blocks: [{ block: { text: 'left' } }, { block: { text: 'right' } }],
        },
    ],
})

// width 595 = one pixel per point, so pixel deltas equal point deltas
function mountCanvas(modelValue = layout) {
    return mount(CertificateLayoutCanvas, {
        props: { modelValue, width: 595 },
        global: { plugins: [i18n, OpenVue] },
        attachTo: document.body,
    })
}

function lastLayout(wrapper: ReturnType<typeof mountCanvas>) {
    const emitted = wrapper.emitted('update:modelValue')!
    return JSON.parse(emitted[emitted.length - 1]![0] as string)
}

function drag(wrapper: ReturnType<typeof mountCanvas>, box: ReturnType<typeof wrapper.find>, dx: number, dy: number) {
    return (async () => {
        await box.trigger('pointerdown', { clientX: 100, clientY: 100, pointerId: 1 })
        await wrapper.find('[data-testid="layout-canvas"]').trigger('pointermove', { clientX: 100 + dx, clientY: 100 + dy, pointerId: 1 })
        await wrapper.find('[data-testid="layout-canvas"]').trigger('pointerup', { pointerId: 1 })
    })()
}

describe('certificateLayoutCanvas', () => {
    it('shows every block with sample data and emits nothing on load', () => {
        const wrapper = mountCanvas()
        expect(wrapper.findAll('[role="button"]')).toHaveLength(3)
        expect(wrapper.text()).toContain('Max Mustermann')
        expect(wrapper.emitted('update:modelValue')).toBeUndefined()
    })

    it('selects a block on click', async () => {
        const wrapper = mountCanvas()
        await wrapper.findAll('[role="button"]')[2]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
        expect(wrapper.emitted('update:selected')![0]).toEqual([{ pi: 1, bi: 1 }])
    })

    it('moves a paragraph vertically by dragging (space above)', async () => {
        const wrapper = mountCanvas()
        await drag(wrapper, wrapper.findAll('[role="button"]')[0]!, 3, 25)
        // 10 + 25 pt; a small horizontal part does not change the tab stop
        expect(lastLayout(wrapper).paragraphs[0].marginTop).toBe(35)
        expect(lastLayout(wrapper).paragraphs[0].tabStops).toBeUndefined()
    })

    it('moves a block horizontally by dragging (tab stop)', async () => {
        const wrapper = mountCanvas()
        await drag(wrapper, wrapper.findAll('[role="button"]')[2]!, 40, 2)
        const result = lastLayout(wrapper).paragraphs[1]
        expect(result.tabStops[1].position).toBe(240)
        expect(result.marginTop).toBeUndefined()
    })

    it('writes the implicit default tab stop down when it is moved', async () => {
        const wrapper = mountCanvas()
        await drag(wrapper, wrapper.findAll('[role="button"]')[0]!, 30, 1)
        expect(lastLayout(wrapper).paragraphs[0].tabStops).toEqual([{ position: 291.5, alignment: 'CENTER' }])
    })

    it('never makes the space above negative', async () => {
        const wrapper = mountCanvas()
        await drag(wrapper, wrapper.findAll('[role="button"]')[0]!, 0, -500)
        expect(lastLayout(wrapper).paragraphs[0].marginTop).toBe(0)
    })

    it('moves the selected block with the arrow keys', async () => {
        const wrapper = mountCanvas()
        const box = wrapper.findAll('[role="button"]')[0]!
        await box.trigger('keydown', { key: 'ArrowDown' })
        expect(lastLayout(wrapper).paragraphs[0].marginTop).toBe(11)
        await box.trigger('keydown', { key: 'ArrowUp', shiftKey: true })
        expect(lastLayout(wrapper).paragraphs[0].marginTop).toBe(1)
        await box.trigger('keydown', { key: 'ArrowRight' })
        expect(lastLayout(wrapper).paragraphs[0].tabStops[0].position).toBe(262.5)
    })

    it('edits the text on the page', async () => {
        const wrapper = mountCanvas()
        await wrapper.findAll('[role="button"]')[1]!.trigger('dblclick')
        const input = wrapper.find('input')
        expect(input.exists()).toBe(true)
        await input.setValue('changed')
        await input.trigger('keydown', { key: 'Enter' })
        expect(lastLayout(wrapper).paragraphs[1].blocks[0].block.text).toBe('changed')
        expect(wrapper.find('input').exists()).toBe(false)
    })

    it('cancels editing with Escape', async () => {
        const wrapper = mountCanvas()
        await wrapper.findAll('[role="button"]')[1]!.trigger('dblclick')
        await wrapper.find('input').setValue('discarded')
        await wrapper.find('input').trigger('keydown', { key: 'Escape' })
        expect(wrapper.emitted('update:modelValue')).toBeUndefined()
    })

    it('shows an error for invalid JSON', () => {
        const wrapper = mountCanvas('{ nope')
        expect(wrapper.find('[role="alert"]').exists()).toBe(true)
    })
})
