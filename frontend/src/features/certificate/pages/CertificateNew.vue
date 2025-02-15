<script setup lang="ts">
import type { Certificate } from '@/features/certificate/model/certificate'
import type { EventKey } from '@/features/event/model/event_key'
import { certificateService } from '@/features/certificate/services/certificate.service'
import CertificateForm from '@/features/certificate/widgets/CertificateForm.vue'
import GenericNew from '@/features/generic/pages/GenericNew.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ event?: EventKey | null, locale?: string }>()
const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['certificate']
const entityLabel: string = 'certificate'
const newLabel = computed(() => t('messages.new_entity', { entity: t('labels.certificate') }))

const localFormData = ref<Certificate | Omit<Certificate, 'id'>>({
    name: '',
    layoutDescription: '',
    blankCertificate: null,
    event: props.event ? props.event : null,
    primary: false,
})
</script>

<template>
    <GenericNew
        :entity="localFormData"
        :entity-service="certificateService"
        :query-key="queryKey"
        :entity-label="entityLabel"
        :new-label="newLabel"
        router-prefix="certificate"
        :changeable="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <CertificateForm
                v-if="formData"
                v-model="formData.data"
            />
        </template>
    </GenericNew>
</template>

<style scoped></style>
