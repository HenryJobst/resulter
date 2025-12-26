<script setup lang="ts">
import type { Organisation } from '@/features/organisation/model/organisation'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import GenericNew from '@/features/generic/pages/GenericNew.vue'
import { organisationService } from '@/features/organisation/services/organisation.service'
import OrganisationForm from '@/features/organisation/widgets/OrganisationForm.vue'

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['organisation']
const entityLabel: string = 'organisation'
const newLabel = computed(() => t('messages.new_entity', { entity: t('labels.organisation') }))

const localFormData = ref<Organisation | Omit<Organisation, 'id'>>({
    name: '',
    shortName: '',
    type: { id: 'Other' },
    country: { id: 1, name: 'GER' },
    childOrganisations: [],
})
</script>

<template>
    <GenericNew
        :entity="localFormData"
        :entity-service="organisationService"
        :query-key="queryKey"
        :entity-label="entityLabel"
        :new-label="newLabel"
        router-prefix="organisation"
        :changeable="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <OrganisationForm
                v-if="formData"
                v-model="formData.data"
                :entity-service="organisationService"
                :query-key="queryKey"
            />
        </template>
    </GenericNew>
</template>

<style scoped></style>
