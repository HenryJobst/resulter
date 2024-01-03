<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import CupForm from '@/features/cup/widgets/CupForm.vue'
import type { Cup } from '@/features/cup/model/cup'
import Button from 'primevue/button'
import { useQuery } from '@tanstack/vue-query'
import { CupService } from '@/features/cup/services/cup.service'
import { computed } from 'vue'

const props = defineProps<{ id: string; locale?: string }>()
const authStore = useAuthStore()

const cupQuery = useQuery({
  queryKey: ['cups'],
  queryFn: () => CupService.getAll(t)
})

const cup = computed(() => {
  return cupQuery?.data?.value?.find((cup) => {
    return cup.id === +props.id
  })
})

const eventSubmitHandler = (cup: Cup) => {
  CupService.update(cup, t).then((updatedCup) => {
    if (updatedCup) {
      cup = updatedCup
    }
  })
}

const { t } = useI18n()

const router = useRouter()
const redirectBack = async () => {
  await router.replace({ name: 'cup-list' })
}
</script>

<template>
  <div v-bind="$attrs">
    <h2>{{ t('messages.edit_cup', { id: props.id }) }}</h2>

    <CupForm :cup="cup" @event-submit="eventSubmitHandler">
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
        @click="redirectBack"
      ></Button>
    </CupForm>
  </div>
</template>

<style scoped></style>
