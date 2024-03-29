<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import OrganisationForm from '@/features/organisation/widgets/OrganisationForm.vue'
import { organisationService } from '@/features/organisation/services/organisation.service'
import type { Organisation } from '@/features/organisation/model/organisation'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['organisations']
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
        :changeable="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <OrganisationForm
                v-if="formData"
                v-model="formData.data"
                :organisation="formData.data as Organisation"
                :entity-service="organisationService"
                :query-key="queryKey"
            />
        </template>
    </GenericEdit>
</template>

<style scoped></style>
