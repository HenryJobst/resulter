export type AnalysisScope = 'event' | 'cup' | 'year'

export type AnalysisType = 'mental-resilience' | 'split-time-ranking' | 'split-time-table' | 'cheat-detection' | 'hanging-detection'

export interface AnalysisConfig {
    type: AnalysisType
    scope: AnalysisScope
    resultListId?: number
    cupId?: number
    years?: number[]
}

export interface AnalysisTypeInfo {
    key: AnalysisType
    titleKey: string
    descriptionKey: string
    icon: string
    enabled: boolean
}
