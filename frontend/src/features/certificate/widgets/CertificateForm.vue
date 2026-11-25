<script setup lang="ts">
import type { SelectChangeEvent } from 'openvue/select'
import type { Certificate } from '@/features/certificate/model/certificate'
import type { EventKey } from '@/features/event/model/event_key'
import type { SportEvent } from '@/features/event/model/sportEvent'
import type { Media } from '@/features/media/model/media'
import type { MediaKey } from '@/features/media/model/media_key'
import { prettyPrint } from '@base2/pretty-print-object'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { useDebounceFn } from '@vueuse/core'
import Button from 'openvue/button'
import Checkbox from 'openvue/checkbox'
import Drawer from 'openvue/drawer'
import InputText from 'openvue/inputtext'
import Select from 'openvue/select'
import SelectButton from 'openvue/selectbutton'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import VuePdfEmbed from 'vue-pdf-embed'
import { validateLayout } from '@/features/certificate/model/certificateLayoutValidation'
import CertificateLayoutCanvas from '@/features/certificate/widgets/CertificateLayoutCanvas.vue'
import CertificateLayoutEditor from '@/features/certificate/widgets/CertificateLayoutEditor.vue'
import NumberedTextarea from '@/features/certificate/widgets/NumberedTextarea.vue'
import { EventService, eventService } from '@/features/event/services/event.service'
import { mediaService } from '@/features/media/services/media.service'

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
    queryFn: () => mediaService.getAllUnpaged(t),
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

const layoutMode = ref<'graphic' | 'visual' | 'json'>('graphic')
const selectedBlock = ref<{ pi: number, bi: number } | null>(null)
const layoutModes = computed(() => [
    { value: 'graphic', label: t('certificateLayout.graphic') },
    { value: 'visual', label: t('certificateLayout.visual') },
    { value: 'json', label: t('certificateLayout.json') },
])
// schema errors are shown in both modes, a JSON syntax error only in the JSON mode
// (the visual mode reports it itself)
const layoutCheck = computed<{ syntaxError: string | null, schemaErrors: string[] }>(() => {
    const text = certificate.value?.layoutDescription
    if (!text || text.trim() === '') {
        return { syntaxError: null, schemaErrors: [] }
    }
    let data: unknown
    try {
        data = JSON.parse(text)
    }
    catch (e) {
        return { syntaxError: e instanceof Error ? e.message : String(e), schemaErrors: [] }
    }
    const schema = schemaQuery.data.value
    return { syntaxError: null, schemaErrors: schema ? validateLayout(schema, data) : [] }
})
const mediaFileNames = computed(() => mediaQuery.data.value?.map(m => m.fileName) ?? [])
const mediaThumbnails = computed(() =>
    Object.fromEntries((mediaQuery.data.value ?? []).map(m => [m.fileName, m.thumbnailContent])),
)

const formattedSchema = computed(() => {
    if (schemaQuery.isFetched)
        return prettyPrint(schemaQuery.data.value)

    return ''
})

function getEventKeyFromId(id: number | null): EventKey | null {
    if (!eventQuery.data?.value || !Array.isArray(eventQuery.data?.value)) {
        return null
    }

    const event: SportEvent | undefined = eventQuery.data.value?.find(ev => ev.id === id)
    if (event !== undefined) {
        return {
            id: event.id,
            name: event.name,
        } as EventKey
    }
    return null
}

function getMediaKeyFromId(id: number | null): MediaKey | null {
    if (!mediaQuery.data?.value || !Array.isArray(mediaQuery.data.value)) {
        return null
    }

    const media: Media | undefined = mediaQuery.data?.value.find(media => media.id === id)
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
    if (ev.value && certificate.value && eventQuery.data?.value) {
        certificate.value.event = getEventKeyFromId(ev.value.id)!
    }
    else {
        certificate.value.event = null
    }
}

function handleMediaSelectionChange(ev: SelectChangeEvent) {
    if (ev.value && certificate.value && mediaQuery.data?.value) {
        certificate.value.blankCertificate = getMediaKeyFromId(ev.value.id)!
    }
    else {
        certificate.value.blankCertificate = null
    }
}

