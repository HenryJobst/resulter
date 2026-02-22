<script setup lang="ts">
import type { CupStatistics } from '@/features/cup/model/cup_statistics'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'
import type { RaceOrganisationGroupedCupScore } from '@/features/cup/model/race_organisation_grouped_cup_score.ts'
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
    persons: Record<number, Person>
    cupStatistics: CupStatistics
}>()

const { t } = useI18n()
const router = useRouter()

// Lade ResultLists für jedes Event um Race-ID → ResultList-ID Mapping zu erstellen
const eventResultsQueries = useQueries({
    queries: computed(() => {
        return props.eventRacesCupScores.map(eventRace => ({
            queryKey: ['eventResults', eventRace.event.id],
            queryFn: () => EventService.getResultsById(eventRace.event.id.toString(), t),
            staleTime: 5 * 60 * 1000, // 5 Minuten
        }))
    }),
})

// Mapping: Race-ID → ResultList-ID
const raceIdToResultListId = computed(() => {
    const mapping = new Map<number, number>()

    eventResultsQueries.value.forEach((query) => {
        if (query.data?.value?.resultLists) {
            query.data.value.resultLists.forEach((rl) => {
                if (rl.raceId) {
                    mapping.set(rl.raceId, rl.id)
                }
            })
        }
    })

    return mapping
})

function getPersonName(personId: number): string {
    const person = props.persons?.[personId]
    if (person && person.givenName && person.familyName) {
        return `${person.givenName} ${person.familyName}`
    }
    return ''
}

// define Types
interface Race {
    score: number
    personWithScores: PersonWithScore[]
}

interface CombinedScore {
    club: string
    total: number
    races: Race[]
}

interface PlaceAndScore {
    place: number
    combinedScore: CombinedScore
}

// set variables used for creating master object
const initRace: Race = {
    score: 0,
    personWithScores: [] as PersonWithScore[],
}

const totalRaces = props.eventRacesCupScores.length

// creating master object
function createBaseCombinedScore(organisationScores: OrganisationScore[]): Map<number, CombinedScore> {
    const combinedScoreMap = new Map()
    for (const organisationScore of organisationScores) {
        if (organisationScore.score !== 0) {
            const combinedScore: CombinedScore = {
                club: organisationScore.organisation.name,
                total: organisationScore.score,
                races: Array.from({ length: totalRaces }).fill(initRace) as Race[],
            }
            combinedScoreMap.set(organisationScore.organisation.id, combinedScore)
        }
    }
    return combinedScoreMap
}

function getExistingOrganisationScores(raceOrganisationGroupedCupScores: RaceOrganisationGroupedCupScore[]): OrganisationScore[] {
    for (let i = 0; i < raceOrganisationGroupedCupScores.length; i++) {
        if (raceOrganisationGroupedCupScores[i] && raceOrganisationGroupedCupScores[i]!.organisationScores.length > 0) {
            return raceOrganisationGroupedCupScores[i]?.organisationScores ?? []
        }
    }
    return []
}

function fillCombinedScore(combinedScoreMap: Map<number, CombinedScore>, eventRacesCupScores: EventRacesCupScore[]): Map<number, CombinedScore> {
    for (let i = 0; i < totalRaces; i++) {
        const organisationScores: OrganisationScore[] = eventRacesCupScores[i]
            ? getExistingOrganisationScores(eventRacesCupScores[i]!.raceOrganisationGroupedCupScores)
            : []
        for (const organisationScore of organisationScores) {
            if (organisationScore.score !== 0) {
                const race: Race = {
                    score: organisationScore.score,
                    personWithScores: organisationScore.personWithScores,
                }
                if (combinedScoreMap.has(organisationScore.organisation.id)) {
                    combinedScoreMap.get(organisationScore.organisation.id)!.races[i] = race
                }
            }
        }
    }
    return combinedScoreMap
}

function sortByTotalPoints(score1: CombinedScore, score2: CombinedScore) {
    return score2.total - score1.total
}

function addPlace(combinedScores: CombinedScore[]): PlaceAndScore[] {
    let place: number = 0
    let previousTotal: number | undefined
    let previousPlace: number = 0

    const placeArray: PlaceAndScore[] = []
    for (const combinedScore of combinedScores) {
        place++
        if (previousTotal && combinedScore.total === previousTotal) {
            placeArray.push({ place: previousPlace, combinedScore })
        }
        else {
            placeArray.push({ place, combinedScore })
            previousPlace = place
            previousTotal = combinedScore.total
        }
    }

    return placeArray
}

