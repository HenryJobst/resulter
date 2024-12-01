<script setup lang="ts">
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'

defineProps<{
    cupName: string
    eventRacesCupScores: EventRacesCupScore[]
    overallScores: OrganisationScore[]
}>()

function findOrganisationScore(org: any, raceCupScores: any): string {
    const entry = raceCupScores.organisationScores.find(
        os => os.organisation.id === org.organisation.id,
    )
    const score = entry?.score ?? 0.0
    return score !== 0.0 ? score.toString() : ''
}
</script>

<template>
    <div class="cup-container">
        <div id="page_header">
            <table>
                <thead>
                    <tr>
                        <th id="cup_name">
                            <nobr>{{ cupName }}</nobr>
                        </th>
                        <th id="date_time">
                            <nobr>{{}}</nobr>
                        </th>
                    </tr>
                    <tr>
                        <th id="event_name">
                            <nobr>Vereinswertung</nobr>
                        </th>
                        <th id="creation_text">
                            <nobr />
                        </th>
                    </tr>
                </thead>
            </table>
        </div>
        <div id="club_results">
            <section v-if="overallScores.length" class="">
                <table>
                    <thead>
                        <tr>
                            <th class="top">
                                Verein/Klassen
                            </th>
                            <th class="sum">
                                Gesamt
                            </th>
                            <th
                                v-for="cls in overallScores[0].personWithScores"
                                :key="cls.classShortName"
                                class="cl"
                            >
                                {{ cls.classShortName }}
                            </th>
                            <th
                                v-for="r in eventRacesCupScores[0].raceCupScores"
                                :key="r.race.id"
                                class="ev"
                            >
                                {{ r.race.name }}
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="org in overallScores" :key="org.organisation.id" class="">
                            <td class="cb">
                                {{ org.organisation.shortName }}
                            </td>
                            <td class="sum">
                                {{ org.score }}
                            </td>
                            <td
                                v-for="cls in org.personWithScores"
                                :key="cls.classShortName"
                                class="cl"
                            >
                                {{ cls.score !== 0 ? cls.score.toString() : '' }}
                            </td>
                            <td
                                v-for="r in eventRacesCupScores[0].raceCupScores"
                                :key="r.race.id"
                                class="ev"
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
</style>
