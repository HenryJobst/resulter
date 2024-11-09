<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { useToast } from 'primevue/usetoast'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import CupForm from '@/features/cup/widgets/CupForm.vue'
import type { Cup } from '@/features/cup/model/cup'
import { cupService } from '@/features/cup/services/cup.service'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Spinner from '@/components/SpinnerComponent.vue'

import { toastDisplayDuration } from '@/utils/constants'
import type { RestResult } from '@/features/generic/models/rest_result'

const props = defineProps<{ id: number, locale?: string }>()
const authStore = useAuthStore()

const { t } = useI18n()

const router = useRouter()

function navigateCupToList() {
    router.replace({ name: 'cup-list' })
}

const queryClient = useQueryClient()

const cupQuery = useQuery({
    queryKey: ['cups', { id: props.id }],
    queryFn: () => cupService.getById(props.id, t),
    initialData: () =>
        queryClient
            .getQueryData<RestResult<Cup>>(['cups'])
            ?.content.find(cup => cup.id === props.id),
    initialDataUpdatedAt: () => queryClient.getQueryState(['cups'])?.dataUpdatedAt,
})

const toast = useToast()

const cupMutation = useMutation({
    mutationFn: (cup: Cup) => cupService.update(cup, t),
    onSuccess: (cup) => {
        queryClient.setQueryData(['cups', { id: props.id }], cup)
        queryClient.invalidateQueries({ queryKey: ['cups'] })
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.cup_changed'),
            life: toastDisplayDuration,
        })
        navigateCupToList()
    },
})

function cupSubmitHandler(cup: Cup) {
    cupMutation.mutate(cup)
}
</script>

<template>
    <div v-bind="$attrs">
        <h1>{{ t('messages.edit_cup', { id: props.id }) }}</h1>

        <span v-if="cupQuery.status.value === 'pending' || cupMutation.status.value === 'pending'">
            {{ t('messages.loading') }}
            <Spinner />
        </span>
        <span v-else-if="cupQuery.status.value === 'error' || cupMutation.status.value === 'error'">
            <ErrorMessage :message="t('messages.error', { message: cupQuery.error.value })" />
            <ErrorMessage :message="t('messages.error', { message: cupMutation.error.value })" />
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
                :aria-label="t('labels.back')"
                class="pi pi-arrow-left ml-2"
                severity="secondary"
                type="reset"
                outlined
                raised
                rounded
                @click="navigateCupToList"
            />
        </CupForm>
    </div>
</template>

<style scoped>
h1 {
    margin-bottom: 1rem;
}
</style>