// the preview is a rendered PDF, so wait until the user pauses typing
const refreshPreview = useDebounceFn(() => {
    queryClient.invalidateQueries({ queryKey: ['certificate'] })
}, 800)

watch(
    () => certificate.value,
    () => {
        refreshPreview()
    },
    { deep: true },
)
</script>

<template>
    <div v-if="certificate" class="flex flex-col">
        <div class="flex flex-row">
            <div class="flex flex-col">
                <div class="flex flex-row">
                    <label for="name" class="col-fixed w-32">{{ t('labels.name') }}<span aria-hidden="true"> *</span></label>
                    <div class="col flex flex-grow">
                        <InputText
                            id="name"
                            v-model="certificate.name"
                            :invalid="!certificate.name" aria-required="true"
                            type="text"
                            class="flex-grow"
                        />
                    </div>
                </div>
                <div class="flex flex-row">
                    <label for="event" class="col-fixed w-32">{{ t('labels.event') }}<span aria-hidden="true"> *</span></label>
                    <div class="col flex">
                        <span v-if="eventQuery.status.value === 'pending'">{{
                            t('messages.loading')
                        }}</span>
                        <Select
                            v-else-if="eventQuery.data.value"
                            id="event"
                            :model-value="certificate.event"
                            :invalid="!certificate.event" aria-required="true"
                            :options="eventQuery.data.value"
                            option-label="name"
                            data-key="id"
                            :placeholder="t('messages.select')"
                            class="flex flex-grow"
                            filter
                            show-clear
                            @change="handleEventSelectionChange"
                        />
                    </div>
                </div>
                <div class="flex flex-row">
                    <label for="media" class="col-fixed w-32">{{ t('labels.background') }}<span aria-hidden="true"> *</span></label>
                    <div class="col flex">
                        <span v-if="mediaQuery.status.value === 'pending'">{{
                            t('messages.loading')
                        }}</span>
                        <Select
                            v-else-if="mediaQuery.data.value"
                            id="media"
                            :model-value="certificate.blankCertificate"
                            :invalid="!certificate.blankCertificate" aria-required="true"
                            :options="mediaQuery.data?.value"
                            data-key="id"
                            option-label="fileName"
                            :placeholder="t('messages.select')"
                            class="flex flex-grow"
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
                <div class="flex flex-col">
                    <div class="flex flex-row flex-wrap">
                        <label for="layoutDescription" class="col-fixed w-60">{{
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
                        <SelectButton
                            v-model="layoutMode"
                            :options="layoutModes"
                            option-label="label"
                            option-value="value"
                            :allow-empty="false"
                            class="mb-2"
                        />
                        <ul
                            v-if="layoutCheck.schemaErrors.length > 0"
                            class="text-red-500 mb-2"
                            role="alert"
                        >
                            <li v-for="message in layoutCheck.schemaErrors" :key="message">
                                {{ message }}
                            </li>
                        </ul>
                        <p
                            v-if="layoutMode === 'json' && layoutCheck.syntaxError"
                            class="text-red-500 mb-2"
                            role="alert"
                        >
                            {{ layoutCheck.syntaxError }}
                        </p>
                        <CertificateLayoutCanvas
                            v-if="layoutMode === 'graphic'"
                            v-model="certificate.layoutDescription"
                            v-model:selected="selectedBlock"
                            :background="certificate.blankCertificate?.thumbnailContent"
                            :media="mediaThumbnails"
                            class="mb-3"
                        />
                        <CertificateLayoutEditor
                            v-if="layoutMode === 'graphic' || layoutMode === 'visual'"
                            v-model="certificate.layoutDescription"
                            :media-files="mediaFileNames"
                            :selected="layoutMode === 'graphic' ? selectedBlock : null"
                        />
                        <NumberedTextarea
                            v-else
                            id="layoutDescription"
                            v-model="certificate.layoutDescription"
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
                class="preview-panel flex flex-col ml-3"
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
                <div v-else-if="certificateQuery.data.value" style="border: 1px solid #ccc">
                    <VuePdfEmbed :source="certificateQuery.data.value" :width="500" />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.preview-panel {
    position: sticky;
    top: 130px;
    align-self: flex-start;
    padding: 10px;
}
</style>
