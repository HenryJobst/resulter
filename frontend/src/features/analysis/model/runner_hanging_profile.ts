import type { HangingPair } from './hanging_pair'

export interface RunnerHangingProfile {
    personId: number
    classResultShortName: string
    raceNumber: number
    startTime: number | null
    classRunnerCount: number
    reliableData: boolean
    normalPI: number
    hangingPairs: HangingPair[]
    averageHangingIndex: number
    classification: 'NO_HANGING' | 'MODERATE_HANGING' | 'HIGH_HANGING' | 'INSUFFICIENT_DATA'
    totalNonMistakeSegments: number
    hangingCount: number
    hangingPercentage: number
}
