import type { RunnerSplit } from './runner_split'

export interface ControlSequenceSegment {
    controls: string[]
    segmentLabel: string
    runnerSplits: RunnerSplit[]
    classes: string[]
}
