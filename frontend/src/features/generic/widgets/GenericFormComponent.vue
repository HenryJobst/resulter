<script setup>
import { defineProps, ref } from 'vue'

const props = defineProps({
  formInputs: Array,
  initialData: Object,
  entity: Object
})

const formData = ref({ ...props.initialData })

const submitHandler = () => {
  props.onSubmit(formData.value)
}
</script>

<template>
  <form @submit.prevent="submitHandler">
    <div v-for="(input, index) in formInputs" :key="index" class="form-group">
      <label :for="input.id">{{ input.label }}</label>
      <input
        v-if="input.type !== 'select'"
        :type="input.type"
        v-model="formData[input.model]"
        :id="input.id"
        class="form-control"
      />
      <select v-else v-model="formData[input.model]" :id="input.id" class="form-control">
        <option v-for="option in input.options" :value="option.value" :key="option.value">
          {{ option.text }}
        </option>
      </select>
    </div>
    <div class="mt-2">
      <slot></slot>
    </div>
  </form>
</template>
