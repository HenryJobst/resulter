import type { DataTableFilterMeta, DataTableSortMeta } from 'primevue/datatable'
import type { PageSettings } from '@/features/generic/models/table_base_settings'

export interface TableSettings extends PageSettings {
    first: number
    paginator: boolean
    paginatorPosition: 'top' | 'bottom' | 'both' | undefined
    rowsPerPageOptions: number[]
    sortMode: 'multiple' | 'single' | undefined
    multiSortMeta: DataTableSortMeta[] | undefined
    sortField: string | ((item: any) => string) | undefined
    sortOrder: 1 | 0 | -1 | undefined | null
    nullSortOrder: number
    defaultSortOrder: number
    filters: DataTableFilterMeta | undefined
    removableSort: boolean
    rowHover: boolean
    stateStorage: 'session' | 'local'
    stateKey: string | undefined
    scrollable: boolean
    stripedRows: boolean

}
