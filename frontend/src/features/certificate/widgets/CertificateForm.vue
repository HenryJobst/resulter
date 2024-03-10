<script setup lang="ts">
import InputText from 'primevue/inputtext'
import type { Certificate } from '@/features/certificate/model/certificate'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Textarea from 'primevue/textarea'

import { CertificateService } from '@/features/certificate/services/certificate.service'
import { useQuery } from '@tanstack/vue-query'
import { eventService } from '@/features/event/services/event.service'
import Dropdown from 'primevue/dropdown'
import type { ListboxChangeEvent } from 'primevue/listbox'
import type { EventKey } from '@/features/event/model/event_key'
import { mediaService } from '@/features/media/services/media.service'
import type { MediaKey } from '@/features/media/model/media_key'
import type { SportEvent } from '@/features/event/model/sportEvent'

const { t } = useI18n()

const props = defineProps<{
  certificate?: Certificate
  entityService: CertificateService
  queryKey: string[]
}>()

const emit = defineEmits(['update:modelValue'])

const certificate = computed({
  get: () => props.certificate,
  set: (value) => emit('update:modelValue', value)
})

const l_certificate = ref<number | null>(
  certificate.value && certificate.value.event ? certificate.value.event.id : null
)

const eventQuery = useQuery({
  queryKey: ['events'],
  queryFn: () => eventService.getAll(t)
})

const mediaQuery = useQuery({
  queryKey: ['media'],
  queryFn: () => mediaService.getAll(t)
})

const l_media = ref<number | null>(
  certificate.value && certificate.value.blankCertificate
    ? certificate.value.blankCertificate.id
    : null
)

const getEventKeyFromId = (id: number | null): EventKey | null => {
  if (!eventQuery.data.value || !eventQuery.data.value.content) {
    return null
  }

  const event: SportEvent | undefined = eventQuery.data.value?.content.find(
    (event) => event.id === id
  )
  if (event !== undefined) {
    return {
      id: event.id,
      name: event.name
    }
  }
  return null
}

const handleEventSelectionChange = (ev: ListboxChangeEvent) => {
  if (ev.value && certificate.value && eventQuery.data.value && eventQuery.data.value.content) {
    certificate.value.event = getEventKeyFromId(ev.value.id)!
  }
}

const getMediaKeyFromId = (id: number | null): MediaKey | null => {
  if (!mediaQuery.data.value || !mediaQuery.data.value.content) {
    return null
  }

  const media = mediaQuery.data.value?.content.find((media) => media.id === id)
  if (media !== undefined) {
    return {
      id: media.id,
      fileName: media.fileName,
      thumbnailContent: media.thumbnailContent
    }
  }
  return null
}

const handleMediaSelectionChange = (ev: ListboxChangeEvent) => {
  if (ev.value && certificate.value && mediaQuery.data.value && mediaQuery.data.value.content) {
    certificate.value.blankCertificate = getMediaKeyFromId(ev.value.id)!
  }
}
</script>

<template>
  <div v-if="certificate" class="flex flex-col">
    <div class="flex flex-row">
      <label for="event" class="col-fixed w-32">{{ t('labels.event') }}</label>
      <div class="col" v-if="certificate.event">
        <InputText v-model="certificate.event.name" type="text" id="event" readonly="true" />
      </div>
      <div class="col" v-else>
        <span v-if="eventQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
        <span v-else-if="eventQuery.status.value === 'error'">
          {{ t('messages.error', { message: eventQuery.error.toLocaleString() }) }}
        </span>
        <Dropdown
          v-else-if="eventQuery.data.value"
          id="event"
          v-model="l_certificate"
          :options="eventQuery.data.value.content"
          optionLabel="name"
          data-key="id"
          :placeholder="t('messages.select')"
          class="w-full md:w-14rem"
          filter
          @change="handleEventSelectionChange"
        />
      </div>
    </div>
    <div class="flex flex-row">
      <label for="media" class="col-fixed w-32">{{ t('labels.background') }}</label>
      <div class="col">
        <span v-if="mediaQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
        <span v-else-if="mediaQuery.status.value === 'error'">
          {{ t('messages.error', { message: mediaQuery.error.toLocaleString() }) }}
        </span>
        <Dropdown
          v-else-if="mediaQuery.data.value"
          id="media"
          v-model="l_media"
          :options="mediaQuery.data.value.content"
          data-key="id"
          :placeholder="t('messages.select')"
          class="w-full md:w-14rem"
          filter
          @change="handleMediaSelectionChange"
        >
          <template v-slot:content="value">
            <div class="flex flex-row">
              <img
                :src="'data:image/jpeg;base64,' + value.thumbnailContent"
                :alt="t('labels.preview')"
                style="width: 100px"
              />
              <!--div class="ml-2">{{ value.fileName }}</div-->
            </div>
          </template>
        </Dropdown>
      </div>
    </div>
    <div class="flex flex-row">
      <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
      <div class="col">
        <InputText v-model="certificate.name" type="text" id="name" />
      </div>
    </div>
    <div class="flex flex-row">
      <label for="layoutDescription" class="col-fixed w-32">{{
        t('labels.layout_description')
      }}</label>
      <div class="col">
        <Textarea
          v-model="certificate.layoutDescription"
          id="layoutDescription"
          autoResize
          rows="20"
          cols="80"
        ></Textarea>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
