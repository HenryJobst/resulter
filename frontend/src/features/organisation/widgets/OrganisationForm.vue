<script setup lang="ts">
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import InputText from 'primevue/inputtext'
import { useI18n } from 'vue-i18n'
import type { Organisation } from '@/features/organisation/model/organisation'
import { useQuery } from '@tanstack/vue-query'
import { OrganisationService } from '@/features/organisation/services/organisation.service'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import { CountryService } from '@/features/country/services/country.service'

const { t } = useI18n()

const props = defineProps<{
  organisation?: Organisation
  entityService: IGenericService<Organisation>
  queryKey: string[]
}>()

const organisationQuery = useQuery({
  queryKey: props.queryKey,
  queryFn: () => props.entityService.getAll(t)
})

const organisationTypesQuery = useQuery({
  queryKey: ['organisation_types'],
  queryFn: () => OrganisationService.getOrganisationTypes(t)
})

const countryQuery = useQuery({
  queryKey: ['countries'],
  queryFn: () => CountryService.getAll(t)
})
</script>

<template>
  <div v-if="props.organisation" class="flex flex-col">
    <div class="flex flex-row">
      <label for="name" class="col-fixed w-40">{{ t('labels.name') }}</label>
      <div class="col">
        <InputText v-model="props.organisation.name" type="text" id="name"></InputText>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="shortname" class="col-fixed w-40">{{ t('labels.short_name') }}</label>
      <div class="col">
        <InputText v-model="props.organisation.shortName" type="text" id="shortname"></InputText>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="type" class="col-fixed w-40">{{ t('labels.type') }}</label>
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
          v-model="props.organisation.type"
          :options="organisationTypesQuery.data.value"
          optionLabel="id"
          data-key="id"
          :placeholder="t('messages.select')"
          class="w-full md:w-14rem"
        />
      </div>
    </div>
    <div class="flex flex-row">
      <label for="country" class="col-fixed w-40">{{ t('labels.country') }}</label>
      <div class="col">
        <span v-if="countryQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
        <span v-else-if="countryQuery.status.value === 'error'">
          {{ t('messages.error', { message: countryQuery.error.toLocaleString() }) }}
        </span>
        <Dropdown
          v-else-if="countryQuery.data"
          id="country"
          v-model="props.organisation.country"
          :options="countryQuery.data.value"
          optionLabel="name"
          optionValue="id"
          data-key="id"
          :placeholder="t('messages.select')"
          class="w-full md:w-14rem"
        />
      </div>
    </div>
    <div class="flex flex-row">
      <label for="organisations" class="col-fixed w-40">{{
        t('labels.parent_organisation')
      }}</label>
      <div class="col">
        <span v-if="organisationQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
        <span v-else-if="organisationQuery.status.value === 'error'">
          {{ t('messages.error', { message: organisationQuery.error.toLocaleString() }) }}
        </span>

        <div v-else-if="organisationQuery.data" class="card">
          <MultiSelect
            id="organisations"
            v-model="props.organisation.organisations"
            :options="organisationQuery.data.value"
            data-key="id"
            filter
            optionLabel="name"
            optionValue="id"
            :placeholder="t('messages.select')"
            class="w-full md:w-20rem"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
