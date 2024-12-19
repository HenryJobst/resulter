<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import InputText from 'primevue/inputtext'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import PersonForm from '@/features/person/widgets/PersonForm.vue'
import { personService } from '@/features/person/services/person.service'
import type { Person } from '@/features/person/model/person'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['person']
const entityLabel: string = 'person'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.person') }))
const otherId: string = '2695'

function mergePersons() {
    console.log(`mergePersons: ${otherId}`)
}
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
                </GenericEdit>
                <div class="flex flex-col ml-3">
                    <div class="flex flex-row">
                        <label for="other_id" class="col-fixed w-40">{{ t('labels.person') }}</label>
                        <div class="col">
                            <InputText id="other_id" v-model="otherId" type="text" />
                        </div>
                    </div>
                </div>
                <GenericEdit
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
                </GenericEdit>
            </div>
        </div>
    </div>
</template>

<style scoped></style>
