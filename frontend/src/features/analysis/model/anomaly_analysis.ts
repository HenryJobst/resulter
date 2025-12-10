import type { RunnerAnomalyProfile } from './runner_anomaly_profile'

export interface AnomalyAnalysis {
    resultListId: number
    eventId: number
    runnerProfiles: RunnerAnomalyProfile[]
}
