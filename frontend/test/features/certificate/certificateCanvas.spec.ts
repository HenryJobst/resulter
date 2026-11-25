import type { CertificateLayout } from '@/features/certificate/model/certificateLayout'
import { describe, expect, it } from 'vitest'
import { BASELINE, computeBoxes, estimateWidth, fontFamilyFor, sampleText } from '@/features/certificate/model/certificateCanvas'

/**
 * The expected numbers were measured on PDFs rendered by the backend (iText) and read with
 * pdf.js: the y values are text baselines in points from the top of the page, the x values
 * are tab positions plus the page margin. They pin the geometry to the real renderer.
 */

const T = (text: string, fontSize = 12, extra: object = {}) => ({ block: { text, fontSize, ...extra } })
const P = (text: string, fontSize: number, extra: object = {}) => ({ ...extra, blocks: [T(text, fontSize)] })

const baselines = (layout: CertificateLayout) => computeBoxes(layout).map(b => b.y + BASELINE * b.fontSize)
function expectClose(actual: number[], expected: number[]) {
    expect(actual).toHaveLength(expected.length)
    actual.forEach((value, i) => expect(value).toBeCloseTo(expected[i]!, 0))
}

describe('computeBoxes: vertical layout', () => {
    it('uses 4 pt default space above and below a paragraph (no document: 36 pt margins)', () => {
        const layout: CertificateLayout = { paragraphs: [P('A', 20), P('B', 20), P('C', 20, { marginTop: 10 })] }
        expectClose(baselines(layout), [61.1, 99.1, 143.1])
    })

    it('lets marginTop replace the default space and handles changing font sizes', () => {
        const layout: CertificateLayout = {
            document: { margins: { top: 40, bottom: 30, left: 30, right: 30 } },
            paragraphs: [P('A', 48, { marginTop: 100 }), P('B', 28, { marginTop: 30 }), P('C', 18, { marginTop: 10 }), P('D', 12)],
        }
        expectClose(baselines(layout), [190.7, 275.5, 320.9, 349.5])
    })

    it('follows the measured baselines for font sizes 8 to 48', () => {
        const sizes = [8, 10, 12, 14, 16, 18, 20, 24, 28, 36, 48]
        const layout: CertificateLayout = {
            document: { margins: { top: 20, bottom: 20, left: 30, right: 30 } },
            paragraphs: sizes.map(fs => P(`s${fs}`, fs)),
        }
        expectClose(baselines(layout), [32.45, 54.55, 79.64, 107.74, 138.83, 172.92, 210, 252.19, 300.38, 358.79, 433.4])
    })

    it('adds marginTop to the 4 pt below the previous paragraph', () => {
        const mts = [0, 5, 10, 20, 40]
        const layout: CertificateLayout = {
            document: { margins: { top: 20, bottom: 20, left: 30, right: 30 } },
            paragraphs: mts.flatMap(m => [P(`x${m}`, 20), P(`m${m}`, 20, { marginTop: m })]),
        }
        expectClose(baselines(layout), [45.12, 79.09, 117.06, 156.03, 194, 237.97, 275.94, 329.91, 367.88, 441.85])
    })

    it('lets marginBottom replace the default space below', () => {
        const mbs = [0, 5, 10, 20, 40]
        const layout: CertificateLayout = {
            document: { margins: { top: 20, bottom: 20, left: 30, right: 30 } },
            paragraphs: mbs.flatMap(m => [P(`y${m}`, 20, { marginBottom: m }), P(`n${m}`, 20)]),
        }
        expectClose(baselines(layout), [45.12, 79.09, 117.06, 156.03, 194, 237.97, 275.94, 329.91, 367.88, 441.85])
    })

    it('replaces the default space above the first paragraph with marginTop', () => {
        const layout: CertificateLayout = {
            document: { margins: { top: 20, bottom: 20, left: 30, right: 30 } },
            paragraphs: [P('f', 20, { marginTop: 50 })],
        }
        expectClose(baselines(layout), [91.12])
    })

    it('uses the backend default margins when the document has none (30/30/20/20)', () => {
        const layout: CertificateLayout = { document: { font: 'Courier' }, paragraphs: [P('A', 20)] }
        expect(computeBoxes(layout)[0]!.y).toBe(30 + 4)
    })

    it('returns no boxes for an empty layout', () => {
        expect(computeBoxes({ paragraphs: [] })).toEqual([])
    })
})

