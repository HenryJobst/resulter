<script setup lang="ts">
import { useEventStore } from '@/features/event/store/event.store'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Spinner from '@/components/SpinnerComponent.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const { t, locale } = useI18n() // same as `useI18n({ useScope: 'global' })`

const store = useEventStore()
const authStore = useAuthStore()

const dateOptions: Intl.DateTimeFormatOptions = {
  year: 'numeric',
  month: 'long',
  day: 'numeric'
}

const timeOptions: Intl.DateTimeFormatOptions = {
  hour: '2-digit',
  minute: '2-digit',
  hour12: false
}

const formatDateFunction = computed(() => {
  return (date: string | Date) => {
    if (!date) return ''
    if (typeof date === 'string') {
      return new Date(date).toLocaleDateString(locale.value, dateOptions)
    }
    return date.toLocaleDateString(locale.value, dateOptions)
  }
})

const formatTimeFunction = computed(() => {
  return (time: string | Date) => {
    if (!time) return ''
    if (typeof time === 'string') {
      return new Date(time).toLocaleTimeString(locale.value, timeOptions)
    }
    return time.toLocaleTimeString(locale.value, timeOptions)
  }
})

const formatDate = (date: string) => {
  return formatDateFunction.value(date)
}
const formatTime = (time: string) => {
  return formatTimeFunction.value(time)
}
</script>

<template>
  <h1>{{ t('labels.event', 2) }}</h1>
  <div class="flex justify-content-between my-4">
    <div class="flex justify-content-start">
      <router-link :to="{ name: 'event-new' }" v-if="authStore.isAuthenticated">
        <Button icon="pi pi-plus" :label="t('labels.new')" outlined></Button>
      </router-link>
      <router-link class="ml-2" :to="{ name: 'event-import' }" v-if="authStore.isAuthenticated">
        <Button icon="pi pi-upload" :label="t('labels.import')" outlined></Button>
      </router-link>
    </div>
    <Button
      icon="pi pi-refresh"
      :label="t('labels.reload')"
      outlined
      severity="secondary"
      @click="store.loadEventsAction"
    />
  </div>

  <ErrorMessage :message="store.errorMessage"></ErrorMessage>
  <Spinner v-if="store.loadingEvents"></Spinner>

  <div v-if="store.errorMessage === null && !store.loadingEvents">
    <DataTable :value="store.events" class="p-datatable-sm">
      <Column field="name" :header="t('labels.name')" />
      <Column field="startDate" :header="t('labels.date')">
        <!--suppress JSUnresolvedReference -->
        <template #body="slotProps">
          {{ formatDate(slotProps.data.startTime) }}
        </template>
      </Column>
      <Column field="startTime" :header="t('labels.time')">
        <!--suppress JSUnresolvedReference -->
        <template #body="slotProps">
          {{ formatTime(slotProps.data.startTime) }}
        </template>
      </Column>
      <Column class="text-right" field="classes" :header="t('labels.class', 2)" />
      <Column class="text-right" field="participants" :header="t('labels.participant', 2)" />
      <Column class="text-right">
        <template #body="slotProps">
          <router-link :to="{ name: 'event-edit', params: { id: slotProps.data.id } }">
            <Button
              icon="pi pi-pencil"
              class="mr-2"
              :label="t('labels.edit')"
              outlined
              v-if="authStore.isAdmin"
            ></Button>
          </router-link>
          <Button
            icon="pi pi-trash"
            severity="danger"
            outlined
            :label="t('labels.delete')"
            @click="store.deleteEventAction(slotProps.data.id)"
            v-if="authStore.isAdmin"
          ></Button>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped></style>
