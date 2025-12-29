<script setup lang="ts">
import type { AggregatedPersonScores } from '@/features/cup/model/aggregated_person_scores'
import type { CupStatistics } from '@/features/cup/model/cup_statistics'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'
import type { Person } from '@/features/person/model/person'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
    cupName: string
    eventRacesCupScores: EventRacesCupScore[]
    overallScores: OrganisationScore[]
    aggregatedPersonScores: AggregatedPersonScores[]
    persons: Record<number, Person>
    cupStatistics: CupStatistics
}>()
const { t } = useI18n()
const showHelpDialog = ref(false)

function person(personId: number): string {
    const person = props.persons?.[personId]
    if (person && person.givenName && person.familyName) {
        return `${person.givenName} ${person.familyName}`
    }
    return ''
}

function calculateRanks(scores: PersonWithScore[]): { pws: PersonWithScore, rank: number }[] {
    const sortedScores = [...scores].sort((a, b) => {
        if (b.score !== a.score) {
            return b.score - a.score
        }
        return person(a.personId).localeCompare(person(b.personId))
    })

    let rank = 0
    let previousScore: number | undefined
    let previousRank = 0

    return sortedScores.map((pws, index) => {
        if (pws.score !== previousScore) {
            rank = index + 1
            previousRank = rank
        }
        else {
            rank = previousRank
        }
        previousScore = pws.score

        return { pws, rank }
    })
}

const allEvents = computed(() => {
    return props.eventRacesCupScores
        .map(eventRacesCupScore => eventRacesCupScore.event)
        .sort((a, b) => {
            const dateA = a.startTime
                ? a.startTime instanceof Date
                    ? a.startTime.getTime()
                    : new Date(a.startTime).getTime()
                : 0
            const dateB = b.startTime
                ? b.startTime instanceof Date
                    ? b.startTime.getTime()
                    : new Date(b.startTime).getTime()
                : 0
            return dateA - dateB
        })
})

function findScoreForEventAndClassResultAndPerson(
    classShortName: string,
    personId: number,
    index: number,
) {
    const eventScore = props.eventRacesCupScores.find(
        e => e.event.id === allEvents.value[index].id,
    )
    return eventScore?.raceClassResultGroupedCupScores
        ?.flatMap(x => x.classResultScores || [])
        .find(it => it.classResultShortName === classShortName)
        ?.personWithScores
        ?.find(it => it.personId === personId)
        ?.score
}

function getRankBadgeClass(rank: number): string {
    if (rank === 1)
        return 'bg-gradient-to-br from-yellow-400 to-yellow-600 text-white dark:from-yellow-500 dark:to-yellow-700'
    if (rank === 2)
        return 'bg-gradient-to-br from-gray-300 to-gray-500 text-white dark:from-gray-400 dark:to-gray-600'
    if (rank === 3)
        return 'bg-gradient-to-br from-orange-400 to-orange-600 text-white dark:from-orange-500 dark:to-orange-700'
    return 'bg-adaptive-tertiary text-adaptive'
}

function formatPercentage(ratio: number): string {
    return `${(ratio * 100).toFixed(1)} %`
}

function formatDecimal(value: number): string {
    return value.toFixed(2)
}
</script>

