<script setup lang="ts">
import type { Ref } from 'vue'
import type { AggregatedPersonScores } from '@/features/cup/model/aggregated_person_scores'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import Button from 'primevue/button'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import FogCupResults from '@/features/cup/pages/FogCupResults.vue'
import KjPokalResults from '@/features/cup/pages/KjPokalResults.vue'
import KristallCupResults from '@/features/cup/pages/KristallCupResults.vue'
import NorCupResults from '@/features/cup/pages/NorCupResults.vue'
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
    return cupResultsQuery.data.value
})

const eventRacesCupScores: Ref<EventRacesCupScore[]> = computed(
    () => cupData.value?.eventRacesCupScores ?? [],
)
const overallOrganisationScores: Ref<OrganisationScore[]> = computed(
    () => cupData.value?.overallOrganisationScores ?? [],
)
const aggregatedPersonScores: Ref<AggregatedPersonScores[]> = computed(
    () => cupData.value?.aggregatedPersonScores ?? [],
)
</script>

<template>
    <div class="mb-3">
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
    </div>
    <span v-if="cupResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
    <div v-else-if="cupData" class="card flex justify-content-start ml-3">
        <KristallCupResults
            v-if="cupData.type?.id === 'KRISTALL'"
            :cup-name="cupData.name"
            :overall-scores="overallOrganisationScores"
            :event-races-cup-scores="eventRacesCupScores"
        />
        <FogCupResults
            v-if="cupData.type?.id === 'NEBEL'"
            :cup-name="cupData.name"
            :overall-scores="overallOrganisationScores"
            :event-races-cup-score="eventRacesCupScores"
            :persons="cupData.persons"
        />
        <KjPokalResults
            v-if="cupData.type?.id === 'KJ'"
            :cup-name="cupData.name"
            :overall-scores="overallOrganisationScores"
            :event-races-cup-scores="eventRacesCupScores"
            :persons="cupData.persons"
        />
        <NorCupResults
            v-if="cupData.type?.id === 'NOR'"
            :cup-name="cupData.name"
            :overall-scores="overallOrganisationScores"
            :event-races-cup-scores="eventRacesCupScores"
            :aggregated-person-scores="aggregatedPersonScores"
            :persons="cupData.persons"
        />
    </div>
</template>

<style scoped></style>
