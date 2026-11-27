<script setup lang="ts">
import type { CanvasBox } from '@/features/certificate/model/certificateCanvas'
import type { BlockEntry, LayoutParagraph, MediaBlock, TabAlignment, TextBlock } from '@/features/certificate/model/certificateLayout'
import Button from 'openvue/button'
import Checkbox from 'openvue/checkbox'
import ColorPicker from 'openvue/colorpicker'
import Dialog from 'openvue/dialog'
import InputNumber from 'openvue/inputnumber'
import Select from 'openvue/select'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, toRaw, watchEffect } from 'vue'
import { useI18n } from 'vue-i18n'
import {
    computeBoxes,
    DEFAULT_PARAGRAPH_SPACE,
    DEFAULT_TAB_STOP,
    estimateWidth,
    fontFamilyFor,
    isBoldFont,
    isItalicFont,
    sampleText,
} from '@/features/certificate/model/certificateCanvas'
import { effectiveMargins, ensureDocumentMargins, FONTS, hexToRgb, isMediaBlock, newTextBlockEntry, PAGE_HEIGHT, PAGE_WIDTH, PLACEHOLDERS, rgbToHex, setOptional, TAB_ALIGNMENTS } from '@/features/certificate/model/certificateLayout'
import { paragraphMargins, shiftParagraphs, snapShift } from '@/features/certificate/model/certificateMove'
import { useLayoutSync } from '@/features/certificate/model/useLayoutSync'
import '@/features/certificate/assets/certificateFonts.css'

const props = withDefaults(
    defineProps<{
        modelValue: string
        /** base64 JPEG of the background image, shown behind the text */
        background?: string | null
        /** base64 thumbnails of the media files by file name, used to show image blocks */
        media?: Record<string, string>
        /** width of the page on screen in px */
        width?: number
    }>(),
    { background: null, media: () => ({}), width: 460 },
)

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const selected = defineModel<{ pi: number, bi: number } | null>('selected', { default: null })

const { t } = useI18n()

const { layout, error } = useLayoutSync(
    () => props.modelValue,
    value => emit('update:modelValue', value),
)

// editing the text directly on the page
const editing = ref<{ pi: number, bi: number } | null>(null)
const editText = ref('')
const editInput = shallowRef<HTMLInputElement | null>(null)

const zoom = ref(1)
const scale = computed(() => (props.width * zoom.value) / PAGE_WIDTH)
const pageHeight = computed(() => PAGE_HEIGHT * scale.value)
// height / width of the images, read from the thumbnails; the PDF keeps the aspect of an image
const aspects = ref<Record<string, number>>({})
watchEffect(() => {
    for (const [name, data] of Object.entries(props.media)) {
        const image = new Image()
        image.onload = () => {
            if (image.naturalWidth > 0) {
                aspects.value = { ...aspects.value, [name]: image.naturalHeight / image.naturalWidth }
            }
        }
        image.src = `data:image/jpeg;base64,${data}`
    }
})
// Text widths are measured with the real fonts; they are loaded when the first text uses them,
// so measure again when the browser has finished loading a font.
const fontsLoaded = ref(0)
const onFontsLoaded = () => fontsLoaded.value++
onMounted(() => document.fonts?.addEventListener('loadingdone', onFontsLoaded))
onBeforeUnmount(() => document.fonts?.removeEventListener('loadingdone', onFontsLoaded))
let measureContext: CanvasRenderingContext2D | null | undefined
function measure(text: string, fontSize: number, font: string | undefined, bold: boolean, italic: boolean) {
    // without a browser (tests) there is no font loading and no canvas: use the estimate
    if (!document.fonts) {
        return estimateWidth(text, fontSize, font, bold)
    }
    measureContext ??= document.createElement('canvas').getContext('2d')
    if (!measureContext) {
        return estimateWidth(text, fontSize, font, bold)
    }
    measureContext.font = `${italic ? 'italic' : 'normal'} ${bold ? 'bold' : 'normal'} ${fontSize}px ${fontFamilyFor(font)}`
    return measureContext.measureText(text).width
}
const boxes = computed(() => {
    void fontsLoaded.value
    return layout.value ? computeBoxes(layout.value, aspects.value, measure) : []
})
const margins = computed(() => (layout.value ? effectiveMargins(layout.value) : null))

const pageStyle = computed(() => ({
    width: `${PAGE_WIDTH * scale.value}px`,
    height: `${pageHeight.value}px`,
    backgroundImage: props.background ? `url(data:image/jpeg;base64,${props.background})` : 'none',
}))

const marginStyle = computed(() => {
    const m = margins.value
    const s = scale.value
    return m
        ? {
                left: `${m.left * s}px`,
                top: `${m.top * s}px`,
                right: `${m.right * s}px`,
                bottom: `${m.bottom * s}px`,
            }
        : {}
})

// the browser puts the baseline in the middle of the line (half-leading), iText near the top:
// shift the box so that both baselines coincide (a typical sans-serif has ascent .905, descent .212)
function baselineShift(box: CanvasBox) {
    if (box.kind === 'media') {
        return 0
    }
    return box.baseline - ((box.lineHeight - 1.117 * box.fontSize) / 2 + 0.905 * box.fontSize)
}

