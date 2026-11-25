import type { CertificateLayout } from '@/features/certificate/model/certificateLayout'
import { effectiveMargins, isMediaBlock } from '@/features/certificate/model/certificateLayout'

/**
 * Geometry of the graphical view. It reproduces how the backend (iText) lays out the flowing
 * text; the constants below were measured on PDFs rendered by the backend.
 *
 * Vertical: every paragraph has 4 pt space above and below unless marginTop/marginBottom are
 * given (they replace the 4, they are not added); a line is 1.5 x font size high and the
 * baseline sits 1.055 x font size below the top of the line.
 * Horizontal: tab stops are used in ascending order; a block sits on the first stop behind
 * the end of the previous block, without a stop left it sits on the next multiple of 50.
 * Only the text width is estimated (font metrics are not available), so line breaks and the
 * exact end of a block are approximations. The PDF preview stays the truth.
 */

// iText default: no tab stops means one centered stop at 261.5
export const DEFAULT_TAB_STOP = { position: 261.5, alignment: 'CENTER' } as const
// iText default text size of a Paragraph
const DEFAULT_FONT_SIZE = 12
// Line height and baseline (times font size) depend on the font. Fitted on the PDFs of the 13
// certificates on staging (they match to 0.1 pt). Tangerine is only used in a last line there, its
// line height is an estimate.
const FONT_LINES: Array<[prefix: string, lineHeight: number, baseline: number]> = [
    ['Times', 1.458, 1.009],
    ['Courier', 1.273, 0.92],
    ['Amaranth', 1.964, 1.426],
    ['Montserrat', 2.108, 1.382],
    ['Satisfy', 1.921, 1.183],
    ['Tangerine', 1.45, 0.949],
]
const HELVETICA_LINE = { lineHeight: 1.499, baseline: 1.056 }
function lineFor(font: string | undefined) {
    const found = FONT_LINES.find(([prefix]) => font?.startsWith(prefix))
    return found ? { lineHeight: found[1], baseline: found[2] } : HELVETICA_LINE
}
export const BASELINE = HELVETICA_LINE.baseline
// measured: default space above and below a paragraph
export const DEFAULT_PARAGRAPH_SPACE = 4
// measured: tabs behind the last stop go to the next multiple of this
const TAB_INTERVAL = 50
// height estimate of an image block relative to its width (the real aspect is not known here)
const MEDIA_ASPECT = 0.5

export interface CanvasBox {
    /** index of the paragraph and of the block in the layout (not the sorted order) */
    pi: number
    bi: number
    kind: 'text' | 'media'
    text: string
    media: string
    /** anchor position in points from the page's top left */
    x: number
    y: number
    anchor: 'left' | 'center' | 'right'
    fontSize: number
    lineHeight: number
    /** distance from the top of the line to the baseline in points */
    baseline: number
    bold: boolean
    italic: boolean
    font: string | undefined
    color: string
    width: number
    height: number
    /** index of the tab stop (in the paragraph's tabStops) the block sits on, null on the 50 grid */
    tabIndex: number | null
}

// the same dummy data the backend uses for its preview (CertificateServiceImpl)
const SAMPLES: Record<string, string> = {
    GIVEN_NAME: 'Max',
    FAMILY_NAME: 'Mustermann',
    EVENT_NAME: 'Beispiel-OL',
    RESULT_POSITION: '1',
    CATEGORY: 'H35-',
    RESULT_TIME: '32:14',
    ORGANISATION: 'Kaulsdorfer OLV Berlin',
}

/** Replaces the placeholders with sample data, like the backend preview does. */
export function sampleText(text: string): string {
    return text.replace(/\{\{(\w+)\}\}/g, (match, name: string) => SAMPLES[name] ?? match)
}

/** The web font of a PDF font: the bundled families are loaded by certificateFonts.css. */
export function fontFamilyFor(font: string | undefined): string {
    if (font?.startsWith('Times')) {
        return '"Times New Roman", Times, serif'
    }
    if (font?.startsWith('Courier')) {
        return '"Courier New", Courier, monospace'
    }
    for (const family of ['Amaranth', 'Montserrat']) {
        if (font?.startsWith(family)) {
            return `"${family}", Helvetica, Arial, sans-serif`
        }
    }
    for (const family of ['Satisfy', 'Tangerine']) {
        if (font?.startsWith(family)) {
            return `"${family}", cursive`
        }
    }
    return 'Helvetica, Arial, sans-serif'
}

/** Whether the font name itself says bold / italic (e.g. a block with font "Amaranth-Bold.ttf"). */
export const isBoldFont = (font: string | undefined) => /Bold/.test(font ?? '')
export const isItalicFont = (font: string | undefined) => /Italic|Oblique/.test(font ?? '')

const NARROW = 'iljtf.,:;!|\'` ()[]{}-/'
const WIDE = 'mwMW@'

