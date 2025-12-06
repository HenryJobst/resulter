import type { MistakeReactionPair } from './mistake_reaction_pair'

export interface RunnerMentalProfile {
    personId: number
    classResultShortName: string
    raceNumber: number
    classRunnerCount: number
    reliableData: boolean
    normalPI: number
    mistakeReactions: MistakeReactionPair[]
    averageMRI: number
    classification: 'panic' | 'ice_man' | 'resigner' | 'chain_error'
    mistakeCount: number
    totalSegments: number
}
