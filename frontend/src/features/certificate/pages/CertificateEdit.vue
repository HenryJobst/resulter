<script setup lang="ts">
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { certificateService } from '@/features/certificate/services/certificate.service'
import type { Certificate } from '@/features/certificate/model/certificate'
import CertificateForm from '@/features/certificate/widgets/CertificateForm.vue'

const props = defineProps<{ id: string; locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['certificates']
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
    :router-prefix="'certificate'"
    :changeable="authStore.isAdmin"
  >
    <template v-slot:default="{ formData }">
      <CertificateForm
        :certificate="formData as Certificate"
        :entity-service="certificateService"
        :query-key="queryKey"
        :v-model="formData"
        v-if="formData"
      />
    </template>
  </GenericEdit>
</template>

<style scoped></style>
