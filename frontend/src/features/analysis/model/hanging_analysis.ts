import type { RunnerHangingProfile } from './runner_hanging_profile'

export interface HangingStatistics {
    totalRunners: number
    runnersWithHanging: number
    totalHangingSegments: number
    highHangingRunners: number
    moderateHangingRunners: number
    averageHangingIndex: number
    medianHangingIndex: number
}

export interface HangingAnalysis {
    resultListId: number
    eventId: number
    runnerProfiles: RunnerHangingProfile[]
    statistics: HangingStatistics
}
