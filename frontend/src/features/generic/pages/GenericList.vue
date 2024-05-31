<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import DataTable, { type DataTableFilterEvent, type DataTablePageEvent, type DataTableSortEvent,
} from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { prettyPrint } from '@base2/pretty-print-object'
import Spinner from '@/components/SpinnerComponent.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import { toastDisplayDuration } from '@/utils/constants'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import { settingsStoreFactory } from '@/features/generic/stores/settings.store'
import type { RestResult } from '@/features/generic/models/rest_result'
import { getValueByPath, truncateString } from '@/utils/tools'
import type { TableSettings } from '@/features/generic/models/table_settings'

const props = defineProps({
    entityService: Object as () => IGenericService<any>,
    settingsStoreSuffix: {
        type: String,
        required: true,
    },
    queryKey: {
        type: String,
        required: true,
    },
    listLabel: String,
    entityLabel: String,
    routerPrefix: String,
    columns: Array as () => GenericListColumn[],
    changeable: Boolean,
    visible: {
        type: Boolean,
        required: false,
        default: false,
    },
    newEnabled: {
        type: Boolean,
        required: false,
        default: true,
    },
    editEnabled: {
        type: Boolean,
        required: false,
        default: true,
    },
    deleteEnabled: {
        type: Boolean,
        required: false,
        default: true,
    },
    enumTypeLabelPrefixes: Map<string, string>,
    filterDisplay: {
        type: String as () => 'menu' | 'row' | undefined,
        required: false,
    },
    initialTableSettings: {
        type: Object as () => TableSettings,
        required: false,
    },
})

const { t, locale } = useI18n()

const queryClient = useQueryClient()

const useSettingsStore = settingsStoreFactory(props.settingsStoreSuffix, props.initialTableSettings)
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
        settingsStore.settings.filters,
    ]
})

const entityQuery = useQuery({
    queryKey: queryKeys,
    queryFn: () => {
        console.log('Get all ...')
        return props.entityService?.getAll(t, settingsStore.settings)
    },
})

const dataValue = computed((): RestResult<any> | undefined => {
    if (entityQuery.data && entityQuery.data.value) {
        const value = entityQuery.data.value as unknown as RestResult<any>
        console.log(`Total elements: ${value.totalElements}`)
        return value
    }
    else {
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
            life: toastDisplayDuration,
        })
    },
})

function deleteEntity(id: number) {
    deleteMutation.mutate(id)
}

function reload() {
    entityQuery.refetch()
    deleteMutation.reset()
}

const dateOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
}

const yearOptions: Intl.DateTimeFormatOptions = {
    year: '2-digit',
    month: undefined,
    day: undefined,
}

const timeOptions: Intl.DateTimeFormatOptions = {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
}

const formatDateFunction = computed(() => {
    return (date: string | Date) => {
        if (!date)
            return ''
        if (typeof date === 'string')
            return new Date(date).toLocaleDateString(locale.value, dateOptions)

        return date.toLocaleDateString(locale.value, dateOptions)
    }
})

const formatYearFunction = computed(() => {
    return (date: string | Date) => {
        if (!date)
            return ''
        if (typeof date === 'string')
            return new Date(date).toLocaleDateString(locale.value, yearOptions)

        return date.toLocaleDateString(locale.value, yearOptions)
    }
})

const formatTimeFunction = computed(() => {
    return (time: string | Date) => {
        if (!time)
            return ''
        if (typeof time === 'string')
            return new Date(time).toLocaleTimeString(locale.value, timeOptions)

        return time.toLocaleTimeString(locale.value, timeOptions)
    }
})

function formatDate(date: string) {
    return formatDateFunction.value(date)
}

function formatYear(date: string) {
    return formatYearFunction.value(date)
}

function formatTime(time: string) {
    return formatTimeFunction.value(time)
}

function pageChanged(e: DataTablePageEvent) {
    console.log(`Page: ${e.page}`)
    console.log(`Rows: ${e.rows}`)
    console.log(`Multi-Sort-Meta: ${prettyPrint(e.multiSortMeta)}`)
    console.log(`Pageable: ${prettyPrint(settingsStore.settings.paginator)}`)
    console.log(`Paginator position: ${prettyPrint(settingsStore.settings.paginatorPosition)}`)
    settingsStore.setPage(e.page)
    settingsStore.settings.rows = e.rows
    settingsStore.settings.multiSortMeta = e.multiSortMeta
}

