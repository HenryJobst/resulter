import type { GenericEntity } from '@/features/generic/models/GenericEntity'
import type { RestPageResult } from '@/features/generic/models/rest_page_result'
import type { TableSettings } from '@/features/generic/models/table_settings'

export interface IGenericService<T> {
    getAll: (t: (key: string) => string, tableSettings: TableSettings) => Promise<RestPageResult<T> | null>

    getAllUnpaged: (t: (key: string) => string) => Promise<T[] | null>

    getById: (id: number, t: (key: string) => string) => Promise<T>

    create: <T extends GenericEntity>(entity: T, t: (key: string) => string) => Promise<T>

    update: <T extends GenericEntity>(entity: T, t: (key: string) => string) => Promise<T>

    deleteById: (id: number, t: (key: string) => string) => Promise<void>
}
