<script setup lang="ts">
import InputText from 'primevue/inputtext'
import type { Event } from '@/features/event/model/event'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import Calendar from 'primevue/calendar'
import MultiSelect from 'primevue/multiselect'
import { useQuery } from '@tanstack/vue-query'
import { EventService } from '@/features/event/services/event.service'
import Dropdown from 'primevue/dropdown'
import { organisationService } from '@/features/organisation/services/organisation.service'

const { t } = useI18n()

const props = defineProps<{
  event: Event
  entityService: EventService
  queryKey: string[]
}>()

const emit = defineEmits(['update:modelValue'])

const event = computed({
  get: () => props.event,
  set: (value) => emit('update:modelValue', value)
})

const organisationQuery = useQuery({
  queryKey: ['organisations'],
  queryFn: () => organisationService.getAll(t),
  select: (data) => data ?? []
})

const eventStatusQuery = useQuery({
  queryKey: ['event_status'],
  queryFn: () => EventService.getEventStatus(t)
})

const localizedEventStatusOptions = computed(() => {
  if (eventStatusQuery.data.value) {
    return eventStatusQuery.data.value.map((option) => ({
      ...option,
      label: t(`event_state.${option.id.toLocaleUpperCase()}`)
    }))
  }
  return []
})

const dateTime = ref(new Date(event.value ? event.value.startTime : new Date()))

const datePart = computed({
  get: () =>
    new Date(dateTime.value.getFullYear(), dateTime.value.getMonth(), dateTime.value.getDate()),
  set: (newDate) => {
    dateTime.value = new Date(
      newDate.getFullYear(),
      newDate.getMonth(),
      newDate.getDate(),
      dateTime.value.getHours(),
      dateTime.value.getMinutes()
    )
    if (props.event) {
      props.event.startTime = dateTime.value.toISOString()
    }
  }
})

const timePart = computed({
  get: () => {
    return new Date(
      dateTime.value.getFullYear(),
      dateTime.value.getMonth(),
      dateTime.value.getDate(),
      dateTime.value.getHours(),
      dateTime.value.getMinutes()
    )
  },
  set: (newTime) => {
    dateTime.value = new Date(
      dateTime.value.getFullYear(),
      dateTime.value.getMonth(),
      dateTime.value.getDate(),
      newTime.getHours(),
      newTime.getMinutes()
    )
    if (event.value) {
      event.value.startTime = dateTime.value.toISOString()
    }
  }
})

onMounted(() => {
  if (props.event) {
    dateTime.value = new Date(props.event.startTime)
  }
})
</script>

<template>
  <div v-if="event" class="flex flex-col">
    <div class="flex flex-row">
      <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
      <div class="col">
        <InputText v-model="event.name" type="text" id="name"></InputText>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="startDate" class="col-fixed w-32">{{ t('labels.date') }}</label>
      <div class="col">
        <Calendar v-model="datePart" id="startDate" date-format="dd.mm.yy" show-icon></Calendar>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="startTime" class="col-fixed w-32">{{ t('labels.time') }}</label>
      <div class="col">
        <Calendar v-model="timePart" id="startTime" showIcon iconDisplay="input" timeOnly>
          <template #inputicon="{ clickCallback }">
            <i class="pi pi-clock" @click="clickCallback" />
          </template>
        </Calendar>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="type" class="col-fixed w-40">{{ t('labels.state') }}</label>
      <div class="col">
        <span v-if="eventStatusQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
        <span v-else-if="eventStatusQuery.status.value === 'error'">
          {{ t('messages.error', { message: eventStatusQuery.error.toLocaleString() }) }}
        </span>
        <Dropdown
          v-else-if="eventStatusQuery.data.value"
          id="state"
          v-model="event.state"
          :options="localizedEventStatusOptions"
          optionLabel="label"
          data-key="id"
          :placeholder="t('messages.select')"
          class="w-full md:w-14rem"
        />
      </div>
    </div>
    <div class="flex flex-row">
      <label for="organisations" class="col-fixed w-32">{{ t('labels.organisation', 2) }}</label>
      <div class="col">
        <span v-if="organisationQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
        <span v-else-if="organisationQuery.status.value === 'error'">
          {{ t('messages.error', { message: organisationQuery.error.toLocaleString() }) }}
        </span>

        <div v-else-if="organisationQuery.data" class="card">
          <MultiSelect
            id="organisations"
            v-model="event.organisations"
            :options="organisationQuery.data.value"
            filter
            optionLabel="name"
            optionValue="id"
            :placeholder="t('messages.select')"
            class="w-full md:w-20rem"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
