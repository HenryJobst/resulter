<script setup lang="ts">
import type { CanvasBox } from '@/features/certificate/model/certificateCanvas'
import type { BlockEntry, LayoutParagraph, MediaBlock, TabAlignment, TextBlock } from '@/features/certificate/model/certificateLayout'
import Button from 'openvue/button'
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
import { effectiveMargins, FONTS, hexToRgb, isMediaBlock, newTextBlockEntry, PAGE_HEIGHT, PAGE_WIDTH, PLACEHOLDERS, rgbToHex, setOptional, TAB_ALIGNMENTS } from '@/features/certificate/model/certificateLayout'
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

const scale = computed(() => props.width / PAGE_WIDTH)
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
    width: `${props.width}px`,
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

const isSelected = (box: CanvasBox) => selected.value?.pi === box.pi && selected.value?.bi === box.bi

function ariaLabel(box: CanvasBox) {
    const where = t('certificateLayout.canvas_block', { p: box.pi + 1, b: box.bi + 1 })
    return `${where}: ${box.kind === 'media' ? box.media : box.text}`
}

// moving: vertical = space above the paragraph, horizontal = position of the tab stop
interface Drag {
    box: CanvasBox
    startX: number
    startY: number
    axis: 'x' | 'y' | null
    marginTop: number
    /** space above the next paragraph when the drag started */
    nextMarginTop: number
    position: number
}
let drag: Drag | null = null

function tabPosition(paragraph: LayoutParagraph, index: number | null): number {
    return index === null ? 0 : (paragraph.tabStops?.[index] ?? DEFAULT_TAB_STOP).position
}

function setTabPosition(paragraph: LayoutParagraph, index: number, position: number) {
    // the default stop only exists implicitly, write it down before changing it
    paragraph.tabStops ??= [{ ...DEFAULT_TAB_STOP }]
    paragraph.tabStops[index]!.position = position
}

/**
 * Moves a paragraph up or down by `delta` from the given start values. The paragraphs below keep
 * their place (the next one gets the opposite delta) unless `pushRest` is set (Alt key): then
 * they move along.
 */
function moveVertically(pi: number, marginTop: number, nextMarginTop: number, delta: number, pushRest: boolean) {
    const paragraphs = layout.value!.paragraphs
    paragraphs[pi]!.marginTop = marginTop + delta
    const next = paragraphs[pi + 1]
    if (next) {
        next.marginTop = nextMarginTop - (pushRest ? 0 : delta)
    }
}

function onPointerDown(event: PointerEvent, box: CanvasBox) {
    if (editing.value || !layout.value) {
        return
    }
    selected.value = { pi: box.pi, bi: box.bi }
    const paragraph = layout.value.paragraphs[box.pi]!
    drag = {
        box,
        startX: event.clientX,
        startY: event.clientY,
        axis: null,
        marginTop: paragraph.marginTop ?? DEFAULT_PARAGRAPH_SPACE,
        nextMarginTop: layout.value.paragraphs[box.pi + 1]?.marginTop ?? DEFAULT_PARAGRAPH_SPACE,
        position: tabPosition(paragraph, box.tabIndex),
    }
    ;(event.currentTarget as HTMLElement).setPointerCapture?.(event.pointerId)
}

// scaling the selected block with the handle at its bottom right: an image gets wider (the PDF
// keeps its aspect), a text gets a larger font in proportion to the way the handle was pulled
interface Resize { box: CanvasBox, startX: number, start: number }
let resize: Resize | null = null

function onResizeDown(event: PointerEvent, box: CanvasBox) {
    const block = layout.value?.paragraphs[box.pi]?.blocks[box.bi]?.block
    if (!block) {
        return
    }
    const start = isMediaBlock(block) ? (block.width ?? 100) : (block.fontSize ?? 12)
    resize = { box, startX: event.clientX, start }
    ;(event.currentTarget as HTMLElement).setPointerCapture?.(event.pointerId)
}

