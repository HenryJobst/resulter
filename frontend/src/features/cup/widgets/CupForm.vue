<script setup lang="ts">
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import type { Cup } from '@/features/cup/model/cup'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import MultiSelect from 'primevue/multiselect'
import { useQuery } from '@tanstack/vue-query'
import { eventService } from '@/features/event/services/event.service'
import { CupService } from '@/features/cup/services/cup.service'

const { t } = useI18n()

const formData = ref<Cup | Omit<Cup, 'id'>>({
  name: '',
  type: { id: 'ADD' },
  eventIds: []
})

const props = defineProps<{ cup?: Cup }>()

onMounted(() => {
  if (props.cup) {
    formData.value = { ...props.cup }
  }
})

const eventQuery = useQuery({
  queryKey: ['events'],
  queryFn: () => eventService.getAll(t)
})

const cupTypesQuery = useQuery({
  queryKey: ['cup_types'],
  queryFn: () => CupService.getCupTypes(t)
})

const emit = defineEmits(['cupSubmit'])

const formSubmitHandler = () => {
  //console.log(formData.value)
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
          <span v-if="cupTypesQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
          <span v-else-if="cupTypesQuery.status.value === 'error'">
            {{ t('messages.error', { message: cupTypesQuery.error.toLocaleString() }) }}
          </span>
          <Dropdown
            v-else-if="cupTypesQuery.data"
            id="type"
            v-model="formData.type"
            :options="cupTypesQuery.data.value"
            optionLabel="id"
            data-key="id"
            :placeholder="t('messages.select')"
            class="w-full md:w-14rem"
          />
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
              v-model="formData.eventIds"
              :options="eventQuery.data.value"
              data-key="id"
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
    <div class="mt-2">
      <slot></slot>
    </div>
  </form>
</template>

<style scoped></style>
