<script setup lang="ts">
import InputText from 'primevue/inputtext'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import Textarea from 'primevue/textarea'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import Dropdown, { type DropdownChangeEvent } from 'primevue/dropdown'
import VuePdfEmbed from 'vue-pdf-embed'
import type { Certificate } from '@/features/certificate/model/certificate'

import type { CertificateService } from '@/features/certificate/services/certificate.service'
import { EventService, eventService } from '@/features/event/services/event.service'
import type { EventKey } from '@/features/event/model/event_key'
import { mediaService } from '@/features/media/services/media.service'
import type { MediaKey } from '@/features/media/model/media_key'
import type { SportEvent } from '@/features/event/model/sportEvent'
import type { Media } from '@/features/media/model/media'

import 'vue-pdf-embed/dist/style/index.css'

const props = defineProps<{
    certificate?: Certificate
    entityService: CertificateService
    queryKey: string[]
}>()

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

const queryClient = useQueryClient()

const certificate = computed({
    get: () => props.certificate,
    set: value => emit('update:modelValue', value),
})

const eventQuery = useQuery({
    queryKey: ['events'],
    queryFn: () => eventService.getAll(t),
})

const mediaQuery = useQuery({
    queryKey: ['media'],
    queryFn: () => mediaService.getAll(t),
})

const certificateQuery = useQuery({
    queryKey: ['certificate', certificate.value?.id, certificate.value?.event?.id],
    queryFn: () => EventService.getCertificate(certificate.value, t),
})

function getEventKeyFromId(id: number | null): EventKey | null {
    if (!eventQuery.data.value || !eventQuery.data.value.content)
        return null

    const event: SportEvent | undefined = eventQuery.data.value?.content.find(
        event => event.id === id,
    )
    if (event !== undefined) {
        return {
            id: event.id,
            name: event.name,
        }
    }
    return null
}

function getMediaKeyFromId(id: number | null): MediaKey | null {
    if (!mediaQuery.data.value || !mediaQuery.data.value.content)
        return null

    const media: Media | undefined = mediaQuery.data.value?.content.find(media => media.id === id)
    if (media !== undefined) {
        return {
            id: media.id,
            fileName: media.fileName,
            thumbnailContent: media.thumbnailContent,
        }
    }
    return null
}

function handleEventSelectionChange(ev: DropdownChangeEvent) {
    if (ev.value && certificate.value && eventQuery.data.value && eventQuery.data.value.content) {
        certificate.value.event = getEventKeyFromId(ev.value.id)!
        queryClient.invalidateQueries({
            queryKey: ['certificate', certificate.value?.id, certificate.value?.event?.id],
        })
        certificateQuery.refetch()
    }
}

function handleMediaSelectionChange(ev: DropdownChangeEvent) {
    if (ev.value && certificate.value && mediaQuery.data.value && mediaQuery.data.value.content) {
        certificate.value.blankCertificate = getMediaKeyFromId(ev.value.id)!
        queryClient.invalidateQueries({
            queryKey: ['certificate', certificate.value?.id, certificate.value?.event?.id],
        })
    }
}

function handleLayoutDescriptionChange(ev: Event) {
    if (certificate.value && ev.target) {
        certificate.value.layoutDescription = (ev.target as HTMLTextAreaElement).value
        queryClient.invalidateQueries({
            queryKey: ['certificate', certificate.value?.id, certificate.value?.event?.id],
        })
    }
}
</script>

<template>
    <div v-if="certificate" class="flex flex-col">
        <div class="flex flex-row">
            <div class="flex flex-col">
                <div class="flex flex-row">
                    <label for="event" class="col-fixed w-32">{{ t('labels.event') }}</label>
                    <div class="col">
                        <span v-if="eventQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
                        <span v-else-if="eventQuery.status.value === 'error'">
                            {{ t('messages.error', { message: eventQuery.error.toLocaleString() }) }}
                        </span>
                        <Dropdown
                            v-else-if="eventQuery.data.value"
                            id="event"
                            v-model="certificate.event"
                            :options="eventQuery.data.value.content"
                            option-label="name"
                            data-key="id"
                            :placeholder="t('messages.select')"
                            class="w-full md:w-14rem"
                            filter
                            :disabled="certificate.event !== null"
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
                            v-model="certificate.blankCertificate"
                            :options="mediaQuery.data.value.content"
                            data-key="id"
                            option-label="fileName"
                            :placeholder="t('messages.select')"
                            class="w-full md:w-14rem"
                            filter
                            @change="handleMediaSelectionChange"
                        >
                            <template #option="value">
                                <div class="flex flex-row">
                                    <img
                                        :src="`data:image/jpeg;base64,${value.option.thumbnailContent}`"
                                        :alt="t('labels.preview')"
                                        style="width: 40px"
                                    >
                                    <div class="ml-2">
                                        {{ value.option.fileName }}
                                    </div>
                                </div>
                            </template>
                        </Dropdown>
                    </div>
                </div>
                <div class="flex flex-row">
                    <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
                    <div class="col">
                        <InputText id="name" v-model="certificate.name" type="text" />
                    </div>
                </div>
                <div class="flex flex-row flex-wrap">
                    <label for="layoutDescription" class="col-fixed w-32">{{
                        t('labels.layout_description')
                    }}</label>
                    <div class="col">
                        <Textarea
                            id="layoutDescription"
                            v-model="certificate.layoutDescription"
                            auto-resize
                            rows="20"
                            cols="40"
                            @input="handleLayoutDescriptionChange"
                        />
                    </div>
                </div>
                <div class="flex flex-row">
                    <label for="primary" class="col-fixed w-32">{{ t('labels.primary') }}</label>
                    <div class="col">
                        <Checkbox id="primary" v-model="certificate.primary" :binary="true" />
                    </div>
                </div>
            </div>
            <div v-if="certificateQuery.status.value" class="flex flex-col flex-grow ml-3">
                <div class="flex flex-row justify-between">
                    <label for="preview" class="col-fixed w-32">{{ t('labels.preview') }}</label>
                    <div>
                        <Button
                            outlined
                            severity="primary"
                            :label="t('labels.reload')"
                            class="mb-2"
                            @click="certificateQuery.refetch()"
                        />
                    </div>
                </div>
                <span v-if="certificateQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
                <span v-else-if="certificateQuery.status.value === 'error'">
                    {{ t('messages.error', { message: mediaQuery.error.toLocaleString() }) }}
                </span>
                <div v-else-if="certificateQuery.data.value" class="flex">
                    <VuePdfEmbed :source="certificateQuery.data.value" :width="600" />
                    <!-- embed
                                                                                                                                    id="preview"
                                                                                                                                    :src="certificateQuery.data.value"
                                                                                                                                    width="100%"
                                                                                                                                    height="1100"
                                                                                                                                    class=""
                                                                                                                                  / -->
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped></style>
