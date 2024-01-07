<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { GenericService } from '@/features/generic/services/GenericService'
import type { Organisation } from '@/features/organisation/model/organisation'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import MultiSelect from 'primevue/multiselect'
import InputText from 'primevue/inputtext'
import { useQuery } from '@tanstack/vue-query'
import Dropdown from 'primevue/dropdown'
import { OrganisationService } from '@/features/organisation/services/organisation.service'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const props = defineProps<{ id: string; locale?: string }>()

const authStore = useAuthStore()
const { t } = useI18n()
const organisationService = new GenericService<Organisation>('/organisation')
const queryKey: string[] = ['organisations']
const entityLabel: string = 'organisation'

const organisationQuery = useQuery({
  queryKey: queryKey,
  queryFn: () => organisationService.getAll(t)
})

const organisationTypesQuery = useQuery({
  queryKey: ['organisation_types'],
  queryFn: () => OrganisationService.getOrganisationTypes(t)
})
</script>

<template>
  <GenericEdit
    :entity-service="organisationService"
    :query-key="queryKey"
    :entity-id="props.id"
    :entity-label="entityLabel"
    :router-prefix="'organisation'"
    :changeable="authStore.isAdmin"
  >
    <template v-slot:default="{ formData }">
      <div v-if="formData" class="flex flex-col">
        <div class="flex flex-row">
          <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
          <div class="col">
            <InputText v-model="formData.name" type="text" id="name"></InputText>
          </div>
        </div>
        <div class="flex flex-row">
          <label for="shortname" class="col-fixed w-32">{{ t('labels.short_name') }}</label>
          <div class="col">
            <InputText v-model="formData.shortName" type="text" id="shortname"></InputText>
          </div>
        </div>
        <div class="flex flex-row">
          <label for="type" class="col-fixed w-32">{{ t('labels.type') }}</label>
          <div class="col">
            <span v-if="organisationTypesQuery.status.value === 'pending'">{{
              t('messages.loading')
            }}</span>
            <span v-else-if="organisationTypesQuery.status.value === 'error'">
              {{ t('messages.error', { message: organisationTypesQuery.error.toLocaleString() }) }}
            </span>
            <Dropdown
              v-else-if="organisationTypesQuery.data"
              id="type"
              v-model="formData.type"
              :options="organisationTypesQuery.data.value"
              optionLabel="id"
              data-key="id"
              :placeholder="t('messages.select')"
              class="w-full md:w-14rem"
            />
          </div>
        </div>
        <div class="flex flex-row">
          <label for="organisations" class="col-fixed w-32">{{ t('labels.organisation') }}</label>
          <div class="col">
            <span v-if="organisationQuery.status.value === 'pending'">{{
              t('messages.loading')
            }}</span>
            <span v-else-if="organisationQuery.status.value === 'error'">
              {{ t('messages.error', { message: organisationQuery.error.toLocaleString() }) }}
            </span>

            <div v-else-if="organisationQuery.data" class="card">
              <MultiSelect
                id="organisations"
                v-model="formData.organisations"
                :options="organisationQuery.data.value"
                data-key="id"
                filter
                optionLabel="name"
                :placeholder="t('messages.select')"
                class="w-full md:w-20rem"
              />
            </div>
          </div>
        </div>
      </div>
    </template>
  </GenericEdit>
</template>

<style scoped></style>
