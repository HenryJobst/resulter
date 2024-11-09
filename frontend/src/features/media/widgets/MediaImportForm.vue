<script setup lang="ts">
import { getCurrentInstance, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import FileUpload, { type FileUploadUploaderEvent } from 'primevue/fileupload'
import ProgressBar from 'primevue/progressbar'
import Badge from 'primevue/badge'
import Button from 'primevue/button'
import type { Upload } from '@/features/media/model/upload'
import type { Media } from '@/features/media/model/media'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

import { toastDisplayDuration } from '@/utils/constants'
import { acceptedFileTypes } from '@/features/media/util/file_types'
import { fileSizeTypes } from '@/features/media/util/file_size_types'

const props = defineProps<{ media?: Media, uploader: any }>()
const emit = defineEmits(['mediaSubmit'])
const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const authStore = useAuthStore()
const url: string = `${import.meta.env.VITE_API_ENDPOINT}/media/upload`

const internalInstance = getCurrentInstance()

const formData = ref<Upload>({
    fileName: '',
})

onMounted(() => {
    if (props.media)
        formData.value = { ...props.media }
})

function formSubmitHandler() {
    emit('mediaSubmit', formData.value)
}

const toast = useToast()

const totalSize = ref<number>(0)
const totalSizePercent = ref<number>(0)
const files_to_upload = ref<File[]>([])

function onRemoveTemplatingFile(
    file: File,
    removeFileCallback: (index: number) => void,
    index: number,
) {
    removeFileCallback(index)
    totalSize.value -= Number.parseInt(formatSize(file.size))
    totalSizePercent.value = totalSize.value / 10
}

function onClearTemplatingUpload() {
    totalSize.value = 0
    totalSizePercent.value = 0
}

function onSelectedFiles(media: { files: File[] }) {
    files_to_upload.value = media.files
    files_to_upload.value.forEach((file) => {
        totalSize.value += Number.parseInt(formatSize(file.size))
    })
}

function uploadMedia(callback: () => void) {
    totalSizePercent.value = totalSize.value / 10
    callback()
}

function onTemplatedUpload(media: FileUploadUploaderEvent) {
    console.debug(media)
    toast.add({
        severity: 'info',
        summary: t('messages.success'),
        detail: t('messages.media_uploaded'),
        life: toastDisplayDuration,
    })
}

function formatSize(bytes: number): string {
    const k = 1024
    const dm = 3
    let fileSizesTypes = fileSizeTypes(t)
    if (internalInstance && internalInstance.appContext.config.globalProperties.$primevue) {
        const primevueConfig = internalInstance.appContext.config.globalProperties.$primevue
        const fileSizeTypes = primevueConfig.config.locale?.fileSizeTypes
        if (fileSizeTypes)
            fileSizesTypes = fileSizeTypes
    }

    if (bytes === 0)
        return `0 ${fileSizesTypes[0]}`

    const i = Math.floor(Math.log(bytes) / Math.log(k))
    const formattedSize = Number.parseFloat((bytes / k ** i).toFixed(dm))

    return `${formattedSize} ${fileSizesTypes[i]}`
}
</script>

<template>
    <form v-if="authStore.isAdmin" @submit.prevent="formSubmitHandler">
        <div class="flex flex-col">
            <div class="card">
                <FileUpload
                    name="file"
                    :url="url"
                    :multiple="false"
                    :accept="acceptedFileTypes.join(',')"
                    custom-upload
                    @upload="onTemplatedUpload"
                    @select="onSelectedFiles"
                    @uploader="props.uploader"
                    @clear="onClearTemplatingUpload"
                >
                    <template #header="{ chooseCallback, uploadCallback, clearCallback, files }">
                        <div
                            class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2"
                        >
                            <div class="flex gap-2">
                                <Button
                                    v-tooltip="t('labels.choose')"
                                    icon="pi pi-images"
                                    :aria-label="t('labels.choose')"
                                    outlined
                                    raised
                                    rounded
                                    @click="chooseCallback()"
                                />
                                <Button
                                    v-if="files.length > 0 && authStore.isAuthenticated"
                                    v-tooltip="t('labels.import')"
                                    icon="pi pi-upload"
                                    :aria-label="t('labels.import')"
                                    outlined
                                    raised
                                    rounded
                                    severity="success"
                                    :disabled="!files || files.length === 0"
                                    @click="uploadMedia(uploadCallback)"
                                />
                                <Button
                                    v-if="files.length > 0"
                                    v-tooltip="t('labels.clear')"
                                    :aria-label="t('labels.clear')"
                                    icon="pi pi-times"
                                    outlined
                                    raised
                                    rounded
                                    severity="danger"
                                    :disabled="!files || files.length === 0"
                                    @click="clearCallback()"
                                />
                            </div>
                            <ProgressBar
                                :value="totalSizePercent"
                                :show-value="false"
                                class="md:w-20rem h-1rem w-full md:ml-auto"
                                :class="[{ 'exceeded-progress-bar': totalSizePercent > 100 }]"
                            >
                                <span class="white-space-nowrap">{{ totalSize }}B / 1Mb</span>
                            </ProgressBar>
                        </div>
                    </template>
                    <template
                        #content="{
                            files,
                            uploadedFiles,
                            removeUploadedFileCallback,
                            removeFileCallback,
                        }"
                    >
                        <div v-if="files.length > 0">
                            <h5>{{ t('messages.pending') }}</h5>
                            <div class="flex flex-wrap p-0 sm:p-5 gap-5">
                                <div
                                    v-for="(file, index) of files"
                                    :key="file.name + file.type + file.size"
                                    class="card m-0 px-6 flex flex-column border-1 surface-border align-items-center gap-3"
                                >
                                    <span class="font-semibold">{{ file.name }}</span>
                                    <div>{{ formatSize(file.size) }}</div>
                                    <Badge :value="t('messages.pending')" severity="warning" />
                                    <Button
                                        v-tooltip="t('labels.clear')"
                                        class="mb-2"
                                        :aria-label="t('labels.clear')"
                                        icon="pi pi-times"
                                        outlined
                                        raised
                                        rounded
                                        severity="danger"
                                        @click="
                                            onRemoveTemplatingFile(file, removeFileCallback, index)
                                        "
                                    />
                                </div>
                            </div>
                        </div>

                        <div v-if="uploadedFiles.length > 0">
                            <h5>{{ t('messages.completed') }}</h5>
                            <div class="flex flex-wrap p-0 sm:p-5 gap-5">
                                <div
                                    v-for="(file, index) of uploadedFiles"
                                    :key="file.name + file.type + file.size"
                                    class="card m-0 px-6 flex flex-column border-1 surface-border align-items-center gap-3"
                                >
                                    <span class="font-semibold">{{ file.name }}</span>
                                    <div>{{ formatSize(file.size) }}</div>
                                    <Badge
                                        :value="t('messages.completed')"
                                        class="mt-3"
                                        severity="success"
                                    />
                                    <Button
                                        v-tooltip="t('labels.clear')"
                                        class="mb-2"
                                        :aria-label="t('labels.clear')"
                                        icon="pi pi-times"
                                        outlined
                                        raised
                                        rounded
                                        severity="danger"
                                        @click="removeUploadedFileCallback(index)"
                                    />
                                </div>
                            </div>
                        </div>
                    </template>
                    <template #empty>
                        <div class="flex align-items-center justify-content-center flex-column">
                            <i
                                v-tooltip="t('labels.upload')"
                                :aria-label="t('labels.upload')"
                                class="pi pi-cloud-upload border-2 border-circle p-5 text-5xl text-400 border-400 rounded"
                            />
                            <p class="mt-4 mb-0">
                                {{ t('messages.drag_drop') }}
                            </p>
                        </div>
                    </template>
                </FileUpload>
            </div>
        </div>
        <div class="mt-2">
            <slot />
        </div>
    </form>
</template>

<style scoped></style>
