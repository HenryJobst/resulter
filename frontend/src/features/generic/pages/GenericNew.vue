<script setup lang="ts">
import { defineProps, type PropType, ref } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { toastDisplayDuration } from '@/utils/constants'
import { useRouter } from 'vue-router'
import FormComponent from '@/features/generic/widgets/GenericFormComponent.vue' // Ihr generisches Formular-Komponente

const props = defineProps({
  entityService: Object,
  queryKey: Array as PropType<(string | number)[]>,
  entityLabel: String,
  routerPrefix: String
})

const { t } = useI18n()
const router = useRouter()
const queryClient = useQueryClient()
const toast = useToast()

const formData = ref({})

const entityMutation = useMutation({
  mutationFn: (entity: any) => props.entityService?.create(entity),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: props.queryKey })
    toast.add({
      severity: 'info',
      summary: t('messages.success'),
      detail: t('messages.entity_created', { entity: props.entityLabel }),
      life: toastDisplayDuration
    })
    router.push({ name: `${props.routerPrefix}-list` })
  }
})

const submitHandler = () => {
  entityMutation.mutate(formData.value)
}
</script>

<template>
  <div v-bind="$attrs">
    <h1>{{ t('messages.new_entity', { entity: props.entityLabel }) }}</h1>
    <FormComponent :formData="formData" @submit="submitHandler" />
  </div>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
