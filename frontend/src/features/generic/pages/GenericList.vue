<script setup lang="ts">
import { computed } from 'vue'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import DataTable, { type DataTablePageEvent, type DataTableSortEvent } from 'primevue/datatable'
import Column from 'primevue/column'
import Spinner from '@/components/SpinnerComponent.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { toastDisplayDuration } from '@/utils/constants'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import { settingsStoreFactory } from '@/features/generic/stores/settings.store'
import type { RestResult } from '@/features/generic/models/rest_result'

const props = defineProps({
  entityService: Object as () => IGenericService<any>,
  settingsStoreSuffix: {
    type: String,
    required: true
  },
  queryKey: {
    type: String,
    required: true
  },
  listLabel: String,
  entityLabel: String,
  routerPrefix: String,
  columns: Array as () => GenericListColumn[],
  changeable: Boolean,
  enumTypeLabelPrefixes: Map<string, string>
})

const { t, locale } = useI18n()

const queryClient = useQueryClient()

const useSettingsStore = settingsStoreFactory(props.settingsStoreSuffix)
const settingsStore = useSettingsStore()

const queryKeys = computed(() => {
  console.log('Calculate query keys ...')
  return [
    props.queryKey,
    settingsStore.settings.page,
    settingsStore.settings.rows,
    settingsStore.settings.multiSortMeta,
    settingsStore.settings.sortMode,
    settingsStore.settings.sortOrder,
    settingsStore.settings.nullSortOrder,
    settingsStore.settings.defaultSortOrder
  ]
})

const entityQuery = useQuery({
  queryKey: queryKeys,
  queryFn: () => {
    console.log('Get all ...')
    return props.entityService?.getAll(t, settingsStore.settings)
  }
})

const dataValue = computed((): RestResult<any> | undefined => {
  if (entityQuery.data && entityQuery.data.value) {
    const value = entityQuery.data.value as unknown as RestResult<any>
    console.log('Total elements: ' + value.totalElements)
    return value
  } else {
    return undefined
  }
})

const toast = useToast()

const deleteMutation = useMutation({
  mutationFn: (id: number) => props.entityService!.deleteById(id, t),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: queryKeys })
    toast.add({
      severity: 'info',
      summary: t('messages.success'),
      detail: t('messages.entity_deleted', { entity: props.entityLabel }),
      life: toastDisplayDuration
    })
  }
})

const deleteEntity = (id: number) => {
  deleteMutation.mutate(id)
}

const reload = () => {
  entityQuery.refetch()
  deleteMutation.reset()
}

const dateOptions: Intl.DateTimeFormatOptions = {
  year: 'numeric',
  month: 'long',
  day: 'numeric'
}

const yearOptions: Intl.DateTimeFormatOptions = {
  year: '2-digit',
  month: undefined,
  day: undefined
}

const timeOptions: Intl.DateTimeFormatOptions = {
  hour: '2-digit',
  minute: '2-digit',
  hour12: false
}

const formatDateFunction = computed(() => {
  return (date: string | Date) => {
    if (!date) return ''
    if (typeof date === 'string') {
      return new Date(date).toLocaleDateString(locale.value, dateOptions)
    }
    return date.toLocaleDateString(locale.value, dateOptions)
  }
})

const formatYearFunction = computed(() => {
  return (date: string | Date) => {
    if (!date) return ''
    if (typeof date === 'string') {
      return new Date(date).toLocaleDateString(locale.value, yearOptions)
    }
    return date.toLocaleDateString(locale.value, yearOptions)
  }
})

const formatTimeFunction = computed(() => {
  return (time: string | Date) => {
    if (!time) return ''
    if (typeof time === 'string') {
      return new Date(time).toLocaleTimeString(locale.value, timeOptions)
    }
    return time.toLocaleTimeString(locale.value, timeOptions)
  }
})

const formatDate = (date: string) => {
  return formatDateFunction.value(date)
}
const formatYear = (date: string) => {
  return formatYearFunction.value(date)
}
const formatTime = (time: string) => {
  return formatTimeFunction.value(time)
}

function pageChanged(e: DataTablePageEvent) {
  console.log('Page: ' + e.page)
  console.log('Rows: ' + e.rows)
  console.log('Multi-Sort-Meta: ' + e.multiSortMeta?.toString())
  console.log('Pageable: ' + settingsStore.settings.paginator)
  console.log('Paginator position: ' + settingsStore.settings.paginatorPosition)
  settingsStore.setPage(e.page)
  settingsStore.settings.rows = e.rows
  settingsStore.settings.multiSortMeta = e.multiSortMeta
}

