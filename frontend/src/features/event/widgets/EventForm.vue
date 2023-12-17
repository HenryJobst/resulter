<script setup lang="ts">
import InputText from 'primevue/inputtext'
import type { Event } from '@/features/event/model/event'
import { onMounted, ref } from 'vue'

const formData = ref<Event | Omit<Event, 'id'>>({
  name: ''
})

const props = defineProps<{ event?: Event }>()

onMounted(() => {
  if (props.event !== void 0) {
    formData.value = {
      ...props.event
    }
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
    <div class="field grid">
      <label for="name" class="col-fixed" style="width: 100px">Name</label>
      <div class="col">
        <InputText v-model="formData.name" type="text" id="name"></InputText>
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
