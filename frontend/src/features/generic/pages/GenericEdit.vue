<script setup lang="ts">
import { type PropType, computed, onMounted, ref, watch } from 'vue'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import { toastDisplayDuration } from '@/utils/constants'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Spinner from '@/components/SpinnerComponent.vue'
import type { GenericEntity } from '@/features/generic/models/GenericEntity'

const props = defineProps({
    entityService: Object,
    queryKey: Array as PropType<(string | number)[]>,
    entityId: String,
    entityLabel: String,
    editLabel: String,
    routerPrefix: String,
    changeable: Boolean,
})

const { t } = useI18n()
const router = useRouter()
const queryClient = useQueryClient()
const toast = useToast()

const formData = ref<GenericEntity | null>(null)

const entityQuery = useQuery({
    queryKey: [props.queryKey, props.entityId],
    queryFn: () => props.entityService?.getById(props.entityId, t),
})

onMounted(() => {
    if (entityQuery.data.value)
        formData.value = { ...entityQuery.data.value }

    // console.log('GenericEdit:onMounted:formData', prettyPrint(formData.value))
})

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
        queryClient.invalidateQueries({ queryKey: props.queryKey })
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.entity_changed', { entity: entityLabel.value }),
            life: toastDisplayDuration,
        })
        router.push({ name: `${props.routerPrefix}-list` })
    },
})

function submitHandler() {
    entityMutation.mutate(formData.value!)
}

function navigateToList() {
    router.replace({ name: `${props.routerPrefix}-list` })
}
</script>

<template>
    <div v-if="changeable" v-bind="$attrs">
        <h1>{{ props.editLabel }}</h1>
        <div
            v-if="
                entityQuery.status.value === 'pending' || entityMutation.status.value === 'pending'
            "
        >
            {{ t('messages.loading') }}
            <Spinner />
        </div>
        <div
            v-else-if="
                entityQuery.status.value === 'error' || entityMutation.status.value === 'error'
            "
        >
            <ErrorMessage :message="t('messages.error', { message: entityQuery.error.value })" />
            <ErrorMessage :message="t('messages.error', { message: entityMutation.error.value })" />
        </div>
        <form @submit.prevent="submitHandler">
            <slot :form-data="{ data: formData }" />
            <div class="mt-2">
                <Button
                    v-if="changeable"
                    v-tooltip="t('labels.save')"
                    :aria-label="t('labels.save')"
                    class="pi pi-save mt-2"
                    type="submit"
                    outlined
                    raised
                    rounded
                />
                <Button
                    v-if="changeable"
                    v-tooltip="t('labels.back')"
                    :aria-label="t('labels.back')"
                    class="pi pi-arrow-left ml-2"
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
