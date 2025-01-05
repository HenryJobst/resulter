<script setup lang="ts">
import { computed } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import { toastDisplayDuration } from '@/utils/constants'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Spinner from '@/components/SpinnerComponent.vue'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import type { GenericEntity } from '@/features/generic/models/GenericEntity'

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
        console.log(e)
        return props.entityService!.create(e, t)
    },
    onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: props.queryKey })
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.entity_created', { entity: entityLabel.value }),
            life: toastDisplayDuration,
        })
        navigateToList()
    },
})

function submitHandler() {
    if (formData)
        entityMutation.mutate(formData)
}

function navigateToList() {
    router.replace({ name: `${props.routerPrefix}-list` })
}
</script>

<template>
    <div v-if="changeable" v-bind="$attrs">
        <h1>{{ props.newLabel }}</h1>
        <div v-if="entityMutation.status.value === 'pending'">
            {{ t('messages.loading') }}
            <Spinner />
        </div>
        <div v-else-if="entityMutation.status.value === 'error'">
            <ErrorMessage :message="t('messages.error', { message: entityMutation.error.value })" />
        </div>
        <form @submit.prevent="submitHandler">
            <slot :form-data="{ data: formData }" />
            <div class="mt-2">
                <Button
                    v-if="changeable"
                    v-tooltip="t('labels.save')"
                    aria-label="t('labels.save')"
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
