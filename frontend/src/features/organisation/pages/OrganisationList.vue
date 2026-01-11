<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { TableSettings } from '@/features/generic/models/table_settings.ts'
import Chip from 'primevue/chip'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { organisationService } from '@/features/organisation/services/organisation.service'

const authStore = useAuthStore()
const { t } = useI18n()

const queryKey: string = 'organisations'
const entityLabel: string = 'organisation'
const settingStoreSuffix: string = 'organisation'
const listLabel = computed(() => t('labels.organisation', 2))
const columns: GenericListColumn[] = [
    { label: 'labels.no', field: 'id', sortable: true, filterable: true, filterType: 'input', style: 'width: 8rem; max-width: 8rem;' },
    { label: 'labels.name', field: 'name', sortable: true, filterable: true, filterType: 'input', style: 'min-width: 15rem;' },
    { label: 'labels.short_name', field: 'shortName', sortable: true, filterable: true, filterType: 'input', style: 'width: 12rem; max-width: 12rem;' },
    { label: 'labels.type', field: 'type', type: 'custom', sortable: true, style: 'width: 10rem; max-width: 10rem;' },
    { label: 'labels.country', field: 'country', type: 'custom', sortable: false, style: 'width: 10rem; max-width: 10rem;' },
    {
        label: 'labels.child_organisation',
        field: 'childOrganisations',
        type: 'list',
        label_count: 2,
        sortable: false,
        style: 'min-width: 12rem;',
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
    stateKey: 'OrganisationList',
    scrollable: true,
    stripedRows: true,
}
</script>

<template>
    <GenericList
        :entity-service="organisationService"
        :query-key="queryKey"
        :list-label="listLabel"
        :entity-label="entityLabel"
        router-prefix="organisation"
        :settings-store-suffix="settingStoreSuffix"
        :columns="columns"
        :changeable="authStore.isAdmin"
        filter-display="row"
        :visible="authStore.isAdmin"
        :initial-table-settings="initialTableSettings"
    >
        <template #type="{ value }">
            <Chip class="type-chip">
                <span class="type-name">{{ t(`organisation_type.${value?.id?.toUpperCase()}`) }}</span>
            </Chip>
        </template>
        <template #country="{ value }">
            <Chip v-if="value?.name" class="country-chip">
                <span class="country-name">{{ value?.name }}</span>
            </Chip>
        </template>
        <template #childOrganisations="{ value }">
            <Chip class="child-org-chip">
                <span class="child-org-name">{{ value?.name }}</span>
            </Chip>
        </template>
    </GenericList>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}

/* Type Chip Styling (Orange) */
.type-chip {
    display: inline-flex;
    align-items: center;
    gap: 0.375rem;
    margin-right: 0.375rem;
    margin-bottom: 0.125rem;
    padding: 0.125rem 0.5rem;
    border-radius: 6px;
    background: linear-gradient(135deg, rgba(251, 146, 60, 0.08) 0%, rgba(251, 146, 60, 0.02) 100%);
    border: 1px solid rgba(251, 146, 60, 0.2);
    font-size: 0.8125rem;
}

.type-name {
    font-weight: 500;
    color: rgb(var(--text-primary));
}

/* Country Chip Styling (Green) */
.country-chip {
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

.country-name {
    font-weight: 500;
    color: rgb(var(--text-primary));
}

/* Child Organisation Chip Styling (Purple) */
.child-org-chip {
    display: inline-flex;
    align-items: center;
    gap: 0.375rem;
    margin-right: 0.375rem;
    margin-bottom: 0.125rem;
    padding: 0.125rem 0.5rem;
    border-radius: 6px;
    background: linear-gradient(135deg, rgba(147, 51, 234, 0.08) 0%, rgba(147, 51, 234, 0.02) 100%);
    border: 1px solid rgba(147, 51, 234, 0.2);
    font-size: 0.8125rem;
}

.child-org-name {
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
    .type-chip {
        background: linear-gradient(135deg, rgba(251, 146, 60, 0.12) 0%, rgba(251, 146, 60, 0.04) 100%);
        border-color: rgba(251, 146, 60, 0.25);
    }

    .country-chip {
        background: linear-gradient(135deg, rgba(34, 197, 94, 0.12) 0%, rgba(34, 197, 94, 0.04) 100%);
        border-color: rgba(34, 197, 94, 0.25);
    }

    .child-org-chip {
        background: linear-gradient(135deg, rgba(147, 51, 234, 0.12) 0%, rgba(147, 51, 234, 0.04) 100%);
        border-color: rgba(147, 51, 234, 0.25);
    }
}
</style>
