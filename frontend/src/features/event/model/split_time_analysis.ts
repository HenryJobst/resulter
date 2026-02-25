import type { ControlSegment } from './control_segment'
import type { ControlSequenceSegment } from './control_sequence_segment'

export interface SplitTimeAnalysis {
    resultListId: number
    eventId: number
    classResultShortName: string
    controlSegments: ControlSegment[]
    sequenceSegments: ControlSequenceSegment[]
}
