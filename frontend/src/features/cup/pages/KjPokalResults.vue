<script setup lang="ts">
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'

const props = defineProps<{
    cupName: string
    eventRacesCupScores: EventRacesCupScore[]
    overallScores: OrganisationScore[]
}>()

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

function fillCombinedScore(combinedScoreMap: Map<number, CombinedScore>, eventRacesCupScores: EventRacesCupScore[]): Map<number, CombinedScore> {
    for (let i = 0; i < totalRaces; i++) {
        const organisationScores: OrganisationScore[] = eventRacesCupScores[i].raceOrganisationGroupedCupScores[0].organisationScores
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
</script>

<template>
    <div class="max-w-7xl mx-auto px-3 py-3 sm:px-4 lg:px-6">
        <!-- Header Section -->
        <div class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
            <div class="bg-gradient-to-r from-orange-600 to-orange-700 dark:from-purple-700 dark:to-purple-800 px-4 py-3">
                <h1 class="text-xl font-bold text-white">
                    {{ cupName }}
                </h1>
            </div>
        </div>

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
                        <span class="inline-flex items-center justify-center w-2rem h-6 flex-shrink-0 rounded-full bg-purple-badge text-purple-badge font-semibold text-xs">
                            {{ index + 1 }}
                        </span>
                        <span class="text-sm text-adaptive-secondary">{{ race.event.name }}</span>
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
                        <col v-for="(index) in props.eventRacesCupScores" :key="index" style="width: 50px;">
                    </colgroup>
                    <thead class="bg-adaptive-secondary">
                        <tr>
                            <th scope="col" class="px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider">
                                Verein
                            </th>
                            <th scope="col" class="px-3 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider bg-purple-highlight">
                                Gesamt
                            </th>
                            <th
                                v-for="(index) in props.eventRacesCupScores"
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
                                    <span class="inline-flex items-center justify-center w-2rem h-5 flex-shrink-0 rounded-full text-xs font-bold" :class="getRankBadgeClass(place)">
                                        {{ place }}
                                    </span>
                                    <span class="text-sm font-semibold text-adaptive whitespace-nowrap">{{ combinedScore.club }}</span>
                                </div>
                            </td>
                            <td class="px-3 py-1.5 text-center bg-purple-highlight">
                                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-sm font-bold bg-purple-badge text-purple-badge">
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
                        <div class="px-3 py-1.5 bg-purple-highlight border-b border-adaptive">
                            <h4 class="text-sm font-semibold text-adaptive">
                                Lauf {{ index + 1 }}
                            </h4>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full">
                                <thead class="bg-adaptive-secondary border-b-2 border-adaptive-strong">
                                    <tr>
                                        <th scope="col" class="px-3 py-1.5 text-left text-xs font-semibold text-adaptive-secondary">
                                            Klasse
                                        </th>
                                        <th scope="col" class="px-3 py-1.5 text-right text-xs font-semibold text-adaptive-secondary">
                                            Punkte
                                        </th>
                                    </tr>
                                </thead>
                                <tbody class="bg-adaptive">
                                    <tr
                                        v-for="(person, index) in sortByClassName(race.personWithScores)"
                                        :key="person.classShortName"
                                        class="hover:bg-adaptive-tertiary transition-colors border-b border-adaptive"
                                        :class="{ 'bg-adaptive-secondary': index % 2 === 1 }"
                                    >
                                        <td class="px-3 py-1">
                                            <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-badge text-gray-badge">
                                                {{ person.classShortName }}
                                            </span>
                                        </td>
                                        <td class="px-3 py-1 text-right text-adaptive-secondary text-xs font-semibold">
                                            {{ person.score }}
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