function boxStyle(box: CanvasBox) {
    const s = scale.value
    const shift = box.anchor === 'center' ? '-50%' : box.anchor === 'right' ? '-100%' : '0'
    return {
        left: `${box.x * s}px`,
        top: `${(box.y + baselineShift(box)) * s}px`,
        transform: `translateX(${shift})`,
        fontSize: `${box.fontSize * s}px`,
        lineHeight: `${box.lineHeight * s}px`,
        fontFamily: fontFamilyFor(box.font),
        fontWeight: isBoldFont(box.font) ? 'bold' : 'normal',
        fontStyle: isItalicFont(box.font) ? 'italic' : 'normal',
        // like iText: bold is a stroke of 1/30 em, italic a slant of about 12 degrees
        ...(box.simulatedBold ? { WebkitTextStroke: `${(box.fontSize * s) / 30}px currentColor` } : {}),
        ...(box.simulatedItalic ? { transform: `translateX(${shift}) skewX(-12deg)`, transformOrigin: '50% 75%' } : {}),
        color: box.color,
        ...(box.kind === 'media' ? { width: `${box.width * s}px`, height: `${box.height * s}px` } : {}),
    }
}

function ariaLabel(box: CanvasBox) {
    const where = t('certificateLayout.canvas_block', { p: box.pi + 1, b: box.bi + 1 })
    return `${where}: ${box.kind === 'media' ? box.media : box.text}`
}

// ---- selection: `selected` is the main block, Shift+click adds or removes blocks
const keyOf = (pi: number, bi: number) => `${pi}-${bi}`
const marked = ref<Set<string>>(new Set())
const selection = computed(() => {
    const main = selected.value
    if (!main) {
        return new Set<string>()
    }
    const key = keyOf(main.pi, main.bi)
    return marked.value.has(key) ? marked.value : new Set([key])
})
const isSelected = (box: CanvasBox) => selection.value.has(keyOf(box.pi, box.bi))
const selectedBoxes = computed(() => boxes.value.filter(isSelected))

// ---- moving: vertical = space above the paragraph, horizontal = position of the tab stop
interface Drag {
    box: CanvasBox
    startX: number
    startY: number
    axis: 'x' | 'y' | null
    margins: number[]
    moved: Set<number>
    stops: { pi: number, index: number, position: number }[]
    /** lines the moved block snaps to, in points */
    targetsX: number[]
    targetsY: number[]
}
let drag: Drag | null = null

const snapOn = ref(true)
// pulling a frame on the empty page selects the blocks it touches (Shift adds to the selection)
const marquee = ref<{ x0: number, y0: number, x1: number, y1: number, add: boolean } | null>(null)
// guide lines (points) while a block snaps
const guideX = ref<number | null>(null)
const guideY = ref<number | null>(null)
const SNAP_PIXELS = 6

function tabPosition(paragraph: LayoutParagraph, index: number | null): number {
    return index === null ? 0 : (paragraph.tabStops?.[index] ?? DEFAULT_TAB_STOP).position
}

function setTabPosition(paragraph: LayoutParagraph, index: number, position: number) {
    // the default stop only exists implicitly, write it down before changing it
    paragraph.tabStops ??= [{ ...DEFAULT_TAB_STOP }]
    paragraph.tabStops[index]!.position = position
}

/** The tab stops (once each) the given blocks sit on. */
function stopsOf(list: CanvasBox[]) {
    const seen = new Set<string>()
    const result: { pi: number, index: number, position: number }[] = []
    for (const box of list) {
        const key = `${box.pi}:${box.tabIndex}`
        if (box.tabIndex !== null && !seen.has(key)) {
            seen.add(key)
            result.push({ pi: box.pi, index: box.tabIndex, position: tabPosition(layout.value!.paragraphs[box.pi]!, box.tabIndex) })
        }
    }
    return result
}

function edgesX(b: CanvasBox) {
    const left = b.anchor === 'center' ? b.x - b.width / 2 : b.anchor === 'right' ? b.x - b.width : b.x
    return [left, left + b.width / 2, left + b.width]
}
function snapTargetsX(others: CanvasBox[]) {
    return [margins.value!.left, PAGE_WIDTH / 2, PAGE_WIDTH - margins.value!.right, ...others.flatMap(edgesX)]
}
const edgesY = (b: CanvasBox) => [b.y, b.y + b.height / 2, b.y + b.height]

function onPointerDown(event: PointerEvent, box: CanvasBox) {
    if (editing.value || !layout.value) {
        return
    }
    const key = keyOf(box.pi, box.bi)
    if (event.shiftKey) {
        const next = new Set(selection.value)
        if (next.has(key)) {
            next.delete(key)
        }
        else {
            next.add(key)
        }
        marked.value = next
        const first = next.has(key) ? key : [...next][0]
        const [pi, bi] = first ? first.split('-').map(Number) : []
        selected.value = first ? { pi: pi!, bi: bi! } : null
        return
    }
    const keys = selection.value.has(key) ? selection.value : new Set([key])
    marked.value = keys
    selected.value = { pi: box.pi, bi: box.bi }
    const moving = boxes.value.filter(b => keys.has(keyOf(b.pi, b.bi)))
    const moved = new Set(moving.map(b => b.pi))
    const others = boxes.value.filter(b => !moved.has(b.pi))
    drag = {
        box,
        startX: event.clientX,
        startY: event.clientY,
        axis: null,
        margins: paragraphMargins(layout.value),
        moved,
        stops: stopsOf(moving),
        targetsX: snapTargetsX(others),
        targetsY: [margins.value!.top, PAGE_HEIGHT / 2, PAGE_HEIGHT - margins.value!.bottom, ...others.flatMap(edgesY)],
    }
    ;(event.currentTarget as HTMLElement).setPointerCapture?.(event.pointerId)
}

