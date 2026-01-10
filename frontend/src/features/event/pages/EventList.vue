<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { TableSettings } from '@/features/generic/models/table_settings'
import { useQuery } from '@tanstack/vue-query'
import Button from 'primevue/button'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { eventService } from '@/features/event/services/event.service'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { organisationService } from '@/features/organisation/services/organisation.service'

const authStore = useAuthStore()
const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const queryKey: string = 'events'
const entityLabel: string = 'event'
const settingStoreSuffix: string = 'event'
const listLabel = computed(() => t('labels.event', 2))
const columns: GenericListColumn[] = [
    { label: 'labels.name', field: 'name', sortable: true, filterable: true, filterType: 'input' },
    { label: 'labels.date', field: 'startTime', type: 'date', sortable: true },
    { label: 'labels.time', field: 'startTime', type: 'time', sortable: true },
    { label: 'labels.state', field: 'state', type: 'enum', sortable: true },
    { label: 'labels.organisation', field: 'organisations', type: 'list', sortable: false },
    { label: 'labels.split_times', field: 'hasSplitTimes', type: 'custom', sortable: true },
]

const organisationQuery = useQuery({
    queryKey: ['organisations'],
    queryFn: () => organisationService.getAllUnpaged(t),
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
    sortField: 'startTime',
    sortOrder: 0,
    nullSortOrder: 1,
    defaultSortOrder: 1,
    filters: undefined,
    removableSort: true,
    rowHover: true,
    stateStorage: 'session',
    stateKey: 'EventList',
    scrollable: true,
    stripedRows: true,
}
</script>

<template>
    <GenericList
        :entity-service="eventService"
        :query-key="queryKey"
        :list-label="listLabel"
        :entity-label="entityLabel"
        router-prefix="event"
        :settings-store-suffix="settingStoreSuffix"
        :columns="columns"
        :changeable="authStore.isAdmin"
        :enum-type-label-prefixes="new Map([['state', 'event_state.']])"
        filter-display="row"
        :visible="true"
        :initial-table-settings="initialTableSettings"
    >
        <template v-if="organisationQuery.data" #organisations="{ value }">
            <div>{{ value?.name }}</div>
        </template>
        <template #hasSplitTimes="{ value }">
            <i v-if="value" class="pi pi-check text-green-600" />
            <i v-else class="pi pi-times text-gray-400" />
        </template>
        <template #extra_list_actions>
            <router-link
                v-if="authStore.isAuthenticated"
                class="ml-2"
                :to="{ name: 'event-import' }"
            >
                <Button
                    v-tooltip="t('labels.import')"
                    icon="pi pi-upload"
                    :aria-label="t('labels.import')"
                    outlined
                    raised
                    rounded
                />
            </router-link>
        </template>
        <template #extra_row_actions="{ value }">
            <router-link :to="{ name: 'event-results', params: { id: value.id } }">
                <Button
                    v-tooltip="t('labels.results')"
                    icon="pi pi-list"
                    class="mr-2"
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
