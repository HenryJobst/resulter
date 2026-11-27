import { describe, expect, it } from 'vitest'
import { paragraphMargins, shiftParagraphs, snapShift } from '@/features/certificate/model/certificateMove'

function make() {
    return {
        paragraphs: [
            { marginTop: 10, blocks: [] },
            { blocks: [] },
            { marginTop: 20, blocks: [] },
        ],
    }
}

describe('shiftParagraphs', () => {
    it('keeps the paragraphs below in place', () => {
        const layout = make()
        shiftParagraphs(layout, paragraphMargins(layout), new Set([0]), 30, false)
        expect(layout.paragraphs.map(p => p.marginTop)).toEqual([40, -26, 20])
    })

    it('pushes the rest along', () => {
        const layout = make()
        shiftParagraphs(layout, paragraphMargins(layout), new Set([0]), 30, true)
        expect(layout.paragraphs.map(p => p.marginTop)).toEqual([40, undefined, 20])
    })

    it('moves several paragraphs and keeps the others', () => {
        const layout = make()
        shiftParagraphs(layout, paragraphMargins(layout), new Set([0, 2]), 5, false)
        // paragraph 1 stays: -5 for the shift of 0, +5 back for paragraph 2
        expect(layout.paragraphs.map(p => p.marginTop)).toEqual([15, -1, 25])
    })
})

describe('snapShift', () => {
    it('snaps to the nearest target within the threshold', () => {
        expect(snapShift([100, 150], [103, 148, 300], 5)).toEqual({ shift: -2, guide: 148 })
    })

    it('does nothing when nothing is near', () => {
        expect(snapShift([100], [200], 5)).toEqual({ shift: 0, guide: null })
    })
})
