import type { GenericEntity } from '@/features/generic/models/GenericEntity'
import type { TableSettings } from '@/features/generic/models/table_settings'
import type { RestResult } from '@/features/generic/models/rest_result'

export interface IGenericService<T> {
    getAll: (t: (key: string) => string, tableSettings: TableSettings) => Promise<RestResult<T> | null>

    getById: (id: number, t: (key: string) => string) => Promise<T>

    create: <T extends GenericEntity>(entity: T, t: (key: string) => string) => Promise<T>

    update: <T extends GenericEntity>(entity: T, t: (key: string) => string) => Promise<T>

    deleteById: (id: number, t: (key: string) => string) => Promise<void>
}
