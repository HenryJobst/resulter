<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import moment from 'moment'
import Button from 'primevue/button'
import { personService } from '@/features/person/services/person.service'
import { organisationService } from '@/features/organisation/services/organisation.service'
import type { PersonResult } from '@/features/event/model/person_result'
import type { Person } from '@/features/person/model/person'
import type { Organisation } from '@/features/organisation/model/organisation'
import type { ResultListIdPersonResults } from '@/features/event/model/result_list_id_person_results'
import { EventService } from '@/features/event/services/event.service'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const props = defineProps<{ data: ResultListIdPersonResults, eventId: number }>()

const { t } = useI18n()

const queryClient = useQueryClient()
const authStore = useAuthStore()

function parseDurationMoment(durationString: string): moment.Duration {
    return moment.duration(durationString)
}

function parseDateMoment(dateString: string): moment.Moment {
    return moment(dateString)
}

function formatTime(time: string): string {
    return moment.utc(parseDurationMoment(time).asMilliseconds()).format('H:mm:ss')
}

function formatBirthYear(date: string | Date): string {
    if (typeof date === 'string')
        return parseDateMoment(date).format('YY')
    else
        return moment(date).format('YY')
}

const personQuery = useQuery({
    queryKey: ['persons'],
    queryFn: () => personService.getAll(t),
})

const organisationQuery = useQuery({
    queryKey: ['organisations'],
    queryFn: () => organisationService.getAll(t),
})

function resultColumn(data: PersonResult): string {
    return data.resultStatus === 'OK'
        ? formatTime(data.runTime)
        : t(`result_state.${data.resultStatus}`)
}

function birthYearColumn(data: any): string {
    const person = findPerson(data.personId)
    if (person)
        return person.birthDate ? formatBirthYear(person.birthDate) : ''

    return ''
}

function findPerson(personId: number): Person | undefined {
    if (personId && personQuery.data.value)
        return personQuery.data.value.content.find(p => p.id === personId)

    return undefined
}

function findOrganisation(organisationId: number): Organisation | undefined {
    if (organisationId && organisationQuery.data.value)
        return organisationQuery.data.value.content.find(o => o.id === organisationId)

    return undefined
}

function personNameColumn(data: PersonResult): string {
    const person = findPerson(data.personId)
    if (person)
        return `${person.givenName} ${person.familyName}`

    return ''
}

function organisationNameColumn(data: PersonResult): string {
    const organisation = findOrganisation(data.organisationId)
    if (organisation)
        return organisation.name

    return ''
}

function certificate(resultListId: number, classResultShortName: string, data: PersonResult) {
    EventService.certificate(resultListId, classResultShortName, data.personId, t).then(() => {
        queryClient.invalidateQueries({
            queryKey: ['eventCertificateStats', props.eventId, authStore.isAdmin],
        })
    })
}
</script>

<template>
    <DataTable :value="props.data.personResults">
        <Column field="position" :header="t('labels.position')" />
        <Column :header="t('labels.name')">
            <template #body="slotProps">
                {{ personNameColumn(slotProps.data) }}
            </template>
        </Column>
        <Column :header="t('labels.birth_year')">
            <template #body="slotProps">
                {{ birthYearColumn(slotProps.data) }}
            </template>
        </Column>
        <Column :header="t('labels.organisation')">
            <template #body="slotProps">
                {{ organisationNameColumn(slotProps.data) }}
            </template>
        </Column>
        <Column :header="t('labels.time')">
            <template #body="slotProps">
                {{ resultColumn(slotProps.data) }}
            </template>
        </Column>
        <Column>
            <template #body="slotProps">
                <Button
                    v-if="props.data.certificateEnabled && slotProps.data.resultStatus === 'OK'"
                    class="p-button-rounded p-button-text"
                    icon="pi pi-print"
                    @click="
                        certificate(props.data.resultListId, props.data.classResultShortName, slotProps.data)
                    "
                />
            </template>
        </Column>
    </DataTable>
</template>