<template>
    <div class="max-w-7xl mx-auto px-3 py-3 sm:px-4 lg:px-6">
        <!-- Header Section -->
        <div class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
            <div class="bg-gradient-to-r from-zinc-600 to-zinc-700 px-4 py-3">
                <h1 class="text-xl font-bold text-white">
                    {{ cupName }} - {{ t('cupStatistics.header.overall') }}
                </h1>
            </div>
        </div>

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

        <!-- Events List Section -->
        <div v-if="allEvents.length" class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
            <div class="px-4 py-2 border-b border-adaptive bg-adaptive-secondary">
                <h2 class="text-base font-semibold text-adaptive">
                    {{ t('cupStatistics.eventsList.title') }}
                </h2>
            </div>
            <div class="overflow-x-auto">
                <table class="min-w-full text-sm">
                    <thead class="bg-adaptive-secondary border-b-2 border-adaptive-strong">
                        <tr>
                            <th scope="col" class="px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider w-16">
                                {{ t('cupStatistics.eventsList.event') }}
                            </th>
                            <th scope="col" class="px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider">
                                {{ t('cupStatistics.eventsList.name') }}
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-adaptive">
                        <tr v-for="(event, index) in allEvents" :key="event.id" class="hover:bg-adaptive-tertiary transition-colors duration-150 border-b border-adaptive" :class="{ 'bg-adaptive-secondary': index % 2 === 1 }">
                            <td class="px-3 py-1.5 whitespace-nowrap">
                                <span class="inline-flex items-center justify-center w-8 h-8 flex-shrink-0 rounded-full bg-zinc-badge text-zinc-badge font-semibold text-xs">
                                    {{ index + 1 }}
                                </span>
                            </td>
                            <td class="px-3 py-1.5 text-sm text-adaptive-secondary">
                                {{ event.name }}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- No Data State -->
        <div v-if="!aggregatedPersonScores.length" class="bg-adaptive rounded shadow-sm border border-adaptive overflow-hidden">
            <div class="px-4 py-8 text-center">
                <div class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-adaptive-tertiary mb-3">
                    <svg class="w-6 h-6 text-adaptive-tertiary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                </div>
                <p class="text-adaptive-tertiary text-sm">
                    {{ t('cupStatistics.resultsTable.noPoints') }}
                </p>
            </div>
        </div>

        <!-- Results by Class Section -->
        <div v-if="aggregatedPersonScores.length" class="space-y-3">
            <div
                v-for="entry in aggregatedPersonScores"
                :key="entry.classResultShortName"
                class="bg-adaptive rounded shadow-sm border border-adaptive overflow-hidden transition-all duration-200 hover:shadow-md"
            >
                <div class="px-4 py-2 bg-zinc-highlight border-b border-adaptive">
                    <h3 class="text-base font-bold text-adaptive">
                        {{ entry.classResultShortName }}
                    </h3>
                </div>

                <div class="overflow-x-auto">
                    <table class="min-w-full text-sm">
                        <thead class="bg-adaptive-secondary border-b-2 border-adaptive-strong">
                            <tr>
                                <th scope="col" class="px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider w-16">
                                    {{ t('cupStatistics.resultsTable.position') }}
                                </th>
                                <th scope="col" class="px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider">
                                    {{ t('cupStatistics.resultsTable.name') }}
                                </th>
                                <th scope="col" class="px-3 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider bg-zinc-highlight border-x-2 border-adaptive-strong w-20">
                                    {{ t('cupStatistics.resultsTable.total') }}
                                </th>
                                <th
                                    v-for="(event, index) in allEvents"
                                    :key="event.id"
                                    scope="col"
                                    class="px-2 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider w-14"
                                >
                                    {{ index + 1 }}
                                </th>
                            </tr>
                        </thead>
                        <tbody class="bg-adaptive">
                            <tr
                                v-for="({ pws, rank }, index) in calculateRanks(
                                    entry.personWithScoreList.filter((o) => o.score > 0),
                                )"
                                :key="pws.personId"
                                class="hover:bg-adaptive-tertiary transition-colors duration-150 border-b border-adaptive"
                                :class="{ 'bg-adaptive-secondary': index % 2 === 1 }"
                            >
                                <td class="px-3 py-1.5 whitespace-nowrap">
                                    <div v-if="rank <= 3" class="flex items-center justify-center">
                                        <img
                                            v-if="rank === 1"
                                            src="@/assets/medal-gold.svg"
                                            alt="Erster Platz"
                                            class="w-14 h-14 object-contain"
                                        >
                                        <img
                                            v-else-if="rank === 2"
                                            src="@/assets/medal-silver.svg"
                                            alt="Zweiter Platz"
                                            class="w-14 h-14 object-contain"
                                        >
                                        <img
                                            v-else-if="rank === 3"
                                            src="@/assets/medal-bronze.svg"
                                            alt="Dritter Platz"
                                            class="w-14 h-14 object-contain"
                                        >
                                    </div>
                                    <div v-else class="flex items-center justify-center">
                                        <span class="inline-flex items-center justify-center w-8 h-8 flex-shrink-0 rounded-full text-xs font-semibold" :class="getRankBadgeClass(rank)">
                                            {{ rank }}
                                        </span>
                                    </div>
                                </td>
                                <td class="px-3 py-1.5">
                                    <div class="text-sm font-semibold text-adaptive">
                                        {{ person(pws.personId) }}
                                    </div>
                                </td>
                                <td class="px-3 py-1.5 text-center bg-zinc-highlight border-x-2 border-adaptive-strong">
                                    <span class="inline-flex items-center px-2 py-0.5 rounded-full text-sm font-bold bg-zinc-badge text-zinc-badge">
                                        {{ pws.score }}
                                    </span>
                                </td>
                                <td
                                    v-for="(event, idx) in allEvents"
                                    :key="event.id"
                                    class="px-2 py-1.5 text-center text-xs"
                                >
                                    <span v-if="findScoreForEventAndClassResultAndPerson(pws.classShortName, pws.personId, idx)" class="text-adaptive font-semibold">
                                        {{
                                            findScoreForEventAndClassResultAndPerson(pws.classShortName, pws.personId, idx)
                                        }}
                                    </span>
                                    <span v-else class="text-adaptive-tertiary">-</span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
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
    </div>
</template>
