<script setup lang="ts">
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'
import type { RaceOrganisationGroupedCupScore } from '@/features/cup/model/race_organisation_grouped_cup_score'
import { computed } from 'vue'

const props = defineProps<{
    cupName: string
    eventRacesCupScores: EventRacesCupScore[]
    overallScores: OrganisationScore[]
}>()

const allClassShortNames = computed(() => {
    const classNames = props.overallScores.flatMap(orgScore =>
        orgScore.personWithScores.map(person => person.classShortName),
    )
    return Array.from(new Set(classNames)).sort()
})

function findOrganisationScore(org: OrganisationScore, raceCupScores: RaceOrganisationGroupedCupScore): string {
    const entry = raceCupScores.organisationScores.find(
        os => os.organisation.id === org.organisation.id,
    )
    const score = entry?.score ?? 0.0
    return score !== 0.0 ? score.toString() : ''
}

function getTotalScoreByClass(scores: PersonWithScore[], targetClassShortName: string): string {
    const score = scores
        .filter(person => person.classShortName === targetClassShortName)
        .reduce((total, person) => total + person.score, 0)
    return score > 0 ? score.toString() : ''
}

function calculateRanks(scores: OrganisationScore[]): { org: OrganisationScore, rank: number }[] {
    const sortedScores = [...scores.filter(org => org.organisation.shortName !== 'ohne Verein')].sort((a, b) => {
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
            <div class="bg-gradient-to-r from-stone-600 to-stone-700 px-4 py-3">
                <h1 class="text-xl font-bold text-white">
                    {{ cupName }} - Vereinswertung
                </h1>
            </div>
        </div>

        <!-- Overall Scores Section -->
        <div class="bg-adaptive rounded shadow-sm border border-adaptive mb-3 overflow-hidden">
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
                <table class="min-w-full text-xs">
                    <thead class="bg-adaptive-secondary border-b-2 border-adaptive-strong">
                        <tr>
                            <th scope="col" class="sticky left-0 z-10 bg-adaptive-secondary px-3 py-2 text-left text-xs font-semibold text-adaptive-secondary tracking-wider min-w-[200px]">
                                Verein
                            </th>
                            <th scope="col" class="px-2 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider bg-stone-highlight border-r-2 border-adaptive-strong w-16">
                                Gesamt
                            </th>
                            <th
                                v-for="csn in allClassShortNames"
                                :key="csn"
                                scope="col"
                                class="px-2 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider w-14"
                            >
                                {{ csn }}
                            </th>
                            <th
                                v-for="(r, index) in eventRacesCupScores[0].raceOrganisationGroupedCupScores"
                                :key="r.race.id"
                                scope="col"
                                class="px-2 py-2 text-center text-xs font-semibold text-adaptive-secondary tracking-wider w-14"
                                :class="[index === 0 ? 'border-l-2 border-adaptive-strong' : '']"
                            >
                                {{ r.race.name }}
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-adaptive">
                        <tr
                            v-for="({ org, rank }, index) in calculateRanks(overallScores)"
                            :key="org.organisation.id"
                            class="hover:bg-adaptive-tertiary transition-colors duration-150 border-b border-adaptive"
                            :class="{ 'bg-adaptive-secondary': index % 2 === 1 }"
                        >
                            <td class="sticky left-0 z-10 bg-adaptive px-3 py-1.5 border-r border-adaptive">
                                <div class="flex items-center space-x-2">
                                    <span class="inline-flex items-center justify-center w-3rem h-6 flex-shrink-0 rounded-full text-xs font-bold" :class="getRankBadgeClass(rank)">
                                        {{ rank }}
                                    </span>
                                    <span class="text-sm font-semibold text-adaptive">{{ org.organisation.shortName }}</span>
                                </div>
                            </td>
                            <td class="px-2 py-1.5 text-center bg-stone-highlight border-r-2 border-adaptive-strong">
                                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-bold bg-stone-badge text-stone-badge">
                                    {{ org.score }}
                                </span>
                            </td>
                            <td
                                v-for="csn in allClassShortNames"
                                :key="csn"
                                class="px-2 py-1.5 text-center text-xs"
                            >
                                <span v-if="getTotalScoreByClass(org.personWithScores, csn)" class="text-adaptive font-semibold">
                                    {{ getTotalScoreByClass(org.personWithScores, csn) }}
                                </span>
                                <span v-else class="text-adaptive-tertiary">-</span>
                            </td>
                            <td
                                v-for="(r, index) in eventRacesCupScores[0].raceOrganisationGroupedCupScores"
                                :key="r.race.id"
                                class="px-2 py-1.5 text-center text-xs"
                                :class="[index === 0 ? 'border-l-2 border-adaptive-strong' : '']"
                            >
                                <span v-if="findOrganisationScore(org, r)" class="text-adaptive font-semibold">
                                    {{ findOrganisationScore(org, r) }}
                                </span>
                                <span v-else class="text-adaptive-tertiary">-</span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<style scoped>
.sticky {
    position: sticky;
}
</style>
