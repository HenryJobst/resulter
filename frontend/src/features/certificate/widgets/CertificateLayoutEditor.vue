<script setup lang="ts">
import type {
    Block,
    BlockEntry,
    CertificateLayout,
    LayoutParagraph,
    TabAlignment,
    TextBlock,
} from '@/features/certificate/model/certificateLayout'
import Button from 'openvue/button'
import Checkbox from 'openvue/checkbox'
import ColorPicker from 'openvue/colorpicker'
import Fieldset from 'openvue/fieldset'
import InputNumber from 'openvue/inputnumber'
import InputText from 'openvue/inputtext'
import Select from 'openvue/select'
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
    BOLD_FONTS,
    BOLD_ITALIC_FONTS,
    FONTS,
    hexToRgb,
    isMediaBlock,
    ITALIC_FONTS,
    moveItem,
    newMediaBlockEntry,
    newParagraph,
    newTextBlockEntry,
    parseLayout,
    PLACEHOLDERS,
    rgbToHex,
    serializeLayout,
    setOptional,
    TAB_ALIGNMENTS,
} from '@/features/certificate/model/certificateLayout'

const props = defineProps<{
    modelValue: string
    /** file names of the media files that can be used as image blocks */
    mediaFiles: string[]
}>()

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const { t } = useI18n()

const layout = ref<CertificateLayout | null>(null)
const error = ref<string | null>(null)

// serialized layout the editor and the parent value agree on; loading a value must not
// emit it again (that would reformat the JSON without any edit), and our own update
// must not reload the editor
let lastSerialized: string | null = null

function load(json: string) {
    const result = parseLayout(json)
    if ('error' in result) {
        error.value = result.error
        layout.value = null
    }
    else {
        error.value = null
        lastSerialized = serializeLayout(result.layout)
        layout.value = result.layout
    }
}

watch(
    () => props.modelValue,
    (value) => {
        if (value !== lastSerialized) {
            load(value)
        }
    },
    { immediate: true },
)

watch(
    layout,
    (value) => {
        if (!value) {
            return
        }
        const serialized = serializeLayout(value)
        if (serialized !== lastSerialized) {
            lastSerialized = serialized
            emit('update:modelValue', serialized)
        }
    },
    { deep: true },
)

const alignmentOptions = TAB_ALIGNMENTS.map(a => ({ id: a, label: t(`certificateLayout.alignments.${a}`) }))

const asText = (block: Block) => block as TextBlock

// document settings
type Side = 'top' | 'bottom' | 'left' | 'right'
type FontKey = 'font' | 'boldFont' | 'italicFont' | 'boldItalicFont'

function cleanupDocument() {
    const doc = layout.value?.document
    if (!doc || !layout.value) {
        return
    }
    if (doc.margins && Object.keys(doc.margins).length === 0) {
        delete doc.margins
    }
    if (Object.keys(doc).length === 0) {
        delete layout.value.document
    }
}

function setMargin(side: Side, value: number | null) {
    const l = layout.value!
    l.document ??= {}
    l.document.margins ??= {}
    setOptional(l.document.margins, side, value)
    cleanupDocument()
}

function setDocFont(key: FontKey, value: string | null) {
    const l = layout.value!
    l.document ??= {}
    setOptional(l.document, key, value)
    cleanupDocument()
}

const documentFonts: { key: FontKey, label: string, options: string[] }[] = [
    { key: 'font', label: 'font', options: FONTS },
    { key: 'boldFont', label: 'bold_font', options: BOLD_FONTS },
    { key: 'italicFont', label: 'italic_font', options: ITALIC_FONTS },
    { key: 'boldItalicFont', label: 'bold_italic_font', options: BOLD_ITALIC_FONTS },
]
const sides: { key: Side, label: string }[] = [
    { key: 'top', label: 'margin_top' },
    { key: 'bottom', label: 'margin_bottom' },
    { key: 'left', label: 'margin_left' },
    { key: 'right', label: 'margin_right' },
]
const paragraphMargins = [
    { key: 'marginTop', label: 'margin_top' },
    { key: 'marginBottom', label: 'margin_bottom' },
    { key: 'marginLeft', label: 'margin_left' },
    { key: 'marginRight', label: 'margin_right' },
] as const

