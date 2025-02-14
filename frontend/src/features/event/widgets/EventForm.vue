<script setup lang="ts">
import type { Certificate } from '@/features/certificate/model/certificate'
import type { CertificateKey } from '@/features/certificate/model/certificate_key'
import type { SportEvent } from '@/features/event/model/sportEvent'

import type { OrganisationKey } from '@/features/organisation/model/organisation_key'
import type { MultiSelectChangeEvent } from 'primevue/multiselect'
import type { SelectChangeEvent } from 'primevue/select'
import { certificateService } from '@/features/certificate/services/certificate.service'
import { EventService } from '@/features/event/services/event.service'
import { organisationService } from '@/features/organisation/services/organisation.service'
import { prettyPrint } from '@base2/pretty-print-object'
import { useQuery } from '@tanstack/vue-query'
import DatePicker from 'primevue/datepicker'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import Select from 'primevue/select'
import { computed, onBeforeUpdate, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
    event: SportEvent
    entityService: EventService
    queryKey: string[]
}>()

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

const event = computed({
    get: () => props.event,
    set: value => emit('update:modelValue', value),
})

const certificateQuery = useQuery({
    queryKey: ['certificates'],
    queryFn: () => certificateService.getAllUnpaged(t),
})

const l_organisations = computed({
    get: () => (event.value ? event.value.organisations.map(org => org.id) : []),
    set: (ids) => {
        if (event.value) {
            event.value.organisations = getOrganisationKeysFromIds(ids)
        }
    },
})

const organisationQuery = useQuery({
    queryKey: ['organisations'],
    queryFn: () => organisationService.getAllUnpaged(t),
})

const eventStatusQuery = useQuery({
    queryKey: ['event_status'],
    queryFn: () => EventService.getEventStatus(t),
})

const localizedEventStatusOptions = computed(() => {
    if (eventStatusQuery.data.value) {
        return eventStatusQuery.data.value.map(option => ({
            ...option,
            label: t(`event_state.${option.id.toLocaleUpperCase()}`),
        }))
    }
    return []
})

const dateTime = computed({
    get: () => new Date(event.value ? event.value.startTime : new Date()),
    set: (newDateTime) => {
        if (event.value)
            event.value.startTime = newDateTime.toISOString()
    },
})

const datePart = computed({
    get: () =>
        new Date(dateTime.value.getFullYear(), dateTime.value.getMonth(), dateTime.value.getDate()),
    set: (newDate) => {
        dateTime.value = new Date(
            newDate.getFullYear(),
            newDate.getMonth(),
            newDate.getDate(),
            dateTime.value.getHours(),
            dateTime.value.getMinutes(),
        )
        if (props.event && event.value)
            event.value.startTime = dateTime.value.toISOString()
    },
})

const timePart = computed({
    get: () => {
        return new Date(
            dateTime.value.getFullYear(),
            dateTime.value.getMonth(),
            dateTime.value.getDate(),
            dateTime.value.getHours(),
            dateTime.value.getMinutes(),
        )
    },
    set: (newTime) => {
        dateTime.value = new Date(
            dateTime.value.getFullYear(),
            dateTime.value.getMonth(),
            dateTime.value.getDate(),
            newTime.getHours(),
            newTime.getMinutes(),
        )
        if (event.value)
            event.value.startTime = dateTime.value.toISOString()
    },
})

onMounted(() => {
    if (props.event)
        dateTime.value = new Date(props.event.startTime)
})

function getOrganisationKeysFromIds(ids: number[]): OrganisationKey[] {
    if (!organisationQuery.data?.value || !Array.isArray(organisationQuery.data?.value)) {
        return []
    }

    const organisations = organisationQuery.data.value
    return ids
        .map(id => organisations.find(b => b.id === id))
        .filter(org => org !== undefined && org.id !== undefined)
        .map(org => ({ id: org!.id, name: org!.name }) as OrganisationKey)
}