function sortChanged(e: DataTableSortEvent) {
  console.log('Rows: ' + e.rows)
  console.log('Multi-Sort-Meta: ' + e.multiSortMeta?.toString())
  console.log('Pageable: ' + settingsStore.settings.paginator)
  console.log('Paginator position: ' + settingsStore.settings.paginatorPosition)
  settingsStore.settings.first = e.first
  settingsStore.settings.page = e.first / e.rows
  settingsStore.settings.rows = e.rows
  settingsStore.settings.multiSortMeta = e.multiSortMeta
}
</script>

<template>
  <div>
    <h1>{{ props.listLabel }}</h1>
    <div class="flex justify-content-between my-4">
      <div class="flex justify-content-start">
        <router-link :to="{ name: `${props.routerPrefix}-new` }" v-if="changeable">
          <Button icon="pi pi-plus" :label="t('labels.new')" outlined></Button>
        </router-link>
        <slot name="extra_list_actions" />
      </div>
      <Button
        icon="pi pi-refresh"
        :label="t('labels.reload')"
        outlined
        severity="secondary"
        @click="reload"
      />
    </div>
    <div v-if="entityQuery.status.value === 'pending' || deleteMutation.status.value === 'pending'">
      {{ t('messages.loading') }}
      <Spinner />
    </div>
    <div
      v-else-if="entityQuery.status.value === 'error' || deleteMutation.status.value === 'error'"
    >
      <ErrorMessage :message="t('messages.error', { message: entityQuery.error.value })" />
      <ErrorMessage
        :message="t('messages.error', { message: deleteMutation?.error.value?.message })"
      />
    </div>
    <div v-else-if="entityQuery.data" class="card">
      <DataTable
        :value="dataValue?.content"
        :paginator="settingsStore.settings.paginator"
        :always-show-paginator="false"
        :rows-per-page-options="settingsStore.settings.rowsPerPageOptions"
        :paginator-position="settingsStore.settings.paginatorPosition"
        :first="settingsStore.settings.first"
        :rows="settingsStore.settings.rows"
        :total-records="dataValue?.totalElements"
        :sort-mode="settingsStore.settings.sortMode"
        :multi-sort-meta="settingsStore.settings.multiSortMeta"
        :sort-field="settingsStore.settings.sortField"
        :sort-order="settingsStore.settings.sortOrder"
        :null-sort-order="settingsStore.settings.nullSortOrder"
        :default-sort-order="settingsStore.settings.defaultSortOrder"
        :lazy="true"
        :size="'small'"
        class="p-datatable-sm"
        @page="pageChanged"
        @sort="sortChanged"
      >
        <!-- Add Columns Here -->
        <Column
          v-for="col in props.columns"
          :key="col.label"
          :field="col.field"
          :header="col.label_count ? t(col.label, col.label_count) : t(col.label)"
          :sortable="col.sortable ? col.sortable : true"
        >
          <template v-slot:body="slotProps" v-if="col.type === 'list'">
            <div v-for="(item, index) in slotProps.data[col.field]" :key="`item-${index}`">
              <template v-if="$slots[col.field]">
                <slot :name="col.field" :value="item" />
              </template>
            </div>
          </template>
          <template v-slot:body="slotProps" v-else-if="col.type === 'id'">
            <template v-if="$slots[col.field]">
              <slot :name="col.field" :value="slotProps.data[col.field]" />
            </template>
          </template>
          <template v-slot:body="slotProps" v-else-if="col.type === 'date'">
            {{ formatDate(slotProps.data[col.field]) }}
          </template>
          <template v-slot:body="slotProps" v-else-if="col.type === 'year'">
            {{ formatYear(slotProps.data[col.field]) }}
          </template>
          <template v-slot:body="slotProps" v-else-if="col.type === 'time'">
            {{ formatTime(slotProps.data[col.field]) }}
          </template>
          <template v-slot:body="slotProps" v-else-if="col.type === 'enum'">
            {{
              t(
                (enumTypeLabelPrefixes ? enumTypeLabelPrefixes.get(col.field) : '') +
                  slotProps.data[col.field].id.toUpperCase()
              )
            }}
          </template>
        </Column>
        <!-- ... Other columns ... -->
        <Column class="text-right">
          <template #body="{ data }">
            <slot name="extra_row_actions" :value="data" />
            <router-link :to="{ name: `${props.routerPrefix}-edit`, params: { id: data.id } }">
              <Button
                v-if="changeable"
                icon="pi pi-pencil"
                class="mr-2"
                :label="t('labels.edit')"
                outlined
              />
            </router-link>
            <Button
              v-if="changeable"
              icon="pi pi-trash"
              severity="danger"
              outlined
              :label="t('labels.delete')"
              @click="deleteEntity(data.id)"
            />
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
