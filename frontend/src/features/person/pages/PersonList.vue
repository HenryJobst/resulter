<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { TableSettings } from '@/features/generic/models/table_settings'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { personService } from '@/features/person/services/person.service'
import Button from 'primevue/button'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const authStore = useAuthStore()
const { t } = useI18n()

const queryKey: string = 'persons'
const entityLabel: string = 'person'
const settingStoreSuffix: string = 'person'
const listLabel = computed(() => t('labels.person', 2))
const columns: GenericListColumn[] = [
    { label: 'labels.no', field: 'id', sortable: true, filterable: true, filterType: 'input' },
    { label: 'labels.family_name', field: 'familyName', sortable: true, filterable: true, filterType: 'input' },
    { label: 'labels.given_name', field: 'givenName', sortable: true, filterable: true, filterType: 'input' },
    { label: 'labels.gender', field: 'gender', type: 'enum', sortable: true },
    { label: 'labels.birth_year', field: 'birthDate', type: 'year', sortable: true },
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
    sortField: 'familyName',
    sortOrder: 1,
    nullSortOrder: 1,
    defaultSortOrder: 1,
    filters: undefined,
    removableSort: true,
    rowHover: true,
    stateStorage: 'session',
    stateKey: 'PersonList',
    scrollable: true,
    stripedRows: true,
}
</script>

<template>
    <GenericList
        v-if="authStore.isAdmin"
        :entity-service="personService"
        :query-key="queryKey"
        :list-label="listLabel"
        :entity-label="entityLabel"
        router-prefix="person"
        :settings-store-suffix="settingStoreSuffix"
        :columns="columns"
        :changeable="authStore.isAdmin"
        :enum-type-label-prefixes="new Map([['gender', 'gender.']])"
        filter-display="row"
        :visible="authStore.isAdmin"
        :initial-table-settings="initialTableSettings"
    >
        <template #extra_row_actions="{ value }">
            <router-link
                v-if="authStore.isAdmin"
                :to="{
                    name: `person-merge`,
                    params: { id: value.id },
                }"
            >
                <Button
                    v-if="authStore.isAdmin"
                    v-tooltip="t('labels.merge')"
                    icon="pi pi-link"
                    class="mr-2 my-1"
                    :aria-label="t('labels.merge')"
                    outlined
                    raised
                    rounded
                />
            </router-link>
        </template>
    </GenericList>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
