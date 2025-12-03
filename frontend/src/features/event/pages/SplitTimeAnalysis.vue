<script lang="ts" setup>
import { useQuery } from '@tanstack/vue-query'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { EventService } from '@/features/event/services/event.service'

const props = defineProps<{
    id: string
    resultListId: string
}>()

const { t } = useI18n()
const router = useRouter()

const mergeBidirectional = ref(false)
const filterNames = ref<string[]>([])
const filterInput = ref('')

const splitTimeQueryRanking = useQuery({
    queryKey: ['splitTimeAnalysisRanking', props.resultListId, mergeBidirectional, filterNames],
    queryFn: () => EventService.getSplitTimeAnalysisRanking(
        Number.parseInt(props.resultListId),
        mergeBidirectional.value,
        filterNames.value,
        t,
    ),
})

function addNameFilter() {
    if (filterInput.value.trim()) {
        filterNames.value = [...filterNames.value, filterInput.value.trim()]
        filterInput.value = ''
    }
}

function removeNameFilter(name: string) {
    filterNames.value = filterNames.value.filter(n => n !== name)
}

function navigateBack() {
    router.back()
}
</script>

<template>
    <div class="split-time-analysis">
        <Button
            v-tooltip="t('labels.back')"
            icon="pi pi-arrow-left"
            :label="t('labels.back')"
            class="mb-3"
            :aria-label="t('labels.back')"
            severity="secondary"
            outlined
            raised
            rounded
            @click="navigateBack"
        />

        <h1 class="mt-3 font-extrabold">
            {{ t('labels.split_time_analysis') }}
        </h1>

        <div class="controls mb-3 card">
            <div class="field-checkbox mb-2">
                <Checkbox
                    v-model="mergeBidirectional"
                    input-id="merge"
                    :binary="true"
                />
                <label for="merge" class="ml-2">{{ t('labels.merge_bidirectional') }}</label>
            </div>

            <div class="filter-section">
                <InputText
                    v-model="filterInput"
                    :placeholder="t('labels.filter_by_name')"
                    class="mr-2"
                    @keyup.enter="addNameFilter"
                />
                <Button
                    icon="pi pi-plus"
                    :label="t('labels.add_filter')"
                    @click="addNameFilter"
                />
            </div>

            <div v-if="filterNames.length > 0" class="active-filters mt-2">
                <span
                    v-for="name in filterNames"
                    :key="name"
                    class="filter-chip"
                >
                    {{ name }}
                    <Button
                        icon="pi pi-times"
                        class="p-button-text p-button-sm ml-1"
                        text
                        rounded
                        @click="removeNameFilter(name)"
                    />
                </span>
            </div>
        </div>

        <div v-if="splitTimeQueryRanking.isLoading.value" class="card">
            {{ t('messages.loading') }}
        </div>

        <div v-else-if="splitTimeQueryRanking.data.value" class="card">
            <Accordion v-if="splitTimeQueryRanking.data.value.controlSegments.length > 0" :multiple="true">
                <AccordionTab
                    v-for="segment in splitTimeQueryRanking.data.value.controlSegments"
                    :key="segment.segmentLabel"
                    :header="`${segment.segmentLabel} (${segment.runnerSplits.length} ${t('labels.runners')})`"
                >
                    <DataTable
                        :value="segment.runnerSplits"
                        striped-rows
                        :rows="50"
                        :paginator="segment.runnerSplits.length > 50"
                        responsive-layout="scroll"
                    >
                        <Column field="position" :header="t('labels.position')" style="width: 10%" />
                        <Column field="personName" :header="t('labels.name')" style="width: 40%" />
                        <Column field="splitTime" :header="t('labels.split_time')" style="width: 25%" />
                        <Column field="timeBehind" :header="t('labels.time_behind')" style="width: 25%" />
                    </DataTable>
                </AccordionTab>
            </Accordion>
            <div v-else class="p-4">
                {{ t('messages.no_split_times') }}
            </div>
        </div>

        <div v-else class="card">
            {{ t('messages.no_data') }}
        </div>
    </div>
</template>

<style scoped>
.split-time-analysis {
    padding: 1rem;
}

.controls {
    padding: 1rem;
}

.filter-section {
    display: flex;
    gap: 0.5rem;
    align-items: center;
}

.active-filters {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.filter-chip {
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
    padding: 0.25rem 0.5rem;
    background: var(--primary-color);
    color: white;
    border-radius: 1rem;
    font-size: 0.875rem;
}
</style>
