<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { CupService } from '@/features/cup/services/cup.service'
import { useI18n } from 'vue-i18n'
import Tree from 'primevue/tree'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import type { ClassResult } from '@/features/event/model/class_result'
import type { TreeNode } from 'primevue/treenode'
import { computed } from 'vue'
import moment from 'moment'

const props = defineProps<{ id: string; locale?: string }>()
const store = useCupStore()
const cup = store.selectCup(+props.id)

const { t } = useI18n()

function parseDurationMoment(durationString: string): moment.Duration {
  return moment.duration(durationString)
}

const formatTime = (time: string): string => {
  return moment.utc(parseDurationMoment(time).asMilliseconds()).format('H:mm:ss')
}

const cupResultsQuery = useQuery({
  queryKey: ['cupResults', props.id],
  queryFn: () => CupService.getResultsById(props.id, t)
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
  if (cupResultsQuery.isFetched) {
    return createTreeNodes(cupResultsQuery.data.value?.classResultDtos)
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
</script>

<template>
  <h2 v-if="cup">{{ cup.name }}</h2>
  <span v-if="cupResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
  <span v-else-if="cupResultsQuery.status.value === 'error'">
    {{ t('messages.error', { message: cupResultsQuery.error.toLocaleString() }) }}
  </span>
  <div v-else-if="cupResultsQuery.data" class="card flex justify-content-start">
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
        </DataTable>
      </template>
    </Tree>
  </div>
</template>

<style scoped></style>
