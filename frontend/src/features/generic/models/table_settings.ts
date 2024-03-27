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
    sortOrder: number | undefined
    nullSortOrder: number
    defaultSortOrder: number
    filters: DataTableFilterMeta | null
}
