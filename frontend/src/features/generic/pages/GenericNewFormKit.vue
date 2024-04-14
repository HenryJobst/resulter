<script setup lang="ts">
import { computed, type PropType, ref, toRef } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { useRouter } from 'vue-router'
import { toastDisplayDuration } from '@/utils/constants'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Spinner from '@/components/SpinnerComponent.vue'

const props = defineProps({
    schema: Array as PropType<any[]>,
    entityService: Object,
    queryKey: Array as PropType<(string | number)[]>,
    entityLabel: String,
    newLabel: String,
    routerPrefix: String,
    changeable: Boolean
})

const { t } = useI18n()
const router = useRouter()
const queryClient = useQueryClient()
const toast = useToast()

const model = defineModel({ required: true })

let extendedSchema = ref(props.schema)

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
            life: toastDisplayDuration
        })
        navigateToList()
    }
})

const submitHandler = async (fields: Object) => {
    if (fields)
        entityMutation.mutate(fields)
}

function navigateToList() {
    router.replace({ name: `${props.routerPrefix}-list` })
}

extendedSchema.value?.push({
    $cmp: 'Button',
    props: {
        label: toRef(() => t('labels.save')),
        severity: 'primary',
        outlined: true,
        onClick: submitHandler
    }
})

extendedSchema.value?.push({
    $cmp: 'Button',
    props: {
        label: toRef(() => t('labels.back')),
        severity: 'secondary',
        outlined: true,
        class: 'ml-2',
        onClick: navigateToList
    }
})

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
        <FormKit type="form" id="form" v-model="model" :actions="false">
            <FormKitSchema :schema="extendedSchema" :data="model" />
        </FormKit>
    </div>
</template>

<style scoped>
h1 {
    margin-bottom: 1rem;
}
</style>