function sortChanged(e: DataTableSortEvent) {
    console.log(`Rows: ${prettyPrint(e.rows)}`)
    console.log(`Multi-Sort-Meta: ${prettyPrint(e.multiSortMeta)}`)
    console.log(`Pageable: ${prettyPrint(settingsStore.settings.paginator)}`)
    console.log(`Paginator position: ${prettyPrint(settingsStore.settings.paginatorPosition)}`)
    settingsStore.settings.first = e.first
    settingsStore.settings.page = e.first / e.rows
    settingsStore.settings.rows = e.rows
    settingsStore.settings.multiSortMeta = e.multiSortMeta
}

function filterChanged(e: DataTableFilterEvent) {
    console.log(`Filters: ${prettyPrint(e)}`)
    // settingsStore.settings.filters = e.filters
}

function getSortable(col: GenericListColumn) {
    // console.log('Get sortable for ' + col.field + ':' + col.sortable)
    return col.sortable ? col.sortable : true
}

onMounted(() => {
    console.log('Mounted ...')
    props.columns?.forEach((col) => {
        if (col.filterable && settingsStore.settings.filters) {
            // console.log('Add filter for ' + col.field)
            settingsStore.settings.filters[col.field] = {
                value: null,
                matchMode: col.filterMatchMode || 'contains',
            }
        }
    })
    console.log(`Filters: ${prettyPrint(settingsStore.settings.filters)}`)
})

function debounce<T extends (...args: any[]) => any>(fn: T, delay: number): (...args: Parameters<T>) => void {
    let timeoutID: ReturnType<typeof setTimeout> | null = null

    return function (this: any, ...args: Parameters<T>) {
        if (timeoutID !== null)
            clearTimeout(timeoutID)

        timeoutID = setTimeout(() => {
            fn.apply(this, args)
        }, delay)
    }
}

const delay = 800
const debounceFilterChanged = debounce(filterChanged, delay)
const debouncedFilterInput = debounce((filterModel: any, filterCallback: Function) => {
    filterCallback()
}, delay)
</script>

<template>
    <div v-if="props.visible">
        <h1>{{ props.listLabel }}</h1>
        <div class="flex justify-content-between my-4">
            <div v-if="props.newEnabled" class="flex justify-content-start">
                <router-link v-if="changeable" :to="{ name: `${props.routerPrefix}-new` }">
                    <Button icon="pi pi-plus" :title="t('labels.new')" outlined />
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
        <div class="card">
            <DataTable
                v-if="props.visible && dataValue?.content"
                v-model:filters="settingsStore.settings.filters"
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
                :filter-display="props.filterDisplay"
                :lazy="true"
                size="small"
                class="p-datatable-sm"
                @page="pageChanged"
                @sort="sortChanged"
                @filter="debounceFilterChanged"
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
                    <template v-if="col.type === 'list'" #body="slotProps">
                        <div v-for="(item, index) in slotProps.data[col.field]" :key="`item-${index}`">
                            <template v-if="$slots[col.field]">
                                <slot :name="col.field" :value="item" />
                            </template>
                        </div>
                    </template>
                    <template v-else-if="col.type === 'id'" #body="slotProps">
                        <template v-if="$slots[col.field]">
                            <slot :name="col.field" :value="slotProps.data[col.field]" />
                        </template>
                    </template>
                    <template v-else-if="col.type === 'date'" #body="slotProps">
                        {{ formatDate(slotProps.data[col.field]) }}
                    </template>
                    <template v-else-if="col.type === 'year'" #body="slotProps">
                        {{ formatYear(slotProps.data[col.field]) }}
                    </template>
                    <template v-else-if="col.type === 'time'" #body="slotProps">
                        {{ formatTime(slotProps.data[col.field]) }}
                    </template>
                    <template v-else-if="col.type === 'enum'" #body="slotProps">
                        {{
                            t(
                                (enumTypeLabelPrefixes ? enumTypeLabelPrefixes.get(col.field) : '')
                                    + slotProps.data[col.field].id.toUpperCase(),
                            )
                        }}
                    </template>
                    <template v-else-if="col.type === 'image'" #body="slotProps">
                        <img
                            v-if="slotProps.data && col.field"
                            :src="`data:image/jpeg;base64,${getValueByPath(slotProps.data, col.field)}`"
                            :alt="t('labels.preview')"
                            style="width: 100px"
                        >
                    </template>
                    <template v-else-if="col.type === 'custom'" #body="slotProps">
                        <template v-if="$slots[col.field]">
                            <slot :name="col.field" :value="slotProps.data[col.field]" />
                        </template>
                    </template>
                    <template v-else-if="col.type === undefined" #body="slotProps">
                        {{ truncateString(slotProps.data, col.field, col.truncate || 1000) }}
                    </template>
                    <template v-if="col.filterable" #filter="{ filterModel, filterCallback }">
                        <InputText
                            v-model="filterModel.value"
                            type="text"
                            class="p-column-filter"
                            @input="debouncedFilterInput(filterModel, filterCallback)"
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
