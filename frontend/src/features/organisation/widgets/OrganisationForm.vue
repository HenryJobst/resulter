<script setup lang="ts">
import Select from 'primevue/select'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import { useI18n } from 'vue-i18n'
import { useQuery } from '@tanstack/vue-query'
import { computed, ref, watch } from 'vue'
import type { Organisation } from '@/features/organisation/model/organisation'
import { OrganisationService } from '@/features/organisation/services/organisation.service'
import { countryService } from '@/features/country/services/country.service'
import type { OrganisationKey } from '@/features/organisation/model/organisation_key'

const props = defineProps<{
    entityService: OrganisationService
    queryKey: string[]
}>()

const organisation = defineModel({
    type: Object as () => Organisation,
    default: null,
})

const { t } = useI18n()

const organisationQuery = useQuery({
    queryKey: props.queryKey,
    queryFn: () => props.entityService.getAll(t),
})

const organisationTypesQuery = useQuery({
    queryKey: ['organisation_types'],
    queryFn: () => OrganisationService.getOrganisationTypes(t),
})

const localizedOrganisationTypeOptions = computed(() => {
    if (organisationTypesQuery.data.value) {
        return organisationTypesQuery.data.value.map(option => ({
            ...option,
            label: t(`organisation_type.${option.id.toLocaleUpperCase()}`),
        }))
    }
    return []
})

const countryQuery = useQuery({
    queryKey: ['countries'],
    queryFn: () => countryService.getAll(t),
})

const childOrganisations = ref<number[]>()

watch(() => organisation.value, (newValue: Organisation) => {
    if (newValue)
        childOrganisations.value = newValue.childOrganisations.map(org => org.id)
})

function getOrganisationKeysFromIds(ids: number[]): OrganisationKey[] | null {
    if (!organisationQuery.data.value || !organisationQuery.data.value.content)
        return null

    return ids
        .map((id) => {
            return organisationQuery.data.value?.content.find(b => b.id === id)
        })
        .filter(org => org !== undefined)
        .map((org) => {
            return {
                id: org!.id,
                name: org!.name,
            }
        })
}

watch(() => childOrganisations.value, (newValue: number[] | undefined) => {
    if (organisation.value && newValue)
        organisation.value.childOrganisations = getOrganisationKeysFromIds(newValue)!
})
</script>

<template>
    <div v-if="organisation" class="flex flex-col">
        <div class="flex flex-row">
            <label for="name" class="col-fixed w-40">{{ t('labels.name') }}</label>
            <div class="col">
                <InputText id="name" v-model="organisation.name" type="text" />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="shortname" class="col-fixed w-40">{{ t('labels.short_name') }}</label>
            <div class="col">
                <InputText id="shortname" v-model="organisation.shortName" type="text" />
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
                <Select
                    v-else-if="organisationTypesQuery.data"
                    id="type"
                    v-model="organisation.type"
                    :options="localizedOrganisationTypeOptions"
                    option-label="label"
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
                <Select
                    v-else-if="countryQuery.data && countryQuery.data.value"
                    id="country"
                    v-model="organisation.country.id"
                    :options="countryQuery.data.value"
                    option-label="name"
                    option-value="id"
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
                        :options="organisationQuery.data.value.content"
                        data-key="id"
                        filter
                        option-label="name"
                        option-value="id"
                        :placeholder="t('messages.select')"
                        class="w-full md:w-20rem"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped></style>