function getCombinedScore(overallScores: OrganisationScore[], eventRacesCupScores: EventRacesCupScore[]): PlaceAndScore[] {
    const baseCombinedScore: Map<number, CombinedScore> = createBaseCombinedScore(overallScores)
    const combinedScoreMap: Map<number, CombinedScore> = fillCombinedScore(baseCombinedScore, eventRacesCupScores)
    const combinedScore: CombinedScore[] = Array.from(combinedScoreMap.values()).sort(sortByTotalPoints)
    return addPlace(combinedScore)
}

const placeAndScore: PlaceAndScore[] = getCombinedScore(props.overallScores, props.eventRacesCupScores)

// helper functions
function sortByClassNameSorter(score1: PersonWithScore, score2: PersonWithScore) {
    return score1.classShortName.localeCompare(score2.classShortName)
}

function sortByClassName(personWithScores: PersonWithScore[]): PersonWithScore[] {
    return personWithScores.slice().sort(sortByClassNameSorter)
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
 * Holt die ResultList-ID für einen bestimmten Race-Index
 */
function getResultListIdForRace(raceIndex: number): number | undefined {
    const eventRace = props.eventRacesCupScores[raceIndex]
    if (!eventRace)
        return undefined

    // Race-ID aus raceClassResultGroupedCupScores extrahieren
    const raceGroup = eventRace.raceClassResultGroupedCupScores?.[0]
    if (raceGroup?.race?.id) {
        return raceIdToResultListId.value.get(raceGroup.race.id)
    }

    // Fallback: raceOrganisationGroupedCupScores
    const orgGroup = eventRace.raceOrganisationGroupedCupScores?.[0]
    if (orgGroup?.race?.id) {
        return raceIdToResultListId.value.get(orgGroup.race.id)
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

    // Nur Deep-Link-Parameter hinzufügen wenn ResultList-ID verfügbar
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
            <div class="bg-linear-to-r from-orange-600 to-orange-700 dark:from-purple-700 dark:to-purple-800 px-4 py-3">
                <h1 class="text-xl font-bold text-white">
                    {{ cupName }}
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

        <!-- Race List Section -->
        <div class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
            <div class="px-4 py-2 border-b border-adaptive bg-adaptive-secondary">
                <h2 class="text-base font-semibold text-adaptive">
                    Läufe
                </h2>
            </div>
            <div class="px-4 py-2">
                <ol class="space-y-1">
                    <li
                        v-for="(race, index) in props.eventRacesCupScores"
                        :key="index"
                        class="flex items-center space-x-2 py-1"
                    >
                        <span class="inline-flex items-center justify-center w-2rem h-6 shrink-0 rounded-full bg-orange-badge text-orange-badge font-semibold text-xs">
                            {{ index + 1 }}
                        </span>
                        <router-link
                            :to="{ name: 'event-results', params: { id: race.event.id } }"
                            target="_blank"
                            class="text-sm text-adaptive-secondary hover:text-primary hover:underline"
                        >
                            {{ race.event.name }}
                        </router-link>
                    </li>
                </ol>
            </div>
        </div>

        <!-- Overall Scores Section -->
        <div class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
            <div class="px-4 py-2 border-b border-adaptive bg-adaptive-secondary">
                <h2 class="text-base font-semibold text-adaptive">
                    Vereinswertung
                </h2>
            </div>

            <div v-if="!overallScores.length" class="px-4 py-8 text-center">
                <div class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-gray-100 mb-3">
                    <svg class="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                </div>
                <p class="text-gray-500 text-sm">
                    Noch keine Punkte vorhanden.
                </p>
            </div>

            <div v-else class="overflow-x-auto">
                <table class="table-fixed divide-y divide-adaptive text-sm" style="width: auto;">
                    <colgroup>
                        <col style="width: 350px;">
                        <col style="width: 80px;">
                        <col v-for="(_cs, index) in props.eventRacesCupScores" :key="index" style="width: 50px;">
                    </colgroup>
                    <thead class="bg-adaptive-secondary">
                        <tr>
                            <th scope="col" class="px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider">
                                Verein
                            </th>
                            <th scope="col" class="px-3 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider bg-orange-highlight">
                                Gesamt
                            </th>
                            <th
                                v-for="(_race, index) in props.eventRacesCupScores"
                                :key="index"
                                scope="col"
                                class="px-2 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider"
                            >
                                L{{ index + 1 }}
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-adaptive">
                        <tr
                            v-for="({ place, combinedScore }, index) in placeAndScore"
                            :key="combinedScore.club"
                            class="hover:bg-adaptive-tertiary transition-colors duration-150 border-b border-adaptive"
                            :class="{ 'bg-adaptive-secondary': index % 2 === 1 }"
                        >
                            <td class="px-3 py-1.5">
                                <div class="flex items-center space-x-2">
                                    <div v-if="place <= 3" class="flex items-center justify-center w-2 shrink-0">
                                        <img
                                            v-if="place === 1"
                                            src="@/assets/medal-gold.svg"
                                            alt="Erster Platz"
                                            class="w-14 h-14 object-contain"
                                        >
                                        <img
                                            v-else-if="place === 2"
                                            src="@/assets/medal-silver.svg"
                                            alt="Zweiter Platz"
                                            class="w-14 h-14 object-contain"
                                        >
                                        <img
                                            v-else-if="place === 3"
                                            src="@/assets/medal-bronze.svg"
                                            alt="Dritter Platz"
                                            class="w-14 h-14 object-contain"
                                        >
                                    </div>
                                    <span v-else class="inline-flex items-center justify-center w-2 h-6 shrink-0 rounded-full text-xs font-bold" :class="getRankBadgeClass(place)">
                                        {{ place }}
                                    </span>
                                    <span class="text-sm font-semibold text-adaptive whitespace-nowrap">{{ combinedScore.club }}</span>
                                </div>
                            </td>
                            <td class="px-3 py-1.5 text-center bg-orange-highlight">
                                <span class="inline-flex items-center justify-center px-2 py-0.5 w-3rem rounded-full text-sm font-bold bg-orange-badge text-orange-badge">
                                    {{ combinedScore.total }}
                                </span>
                            </td>
                            <td
                                v-for="race in combinedScore.races"
                                :key="race.score"
                                class="px-2 py-1.5 text-center text-adaptive-secondary text-xs"
                            >
                                {{ race.score }}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Details Section -->
        <div v-if="overallScores.length" class="space-y-3">
            <div class="flex items-center">
                <h2 class="text-lg font-bold text-adaptive">
                    Details
                </h2>
                <div class="ml-2 flex-1 border-t border-adaptive-strong" />
            </div>

            <div
                v-for="{ combinedScore } in placeAndScore"
                :key="combinedScore.club"
                class="bg-adaptive rounded shadow-sm border border-adaptive overflow-hidden transition-all duration-200 hover:shadow-md"
            >
                <div class="px-4 py-2 bg-adaptive-tertiary border-b border-adaptive">
                    <h3 class="text-base font-bold text-adaptive">
                        {{ combinedScore.club }}
                    </h3>
                </div>

                <div class="p-3 space-y-3">
                    <div
                        v-for="(race, index) in combinedScore.races"
                        :key="index"
                        class="border border-adaptive rounded overflow-hidden"
                    >
                        <div class="px-3 py-1.5 bg-orange-highlight border-b border-adaptive">
                            <h4 class="text-sm font-semibold text-adaptive">
                                Lauf {{ index + 1 }}
                            </h4>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full">
                                <thead class="bg-adaptive-secondary border-b-2 border-adaptive-strong">
                                    <tr>
                                        <th scope="col" class="px-3 py-1.5 w-1 text-left text-xs font-semibold text-adaptive-secondary">
                                            Klasse
                                        </th>
                                        <th scope="col" class="px-3 py-1.5 text-left text-xs font-semibold text-adaptive-secondary">
                                            Name
                                        </th>
                                        <th scope="col" class="px-3 py-1.5 text-right text-xs font-semibold text-adaptive-secondary">
                                            Punkte
                                        </th>
                                    </tr>
                                </thead>
                                <tbody class="bg-adaptive">
                                    <tr
                                        v-for="(person, idx) in sortByClassName(race.personWithScores)"
                                        :key="person.classShortName"
                                        class="hover:bg-adaptive-tertiary transition-colors border-b border-adaptive"
                                        :class="{ 'bg-adaptive-secondary': idx % 2 === 1 }"
                                    >
                                        <td class="px-3 py-1 w-1">
                                            <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-badge text-gray-badge">
                                                {{ person.classShortName }}
                                            </span>
                                        </td>
                                        <td class="px-3 py-1 text-left text-adaptive-secondary text-xs font-semibold">
                                            {{ getPersonName(person.personId) }}
                                        </td>
                                        <td class="px-3 py-1 text-right text-xs">
                                            <button
                                                v-if="person.score > 0"
                                                type="button"
                                                class="text-primary hover:text-primary-600 hover:underline cursor-pointer font-semibold"
                                                @click="navigateToPersonResult(
                                                    props.eventRacesCupScores[index].event.id,
                                                    getResultListIdForRace(index),
                                                    person.classShortName,
                                                    person.personId,
                                                )"
                                            >
                                                {{ person.score }}
                                            </button>
                                            <span v-else class="text-adaptive-secondary font-semibold">{{ person.score }}</span>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
