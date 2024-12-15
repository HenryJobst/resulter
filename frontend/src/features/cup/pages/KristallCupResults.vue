<script setup lang="ts">
import { computed } from 'vue'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'
import type { RaceOrganisationGroupedCupScore } from '@/features/cup/model/race_organisation_grouped_cup_score'

const props = defineProps<{
    cupName: string
    eventRacesCupScores: EventRacesCupScore[]
    overallScores: OrganisationScore[]
}>()

const allClassShortNames = computed(() => {
    // Flache Liste aller `classShortName` aus allen `personWithScores` extrahieren
    const classNames = props.overallScores.flatMap(orgScore =>
        orgScore.personWithScores.map(person => person.classShortName),
    )

    // Duplikate entfernen und sortieren
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
        .filter(person => person.classShortName === targetClassShortName) // Filtere nach classShortName
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
</script>

<template>
    <div class="cup-container">
        <div id="page_header">
            <table>
                <thead>
                    <tr>
                        <th id="cup_name">
                            <span class="nowrap">{{ cupName }}</span>
                        </th>
                        <th id="date_time">
                            <span class="nowrap">{{}}</span>
                        </th>
                    </tr>
                    <tr>
                        <th id="event_name">
                            <span class="nowrap">Vereinswertung</span>
                        </th>
                        <th id="creation_text" />
                    </tr>
                </thead>
            </table>
        </div>
        <div id="club_results">
            <section v-if="!overallScores.length">
                <p>Noch keine Punkte vorhanden.</p>
            </section>
            <section v-if="overallScores.length" class="">
                <table>
                    <thead>
                        <tr>
                            <th class="top">
                                Verein/Klassen
                            </th>
                            <th class="sum with-right-divider">
                                Gesamt
                            </th>
                            <th v-for="csn in allClassShortNames" :key="csn" class="cl">
                                {{ csn }}
                            </th>
                            <th
                                v-for="(r, index) in eventRacesCupScores[0].raceOrganisationGroupedCupScores"
                                :key="r.race.id"
                                class="ev" :class="[index === 0 ? 'with-left-divider' : '']"
                            >
                                {{ r.race.name }}
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="{ org, rank } in calculateRanks(overallScores)" :key="org.organisation.id" class="">
                            <td class="cb">
                                {{ `${rank}. ${org.organisation.shortName}` }}
                            </td>
                            <td class="sum with-right-divider">
                                {{ org.score }}
                            </td>
                            <td v-for="csn in allClassShortNames" :key="csn" class="cl">
                                {{ getTotalScoreByClass(org.personWithScores, csn) }}
                            </td>
                            <td
                                v-for="(r, index) in eventRacesCupScores[0].raceOrganisationGroupedCupScores"
                                :key="r.race.id"
                                class="ev" :class="[index === 0 ? 'with-left-divider' : '']"
                            >
                                {{ findOrganisationScore(org, r) }}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </section>
        </div>
    </div>
</template>

<style scoped>
body {
    padding: 0;
    font-family: Verdana, Arial, Helvetica, sans-serif;
    font-style: normal;
    font-variant: normal;
    font-weight: normal;
    font-size: 100%;
    line-height: 100%;
    font-size-adjust: none;
    font-stretch: normal;
}
table {
    border-style: none;
    margin: auto;
}
h2 {
    font-size: medium;
    margin-left: 10px;
}
div#page_header table {
    border-style: solid none;
    border-color: #000000;
    border-width: 1px 0px;
    margin: 0px auto 30px;
    padding: 10px 5px;
    border-collapse: separate;
    width: 100%;
}
div#page_header #first_row {
    vertical-align: text-top;
    height: 40px;
}
div#page_header #cup_name,
#event_name {
    min-width: 300px;
    text-align: left;
}
div#page_header #cup_name {
    font-size: 120%;
    text-align: left;
}
div#page_header #event_name {
    vertical-align: text-bottom;
}
div#page_header #date_time {
    min-width: 100px;
    text-align: right;
}
div#page_header #creation_text {
    text-align: right;
    vertical-align: text-bottom;
    font-size: 50%;
}
div#club_results {
    margin-bottom: 30px;
}
div#club_results table {
    font-size: 75%;
    margin: auto;
    border-collapse: collapse;
}
div#club_results th,
td {
    border-style: solid;
    border-width: thin;
    padding: 5px;
}
div#club_results th {
    padding-bottom: 8px;
    text-align: left;
    vertical-align: middle;
}
div#club_results th.top {
    width: 260px;
}

div#club_results td.sum {
    font-weight: bold;
}

div#club_results th.cl,
th.ev {
    text-align: center;
}

div#club_results td.sum,
td.cl,
td.ev {
    text-align: center;
}

.with-left-divider {
    border-left: 2px solid #ccc !important;
    padding-left: 8px !important;
}

.with-right-divider {
    border-right: 2px solid #ccc !important;
    padding-right: 8px !important;
}
</style>
