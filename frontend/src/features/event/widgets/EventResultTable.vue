<script setup lang="ts">
import type { Cup } from '@/features/cup/model/cup'
import type { CupScoreList } from '@/features/event/model/cup_score_list'
import type { PersonResult } from '@/features/event/model/person_result'
import type { ResultListIdPersonResults } from '@/features/event/model/result_list_id_person_results'
import type { Organisation } from '@/features/organisation/model/organisation'
import type { Person } from '@/features/person/model/person'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import moment from 'moment'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { cupService } from '@/features/cup/services/cup.service'
import { EventService } from '@/features/event/services/event.service'
import { organisationService } from '@/features/organisation/services/organisation.service'
import { personService } from '@/features/person/services/person.service'

const props = defineProps<{ data: ResultListIdPersonResults, eventId: number }>()

const { t } = useI18n()

const cupQuery = useQuery({
    queryKey: ['cups'],
    queryFn: () => cupService.getAllUnpaged(t),
})

const cups = computed((): Cup[] => {
    const allCupIds = props.data?.cupScoreLists ? props.data.cupScoreLists.map(x => x.cupId) : []
    const cupIds = new Set(allCupIds)
    return cupQuery.data?.value && Array.isArray(cupQuery.data?.value)
        ? cupQuery.data.value.filter((x) => {
                return cupIds.has(x.id)
            })
        : []
})

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
    else return moment(date).format('YY')
}

const personQuery = useQuery({
    queryKey: ['persons'],
    queryFn: () => personService.getAllUnpaged(t),
})

const organisationQuery = useQuery({
    queryKey: ['organisations'],
    queryFn: () => organisationService.getAllUnpaged(t),
})

function resultColumn(data: PersonResult): string {
    return data.resultStatus === 'OK'
        ? formatTime(data.runTime)
        : t(`result_state.${data.resultStatus}`)
}

function cupScore(cup: Cup, data: PersonResult, cupScoreLists: CupScoreList[] | undefined): string {
    const cupScoreList = cupScoreLists?.filter(x => x.cupId === cup.id).pop()
    const cupScore = cupScoreList?.cupScores.filter(x => x.personId === data.personId).pop()
    return cupScore ? cupScore.score.toLocaleString() : ''
}

function birthYearColumn(data: any): string {
    const person = findPerson(data.personId)
    if (person)
        return person.birthDate ? formatBirthYear(person.birthDate) : ''

    return ''
}

function findPerson(personId: number): Person | undefined {
    if (personId && personQuery.data?.value && Array.isArray(personQuery.data.value))
        return personQuery.data.value.find(p => p?.id === personId)

    return undefined
}

function findOrganisation(organisationId: number): Organisation | undefined {
    if (organisationId && organisationQuery.data.value && Array.isArray(organisationQuery.data.value))
        return organisationQuery.data.value.find(o => o?.id === organisationId)

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
        <Column field="position" :header="t('labels.position')">
            <template #body="slotProps">
                <div :id="`person-result-${slotProps.data.personId}`">
                    {{ slotProps.data.position }}
                </div>
            </template>
        </Column>
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
        <div v-if="props.data.cupScoreEnabled">
            <Column v-for="cup in cups" :key="cup.id" :header="cup.type?.id">
                <template #body="slotProps">
                    {{ cupScore(cup, slotProps.data, props.data.cupScoreLists) }}
                </template>
            </Column>
        </div>
        <Column>
            <template #body="slotProps">
                <Button
                    v-if="props.data.certificateEnabled && slotProps.data.resultStatus === 'OK'"
                    v-tooltip="t('labels.download')"
                    :aria-label="t('labels.download')"
                    icon="pi pi-print"
                    outlined
                    raised
                    rounded
                    @click="
                        certificate(
                            props.data.resultListId,
                            props.data.classResultShortName,
                            slotProps.data,
                        )
                    "
                />
            </template>
        </Column>
    </DataTable>
</template>
