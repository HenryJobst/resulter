<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed, ref, toRef } from 'vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { SportEvent } from '@/features/event/model/sportEvent'
import { EventService, eventService } from '@/features/event/services/event.service'
import GenericNewFormKit from '@/features/generic/pages/GenericNewFormKit.vue'
import { useQuery } from '@tanstack/vue-query'

const { t, locale } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['events']
const entityLabel: string = 'event'
const newLabel = computed(() => t('messages.new_entity', { entity: t('labels.event') }))

const sportEvent = ref<SportEvent | Omit<SportEvent, 'id'>>({
    name: '',
    startTime: '',
    state: { id: 'Planned' },
    organisations: [],
    certificate: null
})

const eventStatusQuery = useQuery({
    queryKey: ['event_status'],
    queryFn: () => EventService.getEventStatus(t)
})

const localizedEventStatusOptions = computed(() => {
    if (eventStatusQuery.data.value) {
        return eventStatusQuery.data.value.map(option => ({
            ...option,
            label: t(`event_state.${option.id.toLocaleUpperCase()}`)
        }))
    }
    return []
})

const schema = ref(
        [
            {
                $formkit: 'primeInputText',
                name: 'name',
                label: toRef(() => t('labels.name')),
                validation: 'required'
            },
            {
                $formkit: 'primeCalendar',
                name: 'startTime',
                label: toRef(() => t('labels.date')),
                validation: '',
                showIcon: true,
                showTime: true
            },
            {
                $formkit: 'primeDropdown',
                name: 'state',
                label: toRef(() => t('labels.state')),
                optionLabel: 'label',
                optionValue: 'id',
                options: localizedEventStatusOptions,
                placeholder: toRef(() => t('messages.select'))
            }
        ]
)
</script>

<template>
    <GenericNewFormKit
            v-model="sportEvent"
            :schema="schema"
            :entity-service="eventService"
            :query-key="queryKey"
            :entity-label="entityLabel"
            :new-label="newLabel"
            router-prefix="event"
            :changeable="authStore.isAdmin"
    >
    </GenericNewFormKit>
</template>

<style scoped></style>