function onResizeMove(event: PointerEvent) {
    const block = layout.value?.paragraphs[resize!.box.pi]?.blocks[resize!.box.bi]?.block
    if (!block) {
        return
    }
    const dx = (event.clientX - resize!.startX) / scale.value
    if (isMediaBlock(block)) {
        block.width = Math.max(1, Math.round(resize!.start + dx))
    }
    else {
        const factor = (resize!.box.width + dx) / Math.max(1, resize!.box.width)
        block.fontSize = Math.max(1, Math.round(resize!.start * factor))
    }
}

function onPointerMove(event: PointerEvent) {
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
        drag.axis = Math.abs(dx) > Math.abs(dy) && drag.box.tabIndex !== null ? 'x' : 'y'
    }
    const paragraph = layout.value.paragraphs[drag.box.pi]!
    if (drag.axis === 'y') {
        moveVertically(drag.box.pi, drag.marginTop, drag.nextMarginTop, Math.round(dy / scale.value), event.altKey)
    }
    else {
        setTabPosition(paragraph, drag.box.tabIndex!, drag.position + Math.round(dx / scale.value))
    }
}

function onPointerUp() {
    drag = null
    resize = null
}

function onKeyDown(event: KeyboardEvent, box: CanvasBox) {
    if (editing.value || !layout.value) {
        return
    }
    const step = event.shiftKey ? 10 : 1
    const paragraph = layout.value.paragraphs[box.pi]!
    if (event.key === 'ArrowUp' || event.key === 'ArrowDown') {
        const delta = event.key === 'ArrowUp' ? -step : step
        const next = layout.value.paragraphs[box.pi + 1]
        moveVertically(
            box.pi,
            paragraph.marginTop ?? DEFAULT_PARAGRAPH_SPACE,
            next?.marginTop ?? DEFAULT_PARAGRAPH_SPACE,
            delta,
            event.altKey,
        )
    }
    else if ((event.key === 'ArrowLeft' || event.key === 'ArrowRight') && box.tabIndex !== null) {
        const delta = event.key === 'ArrowLeft' ? -step : step
        setTabPosition(paragraph, box.tabIndex, tabPosition(paragraph, box.tabIndex) + delta)
    }
    else if (event.key === 'Enter' && box.kind === 'text') {
        startEdit(box)
    }
    else if (event.key === 'Delete' || event.key === 'Backspace') {
        removeBlock(box)
    }
    else {
        return
    }
    event.preventDefault()
}

function removeBlock(box: CanvasBox) {
    const paragraphs = layout.value!.paragraphs
    const paragraph = paragraphs[box.pi]!
    paragraph.blocks.splice(box.bi, 1)
    // an empty paragraph is invalid (and invisible), so it goes with its last block
    if (paragraph.blocks.length === 0) {
        paragraphs.splice(box.pi, 1)
    }
    selected.value = null
}

// format bar for the selected text block
const selectedText = computed(() => {
    const block = selected.value && layout.value?.paragraphs[selected.value.pi]?.blocks[selected.value.bi]?.block
    return block && !isMediaBlock(block) ? block : null
})
const selectedTabIndex = computed(() => boxes.value.find(b => isSelected(b))?.tabIndex ?? null)
const fontOptions = [{ id: '', label: '–' }, ...FONTS.map(f => ({ id: f, label: f }))]
const alignmentOptions = computed(() =>
    TAB_ALIGNMENTS.map(a => ({ id: a, label: t(`certificateLayout.alignments.${a}`) })))

