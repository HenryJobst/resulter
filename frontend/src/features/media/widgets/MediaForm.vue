<script setup lang="ts">
import InputText from 'primevue/inputtext'
import type { Media } from '@/features/media/model/media'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { MediaService } from '@/features/media/services/media.service'

const { t } = useI18n()

const props = defineProps<{
  media?: Media
  entityService: MediaService
  queryKey: string[]
}>()

const emit = defineEmits(['update:modelValue'])

const media = computed({
  get: () => props.media,
  set: (value) => emit('update:modelValue', value)
})
</script>

<template>
  <div v-if="media" class="flex flex-col">
    <div class="flex flex-row">
      <label for="fileName" class="col-fixed w-32">{{ t('labels.fileName') }}</label>
      <div class="col">
        <InputText v-model="media.fileName" type="text" id="fileName" readonly="true"></InputText>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="contentType" class="col-fixed w-32">{{ t('labels.contentType') }}</label>
      <div class="col">
        <InputText
          v-model="media.contentType"
          type="text"
          id="contentType"
          readonly="true"
        ></InputText>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="fileSize" class="col-fixed w-32">{{ t('labels.fileSize') }}</label>
      <div class="col">
        <InputNumber v-model="media.fileSize" id="fileSize" readonly></InputNumber>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="description" class="col-fixed w-32">{{ t('labels.description') }}</label>
      <div class="col">
        <InputText v-model="media.description" type="text" id="description"></InputText>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
