<script setup lang="ts">
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { useQuery } from '@tanstack/vue-query'
import { computed } from 'vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import { eventService } from '@/features/event/services/event.service'
import type { TableSettings } from '@/features/generic/models/table_settings'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { cupService } from '@/features/cup/services/cup.service'

const authStore = useAuthStore()
const { t } = useI18n()

const queryKey: string = 'cups'
const entityLabel: string = 'cup'
const settingStoreSuffix: string = 'cup'
const listLabel = computed(() => t('labels.cup', 2))
const columns: GenericListColumn[] = [
    { label: 'labels.name', field: 'name', sortable: true, filterable: true, filterType: 'input' },
    { label: 'labels.year', field: 'year', sortable: true },
    { label: 'labels.event', label_count: 2, field: 'events', type: 'list', sortable: true },
]

const eventsQuery = useQuery({
    queryKey: ['events'],
    queryFn: () => eventService.getAllUnpaged(t),
    select: data => data ?? [],
})

const initialTableSettings: TableSettings = {
    first: 0,
    rows: 10,
    page: 0,
    paginator: true,
    paginatorPosition: 'both',
    rowsPerPageOptions: [5, 10, 20, 50, 100],
    sortMode: 'multiple',
    multiSortMeta: undefined,
    sortField: 'year',
    sortOrder: -1,
    nullSortOrder: 1,
    defaultSortOrder: 1,
    filters: undefined,
}
</script>

<template>
    <GenericList
        :entity-service="cupService"
        :query-key="queryKey"
        :list-label="listLabel"
        :entity-label="entityLabel"
        router-prefix="cup"
        :settings-store-suffix="settingStoreSuffix"
        :columns="columns"
        :changeable="authStore.isAdmin"
        filter-display="row"
        :visible="true"
        :initial-table-settings="initialTableSettings"
    >
        <template v-if="eventsQuery.data.value" #events="{ value }">
            <div>{{ value?.name }}</div>
        </template>
        <template #extra_list_actions />
        <template #extra_row_actions="{ value }">
            <router-link :to="{ name: 'cup-results', params: { id: value.id } }">
                <Button
                    v-tooltip="t('labels.results')"
                    icon="pi pi-list"
                    class="mr-2 my-1"
                    :aria-label="t('labels.results')"
                    outlined
                    raised
                    rounded
                />
            </router-link>
        </template>
    </GenericList>
</template>

<style scoped></style>
