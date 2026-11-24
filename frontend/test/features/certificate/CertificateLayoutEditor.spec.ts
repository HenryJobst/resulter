import { mount } from '@vue/test-utils'
import OpenVue from 'openvue/config'
import { describe, expect, it } from 'vitest'
import { createI18n } from 'vue-i18n'
import CertificateLayoutEditor from '@/features/certificate/widgets/CertificateLayoutEditor.vue'
import de from '@/locales/de.json'

const i18n = createI18n({ legacy: false, locale: 'de', messages: { de } })

const layout = JSON.stringify({
    paragraphs: [{ marginTop: 10, blocks: [{ block: { text: 'Hallo {{GIVEN_NAME}}', fontSize: 20 } }] }],
})

function mountEditor(modelValue: string, attach = false) {
    return mount(CertificateLayoutEditor, {
        attachTo: attach ? document.body : undefined,
        props: { modelValue, mediaFiles: ['logo.png'] },
        global: {
            plugins: [i18n, OpenVue],
            // overlay based components need browser APIs that jsdom does not have
            stubs: { Select: true, ColorPicker: true, Fieldset: true },
        },
    })
}

describe('certificateLayoutEditor', () => {
    it('shows the paragraphs of a valid layout without emitting anything', async () => {
        const wrapper = mountEditor(layout)
        await wrapper.vm.$nextTick()
        expect(wrapper.text()).toContain('Absatz 1')
        expect(wrapper.emitted('update:modelValue')).toBeUndefined()
    })

    it('shows an error for invalid JSON', () => {
        const wrapper = mountEditor('{ nope')
        expect(wrapper.find('[role="alert"]').exists()).toBe(true)
    })

    it('emits the changed layout when a placeholder is inserted', async () => {
        const wrapper = mountEditor(layout)
        const button = wrapper.findAll('button').find(b => b.text() === 'FAMILY_NAME')
        expect(button).toBeDefined()
        await button!.trigger('click')
        const emitted = wrapper.emitted('update:modelValue')
        expect(emitted).toHaveLength(1)
        const result = JSON.parse(emitted![0]![0] as string)
        expect(result.paragraphs[0].blocks[0].block.text).toBe('Hallo {{GIVEN_NAME}}{{FAMILY_NAME}}')
        expect(result.paragraphs[0].marginTop).toBe(10)
    })

    it('inserts a placeholder at the cursor position', async () => {
        const wrapper = mountEditor(layout, true)
        const input = wrapper.findAll('input').find(i => (i.element as HTMLInputElement).value === 'Hallo {{GIVEN_NAME}}')!
        const el = input.element as HTMLInputElement
        el.focus()
        el.setSelectionRange(5, 5)
        const button = wrapper.findAll('button').find(b => b.text() === 'FAMILY_NAME')
        await button!.trigger('click')
        await wrapper.vm.$nextTick()
        const result = JSON.parse(wrapper.emitted('update:modelValue')![0]![0] as string)
        expect(result.paragraphs[0].blocks[0].block.text).toBe('Hallo{{FAMILY_NAME}} {{GIVEN_NAME}}')
        expect(el.selectionStart).toBe(5 + '{{FAMILY_NAME}}'.length)
    })

    it('adds a paragraph', async () => {
        const wrapper = mountEditor('')
        const add = wrapper.findAll('button').find(b => b.text() === 'Absatz hinzufügen')
        await add!.trigger('click')
        const result = JSON.parse(wrapper.emitted('update:modelValue')![0]![0] as string)
        expect(result.paragraphs).toHaveLength(1)
        expect(result.paragraphs[0].blocks).toHaveLength(1)
    })
})
