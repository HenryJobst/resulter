<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { certificateService } from '@/features/certificate/services/certificate.service'
import CertificateForm from '@/features/certificate/widgets/CertificateForm.vue'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['certificate']
const entityLabel: string = 'certificate'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.certificate') }))
</script>

<template>
    <GenericEdit
        :entity-service="certificateService"
        :query-key="queryKey"
        :entity-id="props.id"
        :entity-label="entityLabel"
        :edit-label="editLabel"
        router-prefix="certificate"
        :visible="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <CertificateForm
                v-if="formData"
                v-model="formData.data"
            />
        </template>
    </GenericEdit>
</template>

<style scoped></style>
