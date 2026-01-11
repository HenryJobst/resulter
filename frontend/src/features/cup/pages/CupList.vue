<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { TableSettings } from '@/features/generic/models/table_settings'
import Button from 'primevue/button'
import Chip from 'primevue/chip'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { cupService } from '@/features/cup/services/cup.service'
import GenericList from '@/features/generic/pages/GenericList.vue'

const authStore = useAuthStore()
const { t } = useI18n()

const queryKey: string = 'cups'
const entityLabel: string = 'cup'
const settingStoreSuffix: string = 'cup'
const listLabel = computed(() => t('labels.cup', 2))
const columns: GenericListColumn[] = [
    { label: 'labels.name', field: 'name', sortable: true, filterable: true, filterType: 'input' },
    { label: 'labels.year', field: 'year', sortable: true, filterable: true, filterType: 'input', style: 'width: 10rem; max-width: 10rem;' },
    { label: 'labels.event', label_count: 2, field: 'events', type: 'list', sortable: false },
]

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
    removableSort: true,
    rowHover: true,
    stateStorage: 'session',
    stateKey: 'CupList',
    scrollable: true,
    stripedRows: true,
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
        <template #events="{ value }">
            <router-link
                :to="{ name: 'event-results', params: { id: value.id } }"
                target="_blank"
            >
                <Chip class="event-chip clickable-chip">
                    <span class="event-name">{{ value?.name }}</span>
                </Chip>
            </router-link>
        </template>
        <template #extra_list_actions />
        <template #extra_row_actions="{ value }">
            <router-link :to="{ name: 'cup-results', params: { id: value.id } }">
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
/* Event Chip Styling */
.event-chip {
    display: inline-flex;
    align-items: center;
    gap: 0.375rem;
    margin-right: 0.375rem;
    margin-bottom: 0.125rem;
    padding: 0.125rem 0.5rem;
    border-radius: 6px;
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.08) 0%, rgba(34, 197, 94, 0.02) 100%);
    border: 1px solid rgba(34, 197, 94, 0.2);
    transition: all 0.2s ease;
    font-size: 0.8125rem;
}

.clickable-chip {
    cursor: pointer;
}

.event-chip:hover {
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.12) 0%, rgba(34, 197, 94, 0.04) 100%);
    border-color: rgba(34, 197, 94, 0.3);
    transform: translateY(-1px);
    box-shadow: 0 2px 6px rgba(34, 197, 94, 0.12);
}

.event-name {
    font-weight: 500;
    color: rgb(var(--text-primary));
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
    .event-chip {
        background: linear-gradient(135deg, rgba(34, 197, 94, 0.12) 0%, rgba(34, 197, 94, 0.04) 100%);
        border-color: rgba(34, 197, 94, 0.25);
    }

    .event-chip:hover {
        background: linear-gradient(135deg, rgba(34, 197, 94, 0.18) 0%, rgba(34, 197, 94, 0.06) 100%);
        border-color: rgba(34, 197, 94, 0.35);
        box-shadow: 0 2px 6px rgba(34, 197, 94, 0.18);
    }
}
</style>