describe('computeBoxes: tab stops', () => {
    const doc = { margins: { top: 40, bottom: 30, left: 30, right: 30 } }
    const run = (paragraph: object) => computeBoxes({ document: doc, paragraphs: [paragraph as never] })

    it('centers a single block on the default stop at 261.5', () => {
        expect(run({ blocks: [T('Def', 16)] })[0]).toMatchObject({ x: 30 + 261.5, anchor: 'center', tabIndex: 0 })
    })

    it('puts blocks on their stops with the alignment of the stop', () => {
        const boxes = run({ tabStops: [{ position: 100, alignment: 'LEFT' }, { position: 300, alignment: 'RIGHT' }], blocks: [T('L', 16), T('R', 16)] })
        expect(boxes[0]).toMatchObject({ x: 130, anchor: 'left', tabIndex: 0 })
        expect(boxes[1]).toMatchObject({ x: 330, anchor: 'right', tabIndex: 1 })
    })

    it('skips a stop at position 0 (a tab always moves forward)', () => {
        const boxes = run({ tabStops: [{ position: 0, alignment: 'LEFT' }, { position: 300, alignment: 'RIGHT' }], blocks: [T('L0', 16), T('R0', 16)] })
        expect(boxes[0]).toMatchObject({ x: 330, anchor: 'right', tabIndex: 1 })
        // the second block is behind the last stop: next multiple of 50
        expect(boxes[1]).toMatchObject({ x: 380, anchor: 'left', tabIndex: null })
    })

    it('uses the stops in ascending order whatever their order in the layout', () => {
        const boxes = run({ tabStops: [{ position: 300, alignment: 'LEFT' }, { position: 100, alignment: 'LEFT' }], blocks: [T('A', 16), T('B', 16)] })
        expect(boxes[0]).toMatchObject({ x: 130, tabIndex: 1 })
        expect(boxes[1]).toMatchObject({ x: 330, tabIndex: 0 })
    })

    it('continues on the 50 point grid behind the last stop', () => {
        const boxes = run({ tabStops: [{ position: 100, alignment: 'LEFT' }], blocks: [T('A', 16), T('B', 16), T('C', 16), T('D', 16)] })
        expect(boxes.map(b => b.x)).toEqual([130, 180, 230, 280])
    })

    it('moves a block to the next stop when the previous block is too long for the gap', () => {
        const boxes = run({ tabStops: [{ position: 100, alignment: 'LEFT' }, { position: 110, alignment: 'LEFT' }], blocks: [T('A long first block', 16), T('B', 16)] })
        expect(boxes[0]!.x).toBe(130)
        // measured: the second block sits at 250 (grid) behind the 117 pt wide first block
        expect(boxes[1]!.x).toBe(280)
    })

    it('measures the stop from the paragraph margin', () => {
        const boxes = run({ marginLeft: 50, tabStops: [{ position: 100, alignment: 'LEFT' }], blocks: [T('ML100', 16)] })
        expect(boxes[0]!.x).toBe(30 + 50 + 100)
    })

    it('orders blocks by tabPosition like the backend but keeps the original index', () => {
        const boxes = run({
            tabStops: [{ position: 10, alignment: 'LEFT' }, { position: 200, alignment: 'LEFT' }],
            blocks: [{ block: { text: 'second' }, tabPosition: 2 }, { block: { text: 'first' }, tabPosition: 1 }],
        })
        expect(boxes.find(b => b.text === 'first')).toMatchObject({ bi: 1, x: 40 })
        expect(boxes.find(b => b.text === 'second')).toMatchObject({ bi: 0, x: 230 })
    })

    it('describes text style and media blocks', () => {
        const boxes = run({
            tabStops: [{ position: 10, alignment: 'LEFT' }, { position: 300, alignment: 'LEFT' }],
            blocks: [T('Styled', 30, { bold: true, italic: true, fontRGBColor: { r: 1, g: 2, b: 3 } }), { block: { media: 'logo.png', width: 80 }, tabPosition: 2 }],
        })
        expect(boxes[0]).toMatchObject({ fontSize: 30, bold: true, italic: true, color: 'rgb(1, 2, 3)' })
        expect(boxes[1]).toMatchObject({ kind: 'media', media: 'logo.png', width: 80 })
    })
})

