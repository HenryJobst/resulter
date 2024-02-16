<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { useI18n } from 'vue-i18n'
import moment from 'moment'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { personService } from '@/features/person/services/person.service'
import { organisationService } from '@/features/organisation/services/organisation.service'
import { courseService } from '@/features/course/services/course.service'

const props = defineProps<{ data: any }>()

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

const courseQuery = useQuery({
  queryKey: ['courses'],
  queryFn: () => courseService.getAll(t)
})

const resultColumn = (slotProps: any): string => {
  return slotProps.data.resultStatus === 'OK'
    ? formatTime(slotProps.data.runTime)
    : t('result_state.' + slotProps.data.resultStatus)
}

const birthYearColumn = (slotProps: any): string => {
  const person = findPerson(slotProps)
  if (person) {
    return person.birthDate ? formatBirthYear(person.birthDate) : ''
  }
  return ''
}

const findPerson = (slotProps: any) => {
  if (slotProps.data.personId && personQuery.data.value) {
    return personQuery.data.value.find((p) => p.id === slotProps.data.personId)
  }
  return null
}

const personNameColumn = (slotProps: any): string => {
  const person = findPerson(slotProps)
  if (person) {
    return person.givenName + ' ' + person.familyName
  }
  return ''
}

const findOrganisation = (slotProps: any) => {
  if (slotProps.data.organisationId && organisationQuery.data.value) {
    return organisationQuery.data.value.find((o) => o.id === slotProps.data.organisationId)
  }
  return null
}

const organisationNameColumn = (slotProps: any): string => {
  const organisation = findOrganisation(slotProps)
  if (organisation) {
    return organisation.name
  }
  return ''
}
</script>

<template>
  <DataTable :value="props.data">
    <Column field="position" :header="t('labels.position')" />
    <Column :header="t('labels.name')">
      <template #body="slotProps">
        {{ personNameColumn(slotProps) }}
      </template>
    </Column>
    <Column :header="t('labels.birth_year')">
      <template #body="slotProps">
        {{ birthYearColumn(slotProps) }}
      </template>
    </Column>
    <Column :header="t('labels.organisation')">
      <template #body="slotProps">
        {{ organisationNameColumn(slotProps) }}
      </template>
    </Column>
    <Column :header="t('labels.time')">
      <template #body="slotProps">
        {{ resultColumn(slotProps) }}
      </template>
    </Column>
    <!--Column
                                                                                                                                                                                                                                                                                                                                                                                                                                            v-for="score in cupScores"
                                                                                                                                                                                                                                                                                                                                                                                                                                            :key="score.type.name"
                                                                                                                                                                                                                                                                                                                                                                                                                                            :header="score.type.name"
                                                                                                                                                                                                                                                                                                                                                                                                                                            :field="score.score"
                                                                                                                                                                                                                                                                                                                                                                                                                                          >
                                                                                                                                                                                                                                                                                                                                                                                                                                          </Column-->
  </DataTable>
</template>
