// stores/settingsStore.js
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { TableSettings } from '@/features/generic/models/table_settings'

export const settingsStoreFactory = (id: string, initialSettings?: TableSettings) =>
  defineStore(`settings-${id}`, () => {
    const settings = ref<TableSettings>({
      first: 0,
      page: 0,
      paginator: true,
      paginatorPosition: 'top',
      rows: 10,
      rowsPerPageOptions: [5, 10, 20, 50, 100, 200, 500, 1000],
      sortMode: 'multiple',
      multiSortMeta: undefined,
      sortField: initialSettings ? initialSettings.sortField : undefined,
      sortOrder: initialSettings ? initialSettings.sortOrder : undefined,
      nullSortOrder: 1,
      defaultSortOrder: 1,
      filters: {}
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
