<script setup lang="ts">
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { GenericService } from '@/features/generic/services/GenericService'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import GenericList from '@/features/generic/pages/GenericList.vue'

const authStore = useAuthStore()
const { t, locale } = useI18n() // same as `useI18n({ useScope: 'global' })`

const eventService = new GenericService<Event>('/event')
const queryKey: string[] = ['events']
const entityLabel: string = 'event'
const listLabel = computed(() => t('labels.event', 2))
const columns: GenericListColumn[] = [
  { label: 'labels.name', field: 'name' },
  { label: 'labels.date', field: 'startDate', type: 'date' },
  { label: 'labels.time', field: 'startTime', type: 'time' },
  { label: 'labels.class', label_count: 2, field: 'classes' }
]
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
  >
    <template v-slot:extra_list_actions>
      <router-link class="ml-2" :to="{ name: 'event-import' }" v-if="authStore.isAuthenticated">
        <Button icon="pi pi-upload" :label="t('labels.import')" outlined></Button>
      </router-link>
    </template>
  </GenericList>
</template>

<style scoped></style>
