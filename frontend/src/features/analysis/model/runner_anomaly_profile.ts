import type { AnomaliesIndexInformation } from './anomalies_index_information'

export interface RunnerAnomalyProfile {
    personId: number
    classResultShortName: string
    raceNumber: number
    classRunnerCount: number
    reliableData: boolean
    normalPI: number
    minimumAnomaliesIndex: number
    minimumAnomaliesLegNumber: number
    anomaliesIndexes: AnomaliesIndexInformation[]
    classification: 'NO_SUSPICION' | 'MODERATE_SUSPICION' | 'HIGH_SUSPICION' | 'NO_DATA'
    totalSegments: number
}
