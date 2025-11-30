<script setup lang="ts">
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'
import type { Person } from '@/features/person/model/person'

const props = defineProps<{
    cupName: string
    eventRacesCupScore: EventRacesCupScore[]
    overallScores: OrganisationScore[]
    persons: Record<number, Person>
}>()

function person(personId: number): string {
    const person = props.persons?.[personId]
    if (person && person.givenName && person.familyName) {
        return `${person.givenName} ${person.familyName}`
    }
    return ''
}

function mergeAndSortScoresByClassAndPerson(scores: PersonWithScore[]): PersonWithScore[] {
    const grouped = scores.reduce((acc, curr) => {
        const key = `${curr.classShortName}-${curr.personId}`
        if (!acc.has(key)) {
            acc.set(key, { ...curr })
        }
        else {
            acc.get(key)!.score += curr.score
        }
        return acc
    }, new Map<string, PersonWithScore>())

    return Array.from(grouped.values())
        .filter(person => person.score > 0)
        .sort((a, b) => {
            if (b.score !== a.score) {
                return b.score - a.score
            }
            return a.classShortName.localeCompare(b.classShortName)
        })
}

function calculateRanks(scores: OrganisationScore[]): { org: OrganisationScore, rank: number }[] {
    const sortedScores = [...scores].sort((a, b) => {
        if (b.score !== a.score) {
            return b.score - a.score
        }
        return a.organisation.shortName.localeCompare(b.organisation.shortName)
    })

    let rank = 0
    let previousScore: number | undefined
    let previousRank = 0

    return sortedScores.map((org, index) => {
        if (org.score !== previousScore) {
            rank = index + 1
            previousRank = rank
        }
        else {
            rank = previousRank
        }
        previousScore = org.score

        return { org, rank }
    })
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
    <div class="max-w-[90rem] mx-auto px-3 py-3 sm:px-4 lg:px-6">
        <!-- Header Section -->
        <div class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
            <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-4 py-3">
                <h1 class="text-xl font-bold text-white">
                    {{ cupName }} - Gesamtwertung
                </h1>
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
                <div class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-adaptive-tertiary mb-3">
                    <svg class="w-6 h-6 text-adaptive-tertiary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                </div>
                <p class="text-adaptive-tertiary text-sm">
                    Noch keine Punkte vorhanden.
                </p>
            </div>

            <div v-else class="overflow-x-auto">
                <table class="min-w-full">
                    <thead class="bg-adaptive-secondary border-b-2 border-adaptive-strong">
                        <tr>
                            <th scope="col" class="px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider w-16">
                                Platz
                            </th>
                            <th scope="col" class="px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider">
                                Verein
                            </th>
                            <th scope="col" class="px-3 py-2 text-right text-xs font-semibold text-adaptive-secondary tracking-wider w-24">
                                Punkte
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-adaptive">
                        <tr
                            v-for="({ org, rank }, index) in calculateRanks(
                                overallScores.filter((o) => o.score > 0),
                            )"
                            :key="org.organisation.id"
                            class="hover:bg-adaptive-tertiary transition-colors duration-150 border-b border-adaptive"
                            :class="{ 'bg-adaptive-secondary': index % 2 === 1 }"
                        >
                            <td class="px-3 py-2 whitespace-nowrap">
                                <div v-if="rank <= 3" class="flex items-center justify-center">
                                    <img
                                        v-if="rank === 1"
                                        src="@/assets/medal-gold.svg"
                                        alt="Erster Platz"
                                        class="w-14 h-14 object-contain flex-shrink-0"
                                    >
                                    <img
                                        v-else-if="rank === 2"
                                        src="@/assets/medal-silver.svg"
                                        alt="Zweiter Platz"
                                        class="w-14 h-14 object-contain flex-shrink-0"
                                    >
                                    <img
                                        v-else-if="rank === 3"
                                        src="@/assets/medal-bronze.svg"
                                        alt="Dritter Platz"
                                        class="w-14 h-14 object-contain flex-shrink-0"
                                    >
                                </div>
                                <div v-else class="flex items-center justify-center">
                                    <span class="inline-flex items-center justify-center w-7 h-7 flex-shrink-0 rounded-full text-xs font-semibold" :class="getRankBadgeClass(rank)">
                                        {{ rank }}
                                    </span>
                                </div>
                            </td>
                            <td class="px-3 py-2 whitespace-nowrap">
                                <div class="text-sm font-semibold text-adaptive">
                                    {{ org.organisation.shortName }}
                                </div>
                            </td>
                            <td class="px-3 py-2 whitespace-nowrap text-right">
                                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-sm font-bold bg-slate-badge text-slate-badge">
                                    {{ org.score }}
                                </span>
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
                v-for="{ org, rank } in calculateRanks(
                    overallScores.filter((o) => o.score > 0),
                )"
                :key="org.organisation.id"
                class="bg-adaptive rounded shadow-sm border border-adaptive overflow-hidden transition-all duration-200 hover:shadow-md"
            >
                <div class="px-4 py-2 bg-gradient-to-r bg-adaptive-tertiary border-b border-adaptive">
                    <div class="flex items-center justify-between gap-3">
                        <div class="flex items-center gap-2 flex-1">
                            <div v-if="rank <= 3" class="flex items-center justify-center">
                                <img
                                    v-if="rank === 1"
                                    src="@/assets/medal-gold.svg"
                                    alt="Erster Platz"
                                    class="w-10 h-10 object-contain flex-shrink-0"
                                >
                                <img
                                    v-else-if="rank === 2"
                                    src="@/assets/medal-silver.svg"
                                    alt="Zweiter Platz"
                                    class="w-10 h-10 object-contain flex-shrink-0"
                                >
                                <img
                                    v-else-if="rank === 3"
                                    src="@/assets/medal-bronze.svg"
                                    alt="Dritter Platz"
                                    class="w-10 h-10 object-contain flex-shrink-0"
                                >
                            </div>
                            <span v-else class="inline-flex items-center justify-center w-2rem h-5 flex-shrink-0 rounded-full text-xs font-bold" :class="getRankBadgeClass(rank)">
                                {{ rank }}
                            </span>
                            <h3 class="text-base font-bold text-adaptive ml-1">
                                {{ org.organisation.shortName }}
                            </h3>
                        </div>
                        <span class="inline-flex items-center px-2.5 py-1 rounded-full text-sm font-bold bg-slate-badge text-slate-badge whitespace-nowrap flex-shrink-0">
                            {{ org.score }} Punkte
                        </span>
                    </div>
                </div>

                <div class="overflow-x-auto">
                    <table class="w-1xl table-fixed">
                        <colgroup>
                            <col style="width: 10%">
                            <col style="width: 50%">
                            <col style="width: 15%">
                        </colgroup>
                        <tbody class="bg-adaptive">
                            <tr
                                v-for="(personWithScore, index) in mergeAndSortScoresByClassAndPerson(
                                    org.personWithScores ?? [],
                                )"
                                :key="`${personWithScore.personId}-${personWithScore.classShortName}`"
                                class="hover:bg-adaptive-tertiary transition-colors duration-150 border-b border-adaptive"
                                :class="{ 'bg-adaptive-secondary': index % 2 === 1 }"
                            >
                                <td class="px-3 py-1.5 whitespace-nowrap">
                                    <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-badge text-gray-badge">
                                        {{ personWithScore.classShortName }}
                                    </span>
                                </td>
                                <td class="px-3 py-1.5">
                                    <div class="text-xs text-adaptive">
                                        {{ person(personWithScore.personId) }}
                                    </div>
                                </td>
                                <td class="px-3 py-1.5 whitespace-nowrap text-right">
                                    <span class="text-xs font-semibold text-adaptive-secondary">
                                        {{ personWithScore.score }}
                                    </span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>
