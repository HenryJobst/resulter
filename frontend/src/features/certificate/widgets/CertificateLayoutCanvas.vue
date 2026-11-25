<script setup lang="ts">
import type { CanvasBox } from '@/features/certificate/model/certificateCanvas'
import type { LayoutParagraph, TextBlock } from '@/features/certificate/model/certificateLayout'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watchEffect } from 'vue'
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
import { effectiveMargins, PAGE_HEIGHT, PAGE_WIDTH } from '@/features/certificate/model/certificateLayout'
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
        fontWeight: box.bold || isBoldFont(box.font) ? 'bold' : 'normal',
        fontStyle: box.italic || isItalicFont(box.font) ? 'italic' : 'normal',
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
        position: tabPosition(paragraph, box.tabIndex),
    }
    ;(event.currentTarget as HTMLElement).setPointerCapture?.(event.pointerId)
}

function onPointerMove(event: PointerEvent) {
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
        paragraph.marginTop = Math.max(0, drag.marginTop + Math.round(dy / scale.value))
    }
    else {
        setTabPosition(paragraph, drag.box.tabIndex!, Math.max(0, drag.position + Math.round(dx / scale.value)))
    }
}

function onPointerUp() {
    drag = null
}

function onKeyDown(event: KeyboardEvent, box: CanvasBox) {
    if (editing.value || !layout.value) {
        return
    }
    const step = event.shiftKey ? 10 : 1
    const paragraph = layout.value.paragraphs[box.pi]!
    if (event.key === 'ArrowUp' || event.key === 'ArrowDown') {
        const delta = event.key === 'ArrowUp' ? -step : step
        paragraph.marginTop = Math.max(0, (paragraph.marginTop ?? DEFAULT_PARAGRAPH_SPACE) + delta)
    }
    else if ((event.key === 'ArrowLeft' || event.key === 'ArrowRight') && box.tabIndex !== null) {
        const delta = event.key === 'ArrowLeft' ? -step : step
        setTabPosition(paragraph, box.tabIndex, Math.max(0, tabPosition(paragraph, box.tabIndex) + delta))
    }
    else if (event.key === 'Enter' && box.kind === 'text') {
        startEdit(box)
    }
    else {
        return
    }
    event.preventDefault()
}

const isEditing = (box: CanvasBox) => editing.value?.pi === box.pi && editing.value?.bi === box.bi

function startEdit(box: CanvasBox) {
    if (box.kind !== 'text' || !layout.value) {
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
</template>

<style scoped>
.canvas-page {
    position: relative;
    overflow: hidden;
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
