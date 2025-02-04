<script setup lang="ts">
import InputText from 'primevue/inputtext'
import { useI18n } from 'vue-i18n'
import Textarea from 'primevue/textarea'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import Drawer from 'primevue/drawer'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import Select, { type SelectChangeEvent } from 'primevue/select'
import VuePdfEmbed from 'vue-pdf-embed'
import { computed, ref, watch } from 'vue'
import { prettyPrint } from '@base2/pretty-print-object'
import type { Certificate } from '@/features/certificate/model/certificate'
import { EventService, eventService } from '@/features/event/services/event.service'
import type { EventKey } from '@/features/event/model/event_key'
import { mediaService } from '@/features/media/services/media.service'
import type { MediaKey } from '@/features/media/model/media_key'
import type { SportEvent } from '@/features/event/model/sportEvent'
import type { Media } from '@/features/media/model/media'

const certificate = defineModel({
    type: Object as () => Certificate,
    default: null,
})

const { t } = useI18n()

const queryClient = useQueryClient()

const eventQuery = useQuery({
    queryKey: ['events'],
    queryFn: () => eventService.getAllUnpaged(t),
})

const mediaQuery = useQuery({
    queryKey: ['media'],
    queryFn: () => mediaService.getAll(t),
})

const certificateQuery = useQuery({
    queryKey: ['certificate', certificate.value?.id, certificate.value?.event?.id],
    queryFn: () => EventService.getCertificate(certificate.value, t),
    retry: 1,
})

const schemaQuery = useQuery({
    queryKey: ['certificate_schema'],
    queryFn: () => EventService.getCertificateSchema(t),
    retry: 1,
})

const visibleRight = ref(false)

const formattedSchema = computed(() => {
    if (schemaQuery.isFetched)
        return prettyPrint(schemaQuery.data.value)

    return ''
})

function getEventKeyFromId(id: number | null): EventKey | null {
    if (!eventQuery.data.value)
        return null

    const event: SportEvent | undefined = eventQuery.data.value?.find(
        ev => ev.id === id,
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

function handleEventSelectionChange(ev: SelectChangeEvent) {
    if (ev.value && certificate.value && eventQuery.data.value) {
        certificate.value.event = getEventKeyFromId(ev.value.id)!
    }
    else {
        certificate.value.event = null
    }
}

function handleMediaSelectionChange(ev: SelectChangeEvent) {
    if (ev.value && certificate.value && mediaQuery.data.value && mediaQuery.data.value.content) {
        certificate.value.blankCertificate = getMediaKeyFromId(ev.value.id)!
    }
    else {
        certificate.value.blankCertificate = null
    }
}

watch(
    () => certificate.value,
    () => {
        queryClient.invalidateQueries({ queryKey: ['certificate'] })
    },
    { deep: true },
)
</script>

<template>
    <div v-if="certificate" class="flex flex-col">
        <div class="flex flex-row">
            <div class="flex flex-col">
                <div class="flex flex-row">
                    <label for="event" class="col-fixed w-32">{{ t('labels.event') }}</label>
                    <div class="col">
                        <span v-if="eventQuery.status.value === 'pending'">{{
                            t('messages.loading')
                        }}</span>
                        <span v-else-if="eventQuery.status.value === 'error'">
                            {{
                                t('messages.error', { message: eventQuery.error.toLocaleString() })
                            }}
                        </span>
                        <Select
                            v-else-if="eventQuery.data.value"
                            id="event"
                            :model-value="certificate.event"
                            :options="eventQuery.data.value"
                            option-label="name"
                            data-key="id"
                            :placeholder="t('messages.select')"
                            class="w-full md:w-14rem"
                            filter
                            show-clear
                            @change="handleEventSelectionChange"
                        />
                    </div>
                </div>
                <div class="flex flex-row">
                    <label for="media" class="col-fixed w-32">{{ t('labels.background') }}</label>
                    <div class="col">
                        <span v-if="mediaQuery.status.value === 'pending'">{{
                            t('messages.loading')
                        }}</span>
                        <span v-else-if="mediaQuery.status.value === 'error'">
                            {{
                                t('messages.error', { message: mediaQuery.error.toLocaleString() })
                            }}
                        </span>
                        <Select
                            v-else-if="mediaQuery.data.value"
                            id="media"
                            :model-value="certificate.blankCertificate"
                            :options="mediaQuery.data.value.content"
                            data-key="id"
                            option-label="fileName"
                            :placeholder="t('messages.select')"
                            class="w-full md:w-14rem"
                            filter
                            show-clear
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
                        </Select>
                    </div>
                </div>
                <div class="flex flex-row">
                    <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
                    <div class="col">
                        <InputText id="name" v-model="certificate.name" type="text" />
                    </div>
                </div>
                <div class="flex flex-col">
                    <div class="flex flex-row flex-wrap">
                        <label for="layoutDescription" class="col-fixed w-32">{{
                            t('labels.layout_description')
                        }}</label>
                        <Button
                            v-tooltip="t('labels.help')"
                            icon="pi pi-question-circle"
                            :aria-label="t('labels.help')"
                            outlined
                            raised
                            rounded
                            @click="visibleRight = true"
                        />
                        <Drawer
                            v-if="schemaQuery.isFetched"
                            v-model:visible="visibleRight"
                            :header="t('labels.schema')"
                            position="right"
                            class="w-7"
                        >
                            <pre>{{ formattedSchema }}</pre>
                        </Drawer>
                    </div>
                    <div class="col">
                        <Textarea
                            id="layoutDescription"
                            v-model="certificate.layoutDescription"
                            rows="20"
                            cols="60"
                            style="overflow: auto; min-height: 300px;"
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
            <div
                v-if="certificateQuery.status.value"
                class="fixed-element flex flex-col flex-grow ml-3"
            >
                <div class="flex flex-row justify-between">
                    <label for="preview" class="col-fixed w-32">{{ t('labels.preview') }}</label>
                    <div>
                        <Button
                            v-tooltip="t('labels.reload')"
                            outlined
                            raised
                            rounded
                            severity="primary"
                            icon="pi pi-refresh"
                            class="mb-2"
                            :aria-label="t('labels.reload')"
                            @click="certificateQuery.refetch()"
                        />
                    </div>
                </div>
                <span v-if="certificateQuery.status.value === 'pending'">{{
                    t('messages.loading')
                }}</span>
                <span v-else-if="certificateQuery.status.value === 'error'">
                    {{ t('messages.error', { message: mediaQuery.error.toLocaleString() }) }}
                </span>
                <div v-else-if="certificateQuery.data.value" style="border: 1px solid #ccc">
                    <VuePdfEmbed :source="certificateQuery.data.value" :width="500" />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.fixed-element {
    position: fixed;
    top: 130px;
    left: 780px;
    padding: 10px;
}
</style>
