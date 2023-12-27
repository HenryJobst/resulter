import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { EventService } from '@/features/event/services/event.service'
import { useRouter } from 'vue-router'
import type { Event } from '@/features/event/model/event'

export const useEventStore = defineStore('event', () => {
  const events = ref<Event[]>([])
  const loading = ref(false)
  const errorMessage = ref<null | string>(null)

  const router = useRouter()

  // Actions
  const loadEventsAction = function () {
    loading.value = true
    errorMessage.value = null
    // Effect
    EventService.getAll()
      .then((eventsFromService) => {
        if (eventsFromService) {
          events.value = eventsFromService
        } else {
          events.value = []
        }
      })
      .catch((error: Error) => {
        errorMessage.value = error.message
      })
      .finally(() => {
        loading.value = false
      })
  }

  const redirectToEvents = function () {
    router.replace({ name: 'event-list' }).then()
  }

  const createEventAction = function (event: Omit<Event, 'id'>) {
    loading.value = true
    errorMessage.value = null
    // Effect
    EventService.create(event)
      .then((eventFromService) => {
        events.value.push(eventFromService)
        redirectToEvents()
      })
      .catch((error: Error) => {
        errorMessage.value = error.message
      })
      .finally(() => {
        loading.value = false
      })
  }

  const updateEventAction = function (event: Event) {
    loading.value = true
    errorMessage.value = null
    // Effect
    EventService.update(event)
      .then(() => {
        loadEventsAction()
        redirectToEvents()
      })
      .catch((error: Error) => {
        errorMessage.value = error.message
      })
      .finally(() => {
        loading.value = false
      })
  }

  const deleteEventAction = (id: number | string) => {
    loading.value = true
    errorMessage.value = null
    // Effect
    EventService.deleteById(id)
      .then(() => {
        loadEventsAction()
        redirectToEvents()
      })
      .catch((error: Error) => {
        errorMessage.value = error.message
      })
      .finally(() => {
        loading.value = false
      })
  }

  // Getter / Selektoren
  const selectEvent = (id: number) => {
    return computed(() => {
      return events.value.find((event) => {
        return event.id === id
      })
    })
  }

  return {
    loadingEvents: loading,
    errorMessage,
    events,
    loadEventsAction,
    deleteEventAction,
    createEventAction,
    updateEventAction,
    selectEvent
  }
})
