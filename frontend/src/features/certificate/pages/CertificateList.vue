<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import { certificateService } from '@/features/certificate/services/certificate.service'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

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
        sortable: true,
        filterable: true,
        filterType: 'input',
    },
    {
        label: 'labels.background',
        field: 'blankCertificate.fileName',
        sortable: true,
        filterable: true,
        filterType: 'input',
    },
    {
        label: 'labels.background_preview',
        field: 'blankCertificate.thumbnailContent',
        type: 'image',
        sortable: false,
        filterable: false,
    },
]
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
    />
</template>

<style scoped></style>
