<script setup lang="ts">
import type { Person } from '@/features/person/model/person'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import Button from 'primevue/button'
import Select from 'primevue/select'
import { useToast } from 'primevue/usetoast'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/features/auth/store/auth.store'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { formatYear } from '@/features/generic/services/GenericFunctions'
import { PersonService, personService } from '@/features/person/services/person.service'
import PersonForm from '@/features/person/widgets/PersonForm.vue'
import { toastDisplayDuration } from '@/utils/constants'

const props = defineProps<{ id: string, locale?: string }>()
const route = useRoute()

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

// Automatisch das erste Duplikat auswählen
watch(() => personDoublesQuery.data.value, (newData) => {
    if (newData && newData.length > 0 && !personForMerge.value) {
        personForMerge.value = newData[0]
    }
}, { immediate: true })

// Refs für die formData-Objekte der beiden Personen
const leftPersonData = ref<Person | null>(null)
const rightPersonData = ref<Person | null>(null)

// Funktionen zum Übertragen von Feldwerten von rechts nach links
function transferFamilyName() {
    if (leftPersonData.value && rightPersonData.value) {
        leftPersonData.value.familyName = rightPersonData.value.familyName
    }
}

function transferGivenName() {
    if (leftPersonData.value && rightPersonData.value) {
        leftPersonData.value.givenName = rightPersonData.value.givenName
    }
}

function transferBirthDate() {
    if (leftPersonData.value && rightPersonData.value) {
        leftPersonData.value.birthDate = rightPersonData.value.birthDate
    }
}

function transferGender() {
    if (leftPersonData.value && rightPersonData.value) {
        leftPersonData.value.gender = rightPersonData.value.gender
    }
}

const queryClient = useQueryClient()
function mergePerson() {
    console.log(`mergePerson: ${entityIdAsString?.value}`)
    const removeId = Number.parseInt(entityIdAsString.value!)
    PersonService.merge(Number.parseInt(props.id), removeId, t).then(() => {
        personForMerge.value = null
        queryClient.removeQueries({ queryKey: [...queryKey, removeId], exact: true })
        queryClient.removeQueries({ queryKey: [PERSON_DOUBLES, removeId], exact: true })
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
    <div class="flex flex-row gap-4">
        <div class="flex flex-col flex-1">
            <GenericEdit
                :entity-service="personService"
                :query-key="queryKey"
                :entity-id="props.id"
                :entity-label="entityLabel"
                :edit-label="editLabel"
                router-prefix="person"
                :visible="authStore.isAdmin"
                :changeable="authStore.isAdmin"
                :route-location="{ name: `person-merge`, params: { id: props.id }, query: route.query }"
            >
                <template #default="{ formData }">
                    <PersonForm
                        v-if="formData && (leftPersonData = formData.data as Person)"
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

        <!-- Transfer Buttons Column -->
        <div v-if="personForMerge" class="flex flex-col justify-start" style="padding-top: 2.2rem;">
            <div class="flex flex-row items-center" style="min-height: 3.3rem; margin-bottom: 0.5rem;">
                <Button
                    v-tooltip="t('labels.transfer_value')"
                    icon="pi pi-arrow-left"
                    text
                    rounded
                    size="small"
                    :aria-label="t('labels.transfer_value')"
                    @click="transferFamilyName"
                />
            </div>
            <div class="flex flex-row items-center" style="min-height: 3.3rem; margin-bottom: 0.5rem;">
                <Button
                    v-tooltip="t('labels.transfer_value')"
                    icon="pi pi-arrow-left"
                    text
                    rounded
                    size="small"
                    :aria-label="t('labels.transfer_value')"
                    @click="transferGivenName"
                />
            </div>
            <div class="flex flex-row items-center" style="min-height: 3.3rem; margin-bottom: 0.5rem;">
                <Button
                    v-tooltip="t('labels.transfer_value')"
                    icon="pi pi-arrow-left"
                    text
                    rounded
                    size="small"
                    :aria-label="t('labels.transfer_value')"
                    @click="transferBirthDate"
                />
            </div>
            <div class="flex flex-row items-center" style="min-height: 3.2rem; margin-bottom: 0.5rem;">
                <Button
                    v-tooltip="t('labels.transfer_value')"
                    icon="pi pi-arrow-left"
                    text
                    rounded
                    size="small"
                    :aria-label="t('labels.transfer_value')"
                    @click="transferGender"
                />
            </div>
        </div>

        <div class="flex flex-col flex-1">
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
                :route-location="{ name: `person-merge`, params: { id: props.id }, query: route.query }"
                :save-button-label="t('labels.merge')"
                :return-button-visible="false"
            >
                <template #default="{ formData }">
                    <PersonForm
                        v-if="formData && (rightPersonData = formData.data as Person)"
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
</template>

<style scoped></style>
