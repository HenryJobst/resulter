<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed, ref } from 'vue'
import Select from 'primevue/select'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { useToast } from 'primevue/usetoast'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import PersonForm from '@/features/person/widgets/PersonForm.vue'
import { PersonService, personService } from '@/features/person/services/person.service'
import type { Person } from '@/features/person/model/person'
import { formatYear } from '@/features/generic/services/GenericFunctions'
import { toastDisplayDuration } from '@/utils/constants'

const props = defineProps<{ id: string, locale?: string }>()

const { t, locale } = useI18n()
const authStore = useAuthStore()
const toast = useToast()
const queryKey: string[] = ['person']
const entityLabel: string = 'person'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.person') }))

const PERSON_DOUBLES = 'personDoubles'
const personDoublesQueryKey = computed(() => [PERSON_DOUBLES, props.id])
const personDoublesQuery = useQuery({
    queryKey: personDoublesQueryKey.value,
    queryFn: () => PersonService.getPersonDoubles(Number.parseInt(props.id), t),
})

const personOptions = computed(() => {
    if (personDoublesQuery.data.value) {
        return personDoublesQuery.data.value.map(option => ({
            ...option,
            label: `${option.familyName}, ${option.givenName},
             ${option.birthDate ? formatYear(option.birthDate.toString(), locale.value) : ''},
             ${t(`gender.${option.gender.id.toLocaleUpperCase()}`)}`,
        }))
    }
    return []
})

const personForMerge = ref<Person | null>(null)
const entityIdAsString = computed(() => personForMerge.value?.id.toString())

const queryClient = useQueryClient()
function mergePerson() {
    console.log(`mergePerson: ${entityIdAsString?.value}`)
    const removeId = Number.parseInt(entityIdAsString.value!)
    PersonService.merge(Number.parseInt(props.id), removeId, t).then(() => {
        personForMerge.value = null
        queryClient.removeQueries({ queryKey: [...queryKey, removeId], exact: true })
        queryClient.invalidateQueries({ queryKey: [PERSON_DOUBLES], refetchType: 'all' })
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.entity_changed', { entity: entityLabel }),
            life: toastDisplayDuration,
        })
    }).catch((error) => {
        toast.add({
            severity: 'error',
            summary: t('messages.error'),
            detail: t('messages.error', { message: error.toLocaleString() }),
            life: toastDisplayDuration,
        })
    })
}
</script>

<template>
    <div class="flex flex-row mb-5">
        <div class="flex flex-col">
            <label for="other_id" class="mt-2 mb-3">{{ t('labels.person_merge') }}</label>
            <div class="flex flex-col align-content-center">
                <span v-if="personDoublesQuery.status.value === 'pending'">{{
                    t('messages.loading')
                }}</span>
                <span v-else-if="personDoublesQuery.status.value === 'error'">
                    {{ t('messages.error', { message: personDoublesQuery.error.toLocaleString() }) }}
                </span>
                <Select
                    v-else-if="personDoublesQuery.data"
                    id="personToMerge"
                    v-model="personForMerge"
                    :options="personOptions"
                    option-label="label"
                    data-key="id"
                    :placeholder="t('messages.select')"
                    class="w-full md:w-20rem"
                />
            </div>
        </div>
    </div>
    <div class="flex flex-row">
        <div class="flex flex-col">
            <div class="flex flex-row">
                <GenericEdit
                    :entity-service="personService"
                    :query-key="queryKey"
                    :entity-id="props.id"
                    :entity-label="entityLabel"
                    :edit-label="editLabel"
                    router-prefix="person"
                    :visible="authStore.isAdmin"
                    :changeable="authStore.isAdmin"
                    :route-location="{ name: `person-merge`, params: { id: props.id } }"
                >
                    <template #default="{ formData }">
                        <PersonForm
                            v-if="formData"
                            v-model="formData.data"
                            :person="formData.data as Person"
                            :entity-service="personService"
                            :query-key="queryKey"
                            :changeable="authStore.isAdmin"
                            :visible="authStore.isAdmin"
                        />
                    </template>
                </GenericEdit>
            </div>
        </div>
        <div class="flex flex-col ml-6">
            <div class="flex flex-row">
                <GenericEdit
                    v-if="personForMerge"
                    :entity-service="personService"
                    :query-key="queryKey"
                    :entity-id="entityIdAsString!"
                    :entity-label="entityLabel"
                    :edit-label="t('labels.person_merge')"
                    router-prefix="person"
                    :visible="authStore.isAdmin"
                    :changeable="false"
                    :savable="authStore.isAdmin"
                    :additional-submit-function="mergePerson"
                    :route-location="{ name: `person-merge`, params: { id: props.id } }"
                    :save-button-label="t('labels.merge')"
                    :return-button-visible="false"
                >
                    <template #default="{ formData }">
                        <PersonForm
                            v-if="formData"
                            v-model="formData.data"
                            :person="formData.data as Person"
                            :entity-service="personService"
                            :query-key="queryKey"
                            :visible="authStore.isAdmin"
                            :changeable="false"
                        />
                    </template>
                </GenericEdit>
            </div>
        </div>
    </div>
</template>

<style scoped></style>
