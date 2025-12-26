<script setup lang="ts">
import type { EventCertificateStat } from '@/features/event/model/event_certificate_stat'
import type { EventCertificateStats } from '@/features/event/model/event_certificate_stats'
import { useQueryClient } from '@tanstack/vue-query'
import moment from 'moment'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { EventService } from '@/features/event/services/event.service'
import { toastDisplayDuration } from '@/utils/constants'

const props = defineProps<{ data: EventCertificateStats | undefined | null }>()

const { t } = useI18n()

const queryClient = useQueryClient()
const authStore = useAuthStore()
const toast = useToast()

function parseDateMoment(dateString: string): moment.Moment {
    return moment(dateString)
}

function formatGenerated(date: string | Date): string {
    if (typeof date === 'string')
        return parseDateMoment(date).format('DD.MM.YYYY H:mm:ss')
    else return moment(date).format('DD.MM.YYYY H:mm:ss')
}

function personNameColumn(data: EventCertificateStat): string {
    return `${data.person.givenName} ${data.person.familyName}`
}

function generatedColumn(data: EventCertificateStat): string {
    return formatGenerated(data.generated)
}

function removeEventCertificateStat(id: number) {
    EventService.removeEventCertificateStat(id, t).then(() => {
        queryClient.invalidateQueries({
            queryKey: [
                'eventCertificateStats',
                props.data?.stats.find(s => s.id === id)?.event.id,
                authStore.isAdmin,
            ],
        })
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.entity_deleted', { entity: t('labels.certificate_stat') }),
            life: toastDisplayDuration,
        })
    })
}
</script>

<template>
    <DataTable :value="props.data?.stats">
        <Column :header="t('labels.name')">
            <template #body="slotProps">
                {{ personNameColumn(slotProps.data) }}
            </template>
        </Column>
        <Column :header="t('labels.date')">
            <template #body="slotProps">
                {{ generatedColumn(slotProps.data) }}
            </template>
        </Column>
        <Column>
            <template #body="slotProps">
                <Button
                    v-tooltip="t('labels.delete')"
                    class="p-button-rounded p-button-text"
                    icon="pi pi-trash"
                    outlined
                    raised
                    rounded
                    :aria-label="t('labels.delete')"
                    @click="removeEventCertificateStat(slotProps.data.id)"
                />
            </template>
        </Column>
    </DataTable>
</template>
