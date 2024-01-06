<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import CupForm from '@/features/cup/widgets/CupForm.vue'
import type { Cup } from '@/features/cup/model/cup'
import Button from 'primevue/button'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { CupService } from '@/features/cup/services/cup.service'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Spinner from '@/components/SpinnerComponent.vue'

const props = defineProps<{ id: string; locale?: string }>()
const authStore = useAuthStore()

const { t } = useI18n()

const cupQuery = useQuery({
  queryKey: ['cups', props.id],
  queryFn: () => CupService.getById(props.id, t)
})

const queryClient = useQueryClient()

queryClient.invalidateQueries({ queryKey: ['cups', props.id] })

const cupMutation = useMutation({
  mutationFn: (cup: Cup) => CupService.update(cup, t),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['cups'] })
  }
})

const router = useRouter()

const cupSubmitHandler = (cup: Cup) => {
  cupMutation.mutate(cup)
  router.back()
}
</script>

<template>
  <div v-bind="$attrs">
    <h2>{{ t('messages.edit_cup', { id: props.id }) }}</h2>
    <span v-if="cupQuery.status.value === 'pending'">
      {{ t('messages.loading') }}
      <Spinner />
    </span>
    <span v-else-if="cupQuery.status.value === 'error'">
      <ErrorMessage :message="t('messages.error', { message: cupQuery.error.value?.message })" />
    </span>
    <CupForm :cup="cupQuery.data.value" @cup-submit="cupSubmitHandler" v-else-if="cupQuery.data">
      <Button
        v-if="authStore.isAdmin"
        class="mt-2"
        type="submit"
        :label="t('labels.save')"
        outlined
      ></Button>
      <Button
        class="ml-2"
        severity="secondary"
        type="reset"
        :label="t('labels.back')"
        outlined
        @click="router.back()"
      ></Button>
    </CupForm>
  </div>
</template>

<style scoped></style>
