<script setup lang="ts">
import type { GenericEntity } from '@/features/generic/models/GenericEntity'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import Button from 'primevue/button'
import { useToast } from 'primevue/usetoast'
import { computed, toRaw } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import Spinner from '@/components/SpinnerComponent.vue'
import { toastDisplayDuration } from '@/utils/constants'

interface Props {
    entity: GenericEntity
    entityService: IGenericService<GenericEntity>
    queryKey: (string | number)[]
    entityLabel: string
    newLabel: string
    routerPrefix: string
    visible?: boolean
    changeable?: boolean
    savable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
    visible: true,
    changeable: true,
    savable: true,
})

const { t } = useI18n()
const router = useRouter()
const queryClient = useQueryClient()
const toast = useToast()

const formData = props.entity

const entityLabel = computed(() => (props.entityLabel ? t(`labels.${props.entityLabel}`) : ''))
const entityMutation = useMutation({
    mutationFn: (e: any) => {
        return props.entityService!.create(e, t)
    },
    onSuccess: async (_data) => {
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.entity_created', { entity: entityLabel.value }),
            life: toastDisplayDuration,
        })
        // Navigate first, then refetch will happen automatically on the list page
        await navigateToList()
        // Force refetch after navigation to ensure list is updated
        setTimeout(() => {
            queryClient.refetchQueries({ queryKey: props.queryKey })
        }, 100)
    },
    onError: (error: any) => {
        toast.add({
            severity: 'error',
            summary: t('messages.error'),
            detail: error?.response?.data?.message || error?.message || t('messages.unknown_error'),
            life: toastDisplayDuration * 2,
        })
    },
})

function submitHandler() {
    if (formData) {
        // Unwrap ref before sending to mutation
        const data = toRaw(formData)
        entityMutation.mutate(data)
    }
}

function navigateToList() {
    return router.replace({ name: `${props.routerPrefix}-list` })
}
</script>

<template>
    <div v-if="changeable" v-bind="$attrs">
        <h1>{{ props.newLabel }}</h1>
        <div v-if="entityMutation.status.value === 'pending'">
            {{ t('messages.loading') }}
            <Spinner />
        </div>
        <form @submit.prevent="submitHandler">
            <slot :form-data="{ data: formData }" />
            <div class="mt-2">
                <Button
                    v-if="changeable"
                    v-tooltip="t('labels.save')"
                    :aria-label="t('labels.save')"
                    icon="pi pi-save"
                    class="mt-2"
                    type="submit"
                    outlined
                    raised
                    rounded
                />
                <Button
                    v-if="changeable"
                    v-tooltip="t('labels.back')"
                    icon="pi pi-arrow-left"
                    :aria-label="t('labels.back')"
                    class="ml-2"
                    severity="secondary"
                    type="reset"
                    outlined
                    raised
                    rounded
                    @click="navigateToList"
                />
            </div>
        </form>
    </div>
</template>

<style scoped>
h1 {
    margin-bottom: 1rem;
}
</style>
