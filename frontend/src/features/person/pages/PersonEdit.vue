<script setup lang="ts">
import type { Person } from '@/features/person/model/person'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { personService } from '@/features/person/services/person.service'
import PersonForm from '@/features/person/widgets/PersonForm.vue'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['person']
const entityLabel: string = 'person'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.person') }))
</script>

<template>
    <GenericEdit
        :entity-service="personService"
        :query-key="queryKey"
        :entity-id="props.id"
        :entity-label="entityLabel"
        :edit-label="editLabel"
        router-prefix="person"
        :visible="authStore.isAdmin"
        :changeable="authStore.isAdmin"
        :savable="authStore.isAdmin"
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
</template>

<style scoped></style>
