<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { TableSettings } from '@/features/generic/models/table_settings'
import { useQuery } from '@tanstack/vue-query'
import Button from 'primevue/button'
import Chip from 'primevue/chip'
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
    { label: 'labels.name', field: 'name', sortable: true, filterable: true, filterType: 'input', style: 'min-width: 18rem;' },
    { label: 'labels.date', field: 'startTime', type: 'date', sortable: true, style: 'width: 14rem; max-width: 14rem;' },
    { label: 'labels.time', field: 'startTime', type: 'time', sortable: true, style: 'width: 6rem; max-width: 6rem;' },
    { label: 'labels.organisation', field: 'organisations', type: 'list', sortable: false, style: 'min-width: 15rem;' },
    { label: 'labels.split_times', field: 'hasSplitTimes', type: 'custom', sortable: true, style: 'width: 8rem; max-width: 8rem;' },
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
        filter-display="row"
        :visible="true"
        :initial-table-settings="initialTableSettings"
    >
        <template v-if="organisationQuery.data" #organisations="{ value }">
            <Chip class="organisation-chip">
                <span class="organisation-name">{{ value?.name }}</span>
            </Chip>
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

<style scoped>
/* Organisation Chip Styling */
.organisation-chip {
    display: inline-flex;
    align-items: center;
    gap: 0.375rem;
    margin-right: 0.375rem;
    margin-bottom: 0.125rem;
    padding: 0.125rem 0.5rem;
    border-radius: 6px;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(59, 130, 246, 0.02) 100%);
    border: 1px solid rgba(59, 130, 246, 0.2);
    font-size: 0.8125rem;
}

.organisation-name {
    font-weight: 500;
    color: rgb(var(--text-primary));
}

/* Force column widths */
:deep(.p-datatable-tbody > tr > td),
:deep(.p-datatable-thead > tr > th) {
    white-space: nowrap;
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
    .organisation-chip {
        background: linear-gradient(135deg, rgba(59, 130, 246, 0.12) 0%, rgba(59, 130, 246, 0.04) 100%);
        border-color: rgba(59, 130, 246, 0.25);
    }
}
</style>
