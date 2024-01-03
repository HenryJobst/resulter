<script setup lang="ts">
import InputText from 'primevue/inputtext'
import type { Cup } from '@/features/cup/model/cup'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import MultiSelect from 'primevue/multiselect'
import { useQuery } from '@tanstack/vue-query'
import { EventService } from '@/features/event/services/event.service'

const { t } = useI18n()

const formData = ref<Cup | Omit<Cup, 'id'>>({
  name: '',
  type: '',
  events: []
})

const props = defineProps<{ cup?: Cup }>()

onMounted(() => {
  if (props.cup) {
    formData.value = { ...props.cup }
  }
})

const eventQuery = useQuery({
  queryKey: ['events'],
  queryFn: () => EventService.getAll()
})

const emit = defineEmits(['cupSubmit'])

const formSubmitHandler = () => {
  // console.log(formData.value)
  emit('cupSubmit', formData.value)
}
</script>

<template>
  <form @submit.prevent="formSubmitHandler">
    <div class="flex flex-col">
      <div class="flex flex-row">
        <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
        <div class="col">
          <InputText v-model="formData.name" type="text" id="name" class="w-96"></InputText>
        </div>
      </div>
      <div class="flex flex-row">
        <label for="type" class="col-fixed w-32">{{ t('labels.type') }}</label>
        <div class="col">
          <InputText v-model="formData.type" type="text" id="type"></InputText>
        </div>
      </div>
      <div class="flex flex-row">
        <label for="cups" class="col-fixed w-32">{{ t('labels.event') }}</label>
        <div class="col">
          <span v-if="eventQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
          <span v-else-if="eventQuery.status.value === 'error'">
            {{ t('messages.error', { message: eventQuery.error.toLocaleString() }) }}
          </span>

          <div v-else-if="eventQuery.data" class="card">
            <MultiSelect
              id="events"
              v-model="formData.events"
              :options="eventQuery.data.value"
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