describe('helpers', () => {
    it('maps pdf fonts to css font families', () => {
        expect(fontFamilyFor(undefined)).toContain('Helvetica')
        expect(fontFamilyFor('Times-Roman')).toContain('serif')
        expect(fontFamilyFor('Courier-Bold')).toContain('monospace')
        expect(fontFamilyFor('Satisfy-Regular.ttf')).toContain('"Satisfy"')
        expect(fontFamilyFor('Amaranth-Bold.ttf')).toContain('"Amaranth"')
        expect(fontFamilyFor('Montserrat-Regular.ttf')).toContain('"Montserrat"')
    })

    it('replaces placeholders with sample data', () => {
        expect(sampleText('{{GIVEN_NAME}} {{FAMILY_NAME}}')).toBe('Max Mustermann')
        expect(sampleText('Platz {{RESULT_POSITION}}')).toBe('Platz 1')
        expect(sampleText('{{UNKNOWN}}')).toBe('{{UNKNOWN}}')
    })

    it('estimates the text width roughly like Helvetica', () => {
        // measured in the PDF: 'A long first block' at 16 pt is 117.4 pt wide
        expect(estimateWidth('A long first block', 16)).toBeGreaterThan(100)
        expect(estimateWidth('A long first block', 16)).toBeLessThan(135)
        expect(estimateWidth('abc', 20)).toBeGreaterThan(estimateWidth('abc', 10))
        expect(estimateWidth('abcd', 10, 'Courier')).toBeCloseTo(24)
    })
})

describe('computeBoxes: images', () => {
    const layout = {
        paragraphs: [{ blocks: [{ block: { media: 'logo.png', width: 100 } }] }, { blocks: [{ block: { text: 'x', fontSize: 10 } }] }],
    } as CertificateLayout

    it('uses the real aspect of the image for height and for the following paragraph', () => {
        const [image, next] = computeBoxes(layout, { 'logo.png': 1.5 })
        expect(image!.height).toBe(150)
        expect(next!.y).toBe(36 + 4 + 150 + 4 + 4)
    })

    it('guesses half the width without aspect', () => {
        expect(computeBoxes(layout)[0]!.height).toBe(50)
    })
})

describe('computeBoxes: font dependent line height', () => {
    // measured on the staging PDF of "BBM Mittel 2026": the baselines of the first two lines are 63.0 pt apart
    const layout = {
        document: { font: 'Montserrat-Regular.ttf', boldFont: 'Montserrat-Bold.ttf' },
        paragraphs: [
            { marginTop: 160, marginBottom: 0, blocks: [{ block: { text: 'a', fontSize: 28, bold: true } }] },
            { marginTop: 4, blocks: [{ block: { text: 'b', fontSize: 28, bold: true } }] },
        ],
    } as CertificateLayout

    it('uses the line height of the document font', () => {
        const [first, second] = computeBoxes(layout)
        expect(first!.font).toBe('Montserrat-Bold.ttf')
        expect(second!.y + second!.baseline - (first!.y + first!.baseline)).toBeCloseTo(63, 0)
    })

    it('a block font wins over the document font', () => {
        const withFont = { ...layout, paragraphs: [{ blocks: [{ block: { text: 'a', font: 'Times-Roman' } }] }] } as CertificateLayout
        expect(computeBoxes(withFont)[0]!.font).toBe('Times-Roman')
    })
})

describe('computeBoxes: measured widths', () => {
    it('uses the given measure function for the block width', () => {
        const layout = { paragraphs: [{ blocks: [{ block: { text: 'abc', fontSize: 10 } }] }] } as CertificateLayout
        expect(computeBoxes(layout, {}, () => 77)[0]!.width).toBe(77)
    })
})

describe('computeBoxes: bold and italic like the backend', () => {
    const one = (document: object | undefined, block: object) =>
        computeBoxes({ document, paragraphs: [{ blocks: [{ block: { text: 'x', fontSize: 10, ...block } }] }] } as CertificateLayout)[0]!

    it('fakes bold without a bold font of the document', () => {
        expect(one(undefined, { bold: true })).toMatchObject({ simulatedBold: true, font: undefined })
        expect(one({ font: 'Amaranth-Regular.ttf' }, { bold: true })).toMatchObject({ simulatedBold: true, font: 'Amaranth-Regular.ttf' })
    })

    it('uses the bold font of the document as is', () => {
        expect(one({ font: 'Amaranth-Regular.ttf', boldFont: 'Amaranth-Bold.ttf' }, { bold: true }))
            .toMatchObject({ simulatedBold: false, font: 'Amaranth-Bold.ttf' })
    })

    it('fakes the style on a block font even when the document has a bold font', () => {
        expect(one({ boldFont: 'Amaranth-Bold.ttf' }, { bold: true, font: 'Times-Roman' }))
            .toMatchObject({ simulatedBold: true, font: 'Times-Roman' })
    })

    it('fakes italic separately', () => {
        expect(one({ boldFont: 'Amaranth-Bold.ttf' }, { bold: true, italic: true }))
            .toMatchObject({ simulatedBold: true, simulatedItalic: true })
    })
})
