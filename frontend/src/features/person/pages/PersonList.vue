<script setup lang="ts">
import GenericList from '@/features/generic/pages/GenericList.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { personService } from '@/features/person/services/person.service'

const authStore = useAuthStore()
const { t } = useI18n()

const queryKey: string = 'persons'
const entityLabel: string = 'person'
const settingStoreSuffix: string = 'person'
const listLabel = computed(() => t('labels.person', 2))
const columns: GenericListColumn[] = [
  { label: 'labels.no', field: 'id', sortable: true },
  { label: 'labels.family_name', field: 'familyName', sortable: true },
  { label: 'labels.given_name', field: 'givenName', sortable: true },
  { label: 'labels.gender', field: 'gender', type: 'enum', sortable: true },
  { label: 'labels.birth_year', field: 'birthDate', type: 'year', sortable: true }
]
</script>

<template v-if="authStore.isAdmin">
  <GenericList
    :entity-service="personService"
    :query-key="queryKey"
    :list-label="listLabel"
    :entity-label="entityLabel"
    :router-prefix="'person'"
    :settings-store-suffix="settingStoreSuffix"
    :columns="columns"
    :changeable="authStore.isAdmin"
    :enum-type-label-prefixes="new Map([['gender', 'gender.']])"
  >
  </GenericList>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
