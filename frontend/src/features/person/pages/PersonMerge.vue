<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed, ref } from 'vue'
import Dropdown from 'primevue/dropdown'
import { useQuery } from '@tanstack/vue-query'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import PersonForm from '@/features/person/widgets/PersonForm.vue'
import { PersonService, personService } from '@/features/person/services/person.service'
import type { Person } from '@/features/person/model/person'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['person']
const entityLabel: string = 'person'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.person') }))
const personForMerge = ref<Person | null>(null)

function mergePerson() {
    console.log(`mergePerson: ${personForMerge.value?.id ?? 0}`)
}

const personDoublesQuery = useQuery({
    queryKey: ['personDoubles', props.id],
    queryFn: () => PersonService.getPersonDoubles(Number.parseInt(props.id), t),
})

const personOptions = computed(() => {
    if (personDoublesQuery.data.value) {
        return personDoublesQuery.data.value.map(option => ({
            ...option,
            label: `${option.familyName}, ${option.givenName}, ${option.birthDate}, ${t(`gender.${option.gender.id.toLocaleUpperCase()}`)})`,
        }))
    }
    return []
})
</script>

<template>
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
                    :changeable="authStore.isAdmin"
                    :additional-submit-function="mergePerson"
                >
                    <template #default="{ formData }">
                        <PersonForm
                            v-if="formData"
                            v-model="formData.data"
                            :person="formData.data as Person"
                            :entity-service="personService"
                            :query-key="queryKey"
                        />
                    </template>
                </GenericEdit>
                <!-- GenericEdit
                    v-if="otherId && otherId !== ''"
                    :entity-service="personService"
                    :query-key="queryKey"
                    :entity-id="otherId"
                    :entity-label="entityLabel"
                    :edit-label="editLabel"
                    router-prefix="person"
                    :changeable="false"
                    :additional-submit-function="mergePersons"
                >
                    <template #default="{ formData }">
                        <PersonForm
                            v-if="formData"
                            v-model="formData.data"
                            :person="formData.data as Person"
                            :entity-service="personService"
                            :query-key="queryKey"
                        />
                    </template>
                </GenericEdit -->
            </div>
        </div>
        <div class="flex flex-col ml-3">
            <div class="flex flex-row">
                <label for="other_id" class="col-fixed w-40">{{ t('labels.person') }}</label>
                <div class="col">
                    <span v-if="personDoublesQuery.status.value === 'pending'">{{
                        t('messages.loading')
                    }}</span>
                    <span v-else-if="personDoublesQuery.status.value === 'error'">
                        {{ t('messages.error', { message: personDoublesQuery.error.toLocaleString() }) }}
                    </span>
                    <Dropdown
                        v-else-if="personDoublesQuery.data"
                        id="personToMerge"
                        v-model="personForMerge"
                        :options="personOptions"
                        option-label="label"
                        data-key="id"
                        :placeholder="t('messages.select')"
                        class="w-full md:w-14rem"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped></style>
