<script setup lang="ts">
import type { Media } from '@/features/media/model/media'
import type { FileUploadUploaderEvent } from 'primevue/fileupload'
import { MediaService, mediaService } from '@/features/media/services/media.service'
import { acceptedFileTypes } from '@/features/media/util/file_types'
import MediaImportForm from '@/features/media/widgets/MediaImportForm.vue'
import { toastDisplayDuration } from '@/utils/constants'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import Button from 'primevue/button'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()

const queryClient = useQueryClient()

const toast = useToast()

const router = useRouter()
async function redirectBack() {
    await router.replace({ name: 'media-list' })
}

const mediaMutation = useMutation({
    mutationFn: (media: Omit<Media, 'id'>) => mediaService.create(media, t),
    onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['media'] })
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.media_created'),
            life: toastDisplayDuration,
        })
        redirectBack()
    },
})

function mediaSubmitHandler(media: Omit<Media, 'id'>) {
    mediaMutation.mutate(media)
}

async function validateMimeType(f: File) {
    return acceptedFileTypes.includes(f.type)
}

async function uploader(media: FileUploadUploaderEvent) {
    const formData = new FormData()

    // MimeType Check on all files
    let filesToSend = 0
    const filesToHandle = Array.isArray(media.files) ? media.files : [media.files]
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
        await MediaService.upload(formData, t)
            .then((data) => {
                console.log('File uploaded', data)
                toast.add({
                    severity: 'info',
                    summary: t('messages.success'),
                    detail: t('messages.media_uploaded'),
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
        <h2>{{ t('messages.import_media') }}</h2>

        <MediaImportForm :uploader="uploader" @media-submit="mediaSubmitHandler">
            <Button
                class="ml-2"
                severity="secondary"
                type="reset"
                :label="t('labels.back')"
                outlined
                raised
                rounded
                @click="redirectBack"
            />
        </MediaImportForm>
    </div>
</template>

<style scoped></style>
