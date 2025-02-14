<script setup lang="ts">
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { organisationService } from '@/features/organisation/services/organisation.service'
import OrganisationForm from '@/features/organisation/widgets/OrganisationForm.vue'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['organisation']
const entityLabel: string = 'organisation'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.organisation') }))
</script>

<template>
    <GenericEdit
        :entity-service="organisationService"
        :query-key="queryKey"
        :entity-id="props.id"
        :entity-label="entityLabel"
        :edit-label="editLabel"
        router-prefix="organisation"
        :visible="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <OrganisationForm
                v-if="formData"
                v-model="formData.data"
                :entity-service="organisationService"
                :query-key="queryKey"
            />
        </template>
    </GenericEdit>
</template>

<style scoped></style>
