<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { EventService } from '@/features/event/services/event.service'
import { useI18n } from 'vue-i18n'
import type { ClassResult } from '@/features/event/model/class_result'
import type { TreeNode } from 'primevue/treenode'
import { computed } from 'vue'
import moment from 'moment'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { ResultList } from '@/features/event/model/result_list'
import Tree from 'primevue/tree'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { personService } from '@/features/person/services/person.service'
import { organisationService } from '@/features/organisation/services/organisation.service'
import { courseService } from '@/features/course/services/course.service'

const props = defineProps<{ id: string; locale?: string }>()

const { t } = useI18n()

const authStore = useAuthStore()

function parseDurationMoment(durationString: string): moment.Duration {
  return moment.duration(durationString)
}

const formatTime = (time: string): string => {
  return moment.utc(parseDurationMoment(time).asMilliseconds()).format('H:mm:ss')
}

const eventResultsQuery = useQuery({
  queryKey: ['eventResults', props.id],
  queryFn: () => EventService.getResultsById(props.id, t)
})

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

const createResultListTreeNodes = (aList: ResultList[] | undefined): TreeNode[] => {
  if (!aList) {
    return []
  }
  if (aList.length >= 2) {
    const treeNodes: TreeNode[] = []
    for (let i = 0; i < aList.length; i++) {
      treeNodes.push({
        key: aList[i].id.toString(),
        label: aList[i].status + ' ' + aList[i].createTime,
        children: [
          {
            key: `${aList[i].status}-table`,
            data: createClassResultTreeNodes(aList[i].classResults),
            type: 'dataTable',
            leaf: true
          }
        ]
      })
    }
    return treeNodes
  } else if (aList.length === 1) {
    return createClassResultTreeNodes(aList[0].classResults)
  }
  return []
}

function getClassResultLabel(a: ClassResult) {
  return (
    a.name +
    ' (' +
    a.personResults.length +
    ')' +
    ' - ' +
    courseLengthColumn(a) +
    ' ' +
    t('labels.length_abbreviation') +
    ' - ' +
    courseClimbColumn(a) +
    ' ' +
    t('labels.climb_abbreviation') +
    ' - ' +
    courseControlsColumn(a) +
    ' ' +
    t('labels.control_abbreviation')
  )
}

const createClassResultTreeNodes = (aList: ClassResult[] | undefined): TreeNode[] => {
  if (!aList) {
    return []
  }
  return aList.map(
    (a): TreeNode => ({
      key: a.shortName.toString(),
      label: getClassResultLabel(a),
      children: [
        {
          key: `${a.shortName}-table`,
          data: a.personResults,
          type: 'dataTable',
          leaf: true
        }
      ]
    })
  )
}

const treeNodes = computed(() => {
  if (eventResultsQuery.isFetched) {
    return createResultListTreeNodes(eventResultsQuery.data.value?.resultLists)
  }
  return null
})

const resultColumn = (slotProps: any): string => {
  return slotProps.data.resultStatus === 'OK'
    ? formatTime(slotProps.data.runTime)
    : t('result_state.' + slotProps.data.resultStatus)
}

const birthYearColumn = (slotProps: any): string => {
  const person = findPerson(slotProps)
  if (person) {
    return person.birthDate ? person.birthDate.toLocaleString() : ''
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

const findCourse = (slotProps: any) => {
  if (slotProps.courseId && courseQuery.data.value) {
    return courseQuery.data.value.find((c) => c.id === slotProps.courseId)
  }
  return null
}

/*
const courseNameColumn = (slotProps: any): string => {
  const course = findCourse(slotProps)
  if (course) {
    return course.name
  }
  return ''
}
*/

const courseLengthColumn = (slotProps: any): string => {
  const course = findCourse(slotProps)
  if (course) {
    return (course.length / 1000.0).toFixed(1)
  }
  return ''
}

const courseClimbColumn = (slotProps: any): string => {
  const course = findCourse(slotProps)
  if (course) {
    return course.climb.toFixed(0)
  }
  return ''
}

const courseControlsColumn = (slotProps: any): string => {
  const course = findCourse(slotProps)
  if (course) {
    return course.controls.toFixed(0)
  }
  return ''
}

const calculate = () => {
  EventService.calculate(props.id, t)
}
</script>

<template>
  <Button :label="t('labels.calculate')" @click="calculate()" v-if="authStore.isAdmin" />
  <span v-if="eventResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
  <span v-else-if="eventResultsQuery.status.value === 'error'">
    {{ t('messages.error', { message: eventResultsQuery.error.toLocaleString() }) }}
  </span>
  <div v-else-if="eventResultsQuery.data" class="card flex justify-content-start">
    <div class="flex flex-col flex-grow">
      <div v-if="eventResultsQuery.data.value?.resultLists?.length === 1" class="flex flex-row">
        <div>
          {{ t('labels.created') }}
        </div>
        <div class="ml-2">
          {{ eventResultsQuery.data.value?.resultLists[0]?.createTime.toLocaleString() }}
        </div>
        <div class="ml-2">
          {{ eventResultsQuery.data.value?.resultLists[0]?.status }}
        </div>
      </div>
      <Tree :value="treeNodes" class="w-full">
        <template #default="slotProps">
          <b>{{ slotProps?.node?.label }}</b>
        </template>
        <template #dataTable="slotProps">
          <DataTable :value="slotProps?.node?.data">
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
      </Tree>
    </div>
  </div>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