/** Rough text width in points: Helvetica-like proportions, no real font metrics. */
export function estimateWidth(text: string, fontSize: number, font?: string, bold = false): number {
    if (font?.startsWith('Courier')) {
        return text.length * 0.6 * fontSize
    }
    let units = 0
    for (const ch of text) {
        if (ch === ' ') {
            units += 0.278
        }
        else if (NARROW.includes(ch)) {
            units += 0.26
        }
        else if (WIDE.includes(ch)) {
            units += 0.8
        }
        else if (ch >= '0' && ch <= '9') {
            units += 0.556
        }
        else if (ch >= 'A' && ch <= 'Z') {
            units += 0.68
        }
        else {
            units += 0.53
        }
    }
    const factor = (font?.startsWith('Times') ? 0.92 : 1) * (bold ? 1.08 : 1)
    return units * fontSize * factor
}

interface Stop { position: number, alignment: string, index: number }

/** `aspects` maps an image file name to height / width; without it a guess is used. */
export type MeasureText = (text: string, fontSize: number, font: string | undefined, bold: boolean, italic: boolean) => number

/** `measure` returns the width of a text; the default is the rough `estimateWidth`. */
export function computeBoxes(
    layout: CertificateLayout,
    aspects: Record<string, number> = {},
    measure: MeasureText = (text, fontSize, font, bold) => estimateWidth(text, fontSize, font, bold),
): CanvasBox[] {
    const margins = effectiveMargins(layout)
    const boxes: CanvasBox[] = []
    let y = margins.top

    layout.paragraphs.forEach((paragraph, pi) => {
        y += paragraph.marginTop ?? DEFAULT_PARAGRAPH_SPACE
        const left = margins.left + (paragraph.marginLeft ?? 0)
        const stops: Stop[] = (paragraph.tabStops ?? [DEFAULT_TAB_STOP])
            .map((stop, index) => ({ position: stop.position, alignment: stop.alignment, index }))
            .sort((a, b) => a.position - b.position)
        // the backend sorts by tabPosition (default 1); sort is stable like Java's
        const ordered = paragraph.blocks
            .map((entry, bi) => ({ entry, bi }))
            .sort((a, b) => (a.entry.tabPosition ?? 1) - (b.entry.tabPosition ?? 1))

        const firstBox = boxes.length
        let cursor = 0
        for (const { entry, bi } of ordered) {
            const block = entry.block
            const media = isMediaBlock(block)
            const fontSize = media ? DEFAULT_FONT_SIZE : (block.fontSize ?? DEFAULT_FONT_SIZE)
            const text = media ? '' : sampleText(block.text)
            const doc = layout.document
            const bold = !media && block.bold === true
            const italic = !media && block.italic === true
            // like the backend: the block's own font, else the document font for its style
            const font = media
                ? undefined
                : (block.font
                    ?? (bold && italic ? doc?.boldItalicFont : bold ? doc?.boldFont : italic ? doc?.italicFont : undefined)
                    ?? doc?.font)
            const width = media
                ? (block.width ?? 100)
                : measure(text, fontSize, font, bold || isBoldFont(font), italic || isItalicFont(font))

            // a tab goes to the next stop behind the current position
            const stop = stops.find(s => s.position > cursor)
            const position = stop ? stop.position : (Math.floor(cursor / TAB_INTERVAL) + 1) * TAB_INTERVAL
            const alignment = stop?.alignment ?? 'LEFT'
            cursor = alignment === 'CENTER' ? position + width / 2 : alignment === 'RIGHT' ? position : position + width

            const line = lineFor(font)
            const height = media ? width * (aspects[block.media] ?? MEDIA_ASPECT) : fontSize * line.lineHeight
            const color = !media && block.fontRGBColor
                ? `rgb(${block.fontRGBColor.r}, ${block.fontRGBColor.g}, ${block.fontRGBColor.b})`
                : 'rgb(0, 0, 0)'
            boxes.push({
                pi,
                bi,
                kind: media ? 'media' : 'text',
                text: media ? '' : block.text,
                media: media ? block.media : '',
                x: left + position,
                y,
                anchor: alignment === 'CENTER' ? 'center' : alignment === 'RIGHT' ? 'right' : 'left',
                fontSize,
                lineHeight: height,
                baseline: fontSize * line.baseline,
                bold,
                italic,
                font,
                color,
                width,
                height,
                tabIndex: stop ? stop.index : null,
            })
        }
        // texts and images sit on one baseline: an image reaches up from it, a text has its
        // ascent above and its descent below it
        const added = boxes.slice(firstBox)
        const ascent = Math.max(0, ...added.map(b => (b.kind === 'media' ? b.height : b.baseline)))
        const descent = Math.max(0, ...added.map(b => (b.kind === 'media' ? 0 : b.lineHeight - b.baseline)))
        for (const box of added) {
            box.y = y + ascent - (box.kind === 'media' ? box.height : box.baseline)
        }
        y += ascent + descent + (paragraph.marginBottom ?? DEFAULT_PARAGRAPH_SPACE)
    })
    return boxes
}