/** The shift in points for a raw move, snapped to the targets (and the guide line). */
function snapped(raw: number, edges: number[], targets: number[]): { delta: number, guide: number | null } {
    if (!snapOn.value) {
        return { delta: raw, guide: null }
    }
    const { shift, guide } = snapShift(edges.map(e => e + raw), targets, SNAP_PIXELS / scale.value)
    return { delta: raw + shift, guide }
}

// scaling the selected block with the handle at its bottom right: an image gets wider (the PDF
// keeps its aspect), a text gets a larger font in proportion to the way the handle was pulled.
// The paragraph below keeps its place (Alt: it moves along).
interface Resize { box: CanvasBox, startX: number, start: number, nextMargin: number, nextY: number, targetsX: number[] }
let resize: Resize | null = null

function onResizeDown(event: PointerEvent, box: CanvasBox) {
    const block = layout.value?.paragraphs[box.pi]?.blocks[box.bi]?.block
    if (!block) {
        return
    }
    const start = isMediaBlock(block) ? (block.width ?? 100) : (block.fontSize ?? 12)
    resize = {
        box,
        startX: event.clientX,
        start,
        nextMargin: layout.value!.paragraphs[box.pi + 1]?.marginTop ?? DEFAULT_PARAGRAPH_SPACE,
        nextY: boxes.value.find(b => b.pi === box.pi + 1)?.y ?? 0,
        targetsX: snapTargetsX(boxes.value.filter(b => b.pi !== box.pi || b.bi !== box.bi)),
    }
    ;(event.currentTarget as HTMLElement).setPointerCapture?.(event.pointerId)
}

function onResizeMove(event: PointerEvent) {
    const { box, startX, start, nextMargin, nextY, targetsX } = resize!
    const block = layout.value?.paragraphs[box.pi]?.blocks[box.bi]?.block
    if (!block) {
        return
    }
    // the right edge of the block is the handle that snaps
    const { delta: dx, guide } = snapped((event.clientX - startX) / scale.value, [edgesX(box)[2]!], targetsX)
    guideX.value = guide
    if (isMediaBlock(block)) {
        block.width = Math.max(1, Math.round(start + dx))
    }
    else {
        const factor = (box.width + dx) / Math.max(1, box.width)
        block.fontSize = Math.max(1, Math.round(start * factor))
    }
    const next = layout.value!.paragraphs[box.pi + 1]
    if (next && !event.altKey) {
        // lay the page out once more to see how far the paragraph below moved
        next.marginTop = nextMargin
        const moved = computeBoxes(layout.value!, aspects.value, measure).find(b => b.pi === box.pi + 1)
        if (moved) {
            next.marginTop = nextMargin - Math.round(moved.y - nextY)
        }
    }
}

// dragging the dashed page margins
type Side = 'top' | 'bottom' | 'left' | 'right'
let marginDrag: { side: Side, startX: number, startY: number, start: number } | null = null

function onMarginDown(event: PointerEvent, side: Side) {
    if (!layout.value) {
        return
    }
    marginDrag = { side, startX: event.clientX, startY: event.clientY, start: effectiveMargins(layout.value)[side] }
    ;(event.currentTarget as HTMLElement).setPointerCapture?.(event.pointerId)
}

function onMarginMove(event: PointerEvent) {
    const { side, startX, startY, start } = marginDrag!
    const pixels = side === 'left'
        ? event.clientX - startX
        : side === 'right'
            ? startX - event.clientX
            : side === 'top' ? event.clientY - startY : startY - event.clientY
    // the backend needs all four margins, so they are all written down on the first change
    ensureDocumentMargins(layout.value!)
    layout.value!.document!.margins![side] = start + Math.round(pixels / scale.value)
}

function onPointerMove(event: PointerEvent) {
    if (marquee.value) {
        onMarqueeMove(event)
        return
    }
    if (marginDrag) {
        onMarginMove(event)
        return
    }
    if (resize) {
        onResizeMove(event)
        return
    }
    if (!drag || !layout.value) {
        return
    }
    const dx = event.clientX - drag.startX
    const dy = event.clientY - drag.startY
    if (drag.axis === null) {
        if (Math.max(Math.abs(dx), Math.abs(dy)) < 4) {
            return
        }
        // one axis per gesture, so a slightly crooked drag does not change both
        drag.axis = Math.abs(dx) > Math.abs(dy) && drag.stops.length > 0 ? 'x' : 'y'
    }
    if (drag.axis === 'y') {
        const { delta, guide } = snapped(dy / scale.value, edgesY(drag.box), drag.targetsY)
        guideY.value = guide
        shiftParagraphs(layout.value, drag.margins, drag.moved, Math.round(delta), event.altKey)
    }
    else {
        const { delta, guide } = snapped(dx / scale.value, edgesX(drag.box), drag.targetsX)
        guideX.value = guide
        for (const stop of drag.stops) {
            setTabPosition(layout.value.paragraphs[stop.pi]!, stop.index, stop.position + Math.round(delta))
        }
    }
}

