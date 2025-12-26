<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { TableSettings } from '@/features/generic/models/table_settings.ts'
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
    },
    {
        label: 'labels.event',
        field: 'event.name',
        sortable: false,
        filterable: false,
    },
    {
        label: 'labels.background',
        field: 'blankCertificate.fileName',
        sortable: false,
        filterable: false,
    },
    {
        label: 'labels.background_preview',
        field: 'blankCertificate.thumbnailContent',
        type: 'image',
        sortable: false,
        filterable: false,
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
    />
</template>

<style scoped></style>
