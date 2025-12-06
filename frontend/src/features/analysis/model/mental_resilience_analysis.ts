import type { MriStatistics } from './mri_statistics'
import type { RunnerMentalProfile } from './runner_mental_profile'

export interface MentalResilienceAnalysis {
    resultListId: number
    eventId: number
    runnerProfiles: RunnerMentalProfile[]
    statistics: MriStatistics
}
