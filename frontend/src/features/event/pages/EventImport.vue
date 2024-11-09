<script setup lang="ts">
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { useToast } from 'primevue/usetoast'
import type { FileUploadUploaderEvent } from 'primevue/fileupload'
import EventImportForm from '@/features/event/widgets/EventImportForm.vue'
import { EventService, eventService } from '@/features/event/services/event.service'
import { toastDisplayDuration } from '@/utils/constants'
import type { SportEvent } from '@/features/event/model/sportEvent'

const { t } = useI18n()

const queryClient = useQueryClient()

const toast = useToast()

const router = useRouter()
async function redirectBack() {
    await router.replace({ name: 'event-list' })
}

const eventMutation = useMutation({
    mutationFn: (event: Omit<SportEvent, 'id'>) => eventService.create(event, t),
    onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['events'] })
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.event_created'),
            life: toastDisplayDuration,
        })
        redirectBack()
    },
})

function eventSubmitHandler(event: Omit<SportEvent, 'id'>) {
    eventMutation.mutate(event)
}

async function validateMimeType(f: File) {
    return f.type === 'text/xml'
}

async function uploader(event: FileUploadUploaderEvent) {
    const formData = new FormData()

    // MimeType Check on all files
    let filesToSend = 0
    const filesToHandle = Array.isArray(event.files) ? event.files : [event.files]
    for (const f of filesToHandle) {
        const valid = await validateMimeType(f)
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
                toast.add({
                    severity: 'info',
                    summary: t('messages.success'),
                    detail: t('messages.event_uploaded'),
                    life: toastDisplayDuration,
                })
            })
            .catch((error: any) => {
                console.log('Error uploading file: ', error)
            })
    }
}
</script>

<template>
    <div>
        <h2>{{ t('messages.import_event') }}</h2>

        <EventImportForm :uploader="uploader" @event-submit="eventSubmitHandler">
            <Button
                v-tooltip="t('labels.back')"
                class="pi pi-arrow-left ml-2"
                :aria-label="t('labels.back')"
                severity="secondary"
                type="reset"
                outlined
                raised
                rounded
                @click="redirectBack"
            />
        </EventImportForm>
    </div>
</template>

<style scoped></style>