function onPageDown(event: PointerEvent) {
    if (event.target !== event.currentTarget) {
        return
    }
    const page = (event.currentTarget as HTMLElement).getBoundingClientRect()
    const x = event.clientX - page.left
    const y = event.clientY - page.top
    marquee.value = { x0: x, y0: y, x1: x, y1: y, add: event.shiftKey }
    ;(event.currentTarget as HTMLElement).setPointerCapture?.(event.pointerId)
}

function onMarqueeMove(event: PointerEvent) {
    const page = (event.currentTarget as HTMLElement).getBoundingClientRect()
    marquee.value = { ...marquee.value!, x1: event.clientX - page.left, y1: event.clientY - page.top }
}

function onMarqueeEnd() {
    const frame = marquee.value!
    marquee.value = null
    const s = scale.value
    const [left, right] = [Math.min(frame.x0, frame.x1), Math.max(frame.x0, frame.x1)]
    const [top, bottom] = [Math.min(frame.y0, frame.y1), Math.max(frame.y0, frame.y1)]
    const hit = boxes.value.filter((b) => {
        const [bl, , br] = edgesX(b)
        return bl! * s <= right && br! * s >= left && b.y * s <= bottom && (b.y + b.height) * s >= top
    })
    const keys = new Set(frame.add ? selection.value : [])
    hit.forEach(b => keys.add(keyOf(b.pi, b.bi)))
    marked.value = keys
    if (hit.length > 0) {
        selected.value = { pi: hit[0]!.pi, bi: hit[0]!.bi }
    }
    else if (!frame.add) {
        selected.value = null
    }
}

function onPointerUp() {
    if (marquee.value) {
        onMarqueeEnd()
    }
    drag = null
    resize = null
    marginDrag = null
    guideX.value = null
    guideY.value = null
}

function onKeyDown(event: KeyboardEvent, box: CanvasBox) {
    if (editing.value || !layout.value) {
        return
    }
    const step = event.shiftKey ? 10 : 1
    const own = selection.value.has(keyOf(box.pi, box.bi)) ? selectedBoxes.value : [box]
    if (event.key === 'ArrowUp' || event.key === 'ArrowDown') {
        const delta = event.key === 'ArrowUp' ? -step : step
        shiftParagraphs(layout.value, paragraphMargins(layout.value), new Set(own.map(b => b.pi)), delta, event.altKey)
    }
    else if ((event.key === 'ArrowLeft' || event.key === 'ArrowRight') && stopsOf(own).length > 0) {
        const delta = event.key === 'ArrowLeft' ? -step : step
        for (const stop of stopsOf(own)) {
            setTabPosition(layout.value.paragraphs[stop.pi]!, stop.index, stop.position + delta)
        }
    }
    else if (event.key === 'Enter' && box.kind === 'text') {
        startEdit(box)
    }
    else if (event.key === 'Delete' || event.key === 'Backspace') {
        removeBlocks(own)
    }
    else {
        return
    }
    event.preventDefault()
}

function removeBlocks(list: CanvasBox[]) {
    const paragraphs = layout.value!.paragraphs
    // from the back, so the indexes of the remaining ones stay valid
    for (const box of [...list].sort((a, b) => b.pi - a.pi || b.bi - a.bi)) {
        const paragraph = paragraphs[box.pi]!
        paragraph.blocks.splice(box.bi, 1)
        // an empty paragraph is invalid (and invisible), so it goes with its last block
        if (paragraph.blocks.length === 0) {
            paragraphs.splice(box.pi, 1)
        }
    }
    selected.value = null
}

// ---- tab stops of the selected block's paragraph
const mainBox = computed(() => boxes.value.find(b => selected.value && b.pi === selected.value.pi && b.bi === selected.value.bi) ?? null)
const stopMarkers = computed(() => {
    const box = mainBox.value
    const paragraph = box && layout.value?.paragraphs[box.pi]
    if (!box || !paragraph || !margins.value) {
        return []
    }
    const own = boxes.value.filter(b => b.pi === box.pi)
    const top = Math.min(...own.map(b => b.y))
    const bottom = Math.max(...own.map(b => b.y + b.height))
    const left = margins.value.left + (paragraph.marginLeft ?? 0)
    return (paragraph.tabStops ?? [DEFAULT_TAB_STOP]).map(s => ({ x: left + s.position, top, height: bottom - top }))
})

/** A block on the 50 grid gets its own tab stop at the place where it sits. */
function createTabStop() {
    const box = mainBox.value
    const paragraph = box && layout.value?.paragraphs[box.pi]
    if (!box || !paragraph || !margins.value || box.tabIndex !== null) {
        return
    }
    paragraph.tabStops ??= [{ ...DEFAULT_TAB_STOP }]
    paragraph.tabStops.push({ position: Math.round(box.x - margins.value.left - (paragraph.marginLeft ?? 0)), alignment: 'LEFT' })
}

