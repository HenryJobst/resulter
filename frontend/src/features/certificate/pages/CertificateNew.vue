<script setup lang="ts">
import type { Certificate } from '@/features/certificate/model/certificate'
import GenericNew from '@/features/generic/pages/GenericNew.vue'
import CertificateForm from '@/features/certificate/widgets/CertificateForm.vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { computed, ref } from 'vue'
import { certificateService } from '@/features/certificate/services/certificate.service'
import type { EventKey } from '@/features/event/model/event_key'

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['certificates']
const entityLabel: string = 'certificate'
const newLabel = computed(() => t('messages.new_entity', { entity: t('labels.certificate') }))

const props = defineProps<{ event?: EventKey | null; locale?: string }>()

const formData = ref<Certificate | Omit<Certificate, 'id'>>({
  name: '',
  layoutDescription: '',
  blankCertificate: null,
  event: props.event ? props.event : null
})
</script>

<template>
  <GenericNew
    :entity="formData"
    :entity-service="certificateService"
    :query-key="queryKey"
    :entity-label="entityLabel"
    :new-label="newLabel"
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
  </GenericNew>
</template>

<style scoped></style>
