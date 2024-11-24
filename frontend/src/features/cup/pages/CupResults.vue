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
    router.replace({ name: `event-list` })
}

const queryClient = useQueryClient()

function invalidateCupPointsQuery() {
    queryClient.invalidateQueries({ queryKey: ['cupResults', props.id] })
}
function calculate() {
    CupService.calculate(props.id, t)
    invalidateCupPointsQuery()
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
        <div class="flex flex-col flex-grow w-full">
            <h1 class="mt-3 font-extrabold">{{ cup?.name }} - {{ t('labels.results', 2) }}</h1>
        </div>
    </div>
</template>

<style scoped>
h1 {
    margin-bottom: 1rem;
}
</style>
