<script setup lang="ts">
import Button from 'primevue/button'
import EventForm from '@/features/event/widgets/EventForm.vue'
import { useEventStore } from '@/features/event/store/event.store'
import Spinner from '@/components/SpinnerComponent.vue'
import type { Event } from '@/features/event/model/event'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const props = defineProps<{ id: string; locale?: string }>()
const store = useEventStore()
const authStore = useAuthStore()
const event = store.selectEvent(+props.id)

const eventSubmitHandler = (event: Event) => {
  store.updateEventAction(event)
}

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const router = useRouter()
</script>

<template>
  <div v-bind="$attrs">
    <h2>{{ t('messages.edit_event', { id: props.id }) }}</h2>

    <Spinner v-if="store.loadingEvents"></Spinner>

    <EventForm v-if="!store.loadingEvents" :event="event" @event-submit="eventSubmitHandler">
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
    </EventForm>
  </div>
</template>

<style scoped></style>
