<script setup lang="ts">
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import type { Ref } from 'vue'
import { computed } from 'vue'
import { CupService } from '@/features/cup/services/cup.service'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import KristallCupResults from '@/features/cup/pages/KristallCupResults.vue'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import FogCupResults from '@/features/cup/pages/FogCupResults.vue'
import NorCupResults from '@/features/cup/pages/NorCupResults.vue'
import type { AggregatedPersonScores } from '@/features/cup/model/aggregated_person_scores'

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
    <span v-else-if="cupResultsQuery.status.value === 'error'">
        {{ t('messages.error', { message: cupResultsQuery.error.toLocaleString() }) }}
    </span>
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
            :event-races-cup-scores="eventRacesCupScores"
        />
        <NorCupResults
            v-if="cupData.type?.id === 'NOR'"
            :cup-name="cupData.name"
            :overall-scores="overallOrganisationScores"
            :event-races-cup-scores="eventRacesCupScores"
            :aggregated-person-scores="aggregatedPersonScores"
        />
    </div>
</template>

<style scoped></style>
