import type { CertificateLayout } from '@/features/certificate/model/certificateLayout'
import { DEFAULT_PARAGRAPH_SPACE } from '@/features/certificate/model/certificateCanvas'

/** The space above every paragraph, with the default filled in. */
export function paragraphMargins(layout: CertificateLayout): number[] {
    return layout.paragraphs.map(p => p.marginTop ?? DEFAULT_PARAGRAPH_SPACE)
}

/**
 * Moves the `moved` paragraphs by `delta` (points, down is positive), starting from the margins
 * `start`. The paragraphs below keep their place, unless `pushRest` is set: then everything
 * below the first moved paragraph moves along. Only margins that change are written.
 */
export function shiftParagraphs(
    layout: CertificateLayout,
    start: number[],
    moved: ReadonlySet<number>,
    delta: number,
    pushRest: boolean,
): void {
    const first = Math.min(...moved)
    let previousShift = 0
    layout.paragraphs.forEach((paragraph, index) => {
        const shift = pushRest ? (index >= first ? delta : 0) : (moved.has(index) ? delta : 0)
        const margin = start[index]! + shift - previousShift
        previousShift = shift
        if (margin !== (paragraph.marginTop ?? DEFAULT_PARAGRAPH_SPACE)) {
            paragraph.marginTop = margin
        }
    })
}

/**
 * Snapping: the shift (points) that brings one of the `edges` onto the nearest of the `targets`,
 * and that target as guide line. Nothing happens when no target is within `threshold`.
 */
export function snapShift(edges: number[], targets: number[], threshold: number): { shift: number, guide: number | null } {
    let best: { shift: number, guide: number } | null = null
    for (const edge of edges) {
        for (const target of targets) {
            const shift = target - edge
            if (Math.abs(shift) <= threshold && (!best || Math.abs(shift) < Math.abs(best.shift))) {
                best = { shift, guide: target }
            }
        }
    }
    return best ?? { shift: 0, guide: null }
}