/** Removes the tab stop of the selected block; its blocks move to the next stop. */
function removeTabStop() {
    const box = mainBox.value
    const paragraph = box && layout.value?.paragraphs[box.pi]
    if (!box || !paragraph?.tabStops || box.tabIndex === null) {
        return
    }
    paragraph.tabStops.splice(box.tabIndex, 1)
    if (paragraph.tabStops.length === 0) {
        delete paragraph.tabStops
    }
}

// ---- format bar for the selected text blocks (a change applies to all of them)
const selectedTextBlocks = computed(() =>
    selectedBoxes.value
        .filter(b => b.kind === 'text')
        .map(b => layout.value!.paragraphs[b.pi]!.blocks[b.bi]!.block as TextBlock))
const selectedText = computed(() => selectedTextBlocks.value[0] ?? null)
const selectedTabIndex = computed(() => mainBox.value?.tabIndex ?? null)
const fontOptions = [{ id: '', label: '–' }, ...FONTS.map(f => ({ id: f, label: f }))]
const alignmentOptions = computed(() =>
    TAB_ALIGNMENTS.map(a => ({ id: a, label: t(`certificateLayout.alignments.${a}`) })))

function applyText(change: (block: TextBlock) => void) {
    selectedTextBlocks.value.forEach(change)
}

function setAlignment(alignment: TabAlignment) {
    for (const stop of stopsOf(selectedBoxes.value)) {
        const paragraph = layout.value!.paragraphs[stop.pi]!
        // the default stop only exists implicitly, write it down before changing it
        paragraph.tabStops ??= [{ ...DEFAULT_TAB_STOP }]
        paragraph.tabStops[stop.index]!.alignment = alignment
    }
}
const currentAlignment = computed(() => {
    const box = mainBox.value
    const paragraph = box && layout.value?.paragraphs[box.pi]
    return paragraph && box.tabIndex !== null ? (paragraph.tabStops ?? [DEFAULT_TAB_STOP])[box.tabIndex]?.alignment : undefined
})

// choosing the image of an image block: for a new block or for the one that was double-clicked
const picker = ref<{ pi: number, bi: number } | 'new' | 'line' | null>(null)
const pickerVisible = computed({
    get: () => picker.value !== null,
    set: (value: boolean) => {
        if (!value) {
            picker.value = null
        }
    },
})

function pickImage(fileName: string) {
    const target = picker.value
    picker.value = null
    if (!layout.value || !target) {
        return
    }
    if (target === 'new') {
        addParagraph({ block: { media: fileName, width: 100 } })
        return
    }
    if (target === 'line') {
        addToLine({ block: { media: fileName, width: 100 } })
        return
    }
    const block = layout.value.paragraphs[target.pi]?.blocks[target.bi]?.block
    if (block && isMediaBlock(block)) {
        ;(block as MediaBlock).media = fileName
    }
}

/** Puts a block behind the others of the selected block's paragraph (on the next tab stop). */
function addToLine(entry: BlockEntry) {
    const paragraph = layout.value?.paragraphs[selected.value?.pi ?? -1]
    if (!paragraph) {
        return
    }
    entry.tabPosition = Math.max(0, ...paragraph.blocks.map(b => b.tabPosition ?? 1)) + 1
    paragraph.blocks.push(entry)
    selected.value = { pi: selected.value!.pi, bi: paragraph.blocks.length - 1 }
}

/** Copies the paragraph of the selected block right behind it. */
function duplicateLine() {
    const pi = selected.value?.pi ?? -1
    const paragraph = layout.value?.paragraphs[pi]
    if (paragraph) {
        layout.value!.paragraphs.splice(pi + 1, 0, structuredClone(toRaw(paragraph)))
        selected.value = { pi: pi + 1, bi: selected.value!.bi }
    }
}

/** Inserts a placeholder at the cursor of the text that is being edited. */
function insertPlaceholder(name: string) {
    const input = editInput.value
    const token = `{{${name}}}`
    const start = input?.selectionStart ?? editText.value.length
    const end = input?.selectionEnd ?? start
    editText.value = editText.value.slice(0, start) + token + editText.value.slice(end)
    nextTick(() => {
        input?.focus()
        input?.setSelectionRange(start + token.length, start + token.length)
    })
}

/** A new paragraph with one block at the end; it is selected so it can be moved right away. */
function addParagraph(entry: BlockEntry) {
    if (!layout.value) {
        return
    }
    layout.value.paragraphs.push({ blocks: [entry] })
    selected.value = { pi: layout.value.paragraphs.length - 1, bi: 0 }
}

const isEditing = (box: CanvasBox) => editing.value?.pi === box.pi && editing.value?.bi === box.bi

function startEdit(box: CanvasBox) {
    if (box.kind === 'media') {
        selected.value = { pi: box.pi, bi: box.bi }
        picker.value = { pi: box.pi, bi: box.bi }
        return
    }
    if (!layout.value) {
        return
    }
    selected.value = { pi: box.pi, bi: box.bi }
    editText.value = box.text
    editing.value = { pi: box.pi, bi: box.bi }
    nextTick(() => editInput.value?.focus())
}

