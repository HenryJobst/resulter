export interface GenericListColumn {
  label: string
  field: string | ((obj: object) => string)
}
