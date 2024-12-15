<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { useI18n } from 'vue-i18n'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import { personService } from '@/features/person/services/person.service'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'
import type { AggregatedPersonScores } from '@/features/cup/model/aggregated_person_scores'

defineProps<{
    cupName: string
    eventRacesCupScores: EventRacesCupScore[]
    overallScores: OrganisationScore[]
    aggregatedPersonScores: AggregatedPersonScores[]
}>()

const { t } = useI18n()

const personQuery = useQuery({
    queryKey: ['persons'],
    queryFn: () => personService.getAll(t),
})

function person(personId: number) {
    const person = personQuery.data.value?.content?.find(p => p.id === personId) ?? undefined
    if (person) {
        return `${person?.givenName} ${person?.familyName}`
    }
    return ''
}

function _mergeAndSortScoresByClassAndPerson(scores: PersonWithScore[]): PersonWithScore[] {
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
                            <span class="nowrap">Gesamtwertung</span>
                        </th>
                        <th id="creation_text" />
                    </tr>
                </thead>
            </table>
        </div>
        <div id="club_results">
            <section v-if="!aggregatedPersonScores.length">
                <p>Noch keine Punkte vorhanden.</p>
            </section>
            <section v-if="aggregatedPersonScores.length">
                <div
                    v-for="entry in aggregatedPersonScores"
                    :key="entry.classResultShortName"
                    class="my-3"
                >
                    <table class="my-3">
                        <thead>
                            <tr>
                                <th colspan="4">
                                    {{ entry.classResultShortName }}
                                </th>
                                <th
                                    v-for="it in eventRacesCupScores.flatMap(x => x.raceClassResultGroupedCupScores)" :key="it.race?.id"
                                />
                            </tr>
                        </thead>
                        <tbody>
                            <tr
                                v-for="{ pws, rank } in calculateRanks(
                                    entry.personWithScoreList.filter((o) => o.score > 0),
                                )"
                                :key="pws.personId"
                            >
                                <td class="pl">
                                    <img
                                        v-if="rank === 1"
                                        src="@/assets/1.jpg"
                                        alt="Erster Platz"
                                    >
                                    <img
                                        v-else-if="rank === 2"
                                        src="@/assets/2.jpg"
                                        alt="Zweiter Platz"
                                    >
                                    <img
                                        v-else-if="rank === 3"
                                        src="@/assets/3.jpg"
                                        alt="Dritter Platz"
                                    >
                                    <span v-else class="rank">{{ rank }}.</span>
                                </td>
                                <td class="cl" colspan="2">
                                    {{ `${person(pws.personId)}` }}
                                </td>
                                <td class="pt">
                                    {{ pws.score }}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    </div>
</template>

<style scoped>
body {
    padding: 0px;
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
div#club_results th.pl,
td.pl {
    width: 50px;
}
div#club_results th.cl,
td.cl {
    width: 260px;
}
div#club_results th.pt,
td.pt {
    width: 50px;
}
div#club_results td.pl {
    text-align: right;
}
div#club_results th.cl,
td.cl {
    text-align: left;
}
div#club_results th.pt,
td.pt {
    text-align: right;
}
div#detailed_results table {
    margin: auto auto 10px;
    border-collapse: collapse;
}
div#detailed_results th,
td {
    border-style: solid;
    border-width: thin;
    padding: 5px;
}
div#detailed_results th {
    padding-bottom: 8px;
}
div#detailed_results th.pl,
td.pl {
    width: 50px;
    text-align: right;
}
div#detailed_results th.cl,
td.cl {
    width: 260px;
    text-align: left;
}
div#detailed_results th.pt,
td.pt {
    width: 50px;
    text-align: right;
}
</style>
