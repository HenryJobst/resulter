<script setup lang="ts">
import type { Cup } from '@/features/cup/model/cup'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { CupService } from '@/features/cup/services/cup.service'
import CupForm from '@/features/cup/widgets/CupForm.vue'
import Button from 'primevue/button'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Spinner from '@/components/SpinnerComponent.vue'
import { useToast } from 'primevue/usetoast'

import { toastDisplayDuration } from '@/utils/constants'

const authStore = useAuthStore()

const { t } = useI18n()

const router = useRouter()

const navigateCupToList = () => {
  router.replace({ name: 'cup-list' })
}

const queryClient = useQueryClient()

const toast = useToast()

const cupMutation = useMutation({
  mutationFn: (cup: Omit<Cup, 'id'>) => CupService.create(cup, t),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['cups'] })
    toast.add({
      severity: 'info',
      summary: t('messages.success'),
      detail: t('messages.cup_created'),
      life: toastDisplayDuration
    })
    navigateCupToList()
  }
})

const cupSubmitHandler = (cup: Omit<Cup, 'id'>) => {
  cupMutation.mutate(cup)
}
</script>

<template>
  <div v-bind="$attrs">
    <h1>{{ t('messages.new_cup') }}</h1>

    <span v-if="cupMutation.status.value === 'pending'">
      {{ t('messages.loading') }}
      <Spinner />
    </span>
    <span v-if="cupMutation.status.value === 'error'">
      <ErrorMessage :message="t('messages.error', { message: cupMutation.error.value })" />
    </span>
    <CupForm @cup-submit="cupSubmitHandler">
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
        @click="navigateCupToList"
      ></Button>
    </CupForm>
  </div>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
