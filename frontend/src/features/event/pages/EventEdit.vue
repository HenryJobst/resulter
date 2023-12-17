<script setup lang="ts">
import Button from 'primevue/button'
import EventForm from '@/features/event/widgets/EventForm.vue'
import { useEventStore } from '@/features/event/store/event.store'
import Spinner from '@/components/SpinnerComponent.vue'
import type { Event } from '@/features/event/model/event'

const props = defineProps<{ id: string }>()
const store = useEventStore()
const event = store.selectEvent(+props.id)

const eventSubmitHandler = (event: Event) => {
  store.updateEventAction(event)
}
</script>

<template>
  <h2>Wettkampf mit ID {{ props.id }} bearbeiten</h2>

  <Spinner v-if="store.loadingEvents"></Spinner>

  <EventForm v-if="!store.loadingEvents" :event="event" @event-submit="eventSubmitHandler">
    <Button type="submit" label="Speichern"></Button>
  </EventForm>
</template>

<style scoped></style>