function handleSelectionChange(ev: MultiSelectChangeEvent) {
    if (
        ev.value
        && event.value
        && organisationQuery.data.value
    ) {
        event.value.organisations = getOrganisationKeysFromIds(ev.value)
    }
}

onBeforeUpdate(() => {
    console.log('EventForm:onBeforeUpdate:props.event', prettyPrint(props.event))
    console.log('EventForm:onBeforeUpdate:dateTime', prettyPrint(dateTime.value))
})

function getCertificateKeyFromId(id: number | null): CertificateKey | null {
    if (!certificateQuery.data.value || !Array.isArray(certificateQuery.data.value)) {
        return null
    }

    const certificate: Certificate | undefined = certificateQuery.data?.value.find(
        certificate => certificate.id === id,
    )
    if (certificate) {
        return {
            id: certificate.id,
            name: certificate.name,
        }
    }
    return null
}

function handleCertificateSelectionChange(ev: SelectChangeEvent) {
    if (
        ev.value
        && event.value
        && certificateQuery.data.value
    ) {
        event.value.certificate = getCertificateKeyFromId(ev.value.id)!
    }
}
</script>

<template>
    <div v-if="event" class="flex flex-col">
        <div class="flex flex-row">
            <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
            <div class="col">
                <InputText id="name" v-model="event.name" type="text" />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="startDate" class="col-fixed w-32">{{ t('labels.date') }}</label>
            <div class="col">
                <DatePicker id="startDate" v-model="datePart" date-format="dd.mm.yy" show-icon />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="startTime" class="col-fixed w-32">{{ t('labels.time') }}</label>
            <div class="col">
                <DatePicker
                    id="startTime"
                    v-model="timePart"
                    show-icon
                    icon-display="input"
                    time-only
                >
                    <template #inputicon="{ clickCallback }">
                        <i
                            v-tooltip="t('labels.select')"
                            class="pi pi-clock ml-2 mt-2"
                            :aria-label="t('labels.select')"
                            rounded
                            @click="clickCallback"
                        />
                    </template>
                </DatePicker>
            </div>
        </div>
        <div class="flex flex-row">
            <label for="type" class="col-fixed w-40">{{ t('labels.state') }}</label>
            <div class="col">
                <span v-if="eventStatusQuery.status.value === 'pending'">{{
                    t('messages.loading')
                }}</span>
                <Select
                    v-else-if="eventStatusQuery.data.value"
                    id="state"
                    v-model="event.state"
                    :options="localizedEventStatusOptions"
                    option-label="label"
                    data-key="id"
                    :placeholder="t('messages.select')"
                    class="w-full md:w-14rem"
                />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="organisations" class="col-fixed w-32">{{
                t('labels.organisation', 2)
            }}</label>
            <div class="col">
                <span v-if="organisationQuery.status.value === 'pending'">{{
                    t('messages.loading')
                }}</span>
                <div
                    v-else-if="organisationQuery.data && organisationQuery.data.value"
                    class="card"
                >
                    <MultiSelect
                        id="organisations"
                        v-model="l_organisations"
                        :options="organisationQuery.data.value"
                        data-key="id"
                        filter
                        option-label="name"
                        option-value="id"
                        :placeholder="t('messages.select')"
                        class="w-full md:w-20rem"
                        @change="handleSelectionChange"
                    />
                </div>
            </div>
        </div>
        <div class="flex flex-row">
            <label for="certificate" class="col-fixed w-32">{{ t('labels.certificate') }}</label>
            <div class="col">
                <span v-if="certificateQuery.status.value === 'pending'">{{
                    t('messages.loading')
                }}</span>
                <Select
                    v-else-if="certificateQuery.data.value"
                    id="certificate"
                    v-model="event.certificate"
                    :options="certificateQuery.data.value"
                    option-label="name"
                    data-key="id"
                    :placeholder="t('messages.select')"
                    class="w-full md:w-14rem"
                    filter
                    @change="handleCertificateSelectionChange"
                />
            </div>
        </div>
    </div>
</template>

<style scoped></style>
