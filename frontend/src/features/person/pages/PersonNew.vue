<script setup lang="ts">
import type { Person } from '@/features/person/model/person'
import GenericNew from '@/features/generic/pages/GenericNew.vue'
import PersonForm from '@/features/person/widgets/PersonForm.vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { computed, ref } from 'vue'
import { personService } from '@/features/person/services/person.service'

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['persons']
const entityLabel: string = 'person'
const newLabel = computed(() => t('messages.new_entity', { entity: t('labels.person') }))

const formData = ref<Person | Omit<Person, 'id'>>({
  familyName: '',
  givenName: '',
  gender: { id: 'M' },
  birthDate: ''
})
</script>

<template>
  <GenericNew
    :entity="formData"
    :entity-service="personService"
    :query-key="queryKey"
    :entity-label="entityLabel"
    :new-label="newLabel"
    :router-prefix="'person'"
    :changeable="authStore.isAdmin"
  >
    <template v-slot:default="{ formData }">
      <PersonForm
        :person="formData"
        :entity-service="personService"
        :query-key="queryKey"
        :v-model="formData"
      />
    </template>
  </GenericNew>
</template>

<style scoped></style>
