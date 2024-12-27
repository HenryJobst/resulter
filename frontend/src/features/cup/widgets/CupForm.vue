<script setup lang="ts">
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import MultiSelect, { type MultiSelectChangeEvent } from 'primevue/multiselect'
import { useQuery } from '@tanstack/vue-query'
import type { Cup } from '@/features/cup/model/cup'
import { CupService } from '@/features/cup/services/cup.service'
import type { EventKey } from '@/features/event/model/event_key'
import { eventService } from '@/features/event/services/event.service'

const props = defineProps<{
    cup: Cup
    entityService: CupService
    queryKey: string[]
}>()

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

const cup = computed({
    get: () => props.cup,
    set: value => emit('update:modelValue', value),
})

const l_events = computed({
    get: () => (cup.value ? cup.value.events.map(ev => ev.id) : []),
    set: (ids) => {
        if (cup.value)
            cup.value.events = getEventKeysFromIds(ids)!
    },
})

const eventQuery = useQuery({
    queryKey: ['events'],
    queryFn: () => eventService.getAllUnpaged(t),
})

const cupTypesQuery = useQuery({
    queryKey: ['cup_types'],
    queryFn: () => CupService.getCupTypes(t),
})

function getEventKeysFromIds(ids: number[]): EventKey[] | null {
    if (!eventQuery.data.value || !eventQuery.data.value)
        return null

    return ids
        .map((id) => {
            return eventQuery.data.value?.find(b => b.id === id)
        })
        .filter(ev => ev !== undefined)
        .map((ev) => {
            return {
                id: ev!.id,
                name: ev!.name,
            }
        })
}

function handleSelectionChange(ev: MultiSelectChangeEvent) {
    if (ev.value && cup.value && eventQuery.data.value && eventQuery.data.value) {
        cup.value.events = getEventKeysFromIds(ev.value)!
    }
}
</script>

<template>
    <div v-if="cup" class="flex flex-col">
        <div class="flex flex-row">
            <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
            <div class="col">
                <InputText id="name" v-model="cup.name" type="text" class="w-96" />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="year" class="col-fixed w-32">{{ t('labels.year') }}</label>
            <div class="col">
                <InputNumber
                    id="year"
                    v-model="cup.year"
                    class="w-96"
                    mode="decimal"
                    show_buttons="true"
                    :use-grouping="false"
                    :min="1970"
                    :max="9999"
                />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="type" class="col-fixed w-32">{{ t('labels.type') }}</label>
            <div class="col">
                <span v-if="cupTypesQuery.status.value === 'pending'">{{
                    t('messages.loading')
                }}</span>
                <span v-else-if="cupTypesQuery.status.value === 'error'">
                    {{ t('messages.error', { message: cupTypesQuery.error.toLocaleString() }) }}
                </span>
                <Dropdown
                    v-else-if="cupTypesQuery.data && cupTypesQuery.data.value"
                    id="type"
                    v-model="cup.type"
                    :options="cupTypesQuery.data.value"
                    option-label="id"
                    data-key="id"
                    :placeholder="t('messages.select')"
                    class="w-full md:w-14rem"
                />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="cups" class="col-fixed w-32">{{ t('labels.event', 2) }}</label>
            <div class="col">
                <span v-if="eventQuery.status.value === 'pending'">{{
                    t('messages.loading')
                }}</span>
                <span v-else-if="eventQuery.status.value === 'error'">
                    {{ t('messages.error', { message: eventQuery.error.toLocaleString() }) }}
                </span>
                <div v-else-if="eventQuery.data && eventQuery.data.value" class="card">
                    <MultiSelect
                        id="events"
                        v-model="l_events"
                        :options="eventQuery.data.value"
                        data-key="id"
                        filter
                        option-label="name"
                        option-value="id"
                        :placeholder="t('messages.select')"
                        class="w-full md:w-20rem"
                        @change="handleSelectionChange"
                    />
                </div>
            </div>
        </div>
    </div>
    <div class="mt-2">
        <slot />
    </div>
</template>

<style scoped></style>
