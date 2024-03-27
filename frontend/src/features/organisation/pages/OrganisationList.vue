<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import { organisationService } from '@/features/organisation/services/organisation.service'

const authStore = useAuthStore()
const { t } = useI18n()

const queryKey: string = 'organisations'
const entityLabel: string = 'organisation'
const settingStoreSuffix: string = 'organisation'
const listLabel = computed(() => t('labels.organisation', 2))
const columns: GenericListColumn[] = [
    { label: 'labels.no', field: 'id', sortable: true },
    { label: 'labels.name', field: 'name', sortable: true },
    { label: 'labels.short_name', field: 'shortName', sortable: true },
    { label: 'labels.type', field: 'type', type: 'enum', sortable: true },
    { label: 'labels.country', field: 'country.name', sortable: true },
    {
        label: 'labels.child_organisation',
        field: 'childOrganisations',
        type: 'list',
        label_count: 2,
        sortable: false,
    },
]
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
        :visible="authStore.isAdmin"
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
