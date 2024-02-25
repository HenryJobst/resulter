import type { PageSettings } from '@/features/generic/models/table_base_settings'
import type { DataTableSortMeta } from 'primevue/datatable'

export interface TableSettings extends PageSettings {
  first: number
  paginator: boolean
  paginatorPosition: string
  rowsPerPageOptions: number[]
  sortMode: string
  multiSortMeta: DataTableSortMeta[] | undefined
  sortField: string | null | undefined
  sortOrder: number | null | undefined
  nullSortOrder: number
  defaultSortOrder: number
}
