<script setup lang="ts">
import type { AggregatedPersonScores } from '@/features/cup/model/aggregated_person_scores'
import type { CupStatistics } from '@/features/cup/model/cup_statistics'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'
import type { Person } from '@/features/person/model/person'
import { useQueries } from '@tanstack/vue-query'
import Panel from 'primevue/panel'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import CupStatisticsWidget from '@/features/cup/widgets/CupStatistics.vue'
import { EventService } from '@/features/event/services/event.service'

const props = defineProps<{
    cupName: string
    eventRacesCupScores: EventRacesCupScore[]
    overallScores: OrganisationScore[]
    aggregatedPersonScores: AggregatedPersonScores[]
    persons: Record<number, Person>
    cupStatistics: CupStatistics
}>()

const { t } = useI18n()
const router = useRouter()

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

// Lade ResultLists für alle Events
const eventResultsQueries = useQueries({
    queries: computed(() => {
        return allEvents.value.map(event => ({
            queryKey: ['eventResults', event.id],
            queryFn: () => {
                return EventService.getResultsById(event.id.toString(), t)
            },
            staleTime: 5 * 60 * 1000,
        }))
    }),
})

// Mapping: Race-ID → ResultList-ID
const raceIdToResultListId = computed(() => {
    const mapping = new Map<number, number>()

    eventResultsQueries.value.forEach((query) => {
        // Bei useQueries ist query.data direkt das Objekt, NICHT query.data.value
        if (query.data?.resultLists) {
            query.data.resultLists.forEach((rl) => {
                if (rl.raceId) {
                    mapping.set(rl.raceId, rl.id)
                }
            })
        }
    })

    return mapping
})

function findScoreForEventAndClassResultAndPerson(
    classShortName: string,
    personId: number,
    index: number,
) {
    const eventScore = props.eventRacesCupScores.find(
        e => e.event.id === allEvents.value[index]?.id,
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

/**
 * Holt die ResultList-ID für ein bestimmtes Event und Klasse
 */
function getResultListIdForEvent(eventIndex: number, classShortName: string): number | undefined {
    const event = allEvents.value[eventIndex]
    if (!event) {
        return undefined
    }

    // Finde passendes EventRacesCupScore
    const eventScore = props.eventRacesCupScores.find(
        e => e.event.id === event.id,
    )

    if (!eventScore) {
        return undefined
    }

    // Suche in raceClassResultGroupedCupScores nach passender Klasse
    for (const raceClassGroup of eventScore.raceClassResultGroupedCupScores) {
        const classResult = raceClassGroup.classResultScores.find(
            cs => cs.classResultShortName === classShortName,
        )

        if (classResult && raceClassGroup.race?.id) {
            const raceId = raceClassGroup.race.id
            const resultListId = raceIdToResultListId.value.get(raceId)
            return resultListId
        }
    }

    return undefined
}

/**
 * Navigiert zu Wettkampfergebnissen mit Deep-Link
 */
function navigateToPersonResult(
    eventId: number,
    resultListId: number | undefined,
    classShortName: string,
    personId: number,
) {
    const query: Record<string, string> = {}

    if (resultListId) {
        query.resultListId = resultListId.toString()
        query.classShortName = classShortName
        query.personId = personId.toString()
    }

    const route = router.resolve({
        name: 'event-results',
        params: { id: eventId.toString() },
        query,
    })

    window.open(route.href, '_blank')
}
</script>

<template>
    <div class="max-w-7xl mx-auto px-3 py-3 sm:px-4 lg:px-6">
        <!-- Header Section -->
        <div class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
            <div class="bg-linear-to-r from-zinc-600 to-zinc-700 px-4 py-3">
                <h1 class="text-xl font-bold text-white">
                    {{ cupName }} - {{ t('cupStatistics.header.overall') }}
                </h1>
            </div>
        </div>

        <!-- Cup Statistics -->
        <Panel :collapsed="true" toggleable class="mb-3">
            <template #header>
                <span class="font-semibold">{{ t('labels.statistics') }}</span>
            </template>
            <CupStatisticsWidget :cup-statistics="cupStatistics" />
        </Panel>

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
                                <span class="inline-flex items-center justify-center w-8 h-8 shrink-0 rounded-full bg-zinc-badge text-zinc-badge font-semibold text-xs">
                                    {{ index + 1 }}
                                </span>
                            </td>
                            <td class="px-3 py-1.5">
                                <router-link
                                    :to="{ name: 'event-results', params: { id: event.id } }"
                                    target="_blank"
                                    class="text-sm text-adaptive-secondary hover:text-primary hover:underline"
                                >
                                    {{ event.name }}
                                </router-link>
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
                                        <span class="inline-flex items-center justify-center w-8 h-8 shrink-0 rounded-full text-xs font-semibold" :class="getRankBadgeClass(rank)">
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
                                    <button
                                        v-if="findScoreForEventAndClassResultAndPerson(pws.classShortName, pws.personId, idx)"
                                        type="button"
                                        class="text-primary hover:text-primary-600 hover:underline cursor-pointer font-semibold"
                                        @click="navigateToPersonResult(
                                            event.id,
                                            getResultListIdForEvent(idx, pws.classShortName),
                                            pws.classShortName,
                                            pws.personId,
                                        )"
                                    >
                                        {{ findScoreForEventAndClassResultAndPerson(pws.classShortName, pws.personId, idx) }}
                                    </button>
                                    <span v-else class="text-adaptive-tertiary">-</span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>
