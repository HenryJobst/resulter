<script setup lang="ts">
import InputText from 'primevue/inputtext'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Media } from '@/features/media/model/media'

import type { MediaService } from '@/features/media/services/media.service'

const props = defineProps<{
    media?: Media
    entityService: MediaService
    queryKey: string[]
}>()

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

const media = computed({
    get: () => props.media,
    set: value => emit('update:modelValue', value),
})
</script>

<template>
    <div v-if="media" class="flex flex-col">
        <div class="flex flex-row">
            <label for="fileName" class="col-fixed w-32">{{ t('labels.fileName') }}</label>
            <div class="col">
                <InputText id="fileName" v-model="media.fileName" type="text" readonly="true" />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="contentType" class="col-fixed w-32">{{ t('labels.contentType') }}</label>
            <div class="col">
                <InputText
                    id="contentType"
                    v-model="media.contentType"
                    type="text"
                    readonly="true"
                />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="fileSize" class="col-fixed w-32">{{ t('labels.fileSize') }}</label>
            <div class="col">
                <InputNumber id="fileSize" v-model="media.fileSize" readonly />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="description" class="col-fixed w-32">{{ t('labels.description') }}</label>
            <div class="col">
                <InputText id="description" v-model="media.description" type="text" />
            </div>
        </div>
    </div>
</template>

<style scoped></style>
