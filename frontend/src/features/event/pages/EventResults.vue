<script lang="ts" setup>
import { useQuery } from '@tanstack/vue-query'
import { EventService } from '@/features/event/services/event.service'
import { useI18n } from 'vue-i18n'
import type { ClassResult } from '@/features/event/model/class_result'
import type { TreeNode } from 'primevue/treenode'
import { computed } from 'vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { ResultList } from '@/features/event/model/result_list'
import Tree from 'primevue/tree'
import { courseService } from '@/features/course/services/course.service'
import EventResultTable from '@/features/event/widgets/EventResultTable.vue'
import { raceService } from '@/features/race/services/race.service'

const props = defineProps<{ id: string; locale?: string }>()

const { t } = useI18n()

const authStore = useAuthStore()

const eventResultsQuery = useQuery({
  queryKey: ['eventResults', props.id],
  queryFn: () => EventService.getResultsById(props.id, t)
})

const courseQuery = useQuery({
  queryKey: ['courses'],
  queryFn: () => courseService.getAll(t)
})

const raceQuery = useQuery({
  queryKey: ['races'],
  queryFn: () => raceService.getAll(t)
})

function getResultListLabel(resultList: ResultList) {
  const name = raceQuery.data.value?.find((r) => r.id === resultList.raceId)?.name
  return name
    ? name
    : t('labels.created') + ' ' + resultList.createTime.toLocaleString() + ' ' + resultList.status
}

function getRace(raceId: number | undefined) {
  return raceQuery.data.value?.find((r) => r.id === raceId)
}

const createResultListTreeNodes = (aList: ResultList[] | undefined): TreeNode[] => {
  if (!aList) {
    return []
  }
  const treeNodes: TreeNode[] = []
  for (let i = 0; i < aList.length; i++) {
    const resultList = aList[i]
    treeNodes.push({
      key: resultList.id.toString(),
      label: getResultListLabel(resultList),
      children: [
        {
          key: `${resultList.id.toString()}-table`,
          data: createClassResultTreeNodes(resultList.classResults),
          type: 'tree',
          leaf: false
        }
      ]
    })
  }
  return treeNodes
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
  return undefined
})

const findCourse = (slotProps: any) => {
  if (slotProps.courseId && courseQuery.data.value) {
    return courseQuery.data.value.find((c) => c.id === slotProps.courseId)
  }
  return null
}

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

const resultList0 = computed(() => {
  return eventResultsQuery.data.value?.resultLists[0]
})
</script>

<template>
  <!--Button v-if="authStore.isAdmin" :label="t('labels.calculate')" @click="calculate()" /-->
  <span v-if="eventResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
  <span v-else-if="eventResultsQuery.status.value === 'error'">
    {{ t('messages.error', { message: eventResultsQuery.error.toLocaleString() }) }}
  </span>
  <div v-else-if="eventResultsQuery.data" class="card flex justify-content-start">
    <div class="flex flex-col flex-grow">
      <div v-if="eventResultsQuery.data.value?.resultLists?.length === 1" class="flex flex-row">
        <!--ResultListHeader :resultList="resultList0" :race="getRace(resultList0?.raceId)" /-->
      </div>
      <Tree :value="treeNodes" class="w-full">
        <template #default="slotProps">
          <b>{{ slotProps?.node?.label }}</b>
        </template>
        <template #tree="slotProps">
          <Tree :value="slotProps?.node?.data">
            <template #default="slotProps">
              <b>{{ slotProps?.node?.label }}</b>
            </template>
            <template #dataTable="slotProps">
              <EventResultTable :data="slotProps?.node?.data" />
            </template>
          </Tree>
        </template>
        <template #dataTable="slotProps">
          <EventResultTable :data="slotProps?.node?.data" />
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
