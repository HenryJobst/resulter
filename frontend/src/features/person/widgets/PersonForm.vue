<script setup lang="ts">
import InputText from 'primevue/inputtext'
import type { Person } from '@/features/person/model/person'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { PersonService } from '@/features/person/services/person.service'
import { useQuery } from '@tanstack/vue-query'

const { t } = useI18n()

const defaultDate = new Date()
defaultDate.setHours(11)

const formData = ref<Person | Omit<Person, 'id'>>({
  name: ''
})

onMounted(() => {})

const personQuery = useQuery({
  queryKey: ['persons'],
  queryFn: () => PersonService.getAll()
})

const emit = defineEmits(['personSubmit'])

const formSubmitHandler = () => {
  // console.log(formData.value)
  emit('personSubmit', formData.value)
}
</script>

<template>
  <form @submit.prperson="formSubmitHandler">
    <div class="flex flex-col">
      <div class="flex flex-row">
        <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
        <div class="col">
          <InputText v-model="formData.name" type="text" id="name"></InputText>
        </div>
      </div>
      <div class="flex flex-row">
        <label for="persons" class="col-fixed w-32">{{ t('labels.person') }}</label>
        <div class="col">
          <span v-if="personQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
          <span v-else-if="personQuery.status.value === 'error'">
            {{ t('messages.error', { message: personQuery.error.toLocaleString() }) }}
          </span>
        </div>
      </div>
    </div>
    <div class="mt-2">
      <slot></slot>
    </div>
  </form>
</template>

<style scoped></style>
