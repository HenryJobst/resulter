<script setup lang="ts">
import type { DataTablePageEvent, DataTableSortEvent } from 'primevue/datatable'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { RestPageResult } from '@/features/generic/models/rest_page_result'
import type { TableSettings } from '@/features/generic/models/table_settings'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { useDebounceFn } from '@vueuse/core'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import { useToast } from 'primevue/usetoast'
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatDate, formatTime, formatYear } from '@/features/generic/services/GenericFunctions'
import { settingsStoreFactory } from '@/features/generic/stores/settings.store'
import { toastDisplayDuration } from '@/utils/constants'
import { getValueByPath, truncateString } from '@/utils/tools'

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

const useSettingsStore = settingsStoreFactory(props.settingsStoreSuffix, props.initialTableSettings)
const settingsStore = useSettingsStore()

onMounted(() => {
    props.columns?.forEach((col) => {
        const filters = settingsStore.settings.filters
        if (col.filterable && filters) {
            if (filters && !(col.field in filters)) {
                filters[col.field] = {
                    value: null,
                    matchMode: col.filterMatchMode || 'contains',
                }
            }
        }
    })
})

const queryClient = useQueryClient()

const queryKeys = computed(() => {
    return [
        props.queryKey,
        // TODO: hier und bei allen Queries muss wahrscheinlich der aktuelle DB-Identifier mit rein, oder?
        settingsStore.settings.page,
        settingsStore.settings.rows,
        settingsStore.settings.multiSortMeta,
        settingsStore.settings.sortMode,
        settingsStore.settings.sortOrder,
        settingsStore.settings.nullSortOrder,
        settingsStore.settings.defaultSortOrder,
        settingsStore.settings.filters,
        settingsStore.settings.removableSort,
        settingsStore.settings.rowHover,
        settingsStore.settings.stateStorage,
        settingsStore.settings.stateKey,
        settingsStore.settings.scrollable,
        settingsStore.settings.stripedRows,
    ]
})

const entityQuery = useQuery({
    queryKey: queryKeys,
    queryFn: () => {
        return props.entityService?.getAll(t, settingsStore.settings)
    },
})

