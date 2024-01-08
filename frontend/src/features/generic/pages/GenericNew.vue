<script setup lang="ts">
import { computed, defineProps, type PropType } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { toastDisplayDuration } from '@/utils/constants'
import { useRouter } from 'vue-router'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Spinner from '@/components/SpinnerComponent.vue'
import Button from 'primevue/button'

const props = defineProps({
  entity: Object,
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

const formData = props.entity

const entityLabel = computed(() => (props.entityLabel ? t('labels.' + props.entityLabel) : ''))
const entityMutation = useMutation({
  mutationFn: (entity: any) => props.entityService?.create(entity, t),
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

const submitHandler = () => {
  if (formData) {
    entityMutation.mutate(formData.value)
  }
}
const navigateToList = () => {
  router.replace({ name: `${props.routerPrefix}-list` })
}
</script>

<template>
  <div v-bind="$attrs" v-if="changeable">
    <h1>{{ props.newLabel }}</h1>
    <div v-if="entityMutation.status.value === 'pending'">
      {{ t('messages.loading') }}
      <Spinner />
    </div>
    <div v-else-if="entityMutation.status.value === 'error'">
      <ErrorMessage :message="t('messages.error', { message: entityMutation.error.value })" />
    </div>
    <form @submit.prevent="submitHandler">
      <slot :formData="formData"></slot>
      <div class="mt-2">
        <Button v-if="changeable" class="mt-2" type="submit" :label="t('labels.save')" outlined />
        <Button
          v-if="changeable"
          class="ml-2"
          severity="secondary"
          type="reset"
          :label="t('labels.back')"
          outlined
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
