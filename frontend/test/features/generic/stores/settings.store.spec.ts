import type { TableSettings } from '@/features/generic/models/table_settings'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { settingsStoreFactory } from '@/features/generic/stores/settings.store'

describe('settingsStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
    })

    describe('store creation with default settings', () => {
        it('should create store with default settings', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.first).toBe(0)
            expect(store.settings.page).toBe(0)
            expect(store.settings.paginator).toBe(true)
            expect(store.settings.rows).toBe(10)
            expect(store.settings.sortMode).toBe('multiple')
        })

        it('should have default paginatorPosition as both', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.paginatorPosition).toBe('both')
        })

        it('should have default rowsPerPageOptions', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.rowsPerPageOptions).toEqual([5, 10, 20, 50, 100, 200, 500, 1000])
        })

        it('should have undefined multiSortMeta by default', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.multiSortMeta).toBeUndefined()
        })

        it('should have undefined sortField by default', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.sortField).toBeUndefined()
        })

        it('should have undefined sortOrder by default', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.sortOrder).toBeUndefined()
        })

        it('should have nullSortOrder as 1', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.nullSortOrder).toBe(1)
        })

        it('should have defaultSortOrder as 1', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.defaultSortOrder).toBe(1)
        })

        it('should have empty filters object', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.filters).toEqual({})
        })

        it('should have removableSort as false by default', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.removableSort).toBe(false)
        })

        it('should have rowHover as true by default', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.rowHover).toBe(true)
        })

        it('should have stateStorage as session by default', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.stateStorage).toBe('session')
        })

        it('should have undefined stateKey by default', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.stateKey).toBeUndefined()
        })

        it('should have scrollable as false by default', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.scrollable).toBe(false)
        })

        it('should have stripedRows as true by default', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.settings.stripedRows).toBe(true)
        })
    })

    describe('store creation with initial settings', () => {
        it('should override sortField with initial settings', () => {
            const initialSettings: Partial<TableSettings> = {
                sortField: 'name',
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.sortField).toBe('name')
        })

        it('should override sortOrder with initial settings', () => {
            const initialSettings: Partial<TableSettings> = {
                sortOrder: -1,
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.sortOrder).toBe(-1)
        })

        it('should override removableSort with initial settings', () => {
            const initialSettings: Partial<TableSettings> = {
                removableSort: true,
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.removableSort).toBe(true)
        })

        it('should override rowHover with initial settings', () => {
            const initialSettings: Partial<TableSettings> = {
                rowHover: false,
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.rowHover).toBe(false)
        })

        it('should override stateStorage with initial settings', () => {
            const initialSettings: Partial<TableSettings> = {
                stateStorage: 'local',
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.stateStorage).toBe('local')
        })

        it('should override stateKey with initial settings', () => {
            const initialSettings: Partial<TableSettings> = {
                stateKey: 'my-custom-key',
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.stateKey).toBe('my-custom-key')
        })

        it('should override scrollable with initial settings', () => {
            const initialSettings: Partial<TableSettings> = {
                scrollable: true,
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.scrollable).toBe(true)
        })

        it('should override stripedRows with initial settings', () => {
            const initialSettings: Partial<TableSettings> = {
                stripedRows: false,
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.stripedRows).toBe(false)
        })

        it('should override multiple settings at once', () => {
            const initialSettings: Partial<TableSettings> = {
                sortField: 'email',
                sortOrder: 1,
                removableSort: true,
                rowHover: false,
                stateStorage: 'local',
                scrollable: true,
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.sortField).toBe('email')
            expect(store.settings.sortOrder).toBe(1)
            expect(store.settings.removableSort).toBe(true)
            expect(store.settings.rowHover).toBe(false)
            expect(store.settings.stateStorage).toBe('local')
            expect(store.settings.scrollable).toBe(true)
        })

        it('should keep default values for non-overridden settings', () => {
            const initialSettings: Partial<TableSettings> = {
                sortField: 'id',
            }

            const useSettingsStore = settingsStoreFactory('test', initialSettings as TableSettings)
            const store = useSettingsStore()

            expect(store.settings.sortField).toBe('id')
            expect(store.settings.rows).toBe(10) // default
            expect(store.settings.paginator).toBe(true) // default
            expect(store.settings.rowHover).toBe(true) // default
        })
    })

    describe('currentPage computed', () => {
        it('should calculate current page as 1 initially', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.currentPage).toBe(1)
        })

        it('should calculate current page based on first and rows', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.settings.first = 10
            store.settings.rows = 10

            expect(store.currentPage).toBe(2)
        })

        it('should calculate current page for page 3', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.settings.first = 20
            store.settings.rows = 10

            expect(store.currentPage).toBe(3)
        })

        it('should calculate current page with different rows per page', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.settings.first = 50
            store.settings.rows = 25

            expect(store.currentPage).toBe(3)
        })

        it('should reactively update when first changes', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            expect(store.currentPage).toBe(1)

            store.settings.first = 30
            expect(store.currentPage).toBe(4)
        })

        it('should reactively update when rows changes', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.settings.first = 20

            store.settings.rows = 20
            expect(store.currentPage).toBe(2)

            store.settings.rows = 10
            expect(store.currentPage).toBe(3)
        })
    })

    describe('setPage action', () => {
        it('should set page to 0', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.setPage(0)

            expect(store.settings.page).toBe(0)
            expect(store.settings.first).toBe(0)
        })

        it('should set page to 1', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.setPage(1)

            expect(store.settings.page).toBe(1)
            expect(store.settings.first).toBe(10) // 1 * 10
        })

        it('should set page to 2', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.setPage(2)

            expect(store.settings.page).toBe(2)
            expect(store.settings.first).toBe(20) // 2 * 10
        })

        it('should calculate first based on page and rows', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.settings.rows = 25

            store.setPage(3)

            expect(store.settings.page).toBe(3)
            expect(store.settings.first).toBe(75) // 3 * 25
        })

        it('should update currentPage computed after setPage', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.setPage(5)

            expect(store.currentPage).toBe(6) // (50 / 10) + 1
        })

        it('should handle setting page multiple times', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.setPage(1)
            expect(store.settings.first).toBe(10)

            store.setPage(3)
            expect(store.settings.first).toBe(30)

            store.setPage(0)
            expect(store.settings.first).toBe(0)
        })
    })

    describe('unique store instances', () => {
        it('should create unique stores with different IDs', () => {
            const useSettingsStore1 = settingsStoreFactory('store1')
            const useSettingsStore2 = settingsStoreFactory('store2')

            const store1 = useSettingsStore1()
            const store2 = useSettingsStore2()

            store1.setPage(5)
            store2.setPage(2)

            expect(store1.settings.page).toBe(5)
            expect(store2.settings.page).toBe(2)
        })

        it('should not share state between different store IDs', () => {
            const useStoreA = settingsStoreFactory('storeA')
            const useStoreB = settingsStoreFactory('storeB')

            const storeA = useStoreA()
            const storeB = useStoreB()

            storeA.settings.rows = 50
            storeB.settings.rows = 20

            expect(storeA.settings.rows).toBe(50)
            expect(storeB.settings.rows).toBe(20)
        })

        it('should create same store instance for same ID', () => {
            const useSettingsStore1 = settingsStoreFactory('same-id')
            const useSettingsStore2 = settingsStoreFactory('same-id')

            const store1 = useSettingsStore1()
            const store2 = useSettingsStore2()

            store1.setPage(3)

            // They should be the same store instance
            expect(store2.settings.page).toBe(3)
        })
    })

    describe('edge cases', () => {
        it('should handle page 0 correctly', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.setPage(0)

            expect(store.settings.first).toBe(0)
            expect(store.currentPage).toBe(1)
        })

        it('should handle large page numbers', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.setPage(100)

            expect(store.settings.page).toBe(100)
            expect(store.settings.first).toBe(1000)
            expect(store.currentPage).toBe(101)
        })

        it('should handle changing rows per page', () => {
            const useSettingsStore = settingsStoreFactory('test')
            const store = useSettingsStore()

            store.settings.rows = 10
            store.setPage(5)
            expect(store.settings.first).toBe(50)

            // Change rows per page
            store.settings.rows = 20
            expect(store.currentPage).toBe(3.5) // 50 / 20 + 1
        })

        it('should handle undefined initial settings', () => {
            const useSettingsStore = settingsStoreFactory('test', undefined)
            const store = useSettingsStore()

            expect(store.settings.sortField).toBeUndefined()
            expect(store.settings.rowHover).toBe(true)
        })
    })
})
