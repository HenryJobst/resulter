<script setup lang="ts">
import type { SportEvent } from '@/features/event/model/sportEvent'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { eventService } from '@/features/event/services/event.service'
import EventForm from '@/features/event/widgets/EventForm.vue'
import GenericNew from '@/features/generic/pages/GenericNew.vue'

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['event']
const entityLabel: string = 'event'
const newLabel = computed(() => t('messages.new_entity', { entity: t('labels.event') }))

const localFormData = ref<SportEvent>({
    id: undefined,
    name: '',
    startTime: new Date(),
    state: { id: 'Planned' },
    organisations: [],
    certificate: null,
})
</script>

<template>
    <GenericNew
        :entity="localFormData"
        :entity-service="eventService"
        :query-key="queryKey"
        :entity-label="entityLabel"
        :new-label="newLabel"
        router-prefix="event"
        :changeable="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <EventForm
                v-if="formData"
                v-model="formData.data"
                :event="formData.data"
                :entity-service="eventService"
                :query-key="queryKey"
            />
        </template>
    </GenericNew>
</template>

<style scoped></style>
