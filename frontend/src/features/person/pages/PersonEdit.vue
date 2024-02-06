<script setup lang="ts">
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import PersonForm from '@/features/person/widgets/PersonForm.vue'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { personService } from '@/features/person/services/person.service'
import type { Person } from '@/features/person/model/person'

const props = defineProps<{ id: string; locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['persons']
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
    :router-prefix="'person'"
    :changeable="authStore.isAdmin"
  >
    <template v-slot:default="{ formData }">
      <PersonForm
        :person="formData as Person"
        :entity-service="personService"
        :query-key="queryKey"
        :v-model="formData"
        v-if="formData"
      />
    </template>
  </GenericEdit>
</template>

<style scoped></style>
