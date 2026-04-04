import type { SequenceRunnerSplit } from './sequence_runner_split'

export interface ControlSequenceSegment {
    controls: string[]
    segmentLabel: string
    runnerSplits: SequenceRunnerSplit[]
    classes: string[]
}
