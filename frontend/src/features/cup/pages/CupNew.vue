<script setup lang="ts">
import type { Cup } from '@/features/cup/model/cup'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { cupService } from '@/features/cup/services/cup.service'
import CupForm from '@/features/cup/widgets/CupForm.vue'
import GenericNew from '@/features/generic/pages/GenericNew.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['cup']
const entityLabel: string = 'cup'
const newLabel = computed(() => t('messages.new_entity', { entity: t('labels.cup') }))

const localFormData = ref<Cup | Omit<Cup, 'id'>>({
    name: '',
    type: null,
    year: new Date().getFullYear().valueOf(),
    events: [],
})
</script>

<template>
    <GenericNew
        :entity="localFormData"
        :entity-service="cupService"
        :query-key="queryKey"
        :entity-label="entityLabel"
        :new-label="newLabel"
        router-prefix="cup"
        :changeable="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <CupForm
                v-if="formData"
                v-model="formData.data"
                :cup="formData.data as Cup"
                :entity-service="cupService"
                :query-key="queryKey"
            />
        </template>
    </GenericNew>
</template>

<style scoped></style>
