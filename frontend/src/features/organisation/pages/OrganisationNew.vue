<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed, ref } from 'vue'
import type { Organisation } from '@/features/organisation/model/organisation'
import GenericNew from '@/features/generic/pages/GenericNew.vue'
import OrganisationForm from '@/features/organisation/widgets/OrganisationForm.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { organisationService } from '@/features/organisation/services/organisation.service'

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['organisations']
const entityLabel: string = 'organisation'
const newLabel = computed(() => t('messages.new_entity', { entity: t('labels.organisation') }))

const formData = ref<Organisation | Omit<Organisation, 'id'>>({
    name: '',
    shortName: '',
    type: { id: 'Other' },
    country: { id: 1, name: 'GER' },
    childOrganisations: [],
})
</script>

<template>
    <GenericNew
        :entity="formData"
        :entity-service="organisationService"
        :query-key="queryKey"
        :entity-label="entityLabel"
        :new-label="newLabel"
        router-prefix="organisation"
        :changeable="authStore.isAdmin"
    >
        <template #default="{ myFormData }">
            <OrganisationForm
                v-if="myFormData"
                v-model="myFormData.value"
                :organisation="myFormData.value as Organisation"
                :entity-service="organisationService"
                :query-key="queryKey"
            />
        </template>
    </GenericNew>
</template>

<style scoped></style>
