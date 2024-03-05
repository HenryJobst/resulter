export interface GenericListColumn {
  label: string
  label_count?: number
  field: string
  type?: string
  queryKey?: string
  sortable?: boolean
  filterable?: boolean
  filterType?: string
}
