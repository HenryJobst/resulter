/**
 * Dashboard statistics interface.
 * Contains system-wide counts for various entities.
 */
export interface DashboardStatistics {
    eventCount: number
    cupCount: number
    personCount: number
    organisationCount: number // Excludes OTHER type
    splitTimeCount: number
    raceCount: number
    resultListCount: number
    certificateCount: number
}
