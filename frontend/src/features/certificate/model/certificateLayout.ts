/**
 * Model for the certificate layout JSON (`layoutDescription`), see
 * backend/src/main/resources/schema/certificate_schema.json.
 * The layout is flowing text (paragraphs with tab stops), not absolute positions.
 */

export const FONTS = [
    'Courier',
    'Helvetica',
    'Symbol',
    'Times-Roman',
    'ZapfDingbats',
    'Amaranth-Regular.ttf',
    'Montserrat-Regular.ttf',
    'Satisfy-Regular.ttf',
    'Tangerine-Regular.ttf',
]
export const BOLD_FONTS = [
    'Courier-Bold',
    'Helvetica-Bold',
    'Times-Bold',
    'Amaranth-Bold.ttf',
    'Montserrat-Bold.ttf',
]
export const ITALIC_FONTS = [
    'Courier-Oblique',
    'Helvetica-Oblique',
    'Times-Italic',
    'Amaranth-Italic.ttf',
    'Montserrat-Italic.ttf',
]
export const BOLD_ITALIC_FONTS = [
    'Courier-BoldOblique',
    'Helvetica-BoldOblique',
    'Times-BoldItalic',
    'Amaranth-BoldItalic.ttf',
    'Montserrat-BoldItalic.ttf',
]
export const TAB_ALIGNMENTS = ['LEFT', 'RIGHT', 'CENTER', 'ANCHOR'] as const
// see backend TextBlockProcessor
export const PLACEHOLDERS = [
    'GIVEN_NAME',
    'FAMILY_NAME',
    'EVENT_NAME',
    'RESULT_POSITION',
    'CATEGORY',
    'RESULT_TIME',
    'ORGANISATION',
] as const

export type TabAlignment = (typeof TAB_ALIGNMENTS)[number]

export interface RgbColor { r: number, g: number, b: number }

export interface TextBlock {
    text: string
    fontSize?: number
    bold?: boolean
    italic?: boolean
    font?: string
    fontRGBColor?: RgbColor
}

export interface MediaBlock {
    media: string
    width?: number
}

export type Block = TextBlock | MediaBlock

export interface BlockEntry {
    block: Block
    tabPosition?: number
}

export interface TabStop { position: number, alignment: TabAlignment }

export interface LayoutParagraph {
    marginTop?: number
    marginBottom?: number
    marginLeft?: number
    marginRight?: number
    tabStops?: TabStop[]
    blocks: BlockEntry[]
}

export interface DocumentMargins { top?: number, bottom?: number, left?: number, right?: number }

export interface DocumentSettings {
    margins?: DocumentMargins
    font?: string
    boldFont?: string
    italicFont?: string
    boldItalicFont?: string
}

export interface CertificateLayout {
    document?: DocumentSettings
    paragraphs: LayoutParagraph[]
}

export type ParseResult = { layout: CertificateLayout } | { error: string }

/** Parses the layout JSON. An empty string is a valid, empty layout. Unknown keys are kept. */
export function parseLayout(json: string): ParseResult {
    if (!json || json.trim() === '') {
        return { layout: { paragraphs: [] } }
    }
    let data: unknown
    try {
        data = JSON.parse(json)
    }
    catch (e) {
        return { error: e instanceof Error ? e.message : String(e) }
    }
    if (typeof data !== 'object' || data === null || Array.isArray(data)) {
        return { error: 'The layout must be a JSON object' }
    }
    const layout = data as CertificateLayout
    if (!Array.isArray(layout.paragraphs)) {
        return { error: '"paragraphs" must be an array' }
    }
    if (layout.paragraphs.some(p => !p || !Array.isArray(p.blocks))) {
        return { error: 'Every paragraph needs a "blocks" array' }
    }
    return { layout }
}

export function serializeLayout(layout: CertificateLayout): string {
    return JSON.stringify(layout, null, 4)
}

export function isMediaBlock(block: Block): block is MediaBlock {
    return 'media' in block
}

export function newTextBlockEntry(): BlockEntry {
    return { block: { text: '', fontSize: 20 } }
}

export function newMediaBlockEntry(): BlockEntry {
    return { block: { media: '', width: 100 } }
}

export function newParagraph(): LayoutParagraph {
    return { blocks: [newTextBlockEntry()] }
}

/** Moves the item at `index` by one position, does nothing at the ends. */
export function moveItem<T>(items: T[], index: number, delta: -1 | 1): void {
    const target = index + delta
    if (target < 0 || target >= items.length) {
        return
    }
    const [item] = items.splice(index, 1)
    items.splice(target, 0, item as T)
}

/** Sets an optional value; null, undefined and '' remove the key so the JSON stays schema-valid. */
export function setOptional(target: object, key: string, value: unknown): void {
    const record = target as Record<string, unknown>
    if (value === null || value === undefined || value === '') {
        delete record[key]
    }
    else {
        record[key] = value
    }
}

export function rgbToHex(color: RgbColor): string {
    return [color.r, color.g, color.b]
        .map(v => Math.max(0, Math.min(255, Math.round(v))).toString(16).padStart(2, '0'))
        .join('')
}

export function hexToRgb(hex: string): RgbColor {
    const value = Number.parseInt(hex.replace('#', ''), 16)
    return { r: (value >> 16) & 255, g: (value >> 8) & 255, b: value & 255 }
}
