<script setup lang="ts">
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed } from 'vue'
import { CupService, cupService } from '@/features/cup/services/cup.service'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const props = defineProps<{ id: string }>()

const { t, locale } = useI18n()

const authStore = useAuthStore()
const router = useRouter()

const cupResultsQuery = useQuery({
    queryKey: ['cupResults', props.id],
    queryFn: () => CupService.getResultsById(props.id, t),
})

const cupQuery = useQuery({
    queryKey: ['cups'],
    queryFn: () => cupService.getAll(t),
})

const cup = computed(() => {
    return cupQuery.data.value?.content.find((e) => e.id.toString() === props.id)
})

function navigateToList() {
    router.replace({ name: `cup-list` })
}

const queryClient = useQueryClient()

function invalidateCupPointsQuery() {
    queryClient.invalidateQueries({ queryKey: ['cupResults', props.id] })
}
function calculate() {
    CupService.calculate(props.id, t)
    invalidateCupPointsQuery()
}

const cupData = computed(() => {
    return cupResultsQuery.data.value ?? []
})
const events = computed(() => cupData.value?.events ?? [])
const raceScores = computed(() => cupData.value?.eventRacesCupScoreDto ?? [])
const overallScores = computed(() => cupData.value?.overallOrganisationScores ?? [])
</script>

<template>
    <Button
        v-tooltip="t('labels.back')"
        icon="pi pi-arrow-left"
        class="ml-2"
        :aria-label="t('labels.back')"
        severity="secondary"
        type="reset"
        outlined
        raised
        rounded
        @click="navigateToList"
    />
    <Button
        v-if="authStore.isAdmin"
        v-tooltip="t('labels.calculate')"
        icon="pi pi-calculator"
        class="ml-5"
        :aria-label="t('labels.calculate')"
        outlined
        raised
        rounded
        @click="calculate()"
    />
    <span v-if="cupResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
    <span v-else-if="cupResultsQuery.status.value === 'error'">
        {{ t('messages.error', { message: cupResultsQuery.error.toLocaleString() }) }}
    </span>
    <div v-else-if="cupResultsQuery.data" class="card flex justify-content-start">
        <div class="cup-container">
            <div id="page_header">
                <table>
                    <tr>
                        <th id="cup_name">
                            <nobr>{{ cupData.name }}</nobr>
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
                </table>
            </div>
            <div id="club_results">
                <section v-if="overallScores.length" class="">
                    <table>
                        <tr>
                            <th class="top">Verein/Klassen</th>
                            <th class="sum">Gesamt</th>
                            <th
                                v-for="cls in overallScores[0].personWithScoreDtoList"
                                :key="cls.classShortName"
                                class="cl"
                            >
                                {{ cls.classShortName }}
                            </th>
                            <th class="ev">Nikolaus-OL</th>
                            <th class="ev">Kristall-OL</th>
                            <th class="ev">Alex-OL</th>
                        </tr>
                        <tr v-for="org in overallScores" :key="org.organisation.id" class="">
                            <td class="cb">
                                {{ org.organisation.shortName }}
                            </td>
                            <td class="sum">
                                {{ org.score }}
                            </td>
                            <td
                                v-for="cls in org.personWithScoreDtoList"
                                :key="cls.classShortName"
                                class="cl"
                            >
                                {{ cls.score !== 0 ? cls.score.toString() : '' }}
                            </td>
                            <td class="ev" />
                            <td class="ev" />
                            <td class="ev" />
                        </tr>
                    </table>
                </section>

                <!-- Punktetabelle -->
                <!-- section v-if="raceScores.length" class="scores-section">
                    <h2>Punktetabelle</h2>
                    <div v-for="score in raceScores" :key="score.event.id" class="race-block">
                <h3>
                    {{ score.event.name }} ({{
                        new Date(score.event.startTime).toLocaleDateString()
                    }})
                </h3>
                <p>Status: {{ score.event.state.id }}</p>
                <p v-if="score.event.organisations.length">
                    Ausrichter:
                    {{ score.event.organisations.map((org) => org.name).join(', ') }}
                </p>

                <table class="organisation-table">
                    <thead>
                        <tr>
                            <th>Organisation</th>
                            <th>Punkte</th>
                            <th>Teilnehmer (Klasse)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="org in score.raceCupScores" :key="org.organisation?.id">
                            <td>{{ org.organisation?.name ?? '?' }}</td>
                            <td>{{ org.score }}</td>
                            <td>
                                <ul>
                                    <li
                                        v-for="person in org.personWithScoreDtoList"
                                        :key="person.personId.value"
                                    >
                                        {{ person.classShortName }}: {{ person.score }}
                                    </li>
                                </ul>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            </section>
            <p-- v-else>
                Keine Punkte verfügbar.
            </p -->
            </div>
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
