<script setup lang="ts">
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import MultiSelect, { type MultiSelectChangeEvent } from 'primevue/multiselect'
import { useI18n } from 'vue-i18n'
import type { Organisation } from '@/features/organisation/model/organisation'
import { useQuery } from '@tanstack/vue-query'
import { OrganisationService } from '@/features/organisation/services/organisation.service'
import { countryService } from '@/features/country/services/country.service'
import { computed, ref } from 'vue'
import type { OrganisationKey } from '@/features/organisation/model/organisation_key'

const { t } = useI18n()

const props = defineProps<{
  organisation?: Organisation
  entityService: OrganisationService
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

const localizedOrganisationTypeOptions = computed(() => {
  if (organisationTypesQuery.data.value) {
    return organisationTypesQuery.data.value.map((option) => ({
      ...option,
      label: t(`organisation_type.${option.id.toLocaleUpperCase()}`)
    }))
  }
  return []
})

const countryQuery = useQuery({
  queryKey: ['countries'],
  queryFn: () => countryService.getAll(t)
})

const emit = defineEmits(['update:modelValue'])

const organisation = computed({
  get: () => props.organisation,
  set: (value) => emit('update:modelValue', value)
})

const childOrganisations = ref<number[]>(
  organisation.value ? organisation.value.childOrganisations.map((org) => org.id) : []
)

const getOrganisationKeysFromIds = (ids: number[]): OrganisationKey[] | null => {
  if (!organisationQuery.data.value || !organisationQuery.data.value.content) {
    return null
  }
  return ids
    .map((id) => {
      return organisationQuery.data.value?.content.find((b) => b.id === id)
    })
    .filter((org) => org !== undefined)
    .map((org) => {
      return {
        id: org!.id,
        name: org!.name
      }
    })
}

const handleSelectionChange = (event: MultiSelectChangeEvent) => {
  if (organisation.value && organisationQuery.data.value && organisationQuery.data.value.content) {
    organisation.value.childOrganisations = getOrganisationKeysFromIds(event.value)!
  }
}
</script>

<template>
  <div v-if="organisation" class="flex flex-col">
    <div class="flex flex-row">
      <label for="name" class="col-fixed w-40">{{ t('labels.name') }}</label>
      <div class="col">
        <InputText v-model="organisation.name" type="text" id="name"></InputText>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="shortname" class="col-fixed w-40">{{ t('labels.short_name') }}</label>
      <div class="col">
        <InputText v-model="organisation.shortName" type="text" id="shortname"></InputText>
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
          v-model="organisation.type"
          :options="localizedOrganisationTypeOptions"
          optionLabel="label"
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
          v-else-if="countryQuery.data && countryQuery.data.value"
          id="country"
          v-model="organisation.country.id"
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
        t('labels.child_organisation', 2)
      }}</label>
      <div class="col">
        <span v-if="organisationQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
        <span v-else-if="organisationQuery.status.value === 'error'">
          {{ t('messages.error', { message: organisationQuery.error.toLocaleString() }) }}
        </span>

        <div v-else-if="organisationQuery.data && organisationQuery.data.value" class="card">
          <MultiSelect
            id="organisations"
            v-model="childOrganisations"
            @change="handleSelectionChange"
            :options="organisationQuery.data.value.content"
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
