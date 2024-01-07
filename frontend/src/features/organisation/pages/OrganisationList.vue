<script setup lang="ts">
import { GenericService } from '@/features/generic/services/GenericService'
import type { Organisation } from '@/features/organisation/model/organisation'

import GenericList from '@/features/generic/pages/GenericList.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import { useI18n } from 'vue-i18n'

const authStore = useAuthStore()
const { t } = useI18n()

const organisationService = new GenericService<Organisation>('/organisation')
const queryKey: string[] = ['organisations']
const entityLabel: string = 'organisation'
const listLabel: string = t('labels.organisation', 2)
const columns: GenericListColumn[] = [
  { label: 'labels.no', field: 'id' },
  { label: 'labels.name', field: 'name' },
  { label: 'labels.short_name', field: 'shortName' },
  { label: 'labels.type', field: 'type.id' },
  { label: 'labels.country', field: 'country.name' },
  {
    label: 'labels.parent_organisation',
    field: 'organisations',
    type: 'list',
    listElemField: 'shortName'
  }
]
</script>

<template v-if="authStore.isAdmin">
  <GenericList
    :entity-service="organisationService"
    :query-key="queryKey"
    :list-label="listLabel"
    :entity-label="entityLabel"
    :router-prefix="'organisation'"
    :columns="columns"
    :changeable="authStore.isAdmin"
  />
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
