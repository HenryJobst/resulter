<script setup lang="ts">
import { useEventStore } from '@/features/event/store/event.store'
import Button from 'primevue/button'
import SelectButton from 'primevue/selectbutton'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

import { ref } from 'vue'
import Spinner from '@/components/Spinner.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'

interface Size {
  label: string
  value: string
  class?: string
}

const store = useEventStore()
const size = ref<Size>({
  label: 'Normal',
  value: 'normal'
})

const sizeOptions = ref([
  { label: 'Small', value: 'small', class: 'sm' },
  { label: 'Normal', value: 'normal' },
  { label: 'Large', value: 'large', class: 'lg' }
])
</script>

<template>
  <h2>Wettkämpfe</h2>
  <div class="flex justify-content-between my-4">
    <router-link
      :to="{
        name: 'event-new'
      }"
    >
      <Button label="New"></Button>
    </router-link>
    <Button severity="secondary" label="Neu laden" @click="store.loadEventsAction"></Button>
  </div>

  <ErrorMessage :message="store.errorMessage"></ErrorMessage>
  <Spinner v-if="store.loadingEvents"></Spinner>

  <div v-if="store.errorMessage === null && !store.loadingEvents">
    <div class="flex justify-content-center mb-4">
      <SelectButton v-model="size" :options="sizeOptions" optionLabel="label" dataKey="label" />
    </div>
    <DataTable
      :value="store.events"
      :class="`p-datatable-${size.class}`"
      tableStyle="min-width: 50rem"
    >
      <Column field="id" header="#"></Column>
      <Column field="name" header="Name"></Column>
      <!--Column class="text-right" field="volume" header="Volume"></Column-->
      <Column class="text-right">
        <template #body="slotProps">
          <router-link :to="{ name: 'event-edit', params: { id: slotProps.data.id } }">
            <Button icon="pi pi-pencil" class="mr-2" label="Bearbeiten"></Button>
          </router-link>
          <Button
            icon="pi pi-trash"
            severity="danger"
            label="Löschen"
            @click="store.deleteEventAction(slotProps.data.id)"
          ></Button>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped></style>
