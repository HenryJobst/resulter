import type { GenericEntity } from '@/features/generic/models/GenericEntity'

export interface IGenericService<T> {
  getAll(t: (key: string) => string): Promise<T[] | null>

  getById(id: number, t: (key: string) => string): Promise<T>

  create<T extends GenericEntity>(entity: T, t: (key: string) => string): Promise<T>

  update<T extends GenericEntity>(entity: T, t: (key: string) => string): Promise<T>

  deleteById(id: number, t: (key: string) => string): Promise<void>
}
