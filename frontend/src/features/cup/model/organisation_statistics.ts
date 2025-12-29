import type { Organisation } from '@/features/organisation/model/organisation'

export interface OrganisationStatistics {
    organisation: Organisation
    runnerCount: number
    totalStarts: number
    nonScoringStarts: number
    startsPerRunner: number
    nonScoringStartsPerRunner: number
    nonScoringRatio: number
}
