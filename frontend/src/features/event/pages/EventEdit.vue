<script setup lang="ts">
import Button from 'primevue/button'
import EventForm from '@/features/event/widgets/EventForm.vue'
import { useEventStore } from '@/features/event/store/event.store'
import Spinner from '@/components/SpinnerComponent.vue'
import type { Event } from '@/features/event/model/event'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ id: string }>()
const store = useEventStore()
const event = store.selectEvent(+props.id)

const eventSubmitHandler = (event: Event) => {
  store.updateEventAction(event)
}

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`
</script>

<template>
  <h2>{{ t('messages.edit_event', { id: props.id }) }}</h2>

  <Spinner v-if="store.loadingEvents"></Spinner>

  <EventForm v-if="!store.loadingEvents" :event="event" @event-submit="eventSubmitHandler">
    <Button class="mt-2" type="submit" :label="t('labels.save')"></Button>
  </EventForm>
</template>

<style scoped></style>