function commitEdit() {
    const target = editing.value
    editing.value = null
    if (target && layout.value) {
        const block = layout.value.paragraphs[target.pi]?.blocks[target.bi]?.block as TextBlock | undefined
        if (block && block.text !== editText.value) {
            block.text = editText.value
        }
    }
}

function cancelEdit() {
    editing.value = null
}
</script>

<template>
    <div v-if="error" class="text-red-500" role="alert">
        {{ t('certificateLayout.invalid_json', { error }) }}
    </div>
    <div v-else-if="layout" class="flex flex-col gap-1 min-w-0 max-w-full">
        <details class="text-sm" data-testid="canvas-help">
            <summary class="cursor-pointer">
                {{ t('certificateLayout.canvas_hint') }}
            </summary>
            <ul class="list-disc pl-5 mt-1">
                <li v-for="n in 8" :key="n">
                    {{ t(`certificateLayout.canvas_help_${n}`) }}
                </li>
            </ul>
        </details>
        <!-- The two tool rows always have the same height (controls are disabled, not hidden), else the
             page would jump under the pointer when a block gets selected. -->
        <div class="flex flex-row flex-nowrap items-center gap-2 min-w-0" style="overflow-x: auto" data-testid="add-bar">
            <Button
                :label="t('certificateLayout.canvas_add_text_short')"
                icon="pi pi-plus"
                size="small"
                outlined
                class="flex-shrink-0"
                :aria-label="t('certificateLayout.canvas_add_text')"
                :title="t('certificateLayout.canvas_add_text')"
                @click="addParagraph(newTextBlockEntry())"
            />
            <Button
                :label="t('certificateLayout.canvas_add_image_short')"
                icon="pi pi-plus"
                size="small"
                outlined
                class="flex-shrink-0"
                :aria-label="t('certificateLayout.canvas_add_image')"
                :title="t('certificateLayout.canvas_add_image')"
                @click="picker = 'new'"
            />
            <span class="text-sm flex-shrink-0 ml-2">{{ t('certificateLayout.to_line') }}</span>
            <Button
                :label="t('certificateLayout.canvas_add_text_short')"
                icon="pi pi-plus"
                size="small"
                outlined
                class="flex-shrink-0"
                :disabled="!selected"
                :aria-label="t('certificateLayout.add_text_to_line')"
                :title="t('certificateLayout.add_text_to_line')"
                @click="addToLine(newTextBlockEntry())"
            />
            <Button
                :label="t('certificateLayout.canvas_add_image_short')"
                icon="pi pi-plus"
                size="small"
                outlined
                class="flex-shrink-0"
                :disabled="!selected"
                :aria-label="t('certificateLayout.add_image_to_line')"
                :title="t('certificateLayout.add_image_to_line')"
                @click="picker = 'line'"
            />
            <Button
                icon="pi pi-copy"
                size="small"
                outlined
                class="flex-shrink-0"
                :disabled="!selected"
                :aria-label="t('certificateLayout.duplicate_line')"
                :title="t('certificateLayout.duplicate_line')"
                @click="duplicateLine"
            />
        </div>
        <div
            class="flex flex-row flex-nowrap items-center gap-2 min-w-0"
            style="overflow-x: auto; min-height: 2.75rem"
            data-testid="format-bar"
        >
            <div v-if="editing" class="flex flex-row flex-nowrap gap-2">
                <Button
                    v-for="name in PLACEHOLDERS"
                    :key="name"
                    :label="name"
                    size="small"
                    severity="secondary"
                    outlined
                    class="flex-shrink-0"
                    @pointerdown.prevent
                    @click="insertPlaceholder(name)"
                />
            </div>
            <template v-else>
                <Button
                    :label="t('certificateLayout.bold_short')"
                    size="small"
                    class="flex-shrink-0 font-bold"
                    :outlined="!selectedText?.bold"
                    :disabled="!selectedText"
                    :aria-label="t('certificateLayout.bold')"
                    :aria-pressed="selectedText?.bold === true"
                    :title="t('certificateLayout.bold')"
                    @click="applyText(b => setOptional(b, 'bold', selectedText!.bold ? null : true))"
                />
                <Button
                    :label="t('certificateLayout.italic_short')"
                    size="small"
                    class="flex-shrink-0 italic"
                    :outlined="!selectedText?.italic"
                    :disabled="!selectedText"
                    :aria-label="t('certificateLayout.italic')"
                    :aria-pressed="selectedText?.italic === true"
                    :title="t('certificateLayout.italic')"
                    @click="applyText(b => setOptional(b, 'italic', selectedText!.italic ? null : true))"
                />
                <InputNumber
                    :model-value="selectedText?.fontSize ?? 12"
                    :min="1"
                    :use-grouping="false"
                    :disabled="!selectedText"
                    :aria-label="t('certificateLayout.font_size')"
                    :title="t('certificateLayout.font_size')"
                    input-class="w-4rem"
                    class="flex-shrink-0"
                    @update:model-value="(v: number | null) => applyText(b => setOptional(b, 'fontSize', v))"
                />
                <Select
                    :model-value="selectedText?.font ?? ''"
                    :options="fontOptions"
                    option-label="label"
                    option-value="id"
                    :disabled="!selectedText"
                    :aria-label="t('certificateLayout.font')"
                    :title="t('certificateLayout.font')"
                    class="flex-shrink-0"
                    style="width: 9rem"
                    @update:model-value="(v: string) => applyText(b => setOptional(b, 'font', v))"
                />
                <ColorPicker
                    :model-value="rgbToHex(selectedText?.fontRGBColor ?? { r: 0, g: 0, b: 0 })"
                    format="hex"
                    :disabled="!selectedText"
                    :aria-label="t('certificateLayout.color')"
                    class="flex-shrink-0"
                    @update:model-value="(v: string | undefined) => v && applyText(b => (b.fontRGBColor = hexToRgb(v)))"
                />
                <Select
                    :model-value="currentAlignment"
                    :options="alignmentOptions"
                    option-label="label"
                    option-value="id"
                    :disabled="selectedTabIndex === null"
                    :aria-label="t('certificateLayout.alignment')"
                    :title="t('certificateLayout.alignment')"
                    class="flex-shrink-0"
                    style="width: 10rem"
                    @update:model-value="setAlignment"
                />
                <Button
                    v-if="selectedTabIndex !== null && mainBox && layout?.paragraphs[mainBox.pi]?.tabStops"
                    icon="pi pi-minus-circle"
                    size="small"
                    outlined
                    class="flex-shrink-0"
                    :aria-label="t('certificateLayout.remove_tab_stop')"
                    :title="t('certificateLayout.remove_tab_stop')"
                    @click="removeTabStop"
                />
                <Button
                    v-else
                    icon="pi pi-plus-circle"
                    size="small"
                    outlined
                    class="flex-shrink-0"
                    :disabled="!selected || selectedTabIndex !== null"
                    :aria-label="t('certificateLayout.add_tab_stop')"
                    :title="t('certificateLayout.add_tab_stop')"
                    @click="createTabStop"
                />
            </template>
        </div>
        <div class="flex flex-row items-center gap-2">
            <Button
                icon="pi pi-search-minus"
                size="small"
                outlined
                :disabled="zoom <= 0.5"
                :aria-label="t('certificateLayout.zoom_out')"
                :title="t('certificateLayout.zoom_out')"
                @click="zoom = Math.max(0.5, Math.round((zoom - 0.25) * 100) / 100)"
            />
            <Button
                size="small"
                text
                :label="`${Math.round(zoom * 100)} %`"
                :title="t('certificateLayout.zoom_reset')"
                @click="zoom = 1"
            />
            <Button
                icon="pi pi-search-plus"
                size="small"
                outlined
                :disabled="zoom >= 2"
                :aria-label="t('certificateLayout.zoom_in')"
                :title="t('certificateLayout.zoom_in')"
                @click="zoom = Math.min(2, Math.round((zoom + 0.25) * 100) / 100)"
            />
            <label class="flex flex-row items-center gap-1 text-sm">
                <Checkbox v-model="snapOn" binary input-id="canvas-snap" />
                {{ t('certificateLayout.snap') }}
            </label>
        </div>
        <div style="overflow-x: auto">
            <div
                class="canvas-page"
                data-testid="layout-canvas"
                :style="pageStyle"
                @pointerdown="onPageDown"
                @pointermove="onPointerMove"
                @pointerup="onPointerUp"
                @pointercancel="onPointerUp"
            >
                <div class="canvas-margins" :style="marginStyle">
                    <div
                        v-for="side in (['top', 'bottom', 'left', 'right'] as const)"
                        :key="side"
                        class="canvas-margin-handle"
                        :class="`canvas-margin-${side}`"
                        :data-testid="`margin-${side}`"
                        @pointerdown.stop="onMarginDown($event, side)"
                    />
                </div>
                <div
                    v-for="(stop, i) in stopMarkers"
                    :key="`stop-${i}`"
                    class="canvas-stop"
                    :style="{ left: `${stop.x * scale}px`, top: `${stop.top * scale}px`, height: `${stop.height * scale}px` }"
                />
                <div
                    v-if="marquee"
                    class="canvas-marquee"
                    :style="{
                        left: `${Math.min(marquee.x0, marquee.x1)}px`,
                        top: `${Math.min(marquee.y0, marquee.y1)}px`,
                        width: `${Math.abs(marquee.x1 - marquee.x0)}px`,
                        height: `${Math.abs(marquee.y1 - marquee.y0)}px`,
                    }"
                />
                <div v-if="guideX !== null" class="canvas-guide canvas-guide-x" :style="{ left: `${guideX * scale}px` }" />
                <div v-if="guideY !== null" class="canvas-guide canvas-guide-y" :style="{ top: `${guideY * scale}px` }" />
                <div
                    v-for="box in boxes"
                    :key="`${box.pi}-${box.bi}`"
                    class="canvas-box"
                    :class="{ 'canvas-box-selected': isSelected(box), 'canvas-box-media': box.kind === 'media' }"
                    :style="boxStyle(box)"
                    role="button"
                    tabindex="0"
                    :aria-label="ariaLabel(box)"
                    :aria-pressed="isSelected(box)"
                    @pointerdown="onPointerDown($event, box)"
                    @dblclick="startEdit(box)"
                    @keydown="onKeyDown($event, box)"
                >
                    <span
                        v-if="isSelected(box) && selection.size === 1 && !isEditing(box)"
                        class="canvas-resize"
                        data-testid="resize-handle"
                        @pointerdown.stop="onResizeDown($event, box)"
                    />
                    <input
                        v-if="isEditing(box)"
                        :ref="(el: unknown) => (editInput = el as HTMLInputElement | null)"
                        v-model="editText"
                        class="canvas-input"
                        :aria-label="t('certificateLayout.text')"
                        @pointerdown.stop
                        @keydown.enter.stop.prevent="commitEdit"
                        @keydown.esc.stop.prevent="cancelEdit"
                        @blur="commitEdit"
                    >
                    <img
                        v-else-if="box.kind === 'media' && props.media[box.media]"
                        class="canvas-image"
                        :src="`data:image/jpeg;base64,${props.media[box.media]}`"
                        :alt="box.media"
                        draggable="false"
                    >
                    <template v-else-if="box.kind === 'media'">
                        {{ box.media || t('certificateLayout.image') }}
                    </template>
                    <template v-else>
                        {{ sampleText(box.text) || t('certificateLayout.empty_text') }}
                    </template>
                </div>
            </div>
        </div>
        <Dialog
            v-model:visible="pickerVisible"
            modal
            :header="t('certificateLayout.pick_image')"
            :style="{ width: '40rem' }"
            :breakpoints="{ '575px': '90vw' }"
        >
            <p v-if="Object.keys(props.media).length === 0">
                {{ t('certificateLayout.no_images') }}
            </p>
            <div class="flex flex-row flex-wrap gap-3">
                <button
                    v-for="(thumbnail, name) in props.media"
                    :key="name"
                    type="button"
                    class="picker-item"
                    @click="pickImage(String(name))"
                >
                    <img :src="`data:image/jpeg;base64,${thumbnail}`" :alt="String(name)" draggable="false">
                    <span>{{ name }}</span>
                </button>
            </div>
        </Dialog>
    </div>
