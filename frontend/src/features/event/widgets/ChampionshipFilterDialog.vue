<script setup lang="ts">
import type { Organisation } from '@/features/organisation/model/organisation'
import { useQuery } from '@tanstack/vue-query'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import MultiSelect from 'primevue/multiselect'
import Select from 'primevue/select'
import { useToast } from 'primevue/usetoast'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { EventService } from '@/features/event/services/event.service'
import { organisationService } from '@/features/organisation/services/organisation.service'
import { toastDisplayDuration } from '@/utils/constants'

const props = defineProps<{
    eventId: number
    visible: boolean
}>()

const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'done'): void
}>()

const { t } = useI18n()
const toast = useToast()

const selectedOrgId = ref<number | null>(null)
const excludedClasses = ref<string[]>([])
const isLoadingCleanup = ref(false)
const isLoadingRanking = ref(false)

const organisationQuery = useQuery({
    queryKey: ['organisations'],
    queryFn: () => organisationService.getAllUnpaged(t),
})

const classShortNamesQuery = useQuery({
    queryKey: ['championshipClassShortNames', props.eventId],
    queryFn: () => EventService.getChampionshipClassShortNames(props.eventId, t),
})

const hasMultipleRacesQuery = useQuery({
    queryKey: ['championshipHasMultipleRaces', props.eventId],
    queryFn: () => EventService.getChampionshipHasMultipleRaces(props.eventId, t),
})

// Filter: only NationalFederation and NationalRegion
const eligibleOrganisations = computed<Organisation[]>(() => {
    if (!organisationQuery.data.value)
        return []
    return (organisationQuery.data.value as Organisation[]).filter(
        org => org.type?.id === 'NationalFederation' || org.type?.id === 'NationalRegion',
    )
})

// Pre-select BBM if available
const defaultOrgId = computed(() => {
    const bbm = eligibleOrganisations.value.find(org =>
        org.shortName?.toUpperCase().includes('BBM') || org.name?.toUpperCase().includes('BBM'),
    )
    return bbm?.id ?? null
})

function onDialogShow() {
    selectedOrgId.value = defaultOrgId.value
    excludedClasses.value = []
}

async function handleCleanup() {
    if (!selectedOrgId.value)
        return
    isLoadingCleanup.value = true
    try {
        await EventService.applyChampionshipCleanup(props.eventId, selectedOrgId.value, excludedClasses.value, t)
        emit('done')
        emit('update:visible', false)
    }
    catch (error) {
        console.error('Championship cleanup failed:', error)
        toast.add({
            severity: 'error',
            summary: t('messages.error'),
            detail: t('messages.championship_cleanup_failed') || 'Championship cleanup failed',
            life: toastDisplayDuration,
        })
    }
    finally {
        isLoadingCleanup.value = false
    }
}

async function handleRanking() {
    if (!selectedOrgId.value)
        return
    isLoadingRanking.value = true
    try {
        await EventService.addChampionshipRanking(props.eventId, selectedOrgId.value, excludedClasses.value, t)
        emit('done')
        emit('update:visible', false)
    }
    catch (error) {
        console.error('Championship ranking failed:', error)
        toast.add({
            severity: 'error',
            summary: t('messages.error'),
            detail: t('messages.championship_ranking_failed') || 'Championship ranking failed',
            life: toastDisplayDuration,
        })
    }
    finally {
        isLoadingRanking.value = false
    }
}
</script>

<template>
    <Dialog
        :visible="props.visible"
        :header="t('messages.championship_filter_title')"
        modal
        dismissable-mask
        :style="{ width: '35rem' }"
        :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
        @update:visible="emit('update:visible', $event)"
        @show="onDialogShow"
    >
        <div class="flex flex-col gap-4">
            <div class="flex flex-col gap-2">
                <label for="baseOrg">{{ t('messages.championship_filter_base_org') }}</label>
                <Select
                    id="baseOrg"
                    v-model="selectedOrgId"
                    :options="eligibleOrganisations"
                    option-label="name"
                    option-value="id"
                    :placeholder="t('messages.championship_filter_select_placeholder')"
                    :loading="organisationQuery.isPending.value"
                    class="w-full"
                    filter
                />
            </div>
            <div class="flex flex-col gap-2">
                <label for="excludeClasses">{{ t('messages.championship_filter_exclude_classes') }}</label>
                <MultiSelect
                    id="excludeClasses"
                    v-model="excludedClasses"
                    :options="classShortNamesQuery.data.value ?? []"
                    :placeholder="t('messages.championship_filter_exclude_classes_placeholder')"
                    :loading="classShortNamesQuery.isPending.value"
                    class="w-full"
                    filter
                    display="chip"
                />
            </div>
        </div>

        <template #footer>
            <Button
                v-tooltip="t('messages.championship_cleanup_tooltip')"
                :label="t('messages.championship_cleanup_label')"
                icon="pi pi-filter-slash"
                severity="warning"
                :disabled="!selectedOrgId"
                :loading="isLoadingCleanup"
                @click="handleCleanup"
            />
            <Button
                v-tooltip="t('messages.championship_ranking_tooltip')"
                :label="t('messages.championship_ranking_label')"
                icon="pi pi-list"
                :disabled="!selectedOrgId || !!hasMultipleRacesQuery.data.value"
                :loading="isLoadingRanking"
                @click="handleRanking"
            />
        </template>
    </Dialog>
</template>