const dataValue = computed((): RestPageResult<any> | undefined => {
    if (entityQuery.data && entityQuery.data.value) {
        return entityQuery.data.value as unknown as RestPageResult<any>
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

function pageChanged(e: DataTablePageEvent) {
    /* console.log(`Page: ${e.page}`)
    console.log(`Rows: ${e.rows}`)
    console.log(`Multi-Sort-Meta: ${prettyPrint(e.multiSortMeta)}`)
    console.log(`Pageable: ${prettyPrint(settingsStore.settings.paginator)}`)
    console.log(`Paginator position: ${prettyPrint(settingsStore.settings.paginatorPosition)}`)
    */
    settingsStore.settings.first = e.first
    settingsStore.setPage(e.page)
    settingsStore.settings.rows = e.rows
    settingsStore.settings.multiSortMeta = e.multiSortMeta
}

function sortChanged(e: DataTableSortEvent) {
    /*
    console.log(`Rows: ${prettyPrint(e.rows)}`)
    console.log(`Multi-Sort-Meta: ${prettyPrint(e.multiSortMeta)}`)
    console.log(`Pageable: ${prettyPrint(settingsStore.settings.paginator)}`)
    console.log(`Paginator position: ${prettyPrint(settingsStore.settings.paginatorPosition)}`)
     */
    settingsStore.settings.first = e.first
    settingsStore.settings.page = e.first / e.rows
    settingsStore.settings.rows = e.rows
    settingsStore.settings.multiSortMeta = e.multiSortMeta
    settingsStore.settings.sortField = e.sortField
    settingsStore.settings.sortOrder = e.sortOrder
}

function getSortable(col: GenericListColumn) {
    // console.log('Get sortable for ' + col.field + ':' + col.sortable)
    return col?.sortable ?? false
}

const FILTER_INPUT_DELAY = 800
const debouncedFilterInput = useDebounceFn((_filterModel: any, filterCallback: () => void) => {
    filterCallback()
}, FILTER_INPUT_DELAY)
</script>

<template>
    <div v-if="props.visible">
        <h1>{{ props.listLabel }}</h1>
        <div class="flex justify-content-between my-2">
            <div v-if="props.newEnabled" class="flex justify-content-start">
                <router-link v-if="changeable" :to="{ name: `${props.routerPrefix}-new` }">
                    <Button
                        v-tooltip.right="t('labels.new')"
                        icon="pi pi-plus"
                        :aria-label="t('labels.new')"
                        outlined
                        raised
                        rounded
                    />
                </router-link>
                <slot name="extra_list_actions" />
            </div>
            <Button
                v-tooltip.left="t('labels.reload')"
                icon="pi pi-refresh"
                :aria-label="t('labels.reload')"
                outlined
                raised
                rounded
                severity="secondary"
                @click="reload"
            />
        </div>
        <div class="card">
            <DataTable
                v-if="props.visible"
                v-model:filters="settingsStore.settings.filters"
                :loading="entityQuery.status.value === 'pending' || deleteMutation.status.value === 'pending' || (dataValue?.content === undefined)"
                :value="dataValue?.content"
                :paginator="settingsStore.settings.paginator"
                :always-show-paginator="false"
                :rows-per-page-options="settingsStore.settings.rowsPerPageOptions"
                :paginator-position="settingsStore.settings.paginatorPosition"
                :first="settingsStore.settings.first"
                :rows="settingsStore.settings.rows"
                :total-records="dataValue?.page.totalElements"
                :sort-mode="settingsStore.settings.sortMode"
                :multi-sort-meta="settingsStore.settings.multiSortMeta"
                :sort-field="settingsStore.settings.sortField"
                :sort-order="settingsStore.settings.sortOrder ?? undefined"
                :null-sort-order="settingsStore.settings.nullSortOrder"
                :default-sort-order="settingsStore.settings.defaultSortOrder"
                :removable-sort="settingsStore.settings.removableSort"
                :row-hover="settingsStore.settings.rowHover"
                :state-storage="settingsStore.settings.stateStorage"
                :state-key="settingsStore.settings.stateKey"
                :scrollable="settingsStore.settings.scrollable"
                :striped-rows="settingsStore.settings.stripedRows"
                :filter-display="props.filterDisplay"
                :lazy="true"
                size="small"
                class="p-datatable-sm"
                @page="pageChanged"
                @sort="sortChanged"
            >
                <!-- ... Action columns ... -->
                <Column class="text-left" :header="t('labels.action', 2)">
                    <template #body="{ data }">
                        <div class="flex items-center">
                            <slot name="extra_row_actions" :value="data" />
                            <router-link
                                v-if="props.editEnabled && changeable"
                                :to="{
                                    name: `${props.routerPrefix}-edit`,
                                    params: { id: data.id },
                                }"
                            >
                                <Button
                                    v-if="props.editEnabled && changeable"
                                    v-tooltip="t('labels.edit')"
                                    icon="pi pi-pencil"
                                    class="mr-2"
                                    :aria-label="t('labels.edit')"
                                    outlined
                                    raised
                                    rounded
                                />
                            </router-link>
                            <Button
                                v-if="props.deleteEnabled && changeable"
                                v-tooltip="t('labels.delete')"
                                icon="pi pi-trash"
                                class="mr-2"
                                severity="danger"
                                outlined
                                raised
                                rounded
                                :aria-label="t('labels.delete')"
                                @click="deleteEntity(data.id)"
                            />
                        </div>
                    </template>
                </Column>
                <!-- Add Columns Here -->
                <Column
                    v-for="col in props.columns"
                    :key="col.field"
                    :field="col.field"
                    :header="col.label_count ? t(col.label, col.label_count) : t(col.label)"
                    :sortable="getSortable(col)"
                    :class="col.class || ''"
                    :style="col.style || ''"
                >
                    <template v-if="col.type === 'list'" #body="slotProps">
                        <div
                            v-for="(item, index) in slotProps.data[col.field]"
                            :key="`item-${index}`"
                        >
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
                        {{ formatDate(slotProps.data[col.field], locale) }}
                    </template>
                    <template v-else-if="col.type === 'year'" #body="slotProps">
                        {{ formatYear(slotProps.data[col.field], locale) }}
                    </template>
                    <template v-else-if="col.type === 'time'" #body="slotProps">
                        {{ formatTime(slotProps.data[col.field], locale) }}
                    </template>
                    <template v-else-if="col.type === 'enum'" #body="slotProps">
                        {{
                            t(
                                (enumTypeLabelPrefixes
                                    ? enumTypeLabelPrefixes.get(col.field)
                                    : '') + slotProps.data[col.field].id.toUpperCase(),
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
                        <div v-if="filterModel" class="w-full">
                            <InputText
                                v-model="filterModel.value"
                                type="text"
                                class="p-column-filter w-full"
                                @input="debouncedFilterInput(filterModel, filterCallback)"
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

:deep(.p-column-filter) {
    width: 100% !important;
    box-sizing: border-box;
}
</style>
