<script setup lang="ts">
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed } from 'vue'
import { CupService } from '@/features/cup/services/cup.service'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

const props = defineProps<{ id: string }>()

const { t } = useI18n()

const authStore = useAuthStore()
const router = useRouter()

const cupResultsQuery = useQuery({
    queryKey: ['cupResults', props.id],
    queryFn: () => CupService.getResultsById(props.id, t),
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

const raceScores = computed(() => cupData.value?.eventRacesCupScoreDto ?? [])
const overallScores = computed(() => cupData.value?.overallOrganisationScores ?? [])

function findOrganisationScore(org: any, raceCupScores: any): string {
    const entry = raceCupScores.organisationScoreDtoList.find(
        os => os.organisation.id === org.organisation.id,
    )
    const score = entry?.score ?? 0.0
    return score !== 0.0 ? score.toString() : ''
}
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
                    <thead>
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
                                    v-for="cls in overallScores[0].personWithScoreDtoList"
                                    :key="cls.classShortName"
                                    class="cl"
                                >
                                    {{ cls.classShortName }}
                                </th>
                                <th
                                    v-for="r in raceScores[0].raceCupScores"
                                    :key="r.raceDto.id"
                                    class="ev"
                                >
                                    {{ r.raceDto.name }}
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
                                    v-for="cls in org.personWithScoreDtoList"
                                    :key="cls.classShortName"
                                    class="cl"
                                >
                                    {{ cls.score !== 0 ? cls.score.toString() : '' }}
                                </td>
                                <td
                                    v-for="r in raceScores[0].raceCupScores"
                                    :key="r.raceDto.id"
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
