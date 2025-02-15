import type { TableSettings } from '@/features/generic/models/table_settings'
// stores/settingsStore.js
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export function settingsStoreFactory(id: string, initialSettings?: TableSettings) {
    return defineStore(`settings-${id}`, () => {
        const settings = ref<TableSettings>({
            first: 0,
            page: 0,
            paginator: true,
            paginatorPosition: 'both',
            rows: 10,
            rowsPerPageOptions: [5, 10, 20, 50, 100, 200, 500, 1000],
            sortMode: 'multiple',
            multiSortMeta: undefined,
            sortField: initialSettings?.sortField ?? undefined,
            sortOrder: initialSettings?.sortOrder ?? undefined,
            nullSortOrder: 1,
            defaultSortOrder: 1,
            filters: {},
            removableSort: initialSettings?.removableSort ?? false,
            rowHover: initialSettings?.rowHover ?? true,
            stateStorage: initialSettings?.stateStorage ?? 'session',
            stateKey: initialSettings?.stateKey ?? undefined,
            scrollable: initialSettings?.scrollable ?? false,
            stripedRows: initialSettings?.stripedRows ?? true,
        })

        const currentPage = computed(() => settings.value.first / settings.value.rows + 1)

        /*
                                                                                function updateSettings(newSettings: Partial<TableSettings>) {
                                                                                  settings.value = { ...settings, ...newSettings }
                                                                                }
                                                                             */

        function setPage(pageIndex: number) {
            settings.value.page = pageIndex
            settings.value.first = pageIndex * settings.value.rows
        }

        return { settings, currentPage, setPage }
    })
}
