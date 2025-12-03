import type { ControlSegment } from './control_segment'

export interface SplitTimeAnalysis {
    resultListId: number
    eventId: number
    classResultShortName: string
    controlSegments: ControlSegment[]
}
