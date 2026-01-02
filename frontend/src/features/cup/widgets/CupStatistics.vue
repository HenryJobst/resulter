<script setup lang="ts">
import type { CupStatistics } from '@/features/cup/model/cup_statistics'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

defineProps<{
    cupStatistics: CupStatistics
}>()

const { t } = useI18n()
const showHelpDialog = ref(false)

function formatPercentage(ratio: number): string {
    return `${(ratio * 100).toFixed(1)} %`
}

function formatDecimal(value: number): string {
    return value.toFixed(2)
}
</script>

<template>
    <!-- Overall Statistics Section -->
    <div v-if="cupStatistics" class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
        <div class="px-4 py-2 border-b border-adaptive bg-adaptive-secondary flex justify-between items-center">
            <h2 class="text-base font-semibold text-adaptive">
                {{ t('cupStatistics.overallStatistics.title') }}
            </h2>
            <Button
                icon="pi pi-question-circle"
                :label="t('labels.help')"
                outlined
                size="small"
                @click="showHelpDialog = true"
            />
        </div>
        <div class="px-4 py-3 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            <!-- Anzahl der Läufer -->
            <div class="bg-adaptive-secondary rounded-lg px-3 py-2 border border-adaptive">
                <div class="text-xs text-adaptive-tertiary mb-1">
                    {{ t('cupStatistics.overallStatistics.totalRunners') }}
                </div>
                <div class="text-lg font-bold text-adaptive">
                    {{ cupStatistics.overallStatistics.totalRunners }}
                </div>
            </div>

            <!-- Anzahl der Vereine -->
            <div class="bg-adaptive-secondary rounded-lg px-3 py-2 border border-adaptive">
                <div class="text-xs text-adaptive-tertiary mb-1">
                    {{ t('cupStatistics.overallStatistics.totalOrganisations') }}
                </div>
                <div class="text-lg font-bold text-adaptive">
                    {{ cupStatistics.overallStatistics.totalOrganisations }}
                </div>
            </div>

            <!-- Gesamtstarts -->
            <div class="bg-adaptive-secondary rounded-lg px-3 py-2 border border-adaptive">
                <div class="text-xs text-adaptive-tertiary mb-1">
                    {{ t('cupStatistics.overallStatistics.totalStarts') }}
                </div>
                <div class="text-lg font-bold text-adaptive">
                    {{ cupStatistics.overallStatistics.totalStarts }}
                    <span class="text-sm text-adaptive-tertiary ml-1">
                        {{ t('cupStatistics.overallStatistics.nonScoringStarts', {
                            count: cupStatistics.overallStatistics.totalNonScoringStarts,
                            percentage: formatPercentage(cupStatistics.overallStatistics.totalNonScoringStarts / cupStatistics.overallStatistics.totalStarts),
                        }) }}
                    </span>
                </div>
            </div>

            <!-- Läufer pro Verein -->
            <div class="bg-adaptive-secondary rounded-lg px-3 py-2 border border-adaptive">
                <div class="text-xs text-adaptive-tertiary mb-1">
                    {{ t('cupStatistics.overallStatistics.runnersPerOrganisation') }}
                </div>
                <div class="text-lg font-bold text-adaptive">
                    {{ formatDecimal(cupStatistics.overallStatistics.runnersPerOrganisation) }}
                </div>
            </div>

            <!-- Starts pro Verein -->
            <div class="bg-adaptive-secondary rounded-lg px-3 py-2 border border-adaptive">
                <div class="text-xs text-adaptive-tertiary mb-1">
                    {{ t('cupStatistics.overallStatistics.startsPerOrganisation') }}
                </div>
                <div class="text-lg font-bold text-adaptive">
                    {{ formatDecimal(cupStatistics.overallStatistics.startsPerOrganisation) }}
                    <span class="text-sm text-adaptive-tertiary ml-1">
                        {{ t('cupStatistics.overallStatistics.nonScoringAverage', {
                            average: formatDecimal(cupStatistics.overallStatistics.nonScoringStartsPerOrganisation),
                        }) }}
                    </span>
                </div>
            </div>

            <!-- Starts pro Läufer -->
            <div class="bg-adaptive-secondary rounded-lg px-3 py-2 border border-adaptive">
                <div class="text-xs text-adaptive-tertiary mb-1">
                    {{ t('cupStatistics.overallStatistics.startsPerRunner') }}
                </div>
                <div class="text-lg font-bold text-adaptive">
                    {{ formatDecimal(cupStatistics.overallStatistics.startsPerRunner) }}
                    <span class="text-sm text-adaptive-tertiary ml-1">
                        {{ t('cupStatistics.overallStatistics.nonScoringAverage', {
                            average: formatDecimal(cupStatistics.overallStatistics.nonScoringStartsPerRunner),
                        }) }}
                    </span>
                </div>
            </div>
        </div>
    </div>

    <!-- Per-Organization Statistics Table -->
    <div v-if="cupStatistics?.organisationStatistics?.length" class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
        <div class="px-4 py-2 border-b border-adaptive bg-adaptive-secondary">
            <h2 class="text-base font-semibold text-adaptive">
                {{ t('cupStatistics.organisationTable.title') }}
            </h2>
        </div>
        <div class="overflow-x-auto">
            <table class="min-w-full text-sm">
                <thead class="bg-adaptive-secondary border-b-2 border-adaptive-strong">
                    <tr>
                        <th scope="col" class="px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider">
                            {{ t('cupStatistics.organisationTable.organisation') }}
                        </th>
                        <th scope="col" class="px-3 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider w-20">
                            {{ t('cupStatistics.organisationTable.runners') }}
                        </th>
                        <th scope="col" class="px-3 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider w-20">
                            {{ t('cupStatistics.organisationTable.starts') }}
                        </th>
                        <th scope="col" class="px-3 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider w-20">
                            {{ t('cupStatistics.organisationTable.nonScoring') }}
                        </th>
                        <th scope="col" class="px-3 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider w-24">
                            {{ t('cupStatistics.organisationTable.startsPerRunner') }}
                        </th>
                        <th scope="col" class="px-3 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider w-24">
                            {{ t('cupStatistics.organisationTable.nonScoringPerRunner') }}
                        </th>
                        <th scope="col" class="px-3 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider w-24">
                            {{ t('cupStatistics.organisationTable.nonScoringRatio') }}
                        </th>
                    </tr>
                </thead>
                <tbody class="bg-adaptive">
                    <tr
                        v-for="(orgStat, index) in cupStatistics.organisationStatistics"
                        :key="orgStat.organisation.id"
                        class="hover:bg-adaptive-tertiary transition-colors duration-150 border-b border-adaptive"
                        :class="{ 'bg-adaptive-secondary': index % 2 === 1 }"
                    >
                        <td class="px-3 py-1.5 text-sm text-adaptive font-medium">
                            {{ orgStat.organisation.name }}
                        </td>
                        <td class="px-3 py-1.5 text-center text-sm text-adaptive">
                            {{ orgStat.runnerCount }}
                        </td>
                        <td class="px-3 py-1.5 text-center text-sm text-adaptive">
                            {{ orgStat.totalStarts }}
                        </td>
                        <td class="px-3 py-1.5 text-center text-sm text-adaptive">
                            {{ orgStat.nonScoringStarts }}
                        </td>
                        <td class="px-3 py-1.5 text-center text-sm text-adaptive">
                            {{ formatDecimal(orgStat.startsPerRunner) }}
                        </td>
                        <td class="px-3 py-1.5 text-center text-sm text-adaptive">
                            {{ formatDecimal(orgStat.nonScoringStartsPerRunner) }}
                        </td>
                        <td class="px-3 py-1.5 text-center text-sm text-adaptive">
                            {{ formatPercentage(orgStat.nonScoringRatio) }}
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Help Dialog -->
    <Dialog
        v-model:visible="showHelpDialog"
        :header="t('cupStatistics.help.title')"
        :style="{ width: '50rem' }"
        :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
        modal
    >
        <div class="space-y-4">
            <!-- Overview -->
            <div>
                <h3 class="text-lg font-semibold mb-2">
                    {{ t('cupStatistics.help.overview.heading') }}
                </h3>
                <p class="text-sm">
                    {{ t('cupStatistics.help.overview.text') }}
                </p>
            </div>

            <!-- Overall Statistics -->
            <div>
                <h3 class="text-lg font-semibold mb-2">
                    {{ t('cupStatistics.help.overallStats.heading') }}
                </h3>
                <div class="space-y-2 text-sm">
                    <div>
                        <strong>{{ t('cupStatistics.overallStatistics.totalRunners') }}:</strong> {{ t('cupStatistics.help.overallStats.totalRunners') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.overallStatistics.totalOrganisations') }}:</strong> {{ t('cupStatistics.help.overallStats.totalOrganisations') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.overallStatistics.totalStarts') }}:</strong> {{ t('cupStatistics.help.overallStats.totalStarts') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.overallStatistics.runnersPerOrganisation') }}:</strong> {{ t('cupStatistics.help.overallStats.runnersPerOrganisation') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.overallStatistics.startsPerOrganisation') }}:</strong> {{ t('cupStatistics.help.overallStats.startsPerOrganisation') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.overallStatistics.startsPerRunner') }}:</strong> {{ t('cupStatistics.help.overallStats.startsPerRunner') }}
                    </div>
                </div>
            </div>

            <!-- Per-Organization Table -->
            <div>
                <h3 class="text-lg font-semibold mb-2">
                    {{ t('cupStatistics.help.organisationStats.heading') }}
                </h3>
                <p class="text-sm mb-2">
                    {{ t('cupStatistics.help.organisationStats.intro') }}
                </p>
                <div class="space-y-2 text-sm">
                    <div>
                        <strong>{{ t('cupStatistics.organisationTable.organisation') }}:</strong> {{ t('cupStatistics.help.organisationStats.organisation') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.organisationTable.runners') }}:</strong> {{ t('cupStatistics.help.organisationStats.runners') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.organisationTable.starts') }}:</strong> {{ t('cupStatistics.help.organisationStats.starts') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.organisationTable.nonScoring') }}:</strong> {{ t('cupStatistics.help.organisationStats.nonScoring') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.organisationTable.startsPerRunner') }}:</strong> {{ t('cupStatistics.help.organisationStats.startsPerRunner') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.organisationTable.nonScoringPerRunner') }}:</strong> {{ t('cupStatistics.help.organisationStats.nonScoringPerRunner') }}
                    </div>
                    <div>
                        <strong>{{ t('cupStatistics.organisationTable.nonScoringRatio') }}:</strong> {{ t('cupStatistics.help.organisationStats.nonScoringRatio') }}
                    </div>
                </div>
            </div>

            <!-- What is "ohne Wertung"? -->
            <div>
                <h3 class="text-lg font-semibold mb-2">
                    {{ t('cupStatistics.help.nonScoringExplanation.heading') }}
                </h3>
                <p class="text-sm mb-2">
                    {{ t('cupStatistics.help.nonScoringExplanation.intro') }}
                </p>
                <ul class="list-disc ml-6 space-y-1 text-sm">
                    <li><strong>DNF (Did Not Finish):</strong> {{ t('cupStatistics.help.nonScoringExplanation.dnf') }}</li>
                    <li><strong>{{ t('result_state.Disqualified') }}:</strong> {{ t('cupStatistics.help.nonScoringExplanation.disqualified') }}</li>
                    <li><strong>{{ t('result_state.MissingPunch') }}:</strong> {{ t('cupStatistics.help.nonScoringExplanation.missingPunch') }}</li>
                    <li><strong>{{ t('result_state.OverTime') }}:</strong> {{ t('cupStatistics.help.nonScoringExplanation.overTime') }}</li>
                    <li><strong>DNS (Did Not Start):</strong> {{ t('cupStatistics.help.nonScoringExplanation.dns') }}</li>
                    <li><strong>{{ t('result_state.NotCompeting') }}:</strong> {{ t('cupStatistics.help.nonScoringExplanation.notCompeting') }}</li>
                </ul>
                <p class="text-sm mt-2">
                    {{ t('cupStatistics.help.nonScoringExplanation.note') }}
                </p>
            </div>

            <!-- Calculation Notes -->
            <div class="bg-blue-50 dark:bg-blue-900/20 p-3 rounded">
                <h4 class="font-semibold text-sm mb-1 text-blue-900 dark:text-blue-100">
                    {{ t('cupStatistics.help.calculationNote.title') }}
                </h4>
                <p class="text-xs text-blue-800 dark:text-blue-200">
                    {{ t('cupStatistics.help.calculationNote.text') }}
                </p>
            </div>
        </div>
    </Dialog>
</template>
