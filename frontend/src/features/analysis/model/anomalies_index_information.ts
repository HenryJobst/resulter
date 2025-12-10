export interface AnomaliesIndexInformation {
    legNumber: number
    fromControl: string
    toControl: string
    classification: 'NO_SUSPICION' | 'MODERATE_SUSPICION' | 'HIGH_SUSPICION' | 'NO_DATA'
    actualTimeSeconds: number
    referenceTimeSeconds: number
}
