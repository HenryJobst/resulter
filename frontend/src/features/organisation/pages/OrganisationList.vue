<script setup lang="ts">
import GenericList from '@/features/generic/pages/GenericList.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { organisationService } from '@/features/organisation/services/organisation.service'
import { countryService } from '@/features/country/services/country.service'

const authStore = useAuthStore()
const { t } = useI18n()

const queryKey: string[] = ['organisations']
const entityLabel: string = 'organisation'
const listLabel = computed(() => t('labels.organisation', 2))
const columns: GenericListColumn[] = [
  { label: 'labels.no', field: 'id' },
  { label: 'labels.name', field: 'name' },
  { label: 'labels.short_name', field: 'shortName' },
  { label: 'labels.type', field: 'type', type: 'enum' },
  { label: 'labels.country', field: 'countryId', type: 'id' },
  { label: 'labels.child_organisation', field: 'organisationIds', type: 'list', label_count: 2 }
]

const organisationQuery = useQuery({
  queryKey: ['organisations'],
  queryFn: () => organisationService.getAll(t),
  select: (data) => data ?? []
})

const countryQuery = useQuery({
  queryKey: ['countries'],
  queryFn: () => countryService.getAll(t)
})
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
    :enum-type-label-prefixes="new Map([['type', 'organisation_type.']])"
  >
    <template v-slot:organisationIds="{ value }" v-if="organisationQuery.data.value">
      <div>{{ organisationQuery.data.value.find((org) => org.id === value)?.name }}</div>
    </template>
    <template v-slot:countryId="{ value }" v-if="countryQuery.data.value">
      <div>{{ countryQuery.data.value.find((c) => c.id === value)?.name }}</div>
    </template>
  </GenericList>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
