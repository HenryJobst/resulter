<script setup lang="ts">
import type { AggregatedPersonScores } from '@/features/cup/model/aggregated_person_scores'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'
import { personService } from '@/features/person/services/person.service'
import { useQuery } from '@tanstack/vue-query'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
    cupName: string
    eventRacesCupScores: EventRacesCupScore[]
    overallScores: OrganisationScore[]
    aggregatedPersonScores: AggregatedPersonScores[]
}>()

const { t } = useI18n()

const personQuery = useQuery({
    queryKey: ['persons'],
    queryFn: () => personService.getAllUnpaged(t),
})

function person(personId: number) {
    if (personQuery.data?.value && Array.isArray(personQuery.data.value)) {
        const person = personQuery.data.value.find(p => p.id === personId) ?? undefined
        if (person) {
            return `${person?.givenName} ${person?.familyName}`
        }
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
            <section v-if="allEvents.length">
                <table>
                    <thead>
                        <tr>
                            <th class="pl">
                                {{ 'Lauf' }}
                            </th>
                            <th>
                                {{ 'Name' }}
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(event, index) in allEvents" :key="event.id">
                            <td>{{ index + 1 }}</td>
                            <td>{{ event.name }}</td>
                        </tr>
                    </tbody>
                </table>
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
                                <th colspan="4" class="with-right-divider">
                                    {{ entry.classResultShortName }}
                                </th>
                                <th
                                    v-for="(it, index) in allEvents"
                                    :key="it.id"
                                    class="text-center min-w-10"
                                >
                                    {{ index + 1 }}
                                </th>
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
                                <td class="pt with-right-divider text-center font-bold">
                                    {{ pws.score }}
                                </td>
                                <td
                                    v-for="(it, index) in allEvents"
                                    :key="it.id"
                                    class="min-w-10 text-center"
                                >
                                    {{
                                        findScoreForEventAndClassResultAndPerson(
                                            pws.classShortName,
                                            pws.personId,
                                            index,
                                        )
                                    }}
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
    border-width: 1px 0;
    margin: 0 auto 30px;
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
.with-right-divider {
    border-right: 2px solid #ccc !important;
    padding-right: 8px !important;
}
</style>
