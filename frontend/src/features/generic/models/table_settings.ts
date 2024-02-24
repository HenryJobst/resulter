import type { PageSettings } from '@/features/generic/models/table_base_settings'

export interface TableSettings extends PageSettings {
  paginator: boolean
  first: number
  rowsPerPageOptions: number[]
  paginatorPosition: string
}
