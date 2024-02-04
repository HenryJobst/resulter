<script setup lang="ts">
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { useQuery } from '@tanstack/vue-query'
import { organisationService } from '@/features/organisation/services/organisation.service'
import { eventService } from '@/features/event/services/event.service'

const authStore = useAuthStore()
const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const queryKey: string[] = ['events']
const entityLabel: string = 'event'
const listLabel = computed(() => t('labels.event', 2))
const columns: GenericListColumn[] = [
  { label: 'labels.name', field: 'name' },
  { label: 'labels.date', field: 'startTime', type: 'date' },
  { label: 'labels.time', field: 'startTime', type: 'time' },
  { label: 'labels.state', field: 'state', type: 'enum' },
  { label: 'labels.organisation', field: 'organisations', type: 'list' }
]

const organisationQuery = useQuery({
  queryKey: ['organisations'],
  queryFn: () => organisationService.getAll(t),
  select: (data) => data ?? []
})
</script>

<template>
  <GenericList
    :entity-service="eventService"
    :query-key="queryKey"
    :list-label="listLabel"
    :entity-label="entityLabel"
    :router-prefix="'event'"
    :columns="columns"
    :changeable="authStore.isAdmin"
    :enum-type-label-prefixes="new Map([['state', 'event_state.']])"
  >
    <template v-slot:organisations="{ value }" v-if="organisationQuery.data.value">
      <div>{{ organisationQuery.data.value.find((org) => org.id === value)?.name }}</div>
    </template>
    <template v-slot:extra_list_actions>
      <router-link class="ml-2" :to="{ name: 'event-import' }" v-if="authStore.isAuthenticated">
        <Button icon="pi pi-upload" :label="t('labels.import')" outlined></Button>
      </router-link>
    </template>
    <template v-slot:extra_row_actions="{ value }">
      <router-link :to="{ name: 'event-results', params: { id: value.id } }">
        <Button icon="pi pi-list" class="mr-2" :label="t('labels.results')" outlined />
      </router-link>
    </template>
  </GenericList>
</template>

<style scoped></style>