// paragraphs and blocks
function addParagraph() {
    layout.value!.paragraphs.push(newParagraph())
}

function removeAt<T>(items: T[], index: number) {
    items.splice(index, 1)
}

function addBlock(paragraph: LayoutParagraph, entry: BlockEntry) {
    paragraph.blocks.push(entry)
}

function addTabStop(paragraph: LayoutParagraph) {
    paragraph.tabStops ??= []
    paragraph.tabStops.push({ position: 100, alignment: 'LEFT' as TabAlignment })
}

function removeTabStop(paragraph: LayoutParagraph, index: number) {
    paragraph.tabStops!.splice(index, 1)
    if (paragraph.tabStops!.length === 0) {
        delete paragraph.tabStops
    }
}

function insertPlaceholder(block: TextBlock, name: string) {
    block.text += `{{${name}}}`
}

function setColor(block: TextBlock, hex: string | null | undefined) {
    if (hex) {
        block.fontRGBColor = hexToRgb(hex)
    }
}
</script>

<template>
    <div v-if="error" class="text-red-500" role="alert">
        {{ t('certificateLayout.invalid_json', { error }) }}
    </div>
    <div v-else-if="layout" class="flex flex-col gap-3">
        <Fieldset :legend="t('certificateLayout.document')" toggleable collapsed>
            <div class="flex flex-col gap-3">
                <div class="flex flex-row flex-wrap gap-3">
                    <label v-for="s in sides" :key="s.key" class="flex flex-col text-sm">
                        {{ t(`certificateLayout.${s.label}`) }}
                        <InputNumber
                            input-style="width: 7rem"
                            :model-value="layout.document?.margins?.[s.key]"
                            :use-grouping="false"
                            @update:model-value="(v: number | null) => setMargin(s.key, v)"
                        />
                    </label>
                </div>
                <div class="flex flex-row flex-wrap gap-3">
                    <label v-for="f in documentFonts" :key="f.key" class="flex flex-col text-sm">
                        {{ t(`certificateLayout.${f.label}`) }}
                        <Select
                            :model-value="layout.document?.[f.key]"
                            :options="f.options"
                            show-clear
                            @update:model-value="(v: string | null) => setDocFont(f.key, v)"
                        />
                    </label>
                </div>
            </div>
        </Fieldset>

        <p v-if="layout.paragraphs.length === 0">
            {{ t('certificateLayout.no_paragraphs') }}
        </p>

        <div
            v-for="(paragraph, pi) in layout.paragraphs"
            :key="pi"
            class="flex flex-col gap-2 p-3"
            style="border: 1px solid #ccc; border-radius: 6px"
        >
            <div class="flex flex-row justify-between items-center">
                <strong>{{ t('certificateLayout.paragraph', { n: pi + 1 }) }}</strong>
                <div class="flex flex-row gap-1">
                    <Button
                        icon="pi pi-arrow-up"
                        text
                        rounded
                        :aria-label="t('certificateLayout.move_up')"
                        :disabled="pi === 0"
                        @click="moveItem(layout.paragraphs, pi, -1)"
                    />
                    <Button
                        icon="pi pi-arrow-down"
                        text
                        rounded
                        :aria-label="t('certificateLayout.move_down')"
                        :disabled="pi === layout.paragraphs.length - 1"
                        @click="moveItem(layout.paragraphs, pi, 1)"
                    />
                    <Button
                        icon="pi pi-trash"
                        text
                        rounded
                        severity="danger"
                        :aria-label="t('certificateLayout.remove')"
                        @click="removeAt(layout.paragraphs, pi)"
                    />
                </div>
            </div>

            <div class="flex flex-row flex-wrap gap-3">
                <label v-for="m in paragraphMargins" :key="m.key" class="flex flex-col text-sm">
                    {{ t(`certificateLayout.${m.label}`) }}
                    <InputNumber
                        input-style="width: 7rem"
                        :model-value="paragraph[m.key]"
                        :use-grouping="false"
                        @update:model-value="(v: number | null) => setOptional(paragraph, m.key, v)"
                    />
                </label>
            </div>

            <div class="flex flex-col gap-1">
                <div class="flex flex-row items-center gap-2">
                    <span class="text-sm">{{ t('certificateLayout.tab_stops') }}</span>
                    <Button
                        icon="pi pi-plus"
                        size="small"
                        text
                        rounded
                        :aria-label="t('certificateLayout.add_tab_stop')"
                        @click="addTabStop(paragraph)"
                    />
                </div>
                <div
                    v-for="(stop, si) in paragraph.tabStops"
                    :key="si"
                    class="flex flex-row items-end gap-3"
                >
                    <label class="flex flex-col text-sm">
                        {{ t('certificateLayout.tab_stop_position') }}
                        <InputNumber
                            v-model="stop.position"
                            input-style="width: 7rem"
                            :use-grouping="false"
                            :max-fraction-digits="2"
                        />
                    </label>
                    <label class="flex flex-col text-sm">
                        {{ t('certificateLayout.alignment') }}
                        <Select
                            v-model="stop.alignment"
                            :options="alignmentOptions"
                            option-label="label"
                            option-value="id"
                        />
                    </label>
                    <Button
                        icon="pi pi-times"
                        text
                        rounded
                        severity="danger"
                        :aria-label="t('certificateLayout.remove')"
                        @click="removeTabStop(paragraph, si)"
                    />
                </div>
            </div>

            <div
                v-for="(entry, bi) in paragraph.blocks"
                :key="bi"
                class="flex flex-col gap-2 p-2"
                style="border: 1px dashed #bbb; border-radius: 6px"
            >
                <div class="flex flex-row justify-between items-center">
                    <span class="text-sm">
                        {{ isMediaBlock(entry.block) ? t('certificateLayout.image') : t('certificateLayout.text') }}
                    </span>
                    <div class="flex flex-row gap-1">
                        <Button
                            icon="pi pi-arrow-up"
                            text
                            rounded
                            size="small"
                            :aria-label="t('certificateLayout.move_up')"
                            :disabled="bi === 0"
                            @click="moveItem(paragraph.blocks, bi, -1)"
                        />
                        <Button
                            icon="pi pi-arrow-down"
                            text
                            rounded
                            size="small"
                            :aria-label="t('certificateLayout.move_down')"
                            :disabled="bi === paragraph.blocks.length - 1"
                            @click="moveItem(paragraph.blocks, bi, 1)"
                        />
                        <Button
                            icon="pi pi-trash"
                            text
                            rounded
                            size="small"
                            severity="danger"
                            :aria-label="t('certificateLayout.remove')"
                            :disabled="paragraph.blocks.length === 1"
                            @click="removeAt(paragraph.blocks, bi)"
                        />
                    </div>
                </div>

                <template v-if="isMediaBlock(entry.block)">
                    <div class="flex flex-row flex-wrap gap-3">
                        <label class="flex flex-col text-sm">
                            {{ t('certificateLayout.image') }}
                            <Select
                                v-model="entry.block.media"
                                :options="props.mediaFiles"
                                filter
                            />
                        </label>
                        <label class="flex flex-col text-sm">
                            {{ t('certificateLayout.width') }}
                            <InputNumber
                                input-style="width: 7rem"
                                :model-value="entry.block.width"
                                :use-grouping="false"
                                @update:model-value="(v: number | null) => setOptional(entry.block, 'width', v)"
                            />
                        </label>
                    </div>
                </template>
                <template v-else>
                    <label class="flex flex-col text-sm">
                        {{ t('certificateLayout.text') }}
                        <InputText
                            :model-value="asText(entry.block).text"
                            @update:model-value="(v: string | undefined) => (asText(entry.block).text = v ?? '')"
                        />
                    </label>
                    <div class="flex flex-row flex-wrap items-center gap-1">
                        <span class="text-sm">{{ t('certificateLayout.placeholders') }}:</span>
                        <Button
                            v-for="name in PLACEHOLDERS"
                            :key="name"
                            :label="name"
                            size="small"
                            outlined
                            @click="insertPlaceholder(asText(entry.block), name)"
                        />
                    </div>
                    <div class="flex flex-row flex-wrap items-end gap-3">
                        <label class="flex flex-col text-sm">
                            {{ t('certificateLayout.font_size') }}
                            <InputNumber
                                input-style="width: 7rem"
                                :model-value="asText(entry.block).fontSize"
                                :use-grouping="false"
                                @update:model-value="(v: number | null) => setOptional(entry.block, 'fontSize', v)"
                            />
                        </label>
                        <label class="flex flex-col text-sm">
                            {{ t('certificateLayout.font') }}
                            <Select
                                :model-value="asText(entry.block).font"
                                :options="FONTS"
                                show-clear
                                @update:model-value="(v: string | null) => setOptional(entry.block, 'font', v)"
                            />
                        </label>
                        <label class="flex flex-row items-center gap-1 text-sm">
                            <Checkbox
                                :model-value="asText(entry.block).bold === true"
                                binary
                                @update:model-value="(v: boolean) => setOptional(entry.block, 'bold', v || null)"
                            />
                            {{ t('certificateLayout.bold') }}
                        </label>
                        <label class="flex flex-row items-center gap-1 text-sm">
                            <Checkbox
                                :model-value="asText(entry.block).italic === true"
                                binary
                                @update:model-value="(v: boolean) => setOptional(entry.block, 'italic', v || null)"
                            />
                            {{ t('certificateLayout.italic') }}
                        </label>
                        <div class="flex flex-row items-center gap-1 text-sm">
                            <span>{{ t('certificateLayout.color') }}</span>
                            <template v-if="asText(entry.block).fontRGBColor">
                                <ColorPicker
                                    :model-value="rgbToHex(asText(entry.block).fontRGBColor!)"
                                    format="hex"
                                    @update:model-value="(v: string | undefined) => setColor(asText(entry.block), v)"
                                />
                                <Button
                                    icon="pi pi-times"
                                    size="small"
                                    text
                                    rounded
                                    :aria-label="t('certificateLayout.remove')"
                                    @click="setOptional(entry.block, 'fontRGBColor', null)"
                                />
                            </template>
                            <Button
                                v-else
                                icon="pi pi-plus"
                                size="small"
                                text
                                rounded
                                :aria-label="t('certificateLayout.set_color')"
                                @click="setOptional(entry.block, 'fontRGBColor', { r: 0, g: 0, b: 0 })"
                            />
                        </div>
                    </div>
                </template>

                <label class="flex flex-col text-sm" style="max-width: 12rem">
                    {{ t('certificateLayout.tab_position') }}
                    <InputNumber
                        input-style="width: 7rem"
                        :model-value="entry.tabPosition"
                        :use-grouping="false"
                        :max-fraction-digits="2"
                        @update:model-value="(v: number | null) => setOptional(entry, 'tabPosition', v)"
                    />
                </label>
            </div>

            <div class="flex flex-row gap-2">
                <Button
                    :label="t('certificateLayout.add_text')"
                    icon="pi pi-plus"
                    size="small"
                    outlined
                    @click="addBlock(paragraph, newTextBlockEntry())"
                />
                <Button
                    :label="t('certificateLayout.add_image')"
                    icon="pi pi-plus"
                    size="small"
                    outlined
                    @click="addBlock(paragraph, newMediaBlockEntry())"
                />
            </div>
        </div>

        <div>
            <Button
                :label="t('certificateLayout.add_paragraph')"
                icon="pi pi-plus"
                outlined
                @click="addParagraph"
            />
        </div>
    </div>
</template>
