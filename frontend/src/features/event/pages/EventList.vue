<script setup lang="ts">
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { organisationService } from '@/features/organisation/services/organisation.service'
import { eventService } from '@/features/event/services/event.service'
import type { TableSettings } from '@/features/generic/models/table_settings'

const authStore = useAuthStore()
const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const queryKey: string = 'events'
const entityLabel: string = 'event'
const settingStoreSuffix: string = 'event'
const listLabel = computed(() => t('labels.event', 2))
const columns: GenericListColumn[] = [
    { label: 'labels.name', field: 'name', sortable: true, filterable: false, filterType: 'input' },
    { label: 'labels.date', field: 'startTime', type: 'date', sortable: true },
    { label: 'labels.time', field: 'startTime', type: 'time', sortable: true },
    { label: 'labels.state', field: 'state', type: 'enum', sortable: true },
    { label: 'labels.organisation', field: 'organisations', type: 'list', sortable: true },
]

const organisationQuery = useQuery({
    queryKey: ['organisations'],
    queryFn: () => organisationService.getAll(t),
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
    filters: null,
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
        <template v-if="organisationQuery.data.value" #organisations="{ value }">
            <div>{{ value?.name }}</div>
        </template>
        <template #extra_list_actions>
            <router-link v-if="authStore.isAuthenticated" class="ml-2" :to="{ name: 'event-import' }">
                <Button icon="pi pi-upload" :label="t('labels.import')" outlined />
            </router-link>
        </template>
        <template #extra_row_actions="{ value }">
            <router-link :to="{ name: 'event-results', params: { id: value.id } }">
                <Button icon="pi pi-list" class="mr-2" :label="t('labels.results')" outlined />
            </router-link>
        </template>
    </GenericList>
</template>

<style scoped></style>
