import type { Pageable } from '@/features/generic/models/pageable'
import type { Sort } from '@/features/generic/models/sort'

export interface RestResult<T> {
  content: T[]
  pageable: Pageable
  totalElements: number
  totalPages: number
  last: boolean
  numberOfElements: number
  number: number
  sort: Sort
  first: boolean
  size: number
  empty: boolean
}
