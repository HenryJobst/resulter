<script setup lang="ts">
import { useEventStore } from '@/features/event/store/event.store'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Spinner from '@/components/SpinnerComponent.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const store = useEventStore()
</script>

<template>
  <h1>{{ t('labels.event') }}</h1>
  <div class="flex justify-content-between my-4">
    <router-link
      :to="{
        name: 'event-new'
      }"
    >
      <Button :label="t('labels.new')"></Button>
    </router-link>
    <Button
      severity="secondary"
      :label="t('labels.reload')"
      @click="store.loadEventsAction"
    ></Button>
  </div>

  <ErrorMessage :message="store.errorMessage"></ErrorMessage>
  <Spinner v-if="store.loadingEvents"></Spinner>

  <div v-if="store.errorMessage === null && !store.loadingEvents">
    <DataTable :value="store.events" class="p-datatable-sm" tableStyle="min-width: 50rem">
      <Column field="id" :header="t('labels.no')" />
      <Column field="name" :header="t('labels.name')" />
      <Column field="startTime" :header="t('labels.date')" />
      <Column class="text-right" field="classes" :header="t('labels.class', 2)" />
      <Column class="text-right" field="participants" :header="t('labels.participant', 2)" />
      <Column class="text-right">
        <template #body="slotProps">
          <router-link :to="{ name: 'event-edit', params: { id: slotProps.data.id } }">
            <Button icon="pi pi-pencil" class="mr-2" :label="t('labels.edit')"></Button>
          </router-link>
          <Button
            icon="pi pi-trash"
            severity="danger"
            :label="t('labels.delete')"
            @click="store.deleteEventAction(slotProps.data.id)"
          ></Button>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped></style>