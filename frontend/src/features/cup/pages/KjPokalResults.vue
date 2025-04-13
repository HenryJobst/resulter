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
</script>

<template>
    <div>
        <h2> {{ cupName }} </h2>
        <h3>Läufe</h3>
        <ol class="race-list">
            <li v-for="(race, index) in props.eventRacesCupScores" :key="index">
                {{ race.event.name }}
            </li>
        </ol>
        <h3>Vereinswertung</h3>
        <div>
            <section v-if="!overallScores.length">
                <p>Noch keine Punkte vorhanden.</p>
            </section>
            <section v-if="overallScores.length">
                <table>
                    <thead>
                        <tr>
                            <th>Verein \ Punkte</th>
                            <th>Gesamt</th>
                            <th v-for="(_race, index) in props.eventRacesCupScores" :key="index">
                                Lauf {{ index + 1 }}
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="{ place, combinedScore } in placeAndScore" :key="combinedScore.club">
                            <td>{{ place }}. {{ combinedScore.club }}</td>
                            <td>{{ combinedScore.total }}</td>
                            <td v-for="(race) in combinedScore.races" :key="race.score">
                                {{ race.score }}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </section>
        </div>
        <h3>Details</h3>
        <template v-for="{ combinedScore } in placeAndScore" :key="combinedScore.club">
            <h4>{{ combinedScore.club }}</h4>
            <template v-for="(race, index) in combinedScore.races" :key="index">
                <h5>Lauf {{ index + 1 }}</h5>
                <table>
                    <thead>
                        <tr>
                            <th>Class</th>
                            <th>Points</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="person in sortByClassName(race.personWithScores)" :key="person.classShortName">
                            <td>{{ person.classShortName }}</td>
                            <td>{{ person.score }}</td>
                        </tr>
                    </tbody>
                </table>
            </template>
        </template>
    </div>
</template>

<style scoped>
table, th, td {
    border-style: solid;
    border-width: thin;
    padding: 5px 10px;
}

/* td + td -> only apply if there is a td right before (so not on first column) */
th, td + td {
    text-align: center;
}

.race-list {
    list-style-type: decimal;
    padding-left: 1em;
}

.race-list li {
    padding-left: 1em;
}

/* reset headings back to default */
h2 {
    margin-block: 0.83em;
    font-size: 1.5em;
    font-weight: bold;
}

h3 {
    margin-block: 1em;
    font-size: 1.17em;
    font-weight: bold;
}

h4 {
    margin-block: 1.33em;
    font-size: 1.00em;
    font-weight: bold;
}

h5 {
    margin-block: 1.67em;
    font-size: 0.83em;
    font-weight: bold;
}
</style>
