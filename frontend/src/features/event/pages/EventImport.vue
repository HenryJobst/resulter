<script setup lang="ts">
import Button from 'primevue/button'
import type { Event } from '@/features/event/model/event'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import EventImportForm from '@/features/event/widgets/EventImportForm.vue'
import { EventService } from '@/features/event/services/event.service'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { toastDisplayDuration } from '@/utils/constants'
import { useToast } from 'primevue/usetoast'

const { t } = useI18n()

const queryClient = useQueryClient()

const toast = useToast()

const router = useRouter()
const redirectBack = async () => {
  await router.replace({ name: 'event-list' })
}

const eventMutation = useMutation({
  mutationFn: (event: Omit<Event, 'id'>) => EventService.create(event, t),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['events'] })
    toast.add({
      severity: 'info',
      summary: t('messages.success'),
      detail: t('messages.cup_created'),
      life: toastDisplayDuration
    })
    redirectBack()
  }
})

const eventSubmitHandler = (event: Omit<Event, 'id'>) => {
  eventMutation.mutate(event)
}
</script>

<template>
  <div>
    <h2>{{ t('messages.import_event') }}</h2>

    <EventImportForm @event-submit="eventSubmitHandler">
      <Button
        class="ml-2"
        severity="secondary"
        type="reset"
        :label="t('labels.back')"
        outlined
        @click="redirectBack"
      ></Button>
    </EventImportForm>
  </div>
</template>

<style scoped></style>