function setAlignment(alignment: TabAlignment) {
    const paragraph = layout.value?.paragraphs[selected.value!.pi]
    if (paragraph && selectedTabIndex.value !== null) {
        // the default stop only exists implicitly, write it down before changing it
        paragraph.tabStops ??= [{ ...DEFAULT_TAB_STOP }]
        paragraph.tabStops[selectedTabIndex.value]!.alignment = alignment
    }
}
const currentAlignment = computed(() => {
    const paragraph = selected.value && layout.value?.paragraphs[selected.value.pi]
    const index = selectedTabIndex.value
    return paragraph && index !== null ? (paragraph.tabStops ?? [DEFAULT_TAB_STOP])[index]?.alignment : undefined
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
    <div v-else-if="layout" class="flex flex-col gap-1">
        <p class="text-sm" style="max-width: 460px">
            {{ t('certificateLayout.canvas_hint') }}
        </p>
        <div class="flex flex-row gap-2">
            <Button
                :label="t('certificateLayout.canvas_add_text')"
                icon="pi pi-plus"
                size="small"
                outlined
                @click="addParagraph(newTextBlockEntry())"
            />
            <Button
                :label="t('certificateLayout.canvas_add_image')"
                icon="pi pi-plus"
                size="small"
                outlined
                @click="picker = 'new'"
            />
            <template v-if="selected">
                <Button
                    :label="t('certificateLayout.add_text_to_line')"
                    icon="pi pi-plus"
                    size="small"
                    outlined
                    @click="addToLine(newTextBlockEntry())"
                />
                <Button
                    :label="t('certificateLayout.add_image_to_line')"
                    icon="pi pi-plus"
                    size="small"
                    outlined
                    @click="picker = 'line'"
                />
                <Button
                    :label="t('certificateLayout.duplicate_line')"
                    icon="pi pi-copy"
                    size="small"
                    outlined
                    @click="duplicateLine"
                />
            </template>
        </div>
        <!-- the bar keeps its height when nothing is selected, else the page would jump under the pointer -->
        <div class="flex flex-row flex-wrap items-center gap-2" style="min-height: 2.75rem" data-testid="format-bar">
            <template v-if="selectedText">
                <div v-if="editing" class="flex flex-row gap-2" style="overflow-x: auto; white-space: nowrap; max-width: 100%">
                    <Button
                        v-for="name in PLACEHOLDERS"
                        :key="name"
                        :label="name"
                        size="small"
                        severity="secondary"
                        outlined
                        @pointerdown.prevent
                        @click="insertPlaceholder(name)"
                    />
                </div>
                <template v-else>
                    <Button
                        icon="pi pi-bold"
                        size="small"
                        :outlined="!selectedText.bold"
                        :aria-label="t('certificateLayout.bold')"
                        :aria-pressed="selectedText.bold === true"
                        :title="t('certificateLayout.bold')"
                        @click="setOptional(selectedText, 'bold', selectedText.bold ? null : true)"
                    />
                    <Button
                        icon="pi pi-italic"
                        size="small"
                        :outlined="!selectedText.italic"
                        :aria-label="t('certificateLayout.italic')"
                        :aria-pressed="selectedText.italic === true"
                        :title="t('certificateLayout.italic')"
                        @click="setOptional(selectedText, 'italic', selectedText.italic ? null : true)"
                    />
                    <InputNumber
                        :model-value="selectedText.fontSize ?? 12"
                        :min="1"
                        :use-grouping="false"
                        :aria-label="t('certificateLayout.font_size')"
                        :title="t('certificateLayout.font_size')"
                        input-class="w-4rem"
                        @update:model-value="(v: number | null) => setOptional(selectedText!, 'fontSize', v)"
                    />
                    <Select
                        :model-value="selectedText.font ?? ''"
                        :options="fontOptions"
                        option-label="label"
                        option-value="id"
                        :aria-label="t('certificateLayout.font')"
                        :title="t('certificateLayout.font')"
                        @update:model-value="(v: string) => setOptional(selectedText!, 'font', v)"
                    />
                    <ColorPicker
                        :model-value="rgbToHex(selectedText.fontRGBColor ?? { r: 0, g: 0, b: 0 })"
                        format="hex"
                        :aria-label="t('certificateLayout.color')"
                        @update:model-value="(v: string | undefined) => v && (selectedText!.fontRGBColor = hexToRgb(v))"
                    />
                    <Select
                        v-if="selectedTabIndex !== null"
                        :model-value="currentAlignment"
                        :options="alignmentOptions"
                        option-label="label"
                        option-value="id"
                        :aria-label="t('certificateLayout.alignment')"
                        :title="t('certificateLayout.alignment')"
                        @update:model-value="setAlignment"
                    />
                </template>
            </template>
        </div>
        <div
            class="canvas-page"
            data-testid="layout-canvas"
            :style="pageStyle"
            @pointerdown.self="selected = null"
            @pointermove="onPointerMove"
            @pointerup="onPointerUp"
            @pointercancel="onPointerUp"
        >
            <div class="canvas-margins" :style="marginStyle" />
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
                    v-if="isSelected(box) && !isEditing(box)"
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
