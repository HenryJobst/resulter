<script setup lang="ts">
import InputText from 'primevue/inputtext'
import type { Event } from '@/features/event/model/event'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import Calendar from 'primevue/calendar'
import MultiSelect from 'primevue/multiselect'
import { OrganisationService } from '@/features/organisation/services/organisation.service'
import { useQuery } from '@tanstack/vue-query'

const { t } = useI18n()

const defaultDate = new Date()
defaultDate.setHours(11)

const formData = ref<Event | Omit<Event, 'id'>>({
  name: '',
  startTime: new Date(),
  classes: 0,
  participants: 0,
  organisations: []
})

const dateTime = ref(new Date(formData.value.startTime))

const props = defineProps<{ event?: Event }>()

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
    formData.value.startTime = dateTime.value.toISOString()
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
    formData.value.startTime = dateTime.value.toISOString()
  }
})

onMounted(() => {
  if (props.event) {
    formData.value = { ...props.event }
    dateTime.value = new Date(props.event.startTime)
  }
})

const organisationQuery = useQuery({
  queryKey: ['organisations'],
  queryFn: () => OrganisationService.getAll()
})

const emit = defineEmits(['eventSubmit'])

const formSubmitHandler = () => {
  // console.log(formData.value)
  emit('eventSubmit', formData.value)
}
</script>

<template>
  <form @submit.prevent="formSubmitHandler">
    <div class="flex flex-col">
      <div class="flex flex-row">
        <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
        <div class="col">
          <InputText v-model="formData.name" type="text" id="name"></InputText>
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
        <label for="organisations" class="col-fixed w-32">{{ t('labels.organisation') }}</label>
        <div class="col">
          <span v-if="organisationQuery.status.value === 'pending'">{{
            t('messages.loading')
          }}</span>
          <span v-else-if="organisationQuery.status.value === 'error'">
            {{ t('messages.error', { message: organisationQuery.error.toLocaleString() }) }}
          </span>

          <div v-else-if="organisationQuery.data" class="card">
            <MultiSelect
              id="organisations"
              v-model="formData.organisations"
              :options="organisationQuery.data.value"
              filter
              optionLabel="name"
              option-value="id"
              :placeholder="t('messages.select')"
              class="w-full md:w-20rem"
            />
          </div>
        </div>
      </div>
    </div>
    <div class="mt-2">
      <slot></slot>
    </div>
  </form>
</template>

<style scoped></style>
