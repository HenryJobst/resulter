<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { TableSettings } from '@/features/generic/models/table_settings.ts'
import Chip from 'primevue/chip'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { certificateService } from '@/features/certificate/services/certificate.service'
import GenericList from '@/features/generic/pages/GenericList.vue'

const authStore = useAuthStore()
const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const queryKey: string = 'certificate'
const entityLabel: string = 'certificate'
const settingStoreSuffix: string = 'certificate'
const listLabel = computed(() => t('labels.certificate', 2))
const columns: GenericListColumn[] = [
    {
        label: 'labels.name',
        field: 'name',
        sortable: true,
        filterable: true,
        filterType: 'input',
        style: 'min-width: 15rem;',
    },
    {
        label: 'labels.event',
        field: 'event',
        type: 'custom',
        sortable: false,
        filterable: false,
        style: 'min-width: 15rem;',
    },
    {
        label: 'labels.background',
        field: 'blankCertificate.fileName',
        sortable: false,
        filterable: false,
        style: 'min-width: 12rem;',
    },
    {
        label: 'labels.background_preview',
        field: 'blankCertificate.thumbnailContent',
        type: 'image',
        sortable: false,
        filterable: false,
        style: 'width: 8rem; max-width: 8rem;',
    },
]

const initialTableSettings: TableSettings = {
    first: 0,
    rows: 10,
    page: 0,
    paginator: true,
    paginatorPosition: 'both',
    rowsPerPageOptions: [5, 10, 20, 50, 100, 200, 500],
    sortMode: 'multiple',
    multiSortMeta: undefined,
    sortField: 'name',
    sortOrder: 1,
    nullSortOrder: 1,
    defaultSortOrder: 1,
    filters: undefined,
    removableSort: true,
    rowHover: true,
    stateStorage: 'session',
    stateKey: 'CertificateList',
    scrollable: true,
    stripedRows: true,
}
</script>

<template>
    <GenericList
        :entity-service="certificateService"
        :query-key="queryKey"
        :list-label="listLabel"
        :entity-label="entityLabel"
        router-prefix="certificate"
        :settings-store-suffix="settingStoreSuffix"
        :columns="columns"
        :changeable="authStore.isAdmin"
        filter-display="row"
        :visible="authStore.isAuthenticated"
        :edit-enabled="true"
        :delete-enabled="true"
        :initial-table-settings="initialTableSettings"
    >
        <template #event="{ value }">
            <Chip v-if="value?.name" class="event-chip">
                <span class="event-name">{{ value?.name }}</span>
            </Chip>
        </template>
    </GenericList>
</template>

<style scoped>
/* Event Chip Styling (Green) */
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
    font-size: 0.8125rem;
}

.event-name {
    font-weight: 500;
    color: rgb(var(--text-primary));
}

/* Force column widths */
:deep(.p-datatable-tbody > tr > td),
:deep(.p-datatable-thead > tr > th) {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
    .event-chip {
        background: linear-gradient(135deg, rgba(34, 197, 94, 0.12) 0%, rgba(34, 197, 94, 0.04) 100%);
        border-color: rgba(34, 197, 94, 0.25);
    }
}
</style>
