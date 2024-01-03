<script setup lang="ts">
import InputText from 'primevue/inputtext'
import type { Organisation } from '@/features/organisation/model/organisation'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import MultiSelect from 'primevue/multiselect'
import { OrganisationService } from '@/features/organisation/services/organisation.service'
import { useQuery } from '@tanstack/vue-query'

const { t } = useI18n()

const defaultDate = new Date()
defaultDate.setHours(11)

const formData = ref<Organisation | Omit<Organisation, 'id'>>({
  name: '',
  shortName: '',
  type: '',
  organisations: []
})

const props = defineProps<{ organisation?: Organisation }>()

onMounted(() => {
  if (props.organisation) {
    formData.value = { ...props.organisation }
  }
})

const organisationQuery = useQuery({
  queryKey: ['organisations'],
  queryFn: () => OrganisationService.getAll(t)
})

const emit = defineEmits(['organisationSubmit'])

const formSubmitHandler = () => {
  // console.log(formData.value)
  emit('organisationSubmit', formData.value)
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
