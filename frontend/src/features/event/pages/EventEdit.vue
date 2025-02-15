<script setup lang="ts">
import type { SportEvent } from '@/features/event/model/sportEvent'
import { eventService } from '@/features/event/services/event.service'
import EventForm from '@/features/event/widgets/EventForm.vue'
import GenericEdit from '@/features/generic/pages/GenericEdit.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ id: string, locale?: string }>()

const { t } = useI18n()
const authStore = useAuthStore()
const queryKey: string[] = ['event']
const entityLabel: string = 'event'
const editLabel = computed(() => t('messages.edit_entity', { entity: t('labels.event') }))
</script>

<template>
    <GenericEdit
        :entity-service="eventService"
        :query-key="queryKey"
        :entity-id="props.id"
        :entity-label="entityLabel"
        :edit-label="editLabel"
        router-prefix="event"
        :visible="authStore.isAdmin"
    >
        <template #default="{ formData }">
            <EventForm
                v-if="formData"
                v-model="formData.data"
                :event="formData.data as SportEvent"
                :entity-service="eventService"
                :query-key="queryKey"
            />
        </template>
    </GenericEdit>
</template>

<style scoped></style>
