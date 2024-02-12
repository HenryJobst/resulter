<script setup lang="ts">
import Button from 'primevue/button'
import type { SportEvent } from '@/features/event/model/sportEvent'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import EventImportForm from '@/features/event/widgets/EventImportForm.vue'
import { EventService, eventService } from '@/features/event/services/event.service'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { toastDisplayDuration } from '@/utils/constants'
import { useToast } from 'primevue/usetoast'
import type { FileUploadUploaderEvent } from 'primevue/fileupload'

const { t } = useI18n()

const queryClient = useQueryClient()

const toast = useToast()

const router = useRouter()
const redirectBack = async () => {
  await router.replace({ name: 'event-list' })
}

const eventMutation = useMutation({
  mutationFn: (event: Omit<SportEvent, 'id'>) => eventService.create(event, t),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['events'] })
    toast.add({
      severity: 'info',
      summary: t('messages.success'),
      detail: t('messages.cup_created'),
      life: toastDisplayDuration
    })
    redirectBack()
  }
})

const eventSubmitHandler = (event: Omit<SportEvent, 'id'>) => {
  eventMutation.mutate(event)
}

async function validateImageMimeType(f: File) {
  return f.type === 'text/xml'
}

const uploader = async (event: FileUploadUploaderEvent) => {
  const formData = new FormData()

  // MimeType Check on all files
  let filesToSend = 0
  const filesToHandle = Array.isArray(event.files) ? event.files : [event.files]
  for (const f of filesToHandle) {
    const valid = await validateImageMimeType(f)
    if (valid) {
      formData.append('file', f)
      filesToSend += 1
    } else {
      console.log('Invalid file type')
    }
  }

  if (filesToSend > 0) {
    await EventService.upload(formData, t)
      .then((data) => {
        console.log('File uploaded', data)
      })
      .catch((error: any) => {
        console.log('Error uploading file: ' + error)
      })
  }
}
</script>

<template>
  <div>
    <h2>{{ t('messages.import_event') }}</h2>

    <EventImportForm @event-submit="eventSubmitHandler" :uploader="uploader">
      <Button
        class="ml-2"
        severity="secondary"
        type="reset"
        :label="t('labels.back')"
        outlined
        @click="redirectBack"
      ></Button>
    </EventImportForm>
  </div>
</template>

<style scoped></style>
