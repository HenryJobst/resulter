<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { TableSettings } from '@/features/generic/models/table_settings.ts'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { organisationService } from '@/features/organisation/services/organisation.service'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const authStore = useAuthStore()
const { t } = useI18n()

const queryKey: string = 'organisations'
const entityLabel: string = 'organisation'
const settingStoreSuffix: string = 'organisation'
const listLabel = computed(() => t('labels.organisation', 2))
const columns: GenericListColumn[] = [
    { label: 'labels.no', field: 'id', sortable: true, filterable: true, filterType: 'input' },
    { label: 'labels.name', field: 'name', sortable: true, filterable: true, filterType: 'input' },
    { label: 'labels.short_name', field: 'shortName', sortable: true, filterable: true, filterType: 'input' },
    { label: 'labels.type', field: 'type', type: 'enum', sortable: true },
    { label: 'labels.country', field: 'country.name', sortable: false },
    {
        label: 'labels.child_organisation',
        field: 'childOrganisations',
        type: 'list',
        label_count: 2,
        sortable: false,
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
        :enum-type-label-prefixes="new Map([['type', 'organisation_type.']])"
        filter-display="row"
        :visible="authStore.isAdmin"
        :initial-table-settings="initialTableSettings"
    >
        <template #childOrganisations="{ value }">
            <div>{{ value?.name }}</div>
        </template>
    </GenericList>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
