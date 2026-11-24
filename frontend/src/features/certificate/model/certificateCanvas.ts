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
// measured: line height and distance from the top of the line to the baseline, times font size
const LINE_HEIGHT = 1.5
export const BASELINE = 1.055
// measured: default space above and below a paragraph
const DEFAULT_PARAGRAPH_SPACE = 4
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

/** Closest web font stack for a PDF font name. */
export function fontFamilyFor(font: string | undefined): string {
    if (font?.startsWith('Times')) {
        return '"Times New Roman", Times, serif'
    }
    if (font?.startsWith('Courier')) {
        return '"Courier New", Courier, monospace'
    }
    if (font?.startsWith('Satisfy') || font?.startsWith('Tangerine')) {
        return 'cursive'
    }
    return 'Helvetica, Arial, sans-serif'
}

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

export function computeBoxes(layout: CertificateLayout): CanvasBox[] {
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

        let lineHeight = 0
        let cursor = 0
        for (const { entry, bi } of ordered) {
            const block = entry.block
            const media = isMediaBlock(block)
            const fontSize = media ? DEFAULT_FONT_SIZE : (block.fontSize ?? DEFAULT_FONT_SIZE)
            const text = media ? '' : sampleText(block.text)
            const width = media
                ? (block.width ?? 100)
                : estimateWidth(text, fontSize, block.font, block.bold === true)

            // a tab goes to the next stop behind the current position
            const stop = stops.find(s => s.position > cursor)
            const position = stop ? stop.position : (Math.floor(cursor / TAB_INTERVAL) + 1) * TAB_INTERVAL
            const alignment = stop?.alignment ?? 'LEFT'
            cursor = alignment === 'CENTER' ? position + width / 2 : alignment === 'RIGHT' ? position : position + width

            const height = media ? width * MEDIA_ASPECT : fontSize * LINE_HEIGHT
            lineHeight = Math.max(lineHeight, height)
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
                bold: !media && block.bold === true,
                italic: !media && block.italic === true,
                font: media ? undefined : block.font,
                color,
                width,
                height,
                tabIndex: stop ? stop.index : null,
            })
        }
        y += lineHeight + (paragraph.marginBottom ?? DEFAULT_PARAGRAPH_SPACE)
    })
    return boxes
}