</template>

<style scoped>
.canvas-margin-handle {
    position: absolute;
    pointer-events: auto;
}
.canvas-margin-top, .canvas-margin-bottom {
    left: 0;
    right: 0;
    height: 8px;
    cursor: ns-resize;
}
.canvas-margin-top { top: -4px; }
.canvas-margin-bottom { bottom: -4px; }
.canvas-margin-left, .canvas-margin-right {
    top: 0;
    bottom: 0;
    width: 8px;
    cursor: ew-resize;
}
.canvas-margin-left { left: -4px; }
.canvas-margin-right { right: -4px; }
.canvas-stop {
    position: absolute;
    border-left: 1px dashed rgba(255, 122, 0, 0.7);
    pointer-events: none;
}
.canvas-marquee {
    position: absolute;
    border: 1px dashed rgb(0, 120, 255);
    background: rgba(0, 120, 255, 0.1);
    pointer-events: none;
}
.canvas-guide {
    position: absolute;
    background: rgba(255, 0, 120, 0.7);
    pointer-events: none;
}
.canvas-guide-x { top: 0; bottom: 0; width: 1px; }
.canvas-guide-y { left: 0; right: 0; height: 1px; }
.canvas-resize {
    position: absolute;
    right: 0;
    bottom: 0;
    width: 10px;
    height: 10px;
    background: rgb(255, 122, 0);
    cursor: nwse-resize;
}
.picker-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    width: 8rem;
    padding: 6px;
    border: 1px solid #ccc;
    border-radius: 4px;
    background: transparent;
    cursor: pointer;
    font-size: 12px;
    word-break: break-all;
}
.picker-item:hover {
    border-color: rgb(255, 122, 0);
}
.picker-item img {
    max-width: 100%;
    max-height: 6rem;
    object-fit: contain;
}
.canvas-page {
    position: relative;
    /* blocks may reach over the page edge (the PDF cuts them off); the margin keeps them grabbable */
    overflow: visible;
    margin: 40px;
    background-color: #fff;
    background-size: contain;
    background-position: center;
    background-repeat: no-repeat;
    border: 1px solid #ccc;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.2);
    touch-action: none;
    user-select: none;
}
.canvas-margins {
    position: absolute;
    border: 1px dashed rgba(0, 120, 255, 0.35);
    pointer-events: none;
}
.canvas-box {
    position: absolute;
    white-space: pre;
    cursor: grab;
    outline: 1px dashed transparent;
    min-width: 12px;
}
.canvas-box:hover {
    outline-color: rgba(255, 122, 0, 0.6);
}
.canvas-box:focus-visible,
.canvas-box-selected {
    outline: 2px solid #ff7a00;
}
.canvas-box-media {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px !important;
    overflow: hidden;
}
.canvas-box-media:not(:has(img)) {
    background: rgba(120, 120, 120, 0.25);
}
.canvas-image {
    width: 100%;
    height: 100%;
    pointer-events: none;
}
.canvas-input {
    font: inherit;
    color: inherit;
    width: 100%;
    min-width: 8em;
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid #ff7a00;
}
</style>
