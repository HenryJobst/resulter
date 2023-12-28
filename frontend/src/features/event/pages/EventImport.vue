<script setup lang="ts">
import Button from 'primevue/button'
import { useEventStore } from '@/features/event/store/event.store'
import Spinner from '@/components/SpinnerComponent.vue'
import type { Event } from '@/features/event/model/event'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import EventImportForm from '@/features/event/widgets/EventImportForm.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const store = useEventStore()
const authStore = useAuthStore()

const eventSubmitHandler = (event: Omit<Event, 'id'>) => {
  console.log(event)
  //store.createEventAction(event)
}

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const router = useRouter()
const redirectBack = async () => {
  await router.replace({ name: 'event-list' })
}
</script>

<template>
  <div>
    <h2>{{ t('messages.import_event') }}</h2>

    <Spinner v-if="store.loadingEvents"></Spinner>

    <EventImportForm v-if="!store.loadingEvents" @event-submit="eventSubmitHandler">
      <!--Button type="submit" :label="t('labels.create')" outlined></Button-->
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
