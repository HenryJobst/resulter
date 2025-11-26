<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router'
import type { GenericEntity } from '@/features/generic/models/GenericEntity'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import Button from 'primevue/button'
import { useToast } from 'primevue/usetoast'
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import Spinner from '@/components/SpinnerComponent.vue'
import { toastDisplayDuration } from '@/utils/constants'

interface Props {
    entityService: IGenericService<GenericEntity>
    queryKey: (string | number)[]
    entityId: string
    entityLabel: string
    editLabel: string
    routerPrefix: string
    visible?: boolean
    changeable?: boolean
    savable?: boolean
    additionalSubmitFunction?: () => void
    routeLocation?: RouteLocationRaw
    saveButtonLabel?: string
    returnButtonVisible?: boolean
}

const props = withDefaults(defineProps<Props>(), {
    visible: true,
    changeable: true,
    savable: true,
    returnButtonVisible: true,
    additionalSubmitFunction: undefined,
    routeLocation: undefined,
    saveButtonLabel: undefined,
})

const { t } = useI18n()
const saveButtonLabel = computed(() => props.saveButtonLabel ?? t('labels.save'))

const router = useRouter()
const queryClient = useQueryClient()
const toast = useToast()

const formData = ref<GenericEntity | null>(null)

const entityQuery = useQuery({
    queryKey: [...props.queryKey!, props.entityId],
    queryFn: () => {
        return props.entityService?.getById(Number.parseInt(props.entityId), t)
    },
})

onMounted(() => {
    if (entityQuery.data.value) {
        formData.value = { ...entityQuery.data.value }
    }
})

watch(
    () => props.entityId,
    () => {
        entityQuery.refetch()
    },
)

// Watcher, der auf Änderungen in entityQuery.data reagiert
watch(
    () => entityQuery.data,
    (newData) => {
        if (newData && newData.value)
            formData.value = { ...newData.value }
        else formData.value = null // oder setzen Sie einen Default-Wert
    },
    {
        deep: true, // Falls nötig, um auf tiefergehende Änderungen innerhalb des Objekts zu reagieren
    },
)

const entityLabel = computed(() => (props.entityLabel ? t(`labels.${props.entityLabel}`) : ''))

const entityMutation = useMutation({
    mutationFn: (entity: GenericEntity) => props.entityService?.update(entity, t),
    onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: [`${props.queryKey!}s`] })
        queryClient.invalidateQueries({ queryKey: [`${props.queryKey!}`, props.entityId] })
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.entity_changed', { entity: entityLabel.value }),
            life: toastDisplayDuration,
        })
        router.push(
            props.routeLocation ? props.routeLocation : { name: `${props.routerPrefix}-list` },
        )
    },
})

function submitHandler() {
    if (props.additionalSubmitFunction) {
        props.additionalSubmitFunction()
    }
    if (props.visible && props.changeable && formData.value) {
        entityMutation.mutate(formData.value!)
    }
}

function navigateToList() {
    router.replace({ name: `${props.routerPrefix}-list` })
}
</script>

<template>
    <div v-if="visible" v-bind="$attrs">
        <h1>{{ props.editLabel }}</h1>
        <div
            v-if="
                entityQuery.status.value === 'pending' || entityMutation.status.value === 'pending'
            "
        >
            {{ t('messages.loading') }}
            <Spinner />
        </div>
        <form @submit.prevent="submitHandler">
            <slot :form-data="{ data: formData }" />
            <div class="mt-2">
                <Button
                    v-if="visible && savable"
                    v-tooltip="saveButtonLabel"
                    :aria-label="saveButtonLabel"
                    icon="pi pi-save"
                    class="mt-2"
                    type="submit"
                    outlined
                    raised
                    rounded
                />
                <Button
                    v-if="visible && returnButtonVisible"
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
