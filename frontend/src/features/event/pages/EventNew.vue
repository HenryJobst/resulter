<script setup lang="ts">
import EventForm from '@/features/event/widgets/EventForm.vue'
import Button from 'primevue/button'
import { useEventStore } from '@/features/event/store/event.store'
import Spinner from '@/components/SpinnerComponent.vue'
import type { Event } from '@/features/event/model/event'

const store = useEventStore()

const eventSubmitHandler = (event: Omit<Event, 'id'>) => {
  console.log(event)
  store.createEventAction(event)
}
</script>

<template>
  <div>
    <h2>Neuer Wettkampf</h2>

    <Spinner v-if="store.loadingEvents"></Spinner>

    <EventForm v-if="!store.loadingEvents" @event-submit="eventSubmitHandler">
      <Button type="submit" label="Erstellen"></Button>
    </EventForm>
  </div>
</template>

<style scoped></style>