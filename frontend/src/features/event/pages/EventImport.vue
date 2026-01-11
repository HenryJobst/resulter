<script setup lang="ts">
import type { FileUploadUploaderEvent } from 'primevue/fileupload'
import type { SportEvent } from '@/features/event/model/sportEvent'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import Button from 'primevue/button'
import { useToast } from 'primevue/usetoast'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { EventService, eventService } from '@/features/event/services/event.service'
import EventImportForm from '@/features/event/widgets/EventImportForm.vue'
import { toastDisplayDuration } from '@/utils/constants'

const { t } = useI18n()

const queryClient = useQueryClient()

const toast = useToast()

const router = useRouter()
async function redirectBack() {
    await router.replace({ name: 'event-list' })
}

const isUploading = ref(false)
const fileUploadRef = ref()

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
    isUploading.value = true

    try {
        const formData = new FormData()

        // MimeType Check on all files
        let filesToSend = 0
        const filesToHandle = Array.isArray(event.files) ? event.files : [event.files]
        for (const f of filesToHandle) {
            const valid = await validateMimeType(f)
            if (valid) {
                formData.append('file', f)
                filesToSend += 1
            }
            else {
                console.log('Invalid file type')
            }
        }

        if (filesToSend > 0) {
            await EventService.upload(formData, t)

            // Success handling
            toast.add({
                severity: 'success',
                summary: t('messages.success'),
                detail: t('messages.event_uploaded'),
                life: toastDisplayDuration,
            })

            await queryClient.invalidateQueries({ queryKey: ['events'] })
            await redirectBack()
        }
    }
    catch (error: any) {
        // Error handling
        const errorMessage = error.response?.data?.message?.key
            ? t(error.response.data.message.key)
            : error.message || t('messages.upload_failed')

        toast.add({
            severity: 'error',
            summary: t('messages.error'),
            detail: errorMessage,
            life: toastDisplayDuration,
        })
    }
    finally {
        isUploading.value = false
        fileUploadRef.value?.clear()
    }
}
</script>

<template>
    <div>
        <h2>{{ t('messages.import_event') }}</h2>

        <EventImportForm
            ref="fileUploadRef"
            :uploader="uploader"
            :is-uploading="isUploading"
            @event-submit="eventSubmitHandler"
        >
            <Button
                v-tooltip="t('labels.back')"
                icon="pi pi-arrow-left"
                class="ml-2"
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
