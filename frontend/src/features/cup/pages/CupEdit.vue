<script setup lang="ts">
import type { Cup } from '@/features/cup/model/cup'
import { cupService } from '@/features/cup/services/cup.service'
import CupForm from '@/features/cup/widgets/CupForm.vue'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['cup']
const entityLabel: string = 'cup'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.cup') }))
</script>

<template>
    <GenericEdit
        :entity-service="cupService"
        :query-key="queryKey"
        :entity-id="props.id"
        :entity-label="entityLabel"
        :edit-label="editLabel"
        router-prefix="cup"
        :visible="authStore.isAdmin"
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
    </GenericEdit>
    <!-- div v-bind="$attrs">
        <h1>{{ t('messages.edit_cup', { id: props.id }) }}</h1>

        <span v-if="cupQuery.status.value === 'pending' || cupMutation.status.value === 'pending'">
            {{ t('messages.loading') }}
            <Spinner />
        </span>
        <CupForm
            v-else-if="cupQuery.data"
            :cup="cupQuery.data.value"
            @cup-submit="cupSubmitHandler"
        >
            <Button
                v-if="authStore.isAdmin"
                v-tooltip="t('labels.save')"
                :aria-label="t('labels.save')"
                class="pi pi-save"
                type="submit"
                outlined
                raised
                rounded
            />
            <Button
                v-tooltip="t('labels.back')"
                icon="pi pi-arrow-left"
                :aria-label="t('labels.back')"
                class="ml-2"
                severity="secondary"
                type="reset"
                outlined
                raised
                rounded
                @click="navigateCupToList"
            />
        </CupForm>
    </div -->
</template>

<style scoped>
h1 {
    margin-bottom: 1rem;
}
</style>
