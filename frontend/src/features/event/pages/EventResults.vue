<script setup lang="ts">
import { useEventStore } from '@/features/event/store/event.store'
import { useQuery } from '@tanstack/vue-query'
import { EventService } from '@/features/event/services/event.service'
import { useI18n } from 'vue-i18n'
import Tree from 'primevue/tree'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import type { ClassResult } from '@/features/event/model/class_result'
import type { TreeNode } from 'primevue/treenode'
import { computed } from 'vue'
import moment from 'moment'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const props = defineProps<{ id: string; locale?: string }>()
const store = useEventStore()
const event = store.selectEvent(+props.id)

const { t } = useI18n()

const authStore = useAuthStore()

function parseDurationMoment(durationString: string): moment.Duration {
  return moment.duration(durationString)
}

const formatTime = (time: string): string => {
  return moment.utc(parseDurationMoment(time).asMilliseconds()).format('H:mm:ss')
}

const eventResultsQuery = useQuery({
  queryKey: ['eventResults'],
  queryFn: () => EventService.getResultsById(props.id)
})

const createTreeNodes = (aList: ClassResult[] | undefined): TreeNode[] => {
  if (!aList) {
    return []
  }
  return aList.map(
    (a): TreeNode => ({
      key: a.id.toString(),
      label: a.name,
      children: [
        {
          key: `${a.id}-table`,
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
    return createTreeNodes(eventResultsQuery.data.value?.classResultDtos)
  }
  return null
})

const resultColumn = (slotProps: any): string => {
  return slotProps.data.resultStatus === 'OK'
    ? formatTime(slotProps.data.runTime)
    : t('result_state.' + slotProps.data.resultStatus)
}
const birthYearColumn = (slotProps: any): string => {
  return slotProps.data.birthYear ? slotProps.data.birthYear.slice(-2) : ''
}

const calculate = () => {
  EventService.calculate(props.id, t)
}
</script>

<template>
  <h2 v-if="event">{{ event.name }}</h2>
  <Button :label="t('labels.calculate')" @click="calculate()" v-if="authStore.isAdmin" />
  <span v-if="eventResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
  <span v-else-if="eventResultsQuery.status.value === 'error'">
    {{ t('messages.error', { message: eventResultsQuery.error.toLocaleString() }) }}
  </span>
  <div v-else-if="eventResultsQuery.data" class="card flex justify-content-start">
    <Tree :value="treeNodes" class="w-full">
      <template #default="slotProps">
        <b>{{ slotProps.node.label }}</b>
      </template>
      <template #dataTable="slotProps">
        <DataTable :value="slotProps.node.data">
          <Column field="position" :header="t('labels.position')" />
          <Column field="personName" :header="t('labels.name')" />
          <Column :header="t('labels.birth_year')">
            <template #body="slotProps">
              {{ birthYearColumn(slotProps) }}
            </template>
          </Column>
          <Column field="organisation" :header="t('labels.organisation')" />
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
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
