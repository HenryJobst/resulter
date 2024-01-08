<script setup lang="ts">
import type { Organisation } from '@/features/organisation/model/organisation'
import GenericNew from '@/features/generic/pages/GenericNew.vue'
import { GenericService } from '@/features/generic/services/GenericService'
import OrganisationForm from '@/features/organisation/widgets/OrganisationForm.vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { ref } from 'vue'

const { t } = useI18n()
const authStore = useAuthStore()
const organisationService = new GenericService<Organisation>('/organisation')
const queryKey: string[] = ['organisations']
const entityLabel: string = 'organisation'
const newLabel: string = t('messages.new_entity', { entity: t('labels.organisation') })

const formData = ref<Organisation | Omit<Organisation, 'id'>>({
  name: '',
  shortName: '',
  type: { id: 'OTHER' },
  country: { id: 1, code: 'GER', name: 'GER' },
  organisations: []
})
</script>

<template>
  <GenericNew
    :entity="formData"
    :entity-service="organisationService"
    :query-key="queryKey"
    :entity-label="entityLabel"
    :new-label="newLabel"
    :router-prefix="'organisation'"
    :changeable="authStore.isAdmin"
  >
    <template v-slot:default="{ formData }">
      <OrganisationForm
        :organisation="formData"
        :entity-service="organisationService"
        :query-key="queryKey"
      />
    </template>
  </GenericNew>
</template>

<style scoped></style>
