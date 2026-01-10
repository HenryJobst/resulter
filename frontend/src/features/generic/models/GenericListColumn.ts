import type { HintedString } from 'primevue/ts-helpers'

export interface GenericListColumn {
    label: string
    label_count?: number
    field: string
    class?: string
    style?: string
    truncate?: number
    type?: string
    queryKey?: string
    sortable?: boolean
    filterable?: boolean
    filterType?: string
    filterMatchMode?:
        | HintedString<
            | 'startsWith'
            | 'contains'
            | 'notContains'
            | 'endsWith'
            | 'equals'
            | 'notEquals'
            | 'in'
            | 'lt'
            | 'lte'
            | 'gt'
            | 'gte'
            | 'between'
            | 'dateIs'
            | 'dateIsNot'
            | 'dateBefore'
            | 'dateAfter'
        >
        | undefined
}
