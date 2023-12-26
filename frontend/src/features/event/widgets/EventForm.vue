<script setup lang="ts">
import InputText from 'primevue/inputtext'
import type { Event } from '@/features/event/model/event'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import Calendar from 'primevue/calendar'

const { t, locale } = useI18n() // same as `useI18n({ useScope: 'global' })`

const defaultDate = new Date()
defaultDate.setHours(11)

const formData = ref<Event | Omit<Event, 'id'>>({
  name: '',
  startTime: new Date().toLocaleDateString(locale.value),
  classes: 0,
  participants: 0
})

const props = defineProps<{ event?: Event }>()

onMounted(() => {
  if (props.event !== void 0) {
    formData.value = {
      ...props.event
    }
    //console.log(formData)
  }
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
          <Calendar
            v-model="formData.startTime"
            id="startDate"
            date-format="dd.mm.yy"
            show-icon
          ></Calendar>
        </div>
      </div>
      <div class="flex flex-row">
        <label for="startTime" class="col-fixed w-32">{{ t('labels.time') }}</label>
        <div>
          <Calendar
            v-model="formData.startTime"
            id="startTime"
            showIcon
            iconDisplay="input"
            timeOnly
          >
            <template #inputicon="{ clickCallback }">
              <i class="pi pi-clock" @click="clickCallback" />
            </template>
          </Calendar>
        </div>
      </div>
    </div>
    <!--div class="field grid">
      <label for="volume" class="col-fixed" style="width: 100px">Volume</label>
      <div class="col">
        <InputText v-model.number="formData.volume" type="number" id="volume"></InputText>
      </div>
    </div-->
    <slot></slot>
  </form>
</template>

<style scoped></style>
