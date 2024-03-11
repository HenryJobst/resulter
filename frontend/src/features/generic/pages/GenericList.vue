<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import DataTable, {
  type DataTableFilterEvent,
  type DataTablePageEvent,
  type DataTableSortEvent
} from 'primevue/datatable'
import Column from 'primevue/column'
import Spinner from '@/components/SpinnerComponent.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { toastDisplayDuration } from '@/utils/constants'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import { settingsStoreFactory } from '@/features/generic/stores/settings.store'
import type { RestResult } from '@/features/generic/models/rest_result'
import { prettyPrint } from '@base2/pretty-print-object'
import { truncateString } from '../../../utils/tools'

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
  visible: {
    type: Boolean,
    required: false,
    default: false
  },
  newEnabled: {
    type: Boolean,
    required: false,
    default: true
  },
  editEnabled: {
    type: Boolean,
    required: false,
    default: true
  },
  deleteEnabled: {
    type: Boolean,
    required: false,
    default: true
  },
  enumTypeLabelPrefixes: Map<string, string>,
  filterDisplay: {
    type: String,
    required: false
  }
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
    settingsStore.settings.defaultSortOrder,
    settingsStore.settings.filters
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
  console.log('Multi-Sort-Meta: ' + prettyPrint(e.multiSortMeta))
  console.log('Pageable: ' + prettyPrint(settingsStore.settings.paginator))
  console.log('Paginator position: ' + prettyPrint(settingsStore.settings.paginatorPosition))
  settingsStore.setPage(e.page)
  settingsStore.settings.rows = e.rows
  settingsStore.settings.multiSortMeta = e.multiSortMeta
}

function sortChanged(e: DataTableSortEvent) {
  console.log('Rows: ' + prettyPrint(e.rows))
  console.log('Multi-Sort-Meta: ' + prettyPrint(e.multiSortMeta))
  console.log('Pageable: ' + prettyPrint(settingsStore.settings.paginator))
  console.log('Paginator position: ' + prettyPrint(settingsStore.settings.paginatorPosition))
  settingsStore.settings.first = e.first
  settingsStore.settings.page = e.first / e.rows
  settingsStore.settings.rows = e.rows
  settingsStore.settings.multiSortMeta = e.multiSortMeta
}

function filterChanged(e: DataTableFilterEvent) {
  console.log('Filters: ' + prettyPrint(e))
  settingsStore.settings.filters = e.filters
}

function getSortable(col: GenericListColumn) {
  // console.log('Get sortable for ' + col.field + ':' + col.sortable)
  return col.sortable ? col.sortable : true
}

onMounted(() => {
  console.log('Mounted ...')
  props.columns?.forEach((col) => {
    if (col.filterable) {
      //console.log('Add filter for ' + col.field)
      settingsStore.settings.filters[col.field] = {
        value: null,
        matchMode: col.filterMatchMode || 'contains'
      }
    }
  })
  console.log('Filters: ' + prettyPrint(settingsStore.settings.filters))
})

const getSubField = (field: any, subField: any) => {
  return field[subField]
}
</script>

<template>
  <div v-if="props.visible">
    <h1>{{ props.listLabel }}</h1>
    <div class="flex justify-content-between my-4">
      <div v-if="props.newEnabled" class="flex justify-content-start">
        <router-link :to="{ name: `${props.routerPrefix}-new` }" v-if="changeable">
          <Button icon="pi pi-plus" :title="t('labels.new')" outlined></Button>
        </router-link>
        <slot name="extra_list_actions" />
      </div>
      <Button
        icon="pi pi-refresh"
        :title="t('labels.reload')"
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
        v-model:filters="settingsStore.settings.filters"
        :filter-display="props.filterDisplay"
        :lazy="true"
        :size="'small'"
        class="p-datatable-sm"
        @page="pageChanged"
        @sort="sortChanged"
        @filter="filterChanged"
        v-if="props.visible"
      >
        <!-- Add Columns Here -->
        <Column
          v-for="col in props.columns"
          :key="col.field"
          :field="col.field"
          :header="col.label_count ? t(col.label, col.label_count) : t(col.label)"
          :sortable="getSortable(col)"
          :class="col.class || ''"
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
          <template v-slot:body="slotProps" v-else-if="col.type === 'image'">
            <img
              v-if="slotProps.data[col.field]"
              :src="
                'data:image/jpeg;base64,' + getSubField(slotProps.data[col.field], col.subField)
              "
              :alt="t('labels.preview')"
              style="width: 100px"
            />
          </template>
          <template v-slot:body="slotProps" v-else-if="col.type === 'custom'">
            <template v-if="$slots[col.field]">
              <slot :name="col.field" :value="slotProps.data[col.field]" />
            </template>
          </template>
          <template v-slot:body="slotProps" v-else-if="col.type === undefined">
            {{ truncateString(slotProps.data, col.field, col.truncate || 1000) }}
          </template>
          <template #filter="{ filterModel, filterCallback }" v-if="col.filterable">
            <InputText
              v-model="filterModel.value"
              type="text"
              @input="filterCallback()"
              class="p-column-filter"
            />
          </template>
        </Column>
        <!-- ... Other columns ... -->
        <Column class="text-right">
          <template #body="{ data }">
            <div class="w-24">
              <slot name="extra_row_actions" :value="data" />
              <router-link
                v-if="props.editEnabled && changeable"
                :to="{ name: `${props.routerPrefix}-edit`, params: { id: data.id } }"
              >
                <Button
                  v-if="props.editEnabled && changeable"
                  icon="pi pi-pencil"
                  class="mr-2"
                  :title="t('labels.edit')"
                  outlined
                />
              </router-link>
              <Button
                v-if="props.deleteEnabled && changeable"
                icon="pi pi-trash"
                severity="danger"
                outlined
                :title="t('labels.delete')"
                @click="deleteEntity(data.id)"
              />
            </div>
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
