// stores/settingsStore.js
import { defineStore } from 'pinia'
import { computed } from 'vue'
import type { TableSettings } from '@/features/generic/models/table_settings'

export const settingsStoreFactory = (id: string) =>
  defineStore(`settings-${id}`, () => {
    let settings: TableSettings = {
      paginator: true,
      first: 0,
      rows: 10,
      page: 0,
      rowsPerPageOptions: [5, 10, 20, 50, 100, 200, 500, 1000],
      paginatorPosition: 'bottom'
    }

    const currentPage = computed(() => settings.first / settings.rows + 1)

    function updateSettings(newSettings: Partial<TableSettings>) {
      settings = { ...settings, ...newSettings }
    }

    function setPage(pageIndex: number) {
      settings.page = pageIndex
      settings.first = (pageIndex - 1) * settings.rows
    }

    return { settings, currentPage, updateSettings, setPage }
  })
