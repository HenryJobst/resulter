export interface HangingPair {
    legNumber: number
    fromControl: string
    toControl: string
    busDriverId: number
    busDriverClassName: string
    busDriverRaceNumber: number
    timeDeltaSeconds: number
    passengerPI: number
    busDriverPI: number
    hangingIndex: number
    improvementPercent: number
    passengerActualTime: number
    busDriverActualTime: number
    referenceTime: number
}
