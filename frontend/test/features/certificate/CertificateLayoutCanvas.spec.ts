import { mount } from '@vue/test-utils'
import OpenVue from 'openvue/config'
import { describe, expect, it } from 'vitest'
import { createI18n } from 'vue-i18n'
import CertificateLayoutCanvas from '@/features/certificate/widgets/CertificateLayoutCanvas.vue'
import de from '@/locales/de.json'

// jsdom has no matchMedia, which the PrimeVue color picker and select need
window.matchMedia ??= (() => ({ matches: false, addEventListener: () => {}, removeEventListener: () => {} })) as unknown as typeof window.matchMedia

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
function mountCanvas(modelValue = layout, snap = false) {
    const wrapper = mount(CertificateLayoutCanvas, {
        props: { modelValue, width: 595 },
        global: { plugins: [i18n, OpenVue] },
        attachTo: document.body,
    })
    // snapping would round the pixel deltas of the tests
    if (!snap) {
        ;(wrapper.vm as unknown as { snapOn: boolean }).snapOn = false
    }
    return wrapper
}

function lastLayout(wrapper: ReturnType<typeof mountCanvas>) {
    const emitted = wrapper.emitted('update:modelValue')!
    return JSON.parse(emitted[emitted.length - 1]![0] as string)
}

function drag(wrapper: ReturnType<typeof mountCanvas>, box: ReturnType<typeof wrapper.find>, dx: number, dy: number, altKey = false) {
    return (async () => {
        await box.trigger('pointerdown', { clientX: 100, clientY: 100, pointerId: 1 })
        await wrapper.find('[data-testid="layout-canvas"]').trigger('pointermove', { clientX: 100 + dx, clientY: 100 + dy, pointerId: 1, altKey })
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

    it('keeps the paragraphs below in place when a block is dragged', async () => {
        const wrapper = mountCanvas()
        await drag(wrapper, wrapper.findAll('[role="button"]')[0]!, 0, 30)
        const result = lastLayout(wrapper)
        expect(result.paragraphs[0].marginTop).toBe(40)
        expect(result.paragraphs[1].marginTop).toBe(-26)
    })

    it('pushes the paragraphs below along with Alt', async () => {
        const wrapper = mountCanvas()
        await drag(wrapper, wrapper.findAll('[role="button"]')[0]!, 0, 30, true)
        const result = lastLayout(wrapper)
        expect(result.paragraphs[0].marginTop).toBe(40)
        expect(result.paragraphs[1].marginTop ?? 4).toBe(4)
    })

    it('keeps the paragraphs below in place with the arrow keys, Alt pushes them', async () => {
        const wrapper = mountCanvas()
        const box = wrapper.findAll('[role="button"]')[0]!
        await box.trigger('keydown', { key: 'ArrowDown' })
        expect(lastLayout(wrapper).paragraphs[1].marginTop).toBe(3)
        await box.trigger('keydown', { key: 'ArrowDown', altKey: true })
        expect(lastLayout(wrapper).paragraphs[0].marginTop).toBe(12)
        expect(lastLayout(wrapper).paragraphs[1].marginTop).toBe(3)
    })

    it('allows a negative space above to move upwards', async () => {
        const wrapper = mountCanvas()
        await drag(wrapper, wrapper.findAll('[role="button"]')[0]!, 0, -500)
        expect(lastLayout(wrapper).paragraphs[0].marginTop).toBeLessThan(0)
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

    it('removes the selected block with Delete and its paragraph when it is empty', async () => {
        const wrapper = mountCanvas()
        const boxes = wrapper.findAll('[role="button"]').length
        await wrapper.findAll('[role="button"]')[0]!.trigger('keydown', { key: 'Delete' })
        const after = lastLayout(wrapper)
        expect(wrapper.findAll('[role="button"]').length).toBe(boxes - 1)
        expect(after.paragraphs.every((p: any) => p.blocks.length > 0)).toBe(true)
    })

    it('adds a text paragraph and an image paragraph with the chosen image', async () => {
        const wrapper = mount(CertificateLayoutCanvas, {
            props: { modelValue: layout, width: 595, media: { 'logo.jpg': 'AAAA', 'other.jpg': 'BBBB' } },
            global: { plugins: [i18n, OpenVue] },
            attachTo: document.body,
        })
        const buttons = wrapper.findAll('button')
        await buttons[0]!.trigger('click')
        await buttons[1]!.trigger('click')
        // the dialog is rendered outside the component
        const item = [...document.body.querySelectorAll<HTMLButtonElement>('.picker-item')].find(i => i.textContent?.includes('other.jpg'))!
        item.click()
        await wrapper.vm.$nextTick()
        const last = lastLayout(wrapper).paragraphs.slice(-2)
        expect('text' in last[0].blocks[0].block).toBe(true)
        expect(last[1].blocks[0].block).toEqual({ media: 'other.jpg', width: 100 })
        wrapper.unmount()
    })

    it('scales the font size of a text and the width of an image with the handle', async () => {
        const wrapper = mountCanvas(JSON.stringify({
            paragraphs: [
                { blocks: [{ block: { text: 'abc', fontSize: 20 } }] },
                { blocks: [{ block: { media: 'logo.jpg', width: 100 } }] },
            ],
        }))
        for (const [index, key, expected] of [[0, 'fontSize', (v: number) => v > 20], [1, 'width', (v: number) => v === 150]] as const) {
            await wrapper.findAll('[role="button"]')[index]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
            await wrapper.find('[data-testid="resize-handle"]').trigger('pointerdown', { clientX: 100, clientY: 100, pointerId: 1 })
            await wrapper.find('[data-testid="layout-canvas"]').trigger('pointermove', { clientX: 150, clientY: 100, pointerId: 1 })
            await wrapper.find('[data-testid="layout-canvas"]').trigger('pointerup', { pointerId: 1 })
            expect(expected(lastLayout(wrapper).paragraphs[index].blocks[0].block[key])).toBe(true)
        }
    })

    it('shows the format bar for a selected text and toggles bold', async () => {
        const wrapper = mountCanvas()
        expect(wrapper.find('[aria-pressed="true"]').exists()).toBe(false)
        await wrapper.findAll('[role="button"]')[0]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
        const bold = wrapper.find('[data-testid="format-bar"] button')
        await bold.trigger('click')
        expect(lastLayout(wrapper).paragraphs[0].blocks[0].block.bold).toBe(true)
        await bold.trigger('click')
        expect(lastLayout(wrapper).paragraphs[0].blocks[0].block.bold).toBeUndefined()
    })

    it('adds a block to the line of the selected block and duplicates the line', async () => {
        const wrapper = mountCanvas()
        await wrapper.findAll('[role="button"]')[1]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
        const byLabel = (label: string) => wrapper.findAll('button').find(b => b.text().includes(label))!
        await byLabel('Text zur Zeile').trigger('click')
        const line = lastLayout(wrapper).paragraphs[1].blocks
        expect(line).toHaveLength(3)
        expect(line[2].tabPosition).toBe(2)
        await byLabel('Zeile duplizieren').trigger('click')
        expect(lastLayout(wrapper).paragraphs).toHaveLength(3)
        expect(lastLayout(wrapper).paragraphs[2].blocks).toHaveLength(3)
    })

    it('inserts a placeholder at the cursor while editing', async () => {
        const wrapper = mountCanvas()
        await wrapper.findAll('[role="button"]')[1]!.trigger('dblclick')
        const input = wrapper.find('.canvas-input')
        ;(input.element as HTMLInputElement).setSelectionRange(2, 2)
        await wrapper.findAll('button').find(b => b.text() === 'EVENT_NAME')!.trigger('click')
        expect((input.element as HTMLInputElement).value).toBe('le{{EVENT_NAME}}ft')
    })

    it('selects several blocks with Shift and deletes them together', async () => {
        const wrapper = mountCanvas()
        const boxes = () => wrapper.findAll('[role="button"]')
        await boxes()[0]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
        await boxes()[1]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1, shiftKey: true })
        await boxes()[1]!.trigger('keydown', { key: 'Delete' })
        expect(boxes()).toHaveLength(1)
        expect(lastLayout(wrapper).paragraphs).toHaveLength(1)
    })

    it('moves all selected paragraphs together', async () => {
        const wrapper = mountCanvas()
        const boxes = () => wrapper.findAll('[role="button"]')
        await boxes()[0]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
        await boxes()[2]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1, shiftKey: true })
        await boxes()[2]!.trigger('keydown', { key: 'ArrowDown' })
        const result = lastLayout(wrapper)
        expect(result.paragraphs[0].marginTop).toBe(11)
        // both move, so nothing is left to keep in place
        expect(result.paragraphs[1].marginTop ?? 4).toBe(4)
    })

    it('snaps a dragged block to the page center and shows a guide', async () => {
        const wrapper = mountCanvas(layout, true)
        const page = wrapper.find('[data-testid="layout-canvas"]')
        await wrapper.findAll('[role="button"]')[2]!.trigger('pointerdown', { clientX: 100, clientY: 100, pointerId: 1 })
        // the right edge of the block is at 236, the page center at 297.5: 60 points are 1.5 short
        await page.trigger('pointermove', { clientX: 160, clientY: 100, pointerId: 1 })
        expect(wrapper.find('.canvas-guide-x').exists()).toBe(true)
        await page.trigger('pointerup', { pointerId: 1 })
        expect(wrapper.find('.canvas-guide-x').exists()).toBe(false)
        expect(lastLayout(wrapper).paragraphs[1].tabStops[1].position).toBe(262)
    })

    it('drags the page margins and writes all four', async () => {
        const wrapper = mountCanvas()
        const handle = wrapper.find('[data-testid="margin-left"]')
        await handle.trigger('pointerdown', { clientX: 100, clientY: 100, pointerId: 1 })
        await wrapper.find('[data-testid="layout-canvas"]').trigger('pointermove', { clientX: 130, clientY: 100, pointerId: 1 })
        await wrapper.find('[data-testid="layout-canvas"]').trigger('pointerup', { pointerId: 1 })
        expect(lastLayout(wrapper).document.margins).toEqual({ top: 36, bottom: 36, left: 66, right: 36 })
    })

    it('creates a tab stop for a block on the grid and removes it again', async () => {
        const json = JSON.stringify({ paragraphs: [{ tabStops: [{ position: 10, alignment: 'LEFT' }], blocks: [{ block: { text: 'a' } }, { block: { text: 'b' } }] }] })
        const wrapper = mountCanvas(json)
        await wrapper.findAll('[role="button"]')[1]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
        await wrapper.findAll('button').find(b => b.text().includes('Tabulator hinzufügen'))!.trigger('click')
        expect(lastLayout(wrapper).paragraphs[0].tabStops).toHaveLength(2)
        await wrapper.findAll('button').find(b => b.text().includes('Tabulator entfernen'))!.trigger('click')
        expect(lastLayout(wrapper).paragraphs[0].tabStops).toHaveLength(1)
    })

    it('zooms the page', async () => {
        const wrapper = mountCanvas()
        const width = () => (wrapper.find('[data-testid="layout-canvas"]').element as HTMLElement).style.width
        expect(width()).toBe('595px')
        await wrapper.find('[aria-label="Vergrößern"]').trigger('click')
        expect(width()).toBe('743.75px')
    })

    it('keeps the paragraph below in place when a text is scaled up (Alt pushes it)', async () => {
        for (const altKey of [false, true]) {
            const wrapper = mountCanvas()
            await wrapper.findAll('[role="button"]')[0]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
            await wrapper.find('[data-testid="resize-handle"]').trigger('pointerdown', { clientX: 100, clientY: 100, pointerId: 1 })
            await wrapper.find('[data-testid="layout-canvas"]').trigger('pointermove', { clientX: 200, clientY: 100, pointerId: 1, altKey })
            const result = lastLayout(wrapper)
            expect(result.paragraphs[0].blocks[0].block.fontSize).toBeGreaterThan(20)
            if (altKey) {
                expect(result.paragraphs[1].marginTop).toBeUndefined()
            }
            else {
                expect(result.paragraphs[1].marginTop).toBeLessThan(4)
            }
        }
    })

    it('selects the blocks inside a frame pulled on the empty page and clears on a plain click', async () => {
        const wrapper = mountCanvas()
        const page = wrapper.find('[data-testid="layout-canvas"]')
        await page.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
        await page.trigger('pointermove', { clientX: 595, clientY: 400, pointerId: 1 })
        await page.trigger('pointerup', { pointerId: 1 })
        expect(wrapper.findAll('[aria-pressed="true"]').length).toBeGreaterThan(1)
        await page.trigger('pointerdown', { clientX: 590, clientY: 840, pointerId: 1 })
        await page.trigger('pointerup', { pointerId: 1 })
        expect(wrapper.findAll('[aria-pressed="true"]')).toHaveLength(0)
    })

    it('snaps the right edge of an image to the page center while scaling', async () => {
        const json = JSON.stringify({ paragraphs: [{ tabStops: [{ position: 100, alignment: 'LEFT' }], blocks: [{ block: { media: 'logo.jpg', width: 100 } }] }] })
        const wrapper = mountCanvas(json, true)
        await wrapper.findAll('[role="button"]')[0]!.trigger('pointerdown', { clientX: 0, clientY: 0, pointerId: 1 })
        await wrapper.find('[data-testid="resize-handle"]').trigger('pointerdown', { clientX: 100, clientY: 100, pointerId: 1 })
        // the right edge is at 36 + 100 + 100 = 236, the page center at 297.5: 60 points are 1.5 short
        await wrapper.find('[data-testid="layout-canvas"]').trigger('pointermove', { clientX: 160, clientY: 100, pointerId: 1 })
        expect(lastLayout(wrapper).paragraphs[0].blocks[0].block.width).toBe(162)
    })

    it('edits the text on the page', async () => {
        const wrapper = mountCanvas()
        await wrapper.findAll('[role="button"]')[1]!.trigger('dblclick')
        const input = wrapper.find('.canvas-input')
        expect(input.exists()).toBe(true)
        await input.setValue('changed')
        await input.trigger('keydown', { key: 'Enter' })
        expect(lastLayout(wrapper).paragraphs[1].blocks[0].block.text).toBe('changed')
        expect(wrapper.find('.canvas-input').exists()).toBe(false)
    })

    it('cancels editing with Escape', async () => {
        const wrapper = mountCanvas()
        await wrapper.findAll('[role="button"]')[1]!.trigger('dblclick')
        await wrapper.find('.canvas-input').setValue('discarded')
        await wrapper.find('.canvas-input').trigger('keydown', { key: 'Escape' })
        expect(wrapper.emitted('update:modelValue')).toBeUndefined()
    })

    it('shows an error for invalid JSON', () => {
        const wrapper = mountCanvas('{ nope')
        expect(wrapper.find('[role="alert"]').exists()).toBe(true)
    })
})
