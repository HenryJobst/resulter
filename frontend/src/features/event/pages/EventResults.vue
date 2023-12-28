<script setup lang="ts">
import { useEventStore } from '@/features/event/store/event.store'
import { useQuery } from '@tanstack/vue-query'
import { EventService } from '@/features/event/services/event.service'
import { useI18n } from 'vue-i18n'
import Tree from 'primevue/tree'
import type { ClassResult } from '@/features/event/model/class_result'
import type { TreeNode } from 'primevue/treenode'
import { computed } from 'vue'
import moment from 'moment'

const props = defineProps<{ id: string; locale?: string }>()
const store = useEventStore()
const event = store.selectEvent(+props.id)

const { t, locale } = useI18n()

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
      children: a.personResults.map(
        (b): TreeNode => ({
          key: b.id.toString(),
          label: b.personName,
          data: { punchTime: formatTime(b.runTime), status: b.resultStatus },
          leaf: true,
          type: b.resultStatus === 'OK' ? 'result' : 'invalid'
        })
      )
    })
  )
}

const treeNodes = computed(() => {
  if (eventResultsQuery.isFetched) {
    return createTreeNodes(eventResultsQuery.data.value?.classResultDtos)
  }
})
</script>

<template>
  <h2 v-if="event">{{ event.name }}</h2>
  <span v-if="eventResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
  <span v-else-if="eventResultsQuery.status.value === 'error'">
    {{ t('messages.error', { message: eventResultsQuery.error.toLocaleString() }) }}
  </span>
  <!--DataTable
    v-else-if="eventResultsQuery.data"
    :value="eventResultsQuery.data.value?.classResultDtos"
    class="p-datatable-sm"
  >
    <Column field="name" />
  </DataTable-->
  <div v-else-if="eventResultsQuery.data" class="card flex justify-content-center">
    <Tree :value="treeNodes" class="w-full md:w-30rem">
      <template #default="slotProps">
        <b>{{ slotProps.node.label }}</b>
      </template>
      <template #result="slotProps">
        <div class="text-800">{{ slotProps.node.label }} {{ slotProps.node.data.punchTime }}</div>
      </template>
      <template #invalid="slotProps">
        <div class="text-700">
          {{ slotProps.node.label }} {{ t('result_state.' + slotProps.node.data.status) }}
        </div>
      </template>
    </Tree>
  </div>
</template>

<style scoped></style>
