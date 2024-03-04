<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { useQuery } from '@tanstack/vue-query'
import { personService } from '@/features/person/services/person.service'
import { organisationService } from '@/features/organisation/services/organisation.service'
import type { PersonResult } from '@/features/event/model/person_result'
import type { Person } from '@/features/person/model/person'
import moment from 'moment'
import type { Organisation } from '@/features/organisation/model/organisation'
import type { ResultListIdPersonResults } from '@/features/event/model/result_list_id_person_results'
import { EventService } from '@/features/event/services/event.service'
import Button from 'primevue/button'

const props = defineProps<{ data: ResultListIdPersonResults }>()

const { t } = useI18n()

function parseDurationMoment(durationString: string): moment.Duration {
  return moment.duration(durationString)
}

function parseDateMoment(dateString: string): moment.Moment {
  return moment(dateString)
}

const formatTime = (time: string): string => {
  return moment.utc(parseDurationMoment(time).asMilliseconds()).format('H:mm:ss')
}

const formatBirthYear = (date: string | Date): string => {
  if (typeof date === 'string') {
    return parseDateMoment(date).format('YY')
  } else {
    return moment(date).format('YY')
  }
}

const personQuery = useQuery({
  queryKey: ['persons'],
  queryFn: () => personService.getAll(t)
})

const organisationQuery = useQuery({
  queryKey: ['organisations'],
  queryFn: () => organisationService.getAll(t)
})

const resultColumn = (data: PersonResult): string => {
  return data.resultStatus === 'OK'
    ? formatTime(data.runTime)
    : t('result_state.' + data.resultStatus)
}

const birthYearColumn = (data: any): string => {
  const person = findPerson(data.personId)
  if (person) {
    return person.birthDate ? formatBirthYear(person.birthDate) : ''
  }
  return ''
}

const findPerson = (personId: number): Person | undefined => {
  if (personId && personQuery.data.value) {
    return personQuery.data.value.content.find((p) => p.id === personId)
  }
  return undefined
}

const findOrganisation = (organisationId: number): Organisation | undefined => {
  if (organisationId && organisationQuery.data.value) {
    return organisationQuery.data.value.content.find((o) => o.id === organisationId)
  }
  return undefined
}

const personNameColumn = (data: PersonResult): string => {
  const person = findPerson(data.personId)
  if (person) {
    return person.givenName + ' ' + person.familyName
  }
  return ''
}

const organisationNameColumn = (data: PersonResult): string => {
  const organisation = findOrganisation(data.organisationId)
  if (organisation) {
    return organisation.name
  }
  return ''
}

const certificate = (resultListId: number, data: PersonResult) => {
  EventService.certificate(resultListId, data.personId, t)
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
          class="p-button-rounded p-button-text"
          icon="pi pi-print"
          @click="certificate(props.data.resultListId, slotProps.data)"
        />
      </template>
    </Column>
  </DataTable>
</template>
