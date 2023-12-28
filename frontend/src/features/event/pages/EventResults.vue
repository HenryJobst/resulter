<script setup lang="ts">
import { useEventStore } from '@/features/event/store/event.store'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { useQuery } from '@tanstack/vue-query'
import { EventService } from '@/features/event/services/event.service'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ id: string; locale?: string }>()
const store = useEventStore()
const authStore = useAuthStore()
const event = store.selectEvent(+props.id)

const { t } = useI18n()

const eventResultsQuery = useQuery({
  queryKey: ['eventResults'],
  queryFn: () => EventService.getResultsById(props.id)
})
</script>

<template>
  <h2 v-if="event">{{ event.name }}</h2>
  <span v-if="eventResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
  <span v-else-if="eventResultsQuery.status.value === 'error'">
    {{ t('messages.error', { message: eventResultsQuery.error.toLocaleString() }) }}
  </span>
  <DataTable
    v-else-if="eventResultsQuery.data"
    :value="eventResultsQuery.data.value?.classResultDtos"
    class="p-datatable-sm"
  >
    <Column field="name" />
  </DataTable>
</template>

<style scoped></style>
