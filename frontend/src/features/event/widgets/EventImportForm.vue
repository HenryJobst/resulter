<script setup lang="ts">
import type { Event } from '@/features/event/model/event'
import { getCurrentInstance, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Upload } from '@/features/event/model/upload'
import { useToast } from 'primevue/usetoast'
import FileUpload from 'primevue/fileupload'
import ProgressBar from 'primevue/progressbar'
import Badge from 'primevue/badge'
import Toast from 'primevue/toast'
import Button from 'primevue/button'

const { t, locale } = useI18n() // same as `useI18n({ useScope: 'global' })`

const url: string = import.meta.env.VITE_API_ENDPOINT + 'upload'

const internalInstance = getCurrentInstance()

const formData = ref<Upload>({
  name: ''
})

const props = defineProps<{ event?: Event }>()

onMounted(() => {
  if (props.event) {
    formData.value = { ...props.event }
  }
})

const emit = defineEmits(['eventSubmit'])

const formSubmitHandler = () => {
  emit('eventSubmit', formData.value)
}

const toast = useToast()

const totalSize = ref<number>(0)
const totalSizePercent = ref<number>(0)
const files = ref<File[]>([])

const onRemoveTemplatingFile = (
  file: File,
  removeFileCallback: (index: number) => void,
  index: number
) => {
  removeFileCallback(index)
  totalSize.value -= parseInt(formatSize(file.size))
  totalSizePercent.value = totalSize.value / 10
}

const onClearTemplatingUpload = (clear: () => void) => {
  clear()
  totalSize.value = 0
  totalSizePercent.value = 0
}

const onSelectedFiles = (event: { files: File[] }) => {
  files.value = event.files
  files.value.forEach((file) => {
    totalSize.value += parseInt(formatSize(file.size))
  })
}

const uploadEvent = (callback: () => void) => {
  totalSizePercent.value = totalSize.value / 10
  callback()
}

const onTemplatedUpload = () => {
  toast.add({ severity: 'info', summary: 'Success', detail: 'File Uploaded', life: 3000 })
}

const formatSize = (bytes: number): string => {
  const k = 1024
  const dm = 3

  let sizes: string[] | undefined = [t('labels.megabytes')]
  if (internalInstance && internalInstance.appContext.config.globalProperties.$primevue) {
    const primevueConfig = internalInstance.appContext.config.globalProperties.$primevue
    const fileSizeTypes = primevueConfig.config.locale?.fileSizeTypes
    if (fileSizeTypes) {
      sizes = fileSizeTypes
    }
  }

  if (bytes === 0) {
    return `0 ${sizes[0]}`
  }

  const i = Math.floor(Math.log(bytes) / Math.log(k))
  const formattedSize = parseFloat((bytes / Math.pow(k, i)).toFixed(dm))

  return `${formattedSize} ${sizes[i]}`
}
</script>

<template>
  <form @submit.prevent="formSubmitHandler">
    <div class="flex flex-col">
      <div class="card">
        <Toast />
        <FileUpload
          name="file"
          :url="url"
          @upload="onTemplatedUpload"
          :multiple="false"
          accept="text/xml"
          @select="onSelectedFiles"
          :custom-upload="false"
        >
          <template #header="{ chooseCallback, uploadCallback, clearCallback, files }">
            <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
              <div class="flex gap-2">
                <Button
                  @click="chooseCallback()"
                  icon="pi pi-images"
                  :label="t('labels.choose')"
                  outlined
                ></Button>
                <Button
                  v-if="files.length > 0"
                  @click="uploadEvent(uploadCallback)"
                  icon="pi pi-upload"
                  :label="t('labels.import')"
                  outlined
                  severity="success"
                  :disabled="!files || files.length === 0"
                ></Button>
                <Button
                  v-if="files.length > 0"
                  @click="clearCallback()"
                  :label="t('labels.clear')"
                  icon="pi pi-times"
                  outlined
                  severity="danger"
                  :disabled="!files || files.length === 0"
                ></Button>
              </div>
              <ProgressBar
                :value="totalSizePercent"
                :showValue="false"
                :class="[
                  'md:w-20rem h-1rem w-full md:ml-auto',
                  { 'exceeded-progress-bar': totalSizePercent > 100 }
                ]"
                ><span class="white-space-nowrap">{{ totalSize }}B / 1Mb</span>
              </ProgressBar>
            </div>
          </template>
          <template
            #content="{ files, uploadedFiles, removeUploadedFileCallback, removeFileCallback }"
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
                    class="mb-2"
                    :label="t('labels.clear')"
                    icon="pi pi-times"
                    @click="onRemoveTemplatingFile(file, removeFileCallback, index)"
                    outlined
                    severity="danger"
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
                  <Badge :value="t('messages.completed')" class="mt-3" severity="success" />
                  <Button
                    class="mb-2"
                    :label="t('labels.clear')"
                    icon="pi pi-times"
                    @click="removeUploadedFileCallback(index)"
                    outlined
                    severity="danger"
                  />
                </div>
              </div>
            </div>
          </template>
          <template #empty>
            <div class="flex align-items-center justify-content-center flex-column">
              <i
                class="pi pi-cloud-upload border-2 border-circle p-5 text-5xl text-400 border-400"
              />
              <p class="mt-4 mb-0">{{ t('messages.drag_drop') }}</p>
            </div>
          </template>
        </FileUpload>
      </div>
    </div>
    <div class="mt-2">
      <slot></slot>
    </div>
  </form>
</template>

<style scoped></style>
