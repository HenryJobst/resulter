<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { computed, ref } from 'vue'
import { GenericService } from '@/features/generic/services/GenericService'
import EventForm from '@/features/event/widgets/EventForm.vue'
import GenericNew from '@/features/generic/pages/GenericNew.vue'
import type { Event } from '@/features/event/model/event'

const { t } = useI18n()
const authStore = useAuthStore()
const eventService = new GenericService<Event>('/event')
const queryKey: string[] = ['events']
const entityLabel: string = 'event'
const newLabel = computed(() => t('messages.new_entity', { entity: t('labels.event') }))

const formData = ref<Event | Omit<Event, 'id'>>({
  name: '',
  startTime: new Date(),
  state: { id: 'Planned' },
  organisations: []
})
</script>

<template>
  <GenericNew
    :entity="formData"
    :entity-service="eventService"
    :query-key="queryKey"
    :entity-label="entityLabel"
    :new-label="newLabel"
    :router-prefix="'event'"
    :changeable="authStore.isAdmin"
  >
    <template v-slot:default="{ formData }">
      <EventForm
        :event="formData"
        :entity-service="eventService"
        :query-key="queryKey"
        :v-model="formData"
      />
    </template>
  </GenericNew>
</template>

<style scoped></style>
