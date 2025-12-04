import type { RunnerSplit } from './runner_split'

export interface ControlSegment {
    fromControl: string
    toControl: string
    segmentLabel: string
    runnerSplits: RunnerSplit[]
    classes: string[]
    bidirectional: boolean
}
