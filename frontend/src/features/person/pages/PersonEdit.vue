<script setup lang="ts">
import type { Event } from '@/features/event/model/event'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const props = defineProps<{ id: string; locale?: string }>()
const authStore = useAuthStore()

const eventSubmitHandler = (event: Event) => {
  //  store.updateEventAction(event)
}

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const router = useRouter()
const redirectBack = async () => {
  await router.replace({ name: 'event-list' })
}
</script>

<template>
  <div v-bind="$attrs">
    <h2>{{ t('messages.edit_event', { id: props.id }) }}</h2>

    <!--Spinner v-if="store.loadingEvents"></Spinner-->

    <!--EventForm v-if="!store.loadingEvents" :event="event" @event-submit="eventSubmitHandler">
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
    </EventForm-->
  </div>
</template>

<style scoped></style>
